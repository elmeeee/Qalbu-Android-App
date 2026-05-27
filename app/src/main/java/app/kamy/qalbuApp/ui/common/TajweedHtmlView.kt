package app.kamy.qalbuApp.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Renders tajweed-colored Arabic HTML using the bundled tajweed font.
 * Card height follows verse length — nothing is clipped.
 */
@Composable
fun TajweedHtmlView(
    htmlFragment: String,
    fontSizeSp: Int = 32,
    textColor: String = "#0F172A",
    modifier: Modifier = Modifier
) {
    var contentHeightPx by remember(htmlFragment, fontSizeSp, textColor) { mutableIntStateOf(0) }
    var lastLoadedHtml by remember(htmlFragment, fontSizeSp, textColor) { mutableStateOf<String?>(null) }
    val wrappedHtml = remember(htmlFragment, fontSizeSp, textColor) {
        wrapTajweedHtml(htmlFragment, fontSizeSp, textColor)
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AutoHeightTajweedWebView(ctx) { px ->
                if (px > contentHeightPx) {
                    contentHeightPx = px
                }
            }
        },
        update = { webView ->
            val autoWebView = webView as AutoHeightTajweedWebView
            autoWebView.onHeightChanged = { px ->
                if (px > contentHeightPx) {
                    contentHeightPx = px
                }
            }
            if (lastLoadedHtml != wrappedHtml) {
                lastLoadedHtml = wrappedHtml
                contentHeightPx = 0
                autoWebView.resetHeight()
                autoWebView.loadHtml(wrappedHtml)
            } else if (contentHeightPx > 0) {
                autoWebView.applyContentHeight(contentHeightPx)
            }
        }
    )
}

/**
 * WebView that sizes itself to HTML content via [TajweedBridge] callbacks.
 * Compose only constrains width; height comes from the View's onMeasure.
 */
