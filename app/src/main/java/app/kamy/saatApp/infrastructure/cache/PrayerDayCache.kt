package app.kamy.saatApp.infrastructure.cache

import android.content.Context
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.repository.PrayerDayResult
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PrayerDayCache {
    private const val FILE_NAME = "prayer_day.json"
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun save(context: Context, result: PrayerDayResult) {
        val file = cacheFile(context)
        val entry = CachedPrayerDay(
            dayKey = dayKeyFormat.format(Date()),
            cityName = result.cityName,
            hijriLabel = result.hijriLabel,
            gregorianLabel = result.gregorianLabel,
            hijriDay = result.hijriDay,
            entries = result.timings.map {
                CachedPrayerEntry(
                    typeKey = it.type.aladhanKey,
                    rawTime = it.rawTime,
                    fireAtMillis = it.date.time
                )
            },
            savedAt = System.currentTimeMillis()
        )
        runCatching { file.writeText(json.encodeToString(entry)) }
    }

    fun lastSavedAt(context: Context): Long? {
        val file = cacheFile(context)
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString<CachedPrayerDay>(file.readText()).savedAt
        }.getOrNull()
    }

    fun load(context: Context): PrayerDayResult? {
        val file = cacheFile(context)
        if (!file.exists()) return null
        val entry = runCatching {
            json.decodeFromString<CachedPrayerDay>(file.readText())
        }.getOrNull() ?: return null
        val today = Date()
        val timings = entry.entries.mapNotNull { cached ->
            val type = PrayerType.entries.firstOrNull { it.aladhanKey == cached.typeKey } ?: return@mapNotNull null
            val date = parsePrayerTime(cached.rawTime, today) ?: Date(cached.fireAtMillis)
            PrayerEntry(
                type = type,
                rawTime = cached.rawTime,
                date = date
            )
        }.sortedBy { it.date }
        if (timings.isEmpty()) return null
        return PrayerDayResult(
            timings = timings,
            cityName = entry.cityName,
            hijriLabel = entry.hijriLabel,
            gregorianLabel = entry.gregorianLabel,
            hijriDay = entry.hijriDay,
            scheduleBundle = null
        )
    }

    private fun cacheFile(context: Context): File =
        File(context.filesDir, FILE_NAME)

    private fun parsePrayerTime(raw: String, scheduleDay: Date): Date? {
        val clean = raw.substringBefore(" ").trim()
        val parts = clean.split(":")
        if (parts.size < 2) return null
        return runCatching {
            Calendar.getInstance().apply {
                time = scheduleDay
                set(Calendar.HOUR_OF_DAY, parts[0].trim().toInt())
                set(Calendar.MINUTE, parts[1].trim().toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }.getOrNull()
    }

    @Serializable
    private data class CachedPrayerDay(
        val dayKey: String,
        val cityName: String?,
        val hijriLabel: String?,
        val gregorianLabel: String?,
        val hijriDay: Int? = null,
        val entries: List<CachedPrayerEntry>,
        val savedAt: Long
    )

    @Serializable
    private data class CachedPrayerEntry(
        val typeKey: String,
        val rawTime: String,
        val fireAtMillis: Long
    )
}
