package app.kamy.qalbuApp.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.core.text.HtmlCompat

private val tajweedSpanRegex = Regex(
    """<span\s+class=['"]([^'"]+)['"][^>]*>([\s\S]*?)</span>|([^<]+)""",
    RegexOption.IGNORE_CASE
)

private val endSpanRegex =
    Regex("<span\\b[^>]*\\bclass\\s*=\\s*['\"]?\\s*end\\s*['\"]?[^>]*>[\\s\\S]*?</span>", RegexOption.IGNORE_CASE)

private val ayahEndSymbolRegex =
    Regex("<span\\b[^>]*\\bclass\\s*=\\s*['\"]?\\s*ayah-end-symbol\\s*['\"]?[^>]*>[\\s\\S]*?</span>", RegexOption.IGNORE_CASE)

fun buildTajweedAnnotatedString(
    textUthmani: String?,
    ayahNumber: Int? = null,
    baseColor: Color = Color(0xFF0F172A),
    markerFontSize: TextUnit = TextUnit.Unspecified,
    markerFontFamily: FontFamily? = null
): AnnotatedString {
    val body = textUthmani?.sanitizeTajweedArabicHtml().orEmpty()
    if (body.isEmpty()) return AnnotatedString("")
    val cleaned = stripInlineAyahEndMarkers(body, ayahNumber).ifEmpty { body }
    return buildAnnotatedString {
        appendTajweedHtml(cleaned, baseColor)
        ayahNumber?.takeIf { it > 0 }?.let {
            appendAyahEndMarker(it, markerFontSize, markerFontFamily)
        }
    }
}

private fun AnnotatedString.Builder.appendTajweedHtml(html: String, baseColor: Color) {
    val normalized = html
        .replace(ayahEndSymbolRegex, "")
        .replace(Regex("<div\\b[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</div>", RegexOption.IGNORE_CASE), "")
        .trim()
    if (normalized.isEmpty()) return

    tajweedSpanRegex.findAll(normalized).forEach { match ->
        val className = match.groupValues[1]
        val spanText = match.groupValues[2]
        val plainText = match.groupValues[3]
        when {
            className.isNotEmpty() -> {
                val text = decodeHtmlEntities(spanText)
                if (text.isNotEmpty()) {
                    withStyle(SpanStyle(color = tajweedColor(className, baseColor))) {
                        append(text)
                    }
                }
            }
            plainText.isNotEmpty() -> {
                append(decodeHtmlEntities(stripResidualTags(plainText)))
            }
        }
    }
}

private fun AnnotatedString.Builder.appendAyahEndMarker(
    ayahNumber: Int,
    markerFontSize: TextUnit,
    markerFontFamily: FontFamily?
) {
    val markerColor = Color(0xFFB45309)
    append("\u2009")
    withStyle(
        SpanStyle(
            color = markerColor,
            fontSize = markerFontSize,
            fontFamily = markerFontFamily
        )
    ) {
        // U+06DD draws Eastern-Arabic digits inside the ayah rosette in Uthmani fonts.
        append("\u06DD${easternArabicIndicDigits(ayahNumber)}")
    }
}

private fun decodeHtmlEntities(text: String): String =
    HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()

private fun stripResidualTags(text: String): String =
    text.replace(Regex("<[^>]+>"), "").trim()

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

private fun tajweedColor(className: String, baseColor: Color): Color {
    val key = className.trim().lowercase().split("\\s+".toRegex()).firstOrNull().orEmpty()
    return when (key) {
        "ham_wasl", "silent", "laam_shamsiya" -> Color(0xFFAAAAAA)
        "madda_normal" -> Color(0xFF537FFF)
        "madda_permissible" -> Color(0xFF4050FF)
        "madda_necessary" -> Color(0xFF000EBC)
        "madda_obligatory" -> Color(0xFF2144C1)
        "qalaqah" -> Color(0xFFDD0008)
        "ikhafa_shafawi" -> Color(0xFFD500B7)
        "ikhafa" -> Color(0xFF9400A8)
        "iqlab" -> Color(0xFF26BFFD)
        "idgham_shafawi", "idgham_ghunnah", "idgham_wo_ghunnah" -> Color(0xFF169200)
        "idgham_mutajanisayn", "idgham_mutaqaribayn" -> Color(0xFFA1A1A1)
        "ghunnah" -> Color(0xFFFF7E1E)
        "end" -> Color(0xFFD6A100)
        else -> baseColor
    }
}
