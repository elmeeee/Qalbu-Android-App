package app.kamy.saatApp.domain.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerAdjustments
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import app.kamy.saatApp.domain.prayer.PrayerMethodOption
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.repository.PrayerCalendarDay
import app.kamy.saatApp.infrastructure.repository.PrayerDayResult
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleBuilder
import java.text.SimpleDateFormat
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object LocalPrayerCalculator {

    private val timeOut = SimpleDateFormat("HH:mm", Locale.US)
    private val gregorianOut = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    private val hijriMonthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())

    fun fetchTimings(
        latitude: Double,
        longitude: Double,
        cityName: String? = null,
        method: PrayerCalculationMethod = PrayerCalculationMethod.defaultMethod,
        madhab: PrayerMadhab = PrayerMadhab.SHAFI,
        timestamp: Long = System.currentTimeMillis() / 1000L
    ): PrayerDayResult {
        val day = Date(timestamp * 1000L)
        val cal = Calendar.getInstance().apply { time = day }
        val components = DateComponents(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        val coords = Coordinates(latitude, longitude)
        val params = parametersFor(method, madhab)
        val prayerTimes = PrayerTimes(coords, components, params)

        val baseDate = startOfDay(day)
        val timingsMap = linkedMapOf(
            PrayerType.FAJR.aladhanKey to formatPrayerTime(prayerTimes.fajr),
            PrayerType.SUNRISE.aladhanKey to formatPrayerTime(prayerTimes.sunrise),
            PrayerType.DHUHR.aladhanKey to formatPrayerTime(prayerTimes.dhuhr),
            PrayerType.ASR.aladhanKey to formatPrayerTime(prayerTimes.asr),
            PrayerType.MAGHRIB.aladhanKey to formatPrayerTime(prayerTimes.maghrib),
            PrayerType.ISHA.aladhanKey to formatPrayerTime(prayerTimes.isha)
        )

        val maghribTime = prayerTimes.maghrib
        val tomorrowCal = Calendar.getInstance().apply {
            time = day
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val tomorrowComponents = DateComponents(
            tomorrowCal.get(Calendar.YEAR),
            tomorrowCal.get(Calendar.MONTH) + 1,
            tomorrowCal.get(Calendar.DAY_OF_MONTH)
        )
        val tomorrowPrayerTimes = PrayerTimes(coords, tomorrowComponents, params)
        val tomorrowFajrTime = tomorrowPrayerTimes.fajr

        if (maghribTime != null && tomorrowFajrTime != null) {
            val maghribMillis = maghribTime.time
            val tomorrowFajrMillis = tomorrowFajrTime.time
            val nightDuration = tomorrowFajrMillis - maghribMillis
            if (nightDuration > 0) {
                val firstThirdMillis = maghribMillis + nightDuration / 3
                val midnightMillis = maghribMillis + nightDuration / 2
                val lastThirdMillis = maghribMillis + (2 * nightDuration) / 3

                timingsMap["Firstthird"] = timeOut.format(Date(firstThirdMillis))
                timingsMap["Midnight"] = timeOut.format(Date(midnightMillis))
                timingsMap["Lastthird"] = timeOut.format(Date(lastThirdMillis))
            }
        }

        val entries = PrayerType.entries.mapNotNull { type ->
            val raw = timingsMap[type.aladhanKey] ?: return@mapNotNull null
            val parsed = parseTime(raw, baseDate) ?: return@mapNotNull null
            PrayerEntry(type = type, rawTime = raw, date = parsed)
        }.sortedBy { it.date }

        val hijrah = HijrahDate.from(
            cal.toInstant().atZone(cal.timeZone.toZoneId()).toLocalDate()
        )
        val hijriLabel = buildString {
            append(hijrah.get(ChronoField.DAY_OF_MONTH))
            append(' ')
            append(hijriMonthFormatter.format(hijrah))
            append(' ')
            append(hijrah.get(ChronoField.YEAR_OF_ERA))
        }

        return PrayerDayResult(
            timings = entries,
            cityName = cityName,
            hijriLabel = hijriLabel,
            gregorianLabel = gregorianOut.format(day),
            hijriDay = hijrah.get(ChronoField.DAY_OF_MONTH),
            scheduleBundle = PrayerScheduleBuilder.fromTimings(timingsMap, baseDate)
        )
    }

    fun fetchMonthCalendar(
        year: Int,
        month: Int,
        latitude: Double,
        longitude: Double,
        method: PrayerCalculationMethod = PrayerCalculationMethod.defaultMethod,
        madhab: PrayerMadhab = PrayerMadhab.SHAFI
    ): List<PrayerCalendarDay> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)

        return (1..maxDay).mapNotNull { day ->
            cal.set(Calendar.DAY_OF_MONTH, day)
            val result = fetchTimings(
                latitude = latitude,
                longitude = longitude,
                method = method,
                madhab = madhab,
                timestamp = cal.timeInMillis / 1000L
            )
            val byKey = result.timings.associate { it.type.aladhanKey to formatHm(it.date) }
            PrayerCalendarDay(
                day = day,
                gregorianLabel = "$day $monthName $year",
                hijriLabel = result.hijriLabel,
                fajr = byKey[PrayerType.FAJR.aladhanKey].orEmpty(),
                sunrise = byKey[PrayerType.SUNRISE.aladhanKey].orEmpty(),
                dhuhr = byKey[PrayerType.DHUHR.aladhanKey].orEmpty(),
                asr = byKey[PrayerType.ASR.aladhanKey].orEmpty(),
                maghrib = byKey[PrayerType.MAGHRIB.aladhanKey].orEmpty(),
                isha = byKey[PrayerType.ISHA.aladhanKey].orEmpty(),
                hijriDay = result.hijriDay
            )
        }
    }

    fun calculationMethodOptions(): List<PrayerMethodOption> {
        val muhammadiyah = PrayerCalculationMethod.MUHAMMADIYAH
        val kemenag = PrayerCalculationMethod.KEMENAG
        val rest = PrayerCalculationMethod.entries
            .filter { it != muhammadiyah && it != kemenag }
            .sortedBy { it.displayName.lowercase() }
            .map { method ->
                PrayerMethodOption(
                    aladhanId = method.aladhanMethodId,
                    apiKey = method.rawValue.uppercase(),
                    name = method.displayName,
                    method = method
                )
            }
        return buildList {
            add(
                PrayerMethodOption(
                    aladhanId = muhammadiyah.aladhanMethodId,
                    apiKey = "MUHAMMADIYAH",
                    name = muhammadiyah.displayName,
                    method = muhammadiyah
                )
            )
            add(
                PrayerMethodOption(
                    aladhanId = kemenag.aladhanMethodId,
                    apiKey = "KEMENAG",
                    name = kemenag.displayName,
                    method = kemenag
                )
            )
            addAll(rest)
        }
    }

    private fun parametersFor(method: PrayerCalculationMethod, madhab: PrayerMadhab): CalculationParameters {
        val base = when (method) {
            PrayerCalculationMethod.KARACHI -> CalculationMethod.KARACHI
            PrayerCalculationMethod.ISNA -> CalculationMethod.NORTH_AMERICA
            PrayerCalculationMethod.MWL -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            PrayerCalculationMethod.MCW -> CalculationMethod.MOON_SIGHTING_COMMITTEE
            PrayerCalculationMethod.UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA
            PrayerCalculationMethod.EGYPTIAN -> CalculationMethod.EGYPTIAN
            PrayerCalculationMethod.KUWAIT -> CalculationMethod.KUWAIT
            PrayerCalculationMethod.QATAR -> CalculationMethod.QATAR
            PrayerCalculationMethod.DUBAI -> CalculationMethod.DUBAI
            PrayerCalculationMethod.MUIS,
            PrayerCalculationMethod.JAKIM,
            PrayerCalculationMethod.BRUNEI,
            PrayerCalculationMethod.KEMENAG -> CalculationMethod.SINGAPORE
            PrayerCalculationMethod.TEHRAN,
            PrayerCalculationMethod.JAFARI,
            PrayerCalculationMethod.MUHAMMADIYAH,
            PrayerCalculationMethod.MOROCCO,
            PrayerCalculationMethod.TURKEY,
            PrayerCalculationMethod.FRANCE,
            PrayerCalculationMethod.TUNISIA,
            PrayerCalculationMethod.ALGERIA,
            PrayerCalculationMethod.JORDAN,
            PrayerCalculationMethod.RUSSIA,
            PrayerCalculationMethod.LISBON -> CalculationMethod.OTHER
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }.parameters

        when (method) {
            PrayerCalculationMethod.MUHAMMADIYAH -> {
                base.fajrAngle = 18.0
                base.ishaAngle = 18.0
            }
            PrayerCalculationMethod.KEMENAG -> {
                base.fajrAngle = 20.0
                base.ishaAngle = 18.0
            }
            PrayerCalculationMethod.MUIS -> {
                base.fajrAngle = 20.0
                base.ishaAngle = 18.0
            }
            PrayerCalculationMethod.JAKIM -> {
                base.fajrAngle = 20.0
                base.ishaAngle = 18.0
            }
            PrayerCalculationMethod.BRUNEI -> {
                base.fajrAngle = 20.0
                base.ishaAngle = 18.0
            }
            PrayerCalculationMethod.TEHRAN -> {
                base.fajrAngle = 17.7
                base.ishaAngle = 14.0
            }
            PrayerCalculationMethod.JAFARI -> {
                base.fajrAngle = 16.0
                base.ishaAngle = 14.0
            }
            else -> Unit
        }

        base.madhab = when (madhab) {
            PrayerMadhab.HANAFI -> Madhab.HANAFI
            else -> Madhab.SHAFI
        }
        base.adjustments = parseTune(method.aladhanTune)
        return base
    }

    /** Aladhan tune: imsak,fajr,sunrise,dhuhr,asr,maghrib,sunset,isha,midnight */
    private fun parseTune(tune: String): PrayerAdjustments {
        val parts = tune.split(',').map { it.trim().toIntOrNull() ?: 0 }
        fun at(index: Int) = parts.getOrElse(index) { 0 }
        return PrayerAdjustments(
            at(1),
            at(2),
            at(3),
            at(4),
            at(5),
            at(7)
        )
    }

    private fun formatPrayerTime(date: Date): String = synchronized(timeOut) {
        timeOut.timeZone = TimeZone.getDefault()
        timeOut.format(date)
    }

    private fun formatHm(date: Date): String = synchronized(timeOut) {
        timeOut.timeZone = TimeZone.getDefault()
        timeOut.format(date)
    }

    private fun startOfDay(day: Date): Date = Calendar.getInstance().apply {
        time = day
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun parseTime(raw: String, dayBase: Date): Date? = try {
        val parsed = synchronized(timeOut) {
            timeOut.timeZone = TimeZone.getDefault()
            timeOut.parse(raw)
        } ?: return null
        val cal = Calendar.getInstance().apply { time = dayBase }
        val parsedCal = Calendar.getInstance().apply { time = parsed }
        cal.set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
        cal.set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.time
    } catch (_: Throwable) {
        null
    }
}
