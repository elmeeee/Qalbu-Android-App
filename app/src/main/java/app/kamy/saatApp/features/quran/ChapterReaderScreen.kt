@file:Suppress("SpellCheckingInspection", "UNUSED_VALUE", "AssignedButNeverAccessedVariable", "UnusedAssignment")

package app.kamy.saatApp.features.quran

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import kotlinx.coroutines.launch
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.SaatPartialBottomSheet
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.features.share.AiShareSheet
import androidx.compose.material.icons.filled.Forum
import app.kamy.saatApp.domain.model.HifzStatus
import app.kamy.saatApp.design.components.SaatErrorState
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.displayTransliteration
import app.kamy.saatApp.domain.model.transliterationUsesHtml
import app.kamy.saatApp.domain.model.ArabicTextType
import app.kamy.saatApp.features.reader.HadithSheet
import app.kamy.saatApp.domain.model.RecitationPayload
import app.kamy.saatApp.features.today.components.TafsirSheet
import app.kamy.saatApp.ui.common.TajweedHtmlView
import app.kamy.saatApp.features.quran.tajweed.TajweedType
import app.kamy.saatApp.features.quran.tajweed.TajweedDetailProvider
import app.kamy.saatApp.design.theme.TajweedFontFamily
import androidx.compose.ui.text.font.FontStyle
import app.kamy.saatApp.ui.common.TajweedTextAlign
import app.kamy.saatApp.ui.common.TransliterationView
import app.kamy.saatApp.ui.common.toVerseTranslationPlainText
import app.kamy.saatApp.infrastructure.preferences.ReaderOnboardingStore
import app.kamy.saatApp.ui.components.CoachMarkOverlay
import app.kamy.saatApp.ui.components.coachMarkTarget
import app.kamy.saatApp.ui.components.rememberCoachMarkState
import app.kamy.saatApp.ui.components.FloatingAudioBarMetrics
import app.kamy.saatApp.infrastructure.audio.AudioPlaybackState
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Snapshot of the pager position plus the paging bounds it must be interpreted against.
 * Bundling them means the flow re-emits when verses finish loading, not only when the user
 * swipes, so the ayah the reader opens on is recorded as last read too.
 */
