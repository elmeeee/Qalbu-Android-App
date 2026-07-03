package app.kamy.saatApp.design.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import app.kamy.saatApp.infrastructure.preferences.AppThemeColor

object AlKhatibColors {
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

    // Constant colors
    val Gold = Color(0xFFB45309)
    val GoldBright = Color(0xFFD4A017)
    val GoldDeep = Color(0xFFD97706)
    val AmberWash = Color(0xFFFFFBEB)
    val OffWhite = Color(0xFFF9F9F8)
    val ScreenBackground = Color(0xFFF8FAFC)
    val PureWhite = Color(0xFFFFFFFF)
    val SoftGrey = Color(0xFFE5E7EB)
    val LightGrey = Color(0xFFF3F4F6)
    val Slate500 = Color(0xFF64748B)
    val Slate700 = Color(0xFF334155)
    val Slate800 = Color(0xFF1E293B)
    val Slate900 = Color(0xFF0F172A)
    val PrayerCream = Color(0xFFFFF7ED)
    val PrayerCreamWarm = Color(0xFFFEF3C7)
    val PrayerMint = Color(0xFFF0FDFA)
    val IndigoDeep = Color(0xFF312E81)
    val SageMist = Color(0xFFF1F5F2)
    val PanelGrey = Color(0xFFE8EBEF)
    val PanelGreyAlt = Color(0xFFEEF2EE)
    val BlueLink = Color(0xFF2563EB)
    val IndigoAccent = Color(0xFF4F46E5)
    val Danger = Color(0xFFEF4444)

    fun applyTheme(theme: AppThemeColor) {
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
        }
    }
}
