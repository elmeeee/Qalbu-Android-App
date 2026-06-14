package app.kamy.qalbuApp.features.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.components.AlKhatibSettingsNavigationRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import app.kamy.qalbuApp.infrastructure.preferences.OfflineDownloadStore

@Composable
fun OfflineQuranDownloadRow() {
    val context = LocalContext.current
    var sheetVisible by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(OfflineDownloadStore.downloadedChapterCount(context)) }
    var downloading by remember { mutableStateOf(OfflineDownloadStore.isInProgress(context)) }

    LaunchedEffect(Unit) {
        OfflineDownloadStore.recoverStaleProgress(context)
        downloading = OfflineDownloadStore.isInProgress(context)
        progress = OfflineDownloadStore.downloadedChapterCount(context)
    }

    val complete = OfflineDownloadStore.isFullyDownloaded(context)
    val subtitle = when {
        complete -> stringResource(R.string.offline_quran_complete)
        downloading -> stringResource(R.string.offline_quran_downloading, progress, 114)
        progress > 0 -> stringResource(R.string.offline_quran_partial, progress, 114)
        else -> stringResource(R.string.offline_quran_subtitle)
    }

    AlKhatibSettingsNavigationRow(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = stringResource(R.string.offline_quran_title),
        subtitle = subtitle,
        onClick = { sheetVisible = true }
    )

    if (sheetVisible) {
        OfflineQuranDownloadSheet(
            onDismiss = {
                sheetVisible = false
                downloading = OfflineDownloadStore.isInProgress(context)
                progress = OfflineDownloadStore.downloadedChapterCount(context)
            }
        )
    }
}
