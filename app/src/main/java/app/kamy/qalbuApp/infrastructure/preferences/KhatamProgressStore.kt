package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.core.config.MushafConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class KhatamUiSnapshot(
    val dailyPageGoal: Int,
    val pagesReadToday: Int,
    val totalPagesRead: Int,
    val streakDays: Int,
    val todayProgressFraction: Float,
    val overallProgressFraction: Float,
    val isTodayGoalMet: Boolean
)

object KhatamProgressStore {
    private const val PREFS = "qalbu_khatam"
    private const val KEY_DAILY_GOAL = "daily_goal"
    private const val KEY_PAGES_TODAY = "pages_today"
    private const val KEY_PAGES_TODAY_KEY = "pages_today_key"
    private const val KEY_TOTAL_PAGES = "total_pages_read"
    private const val KEY_STREAK = "streak"
    private const val KEY_LAST_GOAL_DAY = "last_goal_day"
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val totalMushafPages = MushafConfig.TOTAL_PAGES

    fun todayKey(): String = dayKeyFormat.format(Date())

    fun dailyPageGoal(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_DAILY_GOAL, 4)
            .coerceIn(1, 20)

    fun setDailyPageGoal(context: Context, pages: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DAILY_GOAL, pages.coerceIn(1, 20))
            .apply()
    }

    fun recordPageRead(context: Context, page: Int) {
        if (page !in 1..totalMushafPages) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = todayKey()
        val storedDay = prefs.getString(KEY_PAGES_TODAY_KEY, null)
        val pagesToday = if (storedDay == today) prefs.getInt(KEY_PAGES_TODAY, 0) else 0
        val nextToday = (pagesToday + 1).coerceAtMost(totalMushafPages)
        val total = prefs.getInt(KEY_TOTAL_PAGES, 0) + 1
        prefs.edit()
            .putString(KEY_PAGES_TODAY_KEY, today)
            .putInt(KEY_PAGES_TODAY, nextToday)
            .putInt(KEY_TOTAL_PAGES, total)
            .apply()
        updateStreakIfNeeded(context, today, nextToday)
    }

    private fun updateStreakIfNeeded(context: Context, today: String, pagesToday: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = dailyPageGoal(context)
        if (pagesToday < goal) return
        val lastGoalDay = prefs.getString(KEY_LAST_GOAL_DAY, null)
        if (lastGoalDay == today) return
        val streak = when (lastGoalDay) {
            null -> 1
            previousDay(today) -> prefs.getInt(KEY_STREAK, 0) + 1
            else -> 1
        }
        prefs.edit()
            .putString(KEY_LAST_GOAL_DAY, today)
            .putInt(KEY_STREAK, streak)
            .apply()
    }

    private fun previousDay(today: String): String? = runCatching {
        val cal = java.util.Calendar.getInstance()
        cal.time = dayKeyFormat.parse(today) ?: return null
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        dayKeyFormat.format(cal.time)
    }.getOrNull()

    fun snapshot(context: Context): KhatamUiSnapshot {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = todayKey()
        val goal = dailyPageGoal(context)
        val pagesToday = if (prefs.getString(KEY_PAGES_TODAY_KEY, null) == today) {
            prefs.getInt(KEY_PAGES_TODAY, 0)
        } else {
            0
        }
        val total = prefs.getInt(KEY_TOTAL_PAGES, 0)
        val streak = prefs.getInt(KEY_STREAK, 0)
        return KhatamUiSnapshot(
            dailyPageGoal = goal,
            pagesReadToday = pagesToday,
            totalPagesRead = total,
            streakDays = streak,
            todayProgressFraction = (pagesToday.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f),
            overallProgressFraction = MushafReadingStore.progressFraction(context),
            isTodayGoalMet = pagesToday >= goal
        )
    }

    fun resetCycle(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_TOTAL_PAGES)
            .remove(KEY_STREAK)
            .remove(KEY_LAST_GOAL_DAY)
            .remove(KEY_PAGES_TODAY)
            .remove(KEY_PAGES_TODAY_KEY)
            .apply()
    }
}
