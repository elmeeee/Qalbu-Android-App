package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.model.OptionalWorshipHabit

class PrayerTrackerPreferencesStore private constructor(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isOptionalHabitEnabled(habit: OptionalWorshipHabit): Boolean =
        prefs.getBoolean("${KEY_OPTIONAL_PREFIX}${habit.prefKey}", true)

    fun setOptionalHabitEnabled(habit: OptionalWorshipHabit, enabled: Boolean) {
        prefs.edit().putBoolean("${KEY_OPTIONAL_PREFIX}${habit.prefKey}", enabled).apply()
    }

    companion object {
        private const val PREFS = "saat_prayer_tracker_prefs"
        private const val KEY_OPTIONAL_PREFIX = "optional_"

        fun from(context: Context): PrayerTrackerPreferencesStore =
            PrayerTrackerPreferencesStore(context.applicationContext)
    }
}
