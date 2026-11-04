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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import app.kamy.saatApp.domain.model.QuranReadingStyle
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.flow.distinctUntilChanged
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
import app.kamy.saatApp.ui.components.CoachMarkGesture
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

    val pagerState = rememberPagerState(initialPage = calculatedInitialPage) { totalPageCount.coerceAtLeast(1) }

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
    val latestVerseCount by rememberUpdatedState(verseCount)
    val latestPageOffset by rememberUpdatedState(pageOffset)
    val latestTotalPageCount by rememberUpdatedState(totalPageCount)

    LaunchedEffect(pagerState) {
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
                if (snap.isScrolling && showScrollHint) {
                    dismissScrollHint()
                }
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

    LaunchedEffect(showScrollHint) {
        if (showScrollHint) {
            kotlinx.coroutines.delay(6500)
            dismissScrollHint()
        }
    }

    LaunchedEffect(verseCount, pageOffset) {
        if (verseCount == 0) return@LaunchedEffect
        val lastIndex = verseCount - 1 + pageOffset
        if (pagerState.currentPage > lastIndex) {
            pagerState.scrollToPage(lastIndex)
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
                            runCatching { pagerState.scrollToPage(target) }
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

    val scrollListState = rememberLazyListState()

    // Sync scroll list when target verse index changes (e.g. from bookmark, last read, or switching mode)
    LaunchedEffect(state.currentVerseIndex) {
        if (state.readingStyle == QuranReadingStyle.SCROLL && state.verses.isNotEmpty()) {
            val targetIndex = (state.currentVerseIndex + 1).coerceIn(0, state.verses.size)
            if (kotlin.math.abs(scrollListState.firstVisibleItemIndex - targetIndex) > 1) {
                scrollListState.scrollToItem(targetIndex)
            }
        }
    }

    // Auto-scroll in scroll mode when audio plays and advances
    val playingVerseKey = state.currentlyPlayingVerseKey
    LaunchedEffect(playingVerseKey) {
        if (state.readingStyle == QuranReadingStyle.SCROLL && !playingVerseKey.isNullOrBlank() && state.verses.isNotEmpty()) {
            val idx = state.verses.indexOfFirst { it.verseKey == playingVerseKey }
            if (idx >= 0) {
                val targetIndex = idx + 1
                if (kotlin.math.abs(scrollListState.firstVisibleItemIndex - targetIndex) > 1) {
                    runCatching { scrollListState.animateScrollToItem(targetIndex) }
                }
            }
        }
    }

    // Track last read & visible ayah during vertical scrolling
    LaunchedEffect(scrollListState) {
        snapshotFlow { scrollListState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstVisible ->
                if (state.readingStyle == QuranReadingStyle.SCROLL && state.verses.isNotEmpty()) {
                    val verseIdx = (firstVisible - 1).coerceIn(0, (state.verses.size - 1).coerceAtLeast(0))
                    vm.onPageChanged(verseIdx)
                }
            }
    }

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
            if (state.readingStyle == QuranReadingStyle.SCROLL) {
                ContinuousScrollReader(
                    state = state,
                    listState = scrollListState,
                    audioBarVisible = audioBarVisible,
                    audioPlaybackState = audioPlaybackState,
                    onPlay = vm::onTapAyah,
                    onBookmark = vm::toggleBookmark,
                    onTafsir = vm::openTafsir,
                    onAiShare = vm::openAiShare,
                    onTajweedClick = { activeTajweedType.value = it },
                    onLoadPrevious = vm::loadPreviousSurahOrJuz,
                    onLoadNext = vm::loadNextSurahOrJuz
                )
            } else {
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
                                showTranslation = state.showTranslation,
                                showTransliteration = state.showTransliteration,
                                translationId = state.selectedTranslationId,
                                isTajweedEnabled = state.isTajweedEnabled,
                                arabicTextType = state.arabicTextType,
                                audioBarVisible = audioBarVisible,
                                personalDataRevision = state.personalDataRevision,
                                showBismillahPre = verseIdx == 0 && state.bismillahPre,
                                audioPlaybackState = audioPlaybackState,
                                onPlay = { vm.onTapAyah(verseIdx) },
                                onContentScroll = if (verseIdx == 0) ::dismissScrollHint else null,
                                onTajweedClick = { activeTajweedType.value = it },
                                modifier = if (verseIdx == 0) {
                                    Modifier.coachMarkTarget(
                                        state = coachMarkState,
                                        step = 3,
                                        titleRes = R.string.coach_mark_quran_swipe_title,
                                        descriptionRes = R.string.coach_mark_quran_swipe_desc,
                                        gesture = CoachMarkGesture.SWIPE_HORIZONTAL
                                    )
                                } else Modifier
                            )
                        }
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

                if (state.readingStyle == QuranReadingStyle.SCROLL) {
                    IconButton(
                        onClick = { settingsVisible.value = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aa",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                            vm.toggleBookmark(currentVerseIndex)
                        },
                        modifier = Modifier.coachMarkTarget(
                            state = coachMarkState,
                            step = 2,
                            titleRes = R.string.coach_mark_quran_bookmark_title,
                            descriptionRes = R.string.coach_mark_quran_bookmark_desc,
                            gesture = CoachMarkGesture.TAP
                        )
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
        }

        val showBottomFloatingBar = state.readingStyle != QuranReadingStyle.SCROLL || audioPlaybackState.currentUrl != null
        if (showBottomFloatingBar) {
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
                    horizontalArrangement = if (state.readingStyle == QuranReadingStyle.SCROLL) Arrangement.Center else Arrangement.SpaceBetween
                ) {
                    if (state.readingStyle != QuranReadingStyle.SCROLL) {
                        // Left Action: Menu Book / Actions popup
                        Column(
                            horizontalAlignment = Alignment.Start
                ) {
                    if (state.verses.isNotEmpty()) {
                        ReaderVerseActionsMenu(
                            expanded = verseMenuExpanded.value,
                            onToggle = { verseMenuExpanded.value = !verseMenuExpanded.value },
                            bookmarked = state.currentVerseBookmarked,
                            showTafsir = LocalQuranConfig.supportsTafsir(state.selectedTranslationId),
                            onBookmark = {
                                verseMenuExpanded.value = false
                                vm.toggleBookmark(currentVerseIndex)
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
                            .coachMarkTarget(
                                state = coachMarkState,
                                step = 0,
                                titleRes = R.string.coach_mark_quran_menu_title,
                                descriptionRes = R.string.coach_mark_quran_menu_desc,
                                gesture = CoachMarkGesture.TAP
                            )
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
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        shadowElevation = 0.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .height(48.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = Color.Black.copy(alpha = 0.15f),
                                spotColor = Color.Black.copy(alpha = 0.12f)
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

                if (state.readingStyle != QuranReadingStyle.SCROLL) {
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
                            .coachMarkTarget(
                                state = coachMarkState,
                                step = 1,
                                titleRes = R.string.coach_mark_quran_settings_title,
                                descriptionRes = R.string.coach_mark_quran_settings_desc,
                                gesture = CoachMarkGesture.TAP
                            )
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
            onArabicTextTypeChange = vm::setArabicTextType,
            onReadingStyleChange = vm::setReadingStyle
        )
    }

    activeTajweedType.value?.let { type ->
        TajweedInfoSheet(
            type = type,
            onDismiss = { activeTajweedType.value = null }
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
        onShare = { draft: String ->
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
    audioBarVisible: Boolean,
    personalDataRevision: Int,
    showBismillahPre: Boolean,
    audioPlaybackState: AudioPlaybackState? = null,
    onPlay: () -> Unit,
    onContentScroll: (() -> Unit)? = null,
    onTajweedClick: (TajweedType) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentTopPadding = statusBarTop + 68.dp
    val contentBottomPadding = if (audioBarVisible) 160.dp else 110.dp
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState, onContentScroll) {
        if (onContentScroll == null) return@LaunchedEffect
        snapshotFlow { scrollState.value }
            .collect { offset ->
                if (offset > 12) onContentScroll()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onPlay() })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
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
            if (showTransliteration) {
                verse.displayTransliteration(translationId)?.let { transliteration ->
                    Text(
                        text = transliteration,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = (22 * fontScale).sp,
                            fontSize = (15 * fontScale).sp
                        ),
                        color = SaatColors.DeepEmerald,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }
            }
            if (showTranslation) {
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
    modifier: Modifier = Modifier,
    showTafsir: Boolean = true,
    onBookmark: () -> Unit,
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
    onArabicTextTypeChange: (ArabicTextType) -> Unit,
    onReadingStyleChange: (QuranReadingStyle) -> Unit
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

            // Section 0: Quran Reading Style (Gaya Membaca Qur'an)
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
                        text = stringResource(R.string.reading_style_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                    Text(
                        text = stringResource(R.string.reading_style_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate500
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReadingStyleCard(
                            title = stringResource(R.string.reading_style_pager),
                            desc = stringResource(R.string.reading_style_pager_desc),
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            selected = state.readingStyle == QuranReadingStyle.PAGER,
                            onClick = { onReadingStyleChange(QuranReadingStyle.PAGER) },
                            modifier = Modifier.weight(1f)
                        )
                        ReadingStyleCard(
                            title = stringResource(R.string.reading_style_scroll),
                            desc = stringResource(R.string.reading_style_scroll_desc),
                            icon = Icons.Filled.AutoStories,
                            selected = state.readingStyle == QuranReadingStyle.SCROLL,
                            onClick = { onReadingStyleChange(QuranReadingStyle.SCROLL) },
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                        subtitle = stringResource(R.string.reader_show_translation_subtitle),
                        checked = state.showTranslation,
                        onCheckedChange = onToggleTranslation
                    )
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2EFE9)))
                    ReaderSettingToggleItem(
                        title = stringResource(R.string.show_transliteration),
                        subtitle = stringResource(R.string.reader_show_transliteration_subtitle),
                        checked = state.showTransliteration,
                        onCheckedChange = onToggleTransliteration
                    )
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2EFE9)))
                    ReaderSettingToggleItem(
                        title = stringResource(R.string.reader_tajweed_colors),
                        subtitle = stringResource(R.string.reader_tajweed_colors_subtitle),
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
                        text = stringResource(R.string.reader_audio_tilawah),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )

                    ReaderSettingToggleItem(
                        title = stringResource(R.string.continuous_play),
                        subtitle = stringResource(R.string.reader_continuous_play_subtitle),
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
    val avatarRes = app.kamy.saatApp.core.config.LocalQuranConfig.reciterAvatar(recitation.identifiableId)
    val subtitleRes = app.kamy.saatApp.core.config.LocalQuranConfig.reciterSubtitleRes(recitation.identifiableId)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) SaatColors.DeepEmerald.copy(alpha = 0.07f) else Color(0xFFFAFAFA),
        border = BorderStroke(
            1.2.dp,
            if (selected) SaatColors.DeepEmerald else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8F4F7)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(avatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recitation.displayName,
                    color = if (selected) SaatColors.DeepEmerald else SaatColors.Slate900,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (subtitleRes != null) {
                    Text(
                        text = stringResource(subtitleRes),
                        color = if (selected) SaatColors.DeepEmerald.copy(alpha = 0.8f) else SaatColors.Slate500,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(SaatColors.DeepEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_custom),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun ReaderScrollHint(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "swipeHint")
    val swipeOffset by transition.animateFloat(
        initialValue = 16f,
        targetValue = -16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeOffset"
    )
    val handAlpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handAlpha"
    )

    Surface(
        onClick = onDismiss,
        modifier = modifier
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 14.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF085E43),
                            Color(0xFF15AA7C)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .border(
                    BorderStroke(1.2.dp, Color(0xFFE8D39A).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 16.dp, vertical = 11.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Animated Horizontal Swipe Visual Track
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(28.dp)
                        .background(Color.Black.copy(alpha = 0.20f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(11.dp)
                        )
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(11.dp)
                                .graphicsLayer { rotationZ = 180f }
                        )
                    }
                    // Animated glowing touch dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer {
                                translationX = swipeOffset
                                alpha = handAlpha
                            }
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFDF7A), Color(0xFFC9972E))
                                ),
                                CircleShape
                            )
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }

                Text(
                    text = stringResource(R.string.reader_scroll_hint),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Start
                )
            }
        }
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

@Composable
private fun ContinuousScrollReader(
    state: ChapterReaderUiState,
    listState: LazyListState,
    audioBarVisible: Boolean,
    audioPlaybackState: AudioPlaybackState?,
    onPlay: (Int) -> Unit,
    onBookmark: (Int) -> Unit,
    onTafsir: (String) -> Unit,
    onAiShare: (Int) -> Unit,
    onTajweedClick: (TajweedType) -> Unit,
    onLoadPrevious: () -> Unit,
    onLoadNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentTopPadding = statusBarTop + 68.dp
    val contentBottomPadding = if (audioBarVisible) 160.dp else 110.dp

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = contentTopPadding, bottom = contentBottomPadding),
        modifier = modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
    ) {
        // Header: Previous Surah/Juz button + Surah Title Banner + Bismillah
        item(key = "header") {
            val prevChapterNum = (state.chapterNumber - 1).coerceAtLeast(1)
            val prevSurahName = state.chapterLookup[prevChapterNum]
            if (state.chapterNumber > 1 || (state.juzNumber != null && state.juzNumber > 1)) {
                Surface(
                    onClick = onLoadPrevious,
                    shape = RoundedCornerShape(14.dp),
                    color = SaatColors.PureWhite,
                    border = BorderStroke(1.dp, Color(0xFFEBE7DF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        val text = if (state.juzNumber != null) {
                            val prevJuz = (state.juzNumber - 1).coerceAtLeast(1)
                            stringResource(R.string.transition_prev_juz, prevJuz)
                        } else {
                            val targetSurah = prevSurahName ?: stringResource(R.string.transition_prev_surah_default)
                            stringResource(R.string.transition_prev_surah, targetSurah)
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.DeepEmerald
                        )
                    }
                }
            }

            // Surah Header Banner (Premium Islamic Card)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = SaatColors.DeepEmerald.copy(alpha = 0.30f),
                        spotColor = SaatColors.DeepEmerald.copy(alpha = 0.25f)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF064E3B), // Deep Emerald
                                    Color(0xFF0F766E), // Emerald Teal
                                    Color(0xFF10B981)  // Vibrant Mint
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Badge Row: Surah Number & Revelation Place & Verse count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val surahNumberLabel = if (state.juzNumber != null) {
                                stringResource(R.string.juz_number, state.juzNumber)
                            } else {
                                stringResource(R.string.surah_number, state.chapterNumber)
                            }
                            val revelationPlace = state.chapterRevelationPlace?.takeIf { it.isNotBlank() } ?: "Makkah"

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$surahNumberLabel · $revelationPlace".uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.95f),
                                    letterSpacing = 0.6.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${state.verses.size} ${stringResource(R.string.verses)}".uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.95f),
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Arabic Surah Name Calligraphy
                        state.chapterArabicName?.takeIf { it.isNotBlank() }?.let { arabicName ->
                            Text(
                                text = arabicName,
                                fontFamily = TajweedFontFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        // Latin Surah Display Name
                        Text(
                            text = state.chapterDisplayName.orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        // Surah Meaning / Translation Name
                        state.chapterTranslatedName?.takeIf { it.isNotBlank() }?.let { meaning ->
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "($meaning)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Calligraphic Bismillah for all Surahs except At-Tawbah (9)
            if (state.chapterNumber != 9) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                        fontFamily = TajweedFontFamily,
                        fontSize = (32 * state.fontScale).sp,
                        fontWeight = FontWeight.Normal,
                        color = SaatColors.DeepEmerald,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        SaatColors.DeepEmerald.copy(alpha = 0.40f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }

        // Verse Items
        itemsIndexed(
            items = state.verses,
            key = { _, verse -> verse.verseKey.orEmpty() }
        ) { index, verse ->
            ContinuousVerseRow(
                verse = verse,
                index = index,
                fontScale = state.fontScale,
                showTranslation = state.showTranslation,
                showTransliteration = state.showTransliteration,
                translationId = state.selectedTranslationId,
                isTajweedEnabled = state.isTajweedEnabled,
                arabicTextType = state.arabicTextType,
                personalDataRevision = state.personalDataRevision,
                audioPlaybackState = audioPlaybackState,
                onPlay = { onPlay(index) },
                onBookmark = { onBookmark(index) },
                onTafsir = { onTafsir(verse.verseKey.orEmpty()) },
                onAiShare = { onAiShare(index) },
                onTajweedClick = onTajweedClick
            )
        }

        // Footer: Next Surah/Juz button
        item(key = "footer") {
            val nextChapterNum = state.chapterNumber + 1
            val nextSurahName = state.chapterLookup[nextChapterNum]
            if (state.chapterNumber < 114 || (state.juzNumber != null && state.juzNumber < 30)) {
                Surface(
                    onClick = onLoadNext,
                    shape = RoundedCornerShape(16.dp),
                    color = SaatColors.DeepEmerald,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val text = if (state.juzNumber != null) {
                            val nextJuz = (state.juzNumber + 1).coerceAtMost(30)
                            stringResource(R.string.transition_next_juz, nextJuz)
                        } else {
                            val targetSurah = nextSurahName ?: stringResource(R.string.transition_next_surah_default)
                            stringResource(R.string.transition_next_surah, targetSurah)
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.PureWhite
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = SaatColors.PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ContinuousVerseRow(
    verse: RandomAyahPayload,
    index: Int,
    fontScale: Float,
    showTranslation: Boolean,
    showTransliteration: Boolean,
    translationId: Int,
    isTajweedEnabled: Boolean,
    arabicTextType: ArabicTextType,
    personalDataRevision: Int,
    audioPlaybackState: AudioPlaybackState?,
    onPlay: () -> Unit,
    onBookmark: () -> Unit,
    onTafsir: () -> Unit,
    onAiShare: () -> Unit,
    onTajweedClick: (TajweedType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBookmarked = remember(verse.verseKey, personalDataRevision) {
        val key = verse.verseKey
        if (key != null) QuranPersonalStore.isBookmarked(context, key) else false
    }
    val isPlayingThisVerse = audioPlaybackState != null &&
        audioPlaybackState.trackSubtitle == verse.verseKey &&
        audioPlaybackState.currentUrl != null

    val backgroundColor = if (isPlayingThisVerse) {
        SaatColors.DeepEmerald.copy(alpha = 0.06f)
    } else {
        Color.Transparent
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Toolbar: Ayah Number Badge + Badges + Actions (Play, Bookmark, Tafsir, Share)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SaatColors.DeepEmerald.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = verse.resolvedVerseNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }

                VersePersonalBadges(
                    verseKey = verse.verseKey,
                    personalDataRevision = personalDataRevision,
                    compact = true
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlayingThisVerse && audioPlaybackState?.isPlaying == true) {
                            Icons.Filled.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        },
                        contentDescription = if (isPlayingThisVerse && audioPlaybackState?.isPlaying == true) {
                            stringResource(R.string.pause)
                        } else {
                            stringResource(R.string.play)
                        },
                        tint = if (isPlayingThisVerse) SaatColors.DeepEmerald else SaatColors.Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bookmark_custom),
                        contentDescription = stringResource(
                            if (isBookmarked) R.string.remove_bookmark else R.string.bookmark
                        ),
                        tint = if (isBookmarked) SaatColors.DeepEmerald else SaatColors.Slate500,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onTafsir,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tafsir),
                        contentDescription = stringResource(R.string.tafsir),
                        tint = SaatColors.Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onAiShare,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = stringResource(R.string.share_ayah),
                        tint = SaatColors.Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Arabic text with Tajweed and word sync
        val textToRender = if (arabicTextType == ArabicTextType.INDOPAK) {
            verse.textIndopak ?: verse.textUthmani
        } else {
            verse.textUthmani
        }

        val (words, weights, totalWeight) = remember(textToRender) {
            val parsedWords = textToRender?.split("\\s+".toRegex())
                ?.filter { it.isNotBlank() }
                ?.map { word ->
                    word.replace("""^[﴿\(]?\d+[﴾\)]?""".toRegex(), "").trim()
                }
                ?.filter { it.isNotBlank() } ?: emptyList()

            val parsedWeights = parsedWords.mapIndexed { wIdx, word ->
                var weight = word.length.toFloat().coerceAtLeast(1f)
                if (word.contains('ٓ') || word.contains('ۤ') || word.contains('ۧ') || word.contains('ۨ') || word.contains('\u0653') || word.contains('\u06E4')) {
                    weight += 8.0f
                }
                if (word.contains('ّ') || word.contains('\u0651')) {
                    if (word.contains('ن') || word.contains('م')) {
                        weight += 4.0f
                    } else {
                        weight += 2.0f
                    }
                }
                if (word.contains('ۘ') || word.contains('ۚ') || word.contains('ۖ') || word.contains('ۗ') || word.contains('۬') || word.contains('ۥ') || word.contains('ۙ')) {
                    weight += 6.0f
                }
                if (wIdx == parsedWords.lastIndex && parsedWords.size > 1) {
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
                    val rawProg = if (dur > 0L) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                    else (audioPlaybackState?.progress ?: 0f).coerceIn(0f, 1f)
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
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )

        // Transliteration
        if (showTransliteration) {
            verse.displayTransliteration(translationId)?.let { transliteration ->
                Text(
                    text = transliteration,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = (22 * fontScale).sp,
                        fontSize = (15 * fontScale).sp
                    ),
                    color = SaatColors.DeepEmerald,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }

        // Translation
        if (showTranslation) {
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
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEBE7DF).copy(alpha = 0.7f))
        )
    }
}

@Composable
private fun ReadingStyleCard(
    title: String,
    desc: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) SaatColors.DeepEmerald.copy(alpha = 0.08f) else SaatColors.ScreenBackground,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) SaatColors.DeepEmerald else Color(0xFFEBE7DF)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) SaatColors.DeepEmerald else SaatColors.Slate500,
                    modifier = Modifier.size(22.dp)
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) SaatColors.DeepEmerald else SaatColors.Slate900
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}
