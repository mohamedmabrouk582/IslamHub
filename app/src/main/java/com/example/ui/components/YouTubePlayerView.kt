package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerView(
    videoId: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onVideoEnded: () -> Unit = {},
    onPlaybackChanged: (Boolean) -> Unit = {}
) {
    val safeVideoId = remember(videoId) {
        videoId.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
    }
    var isPlayerReady by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentLoadedVideoId by remember { mutableStateOf("") }
    var hasRenderError by remember { mutableStateOf(false) }
    var showLoadingSpinner by remember { mutableStateOf(true) }
    var retryNonce by remember { mutableIntStateOf(0) }

    LaunchedEffect(safeVideoId, retryNonce, hasRenderError) {
        if (!hasRenderError && !isPlayerReady) {
            showLoadingSpinner = true
            delay(8_000)
            showLoadingSpinner = false
        } else {
            showLoadingSpinner = false
        }
    }

    // When isPlaying toggle changes, control video via JS
    LaunchedEffect(isPlaying, isPlayerReady) {
        if (isPlayerReady && webViewRef != null) {
            val js = if (isPlaying) "playVideo();" else "pauseVideo();"
            webViewRef?.evaluateJavascript(js, null)
        }
    }

    // When videoId changes, load the new video
    LaunchedEffect(safeVideoId, isPlayerReady) {
        if (isPlayerReady && webViewRef != null && safeVideoId.isNotEmpty() && safeVideoId != currentLoadedVideoId) {
            currentLoadedVideoId = safeVideoId
            webViewRef?.evaluateJavascript("loadVideo('$safeVideoId');", null)
        }
    }

    val context = LocalContext.current

    fun openExternally() {
        val watchIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=$safeVideoId")
        )
        runCatching { context.startActivity(watchIntent) }.onFailure {
            Toast.makeText(context, "تعذّر فتح تطبيق YouTube", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (!hasRenderError && safeVideoId.isNotEmpty()) {
            key(retryNonce) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { viewContext ->
                        try {
                            WebView(viewContext).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.BLACK)

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    mediaPlaybackRequiresUserGesture = false
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                }

                                webChromeClient = WebChromeClient()

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        return false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        if (request?.isForMainFrame == true) {
                                            hasRenderError = true
                                            isPlayerReady = false
                                        }
                                    }

                                    override fun onRenderProcessGone(
                                        view: WebView?,
                                        detail: RenderProcessGoneDetail?
                                    ): Boolean {
                                        hasRenderError = true
                                        isPlayerReady = false
                                        webViewRef = null
                                        (view?.parent as? ViewGroup)?.removeView(view)
                                        runCatching { view?.destroy() }
                                        return true
                                    }
                                }

                                class AndroidBridge {
                                    @JavascriptInterface
                                    fun onReady() {
                                        post {
                                            isPlayerReady = true
                                            showLoadingSpinner = false
                                            currentLoadedVideoId = safeVideoId
                                            onPlaybackChanged(true)
                                        }
                                    }

                                    @JavascriptInterface
                                    fun onStateChange(state: Int) {
                                        post {
                                            when (state) {
                                                0 -> { // ENDED
                                                    onPlaybackChanged(false)
                                                    onVideoEnded()
                                                }
                                                1 -> onPlaybackChanged(true) // PLAYING
                                                2 -> onPlaybackChanged(false) // PAUSED
                                            }
                                        }
                                    }
                                }

                                addJavascriptInterface(AndroidBridge(), "AndroidBridge")

                                val htmlData = generateYouTubeHtml(safeVideoId)
                                loadDataWithBaseURL(
                                    "https://www.youtube.com",
                                    htmlData,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                                webViewRef = this
                            }
                        } catch (_: Throwable) {
                            hasRenderError = true
                            View(viewContext)
                        }
                    },
                    update = { view ->
                        if (view is WebView) {
                            webViewRef = view
                        }
                    },
                    onRelease = { view ->
                        if (view is WebView) {
                            runCatching {
                                view.stopLoading()
                                view.loadUrl("about:blank")
                                view.removeJavascriptInterface("AndroidBridge")
                                (view.parent as? ViewGroup)?.removeView(view)
                                view.destroy()
                            }
                            if (webViewRef === view) {
                                webViewRef = null
                            }
                        }
                    }
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                if (safeVideoId.isNotEmpty()) {
                    AsyncImage(
                        model = "https://img.youtube.com/vi/$safeVideoId/hqdefault.jpg",
                        contentDescription = "Video thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "مشغل الفيديو متاح للمشاهدة عبر YouTube",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = ::openExternally,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFCC0000),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تشغيل على YouTube", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                hasRenderError = false
                                isPlayerReady = false
                                retryNonce++
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إعادة المحاولة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (!hasRenderError && showLoadingSpinner && !isPlayerReady) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        } else if (!hasRenderError && !isPlayerReady && !showLoadingSpinner) {
            Surface(
                onClick = ::openExternally,
                color = Color.Black.copy(alpha = 0.72f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open in YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "فتح في YouTube",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun generateYouTubeHtml(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                html, body { width: 100%; height: 100%; background-color: #000; overflow: hidden; }
                #player { width: 100%; height: 100%; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        height: '100%',
                        width: '100%',
                        videoId: '$videoId',
                        playerVars: {
                            'autoplay': 1,
                            'playsinline': 1,
                            'rel': 0,
                            'modestbranding': 1,
                            'controls': 1,
                            'fs': 1,
                            'enablejsapi': 1,
                            'origin': 'https://www.youtube.com'
                        },
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange
                        }
                    });
                }

                function onPlayerReady(event) {
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onReady();
                    }
                }

                function onPlayerStateChange(event) {
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onStateChange(event.data);
                    }
                }

                function playVideo() {
                    if (player && typeof player.playVideo === 'function') {
                        player.playVideo();
                    }
                }

                function pauseVideo() {
                    if (player && typeof player.pauseVideo === 'function') {
                        player.pauseVideo();
                    }
                }

                function loadVideo(id) {
                    if (player && typeof player.loadVideoById === 'function') {
                        player.loadVideoById(id);
                    }
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
