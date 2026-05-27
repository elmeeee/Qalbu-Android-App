package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.infrastructure.network.api.AlAdhanDate
import app.kamy.qalbuApp.infrastructure.network.api.AlAdhanGregorian
import app.kamy.qalbuApp.infrastructure.network.api.AlAdhanHijri
import java.text.Normalizer
import java.util.Locale

/**
 * Formats Al-Adhan `date` payload like iOS: "20 May 2026" / "10 Dhu al-Hijj 1447".
 */
internal object AlAdhanDateLabels {

    fun fromApiDate(date: AlAdhanDate?): Pair<String?, String?> {
        if (date == null) return null to null
        return formatHijri(date.hijri) to formatGregorian(date.gregorian, date.readable)
    }

    private fun formatGregorian(gregorian: AlAdhanGregorian?, readable: String?): String? {
        readable?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        return formatFromParts(
            day = gregorian?.day,
            month = gregorian?.month?.en,
            year = gregorian?.year,
            rawDate = gregorian?.date,
            monthFromRaw = ::gregorianMonthName
        )
    }

    private fun formatHijri(hijri: AlAdhanHijri?): String? =
        formatFromParts(
            day = hijri?.day,
            month = hijri?.month?.en?.let(::formatHijriMonthName),
            year = hijri?.year,
            rawDate = hijri?.date,
            monthFromRaw = ::hijriMonthName
        )

    private fun formatFromParts(
        day: String?,
        month: String?,
        year: String?,
        rawDate: String?,
        monthFromRaw: (Int?) -> String?
    ): String? {
        val d = day?.trim().orEmpty()
        val m = month?.trim().orEmpty()
        val y = year?.trim().orEmpty()
        if (d.isNotEmpty() && m.isNotEmpty() && y.isNotEmpty()) {
            return "$d $m $y"
        }
        return rawDate?.let { formatFromDdMmYyyy(it, monthFromRaw) }
    }

    /** API fallback: `10-12-1447` when nested month object is missing. */
    private fun formatFromDdMmYyyy(raw: String, monthFromRaw: (Int?) -> String?): String? {
        val parts = raw.trim().split("-")
        if (parts.size != 3) return raw.replace("-", " ")
        val (d, monthNum, y) = parts
        val monthName = monthFromRaw(monthNum.toIntOrNull())
        return if (monthName != null) "$d $monthName $y" else raw.replace("-", " ")
    }

    private fun formatHijriMonthName(en: String): String {
        val plain = stripDiacritics(en)
            .replace("'", "'")
            .replace("ʾ", "")
            .trim()
        val titled = plain.split(Regex("\\s+")).joinToString(" ") { word ->
            when {
                word.equals("al", ignoreCase = true) -> "al"
                word.equals("adh", ignoreCase = true) -> "ad"
                else -> word.lowercase(Locale.ENGLISH).replaceFirstChar { it.titlecase(Locale.ENGLISH) }
            }
        }
        return titled.replace("Hijjah", "Hajj", ignoreCase = true)
    }

    private fun stripDiacritics(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    private fun hijriMonthName(number: Int?): String? = when (number) {
        1 -> "Muharram"
        2 -> "Safar"
        3 -> "Rabi al-Awwal"
        4 -> "Rabi al-Thani"
        5 -> "Jumada al-Awwal"
        6 -> "Jumada al-Thani"
        7 -> "Rajab"
        8 -> "Sha'ban"
        9 -> "Ramadan"
        10 -> "Shawwal"
        11 -> "Dhu al-Qi'dah"
        12 -> "Dhu al-Hajj"
        else -> null
    }

    private fun gregorianMonthName(number: Int?): String? = when (number) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> null
    }
}
