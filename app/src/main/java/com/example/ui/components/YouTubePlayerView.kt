package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerView(
    videoId: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onVideoEnded: () -> Unit = {},
    onPlaybackChanged: (Boolean) -> Unit = {}
) {
    var isPlayerReady by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentLoadedVideoId by remember { mutableStateOf("") }

    // When isPlaying toggle changes, control video via JS
    LaunchedEffect(isPlaying, isPlayerReady) {
        if (isPlayerReady && webViewRef != null) {
            val js = if (isPlaying) "playVideo();" else "pauseVideo();"
            webViewRef?.evaluateJavascript(js, null)
        }
    }

    // When videoId changes, load the new video
    LaunchedEffect(videoId, isPlayerReady) {
        if (isPlayerReady && webViewRef != null && videoId != currentLoadedVideoId) {
            currentLoadedVideoId = videoId
            webViewRef?.evaluateJavascript("loadVideo('$videoId');", null)
        }
    }

    var hasRenderError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (!hasRenderError) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    try {
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.BLACK)

                            // Defensive layer type handling to prevent rendernode crashes in cloud emulators
                            try {
                                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                            } catch (t: Throwable) {
                                try {
                                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                } catch (ignored: Throwable) {}
                            }

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
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
                                    request: android.webkit.WebResourceRequest?,
                                    error: android.webkit.WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                }
                            }

                            class AndroidBridge {
                                @JavascriptInterface
                                fun onReady() {
                                    post {
                                        isPlayerReady = true
                                        currentLoadedVideoId = videoId
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

                            val htmlData = generateYouTubeHtml(videoId)
                            loadDataWithBaseURL("https://www.youtube.com", htmlData, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    } catch (e: Throwable) {
                        hasRenderError = true
                        WebView(context)
                    }
                },
                update = { view ->
                    webViewRef = view
                },
                onRelease = { view ->
                    try {
                        view.stopLoading()
                        view.loadUrl("about:blank")
                        view.destroy()
                    } catch (ignored: Throwable) {}
                }
            )
        } else {
            val context = androidx.compose.ui.platform.LocalContext.current
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "مشغل الفيديو متاح للمشاهدة المباشرة",
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(10.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        val appIntent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.youtube.com/watch?v=$videoId")
                        )
                        context.startActivity(appIntent)
                    }
                ) {
                    androidx.compose.material3.Text("تشغيل الفيديو على YouTube")
                }
            }
        }

        if (!hasRenderError && !isPlayerReady) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
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
