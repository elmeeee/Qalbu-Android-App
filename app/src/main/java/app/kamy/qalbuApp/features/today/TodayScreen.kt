package app.kamy.qalbuApp.features.today

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import app.kamy.qalbuApp.design.components.AlKhatibPullToRefresh
import app.kamy.qalbuApp.ui.permissions.areAppNotificationsEnabled
import app.kamy.qalbuApp.ui.permissions.canScheduleExactAlarms
import app.kamy.qalbuApp.ui.permissions.hasAggressiveOemBatteryManagement
import app.kamy.qalbuApp.ui.permissions.isIgnoringBatteryOptimizations
import app.kamy.qalbuApp.ui.permissions.openAppNotificationSettings
import app.kamy.qalbuApp.ui.permissions.openBackgroundReliabilitySettings
import app.kamy.qalbuApp.ui.permissions.openExactAlarmSettings
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.features.today.components.PrayerDashboardCard
import app.kamy.qalbuApp.features.today.components.TafsirSheet
import app.kamy.qalbuApp.features.today.components.TodayHeader
import app.kamy.qalbuApp.features.today.components.TodayPrayerMascotSection
import app.kamy.qalbuApp.features.today.components.TodayVerseOfDaySection
import app.kamy.qalbuApp.features.share.AiShareSheet
import app.kamy.qalbuApp.infrastructure.audio.AudioPlayerController
import app.kamy.qalbuApp.ui.layout.floatingNavAndAudioBottomPadding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    audioPlayer: AudioPlayerController,
    onAccountNavigate: () -> Unit = {}
) {
    val todayVm: TodayViewModel = hiltViewModel()
    val prayerVm: PrayerDashboardViewModel = hiltViewModel()
    val todayState by todayVm.state.collectAsState()
    val prayerState by prayerVm.state.collectAsState()
    val audioState by audioPlayer.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPullRefreshing by remember { mutableStateOf(false) }
    val listBottomPadding = floatingNavAndAudioBottomPadding(audioState.currentUrl != null)

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

    suspend fun showNotificationSettingsSnackbar() {
        val result = snackbarHostState.showSnackbar(
            message = "Notifications are off. Enable them in Settings for prayer reminders.",
            actionLabel = "Settings",
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

    // First open: location dialog, then notification dialog (never at the same time).
    LaunchedEffect(Unit) {
        if (locationPrompted) return@LaunchedEffect
        locationPrompted = true
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

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (!locationPrompted) return@LaunchedEffect
        if (locationPermissions.allPermissionsGranted) {
            prayerVm.onPermissionGranted()
        }
    }

    // Exact alarms are required for adhan to fire at the right minute (Android 12+).
    LaunchedEffect(prayerState.timings.isNotEmpty(), exactAlarmPrompted) {
        if (exactAlarmPrompted || prayerState.timings.isEmpty()) return@LaunchedEffect
        if (context.canScheduleExactAlarms()) return@LaunchedEffect
        exactAlarmPrompted = true
        val result = snackbarHostState.showSnackbar(
            message = "Allow exact alarms so adhan and prayer reminders arrive on time.",
            actionLabel = "Allow",
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            context.openExactAlarmSettings()
        }
    }

    // Battery / autostart whitelist (especially Samsung, Xiaomi, Oppo).
    LaunchedEffect(prayerState.timings.isNotEmpty(), exactAlarmPrompted, batteryOptPrompted) {
        if (batteryOptPrompted || prayerState.timings.isEmpty()) return@LaunchedEffect
        if (!exactAlarmPrompted && !context.canScheduleExactAlarms()) return@LaunchedEffect
        if (context.isIgnoringBatteryOptimizations() && !hasAggressiveOemBatteryManagement()) {
            return@LaunchedEffect
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return@LaunchedEffect
        batteryOptPrompted = true
        val message = if (hasAggressiveOemBatteryManagement()) {
            context.getString(
                R.string.battery_opt_snackbar_oem_message,
                Build.MANUFACTURER.replaceFirstChar { it.titlecase() }
            )
        } else {
            context.getString(R.string.battery_opt_snackbar_message)
        }
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Allow",
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            context.openBackgroundReliabilitySettings()
        }
    }

    LaunchedEffect(todayState.publishToast) {
        todayState.publishToast?.let {
            snackbarHostState.showSnackbar(it)
            todayVm.clearPublishToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AlKhatibPullToRefresh(
            isRefreshing = isPullRefreshing,
            onRefresh = {
                scope.launch {
                    isPullRefreshing = true
                    runCatching {
                        coroutineScope {
                            launch { prayerVm.refresh() }
                            launch { todayVm.refreshContent() }
                        }
                    }
                    isPullRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TodayHeader(
                    cityName = prayerState.cityName,
                    locationStatus = when {
                        prayerState.cityName != null -> null
                        prayerState.needsPermission -> "Enable location"
                        prayerState.isLoading -> "Locating…"
                        prayerState.error != null -> "Location unavailable"
                        else -> "Locating…"
                    },
                    hijriLabel = prayerState.hijriLabel,
                    gregorianLabel = prayerState.gregorianLabel,
                    avatarUrl = todayState.profile?.preferredAvatarUrl,
                    isProfileLoading = todayState.profileLoading,
                    onAccountClick = onAccountNavigate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = listBottomPadding)
                ) {
                    item(key = "prayer_card") {
                        Spacer(Modifier.height(8.dp))
                        TodayPrayerMascotSection(
                            state = prayerState,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item(key = "quran_of_day") {
                        TodayVerseOfDaySection(
                            verse = todayState.verse,
                            referenceLabel = todayState.verseReferenceLabel,
                            isLoading = todayState.isLoading,
                            isPlaying = audioPlayer.isPlayingUrl(todayState.verse?.audio?.url),
                            onPlayAudio = {
                                val url = todayState.verse?.audio?.url ?: return@TodayVerseOfDaySection
                                if (audioPlayer.isPlayingUrl(url)) {
                                    audioPlayer.toggle()
                                } else {
                                    val surahTitle = todayState.verseReferenceLabel
                                        ?.substringBefore(" - ")
                                        ?.trim()
                                        .orEmpty()
                                        .ifBlank { "Quran of the Day" }
                                    audioPlayer.playVerse(
                                        url = url,
                                        surahTitle = surahTitle,
                                        ayahLabel = todayState.verse?.verseKey.orEmpty(),
                                        reciterName = todayState.recitations
                                            .firstOrNull { it.identifiableId == todayState.selectedRecitationId }
                                            ?.displayName.orEmpty()
                                    )
                                }
                            },
                            aiShareLoading = todayState.aiShareLoading,
                            onAiShare = { todayVm.openAiShare() },
                            onTafsir = { todayVm.openTafsir() }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    TafsirSheet(
        isVisible = todayState.showTafsir,
        isLoading = todayState.tafsirLoading,
        tafsir = todayState.tafsir,
        verseReference = todayState.verseReferenceLabel.orEmpty(),
        error = todayState.tafsirError,
        onDismiss = { todayVm.dismissTafsir() },
        onReload = { todayVm.reloadTafsir() }
    )

    AiShareSheet(
        visible = todayState.aiShareVisible,
        loading = todayState.aiShareLoading,
        draft = todayState.aiShareDraft,
        error = todayState.aiShareError,
        isPublishing = todayState.isPublishing,
        showPublish = todayVm.isSignedIn(),
        onDismiss = { todayVm.dismissAiShare() },
        onDraftChange = todayVm::updateAiShareDraft,
        onRegenerate = todayVm::regenerateAiShare,
        onShare = { draft ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, draft)
            }
            context.startActivity(Intent.createChooser(intent, "Share reflection"))
        },
        onPublish = {
            val authorId = todayState.profile?.id
            if (authorId.isNullOrBlank()) {
                scope.launch {
                    snackbarHostState.showSnackbar("Profile still loading — try again in a moment.")
                }
            } else {
                todayVm.publishReflectionToReflect(authorId)
            }
        }
    )
}
