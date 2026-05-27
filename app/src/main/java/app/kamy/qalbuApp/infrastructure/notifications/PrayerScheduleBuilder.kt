package app.kamy.qalbuApp.infrastructure.notifications

import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.infrastructure.repository.PrayerEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PrayerScheduleBuilder {

    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /** Builds notification schedule from Al-Adhan timings map (iOS PrayerTimesController parity). */
    fun fromTimings(
        timings: Map<String, String>,
        scheduleDay: Date = Date()
    ): PrayerScheduleBundle? {
        val calendar = Calendar.getInstance()
        val dayKey = dayKeyFormat.format(scheduleDay)

        fun resolveTime(key: String): Date? {
            val raw = timings[key] ?: return null
            val clean = raw.substringBefore(" ").trim()
            val timeOnly = parseHm(clean) ?: return null
            val dayCal = Calendar.getInstance().apply {
                time = scheduleDay
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            dayCal.set(Calendar.HOUR_OF_DAY, timeOnly.get(Calendar.HOUR_OF_DAY))
            dayCal.set(Calendar.MINUTE, timeOnly.get(Calendar.MINUTE))
            return dayCal.time
        }

        fun resolveNightDivision(key: String): Date? {
            val date = resolveTime(key) ?: return null
            val fajr = resolveTime("Fajr")
            val fajrHour = fajr?.let { calendar.apply { time = it }.get(Calendar.HOUR_OF_DAY) } ?: 5
            val cal = Calendar.getInstance().apply { time = date }
            if (cal.get(Calendar.HOUR_OF_DAY) < fajrHour) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.time
        }

        val adzanKeys = listOf(
            PrayerType.FAJR,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )
        val adzan = adzanKeys.mapNotNull { type ->
            resolveTime(type.aladhanKey)?.let {
                PrayerNotificationItem(type.aladhanKey, it.time)
            }
        }.sortedBy { it.fireAtMillis }

        if (adzan.isEmpty()) return null

        val imsak = resolveTime("Imsak")?.let { PrayerNotificationItem("Imsak", it.time) }

        val night = NightDivisionKind.entries.mapNotNull { kind ->
            resolveNightDivision(kind.aladhanKey)?.let {
                NightDivisionItem(kind, it.time)
            }
        }

        return PrayerScheduleBundle(
            adzanPrayers = adzan,
            imsak = imsak,
            nightDivisions = night,
            dayKey = dayKey
        )
    }

    /** Fallback when only [PrayerEntry] list is available (no imsak/night keys). */
    fun fromPrayerEntries(entries: List<PrayerEntry>): PrayerScheduleBundle? {
        val adzanTypes = setOf(
            PrayerType.FAJR,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )
        val adzan = entries
            .filter { it.type in adzanTypes }
            .map { PrayerNotificationItem(it.type.aladhanKey, it.date.time) }
            .sortedBy { it.fireAtMillis }
        if (adzan.isEmpty()) return null
        return PrayerScheduleBundle(
            adzanPrayers = adzan,
            imsak = null,
            nightDivisions = emptyList(),
            dayKey = dayKeyFormat.format(Date())
        )
    }

    fun upcomingOccurrences(fireAtMillis: Long, now: Long = System.currentTimeMillis()): List<Long> {
        if (fireAtMillis > now) return listOf(fireAtMillis)
        val tomorrow = fireAtMillis + 24 * 60 * 60 * 1000L
        return if (tomorrow > now) listOf(tomorrow) else emptyList()
    }

    private fun parseHm(clean: String): Calendar? = try {
        val formatter = SimpleDateFormat("HH:mm", Locale.US)
        val parsed = formatter.parse(clean) ?: return null
        Calendar.getInstance().apply { time = parsed }
    } catch (_: Throwable) {
        null
    }
}
