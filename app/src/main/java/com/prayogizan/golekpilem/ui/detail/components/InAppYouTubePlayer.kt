package com.prayogizan.golekpilem.ui.detail.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Embedded in-app YouTube video player utilizing an isolated Android [WebView] powered by the
 * official YouTube IFrame Player API.
 *
 * Configures inline video streaming inside the app without requiring third-party SDK dependencies,
 * enforces strict lifecycle cleanup upon disposal, and isolates playback context.
 *
 * @param videoKey The unique YouTube video alphanumeric identifier (e.g. `dQw4w9WgXcQ`).
 * @param modifier Optional modifier applied to the root player layout.
 * @param autoPlay Whether playback should commence immediately once the IFrame renders.
 * @param onFullScreenRequest Optional callback invoked when fullscreen playback is requested via YouTube controls.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppYouTubePlayer(
    videoKey: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    onFullScreenRequest: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentOnFullScreenRequest by rememberUpdatedState(onFullScreenRequest)

    val webView = remember(context, videoKey) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    val onFs = currentOnFullScreenRequest
                    if (onFs != null) {
                        callback?.onCustomViewHidden()
                        onFs()
                    } else {
                        super.onShowCustomView(view, callback)
                    }
                }
            }
            webViewClient = WebViewClient()
            setBackgroundColor(AndroidColor.BLACK)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.loadUrl("about:blank")
            webView.stopLoading()
            webView.destroy()
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        AndroidView(
            factory = { webView },
            update = { view ->
                val cacheKey = "$videoKey-$autoPlay"
                if (view.tag != cacheKey) {
                    view.tag = cacheKey
                    val autoplayFlag = if (autoPlay) 1 else 0
                    val html = """
                        <!DOCTYPE html>
                        <html style="margin:0;padding:0;width:100%;height:100%;background-color:#000;">
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                            <style>
                                * { margin:0; padding:0; box-sizing:border-box; }
                                html, body { width:100%; height:100%; background-color:#000; overflow:hidden; }
                                .player-wrapper { position:relative; width:100%; height:100%; }
                                iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:none; }
                            </style>
                        </head>
                        <body>
                            <div class="player-wrapper">
                                <iframe
                                    src="https://www.youtube-nocookie.com/embed/$videoKey?autoplay=$autoplayFlag&playsinline=1&modestbranding=1&rel=0&enablejsapi=1"
                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                    allowfullscreen>
                                </iframe>
                            </div>
                        </body>
                        </html>
                    """.trimIndent()
                    view.loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
