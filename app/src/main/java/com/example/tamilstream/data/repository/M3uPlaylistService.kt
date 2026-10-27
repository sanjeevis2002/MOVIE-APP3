package com.example.tamilstream.data.repository

import com.example.tamilstream.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

data class M3uParseResult(
    val channels: List<Channel>,
    val totalFound: Int,
    val validCount: Int,
    val errorMessage: String? = null,
    val httpStatusCode: Int = 200
)

data class StreamConnectivityResult(
    val isReachable: Boolean,
    val statusCode: Int,
    val statusMessage: String,
    val contentType: String?,
    val latencyMs: Long,
    val finalUrl: String
)

object M3uPlaylistService {

    const val DEFAULT_M3U_URL = "https://iptv-org.github.io/iptv/languages/tam.m3u"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 TamilStream/1.0"

    /**
     * Downloads and parses an M3U/M3U8 playlist with full support for:
     * - UTF-8 BOM removal
     * - CRLF / LF line endings
     * - Quoted attribute parsing (tvg-id, tvg-name, tvg-logo, group-title)
     * - Intermediary #EXTVLCOPT comments
     * - Strict HTTP/HTTPS URL validation
     * - Safe HTTP redirect following (up to 5 hops)
     */
    suspend fun fetchAndParseM3uDetailed(playlistUrl: String = DEFAULT_M3U_URL): M3uParseResult = withContext(Dispatchers.IO) {
        val parsedChannels = mutableListOf<Channel>()
        var currentUrl = playlistUrl
        var redirects = 0
        val maxRedirects = 5
        var finalConnection: HttpURLConnection? = null

        try {
            while (redirects < maxRedirects) {
                val url = URL(currentUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 15000
                conn.readTimeout = 20000
                conn.instanceFollowRedirects = false
                conn.setRequestProperty("User-Agent", USER_AGENT)
                conn.setRequestProperty("Accept", "text/plain, application/x-mpegurl, */*")
                conn.connect()

                val status = conn.responseCode
                if (status in 300..399) {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (location.isNullOrBlank()) {
                        return@withContext M3uParseResult(emptyList(), 0, 0, "Redirect with missing Location header", status)
                    }
                    currentUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                        location
                    } else {
                        URL(url, location).toString()
                    }
                    redirects++
                } else if (status == HttpURLConnection.HTTP_OK) {
                    finalConnection = conn
                    break
                } else {
                    conn.disconnect()
                    return@withContext M3uParseResult(emptyList(), 0, 0, "HTTP error $status: ${conn.responseMessage}", status)
                }
            }

            if (finalConnection == null) {
                return@withContext M3uParseResult(emptyList(), 0, 0, "Exceeded maximum redirects ($maxRedirects)", 310)
            }

            val inputStream = finalConnection.inputStream
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var line: String?
                var currentExtInf: String? = null
                var totalEntries = 0

                val idPattern = Pattern.compile("tvg-id=\"([^\"]*)\"")
                val namePattern = Pattern.compile("tvg-name=\"([^\"]*)\"")
                val logoPattern = Pattern.compile("tvg-logo=\"([^\"]*)\"")
                val groupPattern = Pattern.compile("group-title=\"([^\"]*)\"")

                while (reader.readLine().also { line = it } != null) {
                    var trimmed = line?.trim() ?: continue
                    if (trimmed.isEmpty()) continue

                    // Strip UTF-8 BOM if present on initial lines
                    if (trimmed.startsWith("\uFEFF")) {
                        trimmed = trimmed.substring(1).trim()
                    }

                    if (trimmed.startsWith("#EXTM3U")) {
                        continue
                    }

                    if (trimmed.startsWith("#EXTINF:")) {
                        currentExtInf = trimmed
                        totalEntries++
                        continue
                    }

                    // Ignore other comments (#EXTVLCOPT, #EXTGRP, etc.) without losing currentExtInf
                    if (trimmed.startsWith("#")) {
                        continue
                    }

                    // A stream URL candidate line
                    if (currentExtInf != null) {
                        val streamUrl = trimmed

                        // Validate URL protocol and syntax
                        if (streamUrl.startsWith("http://", ignoreCase = true) || streamUrl.startsWith("https://", ignoreCase = true)) {
                            // Extract tvg metadata
                            val idMatcher = idPattern.matcher(currentExtInf)
                            val tvgId = if (idMatcher.find()) idMatcher.group(1)?.trim() ?: "" else ""

                            val tvgNameMatcher = namePattern.matcher(currentExtInf)
                            val tvgName = if (tvgNameMatcher.find()) tvgNameMatcher.group(1)?.trim() ?: "" else ""

                            val logoMatcher = logoPattern.matcher(currentExtInf)
                            val logoUrl = if (logoMatcher.find()) logoMatcher.group(1)?.trim() ?: "" else "https://i.imgur.com/zDiIhdN.png"

                            val groupMatcher = groupPattern.matcher(currentExtInf)
                            var groupTitle = if (groupMatcher.find()) groupMatcher.group(1)?.trim() ?: "General" else "General"
                            if (groupTitle.contains(";")) {
                                groupTitle = groupTitle.split(";")[0].trim()
                            }
                            if (groupTitle.isEmpty()) groupTitle = "General"

                            // Extract Channel Name from after the last comma
                            val commaIdx = currentExtInf.lastIndexOf(',')
                            val rawName = if (commaIdx != -1) currentExtInf.substring(commaIdx + 1).trim() else (tvgName.ifEmpty { "Tamil Channel" })

                            // Clean decorative tags (e.g., "(720p)", "[Geo-blocked]")
                            var cleanName = rawName.replace(Regex("\\s*\\(.*?\\)"), "").trim()
                            cleanName = cleanName.replace(Regex("\\s*\\[.*?\\]"), "").trim()

                            // Extract Resolution
                            val res = when {
                                rawName.contains("1080p", ignoreCase = true) -> "1080P"
                                rawName.contains("720p", ignoreCase = true) -> "720P"
                                rawName.contains("576p", ignoreCase = true) -> "576P"
                                rawName.contains("480p", ignoreCase = true) -> "480P"
                                else -> "1080P"
                            }

                            // Generate clean unique identifier
                            val channelKey = if (tvgId.isNotEmpty()) {
                                tvgId.lowercase().replace(Regex("[^a-z0-9_]"), "_")
                            } else {
                                "ch_" + streamUrl.hashCode().toString().replace("-", "n")
                            }

                            parsedChannels.add(
                                Channel(
                                    id = channelKey,
                                    name = rawName,
                                    cleanName = if (cleanName.isNotEmpty()) cleanName else rawName,
                                    tvgName = tvgName,
                                    resolution = res,
                                    logo = if (logoUrl.isNotEmpty()) logoUrl else "https://i.imgur.com/zDiIhdN.png",
                                    group = groupTitle.replaceFirstChar { it.uppercase() },
                                    url = streamUrl,
                                    enabled = true,
                                    isFeatured = parsedChannels.size < 6
                                )
                            )
                        }

                        // Reset after associating with URL
                        currentExtInf = null
                    }
                }

                return@withContext M3uParseResult(
                    channels = parsedChannels,
                    totalFound = totalEntries,
                    validCount = parsedChannels.size,
                    errorMessage = null,
                    httpStatusCode = 200
                )
            }
        } catch (e: Exception) {
            return@withContext M3uParseResult(
                channels = emptyList(),
                totalFound = 0,
                validCount = 0,
                errorMessage = e.message ?: "Failed to connect to playlist source"
            )
        } finally {
            finalConnection?.disconnect()
        }
    }

    /**
     * Backward-compatible helper method returning list of channels.
     */
    suspend fun fetchAndParseM3u(playlistUrl: String = DEFAULT_M3U_URL): List<Channel> {
        val result = fetchAndParseM3uDetailed(playlistUrl)
        return result.channels
    }

    /**
     * Performs a non-destructive connectivity check against a stream URL for the Admin Diagnostic tool.
     * Validates headers, status code, latency, and content-type without downloading media packets.
     */
    suspend fun testStreamConnectivity(streamUrl: String): StreamConnectivityResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        var conn: HttpURLConnection? = null
        try {
            val url = URL(streamUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.setRequestProperty("Accept", "*/*")
            conn.setRequestProperty("Range", "bytes=0-1024")
            conn.connect()

            val code = conn.responseCode
            val msg = conn.responseMessage ?: "OK"
            val contentType = conn.contentType
            val latency = System.currentTimeMillis() - start

            StreamConnectivityResult(
                isReachable = code in 200..399,
                statusCode = code,
                statusMessage = msg,
                contentType = contentType,
                latencyMs = latency,
                finalUrl = conn.url.toString()
            )
        } catch (e: Exception) {
            StreamConnectivityResult(
                isReachable = false,
                statusCode = 0,
                statusMessage = e.message ?: "Connection failed",
                contentType = null,
                latencyMs = System.currentTimeMillis() - start,
                finalUrl = streamUrl
            )
        } finally {
            conn?.disconnect()
        }
    }
}
