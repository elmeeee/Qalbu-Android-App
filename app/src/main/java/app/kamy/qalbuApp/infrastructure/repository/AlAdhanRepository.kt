package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.infrastructure.network.api.AlAdhanApiService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class PrayerEntry(
    val type: PrayerType,
    val rawTime: String,
    val date: Date
)

data class PrayerDayResult(
    val timings: List<PrayerEntry>,
    val cityName: String? = null,
    val hijriLabel: String? = null,
    val gregorianLabel: String? = null
)

/**
 * Mirrors iOS Infrastructure/Services prayer-time fetcher in PrayerTimesController.
 *
 * Calls the public Al-Adhan timings endpoint and reshapes the response into a list
 * of [PrayerEntry] sorted by time-of-day.
 */
@Singleton
class AlAdhanRepository @Inject constructor(
    private val api: AlAdhanApiService
) {
    private val timeFormatPatterns = listOf("HH:mm (zzz)", "HH:mm")

    suspend fun fetchTimings(
        latitude: Double,
        longitude: Double,
        cityName: String? = null,
        method: Int = 20,
        school: Int = 0,
        timestamp: Long = System.currentTimeMillis() / 1000L
    ): PrayerDayResult {
        val resp = qfCall {
            api.getTimings(
                timestamp = timestamp,
                latitude = latitude,
                longitude = longitude,
                method = method,
                school = school
            )
        }
        val data = resp.data ?: return PrayerDayResult(emptyList(), cityName)
        val timings = data.timings.orEmpty()
        val baseDate = todayDate()

        val entries = PrayerType.entries.mapNotNull { type ->
            val raw = timings[type.aladhanKey] ?: return@mapNotNull null
            val parsed = parseTime(raw, baseDate) ?: return@mapNotNull null
            PrayerEntry(type = type, rawTime = raw, date = parsed)
        }.sortedBy { it.date }

        return PrayerDayResult(
            timings = entries,
            cityName = cityName,
            hijriLabel = data.date?.hijri?.date,
            gregorianLabel = data.date?.gregorian?.date
        )
    }

    private fun todayDate(): Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun parseTime(raw: String, dayBase: Date): Date? {
        // Al-Adhan returns "05:30" or "05:30 (PST)". Try both.
        for (pattern in timeFormatPatterns) {
            try {
                val formatter = SimpleDateFormat(pattern, Locale.US)
                val parsed = formatter.parse(raw) ?: continue
                val cal = Calendar.getInstance().apply { time = dayBase }
                val parsedCal = Calendar.getInstance().apply { time = parsed }
                cal.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                return cal.time
            } catch (_: Throwable) {
                continue
            }
        }
        return null
    }
}
