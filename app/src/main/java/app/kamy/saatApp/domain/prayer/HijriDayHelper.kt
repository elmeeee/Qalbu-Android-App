package app.kamy.saatApp.domain.prayer

import android.content.Context
import app.kamy.saatApp.infrastructure.cache.PrayerDayCache
import java.util.Calendar

object HijriDayHelper {

    fun todayHijriDay(context: Context): Int? = PrayerDayCache.load(context)?.hijriDay

    fun isAyyamulBidh(hijriDay: Int?): Boolean = hijriDay in 13..15

    fun isMondayThursdayFastDay(calendar: Calendar = Calendar.getInstance()): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.THURSDAY
    }

    fun parseHijriDayFromLabel(label: String?): Int? {
        if (label.isNullOrBlank()) return null
        return label.trim()
            .substringBefore(' ')
            .trim()
            .toIntOrNull()
            ?.takeIf { it in 1..30 }
    }
}
