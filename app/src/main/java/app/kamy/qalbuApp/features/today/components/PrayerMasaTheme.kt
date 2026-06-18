package app.kamy.qalbuApp.features.today.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import app.kamy.qalbuApp.domain.model.PrayerType

internal fun masaPrayerBackground(nextPrayer: PrayerType?): Brush {
    val colors = when (nextPrayer) {
        PrayerType.FAJR -> listOf(Color(0xFF1A3A5C), Color(0xFF4A6FA5))
        PrayerType.SUNRISE -> listOf(Color(0xFFE8A04C), Color(0xFFF5D78E))
        PrayerType.DHUHR -> listOf(Color(0xFF2E8B8B), Color(0xFF5CB8B2))
        PrayerType.ASR -> listOf(Color(0xFFD4845A), Color(0xFFE8B88A))
        PrayerType.MAGHRIB -> listOf(Color(0xFF6B3FA0), Color(0xFFB86B9E))
        PrayerType.ISHA -> listOf(Color(0xFF1E2A4A), Color(0xFF3D4E7A))
        null -> listOf(Color(0xFF1B5E4B), Color(0xFF2D8F6F))
    }
    return Brush.linearGradient(colors)
}
