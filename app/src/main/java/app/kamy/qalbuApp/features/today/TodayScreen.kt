package app.kamy.qalbuApp.features.today

import android.content.Intent
import android.os.Build
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.features.today.components.PrayerDashboardCard
import app.kamy.qalbuApp.features.today.components.TafsirSheet
import app.kamy.qalbuApp.features.today.components.TodayHeader
import app.kamy.qalbuApp.features.today.components.TodayVerseOfDaySection
import app.kamy.qalbuApp.infrastructure.audio.AudioPlayerController

/**
 * Today tab root screen. Mirrors iOS Features/Discovery/Views/TodayDiscoveryView.swift.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TodayScreen(
    audioPlayer: AudioPlayerController,
    onReflectNavigate: () -> Unit
) {
    val todayVm: TodayViewModel = hiltViewModel()
    val prayerVm: PrayerDashboardViewModel = hiltViewModel()
    val todayState by todayVm.state.collectAsState()
    val prayerState by prayerVm.state.collectAsState()
    val audioState by audioPlayer.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Runtime permissions: location for prayer times, notifications for reminders.
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val notificationsPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    var locationPrompted by remember { mutableStateOf(false) }
    var notificationsPrompted by remember { mutableStateOf(false) }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (!locationPermissions.allPermissionsGranted && !locationPrompted) {
            locationPrompted = true
            locationPermissions.launchMultiplePermissionRequest()
        }
    }
    LaunchedEffect(notificationsPermission.status.isGranted) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationsPermission.status.isGranted &&
            !notificationsPrompted
        ) {
            notificationsPrompted = true
            notificationsPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(todayState.publishToast) {
        todayState.publishToast?.let {
            snackbarHostState.showSnackbar(it)
            todayVm.clearPublishToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AlKhatibColors.PanelGrey,
                            AlKhatibColors.PanelGreyAlt,
                            AlKhatibColors.SageTint
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
        ) {
            TodayHeader(
                cityName = prayerState.cityName,
                hijriLabel = prayerState.hijriLabel,
                gregorianLabel = prayerState.gregorianLabel
            )
            Spacer(Modifier.height(16.dp))
            PrayerDashboardCard(
                state = prayerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            TodayVerseOfDaySection(
                verse = todayState.verse,
                isLoading = todayState.isLoading,
                isPlaying = audioPlayer.isPlayingUrl(todayState.verse?.audio?.url),
                onPlayAudio = {
                    val url = todayState.verse?.audio?.url ?: return@TodayVerseOfDaySection
                    if (audioPlayer.isPlayingUrl(url)) {
                        audioPlayer.toggle()
                    } else {
                        audioPlayer.playVerse(
                            url = url,
                            surahTitle = "Verse of the Day",
                            ayahLabel = todayState.verse?.verseKey.orEmpty(),
                            reciterName = todayState.recitations
                                .firstOrNull { it.id == todayState.selectedRecitationId }
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
                        // TODO: wire authorId from a session profile cache.
                        // For now, prompt user to share via system sheet as fallback.
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
            Spacer(Modifier.height(100.dp))
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
