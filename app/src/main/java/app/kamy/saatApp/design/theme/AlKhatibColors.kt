package app.kamy.saatApp.design.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import app.kamy.saatApp.infrastructure.preferences.AppThemeColor

object SaatColors {
    // Primary variables locked to unified Linear Gradient vertikal: #085E43 (Top) to #15AA7C (Bottom)
    var DeepEmerald by mutableStateOf(Color(0xFF085E43))
    var Teal by mutableStateOf(Color(0xFF15AA7C))
    var TealDark by mutableStateOf(Color(0xFF085E43))
    var EmeraldRich by mutableStateOf(Color(0xFF085E43))
    var EmeraldNight by mutableStateOf(Color(0xFF04291D))
    var ForestDark by mutableStateOf(Color(0xFF085E43))
    var ForestDeeper by mutableStateOf(Color(0xFF04291D))
    var ReaderMoss by mutableStateOf(Color(0xFF085E43))
    var ReaderForest by mutableStateOf(Color(0xFF04291D))
    var SageTint by mutableStateOf(Color(0xFFE6F3EE))
    var MintWash by mutableStateOf(Color(0xFFF0F8F5))

    // Vertical linear gradient brush (#085E43 to #15AA7C)
    val PrimaryVerticalGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF085E43),
            Color(0xFF15AA7C)
        )
    )

    val PrimaryGradientColors = listOf(
        Color(0xFF085E43),
        Color(0xFF15AA7C)
    )

    // Layout variables
    var OffWhite by mutableStateOf(Color(0xFFF9F9F8))
    var ScreenBackground by mutableStateOf(Color(0xFFF8FAFC))
    var PureWhite by mutableStateOf(Color(0xFFFFFFFF))
    var SoftGrey by mutableStateOf(Color(0xFFE5E7EB))
    var LightGrey by mutableStateOf(Color(0xFFF3F4F6))
    var Slate500 by mutableStateOf(Color(0xFF64748B))
    var Slate700 by mutableStateOf(Color(0xFF334155))
    var Slate800 by mutableStateOf(Color(0xFF1E293B))
    var Slate900 by mutableStateOf(Color(0xFF0F172A))
    var PrayerCream by mutableStateOf(Color(0xFFFFF7ED))
    var PrayerCreamWarm by mutableStateOf(Color(0xFFFEF3C7))
    var PrayerMint by mutableStateOf(Color(0xFFF0FDFA))
    var SageMist by mutableStateOf(Color(0xFFF1F5F2))
    var PanelGrey by mutableStateOf(Color(0xFFE8EBEF))
    var PanelGreyAlt by mutableStateOf(Color(0xFFEEF2EE))
    var IndigoAccent by mutableStateOf(Color(0xFF085E43))
    var BlueLink by mutableStateOf(Color(0xFF15AA7C))

    // Constants
    val Gold = Color(0xFFB45309)
    val GoldBright = Color(0xFFD4A017)
    val GoldDeep = Color(0xFFD97706)
    val AmberWash = Color(0xFFFFFBEB)
    val IndigoDeep = Color(0xFF085E43)
    val Danger = Color(0xFFEF4444)

    fun applyTheme(theme: AppThemeColor = AppThemeColor.EMERALD) {
        // Unified app theme using linear gradient #085E43 to #15AA7C across all screens
        PureWhite = Color(0xFFFFFFFF)
        OffWhite = Color(0xFFF9F9F8)
        ScreenBackground = Color(0xFFF8FAFC)
        LightGrey = Color(0xFFF3F4F6)
        PanelGrey = Color(0xFFE8EBEF)
        PanelGreyAlt = Color(0xFFEEF2EE)
        SageMist = Color(0xFFF1F5F2)
        SoftGrey = Color(0xFFE5E7EB)
        
        // Fonts
        Slate900 = Color(0xFF0F172A)
        Slate700 = Color(0xFF334155)
        Slate800 = Color(0xFF1E293B)
        Slate500 = Color(0xFF64748B)
        
        // Tints
        PrayerCream = Color(0xFFFFF7ED)
        PrayerCreamWarm = Color(0xFFFEF3C7)
        PrayerMint = Color(0xFFF0FDFA)
        IndigoAccent = Color(0xFF085E43)
        BlueLink = Color(0xFF15AA7C)

        DeepEmerald = Color(0xFF085E43)
        Teal = Color(0xFF15AA7C)
        TealDark = Color(0xFF085E43)
        EmeraldRich = Color(0xFF085E43)
        EmeraldNight = Color(0xFF04291D)
        ForestDark = Color(0xFF085E43)
        ForestDeeper = Color(0xFF04291D)
        ReaderMoss = Color(0xFF085E43)
        ReaderForest = Color(0xFF04291D)
        SageTint = Color(0xFFE6F3EE)
        MintWash = Color(0xFFF0F8F5)
    }
}
