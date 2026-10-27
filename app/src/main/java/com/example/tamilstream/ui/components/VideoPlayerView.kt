package com.example.tamilstream.ui.components

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.tamilstream.data.model.PlaybackDiagnostic
import com.example.tamilstream.data.model.PlaybackItem
import com.example.tamilstream.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private const val CHROME_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 TamilStream/1.0"

@OptIn(UnstableApi::class)
private fun createExoPlayerInstance(context: Context): ExoPlayer {
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent(CHROME_USER_AGENT)
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(20000)
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(
            mapOf(
                "Accept" to "*/*",
                "Accept-Encoding" to "gzip, deflate"
            )
        )

    val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
    val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

    val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs = */ 15_000,
            /* maxBufferMs = */ 50_000,
            /* bufferForPlaybackMs = */ 2_500,
            /* bufferForPlaybackAfterRebufferMs = */ 4_000
        )
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()

    val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .setUsage(C.USAGE_MEDIA)
        .build()

    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .setLoadControl(loadControl)
        .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
        .build()
}

private fun buildMediaItem(url: String, id: String): MediaItem {
    val lower = url.lowercase()
    val builder = MediaItem.Builder()
        .setUri(url)
        .setMediaId(id)

    // Detect format mime-type so Media3 uses the correct MediaSource (HLS, DASH, MP4)
    when {
        lower.contains(".m3u8") -> builder.setMimeType(MimeTypes.APPLICATION_M3U8)
        lower.contains(".mpd") -> builder.setMimeType(MimeTypes.APPLICATION_MPD)
        lower.contains(".mp4") -> builder.setMimeType(MimeTypes.VIDEO_MP4)
    }

    builder.setLiveConfiguration(
        MediaItem.LiveConfiguration.Builder()
            .setMaxPlaybackSpeed(1.02f)
            .setMinPlaybackSpeed(0.98f)
            .build()
    )

    return builder.build()
}

