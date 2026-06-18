package app.kamy.saatApp.domain.share

import java.util.Calendar
import java.util.Date

enum class ShareDayPeriod {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT;

    companion object {
        fun forDate(date: Date = Date(), calendar: Calendar = Calendar.getInstance()): ShareDayPeriod {
            return when (calendar.apply { time = date }.get(Calendar.HOUR_OF_DAY)) {
                in 5..11 -> MORNING
                in 12..15 -> AFTERNOON
                in 16..19 -> EVENING
                else -> NIGHT
            }
        }
    }
}
