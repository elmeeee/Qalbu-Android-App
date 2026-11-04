package app.kamy.saatApp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.TajweedFontFamily

/**
 * Lightweight Markdown renderer tuned for Kīmi answers.
 * Supports headings, bullet / numbered lists, block quotes, horizontal rules,
 * inline bold / italic / code, and renders Arabic-dominant lines right-to-left
 * with the Qur'anic font.
 */
@Composable
fun KimiMarkdown(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF1E293B),
    accentColor: Color = SaatColors.DeepEmerald
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = inlineMarkdown(block.text, textColor, accentColor),
                        style = TextStyle(
                            fontSize = if (block.level <= 2) 16.sp else 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor
                    )
                }

                is MdBlock.Paragraph -> {
                    if (block.isArabic) {
                        ArabicLine(text = block.text, color = textColor)
                    } else {
                        Text(
                            text = inlineMarkdown(block.text, textColor, accentColor),
                            style = bodyStyle,
                            color = textColor
                        )
                    }
                }

                is MdBlock.ListItem -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = block.marker,
                            style = bodyStyle.copy(fontWeight = FontWeight.SemiBold),
                            color = accentColor,
                            modifier = Modifier
                                .padding(start = (block.indent * 12).dp)
                                .width(if (block.marker.length > 2) 26.dp else 16.dp)
                        )
                        if (block.isArabic) {
                            ArabicLine(text = block.text, color = textColor, modifier = Modifier.weight(1f))
                        } else {
                            Text(
                                text = inlineMarkdown(block.text, textColor, accentColor),
                                style = bodyStyle,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                is MdBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.06f))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(accentColor)
                        )
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            if (block.isArabic) {
                                ArabicLine(text = block.text, color = textColor)
                            } else {
                                Text(
                                    text = inlineMarkdown(block.text, textColor, accentColor),
                                    style = bodyStyle.copy(fontStyle = FontStyle.Italic),
                                    color = textColor.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                MdBlock.Rule -> HorizontalDivider(
                    color = accentColor.copy(alpha = 0.18f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

private val bodyStyle = TextStyle(fontSize = 14.5.sp, lineHeight = 22.sp)

@Composable
private fun ArabicLine(text: String, color: Color, modifier: Modifier = Modifier) {
    val clean = unescapeUnicode(text).trim()
    if (clean.isBlank()) return
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = clean,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            style = TextStyle(
                fontFamily = TajweedFontFamily,
                fontSize = 20.sp,
                lineHeight = 38.sp,
                textAlign = TextAlign.Right
            ),
            color = color
        )
    }
}

fun unescapeUnicode(text: String): String {
    val regex = Regex("""\\u([0-9a-fA-F]{4})""")
    return regex.replace(text) { match ->
        val code = match.groupValues[1].toInt(16)
        code.toChar().toString()
    }
}

// ─── Parsing ────────────────────────────────────────────────────────────────

sealed class MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock()
    data class Paragraph(val text: String, val isArabic: Boolean) : MdBlock()
    data class ListItem(val marker: String, val text: String, val indent: Int, val isArabic: Boolean) : MdBlock()
    data class Quote(val text: String, val isArabic: Boolean) : MdBlock()
    data object Rule : MdBlock()
}

private val headingRegex = Regex("^(#{1,6})\\s+(.*)$")
private val bulletRegex = Regex("^(\\s*)[-*•]\\s+(.*)$")
private val numberedRegex = Regex("^(\\s*)(\\d+)[.)]\\s+(.*)$")
private val quoteRegex = Regex("^>\\s?(.*)$")
private val ruleRegex = Regex("^\\s*([-*_])\\s*\\1\\s*\\1[\\s\\-*_]*$")

fun parseMarkdownBlocks(markdown: String): List<MdBlock> {
    val lines = markdown.replace("\r\n", "\n").replace("\\n", "\n").split('\n')
    val blocks = mutableListOf<MdBlock>()
    val paragraph = StringBuilder()

    fun flushParagraph() {
        if (paragraph.isNotBlank()) {
            val text = paragraph.toString().trim()
            blocks += MdBlock.Paragraph(text, isArabicDominant(text))
        }
        paragraph.clear()
    }

    for (rawLine in lines) {
        val line = rawLine.trimEnd()
        if (line.isBlank()) {
            flushParagraph()
            continue
        }
        val heading = headingRegex.find(line)
        if (heading != null) {
            flushParagraph()
            blocks += MdBlock.Heading(heading.groupValues[1].length, heading.groupValues[2].trim())
            continue
        }
        if (ruleRegex.matches(line)) {
            flushParagraph()
            blocks += MdBlock.Rule
            continue
        }
        val quote = quoteRegex.find(line)
        if (quote != null) {
            flushParagraph()
            val text = quote.groupValues[1].trim()
            blocks += MdBlock.Quote(text, isArabicDominant(text))
            continue
        }
        val numbered = numberedRegex.find(line)
        if (numbered != null) {
            flushParagraph()
            val indent = numbered.groupValues[1].length / 2
            val text = numbered.groupValues[3].trim()
            blocks += MdBlock.ListItem("${numbered.groupValues[2]}.", text, indent, isArabicDominant(text))
            continue
        }
        val bullet = bulletRegex.find(line)
        if (bullet != null) {
            flushParagraph()
            val indent = bullet.groupValues[1].length / 2
            val text = bullet.groupValues[2].trim()
            blocks += MdBlock.ListItem("•", text, indent, isArabicDominant(text))
            continue
        }
        // Arabic lines are kept as their own block so they can be rendered RTL.
        if (isArabicDominant(line)) {
            flushParagraph()
            blocks += MdBlock.Paragraph(line.trim(), true)
        } else {
            val trimmedLine = line.trim()
            val isSeparateLine = isTransliterationOrTranslation(trimmedLine) ||
                (paragraph.isNotEmpty() && isTransliteration(paragraph.toString().trim()))
            if (isSeparateLine) {
                flushParagraph()
            }
            if (paragraph.isNotEmpty()) paragraph.append(' ')
            paragraph.append(trimmedLine)
        }
    }
    flushParagraph()
    return blocks
}

private fun isArabicDominant(text: String): Boolean {
    var arabic = 0
    var letters = 0
    for (c in text) {
        if (c.isLetter()) {
            letters++
            if (c in '\u0600'..'\u06FF' || c in '\u0750'..'\u077F' || c in '\uFB50'..'\uFDFF' || c in '\uFE70'..'\uFEFF') {
                arabic++
            }
        }
    }
    return letters > 0 && arabic * 100 / letters >= 60
}

private fun isTransliteration(text: String): Boolean {
    val t = text.trim()
    return (t.startsWith("*") && t.endsWith("*") && t.length > 2 && !t.drop(1).dropLast(1).contains("*")) ||
           (t.startsWith("_") && t.endsWith("_") && t.length > 2 && !t.drop(1).dropLast(1).contains("_")) ||
           t.startsWith("Latin:", ignoreCase = true) ||
           t.startsWith("Cara baca:", ignoreCase = true) ||
           t.startsWith("Transliterasi:", ignoreCase = true)
}

private fun isTransliterationOrTranslation(text: String): Boolean {
    val t = text.trim()
    return isTransliteration(t) ||
           t.startsWith("Artinya:", ignoreCase = true) ||
           t.startsWith("Terjemahan:", ignoreCase = true) ||
           t.startsWith("Meaning:", ignoreCase = true) ||
           t.startsWith("Maksudnya:", ignoreCase = true)
}

// ─── Inline styling ─────────────────────────────────────────────────────────

private val inlineRegex = Regex("(\\*\\*(.+?)\\*\\*)|(__(.+?)__)|(`([^`]+)`)|(\\*(?!\\s)(.+?)(?<!\\s)\\*)|(_(?!\\s)([^_]+?)(?<!\\s)_)")

fun inlineMarkdown(text: String, textColor: Color, accentColor: Color): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (m in inlineRegex.findAll(text)) {
        if (m.range.first > last) append(text.substring(last, m.range.first))
        when {
            m.groups[2] != null || m.groups[4] != null -> {
                val inner = m.groups[2]?.value ?: m.groups[4]!!.value
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) { append(inner) }
            }
            m.groups[6] != null -> {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = accentColor.copy(alpha = 0.08f),
                        color = accentColor,
                        fontSize = 13.sp
                    )
                ) { append(m.groups[6]!!.value) }
            }
            m.groups[8] != null || m.groups[10] != null -> {
                val inner = m.groups[8]?.value ?: m.groups[10]!!.value
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(inner) }
            }
        }
        last = m.range.last + 1
    }
    if (last < text.length) append(text.substring(last))
}
