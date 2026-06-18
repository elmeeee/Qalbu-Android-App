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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import app.kamy.qalbuApp.core.config.LocalQuranConfig
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.infrastructure.offline.MurottalDownloadProgress
import app.kamy.qalbuApp.infrastructure.offline.MurottalDownloader
import app.kamy.qalbuApp.infrastructure.offline.MurottalOfflineStore
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
    fun murottalDownloader(): MurottalDownloader
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
    val downloader = remember { entryPoint.murottalDownloader() }
    val contentRepository = remember { entryPoint.contentRepository() }

    var chapters by remember { mutableStateOf<List<QuranChapter>>(emptyList()) }
    var recitations by remember { mutableStateOf<List<RecitationPayload>>(emptyList()) }
    var selectedRecitationId by remember {
        mutableIntStateOf(MurottalOfflineStore.activeReciter(context))
    }
    var loadingChapters by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(OfflineDownloadMode.ALL) }
    val selectedChapters = remember { mutableStateListOf<Int>() }
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<MurottalDownloadProgress?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var overallDownloaded by remember {
        mutableIntStateOf(MurottalOfflineStore.downloadedCount(context, selectedRecitationId))
    }

    LaunchedEffect(Unit) {
        MurottalOfflineStore.recoverStaleProgress(context)
        downloading = MurottalOfflineStore.isInProgress(context)
        loadingChapters = true
        val loaded = runCatching {
            withContext(Dispatchers.IO) {
                contentRepository.getChapters(force = false) to contentRepository.getRecitations()
            }
        }.getOrNull()
        chapters = loaded?.first.orEmpty().sortedBy { it.id }
        recitations = loaded?.second.orEmpty().ifEmpty { LocalQuranConfig.recitations }
        overallDownloaded = MurottalOfflineStore.downloadedCount(context, selectedRecitationId)
        loadingChapters = false
        if (selectedChapters.isEmpty()) {
            selectedChapters.addAll(chapters.map { it.id })
        }
    }

    LaunchedEffect(selectedRecitationId) {
        overallDownloaded = MurottalOfflineStore.downloadedCount(context, selectedRecitationId)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.offline_murottal_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.offline_murottal_subtitle, overallDownloaded),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.offline_murottal_reciter),
                style = MaterialTheme.typography.labelMedium,
                color = AlKhatibColors.Slate500
            )
            Spacer(Modifier.height(6.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                items(recitations, key = { it.identifiableId }) { recitation ->
                    val selected = recitation.identifiableId == selectedRecitationId
                    TextButton(
                        onClick = { selectedRecitationId = recitation.identifiableId },
                        enabled = !downloading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = recitation.displayName,
                            color = if (selected) AlKhatibColors.DeepEmerald else AlKhatibColors.Slate800,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
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
                            .height(180.dp)
                    ) {
                        items(chapters, key = { it.id }) { chapter ->
                            val checked = chapter.id in selectedChapters
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Checkbox(
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
                            R.string.offline_murottal_downloading_detail,
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
                        onClick = { downloader.requestCancel() },
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
                                recitationId = selectedRecitationId,
                                chapterIds = ids
                            ) { p ->
                                progress = p
                                overallDownloaded = p.overallDownloaded
                            }
                            downloading = MurottalOfflineStore.isInProgress(context)
                            overallDownloaded = MurottalOfflineStore.downloadedCount(
                                context,
                                selectedRecitationId
                            )
                            if (result.isFailure) {
                                val msg = result.exceptionOrNull()?.message
                                errorMessage = msg ?: MurottalOfflineStore.lastError(context)
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
