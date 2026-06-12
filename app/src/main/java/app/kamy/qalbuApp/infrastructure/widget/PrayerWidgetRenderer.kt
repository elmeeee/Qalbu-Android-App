package app.kamy.qalbuApp.infrastructure.widget

import android.content.Context
import app.kamy.qalbuApp.infrastructure.notifications.PrayerScheduleCache
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.infrastructure.preferences.LocationPreferencesStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PrayerWidgetSnapshot(
    val cityLabel: String,
    val countdown: String,
    val subtitle: String
)

object PrayerWidgetRenderer {

    fun snapshot(context: Context): PrayerWidgetSnapshot? {
        val bundle = PrayerScheduleCache.load(context) ?: return null
        val now = System.currentTimeMillis()
        val prayers = bundle.adzanPrayers.sortedBy { it.fireAtMillis }
        if (prayers.isEmpty()) return null

        val cityLabel = LocationPreferencesStore.from(context).displayLabel()
            ?: context.getString(R.string.prayer_schedule)

        val lastPassed = prayers.lastOrNull { it.fireAtMillis <= now }
        val next = prayers.firstOrNull { it.fireAtMillis > now }
            ?: prayers.first().let { first ->
                first.copy(fireAtMillis = first.fireAtMillis + 24 * 60 * 60 * 1000)
            }

        val deltaMs = (next.fireAtMillis - now).coerceAtLeast(0L)
        val countdown = formatDuration(deltaMs)
        val timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(next.fireAtMillis))
        val subtitle = if (lastPassed != null && now - lastPassed.fireAtMillis < 15 * 60 * 1000) {
            context.getString(R.string.prayer_widget_grace, next.name)
        } else {
            context.getString(R.string.prayer_widget_next, next.name, timeLabel)
        }

        return PrayerWidgetSnapshot(
            cityLabel = cityLabel,
            countdown = countdown,
            subtitle = subtitle
        )
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}
