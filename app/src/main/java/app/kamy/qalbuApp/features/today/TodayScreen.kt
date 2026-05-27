package app.kamy.qalbuApp.features.today

import android.content.Intent
import android.os.Build
import android.Manifest
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import app.kamy.qalbuApp.design.components.AlKhatibPullToRefresh
import app.kamy.qalbuApp.features.today.components.PrayerDashboardCard
import app.kamy.qalbuApp.features.today.components.TafsirSheet
import app.kamy.qalbuApp.features.today.components.TodayHeader
import app.kamy.qalbuApp.features.today.components.TodayPrayerMascotSection
import app.kamy.qalbuApp.features.today.components.TodayVerseOfDaySection
import app.kamy.qalbuApp.infrastructure.audio.AudioPlayerController
import app.kamy.qalbuApp.ui.layout.floatingNavAndAudioBottomPadding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    audioPlayer: AudioPlayerController,
    onReflectNavigate: () -> Unit,
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
    val notificationsPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    // Saveable so tab switches do not re-trigger prompts.
    var locationPrompted by rememberSaveable { mutableStateOf(false) }
    var notificationsPrompted by rememberSaveable { mutableStateOf(false) }

    fun requestNotificationsIfNeeded() {
        if (notificationsPrompted) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (notificationsPermission.status.isGranted) return
        notificationsPrompted = true
        notificationsPermission.launchPermissionRequest()
    }

    // First open: ask location, then notifications (never both dialogs at once).
    LaunchedEffect(Unit) {
        if (locationPrompted) return@LaunchedEffect
        locationPrompted = true
        if (locationPermissions.allPermissionsGranted) {
            prayerVm.onPermissionGranted()
            requestNotificationsIfNeeded()
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (!locationPrompted) return@LaunchedEffect
        if (locationPermissions.allPermissionsGranted) {
            prayerVm.onPermissionGranted()
            requestNotificationsIfNeeded()
        }
    }

    LaunchedEffect(locationPermissions.permissions.map { it.status }) {
        if (!locationPrompted) return@LaunchedEffect
        if (locationPermissions.allPermissionsGranted) return@LaunchedEffect
        val locationAnswered = locationPermissions.permissions.any { permission ->
            permission.status is PermissionStatus.Denied
        }
        if (locationAnswered) {
            requestNotificationsIfNeeded()
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
                            onShare = {
                                val text = todayVm.composeShareText()
                                if (text.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share verse"))
                                }
                            },
                            onReflect = {
                                if (todayVm.isSignedIn()) {
                                    val text = todayVm.composeShareText()
                                    if (text.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share reflection"))
                                    }
                                } else {
                                    onReflectNavigate()
                                }
                            },
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
        onDismiss = { todayVm.dismissTafsir() }
    )
}
