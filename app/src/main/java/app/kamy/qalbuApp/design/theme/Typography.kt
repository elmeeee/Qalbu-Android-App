package app.kamy.qalbuApp.design.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Quran Arabic typeface. Prefer res/font/tajweed_font.ttf when bundled; until then
 * use a serif stack so the app builds and runs without the iOS font asset.
 */
val TajweedFontFamily = FontFamily.Serif

/**
 * Default Material 3 typography. Customize per-screen for Arabic vs Latin runs.
 * The Quran reader uses [TajweedFontFamily] with line-height 1.82 (matching iOS
 * QuranVerseArabic.swift).
 */
val AlKhatibTypography: Typography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, lineHeight = 64.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
)

/**
 * Tajweed text style for ayah rendering. The actual rendering of tajweed-colored
 * markup happens inside a WebView using the bundled font (see WebView wrapper in
 * Phase 2). This style is for plain-Arabic fallbacks.
 */
val TajweedTextStyle = TextStyle(
    fontFamily = TajweedFontFamily,
    fontSize = 28.sp,
    lineHeight = 28.sp * 1.82f
)
