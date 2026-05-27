package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import android.content.SharedPreferences
import app.kamy.qalbuApp.infrastructure.notifications.PrayerNotificationScheduleOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Mirrors iOS PrayerNotificationPreferences. */
@Singleton
class PrayerNotificationPreferencesStore @Inject constructor(
    @ApplicationContext context: Context
) : PrayerNotificationPreferencesStoreBase(
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
) {
    companion object {
        fun from(context: Context): PrayerNotificationPreferencesStoreBase =
            PrayerNotificationPreferencesStoreBase(
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            )
    }
}

open class PrayerNotificationPreferencesStoreBase(
    private val prefs: SharedPreferences
) {
    private val _changed = MutableStateFlow(0)
    val changeTick: StateFlow<Int> = _changed.asStateFlow()

    fun scheduleOptions(): PrayerNotificationScheduleOptions = PrayerNotificationScheduleOptions(
        adzanEnabled = bool(KEY_ADZAN, default = true),
        imsakEnabled = bool(KEY_IMSAK, default = true),
        midnightEnabled = bool(KEY_MIDNIGHT, default = true),
        firstThirdEnabled = bool(KEY_FIRST_THIRD, default = true),
        lastThirdEnabled = bool(KEY_TAHAJUD, default = true),
        yasinReminderEnabled = bool(KEY_YASIN, default = true),
        kahfReminderEnabled = bool(KEY_KAHF, default = true)
    )

    fun isAdzanEnabled(): Boolean = bool(KEY_ADZAN, default = true)
    fun isImsakEnabled(): Boolean = bool(KEY_IMSAK, default = true)
    fun isMidnightEnabled(): Boolean = bool(KEY_MIDNIGHT, default = true)
    fun isFirstThirdEnabled(): Boolean = bool(KEY_FIRST_THIRD, default = true)
    fun isTahajudEnabled(): Boolean = bool(KEY_TAHAJUD, default = true)
    fun isYasinReminderEnabled(): Boolean = bool(KEY_YASIN, default = true)
    fun isKahfReminderEnabled(): Boolean = bool(KEY_KAHF, default = true)

    fun setAdzanEnabled(enabled: Boolean) = setBool(KEY_ADZAN, enabled)
    fun setImsakEnabled(enabled: Boolean) = setBool(KEY_IMSAK, enabled)
    fun setMidnightEnabled(enabled: Boolean) = setBool(KEY_MIDNIGHT, enabled)
    fun setFirstThirdEnabled(enabled: Boolean) = setBool(KEY_FIRST_THIRD, enabled)
    fun setTahajudEnabled(enabled: Boolean) = setBool(KEY_TAHAJUD, enabled)
    fun setYasinReminderEnabled(enabled: Boolean) = setBool(KEY_YASIN, enabled)
    fun setKahfReminderEnabled(enabled: Boolean) = setBool(KEY_KAHF, enabled)

    private fun bool(key: String, default: Boolean): Boolean {
        if (!prefs.contains(key)) return default
        return prefs.getBoolean(key, default)
    }

    private fun setBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
        _changed.value = _changed.value + 1
    }

    companion object {
        const val PREFS_NAME = "qalbu_notification_prefs"
        private const val KEY_ADZAN = "adzanNotificationsEnabled"
        private const val KEY_IMSAK = "imsakNotificationsEnabled"
        private const val KEY_MIDNIGHT = "midnightNotificationsEnabled"
        private const val KEY_FIRST_THIRD = "firstThirdNotificationsEnabled"
        private const val KEY_TAHAJUD = "tahajudNotificationsEnabled"
        private const val KEY_YASIN = "yasinReminderEnabled"
        private const val KEY_KAHF = "kahfReminderEnabled"
    }
}
