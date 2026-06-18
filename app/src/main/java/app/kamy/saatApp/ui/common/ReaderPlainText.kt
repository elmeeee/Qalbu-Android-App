package app.kamy.saatApp.ui.common

import androidx.core.text.HtmlCompat

/** Decode API HTML (translations, tafsir, hadith, reflect posts) to plain text. */
fun String.decodeHtmlEntities(): String {
    val trimmed = trim()
    if (trimmed.isEmpty()) return ""
    return if (looksLikeHtml()) {
        HtmlCompat.fromHtml(trimmed, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    } else {
        trimmed
    }
}

fun String.toReaderPlainText(): String = decodeHtmlEntities()
    .replace('\u00A0', ' ')
    .replace(Regex("[ \t]+"), " ")
    .replace(Regex(" *\n *"), "\n")
    .replace(Regex("\n{3,}"), "\n\n")
    .trim()

/** Single-line plain text for verse translations in cards and readers. */
fun String.toVerseTranslationPlainText(): String = decodeHtmlEntities()
    .replace('\u00A0', ' ')
    .replace(Regex("[ \t]+"), " ")
    .replace(Regex(" *\n+ *"), " ")
    .trim()

fun String.stripHtmlTags(): String = toReaderPlainText()

fun String.looksLikeHtml(): Boolean {
    val t = trim()
    return t.contains('<') && t.contains('>') && Regex("<[a-zA-Z][^>]*>").containsMatchIn(t)
}