private data class ReaderPageSnapshot(
    val page: Int,
    val isScrolling: Boolean,
    val verseCount: Int,
    val pageOffset: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderScreen(
    initialVerseNumber: Int? = null,
    initialVerseKey: String? = null,
    audioBarVisible: Boolean = false,
    onBack: () -> Unit
) {
    val vm: ChapterReaderViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val audioPlaybackState by vm.audioPlaybackState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val onboardingStore = remember { ReaderOnboardingStore.from(context) }
    var showScrollHint by remember { mutableStateOf(!onboardingStore.hasShownScrollHint()) }
    val coachMarkState = rememberCoachMarkState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!onboardingStore.hasShownQuranCoachMark()) {
            kotlinx.coroutines.delay(1000)
            coachMarkState.show()
            onboardingStore.markQuranCoachMarkShown()
        }
    }

    fun dismissScrollHint() {
        if (!showScrollHint) return
        showScrollHint = false
        onboardingStore.markScrollHintShown()
    }
    val settingsVisible = remember { mutableStateOf(false) }
    val verseMenuExpanded = remember { mutableStateOf(false) }
    val activeTajweedType = remember { mutableStateOf<TajweedType?>(null) }
    val showImageShareSheet = remember { mutableStateOf(false) }
    var hasScrolledToInitial by remember { mutableStateOf(false) }
    var hasAlignedInitialPage by remember { mutableStateOf(false) }

    val s = state
    val verseCount = s.verses.size
    val showPreviousTransition = verseCount > 0 && (
        (s.juzNumber != null && s.juzNumber > 1) ||
        (s.juzNumber == null && s.chapterNumber > 1)
    )
    val showNextTransition = !s.hasMore && verseCount > 0 && (
        (s.juzNumber != null && s.juzNumber < 30) ||
        (s.juzNumber == null && s.chapterNumber < 114)
    )
    val pageOffset = if (showPreviousTransition) 1 else 0
    val totalPageCount = verseCount + pageOffset + (if (showNextTransition) 1 else 0)
    val pagerState = rememberPagerState(initialPage = pageOffset) { totalPageCount.coerceAtLeast(1) }
    val currentVerseIndex = (pagerState.currentPage - pageOffset).coerceIn(0, (verseCount - 1).coerceAtLeast(0))
    val currentVerse = s.verses.getOrNull(currentVerseIndex)
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

    // These long-lived effects below outlive many recompositions, so they must not capture
    // verseCount/pageOffset/totalPageCount directly: those are 0/0/1 on the first composition
    // (before verses load) and the captured values would stay stale forever, silently
    // discarding every page-change and auto-scroll event.
    val latestVerseCount by rememberUpdatedState(verseCount)
    val latestPageOffset by rememberUpdatedState(pageOffset)
    val latestTotalPageCount by rememberUpdatedState(totalPageCount)

    LaunchedEffect(pagerState) {
        // verseCount/pageOffset are part of the snapshot so the flow also re-emits once the
        // verses finish loading. Without that, staying on the very first page produces no
        // further emission and the opening ayah is never recorded as last read.
        snapshotFlow {
            ReaderPageSnapshot(
                page = pagerState.currentPage,
                isScrolling = pagerState.isScrollInProgress,
                verseCount = latestVerseCount,
                pageOffset = latestPageOffset
            )
        }
            .distinctUntilChanged()
            .collect { snap ->
                val vIdx = snap.page - snap.pageOffset
                if (vIdx in 0 until snap.verseCount) {
                    vm.onPageChanged(vIdx)
                    vm.loadMoreIfNeeded(vIdx)
                    if (!snap.isScrolling) {
                        vm.onPageSettled(vIdx)
                    }
                }
            }
    }

    LaunchedEffect(verseCount) {
        if (verseCount == 0) return@LaunchedEffect
        val lastIndex = verseCount - 1 + pageOffset
        if (pagerState.currentPage > lastIndex) {
            pagerState.scrollToPage(lastIndex)
        }
    }

    // rememberPagerState captured initialPage while verseCount was still 0, i.e. before
    // pageOffset could become 1. Once the leading transition page appears, page 0 is that
    // transition page, so align onto the first real ayah instead of opening on it.
    LaunchedEffect(verseCount, pageOffset) {
        if (verseCount == 0 || pageOffset == 0 || hasAlignedInitialPage) return@LaunchedEffect
        hasAlignedInitialPage = true
        val isDeepLink = !initialVerseKey.isNullOrBlank() || initialVerseNumber != null
        if (isDeepLink) return@LaunchedEffect
        if (pagerState.currentPage == 0) {
            pagerState.scrollToPage(pageOffset)
        }
    }

    LaunchedEffect(verseCount, initialVerseNumber, initialVerseKey, state.hasMore, state.isLoadingMore) {
        if (verseCount == 0 || hasScrolledToInitial) return@LaunchedEffect
        val idx = when {
            !initialVerseKey.isNullOrBlank() ->
                state.verses.indexOfFirst { it.verseKey == initialVerseKey }
            initialVerseNumber != null ->
                state.verses.indexOfFirst { it.resolvedVerseNumber == initialVerseNumber }
            else -> -1
        }
        if (idx >= 0) {
            val targetPage = idx + pageOffset
            if (pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
            hasScrolledToInitial = true
        } else if (initialVerseKey.isNullOrBlank() && initialVerseNumber == null) {
            hasScrolledToInitial = true
        } else if (state.hasMore && !state.isLoadingMore) {
            vm.loadMoreIfNeeded(state.verses.size - 1)
        } else if (!state.hasMore) {
            hasScrolledToInitial = true
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        verseMenuExpanded.value = false
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                is ReaderEvent.AnimateToPage -> {
                    val verses = vm.state.value.verses
                    val target = event.index + latestPageOffset
                    if (event.index in verses.indices && target in 0 until latestTotalPageCount) {
                        scope.launch {
                            runCatching { pagerState.animateScrollToPage(target) }
                        }
                    }
                }
                is ReaderEvent.AutoAdvanceToPage -> {
                    val verses = vm.state.value.verses
                    val target = event.nextIndex + latestPageOffset
                    if (event.nextIndex in verses.indices && target in 0 until latestTotalPageCount) {
                        // Follow the audio only while the pager is still near the ayah that just
                        // finished, so a user who has browsed elsewhere is not yanked away.
                        val previousPage = event.previousIndex + latestPageOffset
                        if (kotlin.math.abs(pagerState.currentPage - previousPage) <= 1) {
                            scope.launch {
                                runCatching { pagerState.animateScrollToPage(target) }
                            }
                        }
                    }
                }
            }
        }
    }

    // Safety net for audio-follow scrolling. The events above go through a droppable
    // tryEmit(buffer = 1, no replay), so a missed emission would leave the pager stuck on an
    // earlier ayah with no way back in sync. Reconciling against the playing verse key here
    // means playback position always wins eventually, even if an event was lost.
    val playingVerseKey = state.currentlyPlayingVerseKey
    LaunchedEffect(playingVerseKey, verseCount) {
        if (playingVerseKey.isNullOrBlank() || verseCount == 0) return@LaunchedEffect
        val idx = state.verses.indexOfFirst { it.verseKey == playingVerseKey }
        if (idx < 0) return@LaunchedEffect
        val target = idx + pageOffset
        if (target !in 0 until totalPageCount || pagerState.currentPage == target) return@LaunchedEffect
        // Only pull the view along if the user has not deliberately browsed away.
        if (kotlin.math.abs(pagerState.currentPage - target) > 1) return@LaunchedEffect
        runCatching { pagerState.animateScrollToPage(target) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
    ) {
        if (state.isLoading && state.verses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = SaatColors.DeepEmerald
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
                SaatErrorState(
                    display = loadErrorDisplay,
                    onRetry = { vm.loadInitial() }
                )
            }
        } else if (state.verses.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { pageIndex ->
                when {
                    showPreviousTransition && pageIndex == 0 -> {
                        val prevChapterNum = (state.chapterNumber - 1).coerceAtLeast(1)
                        val prevSurahName = state.chapterLookup[prevChapterNum]
                        PreviousSurahTransitionPage(
                            currentSurahName = state.chapterDisplayName,
                            prevSurahName = prevSurahName,
                            isJuz = state.juzNumber != null,
                            currentJuzNumber = state.juzNumber,
                            onTriggerTransition = {
                                scope.launch {
                                    pagerState.scrollToPage(pageOffset)
                                }
                                vm.loadPreviousSurahOrJuz()
                            }
                        )
                    }
                    showNextTransition && pageIndex == (verseCount + pageOffset) -> {
                        val nextChapterNum = state.chapterNumber + 1
                        val nextSurahName = state.chapterLookup[nextChapterNum]
                        NextSurahTransitionPage(
                            currentSurahName = state.chapterDisplayName,
                            nextSurahName = nextSurahName,
                            isJuz = state.juzNumber != null,
                            currentJuzNumber = state.juzNumber,
                            onTriggerTransition = {
                                scope.launch {
                                    pagerState.scrollToPage(pageOffset)
                                }
                                vm.loadNextSurahOrJuz()
                            }
                        )
                    }
                    else -> {
                        val verseIdx = pageIndex - pageOffset
                        val verse = state.verses.getOrNull(verseIdx) ?: return@VerticalPager
                        SaatAyahPage(
                            verse = verse,
                            fontScale = state.fontScale,
                            showTranslation = state.showTranslation && !state.hifzModeEnabled,
                            showTransliteration = state.showTransliteration,
                            translationId = state.selectedTranslationId,
                            isTajweedEnabled = state.isTajweedEnabled,
                            arabicTextType = state.arabicTextType,
                            hifzModeEnabled = state.hifzModeEnabled,
                            audioBarVisible = audioBarVisible,
                            personalDataRevision = state.personalDataRevision,
                            showBismillahPre = verseIdx == 0 && state.bismillahPre,
                            audioPlaybackState = audioPlaybackState,
                            onPlay = { vm.onTapAyah(verseIdx) },
                            onContentScroll = if (verseIdx == 0) ::dismissScrollHint else null,
                            onTajweedClick = { activeTajweedType.value = it }
                        )
                    }
                }
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
                            SaatColors.ScreenBackground,
                            SaatColors.ScreenBackground.copy(alpha = 0.92f),
                            SaatColors.ScreenBackground.copy(alpha = 0.6f),
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
                        tint = SaatColors.Slate900
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { settingsVisible.value = true }) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_setting_on),
                        contentDescription = stringResource(R.string.reading_settings_a11y),
                        tint = SaatColors.Slate900
                    )
                }
            }
        }

        currentVerse?.let { verse ->
            val surahCaptionBottom = if (audioBarVisible) {
                FloatingAudioBarMetrics.barHeight + FloatingAudioBarMetrics.bottomGap + 12.dp
            } else {
                18.dp
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                SaatColors.ScreenBackground.copy(alpha = 0.55f),
                                SaatColors.ScreenBackground.copy(alpha = 0.92f),
                                SaatColors.ScreenBackground,
                                SaatColors.ScreenBackground
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = surahCaptionBottom,
                        top = 44.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = surahTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        val subtitle = if (state.juzNumber != null) {
                            val verseLabel = verse.displayVerseReference?.let { ref ->
                                stringResource(R.string.verse_key_label, ref)
                            } ?: verse.resolvedVerseNumber?.let { num ->
                                stringResource(R.string.verse_number, num)
                            } ?: stringResource(R.string.verse_label, "—")
                            listOfNotNull(
                                verseLabel,
                                state.juzNumber?.let { stringResource(R.string.juz_number, it) }
                            ).joinToString(" · ")
                        } else {
                            verse.resolvedVerseNumber?.let { num ->
                                stringResource(R.string.verse_number, num)
                            } ?: verse.displayVerseReference?.let { ref ->
                                stringResource(R.string.verse_key_label, ref)
                            } ?: stringResource(R.string.verse_label, "—")
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = SaatColors.Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (state.verses.isNotEmpty()) {
                        ReaderVerseActionsMenu(
                            expanded = verseMenuExpanded.value,
                            onToggle = { verseMenuExpanded.value = !verseMenuExpanded.value },
                            bookmarked = state.currentVerseBookmarked,
                            hasNote = state.currentVerseHasNote,
                            hifzStatus = state.currentVerseHifzStatus,
                            showTafsir = LocalQuranConfig.supportsTafsir(state.selectedTranslationId),
                            modifier = Modifier.coachMarkTarget(
                                coachMarkState,
                                0,
                                R.string.coach_mark_quran_menu_title,
                                R.string.coach_mark_quran_menu_desc
                            ),
                            onBookmark = {
                                verseMenuExpanded.value = false
                                vm.toggleBookmark(currentVerseIndex)
                            },
                            onNote = {
                                verseMenuExpanded.value = false
                                vm.openNote(currentVerseIndex)
                            },
                            onHifz = {
                                verseMenuExpanded.value = false
                                vm.cycleHifzStatus(currentVerseIndex)
                            },
                            onAiShare = {
                                verseMenuExpanded.value = false
                                vm.openAiShare(currentVerseIndex)
                            },
                            onShareImage = {
                                verseMenuExpanded.value = false
                                showImageShareSheet.value = true
                            },
                            onTafsir = {
                                verseMenuExpanded.value = false
                                currentVerse?.verseKey?.let(vm::openTafsir)
                            },
                            onHadith = {
                                verseMenuExpanded.value = false
                                currentVerse?.verseKey?.let(vm::openHadith)
                            }
                        )
                    }
                }
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

    if (settingsVisible.value) {
        ReaderSettingsSheet(
            state = state,
            onDismiss = { settingsVisible.value = false },
            onFontScaleChange = vm::setFontScale,
            onToggleTranslation = vm::toggleTranslation,
            onToggleTransliteration = vm::toggleTransliteration,
            onToggleTajweed = vm::toggleTajweed,
            onSelectRecitation = vm::selectRecitation,
            onSetPlaybackMode = vm::setPlaybackMode,
            onToggleHifzMode = vm::toggleHifzMode,
            onArabicTextTypeChange = vm::setArabicTextType
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

    activeTajweedType.value?.let { type ->
        TajweedInfoSheet(
            type = type,
            onDismiss = { activeTajweedType.value = null }
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
        onDismiss = { vm.dismissAiShare() },
        onDraftChange = vm::updateAiShareDraft,
        onRegenerate = vm::regenerateAiShare,
        onShare = { draft ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, draft)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_reflection)))
        }
    )

    if (showImageShareSheet.value) {
        currentVerse?.let { verse ->
            app.kamy.saatApp.features.share.AyahImageShareSheet(
                verse = verse,
                surahName = surahTitle,
                onDismiss = { showImageShareSheet.value = false },
                onShare = { template ->
                    app.kamy.saatApp.features.share.AyahImageShare.shareAyahAsImage(
                        context = context,
                        verse = verse,
                        surahName = surahTitle,
                        template = template
                    )
                    showImageShareSheet.value = false
                }
            )
        }
    }
    CoachMarkOverlay(state = coachMarkState, onDismiss = { coachMarkState.skip() })
}

