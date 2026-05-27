package app.kamy.qalbuApp.ui.common

import android.annotation.SuppressLint
import android.content.Context
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
 * Renders tajweed-colored Arabic HTML using the bundled tajweed font
 * (file:///android_asset/fonts/tajweed_font.ttf). Mirrors iOS
 * Design/Organisms/HTMLContentWebView.swift and AyahArabicWebBlock.swift.
 *
 * Height wraps content so long ayat are never clipped.
 *
 * @param htmlFragment the raw `<div dir="rtl" lang="ar">…</div>` produced by
 *   [app.kamy.qalbuApp.domain.model.RandomAyahPayload.tajweedWebHtmlFragment].
 * @param fontSizeSp base Arabic font size in CSS px (we treat 1px≈1sp here).
 *   Compose callers should pass a value already multiplied by their fontScale.
 * @param textColor CSS color (e.g., "#0F172A") — usually scheme.onSurface.toArgbHex().
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
    val density = LocalDensity.current
    val wrappedHtml = remember(htmlFragment, fontSizeSp, textColor) {
        wrapTajweedHtml(htmlFragment, fontSizeSp, textColor)
    }

    val heightModifier = if (contentHeightPx > 0) {
        Modifier.height(with(density) { contentHeightPx.toDp() })
    } else {
        Modifier.heightIn(min = 48.dp)
    }

    AndroidView(
        modifier = modifier.fillMaxWidth().then(heightModifier),
        factory = { ctx -> buildTajweedWebView(ctx) },
        update = { webView ->
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.evaluateJavascript(MEASURE_CONTENT_HEIGHT_JS) { raw ->
                        val px = parseMeasuredHeight(raw) ?: return@evaluateJavascript
                        view.post {
                            if (px > contentHeightPx) contentHeightPx = px
                        }
                    }
                }
            }
            if (lastLoadedHtml != wrappedHtml) {
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

private fun parseMeasuredHeight(raw: String?): Int? {
    if (raw.isNullOrBlank() || raw == "null") return null
    return raw.trim().removeSurrounding("\"").toFloatOrNull()?.toInt()?.takeIf { it > 0 }
}

private const val MEASURE_CONTENT_HEIGHT_JS = """
(function() {
  var b = document.body, e = document.documentElement;
  var h = Math.max(
    b.scrollHeight, b.offsetHeight,
    e.clientHeight, e.scrollHeight, e.offsetHeight
  );
  return Math.ceil(h) + 8;
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

/**
 * Wraps a tajweed HTML fragment in a full document with the bundled font,
 * sensible defaults for RTL Arabic, and inline color tokens for tajweed rules.
 *
 * The class names below match what the QF Content API returns inside
 * `text_uthmani_tajweed` (see iOS QuranVerseArabic.swift). Colors are taken from
 * tajweed.online / community conventions; tune later to match iOS exactly.
 */
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
            line-height: 1.9;
            direction: rtl;
            text-align: center;
            -webkit-text-size-adjust: 100%;
            padding: 4px 8px 6px;
        }

        /* Tajweed rule colors (community palette; adjust to match iOS one-to-one if needed). */
        .ham_wasl     { color: #AAAAAA; }            /* hamzat al-wasl */
        .silent       { color: #AAAAAA; }
        .laam_shamsiya{ color: #AAAAAA; }
        .madda_normal { color: #537FFF; }            /* madd 2 */
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
        .end          { color: #D6A100; }            /* ayah-end markers from API */
        /* Compact stacked rosette + eastern digits (iOS parity, smaller badge). */
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

/**
 * Helper to build the same `<div dir="rtl" lang="ar">…</div>` wrapper that the iOS
 * `RandomAyahPayload.tajweedWebHTMLFragment()` produces. Use this on a raw
 * `text_uthmani_tajweed` value before passing to [TajweedHtmlView].
 */
fun buildTajweedHtmlFragment(textUthmaniTajweed: String?, ayahNumber: Int? = null): String {
    val body = textUthmaniTajweed?.trim().orEmpty()
    if (body.isEmpty()) return "<div dir=\"rtl\" lang=\"ar\"></div>"
    val marker = ayahEndMarkerHtml(ayahNumber)
    val spacer = if (marker.isEmpty()) "" else " "
    // Strip API `class="end"` spans and any trailing duplicate ayah badge text.
    val cleaned = stripInlineAyahEndMarkers(body, ayahNumber)
        .ifEmpty { body }
    return "<div dir=\"rtl\" lang=\"ar\">$cleaned$spacer$marker</div>"
}

private val endSpanRegex =
    Regex("<span\\b[^>]*\\bclass\\s*=\\s*['\"]?\\s*end\\s*['\"]?[^>]*>[\\s\\S]*?</span>", RegexOption.IGNORE_CASE)

/** Removes server-inlined end markers so we render a single stacked badge (iOS parity). */
private fun stripInlineAyahEndMarkers(html: String, ayahNumber: Int?): String {
    var text = html.replace(endSpanRegex, "").trim()
    // U+06DD Arabic End of Ayah + optional joiners + eastern digits at end of verse HTML.
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
