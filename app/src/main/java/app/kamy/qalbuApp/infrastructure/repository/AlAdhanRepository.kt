package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.core.error.qfCall
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.domain.prayer.PrayerCalculationMethod
import app.kamy.qalbuApp.domain.prayer.PrayerMethodOption
import app.kamy.qalbuApp.infrastructure.network.api.AlAdhanApiService
import app.kamy.qalbuApp.infrastructure.notifications.PrayerScheduleBuilder
import app.kamy.qalbuApp.infrastructure.notifications.PrayerScheduleBundle
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
    val gregorianLabel: String? = null,
    val scheduleBundle: PrayerScheduleBundle? = null
)

@Singleton
class AlAdhanRepository @Inject constructor(
    private val api: AlAdhanApiService
) {
    private val timeFormatPatterns = listOf("HH:mm (zzz)", "HH:mm")

    suspend fun fetchCalculationMethods(): List<PrayerMethodOption> {
        val resp = qfCall { api.getMethods() }
        val fromApi = resp.data.orEmpty()
            .filterKeys { it != "CUSTOM" }
            .map { (key, entry) ->
                val method = PrayerCalculationMethod.fromAladhanId(entry.id)
                PrayerMethodOption(
                    aladhanId = entry.id,
                    apiKey = key,
                    name = entry.name?.takeIf { it.isNotBlank() } ?: method.displayName,
                    method = method
                )
            }
        val muhammadiyah = PrayerMethodOption(
            aladhanId = PrayerCalculationMethod.MUHAMMADIYAH.aladhanMethodId,
            apiKey = "MUHAMMADIYAH",
            name = PrayerCalculationMethod.MUHAMMADIYAH.displayName,
            method = PrayerCalculationMethod.MUHAMMADIYAH
        )
        val kemenag = fromApi.firstOrNull { it.aladhanId == 20 }
        val rest = fromApi
            .filter { it.aladhanId != 20 }
            .sortedBy { it.name.lowercase() }
        return buildList {
            add(muhammadiyah)
            if (kemenag != null) add(kemenag)
            addAll(rest)
        }
    }

    suspend fun fetchTimings(
        latitude: Double,
        longitude: Double,
        cityName: String? = null,
        method: PrayerCalculationMethod = PrayerCalculationMethod.defaultMethod,
        timestamp: Long = System.currentTimeMillis() / 1000L
    ): PrayerDayResult {
        val resp = qfCall {
            api.getTimings(
                timestamp = timestamp,
                latitude = latitude,
                longitude = longitude,
                method = method.aladhanMethodId,
                school = method.aladhanSchool,
                tune = method.aladhanTune,
                methodSettings = method.aladhanMethodSettings
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

        val (hijriLabel, gregorianLabel) = AlAdhanDateLabels.fromApiDate(data.date)
        val scheduleBundle = PrayerScheduleBuilder.fromTimings(timings, baseDate)
        return PrayerDayResult(
            timings = entries,
            cityName = cityName,
            hijriLabel = hijriLabel,
            gregorianLabel = gregorianLabel,
            scheduleBundle = scheduleBundle
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
