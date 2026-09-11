package app.kamy.saatApp.infrastructure.widget

import android.content.Context
import android.content.res.Configuration
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache
import app.kamy.saatApp.infrastructure.notifications.PrayerNotificationItem
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleCache
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.KhgtWidgetCache
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PrayerWidgetSlotData(
    val key: String,
    val label: String,
    val time: String,
    val iconRes: Int,
    val isActive: Boolean
)

data class PrayerWidgetSnapshot(
    val isRamadan: Boolean,
    val streakCount: Int,
    val isArrived: Boolean,
    val isFinished: Boolean,
    val stateTitle: String,
    val statePrayerName: String,
    val stateTimeStr: String,
    val nextPrayerName: String,
    val nextPrayerTimeStr: String,
    val chronometerBase: Long,
    val iftarTimeStr: String,
    val imsakTimeStr: String,
    val slots: List<PrayerWidgetSlotData>
)

object PrayerWidgetRenderer {

    private const val PRAYER_ARRIVED_WINDOW_MS = 15 * 60 * 1000L
    private const val PRAYER_FINISHED_WINDOW_MS = 45 * 60 * 1000L

    fun snapshot(context: Context): PrayerWidgetSnapshot {
        val appContext = context.applicationContext
        val lang = AppLanguageStore.from(appContext).current()
        val locale = Locale.forLanguageTag(lang.tag)
        val config = Configuration(appContext.resources.configuration).apply {
            setLocale(locale)
        }
        val localizedContext = runCatching {
            appContext.createConfigurationContext(config)
        }.getOrDefault(appContext)

        val bundle = PrayerScheduleCache.load(appContext)
        val meta = PrayerScheduleCache.loadMeta(appContext)
        val cachedDay = PrayerDayCache.load(appContext)

        val rawPrayers: List<PrayerNotificationItem> = when {
            bundle != null && bundle.adzanPrayers.isNotEmpty() -> bundle.adzanPrayers
            cachedDay != null && cachedDay.timings.isNotEmpty() -> {
                cachedDay.timings.map { entry ->
                    PrayerNotificationItem(entry.type.aladhanKey, entry.date.time)
                }
            }
            else -> emptyList()
        }

        val now = System.currentTimeMillis()
        val prayers = rawPrayers.sortedBy { it.fireAtMillis }

        val hijriLabel = meta?.hijriLabel
            ?: cachedDay?.hijriLabel
            ?: KhgtWidgetCache.hijriLabel(appContext)
            ?: ""
        val isRamadan = hijriLabel.contains("Ramadan", ignoreCase = true)
        val streakCount = PrayerTrackerStore.currentStreak(appContext)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val arrivedPrayer = prayers.firstOrNull { prayer ->
            now >= prayer.fireAtMillis && now < (prayer.fireAtMillis + PRAYER_ARRIVED_WINDOW_MS)
        }

        val finishedPrayer = if (arrivedPrayer == null) {
            prayers.firstOrNull { prayer ->
                now >= (prayer.fireAtMillis + PRAYER_ARRIVED_WINDOW_MS) &&
                        now < (prayer.fireAtMillis + PRAYER_FINISHED_WINDOW_MS)
            }
        } else null

        val nextPrayer = prayers.firstOrNull { it.fireAtMillis > now }
            ?: prayers.firstOrNull()?.let { first ->
                first.copy(fireAtMillis = first.fireAtMillis + 24 * 60 * 60 * 1000L)
            } ?: PrayerNotificationItem("Fajr", now + 3600000L)

        val nextPrayerType = PrayerType.fromAladhanKey(nextPrayer.name) ?: PrayerType.FAJR
        val nextPrayerName = prayerLabel(localizedContext, nextPrayerType)
        val nextPrayerTimeStr = timeFormat.format(Date(nextPrayer.fireAtMillis))

        val timeRemainingMs = (nextPrayer.fireAtMillis - now).coerceAtLeast(0L)
        val chronometerBase = android.os.SystemClock.elapsedRealtime() + timeRemainingMs

        val arrivedType = arrivedPrayer?.let { PrayerType.fromAladhanKey(it.name) } ?: PrayerType.FAJR
        val arrivedName = prayerLabel(localizedContext, arrivedType)
        val arrivedTimeStr = if (arrivedPrayer != null) timeFormat.format(Date(arrivedPrayer.fireAtMillis)) else ""

        val finishedType = finishedPrayer?.let { PrayerType.fromAladhanKey(it.name) } ?: PrayerType.FAJR
        val finishedName = prayerLabel(localizedContext, finishedType)
        val finishedTitle = if (isRamadan && finishedType == PrayerType.MAGHRIB) {
            localizedContext.getString(R.string.live_countdown_iftar_finished)
        } else {
            localizedContext.getString(R.string.live_countdown_prayer_finished_format, finishedName)
        }

        val stateTitle = when {
            arrivedPrayer != null -> localizedContext.getString(R.string.live_countdown_prayer_time_arrived)
            finishedPrayer != null -> finishedTitle
            isRamadan && nextPrayerType == PrayerType.MAGHRIB -> localizedContext.getString(R.string.live_countdown_towards_iftar)
            else -> nextPrayerName
        }

        val statePrayerName = when {
            arrivedPrayer != null -> arrivedName
            finishedPrayer != null -> "$nextPrayerName • $nextPrayerTimeStr"
            isRamadan && nextPrayerType == PrayerType.MAGHRIB -> localizedContext.getString(R.string.live_countdown_towards_iftar)
            else -> nextPrayerName
        }

        val stateTimeStr = when {
            arrivedPrayer != null -> arrivedTimeStr
            else -> nextPrayerTimeStr
        }

        // Compute 6 timings: Imsak, Subuh, Dzuhur, Ashar, Maghrib, Isya
        val timingsMap = meta?.timings
            ?: cachedDay?.timings?.associate { it.type.aladhanKey to it.rawTime }
            ?: emptyMap()

        val fajrTime = timingsMap["Fajr"] ?: "04:45"
        val dhuhrTime = timingsMap["Dhuhr"] ?: "11:54"
        val asrTime = timingsMap["Asr"] ?: "15:12"
        val maghribTime = timingsMap["Maghrib"] ?: "18:11"
        val ishaTime = timingsMap["Isha"] ?: "19:23"

        val imsakTime = timingsMap["Imsak"] ?: run {
            val parts = fajrTime.split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull() ?: 4
                val m = parts[1].toIntOrNull() ?: 45
                val totalM = (h * 60 + m - 10 + 1440) % 1440
                String.format(Locale.US, "%02d:%02d", totalM / 60, totalM % 60)
            } else "04:35"
        }

