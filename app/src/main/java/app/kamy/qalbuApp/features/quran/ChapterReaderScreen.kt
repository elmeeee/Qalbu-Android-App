package app.kamy.qalbuApp.features.quran

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.features.share.AiShareSheet
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.features.reader.HadithSheet
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.features.today.components.TafsirSheet
import app.kamy.qalbuApp.infrastructure.audio.AudioPlayerController
import app.kamy.qalbuApp.ui.common.TajweedHtmlView
import app.kamy.qalbuApp.ui.common.TajweedTextAlign
import app.kamy.qalbuApp.ui.common.toVerseTranslationPlainText
import app.kamy.qalbuApp.ui.components.FloatingAudioBarMetrics
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderScreen(
    audioPlayer: AudioPlayerController,
    initialVerseNumber: Int? = null,
    audioBarVisible: Boolean = false,
    onBack: () -> Unit
) {
    val vm: ChapterReaderViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val audioState by audioPlayer.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var settingsVisible by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = 0) { state.verses.size }
    val currentVerse by remember {
        derivedStateOf { state.verses.getOrNull(pagerState.currentPage) }
    }
    val surahTitle = state.chapterDisplayName ?: "Surah ${state.chapterNumber}"

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                vm.onPageChanged(page)
                vm.loadMoreIfNeeded(page)
            }
    }

    LaunchedEffect(state.verses.size, initialVerseNumber) {
        if (state.verses.isEmpty() || initialVerseNumber == null) return@LaunchedEffect
        val idx = state.verses.indexOfFirst { it.resolvedVerseNumber == initialVerseNumber }
        if (idx >= 0 && pagerState.currentPage != idx) {
            pagerState.scrollToPage(idx)
        }
    }

    LaunchedEffect(state.publishMessage) {
        state.publishMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.clearPublishMessage()
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is ReaderEvent.AnimateToPage -> {
                    if (event.index in state.verses.indices) {
                        pagerState.animateScrollToPage(event.index)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
    ) {
        if (state.isLoading && state.verses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AlKhatibColors.DeepEmerald
                )
            }
        } else if (state.verses.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { pageIndex ->
                val verse = state.verses.getOrNull(pageIndex) ?: return@VerticalPager
                QalbuAyahPage(
                    verse = verse,
                    fontScale = state.fontScale,
                    showTranslation = state.showTranslation,
                    audioBarVisible = audioBarVisible,
                    onPlay = { vm.onTapAyah(pageIndex) }
                )
            }
        }

        // Top chrome — back + settings (TikTok-style overlay)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AlKhatibColors.ScreenBackground.copy(alpha = 0.95f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AlKhatibColors.Slate900
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { settingsVisible = true }) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Reading settings",
                        tint = AlKhatibColors.Slate900
                    )
                }
            }
        }

        val surahCaptionBottom = if (audioBarVisible) {
            FloatingAudioBarMetrics.barHeight + FloatingAudioBarMetrics.bottomGap + 12.dp
        } else {
            20.dp
        }

        if (state.verses.isNotEmpty()) {
            ReaderFloatingActions(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(
                        end = 8.dp,
                        bottom = if (audioBarVisible) 188.dp else 128.dp
                    ),
                onAiShare = { vm.openAiShare(pagerState.currentPage) },
                onTafsir = { currentVerse?.verseKey?.let(vm::openTafsir) },
                onHadith = { currentVerse?.verseKey?.let(vm::openHadith) }
            )
        }

        currentVerse?.let { verse ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.72f)
                    .navigationBarsPadding()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                AlKhatibColors.ScreenBackground.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .padding(
                        start = 20.dp,
                        end = 12.dp,
                        bottom = surahCaptionBottom,
                        top = 48.dp
                    )
            ) {
                Text(
                    text = surahTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                val ayahNo = verse.resolvedVerseNumber
                val juzNo = verse.juzNumber
                Text(
                    text = buildString {
                        append("Ayah ")
                        append(ayahNo?.toString() ?: "-")
                        if (juzNo != null) append(" · Juz ").append(juzNo)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = AlKhatibColors.Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (audioBarVisible) 100.dp else 24.dp)
        )
    }

    if (settingsVisible) {
        ReaderSettingsSheet(
            state = state,
            onDismiss = { settingsVisible = false },
            onFontScaleChange = vm::setFontScale,
            onToggleTranslation = vm::toggleTranslation,
            onSelectRecitation = vm::selectRecitation,
            onSetPlaybackMode = vm::setPlaybackMode
        )
    }

    val activeVerseReference = remember(state.activeAyahKey, state.verses, state.chapterDisplayName) {
        state.activeAyahKey?.let { key ->
            state.verses.find { it.verseKey == key }?.referenceLabel(state.chapterDisplayName)
        }.orEmpty().ifEmpty { state.activeAyahKey.orEmpty() }
    }

    TafsirSheet(
        isVisible = state.tafsirVisible,
        isLoading = state.tafsirLoading,
        tafsir = state.tafsir,
        verseReference = activeVerseReference,
        error = state.tafsirError,
        onDismiss = { vm.dismissTafsir() },
        onReload = { vm.reloadTafsir() }
    )

    HadithSheet(
        isVisible = state.hadithVisible,
        isLoading = state.hadithLoading,
        isLoadingMore = state.hadithLoadingMore,
        hasMore = state.hadithHasMore,
        hadiths = state.hadiths,
        verseReference = activeVerseReference,
        error = state.hadithError,
        onDismiss = { vm.dismissHadith() },
        onReload = { vm.reloadHadith() },
        onLoadMore = { vm.loadMoreHadith() }
    )

    AiShareSheet(
        visible = state.aiShareVisible,
        loading = state.aiShareLoading,
        draft = state.aiShareDraft,
        error = state.aiShareError,
        isPublishing = state.isPublishing,
        showPublish = vm.isSignedIn(),
        onDismiss = { vm.dismissAiShare() },
        onDraftChange = vm::updateAiShareDraft,
        onRegenerate = vm::regenerateAiShare,
        onShare = { draft ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, draft)
            }
            context.startActivity(Intent.createChooser(intent, "Share reflection"))
        },
        onPublish = { vm.publishAiReflection() }
    )
}

