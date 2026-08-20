package app.kamy.saatApp.infrastructure.audio

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import app.kamy.saatApp.MainActivity
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.notifications.NotificationChannels
import app.kamy.saatApp.infrastructure.util.BootContextChecker
import com.google.common.collect.ImmutableList
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecitationPlaybackService : MediaSessionService() {

    @Inject lateinit var playbackEngine: PlaybackEngine

    override fun attachBaseContext(newBase: Context) {
        val language = AppLanguageStore.from(newBase).current()
        super.attachBaseContext(AppLocale.wrap(newBase, language))
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureAll(this)
        setMediaNotificationProvider(CustomMediaNotificationProvider(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val player = runCatching { playbackEngine.player }.getOrNull()
        val isPlaying = player?.playWhenReady == true || player?.isPlaying == true

        val isBoot = BootContextChecker.isRecentlyBooted() || intent == null
        if (isBoot) {
            safePromoteToForegroundAndStop()
            return START_NOT_STICKY
        }

        val fgStarted = promoteToForeground()

        if (!fgStarted && !isPlaying) {
            safePromoteToForegroundAndStop()
            return START_NOT_STICKY
        }

        return try {
            super.onStartCommand(intent, flags, startId)
        } catch (e: Exception) {
            safePromoteToForegroundAndStop()
            START_NOT_STICKY
        }
    }

    private fun promoteToForeground(): Boolean {
        return runCatching {
            val notification = buildPlaceholderNotification()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        }.isSuccess
    }

    private fun safePromoteToForegroundAndStop() {
        promoteToForeground()
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
        stopSelf()
    }

    @OptIn(UnstableApi::class)
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        playbackEngine.mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = playbackEngine.player
        if (player.playWhenReady && player.playbackState != Player.STATE_IDLE) {
            return
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun buildPlaceholderNotification(): android.app.Notification {
        val player = runCatching { playbackEngine.player }.getOrNull()
        val metadata = player?.mediaMetadata
        val isRadio = metadata?.description?.toString() == "RADIO" ||
            metadata?.artist?.toString() == "Radio Quran" ||
            metadata?.albumTitle?.toString()?.contains("Live Radio") == true

        val title: String
        val text: String

        if (isRadio) {
            title = getString(R.string.notification_radio_title)
            text = metadata?.title?.toString()?.ifBlank { "Radio Quran" } ?: "Radio Quran"
        } else if (metadata != null && !metadata.title.isNullOrBlank()) {
            val rawSurahName = metadata.title.toString()
            val cleanSurahName = rawSurahName
                .removePrefix("Surah ")
                .removePrefix("Surat ")
                .removePrefix("Surah")
                .removePrefix("Surat")
                .trim()
                .ifBlank { "Al-Qur'an" }
            title = getString(R.string.notification_recitation_title_format, cleanSurahName)
            text = metadata.artist?.toString()?.ifBlank { getString(R.string.app_name) } ?: getString(R.string.app_name)
        } else {
            title = getString(R.string.app_name)
            text = getString(R.string.media_playback_channel_name)
        }

        return NotificationCompat.Builder(this, NotificationChannels.MEDIA_PLAYBACK)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 8401
    }
}

@OptIn(UnstableApi::class)
private class CustomMediaNotificationProvider(
    private val context: Context
) : MediaNotification.Provider {

    private val defaultProvider = DefaultMediaNotificationProvider.Builder(context)
        .setChannelId(NotificationChannels.MEDIA_PLAYBACK)
        .setChannelName(R.string.media_playback_channel_name)
        .setNotificationId(RecitationPlaybackService.NOTIFICATION_ID)
        .build()

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        val defaultNotification = defaultProvider.createNotification(
            mediaSession,
            customLayout,
            actionFactory,
            onNotificationChangedCallback
        )

        val metadata = mediaSession.player.mediaMetadata
        val isRadio = metadata.description?.toString() == "RADIO" ||
            metadata.artist?.toString() == "Radio Quran" ||
            metadata.albumTitle?.toString()?.contains("Live Radio") == true

        val contentTitle: String
        val contentText: String

        if (isRadio) {
            // Format 1 — Saat Memutar Radio Islami:
            // Baris 1 (Judul Notifikasi): Sedang mendengarkan radio islami
            // Baris 2 (Teks Notifikasi): {nama radionya} (contoh: IKIM FM)
            contentTitle = context.getString(R.string.notification_radio_title)
            contentText = metadata.title?.toString()?.ifBlank { "Radio Quran" } ?: "Radio Quran"
        } else {
            // Format 2 — Saat Memutar Tilawah / Murottal Surah:
            // Baris 1 (Judul Notifikasi): Sedang memutar surah {nama surahnya} (contoh: Sedang memutar surah Al-Fatihah)
            // Baris 2 (Teks Notifikasi): {nama reciter} (contoh: Mishary Rashid Alafasy)
            val rawSurahName = metadata.title?.toString() ?: ""
            val cleanSurahName = rawSurahName
                .removePrefix("Surah ")
                .removePrefix("Surat ")
                .removePrefix("Surah")
                .removePrefix("Surat")
                .trim()
                .ifBlank { "Al-Qur'an" }

            contentTitle = context.getString(R.string.notification_recitation_title_format, cleanSurahName)
            contentText = metadata.artist?.toString()?.ifBlank { context.getString(R.string.app_name) } ?: context.getString(R.string.app_name)
        }

        val updatedNotification = NotificationCompat.Builder(context, defaultNotification.notification)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSubText(null)
            .build()

        return MediaNotification(RecitationPlaybackService.NOTIFICATION_ID, updatedNotification)
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean = defaultProvider.handleCustomCommand(session, action, extras)
}
