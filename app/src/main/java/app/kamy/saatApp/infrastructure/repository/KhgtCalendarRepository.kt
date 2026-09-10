package app.kamy.saatApp.infrastructure.repository

import app.kamy.saatApp.domain.model.KhgtMonth
import app.kamy.saatApp.domain.model.KhgtTodayInfo
import app.kamy.saatApp.domain.model.RamadanDayInfo
import app.kamy.saatApp.infrastructure.local.LocalKhgtCalendar
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KhgtCalendarRepository @Inject constructor(
    private val local: LocalKhgtCalendar
) {
    suspend fun todayInfo(): KhgtTodayInfo? = local.todayInfo()

    suspend fun ramadanInfo(
        date: Calendar = Calendar.getInstance(),
        imsakTime: String? = null,
        iftarTime: String? = null
    ): RamadanDayInfo? = local.ramadanInfo(date, imsakTime, iftarTime)

    suspend fun infoForDate(date: Calendar): KhgtTodayInfo? = local.infoForDate(date)

    suspend fun upcomingEvents(limit: Int = 5): List<Pair<String, String>> =
        local.upcomingEvents(limit)

    suspend fun monthForToday(): KhgtMonth? = local.monthForToday()
}

