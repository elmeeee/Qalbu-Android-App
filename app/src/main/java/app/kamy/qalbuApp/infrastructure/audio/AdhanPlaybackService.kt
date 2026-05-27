package app.kamy.qalbuApp.infrastructure.audio

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import app.kamy.qalbuApp.MainActivity
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.infrastructure.notifications.NotificationChannels

/**
 * Foreground playback so adhan runs to completion even when the app is in the background.
 */
class AdhanPlaybackService : Service() {

    private var player: ExoPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        val rawRes = intent.getIntExtra(EXTRA_RAW_RES, 0)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        if (rawRes == 0) {
            stopSelf()
            return START_NOT_STICKY
        }

        NotificationChannels.ensureAll(this)
        startForeground(NOTIFICATION_ID, buildForegroundNotification(title, body))

        player?.release()
        val exo = ExoPlayer.Builder(this).build().also { player = it }
        exo.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        })
        val uri = Uri.parse("android.resource://$packageName/$rawRes")
        exo.setMediaItem(MediaItem.fromUri(uri))
        exo.prepare()
        exo.play()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun buildForegroundNotification(title: String, body: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NotificationChannels.ADHAN_PLAYBACK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title.ifBlank { getString(R.string.adhan_playback_title) })
            .setContentText(body.ifBlank { getString(R.string.adhan_playback_body) })
            .setContentIntent(pending)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 12_001
        private const val EXTRA_RAW_RES = "raw_res"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_BODY = "body"

        fun start(context: Context, @RawRes rawRes: Int, title: String, body: String) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                putExtra(EXTRA_RAW_RES, rawRes)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
            }
            context.startForegroundService(intent)
        }
    }
}
