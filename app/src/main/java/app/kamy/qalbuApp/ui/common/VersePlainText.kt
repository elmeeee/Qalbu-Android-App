package app.kamy.qalbuApp.ui.common

import androidx.core.text.HtmlCompat

/**
 * Decodes translation HTML from the Content API into clean, readable plain text.
 */
fun String.toVerseTranslationPlainText(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return ""
    val decoded = if (looksLikeHtml()) {
        HtmlCompat.fromHtml(trimmed, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    } else {
        trimmed
    }
    return decoded
        .replace('\u00A0', ' ')
        .replace(Regex("[ \t]+"), " ")
        .replace(Regex(" *\n+ *"), " ")
        .trim()
}

/**
 * Strips block-level HTML wrappers from tajweed Arabic so WebView height hugs the glyphs.
 */
fun String.sanitizeTajweedArabicHtml(): String {
    if (isBlank()) return ""
    return trim()
        .replace(Regex("<p\\b[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<div\\b[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</div>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), " ")
        .replace(Regex("\\s{2,}"), " ")
        .trim()
}
