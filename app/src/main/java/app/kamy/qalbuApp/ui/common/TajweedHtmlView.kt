package app.kamy.qalbuApp.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
 * Renders tajweed-colored Arabic HTML using the bundled tajweed font
 * (file:///android_asset/fonts/tajweed_font.ttf). Mirrors iOS
 * Design/Organisms/HTMLContentWebView.swift and AyahArabicWebBlock.swift.
 *
 * Height wraps content so long ayat are never clipped.
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
    var measureGeneration by remember(htmlFragment, fontSizeSp, textColor) { mutableIntStateOf(0) }
    val pendingRemeasures = remember { mutableListOf<Runnable>() }
    val density = LocalDensity.current
    val wrappedHtml = remember(htmlFragment, fontSizeSp, textColor) {
        wrapTajweedHtml(htmlFragment, fontSizeSp, textColor)
    }

    DisposableEffect(wrappedHtml) {
        onDispose {
            pendingRemeasures.forEach { remeasureHandler.removeCallbacks(it) }
            pendingRemeasures.clear()
        }
    }
    val estimatedMinHeightDp = remember(fontSizeSp) {
        (fontSizeSp * 3.2f).coerceAtLeast(96f).dp
    }

    val heightModifier = if (contentHeightPx > 0) {
        Modifier.height(with(density) { contentHeightPx.toDp() })
    } else {
        Modifier.heightIn(min = estimatedMinHeightDp)
    }

    fun applySafeHeight(px: Int) {
        val safePx = (px * 1.4f).toInt() + 28
        if (safePx > contentHeightPx) contentHeightPx = safePx
    }

    AndroidView(
        modifier = modifier.fillMaxWidth().then(heightModifier),
        factory = { ctx ->
            buildTajweedWebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        update = { webView ->
            webView.layoutParams = webView.layoutParams?.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            } ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.let { wv ->
                        scheduleHeightRemeasures(
                            webView = wv,
                            generation = measureGeneration,
                            pending = pendingRemeasures
                        ) { px -> applySafeHeight(px) }
                    }
                }
            }
            if (lastLoadedHtml != wrappedHtml) {
                pendingRemeasures.forEach { remeasureHandler.removeCallbacks(it) }
                pendingRemeasures.clear()
                measureGeneration += 1
                lastLoadedHtml = wrappedHtml
                contentHeightPx = 0
                webView.loadDataWithBaseURL(
                    "file:///android_asset/",
                    wrappedHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    )
}

private val remeasureHandler = Handler(Looper.getMainLooper())

private const val MEASURE_GENERATION_TAG = 0x71AEE001

private fun scheduleHeightRemeasures(
    webView: WebView,
    generation: Int,
    pending: MutableList<Runnable>,
    onHeight: (Int) -> Unit
) {
    webView.setTag(MEASURE_GENERATION_TAG, generation)
    fun remeasure() {
        webView.evaluateJavascript(MEASURE_CONTENT_HEIGHT_JS) { raw ->
            parseMeasuredHeight(raw)?.let { px ->
                webView.post {
                    if (webView.getTag(MEASURE_GENERATION_TAG) != generation) return@post
                    onHeight(px)
                }
            }
        }
    }
    fun postRemeasure(delayMs: Long = 0L) {
        val task = Runnable { remeasure() }
        pending.add(task)
        if (delayMs == 0L) {
            remeasureHandler.post(task)
        } else {
            remeasureHandler.postDelayed(task, delayMs)
        }
    }
    postRemeasure()
    listOf(50L, 120L, 250L, 450L, 700L).forEach { postRemeasure(it) }
}

private fun parseMeasuredHeight(raw: String?): Int? {
    if (raw.isNullOrBlank() || raw == "null") return null
    return raw.trim().removeSurrounding("\"").toFloatOrNull()?.toInt()?.takeIf { it > 0 }
}

private const val MEASURE_CONTENT_HEIGHT_JS = """
(function() {
  function measure() {
    var b = document.body;
    var top = b.getBoundingClientRect().top;
    var maxBottom = top;
    var nodes = b.querySelectorAll('div, span, p');
    for (var i = 0; i < nodes.length; i++) {
      var r = nodes[i].getBoundingClientRect();
      if (r.height > 0) maxBottom = Math.max(maxBottom, r.bottom);
    }
    var blockHeight = maxBottom - top;
    var h = Math.max(
      blockHeight,
      b.scrollHeight,
      b.offsetHeight,
      document.documentElement.scrollHeight
    );
    return Math.ceil(h * 1.25) + 32;
  }
  return measure();
})();
"""

@SuppressLint("SetJavaScriptEnabled")
private fun buildTajweedWebView(context: Context): WebView = WebView(context).apply {
    setBackgroundColor(0x00000000)
    settings.javaScriptEnabled = true
    settings.allowFileAccess = true
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false
    overScrollMode = WebView.OVER_SCROLL_NEVER
    isNestedScrollingEnabled = false
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
            overflow: visible;
        }
        body {
            font-family: 'AlKhatibQuranWeb', 'KFGQPC HAFS Uthmanic Script', 'Amiri Quran', serif;
            font-size: ${fontSizeSp}px;
            line-height: 2.1;
            direction: rtl;
            text-align: center;
            -webkit-text-size-adjust: 100%;
            padding: 8px 10px 12px;
            overflow: visible;
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
            font-feature-settings: "liga" 1, "kern" 1;
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
            transform: translateY(-0.02em);
            color: #B45309;
        }

        div[lang="ar"] {
            display: block;
            overflow: visible;
        }
    """.trimIndent()

    return """
        <!DOCTYPE html>
        <html lang="ar" dir="rtl">
        <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
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
