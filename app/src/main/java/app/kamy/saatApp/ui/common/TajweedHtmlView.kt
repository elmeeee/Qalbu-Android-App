package app.kamy.saatApp.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.design.theme.TajweedFontFamily

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
    modifier: Modifier = Modifier
) {
    val lineHeightMultiplier = if (compact) 2.05f else 2.35f
    val fontSize = fontSizeSp.sp
    val markerFontSize = (fontSizeSp * 0.58f).sp
    val annotated = remember(textUthmani, ayahNumber, textColor, markerFontSize) {
        buildTajweedAnnotatedString(
            textUthmani = textUthmani,
            ayahNumber = ayahNumber,
            baseColor = textColor,
            markerFontSize = markerFontSize,
            markerFontFamily = TajweedFontFamily
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = annotated,
            modifier = modifier,
            fontFamily = TajweedFontFamily,
            fontSize = fontSize,
            lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
            color = textColor,
            textAlign = textAlign.toComposeAlign()
        )
    }
}
