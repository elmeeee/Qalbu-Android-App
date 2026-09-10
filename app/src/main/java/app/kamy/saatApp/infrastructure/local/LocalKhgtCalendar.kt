package app.kamy.saatApp.infrastructure.local

import android.content.Context
import app.kamy.saatApp.domain.model.KhgtCalendarResponse
import app.kamy.saatApp.domain.model.KhgtDay
import app.kamy.saatApp.domain.model.KhgtMonth
import app.kamy.saatApp.domain.model.KhgtSpecialDay
import app.kamy.saatApp.domain.model.KhgtTodayInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

import app.kamy.saatApp.domain.model.RamadanDayInfo

@Singleton
class LocalKhgtCalendar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    @Volatile
    private var cachedYears: MutableMap<Int, KhgtCalendarResponse> = mutableMapOf()

    suspend fun todayInfo(): KhgtTodayInfo? = infoForDate(Calendar.getInstance())

    suspend fun ramadanInfo(
        date: Calendar = Calendar.getInstance(),
        imsakTime: String? = null,
        iftarTime: String? = null
    ): RamadanDayInfo? = withContext(Dispatchers.IO) {
        val gregorian = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(date.time)
        val hijriYears = listOf(
            currentHijriYearGuess(date) - 1,
            currentHijriYearGuess(date),
            currentHijriYearGuess(date) + 1
        )
        for (year in hijriYears) {
            val calendar = loadYear(year) ?: continue
            val months = calendar.data.orEmpty()
            for (month in months) {
                if (month.name.equals("Ramadan", ignoreCase = true)) {
                    val days = month.days.orEmpty()
                    val dayIndex = days.indexOfFirst { it.masehi == gregorian }
                    if (dayIndex != -1) {
                        val day = days[dayIndex]
                        val dayNumber = parseArabicHijriDay(day.hijri) ?: (dayIndex + 1)
                        val totalDays = days.size.coerceAtLeast(29)
                        val hijriYear = month.year ?: year
                        return@withContext RamadanDayInfo(
                            isRamadan = true,
                            dayNumber = dayNumber,
                            totalDays = totalDays,
                            hijriYear = hijriYear,
                            hijriLabel = "$dayNumber Ramadan $hijriYear H",
                            imsakTime = imsakTime,
                            iftarTime = iftarTime
                        )
                    }
                }
            }
        }
        null
    }

    suspend fun infoForDate(date: Calendar): KhgtTodayInfo? = withContext(Dispatchers.IO) {
        val gregorian = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(date.time)
        val gregorianShort = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date.time)

        for (year in listOf(currentHijriYearGuess(date) - 1, currentHijriYearGuess(date), currentHijriYearGuess(date) + 1)) {
            val calendar = loadYear(year) ?: continue
            val day = findDayByGregorian(calendar.data.orEmpty(), gregorian) ?: continue
            val hijriLabel = buildString {
                append(day.hijri.orEmpty().trim())
                calendar.data?.firstOrNull { month ->
                    month.days?.any { it.masehi == gregorian } == true
                }?.name?.let { monthName ->
                    append(' ')
                    append(monthName)
                }
                append(' ')
                append(year)
            }.trim()
            val event = day.tooltip?.takeIf { it.isNotBlank() && day.isEvent == true }
            return@withContext KhgtTodayInfo(
                hijriLabel = hijriLabel,
                gregorianLabel = gregorianShort,
                pasaran = day.pasaran,
                eventTitle = event,
                isImportantDay = event != null
            )
        }
        null
    }

    suspend fun upcomingEvents(limit: Int = 5): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val today = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val events = mutableListOf<Pair<Long, Pair<String, String>>>()

        for (yearOffset in -1..1) {
            val year = currentHijriYearGuess(today) + yearOffset
            val calendar = loadYear(year) ?: continue
            calendar.data.orEmpty().forEach { month ->
                month.days.orEmpty().forEach { day ->
                    val title = day.tooltip?.takeIf { it.isNotBlank() && day.isEvent == true } ?: return@forEach
                    val masehi = day.masehi ?: return@forEach
                    val date = runCatching { formatter.parse(masehi)?.time }.getOrNull() ?: return@forEach
                    if (date >= today.timeInMillis - 86_400_000L) {
                        events += date to (day.masehiShort.orEmpty() to title)
                    }
                }
            }
            calendar.specialDays.orEmpty().forEach { special ->
                val title = special.keterangan?.takeIf { it.isNotBlank() } ?: return@forEach
                val masehi = special.tanggalMasehi ?: return@forEach
                val date = runCatching { formatter.parse(masehi)?.time }.getOrNull() ?: return@forEach
                if (date >= today.timeInMillis - 86_400_000L) {
                    events += date to (masehi to title)
                }
            }
        }

        events.sortedBy { it.first }
            .map { it.second }
            .distinct()
            .take(limit)
    }

    suspend fun monthForToday(): KhgtMonth? = withContext(Dispatchers.IO) {
        val today = Calendar.getInstance()
        val gregorian = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(today.time)
        for (year in listOf(currentHijriYearGuess(today) - 1, currentHijriYearGuess(today), currentHijriYearGuess(today) + 1)) {
            val calendar = loadYear(year) ?: continue
            calendar.data.orEmpty().forEach { month ->
                if (month.days?.any { it.masehi == gregorian } == true) return@withContext month
            }
        }
        null
    }

    private fun parseArabicHijriDay(arabic: String?): Int? {
        if (arabic.isNullOrBlank()) return null
        val western = arabic.map { ch ->
            when (ch) {
                '٠' -> '0'
                '١' -> '1'
                '٢' -> '2'
                '٣' -> '3'
                '٤' -> '4'
                '٥' -> '5'
                '٦' -> '6'
                '٧' -> '7'
                '٨' -> '8'
                '٩' -> '9'
                else -> ch
            }
        }.joinToString("")
        return western.filter { it.isDigit() }.toIntOrNull()
    }

    private fun loadYear(hijriYear: Int): KhgtCalendarResponse? {
        cachedYears[hijriYear]?.let { return it }
        val asset = "hijri/khgt_$hijriYear.json.gz"
        val plain = "hijri/khgt_$hijriYear.json"
        val text = when {
            assetExists(asset) -> readGzipAsset(asset)
            assetExists(plain) -> readAsset(plain)
            else -> return null
        }
        val parsed = json.decodeFromString(KhgtCalendarResponse.serializer(), text)
        cachedYears[hijriYear] = parsed
        return parsed
    }

    private fun findDayByGregorian(months: List<KhgtMonth>, gregorian: String): KhgtDay? =
        months.asSequence()
            .flatMap { it.days.orEmpty().asSequence() }
            .firstOrNull { it.masehi == gregorian }

    private fun currentHijriYearGuess(today: Calendar): Int {
        val localDate = today.toInstant()
            .atZone(today.timeZone.toZoneId())
            .toLocalDate()
        return java.time.chrono.HijrahDate.from(localDate)
            .get(java.time.temporal.ChronoField.YEAR_OF_ERA)
    }

    private fun assetExists(path: String): Boolean =
        runCatching { context.assets.open(path).close(); true }.getOrDefault(false)

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

    private fun readGzipAsset(path: String): String =
        context.assets.open(path).use { input ->
            GZIPInputStream(input).bufferedReader().use { it.readText() }
        }
}

