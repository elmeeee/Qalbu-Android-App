package app.kamy.qalbuApp.ui.common

import androidx.core.text.HtmlCompat

fun String.toReaderPlainText(): String {
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
        .replace(Regex(" *\n *"), "\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

fun String.stripHtmlTags(): String = toReaderPlainText()

fun String.looksLikeHtml(): Boolean {
    val t = trim()
    return t.contains('<') && t.contains('>') && Regex("<[a-zA-Z][^>]*>").containsMatchIn(t)
}
