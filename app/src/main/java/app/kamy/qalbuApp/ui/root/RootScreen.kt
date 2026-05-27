package app.kamy.qalbuApp.ui.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.kamy.qalbuApp.features.account.AccountScreen
import app.kamy.qalbuApp.features.quran.ChapterReaderScreen
import app.kamy.qalbuApp.features.quran.ChaptersScreen
import app.kamy.qalbuApp.features.reflect.ReflectScreen
import app.kamy.qalbuApp.features.today.TodayScreen
import app.kamy.qalbuApp.infrastructure.audio.AudioPlayerController
import app.kamy.qalbuApp.infrastructure.auth.OAuthService
import app.kamy.qalbuApp.ui.components.FloatingTabBar
import app.kamy.qalbuApp.ui.navigation.RootTab
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthorizationService

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RootEntryPoint {
    fun audioPlayer(): AudioPlayerController
    fun oauthService(): OAuthService
    fun authorizationService(): AuthorizationService
}

/**
 * Root shell — tab content is full-screen; [FloatingTabBar] overlays the bottom (iOS-style).
 */
@Composable
fun RootScreen() {
    val context = LocalContext.current
    val entryPoint = remember(context.applicationContext) {
        EntryPointAccessors.fromApplication(context.applicationContext, RootEntryPoint::class.java)
    }
    val audioPlayer = entryPoint.audioPlayer()
    val oauthService = entryPoint.oauthService()
    val authService = entryPoint.authorizationService()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute?.startsWith("quran/reader") != true &&
        currentRoute != RootTab.Account.route

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = RootTab.Default.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(RootTab.Today.route) {
                TodayScreen(
                    audioPlayer = audioPlayer,
                    onReflectNavigate = { navController.navigate(RootTab.Reflect.route) },
                    onAccountNavigate = { navController.navigate(RootTab.Account.route) }
                )
            }
            composable(RootTab.Reflect.route) {
                ReflectScreen(
                    onSignIn = { navController.navigate(RootTab.Account.route) }
                )
            }
            composable(RootTab.Quran.route) {
                ChaptersScreen(
                    onOpenChapter = { chapter, initialVerse ->
                        navController.navigate("quran/reader/${chapter.id}?ayah=${initialVerse ?: -1}")
                    }
                )
            }
            composable(
                route = "quran/reader/{chapter}?ayah={ayah}",
                arguments = listOf(
                    navArgument("chapter") { type = NavType.IntType },
                    navArgument("ayah") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { entry ->
                val initialAyah = entry.arguments?.getInt("ayah")?.takeIf { it > 0 }
                ChapterReaderScreen(
                    audioPlayer = audioPlayer,
                    initialVerseNumber = initialAyah,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(RootTab.Account.route) {
                AccountScreen(
                    oauthService = oauthService,
                    authService = authService,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showBottomBar) {
            FloatingTabBar(
                selectedRoute = currentRoute,
                onTabSelected = { tab ->
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
