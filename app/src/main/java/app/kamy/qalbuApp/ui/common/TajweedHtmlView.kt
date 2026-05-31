package app.kamy.qalbuApp.ui.common

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

/**
 * Renders tajweed-colored Arabic HTML using the bundled tajweed font.
 * Height is driven by measured HTML content so harakat and ayah markers are never clipped.
 */
@Composable
fun TajweedHtmlView(
    htmlFragment: String,
    fontSizeSp: Int = 32,
    textColor: String = "#0F172A",
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    var contentHeightPx by remember(htmlFragment, fontSizeSp, textColor, compact) { mutableIntStateOf(0) }
    var lastLoadedHtml by remember(htmlFragment, fontSizeSp, textColor, compact) { mutableStateOf<String?>(null) }
    val wrappedHtml = remember(htmlFragment, fontSizeSp, textColor, compact) {
        wrapTajweedHtml(htmlFragment, fontSizeSp, textColor, compact)
    }
    val density = LocalDensity.current
    val minHeightDp = if (compact) 56.dp else 140.dp
    val heightModifier = if (contentHeightPx > 0) {
        Modifier.height(with(density) { contentHeightPx.toDp() })
    } else {
        Modifier.heightIn(min = minHeightDp)
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .then(heightModifier),
        factory = { ctx ->
            AutoHeightTajweedWebView(ctx) { px ->
                if (px != contentHeightPx) {
                    contentHeightPx = px
                }
            }
        },
        update = { webView ->
            val autoWebView = webView as AutoHeightTajweedWebView
            autoWebView.onHeightChanged = { px ->
                if (px != contentHeightPx) {
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
 * Compose applies the reported height explicitly — WebView alone is not enough.
 */
@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
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
        isScrollContainer = false
        clipToPadding = false
        clipChildren = false
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

    /** Let the parent pager/scroll view handle gestures; we only display full content. */
    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density
        val fallbackMinPx = (72f * density).toInt()
        val height = contentHeightPx.takeIf { it > 0 }
            ?: fallbackMinPx
        setMeasuredDimension(width, height)
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

    private fun scheduleHeightReports() {
        val script = """
            (function() {
              function measure() {
                var root = document.querySelector('[lang="ar"]') || document.body;
                var rect = root.getBoundingClientRect();
                var styles = window.getComputedStyle(root);
                var pad = parseFloat(styles.paddingTop) + parseFloat(styles.paddingBottom);
                return Math.ceil(rect.height + pad);
              }
              function report() {
                var h = measure();
                if (h > 0 && window.TajweedBridge) {
                  TajweedBridge.reportHeight(h);
                }
              }
              report();
              if (document.fonts && document.fonts.ready) {
                document.fonts.ready.then(report);
              }
              [50, 120, 250, 450, 800, 1200, 1800].forEach(function(ms) {
                setTimeout(report, ms);
              });
            })();
        """.trimIndent()
        evaluateJavascript(script, null)
    }

    private inner class TajweedBridge {
        @JavascriptInterface
        fun reportHeight(cssPx: Float) {
            // JS returns CSS pixels (1 CSS px = 1 dp). Convert to device pixels for setMeasuredDimension / layout height.
            val density = resources.displayMetrics.density
            val devicePx = (cssPx * density).toInt().coerceAtLeast(1)
            val safePx = devicePx + (10 * density).toInt()
            post {
                if (safePx != contentHeightPx) {
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

private fun wrapTajweedHtml(fragment: String, fontSizeSp: Int, textColor: String, compact: Boolean): String {
    val bodyPadding = if (compact) "4px 10px 0" else "12px 14px 4px"
    val lineHeight = if (compact) "2.05" else "2.35"
    val rootPaddingBottom = if (compact) "0" else "4px"
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
            line-height: $lineHeight;
            direction: rtl;
            text-align: center;
            -webkit-text-size-adjust: 100%;
            padding: $bodyPadding;
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
            width: 1em;
            height: 1em;
            font-size: 0.72em;
            vertical-align: -0.12em;
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
            padding-bottom: $rootPaddingBottom;
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
    val body = textUthmaniTajweed?.sanitizeTajweedArabicHtml().orEmpty()
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
