package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.domain.model.OptionalWorshipHabit

class PrayerTrackerPreferencesStore private constructor(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun checkRemindersEnabled(): Boolean = prefs.getBoolean(KEY_CHECK_REMINDERS, true)

    fun setCheckRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CHECK_REMINDERS, enabled).apply()
    }

    fun isOptionalHabitEnabled(habit: OptionalWorshipHabit): Boolean =
        prefs.getBoolean("${KEY_OPTIONAL_PREFIX}${habit.prefKey}", true)

    fun setOptionalHabitEnabled(habit: OptionalWorshipHabit, enabled: Boolean) {
        prefs.edit().putBoolean("${KEY_OPTIONAL_PREFIX}${habit.prefKey}", enabled).apply()
    }

    companion object {
        private const val PREFS = "qalbu_prayer_tracker_prefs"
        private const val KEY_CHECK_REMINDERS = "check_reminders"
        private const val KEY_OPTIONAL_PREFIX = "optional_"

        fun from(context: Context): PrayerTrackerPreferencesStore =
            PrayerTrackerPreferencesStore(context.applicationContext)
    }
}
