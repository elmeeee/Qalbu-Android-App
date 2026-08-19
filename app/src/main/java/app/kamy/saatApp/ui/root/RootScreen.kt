package app.kamy.saatApp.ui.root

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.features.account.AccountScreen
import app.kamy.saatApp.features.quran.ChapterReaderScreen
import app.kamy.saatApp.features.quran.ChaptersScreen
import app.kamy.saatApp.features.tools.DoaZikirScreen
import app.kamy.saatApp.features.tools.DhikrScreen
import app.kamy.saatApp.features.tools.QiblaScreen
import app.kamy.saatApp.features.tools.ManzilScreen
import app.kamy.saatApp.features.tools.SpiritualToolsScreen
import app.kamy.saatApp.features.tools.ZakatCalculatorScreen
import app.kamy.saatApp.features.tools.faraidh.FaraidhCalculatorScreen
import app.kamy.saatApp.features.quran.QuranBookmarksScreen
import app.kamy.saatApp.features.today.PrayerCalendarScreen
import app.kamy.saatApp.features.today.PrayerTrackerCalendarScreen
import app.kamy.saatApp.features.today.TodayScreen
import app.kamy.saatApp.infrastructure.audio.AudioPlayerController
import app.kamy.saatApp.ui.components.FloatingAudioBar
import app.kamy.saatApp.ui.components.FloatingAudioBarMetrics
import app.kamy.saatApp.ui.components.FloatingTabBar
import app.kamy.saatApp.ui.components.ForceUpdateSheet
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.navigation.RootTab
import app.kamy.saatApp.infrastructure.update.AppUpdateManager
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RootEntryPoint {
    fun audioPlayer(): AudioPlayerController
}

