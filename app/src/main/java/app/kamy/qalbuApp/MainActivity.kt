package app.kamy.qalbuApp

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
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.qalbuApp.ui.root.RootScreen
import app.kamy.qalbuApp.ui.splash.AppSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deepLinkRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        deepLinkRoute.value = intent.toQuranDeepLinkRoute()
        enableEdgeToEdge()
        setContent {
            var showGreetingSplash by rememberSaveable { mutableStateOf(true) }
            val pendingRoute by deepLinkRoute

            AlKhatibTheme {
                if (showGreetingSplash) {
                    AppSplashScreen(onFinished = { showGreetingSplash = false })
                } else {
                    RootScreen(
                        pendingDeepLinkRoute = pendingRoute,
                        onDeepLinkHandled = { deepLinkRoute.value = null }
                    )
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
