package app.kamy.qalbuApp.features.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.components.AlKhatibSettingsNavigationRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import app.kamy.qalbuApp.infrastructure.offline.OfflineQuranDownloader
import app.kamy.qalbuApp.infrastructure.preferences.OfflineDownloadStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OfflineDownloaderEntryPoint {
    fun offlineQuranDownloader(): OfflineQuranDownloader
}

@Composable
fun OfflineQuranDownloadRow() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var downloading by remember { mutableStateOf(OfflineDownloadStore.isInProgress(context)) }
    var progress by remember { mutableIntStateOf(OfflineDownloadStore.downloadedChapterCount(context)) }
    val complete = remember(progress, downloading) {
        OfflineDownloadStore.isFullyDownloaded(context)
    }
    val subtitle = when {
        complete -> stringResource(R.string.offline_quran_complete)
        downloading -> stringResource(R.string.offline_quran_downloading, progress, 114)
        else -> stringResource(R.string.offline_quran_subtitle)
    }

    AlKhatibSettingsNavigationRow(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = stringResource(R.string.offline_quran_title),
        subtitle = subtitle,
        onClick = {
            if (downloading || complete) return@AlKhatibSettingsNavigationRow
            downloading = true
            val downloader = EntryPointAccessors.fromApplication(
                context.applicationContext,
                OfflineDownloaderEntryPoint::class.java
            ).offlineQuranDownloader()
            scope.launch {
                downloader.downloadAllChapters { downloaded, _ ->
                    progress = downloaded
                }
                downloading = OfflineDownloadStore.isInProgress(context)
                progress = OfflineDownloadStore.downloadedChapterCount(context)
            }
        }
    )
}
