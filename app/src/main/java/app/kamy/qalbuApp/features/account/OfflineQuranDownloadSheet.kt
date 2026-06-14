package app.kamy.qalbuApp.features.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.infrastructure.offline.OfflineDownloadProgress
import app.kamy.qalbuApp.infrastructure.preferences.OfflineDownloadStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class OfflineDownloadMode { ALL, PICK }

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OfflineDownloadEntryPoint {
    fun offlineQuranDownloader(): app.kamy.qalbuApp.infrastructure.offline.OfflineQuranDownloader
    fun contentRepository(): ContentRepository
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineQuranDownloadSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, OfflineDownloadEntryPoint::class.java)
    }
    val downloader = remember { entryPoint.offlineQuranDownloader() }
    val contentRepository = remember { entryPoint.contentRepository() }

    var chapters by remember { mutableStateOf<List<QuranChapter>>(emptyList()) }
    var loadingChapters by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(OfflineDownloadMode.PICK) }
    var includeTranslations by remember { mutableStateOf(true) }
    val selectedChapters = remember { mutableStateListOf<Int>() }
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<OfflineDownloadProgress?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var overallDownloaded by remember { mutableIntStateOf(OfflineDownloadStore.downloadedChapterCount(context)) }

    LaunchedEffect(Unit) {
        OfflineDownloadStore.recoverStaleProgress(context)
        downloading = OfflineDownloadStore.isInProgress(context)
        overallDownloaded = OfflineDownloadStore.downloadedChapterCount(context)
        loadingChapters = true
        chapters = runCatching {
            withContext(Dispatchers.IO) { contentRepository.getChapters(force = false) }
        }.getOrDefault(emptyList()).sortedBy { it.id }
        loadingChapters = false
        if (selectedChapters.isEmpty()) {
            selectedChapters.addAll(chapters.map { it.id })
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.offline_quran_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.offline_quran_picker_subtitle, overallDownloaded),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { mode = OfflineDownloadMode.ALL },
                    modifier = Modifier.weight(1f),
                    enabled = !downloading
                ) {
                    Text(stringResource(R.string.offline_quran_mode_all))
                }
                OutlinedButton(
                    onClick = { mode = OfflineDownloadMode.PICK },
                    modifier = Modifier.weight(1f),
                    enabled = !downloading
                ) {
                    Text(stringResource(R.string.offline_quran_mode_pick))
                }
            }

            if (mode == OfflineDownloadMode.PICK) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            selectedChapters.clear()
                            selectedChapters.addAll(chapters.map { it.id })
                        },
                        enabled = !downloading
                    ) { Text(stringResource(R.string.offline_quran_select_all)) }
                    TextButton(
                        onClick = { selectedChapters.clear() },
                        enabled = !downloading
                    ) { Text(stringResource(R.string.offline_quran_clear_all)) }
                }
                if (loadingChapters) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        items(chapters, key = { it.id }) { chapter ->
                            val checked = chapter.id in selectedChapters
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) selectedChapters.add(chapter.id)
                                        else selectedChapters.remove(chapter.id)
                                    },
                                    enabled = !downloading
                                )
                                Text(
                                    text = "${chapter.id}. ${chapter.nameSimple ?: chapter.displayComplexName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.offline_quran_include_translations))
                Switch(
                    checked = includeTranslations,
                    onCheckedChange = { includeTranslations = it },
                    enabled = !downloading
                )
            }

            progress?.let { p ->
                if (p.totalInBatch > 0) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { p.completedInBatch.toFloat() / p.totalInBatch.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.offline_quran_downloading_detail,
                            p.completedInBatch,
                            p.totalInBatch,
                            p.overallDownloaded
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                }
            }

            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(text = it, color = AlKhatibColors.Danger, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (downloading) {
                    OutlinedButton(
                        onClick = {
                            downloader.requestCancel()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.back))
                    }
                }
                Button(
                    onClick = {
                        val ids = if (mode == OfflineDownloadMode.ALL) {
                            (1..114).toList()
                        } else {
                            selectedChapters.toList()
                        }
                        if (ids.isEmpty()) {
                            errorMessage = context.getString(R.string.offline_quran_pick_empty)
                            return@Button
                        }
                        errorMessage = null
                        downloading = true
                        scope.launch {
                            val result = downloader.downloadChapters(
                                chapterIds = ids,
                                includeTranslations = includeTranslations
                            ) { p ->
                                progress = p
                                overallDownloaded = p.overallDownloaded
                            }
                            downloading = OfflineDownloadStore.isInProgress(context)
                            overallDownloaded = OfflineDownloadStore.downloadedChapterCount(context)
                            if (result.isFailure) {
                                val msg = result.exceptionOrNull()?.message
                                errorMessage = msg ?: OfflineDownloadStore.lastError(context)
                            } else if (OfflineDownloadStore.isFullyDownloaded(context)) {
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !downloading && (mode == OfflineDownloadMode.ALL || selectedChapters.isNotEmpty())
                ) {
                    Text(
                        if (downloading) stringResource(R.string.offline_quran_downloading_short)
                        else stringResource(R.string.offline_quran_download)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
