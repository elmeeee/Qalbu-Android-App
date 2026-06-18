package app.kamy.qalbuApp.infrastructure.repository

import app.kamy.qalbuApp.domain.model.KhgtMonth
import app.kamy.qalbuApp.domain.model.KhgtTodayInfo
import app.kamy.qalbuApp.infrastructure.local.LocalKhgtCalendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KhgtCalendarRepository @Inject constructor(
    private val local: LocalKhgtCalendar
) {
    suspend fun todayInfo(): KhgtTodayInfo? = local.todayInfo()

    suspend fun upcomingEvents(limit: Int = 5): List<Pair<String, String>> =
        local.upcomingEvents(limit)

    suspend fun monthForToday(): KhgtMonth? = local.monthForToday()
}
