package app.kamy.qalbuApp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.kamy.qalbuApp.design.theme.AlKhatibTheme
import app.kamy.qalbuApp.core.locale.AppLocale
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.qalbuApp.infrastructure.preferences.AppLanguageStore
import app.kamy.qalbuApp.ui.permissions.ExactAlarmPermissionGate
import app.kamy.qalbuApp.infrastructure.preferences.OnboardingStore
import app.kamy.qalbuApp.ui.onboarding.OnboardingScreen
import app.kamy.qalbuApp.ui.root.RootScreen
import app.kamy.qalbuApp.ui.splash.AppSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var onboardingStore: OnboardingStore

    private val deepLinkRoute = mutableStateOf<String?>(null)

    override fun attachBaseContext(newBase: Context) {
        val language = AppLanguageStore.from(newBase).current()
        super.attachBaseContext(AppLocale.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        deepLinkRoute.value = intent.toQuranDeepLinkRoute()
        enableEdgeToEdge()
        val needsOnboarding = !onboardingStore.isComplete()
        setContent {
            var showGreetingSplash by rememberSaveable { mutableStateOf(true) }
            var showOnboarding by rememberSaveable { mutableStateOf(needsOnboarding) }
            val pendingRoute by deepLinkRoute

            AlKhatibTheme {
                when {
                    showGreetingSplash -> AppSplashScreen(onFinished = { showGreetingSplash = false })
                    showOnboarding -> OnboardingScreen(onFinished = { showOnboarding = false })
                    else -> {
                        ExactAlarmPermissionGate()
                        RootScreen(
                            pendingDeepLinkRoute = pendingRoute,
                            onDeepLinkHandled = { deepLinkRoute.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkRoute.value = intent.toQuranDeepLinkRoute()
    }

    private fun Intent.toQuranDeepLinkRoute(): String? {
        val chapter = getIntExtra(DailyVerseNotificationScheduler.EXTRA_CHAPTER, -1)
        if (chapter <= 0) return null
        val ayah = getIntExtra(DailyVerseNotificationScheduler.EXTRA_AYAH, -1)
        return if (ayah > 0) {
            "quran/reader/$chapter?ayah=$ayah"
        } else {
            "quran/reader/$chapter?ayah=-1"
        }
    }
}
