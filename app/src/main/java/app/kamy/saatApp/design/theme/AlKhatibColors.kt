package app.kamy.saatApp.design.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import app.kamy.saatApp.infrastructure.preferences.AppThemeColor

object AlKhatibColors {
    // Primary variables
    var DeepEmerald by mutableStateOf(Color(0xFF064E3B))
    var Teal by mutableStateOf(Color(0xFF0D9488))
    var TealDark by mutableStateOf(Color(0xFF0F766E))
    var EmeraldRich by mutableStateOf(Color(0xFF065F46))
    var EmeraldNight by mutableStateOf(Color(0xFF022C22))
    var ForestDark by mutableStateOf(Color(0xFF0B3D34))
    var ForestDeeper by mutableStateOf(Color(0xFF051F1A))
    var ReaderMoss by mutableStateOf(Color(0xFF0A3D2E))
    var ReaderForest by mutableStateOf(Color(0xFF0F2A22))
    var SageTint by mutableStateOf(Color(0xFFE7F0DF))
    var MintWash by mutableStateOf(Color(0xFFF0FDF4))

    // Layout variables (changed from val to var)
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

    private fun setupBaseColors(isDark: Boolean) {
        if (isDark) {
            PureWhite = Color(0xFF1E1E1E)
            OffWhite = Color(0xFF121212)
            ScreenBackground = Color(0xFF121212)
            LightGrey = Color(0xFF2A2A2A)
            PanelGrey = Color(0xFF2A2A2A)
            PanelGreyAlt = Color(0xFF222222)
            SageMist = Color(0xFF181818)
            SoftGrey = Color(0xFF374151)
            
            // Fonts
            Slate900 = Color(0xFFF3F4F6) // Text color
            Slate700 = Color(0xFFD1D5DB)
            Slate800 = Color(0xFFE5E7EB)
            Slate500 = Color(0xFF9CA3AF)
            
            // Tints
            SageTint = Color(0xFF2D3748)
            MintWash = Color(0xFF1F2937)
            PrayerCream = Color(0xFF2A2A2A)
            PrayerCreamWarm = Color(0xFF333333)
            PrayerMint = Color(0xFF1F2D2D)
            IndigoAccent = Color(0xFF818CF8)
            BlueLink = Color(0xFF60A5FA)
        } else {
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
            SageTint = Color(0xFFE7F0DF)
            MintWash = Color(0xFFF0FDF4)
            PrayerCream = Color(0xFFFFF7ED)
            PrayerCreamWarm = Color(0xFFFEF3C7)
            PrayerMint = Color(0xFFF0FDFA)
            IndigoAccent = Color(0xFF4F46E5)
            BlueLink = Color(0xFF2563EB)
        }
    }

    fun applyTheme(theme: AppThemeColor, customColorHex: String = "#0F4C3A") {
        val isDark = theme == AppThemeColor.DARK
        setupBaseColors(isDark)

        when (theme) {
            AppThemeColor.EMERALD -> {
                DeepEmerald = Color(0xFF0F4C3A) // Softer emerald
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
                DeepEmerald = Color(0xFF1B4965) // Deep ocean blue
                Teal = Color(0xFF3B82F6) // Softer blue
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
                DeepEmerald = Color(0xFF8B6015) // Royal gold
                Teal = Color(0xFFC08A27) // Softer, less bright gold
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
            AppThemeColor.DARK -> {
                DeepEmerald = Color(0xFF34D399)
                Teal = Color(0xFF2DD4BF)
                TealDark = Color(0xFF14B8A6)
                EmeraldRich = Color(0xFF059669)
                EmeraldNight = Color(0xFF064E3B)
                ForestDark = Color(0xFF047857)
                ForestDeeper = Color(0xFF065F46)
                ReaderMoss = Color(0xFF0F766E)
                ReaderForest = Color(0xFF111827)
                SageTint = Color(0xFF24302A)
                MintWash = Color(0xFF16251D)
            }
            AppThemeColor.ROSE -> {
                DeepEmerald = Color(0xFFE11D48) // rose 600
                Teal = Color(0xFFF43F5E) // rose 500
                TealDark = Color(0xFFBE185D) // rose 700
                EmeraldRich = Color(0xFF9D174D) // rose 800
                EmeraldNight = Color(0xFF4C0519) // rose 950
                ForestDark = Color(0xFF881337)
                ForestDeeper = Color(0xFF5C0620)
                ReaderMoss = Color(0xFFBE185D)
                ReaderForest = Color(0xFFFFF1F2)
                SageTint = Color(0xFFFFE4E6)
                MintWash = Color(0xFFFFF1F2)
            }
            AppThemeColor.PURPLE -> {
                DeepEmerald = Color(0xFF7C3AED) // violet 600
                Teal = Color(0xFF8B5CF6) // violet 500
                TealDark = Color(0xFF6D28D9) // violet 700
                EmeraldRich = Color(0xFF5B21B6) // violet 800
                EmeraldNight = Color(0xFF2E1065) // violet 950
                ForestDark = Color(0xFF4C1D95)
                ForestDeeper = Color(0xFF3B0764)
                ReaderMoss = Color(0xFF6D28D9)
                ReaderForest = Color(0xFFF5F3FF)
                SageTint = Color(0xFFEDE9FE)
                MintWash = Color(0xFFF5F3FF)
            }
            AppThemeColor.ORANGE -> {
                DeepEmerald = Color(0xFFEA580C) // orange 600
                Teal = Color(0xFFF97316) // orange 500
                TealDark = Color(0xFFC2410C) // orange 700
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
                DeepEmerald = Color(0xFFDC2626) // red 600
                Teal = Color(0xFFEF4444) // red 500
                TealDark = Color(0xFFB91C1C) // red 700
                EmeraldRich = Color(0xFF991B1B)
                EmeraldNight = Color(0xFF450A0A)
                ForestDark = Color(0xFF7F1D1D)
                ForestDeeper = Color(0xFF5A1414)
                ReaderMoss = Color(0xFFB91C1C)
                ReaderForest = Color(0xFFFEF2F2)
                SageTint = Color(0xFFFFECEC)
                MintWash = Color(0xFFFEF2F2)
            }
            AppThemeColor.CUSTOM -> {
                val parsed = runCatching { Color(android.graphics.Color.parseColor(customColorHex)) }
                    .getOrDefault(Color(0xFF0F4C3A))
                val isCustomDark = parsed.luminance() < 0.5
                setupBaseColors(isCustomDark)
                
                DeepEmerald = parsed
                Teal = parsed
                TealDark = parsed
                EmeraldRich = parsed
                EmeraldNight = parsed
                ForestDark = parsed
                ForestDeeper = parsed
                ReaderMoss = parsed
                ReaderForest = if (isCustomDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
                SageTint = if (isCustomDark) Color(0xFF2D3748) else Color(0xFFF3F4F6)
                MintWash = if (isCustomDark) Color(0xFF1F2937) else Color(0xFFF9F9F8)
            }
        }
    }
}
