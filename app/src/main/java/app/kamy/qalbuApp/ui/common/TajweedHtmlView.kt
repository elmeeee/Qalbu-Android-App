package app.kamy.qalbuApp.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Renders tajweed-colored Arabic HTML using the bundled tajweed font
 * (file:///android_asset/fonts/tajweed_font.ttf). Mirrors iOS
 * Design/Organisms/HTMLContentWebView.swift and AyahArabicWebBlock.swift.
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
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            buildTajweedWebView(ctx)
        },
        update = { webView ->
            val html = wrapTajweedHtml(htmlFragment, fontSizeSp, textColor)
            webView.loadDataWithBaseURL(
                /* baseUrl     = */ "file:///android_asset/",
                /* data        = */ html,
                /* mimeType    = */ "text/html",
                /* encoding    = */ "UTF-8",
                /* historyUrl  = */ null
            )
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun buildTajweedWebView(context: Context): WebView = WebView(context).apply {
    setBackgroundColor(0x00000000)
    settings.javaScriptEnabled = false
    settings.allowFileAccess = true
    isVerticalScrollBarEnabled = false
    isHorizontalScrollBarEnabled = false
    webViewClient = WebViewClient()
    overScrollMode = WebView.OVER_SCROLL_NEVER
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
        }
        body {
            font-family: 'AlKhatibQuranWeb', 'KFGQPC HAFS Uthmanic Script', 'Amiri Quran', serif;
            font-size: ${fontSizeSp}px;
            line-height: 1.82;
            direction: rtl;
            text-align: center;
            -webkit-text-size-adjust: 100%;
            padding: 4px 8px;
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
        .end          { color: #D6A100; }            /* ayah-end markers */
        .ayah-end-symbol { color: #B45309; font-size: 0.85em; padding: 0 6px; }
        .ayah-end-rosette { font-size: 1.05em; }
        .ayah-end-number { font-size: 0.78em; vertical-align: middle; }

        div[lang="ar"] { display: block; }
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
    // Strip any `class="end"` spans the server may have inlined — iOS does the same.
    val cleaned = body.replace(Regex("<span\\b[^>]*\\bclass\\s*=\\s*['\"]?\\s*end\\s*['\"]?[^>]*>[\\s\\S]*?</span>", RegexOption.IGNORE_CASE), "")
        .trim()
        .ifEmpty { body }
    return "<div dir=\"rtl\" lang=\"ar\">$cleaned$spacer$marker</div>"
}

private fun ayahEndMarkerHtml(ayahNumber: Int?): String {
    val n = ayahNumber?.takeIf { it > 0 } ?: return ""
    val digits = easternArabicIndicDigits(n)
    return """<span lang="ar" dir="rtl" class="ayah-end-symbol" aria-label="Ayah $n"><span class="ayah-end-rosette" aria-hidden="true">۝</span><span class="ayah-end-number">$digits</span></span>"""
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
