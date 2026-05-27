package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import android.content.SharedPreferences
import app.kamy.qalbuApp.domain.model.PrayerType
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
        enabledAdzanPrayers = PrayerType.ADZAN_NOTIFICATION_PRAYERS
            .filter { isPrayerEnabled(it) }
            .map { it.aladhanKey }
            .toSet(),
        imsakEnabled = bool(KEY_IMSAK, default = true),
        midnightEnabled = bool(KEY_MIDNIGHT, default = true),
        firstThirdEnabled = bool(KEY_FIRST_THIRD, default = true),
        lastThirdEnabled = bool(KEY_TAHAJUD, default = true),
        yasinReminderEnabled = bool(KEY_YASIN, default = true),
        kahfReminderEnabled = bool(KEY_KAHF, default = true)
    )

    fun isPrayerEnabled(type: PrayerType): Boolean =
        bool(prayerKey(type), default = legacyAdzanDefault())

    fun isImsakEnabled(): Boolean = bool(KEY_IMSAK, default = true)
    fun isMidnightEnabled(): Boolean = bool(KEY_MIDNIGHT, default = true)
    fun isFirstThirdEnabled(): Boolean = bool(KEY_FIRST_THIRD, default = true)
    fun isTahajudEnabled(): Boolean = bool(KEY_TAHAJUD, default = true)
    fun isYasinReminderEnabled(): Boolean = bool(KEY_YASIN, default = true)
    fun isKahfReminderEnabled(): Boolean = bool(KEY_KAHF, default = true)

    fun setPrayerEnabled(type: PrayerType, enabled: Boolean) = setBool(prayerKey(type), enabled)

    fun setImsakEnabled(enabled: Boolean) = setBool(KEY_IMSAK, enabled)
    fun setMidnightEnabled(enabled: Boolean) = setBool(KEY_MIDNIGHT, enabled)
    fun setFirstThirdEnabled(enabled: Boolean) = setBool(KEY_FIRST_THIRD, enabled)
    fun setTahajudEnabled(enabled: Boolean) = setBool(KEY_TAHAJUD, enabled)
    fun setYasinReminderEnabled(enabled: Boolean) = setBool(KEY_YASIN, enabled)
    fun setKahfReminderEnabled(enabled: Boolean) = setBool(KEY_KAHF, enabled)

    /** @deprecated Use [isPrayerEnabled] per prayer; kept for migration reads. */
    fun isAdzanEnabled(): Boolean =
        PrayerType.ADZAN_NOTIFICATION_PRAYERS.any { isPrayerEnabled(it) }

    private fun prayerKey(type: PrayerType): String = when (type) {
        PrayerType.FAJR -> KEY_FAJR
        PrayerType.DHUHR -> KEY_DHUHR
        PrayerType.ASR -> KEY_ASR
        PrayerType.MAGHRIB -> KEY_MAGHRIB
        PrayerType.ISHA -> KEY_ISHA
        else -> error("Not an adzan prayer: $type")
    }

    /** Before per-prayer keys existed, one master toggle controlled all five. */
    private fun legacyAdzanDefault(): Boolean = bool(KEY_ADZAN, default = true)

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
        private const val KEY_FAJR = "fajrNotificationsEnabled"
        private const val KEY_DHUHR = "dhuhrNotificationsEnabled"
        private const val KEY_ASR = "asrNotificationsEnabled"
        private const val KEY_MAGHRIB = "maghribNotificationsEnabled"
        private const val KEY_ISHA = "ishaNotificationsEnabled"
        private const val KEY_IMSAK = "imsakNotificationsEnabled"
        private const val KEY_MIDNIGHT = "midnightNotificationsEnabled"
        private const val KEY_FIRST_THIRD = "firstThirdNotificationsEnabled"
        private const val KEY_TAHAJUD = "tahajudNotificationsEnabled"
        private const val KEY_YASIN = "yasinReminderEnabled"
        private const val KEY_KAHF = "kahfReminderEnabled"
    }
}
