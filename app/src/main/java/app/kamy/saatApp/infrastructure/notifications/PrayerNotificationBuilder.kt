package app.kamy.saatApp.infrastructure.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.infrastructure.audio.AdhanStopReceiver

object PrayerNotificationBuilder {

    fun build(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        silent: Boolean = false,
        showStopAdhan: Boolean = false,
        @RawRes adhanSoundRes: Int? = null,
        customPendingIntent: PendingIntent? = null,
        useFullScreenIntent: Boolean = false
    ): android.app.Notification {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = customPendingIntent ?: PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        if (useFullScreenIntent) {
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 1_000,
                app.kamy.saatApp.AdhanAlarmActivity.intent(
                    context = context,
                    title = title,
                    body = body,
                    prayerName = "tahajud"
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        val vibrationPattern = longArrayOf(0, 400, 200, 400)
        builder.setVibrate(vibrationPattern)

        if (silent) {
            builder.setSilent(true)
        } else {
            val soundUri = if (adhanSoundRes != null && adhanSoundRes != 0) {
                val resName = runCatching { context.resources.getResourceEntryName(adhanSoundRes) }.getOrNull() ?: "off_toggle_adzan"
                Uri.parse("android.resource://${context.packageName}/raw/$resName")
            } else {
                Uri.parse("android.resource://${context.packageName}/raw/off_toggle_adzan")
            }
            builder.setSound(soundUri)
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        }

        if (showStopAdhan) {
            val stopPending = PendingIntent.getBroadcast(
                context,
                notificationId + 50_000,
                AdhanStopReceiver.intent(context, notificationId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setDeleteIntent(stopPending)
            builder.addAction(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.adhan_stop),
                stopPending
            )
        }

        return builder.build()
    }
}
