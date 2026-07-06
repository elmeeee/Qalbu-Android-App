package app.kamy.saatApp

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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var onboardingStore: OnboardingStore
    @Inject lateinit var themePreferencesStore: ThemePreferencesStore

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
            val currentTheme by themePreferencesStore.themeFlow.collectAsState()

            AlKhatibTheme(theme = currentTheme) {
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