        val activeType = when {
            arrivedPrayer != null -> arrivedType
            else -> nextPrayerType
        }

        val slots = listOf(
            PrayerWidgetSlotData("Imsak", localizedContext.getString(R.string.prayer_imsak), imsakTime, R.drawable.ic_widget_imsak, false),
            PrayerWidgetSlotData("Fajr", localizedContext.getString(R.string.prayer_fajr), fajrTime, R.drawable.ic_widget_subuh, activeType == PrayerType.FAJR),
            PrayerWidgetSlotData("Dhuhr", localizedContext.getString(R.string.prayer_dhuhr), dhuhrTime, R.drawable.ic_widget_dzuhur, activeType == PrayerType.DHUHR),
            PrayerWidgetSlotData("Asr", localizedContext.getString(R.string.prayer_asr), asrTime, R.drawable.ic_widget_ashar, activeType == PrayerType.ASR),
            PrayerWidgetSlotData("Maghrib", localizedContext.getString(R.string.prayer_maghrib), maghribTime, R.drawable.ic_widget_maghrib, activeType == PrayerType.MAGHRIB),
            PrayerWidgetSlotData("Isha", localizedContext.getString(R.string.prayer_isha), ishaTime, R.drawable.ic_widget_isya, activeType == PrayerType.ISHA)
        )

        return PrayerWidgetSnapshot(
            isRamadan = isRamadan,
            streakCount = streakCount,
            isArrived = arrivedPrayer != null,
            isFinished = finishedPrayer != null,
            stateTitle = stateTitle,
            statePrayerName = statePrayerName,
            stateTimeStr = stateTimeStr,
            nextPrayerName = nextPrayerName,
            nextPrayerTimeStr = nextPrayerTimeStr,
            chronometerBase = chronometerBase,
            iftarTimeStr = maghribTime,
            imsakTimeStr = imsakTime,
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
