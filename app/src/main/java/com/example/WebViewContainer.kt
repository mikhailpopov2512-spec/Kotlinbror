package com.example

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewContainer(
    url: String,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    val loadingProgress by viewModel.webViewProgress.collectAsState()
    val isCurrentlyLoading by viewModel.isWebViewLoading.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        supportZoom()
                        builtInZoomControls = true
                        displayZoomControls = false
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val targetUrl = request?.url?.toString() ?: ""
                            
                            // Check blocklist before loading
                            if (viewModel.isBlocklisted(targetUrl)) {
                                viewModel.setUrl("chrome-native://blocked")
                                return true
                            }
                            
                            // Check safety in Kids mode
                            if (viewModel.browserMode.value == BrowserMode.KIDS) {
                                if (!viewModel.checkIfKidsFriendly(targetUrl)) {
                                    viewModel.setUrl("chrome-native://blocked")
                                    return true
                                }
                            }
                            
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            viewModel.setWebViewLoading(true)
                            
                            // Second line safety check
                            url?.let {
                                if (viewModel.isBlocklisted(it)) {
                                    viewModel.setUrl("chrome-native://blocked")
                                    view?.stopLoading()
                                }
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            viewModel.setWebViewLoading(false)
                            viewModel.setWebViewProgress(0f)
                            
                            // Update navigation controller buttons
                            view?.let {
                                viewModel.setCanGoBack(it.canGoBack())
                                viewModel.setCanGoForward(it.canGoForward())
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            viewModel.setWebViewProgress(newProgress / 100f)
                        }
                    }

                    onWebViewCreated(this)
                }
            },
            update = { webView ->
                // Load URL dynamically on state updates
                val currentUrlInWebView = webView.url ?: ""
                
                // Do not load native chrome-native URLs in webview itself, they are handled separately in compose!
                if (!url.startsWith("chrome-native://") && url.isNotBlank() && currentUrlInWebView != url) {
                    webView.loadUrl(url)
                }
            }
        )

        // Floating loader indicator matching theme
        if (isCurrentlyLoading && loadingProgress < 0.95f) {
            LinearProgressIndicator(
                progress = { loadingProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (viewModel.browserMode.collectAsState().value == BrowserMode.STEALTH) Color(0xFF00FF66) else Color(0xFF1E88E5),
                trackColor = Color.LightGray.copy(alpha = 0.3f),
            )
        }
    }
}
