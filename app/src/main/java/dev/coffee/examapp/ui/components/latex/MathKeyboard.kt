package dev.coffee.examapp.ui.components.latex

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
        containerColor = Color.Transparent,
        scrimColor = Color.Transparent,
        content = {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                color = Color.Transparent
            ) {
                KeyboardWebView(onKeyPress, currentLatex?: "", textColor)
            }
            Spacer(modifier = modifier.height(16.dp))
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
                val textColorHex = "#${textColor.toArgb().toUInt().toString(16).substring(2)}"
                val escapedLatex = latex.escapeJS()
                configureKeyboardWebView(onKeyPress)
                loadUrl("file:///android_asset/mathlive/keyboard.html?latex=${
                    URLEncoder.encode(escapedLatex, "UTF-8")
                    }&color=${URLEncoder.encode(textColorHex, "UTF-8")}")
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureKeyboardWebView(onKeyPress: (String) -> Unit) {
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
    }, "KeyboardReceiver")
}