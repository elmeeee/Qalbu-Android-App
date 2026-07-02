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
                DeepEmerald = Color(0xFF064E3B)
                Teal = Color(0xFF0D9488)
                TealDark = Color(0xFF0F766E)
                EmeraldRich = Color(0xFF065F46)
                EmeraldNight = Color(0xFF022C22)
                ForestDark = Color(0xFF0B3D34)
                ForestDeeper = Color(0xFF051F1A)
                ReaderMoss = Color(0xFF0A3D2E)
                ReaderForest = Color(0xFF0F2A22)
                SageTint = Color(0xFFE7F0DF)
                MintWash = Color(0xFFF0FDF4)
            }
            AppThemeColor.INDIGO -> {
                DeepEmerald = Color(0xFF1E3A8A)
                Teal = Color(0xFF2563EB)
                TealDark = Color(0xFF1D4ED8)
                EmeraldRich = Color(0xFF3B82F6)
                EmeraldNight = Color(0xFF172554)
                ForestDark = Color(0xFF1E40AF)
                ForestDeeper = Color(0xFF0F172A)
                ReaderMoss = Color(0xFF1E3A8A)
                ReaderForest = Color(0xFF0F172A)
                SageTint = Color(0xFFDBEAFE)
                MintWash = Color(0xFFEFF6FF)
            }
            AppThemeColor.GOLD -> {
                DeepEmerald = Color(0xFF78350F)
                Teal = Color(0xFFD97706)
                TealDark = Color(0xFFB45309)
                EmeraldRich = Color(0xFFF59E0B)
                EmeraldNight = Color(0xFF451A03)
                ForestDark = Color(0xFF92400E)
                ForestDeeper = Color(0xFF180800)
                ReaderMoss = Color(0xFF78350F)
                ReaderForest = Color(0xFF451A03)
                SageTint = Color(0xFFFEF3C7)
                MintWash = Color(0xFFFFFBEB)
            }
        }
    }
}
