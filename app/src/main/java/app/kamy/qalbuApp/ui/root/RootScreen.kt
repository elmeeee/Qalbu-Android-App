package app.kamy.qalbuApp.ui.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import app.kamy.qalbuApp.ui.components.FloatingAudioBar
import app.kamy.qalbuApp.ui.components.FloatingAudioBarMetrics
import app.kamy.qalbuApp.ui.components.FloatingTabBar
import app.kamy.qalbuApp.ui.layout.floatingNavBottomPadding
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

@Composable
fun RootScreen(
    pendingDeepLinkRoute: String? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val entryPoint = remember(context.applicationContext) {
        EntryPointAccessors.fromApplication(context.applicationContext, RootEntryPoint::class.java)
    }
    val audioPlayer = entryPoint.audioPlayer()
    val audioState by audioPlayer.state.collectAsState()
    val oauthService = entryPoint.oauthService()
    val authService = entryPoint.authorizationService()

    val navController = rememberNavController()
    LaunchedEffect(pendingDeepLinkRoute) {
        val route = pendingDeepLinkRoute ?: return@LaunchedEffect
        navController.navigate(route) {
            launchSingleTop = true
        }
        onDeepLinkHandled()
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isReaderRoute = currentRoute?.startsWith("quran/reader") == true
    val showBottomBar = !isReaderRoute && currentRoute != RootTab.Account.route
    val showAudioBar = audioState.currentUrl != null
    val audioBarBottomPadding = if (showBottomBar) {
        floatingNavBottomPadding() + FloatingAudioBarMetrics.bottomGap
    } else {
        FloatingAudioBarMetrics.bottomGap
    }

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
                    audioBarVisible = showAudioBar,
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

        if (showAudioBar) {
            FloatingAudioBar(
                state = audioState,
                visible = true,
                onToggle = { audioPlayer.toggle() },
                onDismiss = { audioPlayer.stop() },
                onOpenPlayback = {
                    val chapter = audioState.chapterNumber ?: return@FloatingAudioBar
                    val ayah = audioState.ayahNumber ?: -1
                    val readerRoute = "quran/reader/$chapter?ayah=$ayah"
                    if (currentRoute != readerRoute) {
                        navController.navigate(readerRoute) {
                            launchSingleTop = true
                        }
                    }
                }.takeIf { audioState.hasReaderNavigation },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = audioBarBottomPadding)
            )
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
