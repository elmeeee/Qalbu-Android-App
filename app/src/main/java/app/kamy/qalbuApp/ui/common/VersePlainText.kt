package app.kamy.qalbuApp.ui.common

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
