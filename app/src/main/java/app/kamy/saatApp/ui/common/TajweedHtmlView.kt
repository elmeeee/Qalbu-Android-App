package app.kamy.saatApp.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
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
    onTajweedClick: ((TajweedType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lineHeightMultiplier = if (compact) 2.05f else 2.35f
    val fontSize = fontSizeSp.sp
    val markerFontSize = (fontSizeSp * 0.58f).sp
    val annotated = remember(textUthmani, ayahNumber, textColor, markerFontSize, isTajweedEnabled) {
        buildTajweedAnnotatedString(
            textUthmani = textUthmani,
            ayahNumber = ayahNumber,
            baseColor = textColor,
            markerFontSize = markerFontSize,
            markerFontFamily = TajweedFontFamily,
            isTajweedEnabled = isTajweedEnabled
        )
    }

    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ClickableText(
            text = annotated,
            modifier = modifier,
            style = TextStyle(
                fontFamily = TajweedFontFamily,
                fontSize = fontSize,
                lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
                color = textColor,
                textAlign = textAlign.toComposeAlign()
            ),
            onClick = { offset ->
                val annotations = annotated.getStringAnnotations("TAJWEED", offset, offset)
                val annotation = annotations.firstOrNull()
                if (annotation != null) {
                    val tajweedName = annotation.item
                    val tajweedType = runCatching { TajweedType.valueOf(tajweedName) }.getOrNull()
                    if (tajweedType != null) {
                        onTajweedClick?.invoke(tajweedType)
                    }
                }
            }
        )
    }
}
