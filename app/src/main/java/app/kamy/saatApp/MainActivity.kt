package app.kamy.saatApp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatTheme
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.infrastructure.preferences.ThemePreferencesStore
import app.kamy.saatApp.infrastructure.review.AppReviewManager
import app.kamy.saatApp.ui.navigation.DeepLinkRoutes
import app.kamy.saatApp.ui.onboarding.OnboardingScreen
import app.kamy.saatApp.ui.permissions.ExactAlarmPermissionGate
import app.kamy.saatApp.ui.root.RootScreen
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var onboardingStore: OnboardingStore
    @Inject lateinit var themePreferencesStore: ThemePreferencesStore
    @Inject lateinit var appLanguageStore: AppLanguageStore

    private val deepLinkRoute = mutableStateOf<String?>(null)

    override fun attachBaseContext(newBase: Context) {
        val language = AppLanguageStore.from(newBase).current()
        super.attachBaseContext(AppLocale.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.remove()
        }
        super.onCreate(savedInstanceState)

        val startupError = try {
            AppReviewManager.recordAppLaunch(applicationContext)
            enableEdgeToEdge(
                statusBarStyle = androidx.activity.SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                ),
                navigationBarStyle = androidx.activity.SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                )
            )
            if (savedInstanceState == null && deepLinkRoute.value == null) {
                deepLinkRoute.value = DeepLinkRoutes.fromIntent(intent)
            }
            val needsOnboarding = !onboardingStore.isComplete()
            setContent {
                val pendingRoute by deepLinkRoute
                val currentTheme by themePreferencesStore.themeFlow.collectAsStateWithLifecycle(initialValue = app.kamy.saatApp.infrastructure.preferences.AppThemeColor.EMERALD)
                val currentLang by appLanguageStore.currentFlow.collectAsStateWithLifecycle()

                val localizedContext = androidx.compose.runtime.remember(currentLang) {
                    val locale = java.util.Locale.forLanguageTag(currentLang.tag)
                    java.util.Locale.setDefault(locale)
                    val config = android.content.res.Configuration(resources.configuration).apply {
                        setLocale(locale)
                    }
                    @Suppress("DEPRECATION")
                    resources.updateConfiguration(config, resources.displayMetrics)
                    @Suppress("DEPRECATION")
                    applicationContext.resources.updateConfiguration(config, applicationContext.resources.displayMetrics)
                    AppLocale.wrap(this@MainActivity, currentLang)
                }

                val localizedConfiguration = androidx.compose.runtime.remember(currentLang) {
                    val locale = java.util.Locale.forLanguageTag(currentLang.tag)
                    android.content.res.Configuration(resources.configuration).apply {
                        setLocale(locale)
                    }
                }

                var lastLang by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(currentLang) }
                var isLanguageLoading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                androidx.compose.runtime.LaunchedEffect(currentLang) {
                    if (lastLang != currentLang) {
                        lastLang = currentLang
                        isLanguageLoading = true
                        kotlinx.coroutines.delay(750)
                        isLanguageLoading = false
                    }
                }

                var showOnboarding by rememberSaveable { mutableStateOf(needsOnboarding) }

                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalConfiguration provides localizedConfiguration,
                    androidx.compose.ui.platform.LocalContext provides localizedContext
                ) {
                    SaatTheme(theme = currentTheme) {
                        Box(modifier = Modifier.fillMaxSize()) {
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

                            androidx.compose.animation.AnimatedVisibility(
                                visible = isLanguageLoading,
                                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)),
                                exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
                            ) {
                                LanguageChangingLoadingOverlay(language = currentLang)
                            }
                        }
                    }
                }
            }
            null
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Failed to initialize UI", t)
            t
        }

        if (startupError != null) {
            setContent {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Saat hit a startup issue",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Please reopen the app. If it keeps happening, update to the latest version.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = { recreate() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Try again")
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

@Composable
private fun LanguageChangingLoadingOverlay(
    language: app.kamy.saatApp.core.locale.AppLanguage,
    modifier: Modifier = Modifier
) {
    val loadingText = when (language) {
        app.kamy.saatApp.core.locale.AppLanguage.INDONESIAN -> "Menerapkan bahasa & memuat konten..."
        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Applying language & loading content..."
        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Menetapkan bahasa & memuatkan kandungan..."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SaatColors.HomeBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SaatColors.PureWhite, androidx.compose.foundation.shape.CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = SaatColors.HomeDarkGreen,
                    strokeWidth = 3.dp,
                    trackColor = SaatColors.ArcGold.copy(alpha = 0.25f)
                )
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_tasbih_3d),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = SaatColors.HomeDarkGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}