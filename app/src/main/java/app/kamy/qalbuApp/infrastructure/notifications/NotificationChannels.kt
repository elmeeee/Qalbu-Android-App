package app.kamy.qalbuApp.infrastructure.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val DAILY_VERSE = "daily_verse"
    const val PRAYER = "prayer_times"
    const val SUNNAH = "sunnah_reminders"
    const val ADHAN_PLAYBACK = "adhan_playback"
    const val MEDIA_PLAYBACK = "media_playback"

    fun ensureAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                DAILY_VERSE,
                "Daily verse",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Morning Quran verse reminder" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                PRAYER,
                "Prayer times",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Adzan and night prayer reminders" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                SUNNAH,
                "Sunnah reading",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Weekly surah reading reminders" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                ADHAN_PLAYBACK,
                "Adhan playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Plays the call to prayer — use lock screen or headset controls to stop"
                setSound(null, null)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                MEDIA_PLAYBACK,
                context.getString(app.kamy.qalbuApp.R.string.recitation_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Quran recitation controls on lock screen and in background"
                setSound(null, null)
            }
        )
    }
}
