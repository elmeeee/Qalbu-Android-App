package app.kamy.qalbuApp.ui.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import app.kamy.qalbuApp.ui.navigation.RootTab
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import androidx.compose.ui.platform.LocalContext
import net.openid.appauth.AuthorizationService

/**
 * Hilt entry point to grab application-scoped dependencies inside a @Composable
 * without re-declaring them as Activity-injected fields. Used here so RootScreen
 * can hand the singleton AudioPlayerController and OAuthService down the tree.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RootEntryPoint {
    fun audioPlayer(): AudioPlayerController
    fun oauthService(): OAuthService
    fun authorizationService(): AuthorizationService
}

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

    Scaffold(
        bottomBar = {
            val showBottomBar = currentRoute?.startsWith("quran/reader") != true
            if (showBottomBar) {
                NavigationBar {
                    RootTab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
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
                            icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RootTab.Default.route,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(RootTab.Today.route) {
                TodayScreen(
                    audioPlayer = audioPlayer,
                    onReflectNavigate = { navController.navigate(RootTab.Reflect.route) }
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
                    authService = authService
                )
            }
        }
    }
}