internal fun shouldShowBottomBar(
    currentRoute: String?,
    isAccountDetailScreen: Boolean
): Boolean {
    val isReaderRoute = currentRoute?.startsWith("quran/reader") == true ||
        currentRoute?.startsWith("quran/juz") == true
    val isPrayerCalendarRoute = currentRoute == "prayer/calendar" ||
        currentRoute == "prayer/tracker/calendar"
    val isToolRoute = currentRoute?.startsWith("tools/") == true
    val isBookmarksRoute = currentRoute == "quran/bookmarks"
    return !isAccountDetailScreen && !isReaderRoute && !isPrayerCalendarRoute && !isToolRoute &&
        !isBookmarksRoute
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
    val audioState by audioPlayer.state.collectAsStateWithLifecycle()

    val updateInfo by AppUpdateManager.updateInfoFlow.collectAsStateWithLifecycle()
    var showForceUpdateSheet by remember { mutableStateOf(updateInfo.isUpdateAvailable) }

    LaunchedEffect(Unit) {
        AppUpdateManager.checkForUpdateAsync(context) { info ->
            if (info.isUpdateAvailable) {
                showForceUpdateSheet = true
            }
        }
    }

    LaunchedEffect(updateInfo.isUpdateAvailable) {
        if (updateInfo.isUpdateAvailable) {
            showForceUpdateSheet = true
        }
    }

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
        currentRoute?.startsWith("quran/juz") == true
    var isAccountDetailScreen by rememberSaveable { mutableStateOf(false) }
    var isTanyaSaatOpen by remember { mutableStateOf(false) }
    val showBottomBar = shouldShowBottomBar(currentRoute, isAccountDetailScreen) && !isTanyaSaatOpen
    val showAudioBar = audioState.currentUrl != null && !isTanyaSaatOpen
    val audioBarBottomPadding = if (showBottomBar) {
        floatingNavBottomPadding() + FloatingAudioBarMetrics.bottomGap
    } else {
        FloatingAudioBarMetrics.bottomGap
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
    ) {
        NavHost(
            navController = navController,
            startDestination = RootTab.Default.route,
            modifier = Modifier
                .fillMaxSize()
                .background(SaatColors.ScreenBackground)
        ) {
            composable(
                route = RootTab.Today.route,
                enterTransition = { fadeIn(tween(140)) },
                exitTransition = { fadeOut(tween(100)) },
                popEnterTransition = { fadeIn(tween(140)) },
                popExitTransition = { fadeOut(tween(100)) }
            ) {
                TodayScreen(
                    audioPlayer = audioPlayer,
                    onOpenPrayerCalendar = {
                        navController.navigate("prayer/calendar") { launchSingleTop = true }
                    },
                    onOpenTrackerCalendar = {
                        navController.navigate("prayer/tracker/calendar") { launchSingleTop = true }
                    },
                    onOpenChapterReader = { chapter, ayah ->
                        navController.navigate("quran/reader/${chapter}?ayah=${ayah}") { launchSingleTop = true }
                    },
                    onTanyaSaatOpenChanged = { isTanyaSaatOpen = it }
                )
            }
            composable("prayer/calendar") {
                PrayerCalendarScreen(onBack = { navController.popBackStack() })
            }
            composable("prayer/tracker/calendar") {
                PrayerTrackerCalendarScreen(
                    onBack = { navController.popBackStack() },
                    onOpenSunnahPrayer = {
                        navController.navigate("tools/sunnah-prayer") { launchSingleTop = true }
                    },
                    onOpenQuran = {
                        navController.navigate(RootTab.Quran.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenDhikr = {
                        navController.navigate("tools/doa-zikir") { launchSingleTop = true }
                    }
                )
            }
            composable(
                route = RootTab.Quran.route,
                enterTransition = { fadeIn(tween(140)) },
                exitTransition = { fadeOut(tween(100)) },
                popEnterTransition = { fadeIn(tween(140)) },
                popExitTransition = { fadeOut(tween(100)) }
            ) {
                ChaptersScreen(
                    onOpenChapter = { chapter, initialVerse ->
                        navController.navigate("quran/reader/${chapter.id}?ayah=${initialVerse ?: -1}")
                    },
                    onOpenJuz = { juzNumber, verseKey ->
                        val keyArg = verseKey?.let { java.net.URLEncoder.encode(it, Charsets.UTF_8.name()) }.orEmpty()
                        navController.navigate("quran/juz/$juzNumber?verseKey=$keyArg")
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
                enterTransition = { fadeIn(tween(140)) },
                exitTransition = { fadeOut(tween(100)) },
                popEnterTransition = { fadeIn(tween(140)) },
                popExitTransition = { fadeOut(tween(100)) }
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
            composable("tools/manzil") {
                ManzilScreen(onBack = { navController.popBackStack() })
            }
            composable("tools/radio") {
                app.kamy.saatApp.features.tools.radio.QuranRadioScreen(
                    audioPlayer = audioPlayer,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("tools/asmaul-husna") {
                app.kamy.saatApp.features.tools.asmaulhusna.AsmaulHusnaScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("tools/sunnah-prayer") {
                app.kamy.saatApp.features.tools.sunnah.SunnahPrayerScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDoaZikir = { navController.navigate("tools/doa-zikir") }
                )
            }
            composable("tools/jenazah") {
                app.kamy.saatApp.features.tools.janazah.JanazahPrayerScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("tools/fidyah") {
                app.kamy.saatApp.features.tools.fidyah.FidyahCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("tools/encyclopedia") {
                app.kamy.saatApp.features.tools.encyclopedia.EncyclopediaScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTopic = { topicId ->
                        navController.navigate("tools/encyclopedia/$topicId")
                    }
                )
            }
            composable("tools/hajj-umrah") {
                app.kamy.saatApp.features.tools.hajj.HajjUmrahScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "tools/encyclopedia/{topicId}",
                arguments = listOf(navArgument("topicId") { type = NavType.StringType })
            ) {
                app.kamy.saatApp.features.tools.encyclopedia.EncyclopediaDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVerse = { surah, ayah ->
                        navController.navigate("quran/reader/$surah?ayah=$ayah") { launchSingleTop = true }
                    }
                )
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
                    initialVerseKey = verseKey,
                    audioBarVisible = showAudioBar,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = RootTab.Account.route,
                enterTransition = { fadeIn(tween(140)) },
                exitTransition = { fadeOut(tween(100)) },
                popEnterTransition = { fadeIn(tween(140)) },
                popExitTransition = { fadeOut(tween(100)) }
            ) {
                AccountScreen(
                    onBack = null,
                    onAccountDetailScreenChanged = { isAccountDetailScreen = it }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.70f)
                            )
                        )
                    )
            ) {
                FloatingTabBar(
                    selectedRoute = currentRoute,
                    onTabSelected = { tab ->
                        if (currentRoute != tab.route) {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = (tab.route != RootTab.Default.route)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        if (showForceUpdateSheet) {
            ForceUpdateSheet(
                updateInfo = updateInfo,
                onDismiss = { showForceUpdateSheet = false }
            )
        }
    }
}
