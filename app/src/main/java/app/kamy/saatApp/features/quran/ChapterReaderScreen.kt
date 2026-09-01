@file:Suppress("SpellCheckingInspection", "UNUSED_VALUE", "AssignedButNeverAccessedVariable", "UnusedAssignment")

package app.kamy.saatApp.features.quran

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import app.kamy.saatApp.features.quran.components.PageCurlPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.key
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
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.painterResource
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
    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(view) {
        val activity = context as? android.app.Activity
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val root = activity?.window?.decorView ?: view
            root.post {
                val rect = android.graphics.Rect(0, 0, root.width, root.height)
                root.systemGestureExclusionRects = listOf(rect)
            }
        }
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val root = activity?.window?.decorView ?: view
                root.systemGestureExclusionRects = emptyList()
            }
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

    // Intercept system gesture navigation swipe-back so Quran ayah swiping is never interrupted.
    // Closes any open dialogs/sheets first, and requires user to use the top app bar back button to exit.
    BackHandler(enabled = true) {
        when {
            showImageShareSheet.value -> showImageShareSheet.value = false
            activeTajweedType.value != null -> activeTajweedType.value = null
            verseMenuExpanded.value -> verseMenuExpanded.value = false
            settingsVisible.value -> settingsVisible.value = false
            else -> {
                // Consume gesture so swiping pages does not trigger Android OS back navigation
            }
        }
    }

    val s = state
    val verseCount = s.verses.size
    val showPreviousTransition = (
        (s.juzNumber != null && s.juzNumber > 1) ||
        (s.juzNumber == null && s.chapterNumber > 1)
    )
    val showNextTransition = !s.hasMore && verseCount > 0 && (
        (s.juzNumber != null && s.juzNumber < 30) ||
        (s.juzNumber == null && s.chapterNumber < 114)
    )
    val pageOffset = if (showPreviousTransition) 1 else 0
    val totalPageCount = verseCount + pageOffset + (if (showNextTransition) 1 else 0)

    val currentChapterKey = "${s.chapterNumber}_${s.juzNumber}"
    val originChapterNumber = remember { s.chapterNumber }
    val originJuzNumber = remember { s.juzNumber }
    val isOriginChapter = s.chapterNumber == originChapterNumber && s.juzNumber == originJuzNumber

    val defaultInitialVerseIdx = remember(currentChapterKey) {
        if (!isOriginChapter) {
            0
        } else {
            when {
                !initialVerseKey.isNullOrBlank() -> {
                    val verseNum = initialVerseKey.substringAfterLast(':').toIntOrNull()
                    if (verseNum != null && verseNum > 0) verseNum - 1 else 0
                }
                initialVerseNumber != null && initialVerseNumber > 0 -> initialVerseNumber - 1
                else -> 0
            }
        }
    }
    val calculatedInitialPage = pageOffset + defaultInitialVerseIdx

    val pagerState = key(currentChapterKey) {
        rememberPagerState(initialPage = calculatedInitialPage) { totalPageCount.coerceAtLeast(1) }
    }
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

    LaunchedEffect(currentChapterKey) {
        hasScrolledToInitial = false
        hasAlignedInitialPage = false
    }

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

    LaunchedEffect(verseCount, pageOffset) {
        if (verseCount == 0) return@LaunchedEffect
        val lastIndex = verseCount - 1 + pageOffset
        if (pagerState.currentPage > lastIndex) {
            pagerState.scrollToPage(lastIndex)
        }
    }

    LaunchedEffect(currentChapterKey, verseCount, pageOffset) {
        if (verseCount == 0 || hasAlignedInitialPage) return@LaunchedEffect
        hasAlignedInitialPage = true
        val isDeepLink = isOriginChapter && (!initialVerseKey.isNullOrBlank() || initialVerseNumber != null)
        if (!isDeepLink && pagerState.currentPage != pageOffset) {
            pagerState.scrollToPage(pageOffset)
        }
    }

    LaunchedEffect(currentChapterKey, verseCount, initialVerseNumber, initialVerseKey, state.hasMore, state.isLoadingMore) {
        if (verseCount == 0 || hasScrolledToInitial) return@LaunchedEffect
        if (!isOriginChapter) {
            hasScrolledToInitial = true
            return@LaunchedEffect
        }
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
                is ReaderEvent.ShowToast -> {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = androidx.compose.material3.SnackbarDuration.Short
                        )
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
            PageCurlPager(
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
                        val verse = state.verses.getOrNull(verseIdx) ?: return@PageCurlPager
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SaatColors.ScreenBackground,
                            SaatColors.ScreenBackground.copy(alpha = 0.96f),
                            SaatColors.ScreenBackground.copy(alpha = 0.85f),
                            SaatColors.ScreenBackground.copy(alpha = 0.35f),
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
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = SaatColors.Slate900
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = surahTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    val ayahLabel = if (state.juzNumber != null) {
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
                        text = ayahLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        vm.toggleBookmark(currentVerseIndex)
                    }
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_bookmark_custom),
                        contentDescription = stringResource(if (state.currentVerseBookmarked) R.string.remove_bookmark else R.string.bookmark),
                        tint = if (state.currentVerseBookmarked) SaatColors.DeepEmerald else SaatColors.Slate900,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            SaatColors.ScreenBackground.copy(alpha = 0.35f),
                            SaatColors.ScreenBackground.copy(alpha = 0.85f),
                            SaatColors.ScreenBackground.copy(alpha = 0.96f),
                            SaatColors.ScreenBackground
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 28.dp,
                    bottom = 24.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Action: Menu Book / Actions popup
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
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
                            }
                        )
                    }

                    Surface(
                        onClick = { verseMenuExpanded.value = !verseMenuExpanded.value },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.40f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.08f),
                                spotColor = Color.Black.copy(alpha = 0.06f)
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_tafsir),
                                contentDescription = stringResource(R.string.coach_mark_quran_menu_title),
                                tint = if (verseMenuExpanded.value) SaatColors.DeepEmerald else SaatColors.Slate800,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Center Action: Audio Playback Capsule (Liquid Glass between Tafsir and Aa)
                AnimatedVisibility(
                    visible = audioPlaybackState.currentUrl != null,
                    enter = fadeIn() + expandHorizontally() + scaleIn(),
                    exit = fadeOut() + shrinkHorizontally() + scaleOut(),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.45f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                        modifier = Modifier
                            .height(48.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = Color.Black.copy(alpha = 0.08f),
                                spotColor = Color.Black.copy(alpha = 0.06f)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 5.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            // Custom Play / Pause Circle
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SaatColors.DeepEmerald)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { vm.toggleAudioPlay(currentVerseIndex) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(
                                        if (audioPlaybackState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                    ),
                                    contentDescription = if (audioPlaybackState.isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Audio Info (Surah, Ayah, Reciter) & Progress
                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(end = 2.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                val surahTitle = audioPlaybackState.trackTitle.ifBlank {
                                    state.chapterDisplayName ?: "Surah ${state.chapterNumber}"
                                }
                                val ayahNum = audioPlaybackState.ayahNumber ?: currentVerse?.resolvedVerseNumber
                                val ayahLabel = if (ayahNum != null && ayahNum > 0) "Ayat $ayahNum" else audioPlaybackState.trackSubtitle
                                val reciterName = audioPlaybackState.reciterName.ifBlank {
                                    state.recitations.firstOrNull { it.identifiableId == state.selectedRecitationId }?.displayName ?: ""
                                }

                                Text(
                                    text = "$surahTitle · $ayahLabel",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.Slate900,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                if (reciterName.isNotBlank()) {
                                    Text(
                                        text = reciterName,
                                        fontSize = 10.sp,
                                        color = SaatColors.Slate500,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { audioPlaybackState.progress.coerceIn(0f, 1f) },
                                    color = SaatColors.DeepEmerald,
                                    trackColor = SaatColors.SoftGrey.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(2.dp)
                                        .clip(CircleShape)
                                )
                            }

                            // Close / Stop Button
                            IconButton(
                                onClick = { vm.stopAudio() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Stop",
                                    tint = SaatColors.Slate500,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Right Action: "Aa" (Reading Settings Liquid Glass)
                Surface(
                    onClick = { settingsVisible.value = true },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.40f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.06f)
                        )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.Slate800
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (audioBarVisible) 110.dp else 68.dp, start = 20.dp, end = 20.dp)
        ) { snackbarData ->
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.95f)),
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.06f)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    SaatColors.DeepEmerald.copy(alpha = 0.08f),
                                    Color.White.copy(alpha = 0.92f)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bookmark_custom),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.Slate900
                    )
                }
            }
        }

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
        selectedSource = state.selectedTafsirSource,
        error = state.tafsirError,
        onDismiss = { vm.dismissTafsir() },
        onReload = { vm.reloadTafsir() },
        onSelectSource = { vm.selectTafsirSource(it) }
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
    val contentBottomPadding = if (audioBarVisible) 160.dp else 110.dp
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
            .background(SaatColors.ScreenBackground)
            .pointerInput(hifzModeEnabled, showTranslation) {
                detectTapGestures(onTap = {
                    if (hifzModeEnabled) {
                        when (hifzRevealStage) {
                            0 -> hifzRevealStage = 1
                            1 -> if (showTranslation) hifzRevealStage = 2 else onPlay()
                            else -> onPlay()
                        }
                    } else {
                        onPlay()
                    }
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = scrollState)
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
                    val isPlayingThisVerse = audioPlaybackState != null && audioPlaybackState.trackSubtitle == verse.verseKey && audioPlaybackState.currentUrl != null
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
                        audioPlaybackState?.durationMs,
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
                                val rawProg = if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else (audioPlaybackState?.progress ?: 0f).coerceIn(0f, 1f)

                                // Normalize progress to trim leading (~3%) and trailing (~5%) audio padding/silence
                                val leadPaddingRatio = 0.03f
                                val trailPaddingRatio = 0.05f
                                val prog = ((rawProg - leadPaddingRatio) / (1f - leadPaddingRatio - trailPaddingRatio)).coerceIn(0f, 1f)

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
                    Text(
                        text = transliteration,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = (22 * fontScale).sp,
                            fontSize = (15 * fontScale).sp
                        ),
                        color = SaatColors.Slate700,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }
            }
            if (showMeaning) {
                verse.translations?.firstOrNull()?.text?.let { translation ->
                    val clean = translation.toVerseTranslationPlainText()
                    if (clean.isNotEmpty()) {
                        Text(
                            text = clean,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = (24 * fontScale).sp,
                                fontSize = (15 * fontScale).sp
                            ),
                            color = SaatColors.Slate900,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
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
    onTafsir: () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            if (showTafsir) {
                ReaderActionPill(
                    icon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_tafsir),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    label = stringResource(R.string.tafsir),
                    accent = SaatColors.DeepEmerald,
                    onClick = onTafsir
                )
            }
            ReaderActionPill(
                icon = {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_ai),
                        contentDescription = null,
                        tint = SaatColors.GoldDeep,
                        modifier = Modifier.size(17.dp)
                    )
                },
                label = stringResource(R.string.ai_label),
                accent = SaatColors.GoldDeep,
                onClick = onAiShare
            )
            ReaderActionPill(
                icon = {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_share_custom),
                        contentDescription = null,
                        tint = SaatColors.Slate700,
                        modifier = Modifier.size(17.dp)
                    )
                },
                label = "Bagikan",
                accent = SaatColors.Slate700,
                onClick = onShareImage
            )
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
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFEBE7DF)),
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(50),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
    ) {
        Row(
            modifier = Modifier.padding(start = 5.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = SaatColors.Slate800,
                maxLines = 1
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
        containerColor = SaatColors.ScreenBackground,
        scrimColor = Color.Black.copy(alpha = 0.35f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(SaatColors.Slate500.copy(alpha = 0.3f))
            )
        }
    ) {
        val maxSheetHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp * 0.82f
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aa",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reading_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                    Text(
                        text = stringResource(R.string.font_size_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate500
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SaatColors.PureWhite)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = SaatColors.Slate700,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Section 1: Font Size & Live Arabic Preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SaatColors.PureWhite,
                border = BorderStroke(1.dp, Color(0xFFEBE7DF)),
                shadowElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.text_size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )

                    // Live Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SaatColors.ScreenBackground)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                            fontSize = (24 * state.fontScale).sp,
                            fontWeight = FontWeight.Normal,
                            color = SaatColors.Slate900,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Slider with Small/Large A labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "A",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SaatColors.Slate500
                        )
                        Slider(
                            value = state.fontScale,
                            onValueChange = onFontScaleChange,
                            valueRange = 0.85f..2.0f,
                            steps = 11,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = SaatColors.DeepEmerald,
                                activeTrackColor = SaatColors.DeepEmerald,
                                inactiveTrackColor = SaatColors.SoftGrey
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "A",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.Slate900
                        )
                    }
                }
            }

            // Section 2: Display & Content Toggles
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SaatColors.PureWhite,
                border = BorderStroke(1.dp, Color(0xFFEBE7DF)),
                shadowElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    ReaderSettingToggleItem(
                        title = stringResource(R.string.show_translation),
                        subtitle = "Tampilkan arti ayat dalam Bahasa Indonesia",
                        checked = state.showTranslation,
                        onCheckedChange = onToggleTranslation
                    )
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2EFE9)))
                    ReaderSettingToggleItem(
                        title = stringResource(R.string.show_transliteration),
                        subtitle = "Tampilkan teks latin / transliterasi",
                        checked = state.showTransliteration,
                        onCheckedChange = onToggleTransliteration
                    )
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2EFE9)))
                    ReaderSettingToggleItem(
                        title = "Warna Tajwid",
                        subtitle = "Panduan hukum bacaan dengan warna tajwid",
                        checked = state.isTajweedEnabled,
                        onCheckedChange = onToggleTajweed
                    )
                }
            }

            // Section 3: Audio Playback & Reciter
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SaatColors.PureWhite,
                border = BorderStroke(1.dp, Color(0xFFEBE7DF)),
                shadowElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Audio & Tilawah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )

                    ReaderSettingToggleItem(
                        title = stringResource(R.string.continuous_play),
                        subtitle = "Lanjut putar ayat berikutnya secara otomatis",
                        checked = state.playbackMode == AyahPlaybackMode.CONTINUOUS,
                        onCheckedChange = { enabled ->
                            onSetPlaybackMode(
                                if (enabled) AyahPlaybackMode.CONTINUOUS else AyahPlaybackMode.SINGLE
                            )
                        }
                    )

                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2EFE9)))

                    Text(
                        text = stringResource(R.string.reciter),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.Slate800
                    )

                    if (state.recitations.isEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp),
                            color = SaatColors.DeepEmerald
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.recitations.forEach { recitation ->
                                ReciterRow(
                                    recitation = recitation,
                                    selected = recitation.identifiableId == state.selectedRecitationId,
                                    onClick = { onSelectRecitation(recitation.identifiableId) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReaderSettingToggleItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = SaatColors.Slate900,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(
                if (checked) R.drawable.ic_toggle_on_custom else R.drawable.ic_toggle_off_custom
            ),
            contentDescription = if (checked) "On" else "Off",
            modifier = Modifier.size(width = 48.dp, height = 26.dp)
        )
    }
}

@Composable
private fun ReciterRow(
    recitation: RecitationPayload,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) SaatColors.DeepEmerald.copy(alpha = 0.08f) else SaatColors.ScreenBackground,
        border = BorderStroke(
            1.dp,
            if (selected) SaatColors.DeepEmerald.copy(alpha = 0.4f) else Color.Transparent
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = recitation.displayName,
                modifier = Modifier.weight(1f),
                color = if (selected) SaatColors.DeepEmerald else SaatColors.Slate800,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
            if (selected) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_check_custom),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
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
