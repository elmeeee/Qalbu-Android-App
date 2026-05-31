package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyVerseNotificationStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _enabled = MutableStateFlow(isEnabled())
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _hour = MutableStateFlow(morningHour())
    val hour: StateFlow<Int> = _hour.asStateFlow()

    private val _minute = MutableStateFlow(morningMinute())
    val minute: StateFlow<Int> = _minute.asStateFlow()

    fun isEnabled(): Boolean =
        if (!prefs.contains(KEY_ENABLED)) true else prefs.getBoolean(KEY_ENABLED, true)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _enabled.value = enabled
    }

    fun morningHour(): Int =
        prefs.getInt(KEY_HOUR, DEFAULT_HOUR).coerceIn(0, 23)

    fun morningMinute(): Int =
        prefs.getInt(KEY_MINUTE, DEFAULT_MINUTE).coerceIn(0, 59)

    fun setMorningTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        prefs.edit().putInt(KEY_HOUR, h).putInt(KEY_MINUTE, m).apply()
        _hour.value = h
        _minute.value = m
    }

    fun formattedMorningTime(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, morningHour())
            set(Calendar.MINUTE, morningMinute())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(cal.time)
    }

    companion object {
        const val DEFAULT_HOUR = 7
        const val DEFAULT_MINUTE = 0
        private const val PREFS_NAME = "qalbu_notification_prefs"
        private const val KEY_ENABLED = "dailyVerseNotificationsEnabled"
        private const val KEY_HOUR = "dailyVerseNotificationHour"
        private const val KEY_MINUTE = "dailyVerseNotificationMinute"
    }
}
