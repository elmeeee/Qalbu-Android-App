package app.kamy.saatApp.infrastructure.repository

import app.kamy.saatApp.domain.prayer.LocalPrayerCalculator
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.domain.prayer.PrayerMethodOption
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleBundle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

import app.kamy.saatApp.domain.prayer.PrayerMadhab

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
    val hijriDay: Int? = null,
    val scheduleBundle: PrayerScheduleBundle? = null
)

data class PrayerCalendarDay(
    val day: Int,
    val gregorianLabel: String,
    val hijriLabel: String?,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val khgtEventTitle: String? = null,
    val isImportantDay: Boolean = false,
    val hijriDay: Int? = null
)

@Singleton
class AlAdhanRepository @Inject constructor() {

    suspend fun fetchCalculationMethods(): List<PrayerMethodOption> =
        LocalPrayerCalculator.calculationMethodOptions()

    suspend fun fetchTimings(
        latitude: Double,
        longitude: Double,
        cityName: String? = null,
        method: PrayerCalculationMethod = PrayerCalculationMethod.defaultMethod,
        madhab: PrayerMadhab = PrayerMadhab.SHAFI,
        timestamp: Long = System.currentTimeMillis() / 1000L
    ): PrayerDayResult = LocalPrayerCalculator.fetchTimings(
        latitude = latitude,
        longitude = longitude,
        cityName = cityName,
        method = method,
        madhab = madhab,
        timestamp = timestamp
    )

    suspend fun fetchMonthCalendar(
        year: Int,
        month: Int,
        latitude: Double,
        longitude: Double,
        method: PrayerCalculationMethod = PrayerCalculationMethod.defaultMethod,
        madhab: PrayerMadhab = PrayerMadhab.SHAFI
    ): List<PrayerCalendarDay> = LocalPrayerCalculator.fetchMonthCalendar(
        year = year,
        month = month,
        latitude = latitude,
        longitude = longitude,
        method = method,
        madhab = madhab
    )
}
