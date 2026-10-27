package com.example.tamilstream.ui.components

import android.content.Intent
import android.net.Uri
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tamilstream.data.model.AdConfig
import com.example.tamilstream.ui.theme.Amber500
import com.example.tamilstream.ui.theme.DarkSurface
import com.example.tamilstream.ui.theme.DarkSurfaceElevated
import com.example.tamilstream.ui.theme.LiveRed
import com.example.tamilstream.ui.theme.TextMuted

/**
 * Normalizes raw HTML advertisement code by converting protocol-relative URLs (//)
 * to explicit https:// links so Adsterra and third-party ad scripts load reliably.
 */
fun normalizeAdHtml(rawHtml: String): String {
    return rawHtml
        .replace("src=\"//", "src=\"https://")
        .replace("src='//", "src='https://")
        .replace("href=\"//", "href=\"https://")
        .replace("href='//", "href='https://")
}

/**
 * Production-ready Global HTML Advertisement Banner & Preview Component.
 *
 * Supports Adsterra, Google Publisher Tags, and external iframe/script ad units:
 * - Proper WebResourceRequest handling in shouldOverrideUrlLoading so subframes and invoke.js load internally.
 * - Dynamic viewport scaling and protocol normalization.
 * - Diagnostic loading state and callback hooks for Admin preview monitoring.
 * - Sandboxed execution with safe external link redirection on user gestures.
 */
@Composable
fun AdBannerView(
    adConfig: AdConfig,
    modifier: Modifier = Modifier,
    isPreviewMode: Boolean = false,
    customHtmlCode: String? = null,
    placementTag: String = "ad_banner",
    refreshKey: Int = 0,
    onAdStatusChanged: ((isLoaded: Boolean, errorMsg: String?) -> Unit)? = null
) {
    val rawHtmlToRender = customHtmlCode ?: adConfig.htmlContent
    val isEnabled = if (customHtmlCode != null) true else adConfig.enabled

    if (!isEnabled || rawHtmlToRender.isBlank()) {
        Box(modifier = modifier.height(0.dp))
        return
    }

    val context = LocalContext.current
    val normalizedCode = remember(rawHtmlToRender) { normalizeAdHtml(rawHtmlToRender) }

    var isLoading by remember(normalizedCode, refreshKey) { mutableStateOf(true) }
    var errorMessage by remember(normalizedCode, refreshKey) { mutableStateOf<String?>(null) }

    val wrappedHtml = remember(normalizedCode) {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * {
              box-sizing: border-box;
              margin: 0;
              padding: 0;
            }
            html, body {
              width: 100%;
              height: 100%;
              background-color: transparent;
              overflow: hidden;
              display: flex;
              align-items: center;
              justify-content: center;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            }
            iframe {
              border: 0;
              margin: 0;
              padding: 0;
            }
            img, iframe, video {
              max-width: 100%;
              max-height: 100%;
              object-fit: contain;
            }
          </style>
        </head>
        <body>
          $normalizedCode
        </body>
        </html>
        """.trimIndent()
    }

    val tagWithKey = remember(wrappedHtml, refreshKey) { "$wrappedHtml#rk=$refreshKey" }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 728.dp)
                .fillMaxWidth()
                .heightIn(min = 90.dp, max = 220.dp)
                .aspectRatio(8.088889f) // 728:90 target aspect ratio
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .border(
                    width = 1.dp,
                    color = when {
                        errorMessage != null -> LiveRed.copy(alpha = 0.6f)
                        isPreviewMode -> Amber500.copy(alpha = 0.5f)
                        else -> Color.White.copy(alpha = 0.08f)
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .testTag(placementTag),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            javaScriptCanOpenWindowsAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = false
                            allowContentAccess = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                safeBrowsingEnabled = false
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                // Capture JavaScript errors or warnings for diagnostics
                                if (consoleMessage != null && consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                    val err = "JS Error: ${consoleMessage.message()} (Line ${consoleMessage.lineNumber()})"
                                    errorMessage = err
                                    onAdStatusChanged?.invoke(false, err)
                                }
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val uri = request?.url ?: return false
                                val isMainFrame = request.isForMainFrame
                                val hasGesture = request.hasGesture()

                                // Only intercept top-level user tap clicks to open in browser
                                if (isMainFrame && hasGesture) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        ctx.startActivity(intent)
                                    } catch (_: Exception) {}
                                    return true
                                }
                                // Allow internal script redirects, iframes, and subresources to load inside WebView
                                return false
                            }

                            @Suppress("DEPRECATION")
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                if (errorMessage == null) {
                                    onAdStatusChanged?.invoke(true, null)
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    val description = error?.description?.toString() ?: "Resource failed to load"
                                    errorMessage = description
                                    isLoading = false
                                    onAdStatusChanged?.invoke(false, description)
                                }
                            }

                            @Suppress("DEPRECATION")
                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                isLoading = false
                            }
                        }

                        tag = tagWithKey
                        loadDataWithBaseURL(
                            "https://bancadeltempoidea.org",
                            wrappedHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                },
                update = { webView ->
                    if (webView.tag != tagWithKey) {
                        webView.tag = tagWithKey
                        isLoading = true
                        errorMessage = null
                        webView.loadDataWithBaseURL(
                            "https://bancadeltempoidea.org",
                            wrappedHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading state overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkSurfaceElevated.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Amber500,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Initializing Adsterra Script...",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Error diagnostic overlay
            if (errorMessage != null && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkSurfaceElevated.copy(alpha = 0.9f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ad Diagnostic: $errorMessage",
                        color = LiveRed,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
