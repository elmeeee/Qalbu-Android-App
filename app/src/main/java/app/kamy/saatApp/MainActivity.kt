package app.kamy.saatApp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.kamy.saatApp.design.theme.AlKhatibTheme
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.ui.navigation.DeepLinkRoutes
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.permissions.ExactAlarmPermissionGate
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.ui.onboarding.OnboardingScreen
import app.kamy.saatApp.ui.root.RootScreen
import app.kamy.saatApp.ui.splash.AppSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.compose.runtime.collectAsState
import app.kamy.saatApp.infrastructure.preferences.ThemePreferencesStore
import app.kamy.saatApp.infrastructure.review.AppReviewManager
import app.kamy.saatApp.ui.adhan.AdhanFullScreenOverlay
import app.kamy.saatApp.infrastructure.audio.AdhanStopReceiver

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var onboardingStore: OnboardingStore
    @Inject lateinit var themePreferencesStore: ThemePreferencesStore

    private val deepLinkRoute = mutableStateOf<String?>(null)
    private var currentIntent by mutableStateOf<Intent?>(null)

    override fun attachBaseContext(newBase: Context) {
        val language = AppLanguageStore.from(newBase).current()
        super.attachBaseContext(AppLocale.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AppReviewManager.recordAppLaunch(applicationContext)

        if (intent.getBooleanExtra("from_adhan_full_screen", false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                )
            }
        }

        currentIntent = intent

        enableEdgeToEdge()
        val needsOnboarding = !onboardingStore.isComplete()
        setContent {
            val resolvedIntent = currentIntent ?: intent
            val isAdhanFullScreen = resolvedIntent.getBooleanExtra("from_adhan_full_screen", false)
            val adhanTitle = resolvedIntent.getStringExtra("adhan_title") ?: ""
            val adhanBody = resolvedIntent.getStringExtra("adhan_body") ?: ""

            var showGreetingSplash by rememberSaveable { mutableStateOf(true) }
            var showOnboarding by rememberSaveable { mutableStateOf(needsOnboarding) }
            val pendingRoute by deepLinkRoute
            val currentTheme by themePreferencesStore.themeFlow.collectAsState(initial = app.kamy.saatApp.infrastructure.preferences.AppThemeColor.EMERALD)

            AlKhatibTheme(theme = currentTheme) {
                if (isAdhanFullScreen) {
                    AdhanFullScreenOverlay(
                        title = adhanTitle,
                        body = adhanBody,
                        onStopClick = {
                            sendBroadcast(Intent(this@MainActivity, AdhanStopReceiver::class.java).apply {
                                action = AdhanStopReceiver.ACTION_STOP
                            })
                            finish()
                        }
                    )
                } else {
                    when {
                    showGreetingSplash -> AppSplashScreen(onFinished = { showGreetingSplash = false })
                    showOnboarding -> OnboardingScreen(onFinished = { showOnboarding = false })
                    else -> {
                        ExactAlarmPermissionGate()
                        app.kamy.saatApp.ui.permissions.FullScreenIntentPermissionGate()
                        RootScreen(
                            pendingDeepLinkRoute = pendingRoute,
                            onDeepLinkHandled = { deepLinkRoute.value = null }
                        )
                    }
                }
            }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkRoute.value = DeepLinkRoutes.fromIntent(intent)
        currentIntent = intent

        if (intent.getBooleanExtra("from_adhan_full_screen", false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                )
            }
        }
    }
}