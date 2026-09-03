package app.kamy.saatApp.infrastructure.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RawRes
import android.media.RingtoneManager
import app.kamy.saatApp.R

object NotificationChannels {
    const val DAILY_VERSE = "daily_verse_v6"
    const val PRAYER = "prayer_times_v8"
    const val PRAYER_ALERT = "prayer_alert_v8"
    const val SUNNAH = "sunnah_reminders_v5"
    const val ADHAN_PLAYBACK = "adhan_playback"
    const val ADHAN_ALERT = "adhan_alert_v5"
    const val MEDIA_PLAYBACK = "media_playback"
    /** Old channel IDs that must be deleted so the new configuration takes effect. */
    private val DEPRECATED_CHANNELS = listOf(
        "daily_verse_v1", "daily_verse_v2", "daily_verse_v3", "daily_verse_v4", "daily_verse_v5",
        "prayer_times_v1", "prayer_times_v2", "prayer_times_v3", "prayer_times_v4", "prayer_times_v5", "prayer_times_v6", "prayer_times_v7",
        "prayer_alert_v1", "prayer_alert_v2", "prayer_alert_v3", "prayer_alert_v4", "prayer_alert_v5", "prayer_alert_v6", "prayer_alert_v7",
        "sunnah_reminders_v1", "sunnah_reminders_v2", "sunnah_reminders_v3", "sunnah_reminders_v4",
        "prayer_tracker_v1", "prayer_tracker_v2", "prayer_tracker_v3", "prayer_tracker_v4", "prayer_tracker_v5",
        "adhan_alert_v1", "adhan_alert_v2", "adhan_alert_v3", "adhan_alert_v4"
    )

    fun ensureAll(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = Uri.parse("android.resource://${context.packageName}/raw/off_toggle_adzan")

        // Clean up deprecated channels so new audio attributes take effect.
        DEPRECATED_CHANNELS.forEach { oldId ->
            runCatching { manager.deleteNotificationChannel(oldId) }
        }

        val notificationAudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Prayer channels use USAGE_ALARM so the sound reliably bypasses DND on all OEMs.
        val alarmAudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val vibrationPattern = longArrayOf(0, 400, 200, 400)

        manager.createNotificationChannel(
            NotificationChannel(
                DAILY_VERSE,
                context.getString(R.string.channel_daily_verse),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_daily_verse_desc)
                setSound(soundUri, notificationAudioAttributes)
                enableVibration(true)
                setVibrationPattern(vibrationPattern)
                setBypassDnd(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                PRAYER,
                context.getString(R.string.channel_prayer),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_prayer_desc)
                setSound(soundUri, alarmAudioAttributes)
                enableVibration(true)
                setVibrationPattern(vibrationPattern)
                setBypassDnd(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                PRAYER_ALERT,
                context.getString(R.string.channel_prayer),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_prayer_desc)
                setSound(soundUri, alarmAudioAttributes)
                enableVibration(true)
                setVibrationPattern(vibrationPattern)
                setBypassDnd(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                SUNNAH,
                context.getString(R.string.channel_sunnah),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_sunnah_desc)
                setSound(soundUri, notificationAudioAttributes)
                enableVibration(true)
                setVibrationPattern(vibrationPattern)
                setBypassDnd(true)
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                ADHAN_PLAYBACK,
                context.getString(R.string.adhan_playback_title),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.adhan_playback_body)
                setSound(null, null)
                enableVibration(true)
                setVibrationPattern(vibrationPattern)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                MEDIA_PLAYBACK,
                context.getString(R.string.media_playback_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.media_playback_channel_name)
                setSound(null, null)
            }
        )
    }

    /** Channel with custom adhan sound for notification fallback when foreground playback cannot start. */
    fun ensureAdhanAlert(context: Context, @RawRes rawRes: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(ADHAN_ALERT) != null) return
        val resName = runCatching { context.resources.getResourceEntryName(rawRes) }.getOrNull()
        val soundUri = if (resName != null) {
            Uri.parse("android.resource://${context.packageName}/raw/$resName")
        } else {
            Uri.parse("android.resource://${context.packageName}/$rawRes")
        }
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        manager.createNotificationChannel(
            NotificationChannel(
                ADHAN_ALERT,
                context.getString(R.string.channel_adhan_alert),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_adhan_alert_desc)
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                setBypassDnd(true)
            }
        )
    }
}

