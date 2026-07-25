package app.kamy.saatApp.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.design.theme.TajweedFontFamily
import app.kamy.saatApp.features.quran.tajweed.TajweedType

enum class TajweedTextAlign {
    Center,
    Right,
    Justify;

    fun toComposeAlign(): TextAlign = when (this) {
        Center -> TextAlign.Center
        Right -> TextAlign.Right
        Justify -> TextAlign.Justify
    }
}
@Composable
fun TajweedHtmlView(
    textUthmani: String?,
    ayahNumber: Int? = null,
    fontSizeSp: Int = 32,
    textColor: Color = Color(0xFF0F172A),
    compact: Boolean = false,
    textAlign: TajweedTextAlign = TajweedTextAlign.Center,
    isTajweedEnabled: Boolean = true,
    activeWordIndex: Int? = null,
    onTajweedClick: ((TajweedType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lineHeightMultiplier = if (compact) 2.05f else 2.35f
    val fontSize = fontSizeSp.sp
    val markerFontSize = (fontSizeSp * 0.58f).sp
    val annotated = remember(textUthmani, ayahNumber, textColor, markerFontSize, isTajweedEnabled, activeWordIndex) {
        buildTajweedAnnotatedString(
            textUthmani = textUthmani,
            ayahNumber = ayahNumber,
            baseColor = textColor,
            markerFontSize = markerFontSize,
            markerFontFamily = TajweedFontFamily,
            isTajweedEnabled = isTajweedEnabled,
            activeWordIndex = activeWordIndex
        )
    }

    var currentTextLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BasicText(
            text = annotated,
            modifier = modifier.pointerInput(onTajweedClick, annotated) {
                detectTapGestures { offsetPosition ->
                    val layoutResult = currentTextLayoutResult ?: return@detectTapGestures
                    val offset = layoutResult.getOffsetForPosition(offsetPosition)
                    var annotation = annotated.getStringAnnotations("TAJWEED", offset, offset).firstOrNull()

                    if (annotation == null) {
                        val maxSearchDistance = 3
                        val searchStart = (offset - maxSearchDistance).coerceAtLeast(0)
                        val searchEnd = (offset + maxSearchDistance).coerceAtMost(annotated.length)
                        val neighborhoodAnnotations = annotated.getStringAnnotations("TAJWEED", searchStart, searchEnd)

                        var bestAnnotation: androidx.compose.ui.text.AnnotatedString.Range<String>? = null
                        var bestDistance = Int.MAX_VALUE

                        for (ann in neighborhoodAnnotations) {
                            val dist = when {
                                offset < ann.start -> ann.start - offset
                                offset >= ann.end -> offset - (ann.end - 1)
                                else -> 0
                            }
                            if (dist < bestDistance) {
                                bestDistance = dist
                                bestAnnotation = ann
                            }
                        }
                        annotation = bestAnnotation
                    }

                    if (annotation != null) {
                        val tajweedName = annotation.item
                        val tajweedType = runCatching { TajweedType.valueOf(tajweedName) }.getOrNull()
                        if (tajweedType != null) {
                            onTajweedClick?.invoke(tajweedType)
                        }
                    }
                }
            },
            style = TextStyle(
                fontFamily = TajweedFontFamily,
                fontSize = fontSize,
                lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
                color = textColor,
                textAlign = textAlign.toComposeAlign()
            ),
            onTextLayout = {
                currentTextLayoutResult = it
            }
        )
    }
}

