package app.kamy.saatApp.features.today

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import coil.compose.AsyncImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import app.kamy.saatApp.domain.model.ReadingSession
import app.kamy.saatApp.domain.model.RecitationPayload
import app.kamy.saatApp.design.theme.SaatColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import app.kamy.saatApp.features.today.components.getPrayerCardDrawable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import app.kamy.saatApp.design.components.SaatPullToRefresh
import app.kamy.saatApp.ui.permissions.areAppNotificationsEnabled
import app.kamy.saatApp.ui.permissions.canScheduleExactAlarms
import app.kamy.saatApp.ui.permissions.hasAggressiveOemBatteryManagement
import app.kamy.saatApp.ui.permissions.isIgnoringBatteryOptimizations
import app.kamy.saatApp.ui.permissions.openAppNotificationSettings
import app.kamy.saatApp.ui.permissions.openBackgroundReliabilitySettings
import app.kamy.saatApp.ui.permissions.openExactAlarmSettings
import app.kamy.saatApp.R
import app.kamy.saatApp.features.today.components.TafsirSheet
import app.kamy.saatApp.features.today.components.TodayImportantDayBanner
import app.kamy.saatApp.features.today.components.TodayHeader
import app.kamy.saatApp.features.today.components.PrayerDashboardCard
import app.kamy.saatApp.features.today.components.PrayerLocationSheet
import app.kamy.saatApp.features.today.components.PrayerTrackerCard
import app.kamy.saatApp.features.today.components.TodayReciterSheet
import app.kamy.saatApp.features.today.components.TodayVerseOfDaySection
import app.kamy.saatApp.infrastructure.preferences.LocationMode
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.ui.components.CoachMarkOverlay
import app.kamy.saatApp.ui.components.coachMarkTarget
import app.kamy.saatApp.ui.components.rememberCoachMarkState
import app.kamy.saatApp.features.share.AiShareSheet
import app.kamy.saatApp.infrastructure.audio.AudioPlayerController
import app.kamy.saatApp.ui.layout.floatingNavAndAudioBottomPadding
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodayScreen(
    audioPlayer: AudioPlayerController,
    onOpenPrayerCalendar: () -> Unit = {},
    onOpenTrackerCalendar: () -> Unit = {},
    onOpenChapterReader: (Int, Int) -> Unit = { _, _ -> },
    onTanyaSaatOpenChanged: (Boolean) -> Unit = {}
) {
    val todayVm: TodayViewModel = hiltViewModel()
    val prayerVm: PrayerDashboardViewModel = hiltViewModel()
    val trackerVm: PrayerTrackerViewModel = hiltViewModel()
    val tanyaSaatVm: TanyaSaatViewModel = hiltViewModel()
    val todayState by todayVm.state.collectAsStateWithLifecycle()
    val prayerState by prayerVm.state.collectAsStateWithLifecycle()
    val trackerState by trackerVm.state.collectAsStateWithLifecycle()
    val tanyaSaatState by tanyaSaatVm.state.collectAsStateWithLifecycle()
    val audioState by audioPlayer.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPullRefreshing by remember { mutableStateOf(false) }
    val listBottomPadding = floatingNavAndAudioBottomPadding(audioState.currentUrl != null)

    LaunchedEffect(tanyaSaatState.isSheetVisible) {
        onTanyaSaatOpenChanged(tanyaSaatState.isSheetVisible)
    }

    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        trackerVm.refresh()
        todayVm.loadContinueReading()
        onPauseOrDispose {}
    }

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    var locationPrompted by rememberSaveable(key = "location_prompt_v2") { mutableStateOf(false) }
    var notificationsPrompted by rememberSaveable(key = "notifications_prompt_v2") { mutableStateOf(false) }
    var exactAlarmPrompted by rememberSaveable(key = "exact_alarm_prompt_v2") { mutableStateOf(false) }
    var batteryOptPrompted by rememberSaveable(key = "battery_opt_prompt_v1") { mutableStateOf(false) }

    val notificationsDisabledMsg = stringResource(R.string.notifications_disabled_snackbar)
    val settingsLabel = stringResource(R.string.action_settings)
    val exactAlarmMsg = stringResource(R.string.exact_alarm_rationale)
    val allowLabel = stringResource(R.string.action_allow)
    val locationEnable = stringResource(R.string.location_enable)
    val locating = stringResource(R.string.locating)
    val locationUnavailable = stringResource(R.string.location_unavailable)
    val shareReflectionLabel = stringResource(R.string.share_reflection)
    val profileStillLoading = stringResource(R.string.profile_still_loading)
    val verseOfDayTitle = stringResource(R.string.verse_of_day)
    val onboardingStore = remember { OnboardingStore.from(context) }
    val onboardingComplete = remember { onboardingStore.isComplete() }
    val permissionsHandledInOnboarding = remember { onboardingStore.permissionsHandledInOnboarding() }
    val hasManualLocation = remember {
        LocationPreferencesStore.from(context).mode() == LocationMode.MANUAL
    }
    
    val coachMarkState = rememberCoachMarkState()
    LaunchedEffect(Unit) {
        if (!onboardingStore.hasShownHomeCoachMark()) {
            kotlinx.coroutines.delay(1000)
            coachMarkState.show()
            onboardingStore.markHomeCoachMarkShown()
        }
    }

    suspend fun showNotificationSettingsSnackbar() {
        val result = snackbarHostState.showSnackbar(
            message = notificationsDisabledMsg,
            actionLabel = settingsLabel,
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            context.openAppNotificationSettings()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsPrompted = true
        if (!granted && !context.areAppNotificationsEnabled()) {
            scope.launch { showNotificationSettingsSnackbar() }
        }
    }

    fun requestNotificationsIfNeeded() {
        if (permissionsHandledInOnboarding) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (!context.areAppNotificationsEnabled()) {
                scope.launch { showNotificationSettingsSnackbar() }
            }
            return
        }
        if (context.areAppNotificationsEnabled()) return
        if (notificationsPrompted) return
        notificationsPrompted = true
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            prayerVm.onPermissionGranted()
        }
        requestNotificationsIfNeeded()
    }

    // After onboarding: still request location if needed, but skip duplicate notification prompts.
    LaunchedEffect(onboardingComplete, permissionsHandledInOnboarding) {
        if (!onboardingComplete || locationPrompted) return@LaunchedEffect
        locationPrompted = true
        if (hasManualLocation) {
            scope.launch { prayerVm.refresh(force = true) }
            requestNotificationsIfNeeded()
            return@LaunchedEffect
        }
        if (locationPermissions.allPermissionsGranted) {
            prayerVm.onPermissionGranted()
            requestNotificationsIfNeeded()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(prayerState.timings.isNotEmpty(), exactAlarmPrompted) {
        if (exactAlarmPrompted || prayerState.timings.isEmpty()) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect
        if (context.canScheduleExactAlarms()) return@LaunchedEffect
        exactAlarmPrompted = true
        val result = snackbarHostState.showSnackbar(
            message = exactAlarmMsg,
            actionLabel = allowLabel,
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            context.openExactAlarmSettings()
        }
    }

    // Battery / autostart — only on OEMs that kill background apps (Samsung, Xiaomi, Oppo, …).
    LaunchedEffect(prayerState.timings.isNotEmpty(), batteryOptPrompted) {
        if (batteryOptPrompted || prayerState.timings.isEmpty()) return@LaunchedEffect
        if (context.isIgnoringBatteryOptimizations()) return@LaunchedEffect
        batteryOptPrompted = true
        val message = context.getString(R.string.battery_opt_snackbar_message)
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = allowLabel,
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            context.openBackgroundReliabilitySettings()
        }
    }

    LaunchedEffect(prayerState.activePrayer, prayerState.nextPrayer) {
        trackerVm.refresh()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                trackerVm.refresh()
                todayVm.loadContinueReading()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        trackerVm.toastMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }



    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }

    val targetPrayer = prayerState.activePrayer ?: prayerState.nextPrayer
    val cardDrawable = getPrayerCardDrawable(targetPrayer)

    val view = androidx.compose.ui.platform.LocalView.current
    val isHeaderLightBackground = isScrolled || cardDrawable == R.drawable.day

    androidx.compose.runtime.SideEffect {
        val window = (view.context as? android.app.Activity)?.window
        if (window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = isHeaderLightBackground
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SaatColors.HomeBg)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(393f / 238f)
                .graphicsLayer {
                    val progress = if (listState.firstVisibleItemIndex > 0) 1f
                    else (listState.firstVisibleItemScrollOffset / 160f).coerceIn(0f, 1f)
                    val offset = if (listState.firstVisibleItemIndex == 0) {
                        listState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        1000f
                    }
                    alpha = (1f - progress).coerceIn(0f, 1f)
                    translationY = -offset * 0.45f
                }
        ) {
            AsyncImage(
                model = cardDrawable,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle bottom smooth gradient fade into app background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Transparent,
                                SaatColors.HomeBg.copy(alpha = 0.30f),
                                SaatColors.HomeBg
                            )
                        )
                    )
            )
        }

        SaatPullToRefresh(
            isRefreshing = isPullRefreshing,
            onRefresh = {
                scope.launch {
                    isPullRefreshing = true
                    val startTime = System.currentTimeMillis()
                    runCatching {
                        coroutineScope {
                            launch { prayerVm.refresh(force = true) }
                            launch { todayVm.refreshContent(refreshTranslation = true) }
                            launch { trackerVm.refresh() }
                        }
                    }
                    val elapsed = System.currentTimeMillis() - startTime
                    val remainingDelay = 700L - elapsed
                    if (remainingDelay > 0) {
                        kotlinx.coroutines.delay(remainingDelay)
                    }
                    isPullRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(top = 0.dp, bottom = listBottomPadding + 12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
            ) {
                stickyHeader(key = "today_header") {
                    TodayHeader(
                        cityName = prayerState.cityName,
                        locationStatus = when {
                            prayerState.cityName != null -> null
                            prayerState.needsPermission -> locationEnable
                            prayerState.error?.kind == app.kamy.saatApp.core.error.AppErrorKind.Location -> locationUnavailable
                            prayerState.isLoading -> locating
                            else -> locating
                        },
                        hijriLabel = prayerState.hijriLabel,
                        gregorianLabel = prayerState.gregorianLabel,
                        isScrolled = isScrolled,
                        isDarkBackground = cardDrawable != R.drawable.day,
                        onLocationClick = prayerVm::openLocationSheet,
                        onCalendarClick = onOpenPrayerCalendar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .coachMarkTarget(
                                coachMarkState,
                                0,
                                R.string.coach_mark_today_location_title,
                                R.string.coach_mark_today_location_desc
                            )
                    )
                }

                item(key = "top_header_spacer") {
                    Spacer(modifier = Modifier.height(100.dp))
                }
                item(key = "prayer_card") {
                    PrayerDashboardCard(
                        state = prayerState,
                        onRetry = { scope.launch { prayerVm.refresh(force = true) } },
                        onOpenCalendar = onOpenPrayerCalendar,
                        onLocationClick = prayerVm::openLocationSheet,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .coachMarkTarget(
                                coachMarkState,
                                2,
                                R.string.coach_mark_today_prayer_title,
                                R.string.coach_mark_today_prayer_desc
                            )
                    )
                }

                todayState.continueReading?.let { session ->
                    item(key = "continue_reading") {
                        TodayContinueReadingCard(
                            session = session,
                            chapterName = todayState.continueReadingChapterName,
                            totalVerses = todayState.continueReadingTotalVerses,
                            onTap = {
                                onOpenChapterReader(session.chapterNumber, session.verseNumber)
                            },
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .coachMarkTarget(
                                    coachMarkState,
                                    3,
                                    R.string.coach_mark_today_continue_title,
                                    R.string.coach_mark_today_continue_desc
                                )
                        )
                    }
                }

                item(key = "prayer_tracker") {
                    val isAfterIsha = remember(prayerState.activePrayer, prayerState.nextPrayer) {
                        prayerState.activePrayer == app.kamy.saatApp.domain.model.PrayerType.ISHA
                    }
                    PrayerTrackerCard(
                        state = trackerState,
                        verse = todayState.verse,
                        referenceLabel = todayState.verseReferenceLabel,
                        dailyQuote = todayState.dailyQuote,
                        isAfterIsha = isAfterIsha,
                        onShowToastMessage = { msg ->
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(msg)
                            }
                        },
                        onToggleDailyPrayer = trackerVm::toggleDailyPrayer,
                        onToggleOptional = trackerVm::toggleOptionalHabit,
                        onOpenCalendar = onOpenTrackerCalendar,
                        onShareVerse = { todayVm.openAiShare() },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .coachMarkTarget(
                                coachMarkState,
                                4,
                                R.string.coach_mark_today_tracker_title,
                                R.string.coach_mark_today_tracker_desc
                            )
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = floatingNavBottomPadding() + 12.dp, start = 20.dp, end = 20.dp)
        ) { snackbarData ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SaatColors.HomeDarkGreen,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, SaatColors.ArcGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .background(SaatColors.HomeDarkGreen)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE6F4EA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Notification",
                            tint = SaatColors.HomeDarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    snackbarData.visuals.actionLabel?.let { action ->
                        TextButton(
                            onClick = { snackbarData.performAction() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = action,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (tanyaSaatState.isSheetVisible) {
        app.kamy.saatApp.ui.components.TanyaSaatFullScreen(
            state = tanyaSaatState,
            onDismiss = tanyaSaatVm::closeSheet,
            onMoodSelected = tanyaSaatVm::onMoodSelected,
            onInputChanged = tanyaSaatVm::onInputTextChanged,
            onSendMessage = tanyaSaatVm::sendMessage,
            onOpenVerseInReader = { chapter, verse ->
                tanyaSaatVm.closeSheet()
                onOpenChapterReader(chapter, verse)
            },
            onBookmarkVerse = { verseData ->
                tanyaSaatVm.bookmarkVerse(context, verseData)
            },
            onClearToast = tanyaSaatVm::clearToast
        )
    }

    PrayerLocationSheet(
        visible = prayerState.showLocationSheet,
        query = prayerState.locationQuery,
        saving = prayerState.locationSaving,
        error = prayerState.locationSaveError,
        onQueryChange = prayerVm::updateLocationQuery,
        onSave = prayerVm::saveManualLocation,
        onUseGps = prayerVm::useCurrentLocation,
        onDismiss = prayerVm::dismissLocationSheet
    )

    TodayReciterSheet(
        visible = todayState.showReciterSheet,
        recitations = todayState.recitations,
        selectedRecitationId = todayState.selectedRecitationId,
        onDismiss = todayVm::dismissReciterSheet,
        onSelectRecitation = todayVm::selectRecitation
    )

    TafsirSheet(
        isVisible = todayState.showTafsir,
        isLoading = todayState.tafsirLoading,
        tafsir = todayState.tafsir,
        verseReference = todayState.verseReferenceLabel.orEmpty(),
        selectedSource = todayState.selectedTafsirSource,
        error = todayState.tafsirError,
        onDismiss = { todayVm.dismissTafsir() },
        onReload = { todayVm.reloadTafsir() },
        onSelectSource = todayVm::selectTafsirSource
    )

    AiShareSheet(
        visible = todayState.aiShareVisible,
        loading = todayState.aiShareLoading,
        draft = todayState.aiShareDraft,
        error = todayState.aiShareError,
        onDismiss = { todayVm.dismissAiShare() },
        onDraftChange = todayVm::updateAiShareDraft,
        onRegenerate = todayVm::regenerateAiShare,
        onShare = { draft ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, draft)
            }
            context.startActivity(Intent.createChooser(intent, shareReflectionLabel))
        }
    )
    CoachMarkOverlay(state = coachMarkState, onDismiss = { coachMarkState.skip() })
}

@Composable
private fun TodayContinueReadingCard(
    session: ReadingSession,
    chapterName: String?,
    totalVerses: Int?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentInt = remember(session.verseNumber, totalVerses) {
        if (totalVerses != null && totalVerses > 0) {
            ((session.verseNumber.toFloat() / totalVerses.toFloat()) * 100).toInt().coerceIn(1, 100)
        } else 50
    }
    val fillFraction = (percentInt / 100f).coerceIn(0.05f, 1f)

    Surface(
        onClick = onTap,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = SaatColors.LastReadBg,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.today_continue_reading_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaatColors.HomeDarkGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                val surahTitle = chapterName ?: stringResource(R.string.surah_number, session.chapterNumber)
                val verseTitle = stringResource(R.string.today_continue_reading_verse, session.verseNumber)
                Text(
                    text = "$surahTitle • $verseTitle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SaatColors.HomeDarkGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$percentInt%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.HomeDarkGreen
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(SaatColors.TimelineGreen)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fillFraction)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(SaatColors.HomeDarkGreen)
                        )
                    }
                }
            }

            AsyncImage(
                model = R.drawable.last_read,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(90.dp, 60.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}
