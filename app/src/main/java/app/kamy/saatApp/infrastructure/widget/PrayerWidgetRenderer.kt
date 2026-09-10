package app.kamy.saatApp.infrastructure.widget

import android.content.Context
import android.content.res.Configuration
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleCache
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.KhgtWidgetCache
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import java.util.Locale

data class PrayerWidgetSlot(
    val type: PrayerType,
    val label: String,
    val time: String,
    val isActive: Boolean,
    val statusText: String? = null
)

data class PrayerWidgetSnapshot(
    val title: String,
    val cityLabel: String,
    val hijriLabel: String?,
    val fullSubtitle: String,
    val slots: List<PrayerWidgetSlot>
)

object PrayerWidgetRenderer {

    fun snapshot(context: Context): PrayerWidgetSnapshot {
        val lang = AppLanguageStore.from(context).current()
        val locale = Locale.forLanguageTag(lang.tag)
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        val localizedContext = runCatching {
            context.createConfigurationContext(config)
        }.getOrDefault(context)

        val bundle = PrayerScheduleCache.load(context)
        val meta = PrayerScheduleCache.loadMeta(context)
        val cachedDay = PrayerDayCache.load(context)

        val now = System.currentTimeMillis()
        val prayers = bundle?.adzanPrayers?.sortedBy { it.fireAtMillis }.orEmpty()

        val cityLabel = meta?.cityLabel
            ?: cachedDay?.cityName
            ?: LocationPreferencesStore.from(context).displayLabel()
            ?: localizedContext.getString(R.string.prayer_schedule)

        val hijriLabel = meta?.hijriLabel
            ?: cachedDay?.hijriLabel
            ?: KhgtWidgetCache.hijriLabel(context)

        val subtitle = if (!hijriLabel.isNullOrBlank()) {
            "$cityLabel, $hijriLabel"
        } else {
            cityLabel
        }

        val lastPassed = prayers.lastOrNull { it.fireAtMillis <= now }
        val next = prayers.firstOrNull { it.fireAtMillis > now }
            ?: prayers.firstOrNull()?.let { first ->
                first.copy(fireAtMillis = first.fireAtMillis + 24 * 60 * 60 * 1000)
            }

        val isNowInProgress = lastPassed != null && (now - lastPassed.fireAtMillis < 20 * 60 * 1000L)
        val activePrayerEntry = if (isNowInProgress) lastPassed else next

        val activeType = activePrayerEntry?.let { PrayerType.fromAladhanKey(it.name) } ?: PrayerType.FAJR
        val activeStatus = if (isNowInProgress) {
            localizedContext.getString(R.string.prayer_widget_status_now)
        } else {
            localizedContext.getString(R.string.prayer_widget_status_soon)
        }

        val timings = meta?.timings
            ?: cachedDay?.timings?.associate { it.type.aladhanKey to it.rawTime }
            ?: emptyMap()

        val slots = PrayerType.ADZAN_NOTIFICATION_PRAYERS.map { type ->
            val time = timings[type.aladhanKey] ?: "--:--"
            val isActive = (type == activeType)
            PrayerWidgetSlot(
                type = type,
                label = prayerLabel(localizedContext, type),
                time = time,
                isActive = isActive,
                statusText = if (isActive) activeStatus else null
            )
        }

        return PrayerWidgetSnapshot(
            title = localizedContext.getString(R.string.prayer_widget_title),
            cityLabel = cityLabel,
            hijriLabel = hijriLabel,
            fullSubtitle = subtitle,
            slots = slots
        )
    }

    private fun prayerLabel(context: Context, type: PrayerType): String = when (type) {
        PrayerType.FAJR -> context.getString(R.string.prayer_fajr)
        PrayerType.DHUHR -> context.getString(R.string.prayer_dhuhr)
        PrayerType.ASR -> context.getString(R.string.prayer_asr)
        PrayerType.MAGHRIB -> context.getString(R.string.prayer_maghrib)
        PrayerType.ISHA -> context.getString(R.string.prayer_isha)
        PrayerType.SUNRISE -> context.getString(R.string.prayer_sunrise)
    }
}
