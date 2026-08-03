package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isComplete(): Boolean = prefs.getBoolean(KEY_COMPLETE, false)

    fun markComplete() {
        prefs.edit().putBoolean(KEY_COMPLETE, true).apply()
    }

    fun markNotificationsHandled() {
        prefs.edit().putBoolean(KEY_PERMISSIONS_HANDLED, true).apply()
    }

    fun permissionsHandledInOnboarding(): Boolean =
        prefs.getBoolean(KEY_PERMISSIONS_HANDLED, false)

    fun hasShownHomeCoachMark(): Boolean = prefs.getBoolean(KEY_HOME_COACH_MARK, false)

    fun markHomeCoachMarkShown() {
        prefs.edit().putBoolean(KEY_HOME_COACH_MARK, true).apply()
    }

    fun isFirstLaunchDefaultApplied(): Boolean =
        prefs.getBoolean(KEY_FIRST_LAUNCH_DEFAULTS, false)

    fun markFirstLaunchDefaultApplied() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH_DEFAULTS, true).apply()
    }

    companion object {
        fun from(context: Context): OnboardingStore = OnboardingStore(context.applicationContext)

        private const val PREFS = "saat_onboarding"
        private const val KEY_COMPLETE = "complete"
        private const val KEY_PERMISSIONS_HANDLED = "permissions_handled"
        private const val KEY_HOME_COACH_MARK = "home_coach_mark_shown"
        private const val KEY_FIRST_LAUNCH_DEFAULTS = "first_launch_defaults_applied"
    }
}
