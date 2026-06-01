package app.kamy.qalbuApp.infrastructure.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import app.kamy.qalbuApp.R

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
                context.getString(R.string.channel_daily_verse),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.channel_daily_verse_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                PRAYER,
                context.getString(R.string.channel_prayer),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.channel_prayer_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                SUNNAH,
                context.getString(R.string.channel_sunnah),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.channel_sunnah_desc) }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                ADHAN_PLAYBACK,
                context.getString(R.string.adhan_playback_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.adhan_playback_body)
                setSound(null, null)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                MEDIA_PLAYBACK,
                context.getString(R.string.recitation_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.recitation_notification_channel)
                setSound(null, null)
            }
        )
    }
}