@SuppressLint("ViewConstructor")
private class AutoHeightTajweedWebView(
    context: Context,
    initialOnHeight: (Int) -> Unit
) : WebView(context) {

    var onHeightChanged: (Int) -> Unit = initialOnHeight
    private var contentHeightPx: Int = 0

    init {
        setBackgroundColor(0x00000000)
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.useWideViewPort = false
        settings.loadWithOverviewMode = false
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        isNestedScrollingEnabled = false
        addJavascriptInterface(TajweedBridge(), "TajweedBridge")
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

    fun loadHtml(html: String) {
        loadDataWithBaseURL(
            "file:///android_asset/",
            html,
            "text/html",
            "UTF-8",
            null
        )
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = when {
            contentHeightPx > 0 -> contentHeightPx
            else -> {
                // Until JS reports, reserve space from a generous estimate (font + line count).
                val estimated = (layoutParams.width.takeIf { it > 0 } ?: width)
                (estimated * 0.35f).toInt().coerceAtLeast(120)
            }
        }
        setMeasuredDimension(width, height)
    }

    private fun scheduleHeightReports() {
        val script = """
            (function() {
              function report() {
                var b = document.body;
                var h = Math.max(
                  b.getBoundingClientRect().height,
                  b.scrollHeight,
                  b.offsetHeight,
                  document.documentElement.scrollHeight
                );
                if (h > 0 && window.TajweedBridge) {
                  TajweedBridge.reportHeight(Math.ceil(h));
                }
              }
              report();
              if (document.fonts && document.fonts.ready) {
                document.fonts.ready.then(report);
              }
              [80, 200, 400, 700, 1100].forEach(function(ms) {
                setTimeout(report, ms);
              });
            })();
        """.trimIndent()
        evaluateJavascript(script, null)
    }

    private inner class TajweedBridge {
        @JavascriptInterface
        fun reportHeight(cssPx: Float) {
            val px = cssPx.toInt().coerceAtLeast(1)
            // Extra room for harakat / ayah-end badge below the last line.
            val safePx = (px * 1.08f).toInt() + 24
            post {
                if (safePx > contentHeightPx) {
                    contentHeightPx = safePx
                    val lp = layoutParams ?: ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    lp.height = safePx
                    layoutParams = lp
                    onHeightChanged(safePx)
                    requestLayout()
                }
            }
        }
    }
}

private fun wrapTajweedHtml(fragment: String, fontSizeSp: Int, textColor: String): String {
    val css = """
        @font-face {
            font-family: 'AlKhatibQuranWeb';
            src: url('fonts/tajweed_font.ttf') format('truetype');
            font-display: swap;
        }
        html, body {
            margin: 0;
            padding: 0;
            background: transparent;
            color: $textColor;
            overflow: visible !important;
            height: auto !important;
            min-height: 0 !important;
        }
        body {
            font-family: 'AlKhatibQuranWeb', 'KFGQPC HAFS Uthmanic Script', 'Amiri Quran', serif;
            font-size: ${fontSizeSp}px;
            line-height: 2.15;
            direction: rtl;
            text-align: center;
            -webkit-text-size-adjust: 100%;
            padding: 10px 12px 16px;
            overflow: visible !important;
            word-wrap: break-word;
        }
        .ham_wasl     { color: #AAAAAA; }
        .silent       { color: #AAAAAA; }
        .laam_shamsiya{ color: #AAAAAA; }
        .madda_normal { color: #537FFF; }
        .madda_permissible { color: #4050FF; }
        .madda_necessary   { color: #000EBC; }
        .madda_obligatory  { color: #2144C1; }
        .qalaqah      { color: #DD0008; }
        .ikhafa_shafawi { color: #D500B7; }
        .ikhafa       { color: #9400A8; }
        .iqlab        { color: #26BFFD; }
        .idgham_shafawi { color: #58B800; }
        .idgham_ghunnah { color: #169200; }
        .idgham_wo_ghunnah { color: #169200; }
        .idgham_mutajanisayn { color: #A1A1A1; }
        .idgham_mutaqaribayn { color: #A1A1A1; }
        .ghunnah      { color: #FF7E1E; }
        .end          { color: #D6A100; }
        .ayah-end-symbol {
            display: inline-grid;
            place-items: center;
            white-space: nowrap;
            unicode-bidi: embed;
            color: #B45309;
            margin-inline-start: 0.25em;
            width: 0.95em;
            height: 0.95em;
            font-size: 0.72em;
            vertical-align: -0.06em;
            line-height: 1;
            overflow: visible;
        }
        .ayah-end-rosette {
            grid-area: 1 / 1;
            font-size: 1em;
            line-height: 1;
            color: #B45309;
        }
        .ayah-end-number {
            grid-area: 1 / 1;
            font-size: 0.44em;
            line-height: 1;
            font-weight: 700;
            color: #B45309;
        }
        div[lang="ar"] {
            display: block;
            overflow: visible !important;
        }
    """.trimIndent()

    return """
        <!DOCTYPE html>
        <html lang="ar" dir="rtl">
        <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>$css</style>
        </head>
        <body>$fragment</body>
        </html>
    """.trimIndent()
}

fun buildTajweedHtmlFragment(textUthmaniTajweed: String?, ayahNumber: Int? = null): String {
    val body = textUthmaniTajweed?.trim().orEmpty()
    if (body.isEmpty()) return "<div dir=\"rtl\" lang=\"ar\"></div>"
    val marker = ayahEndMarkerHtml(ayahNumber)
    val spacer = if (marker.isEmpty()) "" else " "
    val cleaned = stripInlineAyahEndMarkers(body, ayahNumber)
        .ifEmpty { body }
    return "<div dir=\"rtl\" lang=\"ar\">$cleaned$spacer$marker</div>"
}

private val endSpanRegex =
    Regex("<span\\b[^>]*\\bclass\\s*=\\s*['\"]?\\s*end\\s*['\"]?[^>]*>[\\s\\S]*?</span>", RegexOption.IGNORE_CASE)

private fun stripInlineAyahEndMarkers(html: String, ayahNumber: Int?): String {
    var text = html.replace(endSpanRegex, "").trim()
    text = text.replace(
        Regex("\u06DD[\u200C\u200D\u200E\u200F\\s]*[\u0660-\u0669]+\$")
    ) { "" }.trim()
    val n = ayahNumber?.takeIf { it > 0 } ?: return text
    val digits = easternArabicIndicDigits(n)
    text = text.replace(Regex("\\s*${Regex.escape(digits)}\\s*$"), "").trim()
    return text
}

private fun ayahEndMarkerHtml(ayahNumber: Int?): String {
    val n = ayahNumber?.takeIf { it > 0 } ?: return ""
    val digits = easternArabicIndicDigits(n)
    val rosette = "\u06DD"
    return """<span lang="ar" dir="rtl" class="ayah-end-symbol" aria-label="Ayah $n"><span class="ayah-end-rosette" aria-hidden="true">$rosette</span><span class="ayah-end-number">$digits</span></span>"""
}

private fun easternArabicIndicDigits(value: Int): String {
    val table = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
    if (value <= 0) return table[0]
    var n = value
    val sb = StringBuilder()
    while (n > 0) {
        sb.insert(0, table[n % 10])
        n /= 10
    }
    return sb.toString()
}
