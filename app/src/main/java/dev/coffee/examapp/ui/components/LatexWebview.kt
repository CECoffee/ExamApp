package dev.coffee.examapp.ui.components

import dev.coffee.examapp.R
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.random.Random

@Composable
fun LatexWebview(
    latex: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val bgColor = MaterialTheme.colorScheme.background
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var webViewHeight by remember { mutableStateOf(0) }
    val touchStartTimeTagKey = remember { Random.nextInt() }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                webViewRef?.let { webView ->
                    if (webView.canScrollVertically(-1) || webView.canScrollVertically(1)) {
                        return Offset.Zero
                    }
                }
                return super.onPreScroll(available, source)
            }
        }
    }

    AndroidView(
        modifier = modifier
            .background(Color.Transparent)
            .nestedScroll(nestedScrollConnection)
            .heightIn(min = 20.dp, max = 150.dp)
            .height(webViewHeight.dp)
            .pointerInput(Unit) {
                detectTapGestures { _ ->
                    webViewRef?.apply {
                        requestFocus()
                        performClick()
                    }
                }
            },
        factory = { ctx ->
            WebView(ctx).apply {
                webViewRef = this
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

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

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        view.evaluateJavascript(
                            "document.body.style.backgroundColor='transparent';",
                            null
                        )
                        view.evaluateJavascript("""
                            document.body.scrollHeight;
                            """) { height ->
                                webViewHeight = height.toIntOrNull() ?: 0
                            }
                    }
                }

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowContentAccess = true
                    allowFileAccess = true
                }
            }
        },
        update = { webView ->
            val htmlContent = buildHtmlContent(latex, textColor, bgColor)
            webView.loadDataWithBaseURL(
                "https://example.com/",
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

private fun buildHtmlContent(
    latex: String,
    textColor: Color,
    bgColor: Color
): String {
    val textColorHex = "#${textColor.toArgb().toUInt().toString(16).substring(2)}"
    val bgColorHex = "#${bgColor.toArgb().toUInt().toString(16).substring(2)}"

    val escapedLatex = latex
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
        <style>
            body {
                margin: 0;
                padding: 0;
                background-color: transparent !important;
                color: $textColorHex !important;
                overflow: hidden;
                touch-action: manipulation;
                -webkit-overflow-scrolling: touch;
            }
            .katex {
                color: $textColorHex !important;
                background: transparent !important;
            }
            .katex-display {
                margin: 0;
                padding: 8px 0;
                overflow: auto;
            }
            #katex-container {
                display: block;
                width: 100%;
                height: 100%;
                overflow: auto;
            }
        </style>
    </head>
    <body>
        <div id="katex-container"></div>
        <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
        <script>
            document.addEventListener('DOMContentLoaded', () => {
                const container = document.getElementById('katex-container');
                const latexString = "$escapedLatex";
                
                katex.render(latexString, container, {
                    displayMode: true,
                    throwOnError: false
                }).then(() => {
                    document.body.style.backgroundColor = 'transparent';
                    container.style.height = 'fit-content';
                });
            });
        </script>
    </body>
    </html>
    """.trimIndent()
}