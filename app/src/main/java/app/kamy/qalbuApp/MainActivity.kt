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
import app.kamy.qalbuApp.ui.navigation.DeepLinkRoutes
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
        deepLinkRoute.value = DeepLinkRoutes.fromIntent(intent)
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
        deepLinkRoute.value = DeepLinkRoutes.fromIntent(intent)
    }
}
