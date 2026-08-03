package app.kamy.saatApp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.ui.edge.enableEdgeToEdge
import app.kamy.saatApp.design.theme.SaatTheme
import app.kamy.saatApp.infrastructure.audio.AdhanStopReceiver
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.ThemePreferencesStore
import app.kamy.saatApp.ui.adhan.AdhanFullScreenOverlay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Lightweight, dedicated Activity for the adhan full-screen alarm overlay.
 *
 * This must be separate from [MainActivity] so that:
 * 1. It can be declared with `showWhenLocked` / `turnScreenOn` in the manifest,
 *    which is the only reliable way to appear above the lock screen on all API levels.
 * 2. It launches instantly (no onboarding, no splash, no Hilt injection) so the
 *    `USE_FULL_SCREEN_INTENT` fires before the user notices any delay.
 * 3. [MainActivity] stays clean and single-responsibility.
 *
 * Toggle ON  → [app.kamy.saatApp.infrastructure.audio.AdhanPlaybackService] starts this Activity
 *              as a full-screen intent → adhan audio + this overlay.
 * Toggle OFF → regular notification only, no adhan audio, this Activity is never started.
 */
import android.content.BroadcastReceiver
import android.content.IntentFilter
import app.kamy.saatApp.infrastructure.audio.AdhanPlaybackService

class AdhanAlarmActivity : ComponentActivity() {

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val language = AppLanguageStore.from(newBase).current()
        super.attachBaseContext(AppLocale.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter().apply {
            addAction(AdhanPlaybackService.ACTION_ADHAN_STOPPED)
            addAction(AdhanStopReceiver.ACTION_STOP)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stopReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(stopReceiver, filter)
        }

        // Show above the lock screen and turn the screen on — mandatory for alarm-style UX.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge(window, window.decorView, true)

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME)

        // ThemePreferencesStore is DataStore-backed — safe to instantiate without Hilt here.
        val themeStore = ThemePreferencesStore.from(applicationContext)

        setContent {
            val currentTheme by themeStore.themeFlow.collectAsState(
                initial = app.kamy.saatApp.infrastructure.preferences.AppThemeColor.EMERALD
            )
            SaatTheme(theme = currentTheme) {
                AdhanFullScreenOverlay(
                    title = title,
                    body = body,
                    prayerName = prayerName,
                    onStopClick = {
                        sendBroadcast(
                            AdhanStopReceiver.intent(this@AdhanAlarmActivity)
                        )
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(stopReceiver) }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TITLE = "adhan_alarm_title"
        const val EXTRA_BODY = "adhan_alarm_body"
        const val EXTRA_PRAYER_NAME = "adhan_alarm_prayer_name"

        fun intent(
            context: Context,
            title: String,
            body: String,
            prayerName: String? = null
        ): Intent =
            Intent(context, AdhanAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                if (!prayerName.isNullOrBlank()) {
                    putExtra(EXTRA_PRAYER_NAME, prayerName)
                }
            }
    }
}
