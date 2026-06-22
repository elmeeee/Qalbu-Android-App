package app.kamy.saatApp.features.quran

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.AlKhatibPartialBottomSheet
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.features.share.AiShareSheet
import androidx.compose.material.icons.filled.Forum
import app.kamy.saatApp.domain.model.HifzStatus
import app.kamy.saatApp.design.components.AlKhatibErrorState
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.displayTransliteration
import app.kamy.saatApp.domain.model.transliterationUsesHtml
import app.kamy.saatApp.features.reader.HadithSheet
import app.kamy.saatApp.domain.model.RecitationPayload
import app.kamy.saatApp.features.today.components.TafsirSheet
import app.kamy.saatApp.infrastructure.audio.AudioPlayerController
import app.kamy.saatApp.ui.common.TajweedHtmlView
import app.kamy.saatApp.ui.common.TajweedTextAlign
import app.kamy.saatApp.ui.common.TransliterationView
import app.kamy.saatApp.ui.common.toVerseTranslationPlainText
import app.kamy.saatApp.infrastructure.preferences.ReaderOnboardingStore
import app.kamy.saatApp.ui.components.FloatingAudioBarMetrics
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderScreen(
    audioPlayer: AudioPlayerController,
    initialVerseNumber: Int? = null,
    initialVerseKey: String? = null,
    audioBarVisible: Boolean = false,
    onBack: () -> Unit
) {
    val vm: ChapterReaderViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val audioState by audioPlayer.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val onboardingStore = remember { ReaderOnboardingStore.from(context) }
    var showScrollHint by remember { mutableStateOf(!onboardingStore.hasShownScrollHint()) }

    fun dismissScrollHint() {
        if (!showScrollHint) return
        showScrollHint = false
        onboardingStore.markScrollHintShown()
    }
    var settingsVisible by remember { mutableStateOf(false) }
    var verseMenuExpanded by remember { mutableStateOf(false) }

    // Pager requires pageCount > 0; verses may be empty while the first page is loading.
    val verseCount = state.verses.size
    val pagerState = rememberPagerState(initialPage = 0) { verseCount.coerceAtLeast(1) }
    val pageIndex = pagerState.currentPage.coerceIn(0, (verseCount - 1).coerceAtLeast(0))
    val currentVerse = state.verses.getOrNull(pageIndex)
    val surahTitle = when {
        state.juzNumber != null -> {
            val chapterNum = currentVerse?.chapterNumber
            state.chapterLookup[chapterNum]
                ?: chapterNum?.let { stringResource(R.string.surah_number, it) }
                ?: state.chapterDisplayName.orEmpty()
        }
        else -> state.chapterDisplayName ?: stringResource(R.string.surah_number, state.chapterNumber)
    }
    val loadErrorDisplay = state.error.rememberErrorDisplay(R.string.verses_load_failed)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                vm.onPageChanged(page)
                vm.loadMoreIfNeeded(page)
            }
    }

    LaunchedEffect(verseCount) {
        if (verseCount == 0) return@LaunchedEffect
        val lastIndex = verseCount - 1
        if (pagerState.currentPage > lastIndex) {
            pagerState.scrollToPage(lastIndex)
        }
    }

    LaunchedEffect(verseCount, initialVerseNumber, initialVerseKey) {
        if (verseCount == 0) return@LaunchedEffect
        val idx = when {
            !initialVerseKey.isNullOrBlank() ->
                state.verses.indexOfFirst { it.verseKey == initialVerseKey }
            initialVerseNumber != null ->
                state.verses.indexOfFirst { it.resolvedVerseNumber == initialVerseNumber }
            else -> -1
        }
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

    LaunchedEffect(vm, pagerState) {
        vm.events.collect { event ->
            when (event) {
                is ReaderEvent.AnimateToPage -> {
                    val verses = vm.state.value.verses
                    if (event.index in verses.indices) {
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
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp),
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
                SaatAyahPage(
                    verse = verse,
                    fontScale = state.fontScale,
                    showTranslation = state.showTranslation && !state.hifzModeEnabled,
                    showTransliteration = state.showTransliteration && !state.hifzModeEnabled,
                    translationId = state.selectedTranslationId,
                    hifzModeEnabled = state.hifzModeEnabled,
                    audioBarVisible = audioBarVisible,
                    personalDataRevision = state.personalDataRevision,
                    onPlay = { vm.onTapAyah(pageIndex) },
                    onContentScroll = if (pageIndex == 0) ::dismissScrollHint else null
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
                bookmarked = state.currentVerseBookmarked,
                hasNote = state.currentVerseHasNote,
                hifzStatus = state.currentVerseHifzStatus,
                showTafsir = LocalQuranConfig.supportsTafsir(state.selectedTranslationId),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 10.dp, bottom = readerActionsBottom),
                onBookmark = {
                    verseMenuExpanded = false
                    vm.toggleBookmark(pagerState.currentPage.coerceIn(0, state.verses.lastIndex))
                },
                onNote = {
                    verseMenuExpanded = false
                    vm.openNote(pagerState.currentPage.coerceIn(0, state.verses.lastIndex))
                },
                onHifz = {
                    verseMenuExpanded = false
                    vm.cycleHifzStatus(pagerState.currentPage.coerceIn(0, state.verses.lastIndex))
                },
                onAiShare = {
                    verseMenuExpanded = false
                    vm.openAiShare(pagerState.currentPage.coerceIn(0, state.verses.lastIndex))
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
                val subtitle = if (state.juzNumber != null) {
                    val verseLabel = currentVerse?.displayVerseReference?.let { ref ->
                        stringResource(R.string.verse_key_label, ref)
                    } ?: currentVerse?.resolvedVerseNumber?.let { num ->
                        stringResource(R.string.verse_number, num)
                    } ?: stringResource(R.string.verse_label, "—")
                    listOfNotNull(
                        verseLabel,
                        state.juzNumber?.let { stringResource(R.string.juz_number, it) }
                    ).joinToString(" · ")
                } else {
                    currentVerse?.resolvedVerseNumber?.let { num ->
                        stringResource(R.string.verse_number, num)
                    } ?: currentVerse?.displayVerseReference?.let { ref ->
                        stringResource(R.string.verse_key_label, ref)
                    } ?: stringResource(R.string.verse_label, "—")
                }
                Text(
                    text = subtitle,
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

        if (showScrollHint && state.verses.isNotEmpty()) {
            ReaderScrollHint(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = if (audioBarVisible) 132.dp else 96.dp),
                onDismiss = ::dismissScrollHint
            )
        }
    }

    if (settingsVisible) {
        ReaderSettingsSheet(
            state = state,
            onDismiss = { settingsVisible = false },
            onFontScaleChange = vm::setFontScale,
            onToggleTranslation = vm::toggleTranslation,
            onToggleTransliteration = vm::toggleTransliteration,
            onSelectRecitation = vm::selectRecitation,
            onSetPlaybackMode = vm::setPlaybackMode,
            onToggleHifzMode = vm::toggleHifzMode
        )
    }

    if (state.noteVisible) {
        VerseNoteSheet(
            draft = state.noteDraft,
            hasExistingNote = state.currentVerseHasNote,
            onDraftChange = vm::updateNoteDraft,
            onDismiss = vm::dismissNote,
            onSave = vm::saveNote,
            onDelete = vm::deleteNote
        )
    }

    if (state.hifzPickerVisible) {
        HifzPickerSheet(
            currentStatus = state.currentVerseHifzStatus,
            onDismiss = vm::dismissHifzPicker,
            onSelect = vm::setHifzStatus
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
private fun SaatAyahPage(
    verse: RandomAyahPayload,
    fontScale: Float,
    showTranslation: Boolean,
    showTransliteration: Boolean,
    translationId: Int,
    hifzModeEnabled: Boolean,
    audioBarVisible: Boolean,
    personalDataRevision: Int,
    onPlay: () -> Unit,
    onContentScroll: (() -> Unit)? = null
) {
    val contentTopPadding = 56.dp
    val contentBottomPadding = if (audioBarVisible) 188.dp else 148.dp
    val scrollState = rememberScrollState()
    var hifzRevealStage by remember(verse.listIdentity) {
        mutableIntStateOf(0)
    }

    LaunchedEffect(scrollState, onContentScroll) {
        if (onContentScroll == null) return@LaunchedEffect
        snapshotFlow { scrollState.value }
            .collect { offset ->
                if (offset > 12) onContentScroll()
            }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (hifzModeEnabled) {
                    when (hifzRevealStage) {
                        0 -> hifzRevealStage = 1
                        1 -> if (showTranslation) hifzRevealStage = 2 else onPlay()
                        else -> onPlay()
                    }
                } else {
                    onPlay()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    state = scrollState,
                    enabled = scrollState.maxValue > 0
                )
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = contentTopPadding,
                    bottom = contentBottomPadding
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VersePersonalBadges(
                verseKey = verse.verseKey,
                personalDataRevision = personalDataRevision,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            when {
                hifzModeEnabled && hifzRevealStage == 0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(AlKhatibColors.SoftGrey.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.hifz_recall_prompt),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AlKhatibColors.Slate800,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.hifz_tap_to_reveal),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AlKhatibColors.Slate500,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    TajweedHtmlView(
                        textUthmani = verse.textUthmani,
                        ayahNumber = verse.resolvedVerseNumber,
                        fontSizeSp = (30 * fontScale).toInt(),
                        compact = true,
                        textAlign = TajweedTextAlign.Justify,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            val showLatin = showTransliteration && (!hifzModeEnabled || hifzRevealStage >= 1)
            val showMeaning = showTranslation && (!hifzModeEnabled || hifzRevealStage >= 2)
            if (showLatin) {
                verse.displayTransliteration(translationId)?.let { transliteration ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AlKhatibColors.LightGrey.copy(alpha = 0.45f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        TransliterationView(
                            text = transliteration,
                            useHtml = verse.transliterationUsesHtml(translationId),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (showMeaning) {
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
    bookmarked: Boolean,
    hasNote: Boolean,
    hifzStatus: HifzStatus,
    showTafsir: Boolean = true,
    onBookmark: () -> Unit,
    onNote: () -> Unit,
    onHifz: () -> Unit,
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
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                ReaderActionPill(
                    icon = {
                        Icon(
                            if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            tint = AlKhatibColors.GoldDeep,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(
                        if (bookmarked) R.string.remove_bookmark else R.string.bookmark
                    ),
                    accent = AlKhatibColors.GoldDeep,
                    onClick = onBookmark
                )
                ReaderActionPill(
                    icon = {
                        Icon(
                            Icons.Filled.EditNote,
                            contentDescription = null,
                            tint = if (hasNote) AlKhatibColors.DeepEmerald else AlKhatibColors.Slate500,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(
                        if (hasNote) R.string.verse_note_edit else R.string.verse_note
                    ),
                    accent = AlKhatibColors.DeepEmerald,
                    onClick = onNote
                )
                ReaderActionPill(
                    icon = {
                        Icon(
                            Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = hifzStatusColor(hifzStatus),
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = hifzStatusLabel(hifzStatus),
                    accent = hifzStatusColor(hifzStatus),
                    onClick = onHifz
                )
                ReaderActionPill(
                    icon = {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = AlKhatibColors.Gold,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(R.string.ai_label),
                    accent = AlKhatibColors.Gold,
                    onClick = onAiShare
                )
                if (showTafsir) {
                    ReaderActionPill(
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = AlKhatibColors.IndigoAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        },
                        label = stringResource(R.string.tafsir),
                        accent = AlKhatibColors.IndigoAccent,
                        onClick = onTafsir
                    )
                }
                ReaderActionPill(
                    icon = {
                        Icon(
                            Icons.Filled.Forum,
                            contentDescription = null,
                            tint = AlKhatibColors.Teal,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(R.string.hadith),
                    accent = AlKhatibColors.Teal,
                    onClick = onHadith
                )
            }
        }

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.Teal)
                    )
                )
                .clickable(onClick = onToggle)
                .padding(2.dp)
        ) {
            Surface(
                onClick = onToggle,
                shape = CircleShape,
                color = AlKhatibColors.PureWhite,
                shadowElevation = if (expanded) 2.dp else 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val closeActionsLabel = stringResource(R.string.close_verse_actions)
                    val studyToolsLabel = stringResource(R.string.study_tools)
                    Icon(
                        imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.AutoStories,
                        contentDescription = if (expanded) closeActionsLabel else studyToolsLabel,
                        tint = if (expanded) AlKhatibColors.Slate800 else AlKhatibColors.DeepEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderActionPill(
    icon: @Composable () -> Unit,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 6.dp,
        modifier = Modifier
            .widthIn(min = 156.dp)
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.22f),
                shape = RoundedCornerShape(50)
            )
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 14.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.Slate800,
                maxLines = 2
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
    onToggleTransliteration: (Boolean) -> Unit,
    onSelectRecitation: (Int) -> Unit,
    onSetPlaybackMode: (AyahPlaybackMode) -> Unit,
    onToggleHifzMode: (Boolean) -> Unit
) {
    AlKhatibPartialBottomSheet(onDismiss = onDismiss, maxHeightFraction = 0.58f) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AlKhatibColors.DeepEmerald.copy(alpha = 0.14f),
                                AlKhatibColors.Teal.copy(alpha = 0.07f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    stringResource(R.string.reading_settings),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
                Text(
                    stringResource(R.string.font_size_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            ReaderSettingToggleRow(
                title = stringResource(R.string.text_size),
                content = {
                    Slider(
                        value = state.fontScale,
                        onValueChange = onFontScaleChange,
                        valueRange = 0.85f..1.35f,
                        steps = 9,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = AlKhatibColors.Teal,
                            activeTrackColor = AlKhatibColors.DeepEmerald
                        )
                    )
                }
            )
            ReaderSettingToggleRow(
                title = stringResource(R.string.show_translation),
                checked = state.showTranslation,
                onCheckedChange = onToggleTranslation
            )
            ReaderSettingToggleRow(
                title = stringResource(R.string.show_transliteration),
                checked = state.showTransliteration,
                onCheckedChange = onToggleTransliteration
            )
            ReaderSettingToggleRow(
                title = stringResource(R.string.hifz_mode),
                checked = state.hifzModeEnabled,
                onCheckedChange = onToggleHifzMode
            )
            ReaderSettingToggleRow(
                title = stringResource(R.string.continuous_play),
                checked = state.playbackMode == AyahPlaybackMode.CONTINUOUS,
                onCheckedChange = { enabled ->
                    onSetPlaybackMode(
                        if (enabled) AyahPlaybackMode.CONTINUOUS else AyahPlaybackMode.SINGLE
                    )
                }
            )
            Text(
                stringResource(R.string.reciter),
                style = MaterialTheme.typography.labelLarge,
                color = AlKhatibColors.Slate500,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
            if (state.recitations.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = AlKhatibColors.DeepEmerald
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    state.recitations.forEach { recitation ->
                        ReciterRow(
                            recitation = recitation,
                            selected = recitation.identifiableId == state.selectedRecitationId,
                            onClick = { onSelectRecitation(recitation.identifiableId) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReaderSettingToggleRow(
    title: String,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AlKhatibColors.LightGrey.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (content != null) {
            Text(title, color = AlKhatibColors.Slate900, fontWeight = FontWeight.Medium)
            content()
        } else if (checked != null && onCheckedChange != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f), color = AlKhatibColors.Slate900)
                Switch(checked = checked, onCheckedChange = onCheckedChange)
            }
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

@Composable
private fun hifzStatusLabel(status: HifzStatus): String = when (status) {
    HifzStatus.NONE -> stringResource(R.string.hifz_mark)
    HifzStatus.LEARNING -> stringResource(R.string.hifz_learning)
    HifzStatus.MEMORIZED -> stringResource(R.string.hifz_memorized)
    HifzStatus.NEEDS_REVIEW -> stringResource(R.string.hifz_review)
}

private fun hifzStatusColor(status: HifzStatus): Color = when (status) {
    HifzStatus.NONE -> AlKhatibColors.Slate500
    HifzStatus.LEARNING -> AlKhatibColors.IndigoAccent
    HifzStatus.MEMORIZED -> AlKhatibColors.DeepEmerald
    HifzStatus.NEEDS_REVIEW -> AlKhatibColors.GoldDeep
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HifzPickerSheet(
    currentStatus: HifzStatus,
    onDismiss: () -> Unit,
    onSelect: (HifzStatus) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.hifz_picker_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Text(
                text = stringResource(R.string.hifz_picker_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )
            HifzPickerOption(
                title = stringResource(R.string.hifz_learning),
                subtitle = stringResource(R.string.hifz_learning_desc),
                selected = currentStatus == HifzStatus.LEARNING,
                color = AlKhatibColors.IndigoAccent,
                onClick = { onSelect(HifzStatus.LEARNING) }
            )
            HifzPickerOption(
                title = stringResource(R.string.hifz_memorized),
                subtitle = stringResource(R.string.hifz_memorized_desc),
                selected = currentStatus == HifzStatus.MEMORIZED,
                color = AlKhatibColors.DeepEmerald,
                onClick = { onSelect(HifzStatus.MEMORIZED) }
            )
            HifzPickerOption(
                title = stringResource(R.string.hifz_review),
                subtitle = stringResource(R.string.hifz_review_desc),
                selected = currentStatus == HifzStatus.NEEDS_REVIEW,
                color = AlKhatibColors.GoldDeep,
                onClick = { onSelect(HifzStatus.NEEDS_REVIEW) }
            )
            if (currentStatus != HifzStatus.NONE) {
                TextButton(onClick = { onSelect(HifzStatus.NONE) }) {
                    Text(stringResource(R.string.hifz_clear), color = AlKhatibColors.Danger)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HifzPickerOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) color.copy(alpha = 0.12f) else AlKhatibColors.SoftGrey.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.Slate900)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
        }
        if (selected) {
            Text("✓", color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseNoteSheet(
    draft: String,
    hasExistingNote: Boolean,
    onDraftChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.verse_note),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            androidx.compose.material3.OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text(stringResource(R.string.verse_note_hint)) }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasExistingNote) {
                    androidx.compose.material3.TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.delete_note), color = AlKhatibColors.Danger)
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Row {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.back))
                    }
                    androidx.compose.material3.TextButton(onClick = onSave) {
                        Text(stringResource(R.string.done))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ReaderScrollHint(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "scrollHint")
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    Surface(
        onClick = onDismiss,
        modifier = modifier
            .graphicsLayer { translationY = bounce },
        shape = RoundedCornerShape(20.dp),
        color = AlKhatibColors.DeepEmerald.copy(alpha = 0.94f),
        shadowElevation = 8.dp
    ) {
        Text(
            text = stringResource(R.string.reader_scroll_hint),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
