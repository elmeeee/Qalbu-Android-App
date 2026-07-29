package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import android.content.SharedPreferences
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.notifications.PrayerNotificationScheduleOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

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
        lastThirdEnabled = isTahajudEnabled(),
        yasinReminderEnabled = bool(KEY_YASIN, default = true),
        kahfReminderEnabled = bool(KEY_KAHF, default = true),
        importantDaysReminderEnabled = bool(KEY_IMPORTANT_DAYS, default = true),
        adhanSoundEnabled = bool(KEY_ADZAN_SOUND, default = true),
        monThuFastReminderEnabled = bool(KEY_MON_THU_FAST, default = true),
        dhuhaReminderEnabled = bool(KEY_DHUHA, default = true),
        dhuhaHour = dhuhaHour(),
        dhuhaMinute = dhuhaMinute(),
        tahajudHour = tahajudHour(),
        tahajudMinute = tahajudMinute()
    )

    fun isAdhanSoundEnabled(): Boolean = bool(KEY_ADZAN_SOUND, default = true)
    fun setAdhanSoundEnabled(enabled: Boolean) = setBool(KEY_ADZAN_SOUND, enabled)

    fun isMonThuFastEnabled(): Boolean = bool(KEY_MON_THU_FAST, default = true)
    fun setMonThuFastEnabled(enabled: Boolean) = setBool(KEY_MON_THU_FAST, enabled)

    fun isDhuhaEnabled(): Boolean = bool(KEY_DHUHA, default = true)
    fun setDhuhaEnabled(enabled: Boolean) = setBool(KEY_DHUHA, enabled)

    fun dhuhaHour(): Int = prefs.getInt(KEY_DHUHA_HOUR, 8)
    fun dhuhaMinute(): Int = prefs.getInt(KEY_DHUHA_MINUTE, 30)
    fun setDhuhaTime(hour: Int, minute: Int) {
        prefs.edit().putInt(KEY_DHUHA_HOUR, hour).putInt(KEY_DHUHA_MINUTE, minute).apply()
        _changed.value = _changed.value + 1
    }


    fun isPrayerEnabled(type: PrayerType): Boolean =
        bool(prayerKey(type), default = legacyAdzanDefault())

    fun isImsakEnabled(): Boolean = bool(KEY_IMSAK, default = true)
    fun isMidnightEnabled(): Boolean = bool(KEY_MIDNIGHT, default = true)
    fun isFirstThirdEnabled(): Boolean = bool(KEY_FIRST_THIRD, default = true)
    fun isTahajudEnabled(): Boolean = bool(KEY_TAHAJUD, default = false)
    fun tahajudHour(): Int = prefs.getInt(KEY_TAHAJUD_HOUR, 3)
    fun tahajudMinute(): Int = prefs.getInt(KEY_TAHAJUD_MINUTE, 30)
    fun setTahajudTime(hour: Int, minute: Int) {
        prefs.edit().putInt(KEY_TAHAJUD_HOUR, hour).putInt(KEY_TAHAJUD_MINUTE, minute).apply()
        _changed.value = _changed.value + 1
    }
    fun isYasinReminderEnabled(): Boolean = bool(KEY_YASIN, default = true)
    fun isKahfReminderEnabled(): Boolean = bool(KEY_KAHF, default = true)
    fun isImportantDaysReminderEnabled(): Boolean = bool(KEY_IMPORTANT_DAYS, default = true)

    fun setPrayerEnabled(type: PrayerType, enabled: Boolean) = setBool(prayerKey(type), enabled)

    fun setImsakEnabled(enabled: Boolean) = setBool(KEY_IMSAK, enabled)
    fun setMidnightEnabled(enabled: Boolean) = setBool(KEY_MIDNIGHT, enabled)
    fun setFirstThirdEnabled(enabled: Boolean) = setBool(KEY_FIRST_THIRD, enabled)
    fun setTahajudEnabled(enabled: Boolean) = setBool(KEY_TAHAJUD, enabled)
    fun setYasinReminderEnabled(enabled: Boolean) = setBool(KEY_YASIN, enabled)
    fun setKahfReminderEnabled(enabled: Boolean) = setBool(KEY_KAHF, enabled)
    fun setImportantDaysReminderEnabled(enabled: Boolean) = setBool(KEY_IMPORTANT_DAYS, enabled)

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
        const val PREFS_NAME = "saat_notification_prefs"
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
        private const val KEY_TAHAJUD_HOUR = "tahajudReminderHour"
        private const val KEY_TAHAJUD_MINUTE = "tahajudReminderMinute"
        private const val KEY_YASIN = "yasinReminderEnabled"
        private const val KEY_KAHF = "kahfReminderEnabled"
        private const val KEY_IMPORTANT_DAYS = "importantDaysReminderEnabled"
        private const val KEY_ADZAN_SOUND = "adhanSoundEnabled"
        private const val KEY_MON_THU_FAST = "monThuFastReminderEnabled"
        private const val KEY_DHUHA = "dhuhaReminderEnabled"
        private const val KEY_DHUHA_HOUR = "dhuhaReminderHour"
        private const val KEY_DHUHA_MINUTE = "dhuhaReminderMinute"
    }
}
