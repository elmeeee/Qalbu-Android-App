package app.kamy.saatApp.infrastructure.widget

import android.content.Context
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleCache
import app.kamy.saatApp.infrastructure.preferences.KhgtWidgetCache
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PrayerWidgetSlot(
    val type: PrayerType,
    val label: String,
    val time: String,
    val isActive: Boolean
)

data class PrayerWidgetSnapshot(
    val brandLabel: String,
    val cityLabel: String,
    val hijriLabel: String?,
    val gregorianLabel: String?,
    val khgtEventTitle: String? = null,
    val nextPrayerLabel: String,
    val nextPrayerName: String,
    val countdown: String,
    val countdownCompact: String,
    val nextPrayerTime: String,
    val slots: List<PrayerWidgetSlot>
)

object PrayerWidgetRenderer {

    fun snapshot(context: Context): PrayerWidgetSnapshot {
        val bundle = PrayerScheduleCache.load(context)
        if (bundle == null) {
            app.kamy.saatApp.infrastructure.notifications.PrayerNotificationCoordinator.rescheduleFromCache(context)
            return fallbackSnapshot(context)
        }
        val meta = PrayerScheduleCache.loadMeta(context)
        val now = System.currentTimeMillis()
        val prayers = bundle.adzanPrayers.sortedBy { it.fireAtMillis }
        if (prayers.isEmpty()) return fallbackSnapshot(context)

        val cityLabel = meta?.cityLabel
            ?: LocationPreferencesStore.from(context).displayLabel()
            ?: context.getString(R.string.prayer_schedule)

        val lastPassed = prayers.lastOrNull { it.fireAtMillis <= now }
        val next = prayers.firstOrNull { it.fireAtMillis > now }
            ?: prayers.first().let { first ->
                first.copy(fireAtMillis = first.fireAtMillis + 24 * 60 * 60 * 1000)
            }

        val deltaMs = (next.fireAtMillis - now).coerceAtLeast(0L)
        val countdown = formatDuration(deltaMs)
        val countdownCompact = formatDurationCompact(deltaMs)
        val timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(next.fireAtMillis))
        val nextType = PrayerType.fromAladhanKey(next.name)
        val nextName = nextType?.let { prayerLabel(context, it) } ?: next.name

        val nextPrayerLabel = if (lastPassed != null && now - lastPassed.fireAtMillis < 15 * 60 * 1000) {
            context.getString(R.string.prayer_widget_in_progress)
        } else {
            context.getString(R.string.prayer_widget_next_label)
        }

        val nextPrayerTime = timeLabel

        val slots = buildSlots(context, meta, nextType)

        return PrayerWidgetSnapshot(
            brandLabel = context.getString(R.string.app_name),
            cityLabel = cityLabel,
            hijriLabel = meta?.hijriLabel ?: KhgtWidgetCache.hijriLabel(context),
            gregorianLabel = meta?.gregorianLabel,
            khgtEventTitle = KhgtWidgetCache.eventTitle(context),
            nextPrayerLabel = nextPrayerLabel,
            nextPrayerName = nextName,
            countdown = countdown,
            countdownCompact = countdownCompact,
            nextPrayerTime = nextPrayerTime,
            slots = slots
        )
    }

    private fun buildSlots(
        context: Context,
        meta: PrayerScheduleCache.WidgetMeta?,
        activeType: PrayerType?
    ): List<PrayerWidgetSlot> {
        val timings = meta?.timings.orEmpty()
        return PrayerType.ADZAN_NOTIFICATION_PRAYERS.map { type ->
            val time = timings[type.aladhanKey] ?: "--:--"
            PrayerWidgetSlot(
                type = type,
                label = prayerLabel(context, type),
                time = time,
                isActive = type == activeType
            )
        }
    }

    private fun prayerLabel(context: Context, type: PrayerType): String = when (type) {
        PrayerType.FAJR -> context.getString(R.string.prayer_fajr)
        PrayerType.DHUHR -> context.getString(R.string.prayer_dhuhr)
        PrayerType.ASR -> context.getString(R.string.prayer_asr)
        PrayerType.MAGHRIB -> context.getString(R.string.prayer_maghrib)
        PrayerType.ISHA -> context.getString(R.string.prayer_isha)
        PrayerType.SUNRISE -> context.getString(R.string.prayer_sunrise)
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    private fun formatDurationCompact(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            "%d:%02d".format(hours, minutes)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    private fun fallbackSnapshot(context: Context): PrayerWidgetSnapshot {
        val cityLabel = LocationPreferencesStore.from(context).displayLabel()
            ?: context.getString(R.string.prayer_schedule)
        return PrayerWidgetSnapshot(
            brandLabel = "SĀAT",
            cityLabel = cityLabel,
            hijriLabel = null,
            gregorianLabel = null,
            nextPrayerLabel = context.getString(R.string.prayer_widget_next_title),
            nextPrayerName = context.getString(R.string.prayer_fajr),
            countdown = "--:--",
            countdownCompact = "--:--",
            nextPrayerTime = "--:--",
            slots = emptyList()
        )
    }
}
