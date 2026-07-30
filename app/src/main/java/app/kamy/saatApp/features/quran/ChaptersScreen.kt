@file:Suppress("SpellCheckingInspection")

package app.kamy.saatApp.features.quran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.SaatErrorState
import app.kamy.saatApp.design.components.SaatPullToRefresh
import app.kamy.saatApp.design.components.ChapterRowSkeleton
import app.kamy.saatApp.design.components.SaatRevelationChip
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.model.QuranChapter
import app.kamy.saatApp.domain.model.QuranJuz
import app.kamy.saatApp.domain.model.ReadingSession
import app.kamy.saatApp.domain.model.SearchVerseResult
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    onOpenChapter: (chapter: QuranChapter, initialVerse: Int?) -> Unit,
    onOpenJuz: (juzNumber: Int, verseKey: String?) -> Unit,
    onOpenBookmarks: () -> Unit = {}
) {
    val vm: ChaptersViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    val searchFocusRequester = remember { FocusRequester() }
    var isPullRefreshing by remember { mutableStateOf(false) }
    val listBottomPadding = floatingNavBottomPadding()
    val errorDisplay = state.error.rememberErrorDisplay(R.string.chapters_load_failed)
    val snackbarHostState = remember { SnackbarHostState() }
    val chaptersRefreshFailed = stringResource(R.string.chapters_refresh_failed)
    val isSearching = state.isSearchActive && state.searchQuery.isNotBlank()
    val activeQuery = state.searchQuery.normalizedSearchQuery()
    val hasSearchResults = state.verseRef != null ||
        state.juzRef != null ||
        state.localSearchChapters.isNotEmpty() ||
        state.remoteVerses.isNotEmpty()

    fun openVerse(chapterNumber: Int, ayah: Int) {
        vm.chapterForNumber(chapterNumber)?.let { chapter ->
            onOpenChapter(chapter, ayah)
            vm.clearSearch()
        }
    }

    fun openVerseResult(result: SearchVerseResult) {
        openVerse(result.chapterNumber, result.ayahNumber)
    }

    // Use LifecycleResumeEffect so this fires every time the user returns to this screen
    // (e.g. after reading a surah), not just the first time the composable enters composition.
    LifecycleResumeEffect(vm) {
        vm.onScreenVisible()
        onPauseOrDispose { }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        vm.reviewFlow.collect {
            val act = context as? android.app.Activity
            if (act != null) {
                app.kamy.saatApp.infrastructure.review.AppReviewManager.launchReviewFlow(act)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading && state.chapters.isEmpty() && !isPullRefreshing ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .tabContentStatusBarInset()
                ) {
                    QuranListHeader(
                        searchQuery = "",
                        onSearchQueryChange = {},
                        onClearSearch = {},
                        searchEnabled = false,
                        isSearching = false,
                        browseMode = state.browseMode,
                        onBrowseModeChange = vm::setBrowseMode,
                        showBrowseTabs = false,
                        showSuggestions = false,
                        onSuggestionClick = {},
                        resultCount = null,
                        searchFocusRequester = searchFocusRequester,
                        onSearchFocusChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )
                    repeat(10) { index ->
                        ChapterRowSkeleton(
                            modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal)
                        )
                        if (index < 9) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            state.error != null && state.chapters.isEmpty() && errorDisplay != null ->
                SaatErrorState(
                    display = errorDisplay,
                    onRetry = { vm.loadAll(force = true) },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = SaatSpacing.screenHorizontal)
                )
            else -> SaatPullToRefresh(
                isRefreshing = isPullRefreshing,
                onRefresh = {
                    scope.launch {
                        isPullRefreshing = true
                        runCatching { vm.refresh(force = true) }
                            .onFailure { t ->
                                val msg = state.error?.apiMessage
                                    ?: t.message
                                    ?: chaptersRefreshFailed
                                snackbarHostState.showSnackbar(msg)
                            }
                        isPullRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(Modifier.fillMaxSize()) {
                    QuranListHeader(
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = vm::onSearchQueryChange,
                        onClearSearch = vm::clearSearch,
                        searchEnabled = true,
                        isSearching = isSearching,
                        browseMode = state.browseMode,
                        onBrowseModeChange = vm::setBrowseMode,
                        showBrowseTabs = !isSearching,
                        showSuggestions = state.isSearchActive && activeQuery.isEmpty(),
                        onSuggestionClick = { suggestion ->
                            vm.onSearchQueryChange(suggestion)
                            vm.onSearchActiveChange(true)
                            searchFocusRequester.requestFocus()
                        },
                        resultCount = if (isSearching) {
                            state.localSearchChapters.size +
                                state.remoteVerses.size +
                                (if (state.verseRef != null) 1 else 0) +
                                (if (state.juzRef != null) 1 else 0)
                        } else null,
                        searchFocusRequester = searchFocusRequester,
                        onSearchFocusChange = vm::onSearchActiveChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )
                    key(state.browseMode) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = listBottomPadding)
                    ) {
                        if (!isSearching && state.browseMode == QuranBrowseMode.SURAH) {
                            if (state.hasBookmarks) {
                                item(key = "bookmarks_link") {
                                    MyQuranLibraryCard(
                                        onClick = onOpenBookmarks,
                                        modifier = Modifier.padding(
                                            horizontal = SaatSpacing.screenHorizontal,
                                            vertical = SaatSpacing.sm
                                        )
                                    )
                                }
                            }
                            state.continueReading?.let { session ->
                                item(key = "continue_reading") {
                                    ContinueReadingCard(
                                        session = session,
                                        chapter = state.chapters.firstOrNull { it.id == session.chapterNumber },
                                        onTap = {
                                            vm.continueReadingTarget()?.let { (c, v) -> onOpenChapter(c, v) }
                                        },
                                        modifier = Modifier.padding(
                                            horizontal = SaatSpacing.screenHorizontal,
                                            vertical = SaatSpacing.sm
                                        )
                                    )
                                }
                            }
                        }

                        if (isSearching) {
                            state.verseRef?.let { ref ->
                                item(key = "verse_ref_${ref.chapter}_${ref.ayah}") {
                                    VerseReferenceResultRow(
                                        reference = ref,
                                        chapter = vm.chapterForNumber(ref.chapter),
                                        onOpen = { chapter, ayah -> openVerse(chapter.id, ayah) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    QuranSearchResultDivider()
                                }
                            }

                            state.juzRef?.let { juzNumber ->
                                item(key = "juz_ref_$juzNumber") {
                                    JuzReferenceResultRow(
                                        juzNumber = juzNumber,
                                        onOpen = { vm.openJuz(juzNumber, onOpenJuz) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    QuranSearchResultDivider()
                                }
                            }

                            if (state.localSearchChapters.isNotEmpty()) {
                                item(key = "label_local_surahs") {
                                    QuranSearchSectionLabel(stringResource(R.string.search_section_surahs))
                                }
                                items(state.localSearchChapters, key = { "local_${it.id}" }) { chapter ->
                                    ChapterRow(
                                        chapter = chapter,
                                        onClick = {
                                            val ayah = state.verseRef
                                                ?.takeIf { it.chapter == chapter.id }
                                                ?.ayah
                                            onOpenChapter(chapter, ayah)
                                            vm.clearSearch()
                                        },
                                        modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                }
                            }

                            if (state.remoteVerses.isNotEmpty()) {
                                item(key = "label_remote_verses") {
                                    QuranSearchSectionLabel(stringResource(R.string.search_section_verses))
                                }
                                items(state.remoteVerses, key = { "verse_${it.verseKey}" }) { result ->
                                    SearchVerseResultRow(
                                        result = result,
                                        chapter = vm.chapterForNumber(result.chapterNumber),
                                        enabled = vm.chapterForNumber(result.chapterNumber) != null,
                                        onClick = { openVerseResult(result) }
                                    )
                                }
                            }

                            if (state.searchLoading) {
                                item(key = "search_loading") {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        QuranSearchLoadingRow()
                                    }
                                }
                            }

                            state.searchError?.let { error ->
                                item(key = "search_error") {
                                    QuranSearchErrorRow(
                                        error = error,
                                        onRetry = { vm.onSearchQueryChange(state.searchQuery) }
                                    )
                                }
                            }

                            if (!state.searchLoading && !hasSearchResults && state.searchError == null) {
                                item(key = "search_empty") {
                                    QuranSearchEmptyState(query = activeQuery)
                                }
                            }
                        } else {
                            when (state.browseMode) {
                                QuranBrowseMode.SURAH -> {
                                    items(state.chapters, key = { "surah_${it.id}" }) { chapter ->
                                        ChapterRow(
                                            chapter = chapter,
                                            isRead = chapter.id in state.readChapters,
                                            onClick = { onOpenChapter(chapter, null) },
                                            modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal)
                                        )
                                    }
                                }
                                QuranBrowseMode.JUZ -> {
                                    item(key = "khatam_header") {
                                        val readCount = state.readJuzs.size
                                        val totalJuzs = 30
                                        val progressFraction = readCount.toFloat() / totalJuzs.toFloat()
                                        val percentage = (progressFraction * 100).toInt()
                                        
                                        Surface(
                                            onClick = {
                                                val targetJuz = state.lastReadJuz ?: 1
                                                val targetKey = state.lastReadVerseKey
                                                onOpenJuz(targetJuz, targetKey)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 8.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = androidx.compose.material.icons.Icons.Default.Bookmark,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text(
                                                            text = stringResource(R.string.khatam_progress_title),
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Text(
                                                        text = "$percentage%",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Spacer(Modifier.height(10.dp))
                                                androidx.compose.material3.LinearProgressIndicator(
                                                    progress = { progressFraction },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(4.dp)),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.khatam_progress, readCount),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    state.lastReadJuz?.let { juz ->
                                                        val ayahNum = state.lastReadVerseKey?.substringAfter(":")?.toIntOrNull() ?: 1
                                                        Text(
                                                            text = stringResource(R.string.khatam_last_read, juz, ayahNum),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    when {
                                        state.juzsLoading && state.juzs.isEmpty() -> {
                                            items(10, key = { "juz_skeleton_$it" }) { index ->
                                                ChapterRowSkeleton(
                                                    modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal)
                                                )
                                                if (index < 9) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal),
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                                    )
                                                }
                                            }
                                        }
                                        state.juzsError != null && state.juzs.isEmpty() -> {
                                            item(key = "juz_error") {
                                                val juzErrorDisplay = state.juzsError.rememberErrorDisplay(R.string.juz_load_failed)
                                                if (juzErrorDisplay != null) {
                                                    SaatErrorState(
                                                        display = juzErrorDisplay,
                                                        onRetry = { vm.reloadJuzs() },
                                                        modifier = Modifier.padding(
                                                            horizontal = SaatSpacing.screenHorizontal,
                                                            vertical = 24.dp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        else -> {
                                            items(state.juzs, key = { "juz_${it.juzNumber}" }) { juz ->
                                                JuzRow(
                                                    juz = juz,
                                                    chapter = juz.firstChapterNumber()?.let { vm.chapterForNumber(it) },
                                                    isRead = juz.juzNumber in state.readJuzs,
                                                    onClick = { onOpenJuz(juz.juzNumber, null) },
                                                    modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun QuranListHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    searchEnabled: Boolean,
    isSearching: Boolean,
    browseMode: QuranBrowseMode,
    onBrowseModeChange: (QuranBrowseMode) -> Unit,
    showBrowseTabs: Boolean,
    showSuggestions: Boolean,
    onSuggestionClick: (String) -> Unit,
    resultCount: Int?,
    searchFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onSearchFocusChange: (Boolean) -> Unit = {}
) {
    Column(
        modifier = modifier
            .tabContentStatusBarInset()
            .padding(
                horizontal = SaatSpacing.screenHorizontal,
                vertical = SaatSpacing.md
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "✦",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(SaatSpacing.sm))
            Text(
                text = stringResource(R.string.quran_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        val defaultSubtitle = when (browseMode) {
            QuranBrowseMode.SURAH -> stringResource(R.string.quran_subtitle)
            QuranBrowseMode.JUZ -> stringResource(R.string.quran_subtitle_juz)
        }
        val noMatchesSubtitle = stringResource(R.string.no_matches)
        val oneSurahSubtitle = stringResource(R.string.one_surah_found)
        val subtitle = when (resultCount) {
            null -> defaultSubtitle
            0 -> noMatchesSubtitle
            1 -> oneSurahSubtitle
            else -> stringResource(R.string.search_results_found, resultCount)
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 18.dp, top = 4.dp)
        )
        Spacer(Modifier.height(SaatSpacing.sm))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(48.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                    )
                )
        )
        Spacer(Modifier.height(SaatSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuranChapterSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClear = onClearSearch,
                onFocusChange = onSearchFocusChange,
                onDismiss = onClearSearch,
                enabled = searchEnabled,
                focusRequester = searchFocusRequester,
                placeholder = stringResource(R.string.search_quran_placeholder),
                modifier = Modifier.weight(1f)
            )
            if (isSearching && searchEnabled) {
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = onClearSearch,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge,
                        color = SaatColors.DeepEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        if (showSuggestions && searchEnabled) {
            Spacer(Modifier.height(SaatSpacing.sm))
            QuranSearchSuggestionChips(onSuggestionClick = onSuggestionClick)
        }
        if (showBrowseTabs) {
            Spacer(Modifier.height(SaatSpacing.md))
            QuranBrowseTabs(
                browseMode = browseMode,
                onBrowseModeChange = onBrowseModeChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuranBrowseTabs(
    browseMode: QuranBrowseMode,
    onBrowseModeChange: (QuranBrowseMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val surahLabel = stringResource(R.string.quran_tab_surah)
    val juzLabel = stringResource(R.string.quran_tab_juz)
    val tabShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        SaatColors.DeepEmerald.copy(alpha = 0.08f),
                        SaatColors.GoldDeep.copy(alpha = 0.05f)
                    )
                )
            )
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(26.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuranBrowseTab(
                label = surahLabel,
                selected = browseMode == QuranBrowseMode.SURAH,
                drawableResId = R.drawable.ic_quran_on,
                onClick = { onBrowseModeChange(QuranBrowseMode.SURAH) },
                modifier = Modifier.weight(1f),
                shape = tabShape
            )
            QuranBrowseTab(
                label = juzLabel,
                selected = browseMode == QuranBrowseMode.JUZ,
                drawableResId = R.drawable.ic_tafsir,
                onClick = { onBrowseModeChange(QuranBrowseMode.JUZ) },
                modifier = Modifier.weight(1f),
                shape = tabShape
            )
        }
    }
}

@Composable
private fun QuranBrowseTab(
    label: String,
    selected: Boolean,
    drawableResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(22.dp)
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) SaatColors.DeepEmerald else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else SaatColors.Slate700,
        animationSpec = tween(200),
        label = "tabText"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.clip(shape),
        shape = shape,
        color = bgColor,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(drawableResId),
                contentDescription = null,
                tint = if (selected) Color.White else SaatColors.DeepEmerald,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun ContinueReadingCard(
    session: ReadingSession,
    chapter: QuranChapter?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onTap,
            modifier = Modifier
                .width(349.dp)
                .height(120.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            ) {
                // Background image
                Image(
                    painter = painterResource(R.drawable.last_read_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Text overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 20.dp, end = 160.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.today_continue_reading_title),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = chapter?.displayComplexName ?: stringResource(R.string.surah_number, session.chapterNumber),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.today_continue_reading_verse, session.verseNumber),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: QuranChapter,
    isRead: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val meanings = stringArrayResource(R.array.chapter_meanings)
    val meaning = remember(meanings, chapter.id) {
        meanings.getOrNull(chapter.id - 1) ?: chapter.displayTranslatedName
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .width(345.dp)
                .height(104.dp),
            shape = RoundedCornerShape(16.dp),
            color = SaatColors.PureWhite,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChapterNumberBadge(number = chapter.id)
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chapter.displayComplexName,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isRead) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                contentDescription = stringResource(R.string.khatam_completed),
                                tint = SaatColors.DeepEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (meaning.isNotEmpty()) {
                        Text(
                            text = meaning,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = SaatColors.Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (chapter.revelationLabel.isNotEmpty()) {
                            SaatRevelationChip(
                                label = chapter.revelationLabel,
                                isMeccan = chapter.isMeccan
                            )
                        }
                        chapter.versesCount?.let { count ->
                            Text(
                                text = pluralStringResource(R.plurals.chapter_verse_count, count, count),
                                style = MaterialTheme.typography.labelMedium,
                                color = SaatColors.Slate500
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Image(
                    painter = painterResource(if (chapter.isMeccan) R.drawable.mecca else R.drawable.medina),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}


@Composable
private fun JuzRow(
    juz: QuranJuz,
    chapter: QuranChapter?,
    isRead: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val start = juz.startChapterAndAyah()
    val startLabel = start?.let { (chapterNumber, ayah) ->
        val surahName = chapter?.displayComplexName
            ?: stringResource(R.string.surah_number, chapterNumber)
        "$surahName · ${stringResource(R.string.verse_number, ayah)}"
    }
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChapterNumberBadge(number = juz.juzNumber)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.juz_number, juz.juzNumber),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                    if (isRead) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Check,
                            contentDescription = stringResource(R.string.khatam_completed),
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                startLabel?.let { label ->
                    Text(
                        text = stringResource(R.string.juz_starts_at, label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                juz.versesCount?.let { count ->
                    Text(
                        text = pluralStringResource(R.plurals.juz_verse_count, count, count),
                        style = MaterialTheme.typography.labelMedium,
                        color = SaatColors.Teal,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SaatColors.Teal.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ChapterNumberBadge(number: Int) {
    Box(
        modifier = Modifier.size(38.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.frame_number_icon),
            contentDescription = null,
            tint = SaatColors.Teal,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = if (number >= 100) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = SaatColors.Slate900
        )
    }
}
