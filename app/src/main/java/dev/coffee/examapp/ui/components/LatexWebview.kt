package dev.coffee.examapp.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LatexWebview(
    latex: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val bgColor = MaterialTheme.colorScheme.background

    AndroidView(
        modifier = modifier.background(Color.Transparent),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        // 确保透明背景生效
                        view.evaluateJavascript(
                            "document.body.style.backgroundColor='transparent';",
                            null
                        )
                    }
                }
            }
        },
        update = { webView ->
            val htmlContent = buildHtmlContent(latex, textColor, bgColor)
            webView.loadDataWithBaseURL(
                null,
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
    val textColorHex = "#" + Integer.toHexString(textColor.toArgb()).substring(2)
    val bgColorHex = "#" + Integer.toHexString(bgColor.toArgb()).substring(2)

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
            <style>
                body {
                    margin: 0;
                    padding: 0;
                    background-color: transparent !important;
                    color: $textColorHex !important;
                }
                .katex {
                    color: $textColorHex !important;
                    background: transparent !important;
                }
                .katex-display {
                    margin: 0;
                    padding: 8px 0;
                }
            </style>
        </head>
        <body>
            <div id="katex-container">$latex</div>
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    const container = document.getElementById('katex-container');
                    katex.render(container.textContent, container, {
                        displayMode: true,
                        throwOnError: false,
                        fleqn: false,
                        output: 'html',
                        strict: false
                    });
                    // 强制设置透明背景
                    document.body.style.backgroundColor = 'transparent';
                });
            </script>
        </body>
        </html>
    """.trimIndent()
}