@Composable
private fun QalbuAyahPage(
    verse: RandomAyahPayload,
    fontScale: Float,
    showTranslation: Boolean,
    audioBarVisible: Boolean,
    onPlay: () -> Unit
) {
    val contentTopPadding = 56.dp
    val contentBottomPadding = if (audioBarVisible) 200.dp else 152.dp
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(verse.listIdentity) {
                detectTapGestures(onTap = { onPlay() })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = contentTopPadding,
                    bottom = contentBottomPadding
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TajweedHtmlView(
                textUthmaniTajweed = verse.textUthmaniTajweed ?: verse.textUthmani,
                ayahNumber = verse.resolvedVerseNumber,
                fontSizeSp = (30 * fontScale).toInt(),
                compact = true,
                textAlign = TajweedTextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
            if (showTranslation) {
                verse.translations?.firstOrNull()?.text?.let { translation ->
                    val clean = translation.toVerseTranslationPlainText()
                    if (clean.isNotEmpty()) {
                        Text(
                            text = clean,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.35f
                            ),
                            color = AlKhatibColors.Slate800,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderFloatingActions(
    modifier: Modifier = Modifier,
    onAiShare: () -> Unit,
    onTafsir: () -> Unit,
    onHadith: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.94f),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ReaderSideAction(
                icon = {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = "AI reflection",
                        tint = AlKhatibColors.Gold,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "AI",
                onClick = onAiShare
            )
            ReaderSideAction(
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Tafsir",
                        tint = AlKhatibColors.IndigoAccent,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Tafsir",
                onClick = onTafsir
            )
            ReaderSideAction(
                icon = {
                    Icon(
                        Icons.Filled.Forum,
                        contentDescription = "Hadith",
                        tint = AlKhatibColors.Gold,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Hadith",
                onClick = onHadith
            )
        }
    }
}

@Composable
private fun ReaderSideAction(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    iconBackground: Color = AlKhatibColors.LightGrey
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = AlKhatibColors.Slate500,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(
    state: ChapterReaderUiState,
    onDismiss: () -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onToggleTranslation: (Boolean) -> Unit,
    onSelectRecitation: (Int) -> Unit,
    onSetPlaybackMode: (AyahPlaybackMode) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Reading settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Text("Text size", style = MaterialTheme.typography.labelLarge, color = AlKhatibColors.Slate500)
            Slider(
                value = state.fontScale,
                onValueChange = onFontScaleChange,
                valueRange = 0.85f..1.35f,
                steps = 9
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show translation", modifier = Modifier.weight(1f), color = AlKhatibColors.Slate900)
                Switch(checked = state.showTranslation, onCheckedChange = onToggleTranslation)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Continuous play",
                    modifier = Modifier.weight(1f),
                    color = AlKhatibColors.Slate900
                )
                Switch(
                    checked = state.playbackMode == AyahPlaybackMode.CONTINUOUS,
                    onCheckedChange = { enabled ->
                        onSetPlaybackMode(
                            if (enabled) AyahPlaybackMode.CONTINUOUS else AyahPlaybackMode.SINGLE
                        )
                    }
                )
            }
            Text("Reciter", style = MaterialTheme.typography.labelLarge, color = AlKhatibColors.Slate500)
            if (state.recitations.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = AlKhatibColors.DeepEmerald
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(state.recitations.size, key = { state.recitations[it].identifiableId }) { index ->
                        val recitation = state.recitations[index]
                        ReciterRow(
                            recitation = recitation,
                            selected = recitation.identifiableId == state.selectedRecitationId,
                            onClick = { onSelectRecitation(recitation.identifiableId) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ReciterRow(
    recitation: RecitationPayload,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .background(
                    if (selected) AlKhatibColors.DeepEmerald.copy(alpha = 0.1f)
                    else Color.Transparent
                )
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = recitation.displayName,
                modifier = Modifier.weight(1f),
                color = AlKhatibColors.Slate900,
                style = MaterialTheme.typography.bodyLarge
            )
            if (selected) {
                Text("✓", color = AlKhatibColors.DeepEmerald, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AlKhatibColors.SoftGrey)
        )
    }
}
