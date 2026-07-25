package app.kamy.saatApp.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.core.text.HtmlCompat
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.features.quran.tajweed.TajweedEngine

private val tajweedTagRegex = Regex(
    """<(?:span|tajweed)\b[^>]*?\bclass\s*=\s*['"]?([^'">\s]+)['"]?[^>]*>([\s\S]*?)</(?:span|tajweed)>|([^<]+)""",
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
    markerFontFamily: FontFamily? = null,
    isTajweedEnabled: Boolean = true,
    activeWordIndex: Int? = null
): AnnotatedString {
    val body = textUthmani?.sanitizeTajweedArabicHtml().orEmpty()
    if (body.isEmpty()) return AnnotatedString("")
    val cleaned = stripInlineAyahEndMarkers(body, ayahNumber).ifEmpty { body }
    val strippedHtml = cleaned.stripHtmlTags()
    
    // Apply Dynamic Algorithmic Tajweed Engine
    val tajweedAnnotated = TajweedEngine.applyTajweed(strippedHtml, isTajweedEnabled = isTajweedEnabled)

    val built = buildAnnotatedString {
        append(tajweedAnnotated)
        if (activeWordIndex != null && activeWordIndex >= 0) {
            val matches = Regex("\\S+").findAll(tajweedAnnotated.text).toList()
            if (activeWordIndex in matches.indices) {
                val match = matches[activeWordIndex]
                addStyle(
                    style = SpanStyle(
                        background = SaatColors.GoldBright.copy(alpha = 0.35f)
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
        ayahNumber?.takeIf { it > 0 }?.let {
            appendAyahEndMarker(it, markerFontSize, markerFontFamily)
        }
    }
    return built
}

private fun AnnotatedString.Builder.appendTajweedHtml(html: String, baseColor: Color) {
    val normalized = html
        .replace(ayahEndSymbolRegex, "")
        .replace(Regex("<div\\b[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</div>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<p\\b[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), " ")
        .trim()
    if (normalized.isEmpty()) return

    val matches = tajweedTagRegex.findAll(normalized).toList()
    val hasColoredSpans = matches.any { it.groupValues[1].isNotEmpty() }
    if (!hasColoredSpans && normalized.looksLikeHtml()) {
        append(normalized.stripHtmlTags())
        return
    }

    matches.forEach { match ->
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
        // U+FD3F and U+FD3E are Ornate Parentheses for ayah endings.
        append("\uFD3F${easternArabicIndicDigits(ayahNumber)}\uFD3E")
    }
}

private fun decodeHtmlEntities(text: String): String =
    HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()

private fun stripResidualTags(text: String): String =
    text.replace(Regex("<[^>]+>"), "").trim()

private fun stripInlineAyahEndMarkers(html: String, ayahNumber: Int?): String {
    var text = html.replace(endSpanRegex, "").trim()
    text = text.replace("\u06DD", "").trim()
    text = text.replace("?", "").trim() // Also remove literal question marks just in case
    text = text.replace(Regex("[\\s\u200C-\u200F]*[\u0660-\u0669\u06F0-\u06F9]+\$"), "").trim()
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
        "ham_wasl", "silent", "laam_shamsiya", "laam_shamsiyah" -> Color(0xFFAAAAAA)
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
