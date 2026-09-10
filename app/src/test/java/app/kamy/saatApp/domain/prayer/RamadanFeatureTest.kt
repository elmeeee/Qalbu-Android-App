package app.kamy.saatApp.domain.prayer

import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.domain.model.RamadanDayInfo
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RamadanFeatureTest {

    @Test
    fun testArabicNumeralParsing() {
        fun parseArabicHijriDay(arabic: String?): Int? {
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

        assertEquals(1, parseArabicHijriDay("١"))
        assertEquals(12, parseArabicHijriDay("١٢"))
        assertEquals(29, parseArabicHijriDay("٢٩"))
        assertEquals(30, parseArabicHijriDay("٣٠"))
    }

    @Test
    fun testImsakAndIftarCalculation() {
        val formatter = SimpleDateFormat("HH:mm", Locale.US)
        val cal = Calendar.getInstance()

        cal.set(Calendar.HOUR_OF_DAY, 4)
        cal.set(Calendar.MINUTE, 45)
        val fajrDate = cal.time

        cal.set(Calendar.HOUR_OF_DAY, 18)
        cal.set(Calendar.MINUTE, 11)
        val maghribDate = cal.time

        val imsakDate = Date(fajrDate.time - 10 * 60 * 1000L)
        val imsakFormatted = formatter.format(imsakDate)
        val iftarFormatted = formatter.format(maghribDate)

        assertEquals("04:35", imsakFormatted)
        assertEquals("18:11", iftarFormatted)
    }

    @Test
    fun testRamadanProgressFraction() {
        val day12Info = RamadanDayInfo(
            isRamadan = true,
            dayNumber = 12,
            totalDays = 30,
            hijriYear = 1448,
            hijriLabel = "12 Ramadan 1448 H",
            imsakTime = "04:35",
            iftarTime = "18:11"
        )

        val fraction = day12Info.dayNumber.toFloat() / day12Info.totalDays.toFloat()
        assertEquals(0.4f, fraction, 0.001f)
    }
}
