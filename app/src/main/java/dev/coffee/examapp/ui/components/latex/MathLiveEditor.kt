package dev.coffee.examapp.ui.components.latex

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder
import kotlin.random.Random

@Composable
fun MathLiveEditor(
    initialLatex: String = "",
    onAnswerChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showKeyboard by remember { mutableStateOf(false) }
    var currentLatex by remember { mutableStateOf(initialLatex) }
    val touchStartTimeTagKey = remember { Random.nextInt() }

    // JavaScript接口用于双向通信
    class WebViewBridge {
        @JavascriptInterface
        fun onAnswerChanged(latex: String) {
            onAnswerChanged(latex)
        }
    }

    DisposableEffect(currentLatex) {
        webViewRef?.evaluateJavascript(
            "window.syncEditor('${currentLatex.escapeJS()}')",
            null
        )
        onDispose { }
    }

    Box(modifier = modifier) {
        // 编辑器WebView
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Transparent)
                .onFocusChanged {
                    webViewRef?.evaluateJavascript(
                        "window.keyboardOnDismiss()",
                        null
                    )
                },
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewRef = this
                    configureWebView(textColor)
                    addJavascriptInterface(WebViewBridge(), "AndroidBridge")
                    loadMathLiveContent(currentLatex, textColor)

                    setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                v.parent.requestDisallowInterceptTouchEvent(true)
                                v.setTag(touchStartTimeTagKey, System.currentTimeMillis())
                            }
                            MotionEvent.ACTION_MOVE -> {
                                v.parent.requestDisallowInterceptTouchEvent(true)
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                v.parent.requestDisallowInterceptTouchEvent(false)

                                val startTime = v.getTag(touchStartTimeTagKey) as? Long ?: 0
                                if (System.currentTimeMillis() - startTime < ViewConfiguration.getTapTimeout()) {
                                    v.performClick()
                                }
                            }
                        }
                        false
                    }
                    setOnClickListener { v -> showKeyboard = true }
                }
            }
        )

        // 虚拟键盘组件
        if (showKeyboard) {
            MathKeyboard(
                currentLatex = currentLatex,
                onKeyPress = { symbol ->
                    currentLatex = symbol
                },
                onDismiss = {
                    showKeyboard = false
                    webViewRef?.evaluateJavascript(
                        "window.keyboardOnDismiss()",
                        null
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureWebView(textColor: Color) {
    setBackgroundColor(android.graphics.Color.TRANSPARENT)
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        allowContentAccess = true
        allowFileAccess = true
        loadWithOverviewMode = true
        useWideViewPort = true
        allowFileAccessFromFileURLs = true
        allowUniversalAccessFromFileURLs = true
    }

    /*webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String?) {
            view.evaluateJavascript(
                "document.body.style.backgroundColor='transparent';",
                null
            )
        }
    }*/
}

private fun WebView.loadMathLiveContent(latex: String, textColor: Color) {
    val textColorHex = "#${textColor.toArgb().toUInt().toString(16).substring(2)}"
    val escapedLatex = latex.escapeJS()

    // 加载本地HTML文件
    loadUrl("file:///android_asset/mathlive/editor.html?latex=${
        URLEncoder.encode(escapedLatex, "UTF-8")
    }&color=${URLEncoder.encode(textColorHex, "UTF-8")}")
}

fun String.escapeJS(): String =
    replace("\\", "\\\\").replace("'", "\\'")