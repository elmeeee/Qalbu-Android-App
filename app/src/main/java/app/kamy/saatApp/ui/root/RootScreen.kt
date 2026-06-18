package app.kamy.saatApp.ui.root

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import app.kamy.saatApp.features.account.AccountScreen
import app.kamy.saatApp.features.quran.ChapterReaderScreen
import app.kamy.saatApp.features.quran.ChaptersScreen
import app.kamy.saatApp.features.tools.DoaZikirScreen
import app.kamy.saatApp.features.tools.DhikrScreen
import app.kamy.saatApp.features.tools.QiblaScreen
import app.kamy.saatApp.features.tools.QiyamScreen
import app.kamy.saatApp.features.tools.SpiritualToolsScreen
import app.kamy.saatApp.features.tools.ZakatCalculatorScreen
import app.kamy.saatApp.features.tools.faraidh.FaraidhCalculatorScreen
import app.kamy.saatApp.features.quran.QuranBookmarksScreen
import app.kamy.saatApp.features.quran.MushafReaderScreen
import app.kamy.saatApp.features.reflect.ReflectScreen
import app.kamy.saatApp.features.today.PrayerCalendarScreen
import app.kamy.saatApp.features.today.PrayerTrackerCalendarScreen
import app.kamy.saatApp.features.today.TodayScreen
import app.kamy.saatApp.infrastructure.audio.AudioPlayerController
import app.kamy.saatApp.infrastructure.preferences.MushafReadingStore
import app.kamy.saatApp.infrastructure.audio.parseVerseKey
import app.kamy.saatApp.infrastructure.auth.OAuthService
import app.kamy.saatApp.ui.components.FloatingAudioBar
import app.kamy.saatApp.ui.components.FloatingAudioBarMetrics
import app.kamy.saatApp.ui.components.FloatingTabBar
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.navigation.RootTab
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
    val isReaderRoute = currentRoute?.startsWith("quran/reader") == true ||
        currentRoute?.startsWith("quran/juz") == true ||
        currentRoute?.startsWith("quran/mushaf") == true
    val isPrayerCalendarRoute = currentRoute == "prayer/calendar" ||
        currentRoute == "prayer/tracker/calendar"
    val isToolRoute = currentRoute?.startsWith("tools/") == true
    val isBookmarksRoute = currentRoute == "quran/bookmarks"
    val showBottomBar = !isReaderRoute && !isPrayerCalendarRoute && !isToolRoute &&
        !isBookmarksRoute
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
            composable(
                route = RootTab.Today.route,
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 8 } }
            ) {
                TodayScreen(
                    audioPlayer = audioPlayer,
                    onOpenPrayerCalendar = {
                        navController.navigate("prayer/calendar") { launchSingleTop = true }
                    },
                    onOpenTrackerCalendar = {
                        navController.navigate("prayer/tracker/calendar") { launchSingleTop = true }
                    }
                )
            }
            composable("prayer/calendar") {
                PrayerCalendarScreen(onBack = { navController.popBackStack() })
            }
            composable("prayer/tracker/calendar") {
                PrayerTrackerCalendarScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = RootTab.Reflect.route,
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 8 } }
            ) {
                ReflectScreen(
                    onSignIn = { navController.navigate(RootTab.Account.route) },
                    onOpenVerse = { verseKey ->
                        val (chapter, ayah) = parseVerseKey(verseKey) ?: return@ReflectScreen
                        navController.navigate("quran/reader/$chapter?ayah=$ayah") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = RootTab.Quran.route,
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 8 } }
            ) {
                ChaptersScreen(
                    onOpenChapter = { chapter, initialVerse ->
                        navController.navigate("quran/reader/${chapter.id}?ayah=${initialVerse ?: -1}")
                    },
                    onOpenJuz = { juzNumber, verseKey ->
                        val keyArg = verseKey?.let { java.net.URLEncoder.encode(it, Charsets.UTF_8.name()) }.orEmpty()
                        navController.navigate("quran/juz/$juzNumber?verseKey=$keyArg")
                    },
                    onOpenMushaf = { page ->
                        navController.navigate("quran/mushaf/$page") {
                            popUpTo(RootTab.Quran.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onOpenBookmarks = {
                        navController.navigate("quran/bookmarks") { launchSingleTop = true }
                    }
                )
            }
            composable("quran/bookmarks") {
                QuranBookmarksScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVerse = { chapter, ayah ->
                        navController.navigate("quran/reader/$chapter?ayah=$ayah") { launchSingleTop = true }
                    }
                )
            }
            composable(
                route = RootTab.Tools.route,
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 8 } }
            ) {
                SpiritualToolsScreen(
                    onOpenTool = { tool ->
                        navController.navigate("tools/$tool") { launchSingleTop = true }
                    }
                )
            }
            composable("tools/qibla") {
                QiblaScreen(onBack = { navController.popBackStack() })
            }
            composable("tools/dhikr") {
                DhikrScreen(onBack = { navController.popBackStack() })
            }
            composable("tools/doa-zikir") {
                DoaZikirScreen(onBack = { navController.popBackStack() })
            }
            composable("tools/zakat") {
                ZakatCalculatorScreen(onBack = { navController.popBackStack() })
            }
            composable("tools/qiyam") {
                QiyamScreen(onBack = { navController.popBackStack() })
            }
            composable("tools/faraidh",
                enterTransition = { slideInHorizontally(tween(280)) { it } + fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { slideOutHorizontally(tween(280)) { it } + fadeOut(tween(180)) }
            ) {
                FaraidhCalculatorScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVerse = { surah, ayah ->
                        navController.navigate("quran/reader/$surah?ayah=$ayah") { launchSingleTop = true }
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
            composable(
                route = "quran/juz/{juzNumber}?verseKey={verseKey}",
                arguments = listOf(
                    navArgument("juzNumber") { type = NavType.IntType },
                    navArgument("verseKey") { type = NavType.StringType; defaultValue = "" }
                )
            ) { entry ->
                val verseKey = entry.arguments?.getString("verseKey")
                    ?.takeIf { it.isNotBlank() }
                    ?.let { java.net.URLDecoder.decode(it, Charsets.UTF_8.name()) }
                ChapterReaderScreen(
                    audioPlayer = audioPlayer,
                    initialVerseKey = verseKey,
                    audioBarVisible = showAudioBar,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "quran/mushaf/{page}",
                arguments = listOf(
                    navArgument("page") { type = NavType.IntType; defaultValue = 1 }
                )
            ) { entry ->
                val page = entry.arguments?.getInt("page")?.coerceIn(1, MushafReadingStore.totalPages) ?: 1
                key(page) {
                    MushafReaderScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(
                route = RootTab.Account.route,
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { it / 8 } }
            ) {
                AccountScreen(
                    oauthService = oauthService,
                    authService = authService,
                    onBack = null
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
                reserveTrailingSpace = if (isReaderRoute) 52.dp else 0.dp,
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
