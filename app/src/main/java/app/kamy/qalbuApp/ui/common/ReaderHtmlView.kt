package app.kamy.qalbuApp.ui.common

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.kamy.qalbuApp.design.theme.AlKhatibColors

/**
 * Auto-height WebView for tafsir / commentary HTML (LTR prose).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderHtmlView(
    htmlBody: String,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var contentHeightPx by remember(htmlBody) { mutableIntStateOf(0) }
    var lastLoaded by remember(htmlBody) { mutableStateOf<String?>(null) }
    val wrapped = remember(htmlBody) { wrapReaderProseHtml(htmlBody) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            AutoHeightReaderWebView(ctx) { px ->
                val dp = with(density) { px.toDp() }
                if (dp > 0.dp) contentHeightPx = px
            }.apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
            }
        },
        update = { webView ->
            webView.onHeightChanged = { px ->
                val dp = with(density) { px.toDp() }
                if (dp > 0.dp) contentHeightPx = px
            }
            if (lastLoaded != wrapped) {
                lastLoaded = wrapped
                contentHeightPx = 0
                webView.resetHeight()
                webView.loadHtml(wrapped)
            } else if (contentHeightPx > 0) {
                webView.applyContentHeight(contentHeightPx)
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
private class AutoHeightReaderWebView(
    context: android.content.Context,
    private val onMeasured: (Int) -> Unit
) : WebView(context) {
    var onHeightChanged: ((Int) -> Unit)? = null

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = false
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        addJavascriptInterface(ReaderHeightBridge { px ->
            post {
                onHeightChanged?.invoke(px)
                onMeasured(px)
            }
        }, "ReaderBridge")
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                evaluateJavascript(
                    "(function(){ return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight); })();"
                ) { raw ->
                    raw?.trim()?.removeSurrounding("\"")?.toIntOrNull()?.let { h ->
                        if (h > 0) {
                            post {
                                onHeightChanged?.invoke(h)
                                onMeasured(h)
                            }
                        }
                    }
                }
            }
        }
    }

    fun resetHeight() {
        layoutParams = layoutParams?.apply { height = 1 } ?: LayoutParams(LayoutParams.MATCH_PARENT, 1)
    }

    fun applyContentHeight(px: Int) {
        val h = px.coerceAtLeast(1)
        layoutParams = layoutParams?.apply { height = h }
            ?: LayoutParams(LayoutParams.MATCH_PARENT, h)
        requestLayout()
    }

    fun loadHtml(html: String) {
        loadDataWithBaseURL(
            "https://app.local/",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }
}

private class ReaderHeightBridge(private val onHeight: (Int) -> Unit) {
    @JavascriptInterface
    fun reportHeight(px: Int) {
        if (px > 0) onHeight(px)
    }
}

fun wrapReaderProseHtml(body: String): String {
    val emerald = AlKhatibColors.DeepEmerald.value.toInt() and 0xFFFFFF
    val hex = String.format("#%06X", emerald)
    val css = """
        html, body {
            margin: 0;
            padding: 0;
            background: transparent;
            color: #1E293B;
            overflow: visible !important;
            height: auto !important;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 16px;
            line-height: 1.65;
            direction: ltr;
            text-align: left;
            padding: 4px 2px 20px;
            -webkit-text-size-adjust: 100%;
        }
        h1, h2, h3, h4 { color: $hex; font-weight: 600; margin: 1em 0 0.5em; }
        p { margin: 0 0 0.85em; }
        a { color: $hex; }
        strong, b { color: #0F172A; }
    """.trimIndent()
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>$css</style>
        </head>
        <body>$body</body>
        </html>
    """.trimIndent()
}

fun String.stripHtmlTags(): String = replace(Regex("<[^>]+>"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()

fun String.looksLikeHtml(): Boolean {
    val t = trim()
    return t.contains('<') && t.contains('>') && Regex("<[a-zA-Z][^>]*>").containsMatchIn(t)
}
