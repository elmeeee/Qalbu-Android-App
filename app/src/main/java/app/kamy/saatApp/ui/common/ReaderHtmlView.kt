package app.kamy.saatApp.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import app.kamy.saatApp.design.theme.SaatColors

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderHtmlView(
    htmlBody: String,
    modifier: Modifier = Modifier,
    wrapHtml: (String) -> String = ::wrapReaderProseHtml
) {
    val density = LocalDensity.current
    var contentHeightPx by remember(htmlBody) { mutableIntStateOf(0) }
    var lastLoaded by remember(htmlBody) { mutableStateOf<String?>(null) }
    val wrapped = remember(htmlBody, wrapHtml) { wrapHtml(htmlBody) }
    val heightModifier = if (contentHeightPx > 0) {
        val bufferPx = with(density) { 16.dp.roundToPx() }
        Modifier.height(with(density) { (contentHeightPx + bufferPx).toDp() })
    } else {
        Modifier.heightIn(min = 48.dp)
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .then(heightModifier),
        factory = { ctx ->
            AutoHeightReaderWebView(ctx) { px ->
                if (px > 0 && px != contentHeightPx) {
                    contentHeightPx = px
                }
            }.apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            webView.onHeightChanged = { px ->
                if (px > 0 && px != contentHeightPx) {
                    contentHeightPx = px
                }
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

@SuppressLint("ViewConstructor", "ClickableViewAccessibility", "SetJavaScriptEnabled")
private class AutoHeightReaderWebView(
    context: Context,
    initialOnHeight: (Int) -> Unit
) : WebView(context) {
    var onHeightChanged: (Int) -> Unit = initialOnHeight
    private var contentHeightPx: Int = 0

    init {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = false
        settings.useWideViewPort = false
        settings.loadWithOverviewMode = false
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        isNestedScrollingEnabled = false
        isScrollContainer = false
        clipToPadding = false
        clipChildren = false
        addJavascriptInterface(ReaderHeightBridge(), "ReaderBridge")
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                scheduleHeightReports()
            }
        }
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density
        val fallbackMinPx = (80f * density).toInt()
        val height = contentHeightPx.takeIf { it > 0 }
            ?: fallbackMinPx
        setMeasuredDimension(width, height)
    }

    fun resetHeight() {
        contentHeightPx = 0
        val lp = layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams = lp
        requestLayout()
    }

    fun applyContentHeight(px: Int) {
        if (px <= 0) return
        contentHeightPx = px
        val lp = layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.height = px
        layoutParams = lp
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

    private fun scheduleHeightReports() {
        val script = """
            (function() {
              function measure() {
                var body = document.body;
                var html = document.documentElement;
                return Math.max(
                  body ? body.scrollHeight : 0,
                  body ? body.offsetHeight : 0,
                  html ? html.scrollHeight : 0,
                  html ? html.offsetHeight : 0
                );
              }
              var h = measure();
              if (window.ReaderBridge && h > 0) {
                ReaderBridge.reportHeight(h);
              }
              return h;
            })();
        """.trimIndent()
        evaluateJavascript(script, null)
        postDelayed({ evaluateJavascript(script, null) }, 120)
        postDelayed({ evaluateJavascript(script, null) }, 400)
        postDelayed({ evaluateJavascript(script, null) }, 800)
        postDelayed({ evaluateJavascript(script, null) }, 1200)
    }

    private inner class ReaderHeightBridge {
        @JavascriptInterface
        fun reportHeight(px: Int) {
            if (px <= 0) return
            post {
                contentHeightPx = px
                onHeightChanged(px)
                applyContentHeight(px)
            }
        }
    }
}

fun wrapReaderProseHtml(body: String): String {
    val emerald = SaatColors.DeepEmerald.value.toInt() and 0xFFFFFF
    val hex = String.format("#%06X", emerald)
    val css = """
        html, body {
            margin: 0;
            padding: 0;
            background: transparent;
            color: #1E293B;
            overflow: visible !important;
            height: auto !important;
            min-height: 0 !important;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 16px;
            line-height: 1.65;
            direction: ltr;
            text-align: left;
            padding: 4px 2px 24px;
            -webkit-text-size-adjust: 100%;
            word-wrap: break-word;
            overflow-wrap: break-word;
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