private fun sanitizeHost(rawUrl: String): String {
    return try {
        val uri = Uri.parse(rawUrl)
        uri.host ?: "live-stream"
    } catch (_: Exception) {
        "broadcast-host"
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    playbackItem: PlaybackItem,
    isPlaying: Boolean,
    isMuted: Boolean,
    isFullScreen: Boolean,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleFullScreen: () -> Unit,
    onClose: () -> Unit,
    onNextChannel: (() -> Unit)? = null,
    onPreviousChannel: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onRecordDiagnostic: ((PlaybackDiagnostic) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) { context.findActivity() }

    // Intercept Back button when in fullscreen to return to portrait layout first
    BackHandler(enabled = isFullScreen) {
        onToggleFullScreen()
    }

    // Dynamic orientation and immersive status/navigation bar controller
    LaunchedEffect(isFullScreen, activity) {
        val act = activity ?: return@LaunchedEffect
        val window = act.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        if (isFullScreen) {
            // Immediately request landscape orientation
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            // Hide system bars (status & navigation) with sticky swipe behavior
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            // Restore portrait orientation and normal system bars
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Keep screen active during playback
    LaunchedEffect(isPlaying, activity) {
        val window = activity?.window ?: return@LaunchedEffect
        if (isPlaying) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Clean up orientation and system bars on exit or when navigating away
    DisposableEffect(activity) {
        val act = activity ?: return@DisposableEffect onDispose {}
        val window = act.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        onDispose {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasRenderedFirstFrame by remember { mutableStateOf(false) }

    // Active URL track (support switching to fallback stream)
    var currentStreamUrl by remember(playbackItem.id) { mutableStateOf(playbackItem.streamUrl) }
    var isUsingFallback by remember(playbackItem.id) { mutableStateOf(false) }

    // Diagnostic state
    var playbackError by remember(playbackItem.id) { mutableStateOf<String?>(null) }
    var errorHttpStatus by remember(playbackItem.id) { mutableStateOf<Int?>(null) }
    var rawErrorCode by remember(playbackItem.id) { mutableStateOf<String?>(null) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }

    // Auto-hide controls after 4 seconds of active playback
    LaunchedEffect(showControls, isPlaying, playbackError) {
        if (showControls && isPlaying && playbackError == null) {
            delay(4000)
            showControls = false
        }
    }

    // ExoPlayer single instance per playback
    val exoPlayer = remember { createExoPlayerInstance(context) }

    // Load stream whenever currentStreamUrl changes
    LaunchedEffect(currentStreamUrl) {
        hasRenderedFirstFrame = false
        isBuffering = true
        playbackError = null
        errorHttpStatus = null

        val mediaItem = buildMediaItem(currentStreamUrl, playbackItem.id)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isPlaying
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // React to play/pause updates
    LaunchedEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
    }

    // React to mute/unmute
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    // ExoPlayer event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        playbackError = null
                        retryCount = 0
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                    }
                    Player.STATE_IDLE -> {
                        // Player was reset or experienced an error
                    }
                }
            }

            override fun onRenderedFirstFrame() {
                hasRenderedFirstFrame = true
                isBuffering = false
                playbackError = null
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                hasRenderedFirstFrame = false
                val host = sanitizeHost(currentStreamUrl)
                var httpCode: Int? = null

                val rootCause = error.cause
                val readableMessage = when {
                    rootCause is HttpDataSource.InvalidResponseCodeException -> {
                        httpCode = rootCause.responseCode
                        when (rootCause.responseCode) {
                            401, 403 -> "Stream access denied or token expired (HTTP ${rootCause.responseCode})"
                            404 -> "Stream playlist not found on broadcaster server (HTTP 404)"
                            in 500..599 -> "Broadcaster CDN is currently offline (HTTP ${rootCause.responseCode})"
                            else -> "Broadcaster returned error HTTP ${rootCause.responseCode}"
                        }
                    }
                    rootCause is HttpDataSource.CleartextNotPermittedException -> {
                        "Cleartext HTTP stream blocked by security policy"
                    }
                    rootCause is HttpDataSource.HttpDataSourceException -> {
                        val cause = rootCause.cause
                        when (cause) {
                            is UnknownHostException -> "DNS lookup failed: host '$host' not found"
                            is ConnectException -> "Connection failed: host '$host' refused connection"
                            else -> "Network connection interrupted while streaming"
                        }
                    }
                    error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ||
                    error.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> {
                        "Malformed broadcast stream manifest ($host)"
                    }
                    error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {
                        "Device decoder incompatible with this stream's video codec"
                    }
                    error.errorCode == PlaybackException.ERROR_CODE_TIMEOUT -> {
                        "Stream connection timed out"
                    }
                    else -> {
                        error.message ?: "Playback error: ${error.errorCodeName}"
                    }
                }

                playbackError = readableMessage
                errorHttpStatus = httpCode
                rawErrorCode = error.errorCodeName

                // Record diagnostic for Admin portal inspection
                onRecordDiagnostic?.invoke(
                    PlaybackDiagnostic(
                        channelId = playbackItem.id,
                        channelName = playbackItem.title,
                        host = host,
                        protocol = if (currentStreamUrl.startsWith("https", ignoreCase = true)) "HTTPS" else "HTTP",
                        errorCode = error.errorCodeName,
                        errorMessage = readableMessage,
                        httpStatus = httpCode
                    )
                )

                // Automatic fallback attempt once if fallback stream is configured
                if (!isUsingFallback && !playbackItem.fallbackUrl.isNullOrBlank() && playbackItem.fallbackUrl != currentStreamUrl) {
                    coroutineScope.launch {
                        delay(600)
                        isUsingFallback = true
                        currentStreamUrl = playbackItem.fallbackUrl
                    }
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Sync with Android Lifecycle (pause when app backgrounded, resume when foregrounded)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying && playbackError == null) {
                        exoPlayer.playWhenReady = true
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .testTag("video_player_container")
            .then(
                if (isFullScreen) Modifier.fillMaxSize()
                else Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            )
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        // Video Render Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            },
            modifier = Modifier.fillMaxSize()
        )

        // Poster Placeholder (visible before first frame renders or during error)
        if (!hasRenderedFirstFrame || playbackError != null) {
            AsyncImage(
                model = playbackItem.thumbnail,
                contentDescription = playbackItem.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Buffering progress spinner
            if (isBuffering && playbackError == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Amber500,
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = if (isUsingFallback) "Connecting to backup stream..." else "Tuning live broadcast...",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Error & Recovery Overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Amber500,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Stream Unavailable",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = playbackError ?: "The broadcaster stream is currently unreachable.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        if (isUsingFallback) {
                            Surface(
                                color = Amber500.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Backup authorized stream also tested",
                                    color = Amber500,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action Buttons: Retry, Diagnostic Info, Back to Channels
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (retryCount < 5) {
                                        retryCount++
                                        playbackError = null
                                        isBuffering = true
                                        exoPlayer.setMediaItem(buildMediaItem(currentStreamUrl, playbackItem.id))
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                        onRetry?.invoke()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Amber500,
                                    contentColor = ObsidianBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("stream_retry_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry Stream", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Diagnostics toggle button
                            OutlinedButton(
                                onClick = { showDiagnosticDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Diagnostics", fontSize = 11.sp, color = TextSecondary)
                            }

                            // Close / Back button
                            TextButton(
                                onClick = onClose,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Back", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Diagnostics Dialog
        if (showDiagnosticDialog) {
            AlertDialog(
                onDismissRequest = { showDiagnosticDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = Amber500)
                        Text("Stream Playback Diagnostics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Channel: ${playbackItem.title}", fontSize = 12.sp, color = TextPrimary)
                        Text("Target Host: ${sanitizeHost(currentStreamUrl)}", fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                        Text("Protocol: ${if (currentStreamUrl.startsWith("https")) "HTTPS (TLS)" else "HTTP"}", fontSize = 12.sp, color = TextSecondary)
                        Text("HTTP Code: ${errorHttpStatus?.toString() ?: "No HTTP response / Network err"}", fontSize = 12.sp, color = if (errorHttpStatus == 200) LiveGreen else Amber500)
                        Text("ExoPlayer Code: ${rawErrorCode ?: "NONE"}", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                        Text("Active Stream: ${if (isUsingFallback) "Backup stream" else "Primary stream"}", fontSize = 12.sp, color = TextPrimary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDiagnosticDialog = false }) {
                        Text("Close", color = Amber500)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Interactive Player Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.80f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .then(
                        if (isFullScreen) {
                            Modifier
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        } else {
                            Modifier.padding(12.dp)
                        }
                    )
            ) {
                // Top bar: Stream info, live indicator, and close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (playbackItem.isLive) {
                            Surface(
                                color = LiveRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = playbackItem.title,
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${playbackItem.category} • ${sanitizeHost(currentStreamUrl)}",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("player_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Player",
                            tint = Color.White
                        )
                    }
                }

                // Center Controls: Prev Channel, Play/Pause, Next Channel
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (onPreviousChannel != null && playbackItem.isLive) {
                        IconButton(
                            onClick = onPreviousChannel,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .testTag("player_prev_channel_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Channel",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Center Play/Pause button
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Amber500)
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    if (onNextChannel != null && playbackItem.isLive) {
                        IconButton(
                            onClick = onNextChannel,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .testTag("player_next_channel_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Channel",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Bottom bar controls: Mute, Duration/Status, Fullscreen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier.testTag("player_mute_button")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = if (playbackItem.isLive) "LIVE BROADCAST" else "${playbackItem.durationMinutes} min",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = onToggleFullScreen,
                        modifier = Modifier.testTag("player_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
