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
import app.kamy.saatApp.design.theme.SaatTheme
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.ui.navigation.DeepLinkRoutes
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.permissions.ExactAlarmPermissionGate
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.ui.onboarding.OnboardingScreen
import app.kamy.saatApp.ui.root.RootScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.infrastructure.preferences.ThemePreferencesStore
import app.kamy.saatApp.infrastructure.review.AppReviewManager

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
        AppReviewManager.recordAppLaunch(applicationContext)

        enableEdgeToEdge()
        val needsOnboarding = !onboardingStore.isComplete()
        setContent {
            val pendingRoute by deepLinkRoute
            val currentTheme by themePreferencesStore.themeFlow.collectAsStateWithLifecycle(initialValue = app.kamy.saatApp.infrastructure.preferences.AppThemeColor.EMERALD)
            val languageStore = androidx.compose.runtime.remember { AppLanguageStore.from(this@MainActivity) }
            val currentLang by languageStore.currentFlow.collectAsStateWithLifecycle(initialValue = app.kamy.saatApp.core.locale.AppLanguage.INDONESIAN)

            val localizedConfiguration = androidx.compose.runtime.remember(currentLang) {
                val locale = java.util.Locale.forLanguageTag(currentLang.tag)
                java.util.Locale.setDefault(locale)
                android.content.res.Configuration(resources.configuration).apply {
                    setLocale(locale)
                }
            }

            androidx.compose.runtime.LaunchedEffect(currentLang) {
                @Suppress("DEPRECATION")
                resources.updateConfiguration(localizedConfiguration, resources.displayMetrics)
            }

            var showOnboarding by rememberSaveable { mutableStateOf(needsOnboarding) }

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalConfiguration provides localizedConfiguration
            ) {
                SaatTheme(theme = currentTheme) {
                    when {
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
    }
}