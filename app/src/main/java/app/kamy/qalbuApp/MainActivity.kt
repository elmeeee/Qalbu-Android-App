package app.kamy.qalbuApp

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
import app.kamy.qalbuApp.ui.root.RootScreen
import app.kamy.qalbuApp.ui.splash.AppSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showGreetingSplash by rememberSaveable { mutableStateOf(true) }

            AlKhatibTheme {
                if (showGreetingSplash) {
                    AppSplashScreen(onFinished = { showGreetingSplash = false })
                } else {
                    RootScreen()
                }
            }
        }
    }
}
