package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.infrastructure.cache.PrayerDayCache

object RamadanPreferencesStore {
    private const val PREFS = "qalbu_ramadan"
    private const val KEY_MODE_ENABLED = "mode_enabled"
    private const val KEY_TARAWIH_GOAL = "tarawih_goal"
    private const val KEY_TARAWIH_DONE_PREFIX = "tarawih_"

    fun isModeEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_MODE_ENABLED, isRamadanSeason(context))

    fun setModeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MODE_ENABLED, enabled)
            .apply()
    }

    fun isRamadanSeason(context: Context): Boolean {
        val label = PrayerDayCache.load(context)?.hijriLabel ?: return false
        return label.contains("ramadan", ignoreCase = true) ||
            label.contains("ramadhan", ignoreCase = true)
    }

    fun tarawihGoal(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_TARAWIH_GOAL, 8)
            .coerceIn(4, 20)

    fun setTarawihGoal(context: Context, rakah: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TARAWIH_GOAL, rakah.coerceIn(4, 20))
            .apply()
    }

    fun isTarawihDone(context: Context, dayKey: String = PrayerTrackerStore.todayKey()): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_TARAWIH_DONE_PREFIX + dayKey, false)

    fun setTarawihDone(context: Context, done: Boolean, dayKey: String = PrayerTrackerStore.todayKey()) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_TARAWIH_DONE_PREFIX + dayKey, done)
            .apply()
    }

    fun toggleTarawihDone(context: Context): Boolean {
        val day = PrayerTrackerStore.todayKey()
        val next = !isTarawihDone(context, day)
        setTarawihDone(context, next, day)
        return next
    }
}
