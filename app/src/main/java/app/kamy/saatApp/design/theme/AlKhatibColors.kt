package app.kamy.saatApp.design.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.kamy.saatApp.infrastructure.preferences.AppThemeColor

object SaatColors {
    // Primary variables
    var DeepEmerald by mutableStateOf(Color(0xFF0F4C3A))
    var Teal by mutableStateOf(Color(0xFF14806A))
    var TealDark by mutableStateOf(Color(0xFF106352))
    var EmeraldRich by mutableStateOf(Color(0xFF165C4B))
    var EmeraldNight by mutableStateOf(Color(0xFF09291F))
    var ForestDark by mutableStateOf(Color(0xFF0C382A))
    var ForestDeeper by mutableStateOf(Color(0xFF062118))
    var ReaderMoss by mutableStateOf(Color(0xFF0E4030))
    var ReaderForest by mutableStateOf(Color(0xFF08261C))
    var SageTint by mutableStateOf(Color(0xFFE8F2ED))
    var MintWash by mutableStateOf(Color(0xFFF2F9F6))

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
    var IndigoAccent by mutableStateOf(Color(0xFF4F46E5))
    var BlueLink by mutableStateOf(Color(0xFF2563EB))

    // Constants
    val Gold = Color(0xFFB45309)
    val GoldBright = Color(0xFFD4A017)
    val GoldDeep = Color(0xFFD97706)
    val AmberWash = Color(0xFFFFFBEB)
    val IndigoDeep = Color(0xFF312E81)
    val Danger = Color(0xFFEF4444)

    fun applyTheme(theme: AppThemeColor) {
        // Base layout is always light (as requested, dark mode removed)
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
        IndigoAccent = Color(0xFF4F46E5)
        BlueLink = Color(0xFF2563EB)

        when (theme) {
            AppThemeColor.EMERALD -> {
                DeepEmerald = Color(0xFF0F4C3A)
                Teal = Color(0xFF14806A)
                TealDark = Color(0xFF106352)
                EmeraldRich = Color(0xFF165C4B)
                EmeraldNight = Color(0xFF09291F)
                ForestDark = Color(0xFF0C382A)
                ForestDeeper = Color(0xFF062118)
                ReaderMoss = Color(0xFF0E4030)
                ReaderForest = Color(0xFF08261C)
                SageTint = Color(0xFFE8F2ED)
                MintWash = Color(0xFFF2F9F6)
            }
            AppThemeColor.OCEAN -> {
                DeepEmerald = Color(0xFF1B4965)
                Teal = Color(0xFF3B82F6)
                TealDark = Color(0xFF2563EB)
                EmeraldRich = Color(0xFF2C6CA5)
                EmeraldNight = Color(0xFF0F2C3F)
                ForestDark = Color(0xFF153B53)
                ForestDeeper = Color(0xFF0A1F2D)
                ReaderMoss = Color(0xFF18425C)
                ReaderForest = Color(0xFF0D2534)
                SageTint = Color(0xFFE8F2F8)
                MintWash = Color(0xFFF3F8FC)
            }
            AppThemeColor.GOLD -> {
                DeepEmerald = Color(0xFF8B6015)
                Teal = Color(0xFFC08A27)
                TealDark = Color(0xFFA6741F)
                EmeraldRich = Color(0xFF9C7221)
                EmeraldNight = Color(0xFF382607)
                ForestDark = Color(0xFF5A3E0C)
                ForestDeeper = Color(0xFF2B1D04)
                ReaderMoss = Color(0xFF6B4A0E)
                ReaderForest = Color(0xFF3F2B07)
                SageTint = Color(0xFFF9F5EC)
                MintWash = Color(0xFFFDFBF7)
            }
            AppThemeColor.ROSE -> {
                DeepEmerald = Color(0xFFE11D48)
                Teal = Color(0xFFF43F5E)
                TealDark = Color(0xFFBE185D)
                EmeraldRich = Color(0xFF9D174D)
                EmeraldNight = Color(0xFF4C0519)
                ForestDark = Color(0xFF881337)
                ForestDeeper = Color(0xFF5C0620)
                ReaderMoss = Color(0xFFBE185D)
                ReaderForest = Color(0xFFFFF1F2)
                SageTint = Color(0xFFFFE4E6)
                MintWash = Color(0xFFFFF1F2)
            }
            AppThemeColor.PURPLE -> {
                DeepEmerald = Color(0xFF7C3AED)
                Teal = Color(0xFF8B5CF6)
                TealDark = Color(0xFF6D28D9)
                EmeraldRich = Color(0xFF5B21B6)
                EmeraldNight = Color(0xFF2E1065)
                ForestDark = Color(0xFF4C1D95)
                ForestDeeper = Color(0xFF3B0764)
                ReaderMoss = Color(0xFF6D28D9)
                ReaderForest = Color(0xFFF5F3FF)
                SageTint = Color(0xFFEDE9FE)
                MintWash = Color(0xFFF5F3FF)
            }
            AppThemeColor.ORANGE -> {
                DeepEmerald = Color(0xFFEA580C)
                Teal = Color(0xFFF97316)
                TealDark = Color(0xFFC2410C)
                EmeraldRich = Color(0xFF9A3412)
                EmeraldNight = Color(0xFF431407)
                ForestDark = Color(0xFF7C2D12)
                ForestDeeper = Color(0xFF541D0F)
                ReaderMoss = Color(0xFFC2410C)
                ReaderForest = Color(0xFFFFF7ED)
                SageTint = Color(0xFFFFEDD5)
                MintWash = Color(0xFFFFF7ED)
            }
            AppThemeColor.RED -> {
                DeepEmerald = Color(0xFFDC2626)
                Teal = Color(0xFFEF4444)
                TealDark = Color(0xFFB91C1C)
                EmeraldRich = Color(0xFF991B1B)
                EmeraldNight = Color(0xFF450A0A)
                ForestDark = Color(0xFF7F1D1D)
                ForestDeeper = Color(0xFF5A1414)
                ReaderMoss = Color(0xFFB91C1C)
                ReaderForest = Color(0xFFFEF2F2)
                SageTint = Color(0xFFFFECEC)
                MintWash = Color(0xFFFEF2F2)
            }
        }
    }
}
