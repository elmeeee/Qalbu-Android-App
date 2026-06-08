package app.kamy.qalbuApp.features.quran

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.stringResource
import app.kamy.qalbuApp.R
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.features.share.AiShareSheet
import app.kamy.qalbuApp.design.components.AlKhatibErrorState
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.ui.common.rememberErrorDisplay
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
    var verseMenuExpanded by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = 0) { state.verses.size }
    val currentVerse by remember {
        derivedStateOf { state.verses.getOrNull(pagerState.currentPage) }
    }
    val surahTitle = state.chapterDisplayName ?: stringResource(R.string.surah_number, state.chapterNumber)
    val loadErrorDisplay = state.error.rememberErrorDisplay(R.string.verses_load_failed)

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

    LaunchedEffect(pagerState.currentPage) {
        verseMenuExpanded = false
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
        } else if (state.error != null && state.verses.isEmpty() && loadErrorDisplay != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                AlKhatibErrorState(
                    display = loadErrorDisplay,
                    onRetry = { vm.loadInitial() }
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
                        contentDescription = stringResource(R.string.back),
                        tint = AlKhatibColors.Slate900
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { settingsVisible = true }) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.reading_settings_a11y),
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
            val readerActionsBottom = FloatingAudioBarMetrics.bottomGap + 14.dp
            ReaderVerseActionsMenu(
                expanded = verseMenuExpanded,
                onToggle = { verseMenuExpanded = !verseMenuExpanded },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 10.dp, bottom = readerActionsBottom),
                onAiShare = {
                    verseMenuExpanded = false
                    vm.openAiShare(pagerState.currentPage)
                },
                onTafsir = {
                    verseMenuExpanded = false
                    currentVerse?.verseKey?.let(vm::openTafsir)
                },
                onHadith = {
                    verseMenuExpanded = false
                    currentVerse?.verseKey?.let(vm::openHadith)
                }
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
                val ayahLabel = ayahNo?.let { stringResource(R.string.ayah_number, it) }
                    ?: stringResource(R.string.ayah_label, "—")
                val juzLabel = juzNo?.let { stringResource(R.string.juz_number, it) }
                Text(
                    text = listOfNotNull(ayahLabel, juzLabel).joinToString(" · "),
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
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_reflection)))
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
    val contentBottomPadding = if (audioBarVisible) 188.dp else 148.dp
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
                textUthmani = verse.textUthmani,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderVerseActionsMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAiShare: () -> Unit,
    onTafsir: () -> Unit,
    onHadith: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReaderCompactMenuItem(
                    icon = {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = AlKhatibColors.Gold,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = stringResource(R.string.ai_label),
                    onClick = onAiShare
                )
                ReaderCompactMenuItem(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = AlKhatibColors.IndigoAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = stringResource(R.string.tafsir),
                    onClick = onTafsir
                )
                ReaderCompactMenuItem(
                    icon = {
                        Icon(
                            Icons.Filled.Forum,
                            contentDescription = null,
                            tint = AlKhatibColors.Gold,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = stringResource(R.string.hadith),
                    onClick = onHadith
                )
            }
        }

        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.96f),
            shadowElevation = 4.dp,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                val closeActionsLabel = stringResource(R.string.close_verse_actions)
                val studyToolsLabel = stringResource(R.string.study_tools)
                Icon(
                    imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.AutoStories,
                    contentDescription = if (expanded) closeActionsLabel else studyToolsLabel,
                    tint = if (expanded) AlKhatibColors.Slate800 else AlKhatibColors.DeepEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderCompactMenuItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.Slate800
            )
        }
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
                stringResource(R.string.reading_settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Text(stringResource(R.string.text_size), style = MaterialTheme.typography.labelLarge, color = AlKhatibColors.Slate500)
            Slider(
                value = state.fontScale,
                onValueChange = onFontScaleChange,
                valueRange = 0.85f..1.35f,
                steps = 9
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.show_translation), modifier = Modifier.weight(1f), color = AlKhatibColors.Slate900)
                Switch(checked = state.showTranslation, onCheckedChange = onToggleTranslation)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.continuous_play),
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
            Text(stringResource(R.string.reciter), style = MaterialTheme.typography.labelLarge, color = AlKhatibColors.Slate500)
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
