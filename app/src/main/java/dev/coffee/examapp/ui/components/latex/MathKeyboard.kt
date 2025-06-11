package dev.coffee.examapp.ui.components.latex

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.coffee.examapp.R
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathKeyboard(
    onKeyPress: (String) -> Unit,
    onDismiss: () -> Unit,
    currentLatex: String?,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val isWebViewLoaded = remember { mutableStateOf(false) }
    val modalState = rememberModalBottomSheetState(
        confirmValueChange = { target ->
            if (target == SheetValue.Hidden) {
                onDismiss()
            }
            true
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        scrimColor = Color.Transparent,
        content = {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    KeyboardWebView(onKeyPress, currentLatex?: "", textColor)
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(Color(0xFF151515))
                    )
                }
            }
        }
    )
}

@Composable
private fun KeyboardWebView(
    onKeyPress: (String) -> Unit,
    latex: String, textColor: Color
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                id = R.id.math_keyboard
                tag = ""
                val textColorHex = "#${textColor.toArgb().toUInt().toString(16).substring(2)}"
                val escapedLatex = latex.escapeJS()
                configureKeyboardWebView(onKeyPress) { this.post { this.tag = "math_keyboard" } }
                loadUrl("file:///android_asset/mathlive/keyboard.html?latex=${
                    URLEncoder.encode(escapedLatex, "UTF-8")
                    }&color=${URLEncoder.encode(textColorHex, "UTF-8")}")
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureKeyboardWebView(
    onKeyPress: (String) -> Unit,
    onLoaded: () -> Unit
) {
    setBackgroundColor(android.graphics.Color.TRANSPARENT)
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        allowContentAccess = true
        allowFileAccess = true
        allowFileAccessFromFileURLs = true
        allowUniversalAccessFromFileURLs = true
    }

    addJavascriptInterface(object {
        @JavascriptInterface
        fun onKeyPressed(symbol: String) {
            onKeyPress(symbol)
        }
        @JavascriptInterface
        fun onLoaded() {
            onLoaded()
        }
    }, "KeyboardReceiver")
}