@Composable
private fun SaatAyahPage(
    verse: RandomAyahPayload,
    fontScale: Float,
    showTranslation: Boolean,
    showTransliteration: Boolean,
    translationId: Int,
    isTajweedEnabled: Boolean,
    arabicTextType: ArabicTextType,
    hifzModeEnabled: Boolean,
    audioBarVisible: Boolean,
    personalDataRevision: Int,
    showBismillahPre: Boolean,
    audioPlaybackState: AudioPlaybackState? = null,
    onPlay: () -> Unit,
    onContentScroll: (() -> Unit)? = null,
    onTajweedClick: (TajweedType) -> Unit
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentTopPadding = statusBarTop + 68.dp
    val contentBottomPadding = if (audioBarVisible) 200.dp else 160.dp
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

    // Nested scroll connection: let inner Column consume scroll first.
    // Only release the gesture to the parent VerticalPager when the inner
    // content has already hit its top or bottom boundary.
    val nestedScrollConnection = remember(scrollState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Scrolling down (negative dy) — forward to pager only when already at bottom
                if (available.y < 0 && scrollState.value >= scrollState.maxValue) {
                    return Offset.Zero
                }
                // Scrolling up (positive dy) — forward to pager only when already at top
                if (available.y > 0 && scrollState.value <= 0) {
                    return Offset.Zero
                }
                // Inner scroll can still handle it — consume nothing here so the
                // inner verticalScroll modifier gets the event.
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return available
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
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
            if (showBismillahPre) {
                Text(
                    text = "﷽",
                    fontSize = (38 * fontScale).sp,
                    color = SaatColors.DeepEmerald,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
            when {
                hifzModeEnabled && hifzRevealStage == 0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(SaatColors.SoftGrey.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.hifz_recall_prompt),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SaatColors.Slate800,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.hifz_tap_to_reveal),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SaatColors.Slate500,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    val textToRender = if (arabicTextType == ArabicTextType.INDOPAK) (verse.textIndopak ?: verse.textUthmani) else verse.textUthmani
                    val isPlayingThisVerse = audioPlaybackState != null && audioPlaybackState.isPlaying && audioPlaybackState.trackSubtitle == verse.verseKey && audioPlaybackState.currentUrl != null
                    val (words, weights, totalWeight) = remember(textToRender) {
                        val parsedWords = textToRender?.split("\\s+".toRegex())
                            ?.filter { it.isNotBlank() }
                            ?.map { word ->
                                word.replace("""^[﴿\(]?\d+[﴾\)]?""".toRegex(), "").trim()
                            }
                            ?.filter { it.isNotBlank() } ?: emptyList()

                        val parsedWeights = parsedWords.mapIndexed { index, word ->
                            var weight = word.length.toFloat().coerceAtLeast(1f)
                            // Madd (long vowels) & Tajweed extensions
                            if (word.contains('ٓ') || word.contains('ۤ') || word.contains('ۧ') || word.contains('ۨ') || word.contains('\u0653') || word.contains('\u06E4')) {
                                weight += 8.0f
                            }
                            // Shaddah & Ghunnah
                            if (word.contains('ّ') || word.contains('\u0651')) {
                                if (word.contains('ن') || word.contains('م')) {
                                    weight += 4.0f
                                } else {
                                    weight += 2.0f
                                }
                            }
                            // Pause / Waqf signs (ۘ, ۚ, ۖ, ۗ, ۬, ۥ, ۙ)
                            if (word.contains('ۘ') || word.contains('ۚ') || word.contains('ۖ') || word.contains('ۗ') || word.contains('۬') || word.contains('ۥ') || word.contains('ۙ')) {
                                weight += 6.0f
                            }
                            if (index == parsedWords.lastIndex && parsedWords.size > 1) {
                                weight *= 1.25f
                            }
                            weight
                        }
                        Triple(parsedWords, parsedWeights, parsedWeights.sum().coerceAtLeast(1f))
                    }

                    val activeWordIndex by remember(
                        isPlayingThisVerse,
                        audioPlaybackState?.currentPositionMs,
                        audioPlaybackState?.progress,
                        words,
                        weights,
                        totalWeight
                    ) {
                        derivedStateOf {
                            if (!isPlayingThisVerse || words.isEmpty()) {
                                null
                            } else {
                                val dur = audioPlaybackState?.durationMs ?: 0L
                                val pos = audioPlaybackState?.currentPositionMs ?: 0L
                                val prog = if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else (audioPlaybackState?.progress ?: 0f).coerceIn(0f, 1f)

                                val targetWeight = prog * totalWeight
                                var accum = 0f
                                var foundIndex = 0
                                for (i in words.indices) {
                                    accum += weights[i]
                                    if (targetWeight <= accum) {
                                        foundIndex = i
                                        break
                                    }
                                    foundIndex = i
                                }
                                foundIndex
                            }
                        }
                    }
                    TajweedHtmlView(
                        textUthmani = textToRender ?: "",
                        ayahNumber = verse.resolvedVerseNumber,
                        fontSizeSp = (30 * fontScale).toInt(),
                        compact = true,
                        textAlign = TajweedTextAlign.Justify,
                        isTajweedEnabled = isTajweedEnabled,
                        activeWordIndex = activeWordIndex,
                        onTajweedClick = onTajweedClick,
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
                            .background(SaatColors.LightGrey.copy(alpha = 0.45f))
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
                            color = SaatColors.Slate800,
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
    modifier: Modifier = Modifier,
    showTafsir: Boolean = true,
    onBookmark: () -> Unit,
    onNote: () -> Unit,
    onHifz: () -> Unit,
    onAiShare: () -> Unit,
    onShareImage: () -> Unit,
    onTafsir: () -> Unit,
    onHadith: () -> Unit
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
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_bookmark_custom),
                            contentDescription = null,
                            tint = SaatColors.GoldDeep,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(
                        if (bookmarked) R.string.remove_bookmark else R.string.bookmark
                    ),
                    accent = SaatColors.GoldDeep,
                    onClick = onBookmark
                )
                ReaderActionPill(
                    icon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_personalnote_custom),
                            contentDescription = null,
                            tint = if (hasNote) SaatColors.DeepEmerald else SaatColors.Slate500,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(
                        if (hasNote) R.string.verse_note_edit else R.string.verse_note
                    ),
                    accent = SaatColors.DeepEmerald,
                    onClick = onNote
                )
                ReaderActionPill(
                    icon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_memorization_custom),
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
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_ai),
                            contentDescription = null,
                            tint = SaatColors.Gold,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(R.string.ai_label),
                    accent = SaatColors.Gold,
                    onClick = onAiShare
                )
                ReaderActionPill(
                    icon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_share_custom),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(R.string.share_as_image),
                    accent = SaatColors.DeepEmerald,
                    onClick = onShareImage
                )
                if (showTafsir) {
                    ReaderActionPill(
                        icon = {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_tafsir),
                                contentDescription = null,
                                tint = SaatColors.IndigoAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        },
                        label = stringResource(R.string.tafsir),
                        accent = SaatColors.IndigoAccent,
                        onClick = onTafsir
                    )
                }
                ReaderActionPill(
                    icon = {
                        Icon(
                            Icons.Filled.Forum,
                            contentDescription = null,
                            tint = SaatColors.Teal,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(R.string.hadith),
                    accent = SaatColors.Teal,
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
                        listOf(SaatColors.DeepEmerald, SaatColors.Teal)
                    )
                )
                .clickable(onClick = onToggle)
                .padding(2.dp)
        ) {
            Surface(
                onClick = onToggle,
                shape = CircleShape,
                color = SaatColors.PureWhite,
                shadowElevation = if (expanded) 2.dp else 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val closeActionsLabel = stringResource(R.string.close_verse_actions)
                    val studyToolsLabel = stringResource(R.string.study_tools)
                    Icon(
                        imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.AutoStories,
                        contentDescription = if (expanded) closeActionsLabel else studyToolsLabel,
                        tint = if (expanded) SaatColors.Slate800 else SaatColors.DeepEmerald,
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
        color = SaatColors.PureWhite,
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
                color = SaatColors.Slate800,
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
    onToggleTajweed: (Boolean) -> Unit,
    onSelectRecitation: (Int) -> Unit,
    onSetPlaybackMode: (AyahPlaybackMode) -> Unit,
    onToggleHifzMode: (Boolean) -> Unit,
    onArabicTextTypeChange: (ArabicTextType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SaatColors.PureWhite,
        scrimColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.32f)
    ) {
        val maxSheetHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp * 0.78f
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                SaatColors.DeepEmerald.copy(alpha = 0.12f),
                                SaatColors.Teal.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SaatColors.DeepEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_setting_on),
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reading_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Text(
                        text = stringResource(R.string.font_size_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Slider card
            ReaderSettingToggleRow(
                title = stringResource(R.string.text_size),
                content = {
                    Slider(
                        value = state.fontScale,
                        onValueChange = onFontScaleChange,
                        valueRange = 0.85f..2.0f,
                        steps = 11,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = SaatColors.Teal,
                            activeTrackColor = SaatColors.DeepEmerald
                        )
                    )
                }
            )

            // Toggles using custom icons
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
                title = "Show Tajweed",
                checked = state.isTajweedEnabled,
                onCheckedChange = onToggleTajweed
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
                text = stringResource(R.string.reciter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate800,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            if (state.recitations.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = SaatColors.DeepEmerald
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (content != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SaatColors.Slate900,
                    fontWeight = FontWeight.SemiBold
                )
                content()
            } else if (checked != null && onCheckedChange != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SaatColors.Slate900,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(
                            if (checked) R.drawable.ic_toggle_on_custom else R.drawable.ic_toggle_off_custom
                        ),
                        contentDescription = if (checked) "On" else "Off",
                        modifier = Modifier
                            .size(width = 52.dp, height = 28.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onCheckedChange(!checked) }
                            )
                    )
                }
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
                    if (selected) SaatColors.DeepEmerald.copy(alpha = 0.1f)
                    else Color.Transparent
                )
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = recitation.displayName,
                modifier = Modifier.weight(1f),
                color = SaatColors.Slate900,
                style = MaterialTheme.typography.bodyLarge
            )
            if (selected) {
                Text("✓", color = SaatColors.DeepEmerald, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SaatColors.SoftGrey)
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
    HifzStatus.NONE -> SaatColors.Slate500
    HifzStatus.LEARNING -> SaatColors.IndigoAccent
    HifzStatus.MEMORIZED -> SaatColors.DeepEmerald
    HifzStatus.NEEDS_REVIEW -> SaatColors.GoldDeep
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
                color = SaatColors.DeepEmerald
            )
            Text(
                text = stringResource(R.string.hifz_picker_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate500
            )
            HifzPickerOption(
                title = stringResource(R.string.hifz_learning),
                subtitle = stringResource(R.string.hifz_learning_desc),
                selected = currentStatus == HifzStatus.LEARNING,
                color = SaatColors.IndigoAccent,
                onClick = { onSelect(HifzStatus.LEARNING) }
            )
            HifzPickerOption(
                title = stringResource(R.string.hifz_memorized),
                subtitle = stringResource(R.string.hifz_memorized_desc),
                selected = currentStatus == HifzStatus.MEMORIZED,
                color = SaatColors.DeepEmerald,
                onClick = { onSelect(HifzStatus.MEMORIZED) }
            )
            HifzPickerOption(
                title = stringResource(R.string.hifz_review),
                subtitle = stringResource(R.string.hifz_review_desc),
                selected = currentStatus == HifzStatus.NEEDS_REVIEW,
                color = SaatColors.GoldDeep,
                onClick = { onSelect(HifzStatus.NEEDS_REVIEW) }
            )
            if (currentStatus != HifzStatus.NONE) {
                TextButton(onClick = { onSelect(HifzStatus.NONE) }) {
                    Text(stringResource(R.string.hifz_clear), color = SaatColors.Danger)
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
            .background(if (selected) color.copy(alpha = 0.12f) else SaatColors.SoftGrey.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = SaatColors.Slate900)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SaatColors.Slate500)
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
                color = SaatColors.DeepEmerald
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
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.delete_note), color = SaatColors.Danger)
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Row {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.back))
                    }
                    TextButton(onClick = onSave) {
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
        color = SaatColors.DeepEmerald.copy(alpha = 0.94f),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TajweedInfoSheet(
    type: TajweedType,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val languageCode = context.resources.configuration.locales[0].language
    val detail = remember(type, languageCode) {
        TajweedDetailProvider.getDetail(type, languageCode)
    }

    SaatPartialBottomSheet(
        onDismiss = onDismiss,
        maxHeightFraction = 0.85f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(detail.color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = detail.color
                    )
                }
                Column {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = SaatColors.DeepEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (languageCode == "in" || languageCode == "id" || languageCode == "ms") "Hukum Tajwid" else "Tajweed Rule",
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (languageCode == "in" || languageCode == "id" || languageCode == "ms") "Huruf Tajwid" else "Letters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SaatColors.LightGrey,
                border = androidx.compose.foundation.BorderStroke(1.dp, SaatColors.SoftGrey)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = detail.letters,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = TajweedFontFamily,
                            textAlign = TextAlign.Center,
                            color = detail.color
                        ),
                        letterSpacing = 4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (languageCode == "in" || languageCode == "id" || languageCode == "ms") "Pengertian" else "Definition",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = detail.description,
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate700,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (languageCode == "in" || languageCode == "id" || languageCode == "ms") "Cara Membaca" else "How to Read",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = detail.howToRead,
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate700,
                lineHeight = 22.sp
            )

            if (detail.examples.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (languageCode == "in" || languageCode == "id" || languageCode == "ms") "Contoh Bacaan" else "Examples",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate800
                )
                Spacer(modifier = Modifier.height(10.dp))

                detail.examples.forEach { example ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SaatColors.PureWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SaatColors.SoftGrey)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.runtime.CompositionLocalProvider(
                                    androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
                                ) {
                                    Text(
                                        text = example.arabic,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = TajweedFontFamily,
                                            fontSize = 28.sp,
                                            color = SaatColors.Slate900
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = example.transliteration,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium,
                                    color = SaatColors.DeepEmerald
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = example.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = SaatColors.Slate500,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReaderTransitionCard(
    titleText: String,
    targetText: String,
    buttonText: String,
    icon: ImageVector,
    onTrigger: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SaatColors.DeepEmerald.copy(alpha = 0.1f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = SaatColors.Slate500,
                textAlign = TextAlign.Center
            )
            Text(
                text = targetText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onTrigger,
                colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun NextSurahTransitionPage(
    currentSurahName: String?,
    nextSurahName: String?,
    isJuz: Boolean,
    currentJuzNumber: Int?,
    onTriggerTransition: () -> Unit
) {
    val titleText = if (isJuz) {
        currentJuzNumber?.let { stringResource(R.string.transition_finished_juz, it) }
            ?: stringResource(R.string.transition_finished_surah, currentSurahName.orEmpty())
    } else {
        stringResource(R.string.transition_finished_surah, currentSurahName.orEmpty())
    }

    val targetText = if (isJuz) {
        val nextJuz = (currentJuzNumber ?: 1) + 1
        stringResource(R.string.transition_next_juz, nextJuz)
    } else {
        val targetSurah = nextSurahName ?: stringResource(R.string.transition_next_surah_default)
        stringResource(R.string.transition_next_surah, targetSurah)
    }

    val buttonText = stringResource(R.string.transition_btn_start)

    ReaderTransitionCard(
        titleText = titleText,
        targetText = targetText,
        buttonText = buttonText,
        icon = Icons.Filled.AutoStories,
        onTrigger = onTriggerTransition
    )
}

@Composable
private fun PreviousSurahTransitionPage(
    currentSurahName: String?,
    prevSurahName: String?,
    isJuz: Boolean,
    currentJuzNumber: Int?,
    onTriggerTransition: () -> Unit
) {
    val titleText = if (isJuz) {
        currentJuzNumber?.let { stringResource(R.string.juz_number, it) }
            ?: currentSurahName.orEmpty()
    } else {
        currentSurahName.orEmpty()
    }

    val targetText = if (isJuz) {
        val prevJuz = (currentJuzNumber ?: 1) - 1
        stringResource(R.string.transition_prev_juz, prevJuz)
    } else {
        val targetSurah = prevSurahName ?: stringResource(R.string.transition_prev_surah_default)
        stringResource(R.string.transition_prev_surah, targetSurah)
    }

    val buttonText = stringResource(R.string.transition_btn_back)

    ReaderTransitionCard(
        titleText = titleText,
        targetText = targetText,
        buttonText = buttonText,
        icon = Icons.AutoMirrored.Filled.MenuBook,
        onTrigger = onTriggerTransition
    )
}
