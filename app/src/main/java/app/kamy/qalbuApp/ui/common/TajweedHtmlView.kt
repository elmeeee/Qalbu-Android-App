package app.kamy.qalbuApp.ui.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import app.kamy.qalbuApp.design.theme.TajweedFontFamily

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

/**
 * Renders tajweed-colored Arabic using native Compose text (no WebView).
 * Avoids crashes from broken Android System WebView updates on some devices.
 */
@Composable
fun TajweedHtmlView(
    textUthmaniTajweed: String?,
    ayahNumber: Int? = null,
    fontSizeSp: Int = 32,
    textColor: Color = Color(0xFF0F172A),
    compact: Boolean = false,
    textAlign: TajweedTextAlign = TajweedTextAlign.Center,
    modifier: Modifier = Modifier
) {
    val lineHeightMultiplier = if (compact) 2.05f else 2.35f
    val fontSize = fontSizeSp.sp
    val markerFontSize = (fontSizeSp * 0.72f).sp
    val annotated = remember(textUthmaniTajweed, ayahNumber, textColor, markerFontSize) {
        buildTajweedAnnotatedString(
            textUthmaniTajweed = textUthmaniTajweed,
            ayahNumber = ayahNumber,
            baseColor = textColor,
            markerFontSize = markerFontSize
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
            textAlign = textAlign.toComposeAlign(),
            textDirection = TextDirection.Content
        )
    }
}
