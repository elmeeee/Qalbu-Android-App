package app.kamy.qalbuApp.features.quran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.components.AlKhatibErrorState
import app.kamy.qalbuApp.design.components.AlKhatibPullToRefresh
import app.kamy.qalbuApp.design.components.OfflineBanner
import app.kamy.qalbuApp.design.components.ChapterRowSkeleton
import app.kamy.qalbuApp.design.components.AlKhatibRevelationChip
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.QuranJuz
import app.kamy.qalbuApp.domain.model.ReadingSession
import app.kamy.qalbuApp.domain.model.SearchNavResult
import app.kamy.qalbuApp.domain.model.SearchVerseResult
import app.kamy.qalbuApp.ui.common.rememberErrorDisplay
import app.kamy.qalbuApp.ui.layout.floatingNavBottomPadding
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    onOpenChapter: (chapter: QuranChapter, initialVerse: Int?) -> Unit,
    onOpenJuz: (juzNumber: Int, verseKey: String?) -> Unit,
    onOpenMushaf: (page: Int) -> Unit = {},
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
    val mushafOpenFailedMsg = stringResource(R.string.mushaf_open_juz_failed)
    val isSearching = state.isSearchActive && state.searchQuery.isNotBlank()
    val activeQuery = state.searchQuery.normalizedSearchQuery()
    val hasSearchResults = state.verseRef != null ||
        state.mushafPageRef != null ||
        state.localSearchChapters.isNotEmpty() ||
        state.remoteNavigation.isNotEmpty() ||
        state.remoteVerses.isNotEmpty()

    fun openVerse(chapterNumber: Int, ayah: Int) {
        vm.chapterForNumber(chapterNumber)?.let { chapter ->
            onOpenChapter(chapter, ayah)
            vm.clearSearch()
        }
    }

    fun openNavResult(result: SearchNavResult) {
        val verseRef = state.verseRef
        when (result.type) {
            "surah" -> result.chapterNumber?.let { chapterNum ->
                val ayah = verseRef?.takeIf { it.chapter == chapterNum }?.ayah ?: 1
                openVerse(chapterNum, ayah)
            }
            "juz" -> result.key.toIntOrNull()?.let { juzNumber ->
                vm.openJuz(juzNumber, onOpenJuz)
            }
            "page" -> result.key.toIntOrNull()?.let { page ->
                onOpenMushaf(page.coerceIn(1, app.kamy.qalbuApp.infrastructure.preferences.MushafReadingStore.totalPages))
                vm.clearSearch()
            }
            else -> result.chapterNumber?.let { chapterNum ->
                val ayah = verseRef?.takeIf { it.chapter == chapterNum }?.ayah ?: 1
                openVerse(chapterNum, ayah)
            }
        }
    }

    fun openVerseResult(result: SearchVerseResult) {
        openVerse(result.chapterNumber, result.ayahNumber)
    }

    LaunchedEffect(state.mushafOpenFailed) {
        if (state.mushafOpenFailed) {
            snackbarHostState.showSnackbar(mushafOpenFailedMsg)
            vm.clearMushafOpenFailed()
        }
    }

    LaunchedEffect(state.browseMode) {
        if (state.browseMode == QuranBrowseMode.MUSHAF) {
            vm.onMushafVisible()
        }
    }

    LaunchedEffect(Unit) {
        vm.onScreenVisible()
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
                            modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                        )
                        if (index < 9) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            state.error != null && state.chapters.isEmpty() && errorDisplay != null ->
                AlKhatibErrorState(
                    display = errorDisplay,
                    onRetry = { vm.loadAll(force = true) },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = AlKhatibSpacing.screenHorizontal)
                )
            else -> AlKhatibPullToRefresh(
                isRefreshing = isPullRefreshing,
                onRefresh = {
                    scope.launch {
                        isPullRefreshing = true
                        runCatching { vm.refresh(force = true) }
                            .onFailure { t ->
                                val msg = state.error?.apiMessage
                                    ?: t.message
                                    ?: "Couldn't refresh chapters"
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
                                state.remoteNavigation.size +
                                state.remoteVerses.size +
                                if (state.verseRef != null) 1 else 0
                        } else null,
                        searchFocusRequester = searchFocusRequester,
                        onSearchFocusChange = vm::onSearchActiveChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )
                    if (state.isOfflineData) {
                        OfflineBanner(
                            modifier = Modifier.padding(
                                horizontal = AlKhatibSpacing.screenHorizontal,
                                vertical = AlKhatibSpacing.sm
                            )
                        )
                    }
                    key(state.browseMode) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = listBottomPadding)
                    ) {
                        if (!isSearching && state.browseMode == QuranBrowseMode.SURAH) {
                            item(key = "khatam_progress") {
                                KhatamProgressCard(
                                    modifier = Modifier.padding(
                                        horizontal = AlKhatibSpacing.screenHorizontal,
                                        vertical = AlKhatibSpacing.sm
                                    )
                                )
                            }
                            item(key = "bookmarks_link") {
                                TextButton(
                                    onClick = onOpenBookmarks,
                                    modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                                ) {
                                    Icon(Icons.Filled.Bookmark, contentDescription = null, tint = AlKhatibColors.GoldDeep)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.quran_library_title))
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
                                            horizontal = AlKhatibSpacing.screenHorizontal,
                                            vertical = AlKhatibSpacing.sm
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

                            state.mushafPageRef?.let { page ->
                                item(key = "mushaf_page_$page") {
                                    MushafPageResultRow(
                                        page = page,
                                        onOpen = {
                                            onOpenMushaf(page)
                                            vm.clearSearch()
                                        },
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
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                }
                            }

                            if (state.remoteNavigation.isNotEmpty()) {
                                item(key = "label_remote_nav") {
                                    QuranSearchSectionLabel(stringResource(R.string.search_section_navigation))
                                }
                                items(state.remoteNavigation, key = { "nav_${it.type}_${it.key}" }) { result ->
                                    SearchNavResultRow(
                                        result = result,
                                        enabled = when (result.type) {
                                            "surah" -> result.chapterNumber != null
                                            "juz" -> result.key.toIntOrNull() != null
                                            "page" -> result.key.toIntOrNull() != null
                                            else -> false
                                        },
                                        onClick = { openNavResult(result) },
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
                        } else when (state.browseMode) {
                            QuranBrowseMode.SURAH -> {
                                items(state.chapters, key = { "surah_${it.id}" }) { chapter ->
                                    ChapterRow(
                                        chapter = chapter,
                                        onClick = { onOpenChapter(chapter, null) },
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                }
                            }
                            QuranBrowseMode.JUZ -> when {
                                state.juzsLoading && state.juzs.isEmpty() -> {
                                    items(10, key = { "juz_skeleton_$it" }) { index ->
                                        ChapterRowSkeleton(
                                            modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                                        )
                                        if (index < 9) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                            )
                                        }
                                    }
                                }
                                state.juzsError != null && state.juzs.isEmpty() -> {
                                    item(key = "juz_error") {
                                        val juzErrorDisplay = state.juzsError.rememberErrorDisplay(R.string.juz_load_failed)
                                        if (juzErrorDisplay != null) {
                                            AlKhatibErrorState(
                                                display = juzErrorDisplay,
                                                onRetry = { vm.reloadJuzs() },
                                                modifier = Modifier.padding(
                                                    horizontal = AlKhatibSpacing.screenHorizontal,
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
                                            onClick = { onOpenJuz(juz.juzNumber, null) },
                                            modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                        )
                                    }
                                }
                            }
                            QuranBrowseMode.MUSHAF -> {
                                item(key = "mushaf_hero") {
                                    MushafHeroCard(
                                        browse = state.mushafBrowse,
                                        onContinue = { onOpenMushaf(state.mushafBrowse.lastPage) },
                                        modifier = Modifier.padding(
                                            horizontal = AlKhatibSpacing.screenHorizontal,
                                            vertical = AlKhatibSpacing.sm
                                        )
                                    )
                                }
                                item(key = "mushaf_juz_label") {
                                    Text(
                                        text = stringResource(R.string.mushaf_juz_section_title),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(
                                            horizontal = AlKhatibSpacing.screenHorizontal,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                                val mushafJuzs = if (state.juzs.isNotEmpty()) {
                                    state.juzs
                                } else {
                                    (1..30).map { n -> QuranJuz(juzNumber = n, verseMapping = emptyMap()) }
                                }
                                items(mushafJuzs, key = { "mushaf_juz_${it.juzNumber}" }) { juz ->
                                    MushafJuzShortcutRow(
                                        juzNumber = juz.juzNumber,
                                        isLoading = state.openingMushafJuz == juz.juzNumber,
                                        onClick = { vm.openMushafAtJuz(juz.juzNumber, onOpenMushaf) },
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
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
    onSearchFocusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .tabContentStatusBarInset()
            .padding(
                horizontal = AlKhatibSpacing.screenHorizontal,
                vertical = AlKhatibSpacing.md
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "✦",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(AlKhatibSpacing.sm))
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
            QuranBrowseMode.MUSHAF -> stringResource(R.string.quran_subtitle_mushaf)
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
        Spacer(Modifier.height(AlKhatibSpacing.sm))
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
        Spacer(Modifier.height(AlKhatibSpacing.md))
        if (showBrowseTabs) {
            QuranBrowseTabs(
                browseMode = browseMode,
                onBrowseModeChange = onBrowseModeChange,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(AlKhatibSpacing.sm))
        }
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
                placeholder = stringResource(R.string.search_surah_placeholder),
                modifier = Modifier.weight(1f)
            )
            if (isSearching && searchEnabled) {
                TextButton(onClick = onClearSearch) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (showSuggestions && searchEnabled) {
            Spacer(Modifier.height(AlKhatibSpacing.sm))
            QuranSearchSuggestionChips(onSuggestionClick = onSuggestionClick)
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
    val mushafLabel = stringResource(R.string.quran_tab_mushaf)
    val tabShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        AlKhatibColors.SageMist.copy(alpha = 0.5f)
                    )
                )
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuranBrowseTab(
                label = surahLabel,
                selected = browseMode == QuranBrowseMode.SURAH,
                onClick = { onBrowseModeChange(QuranBrowseMode.SURAH) },
                modifier = Modifier.weight(1f),
                shape = tabShape
            )
            QuranBrowseTab(
                label = juzLabel,
                selected = browseMode == QuranBrowseMode.JUZ,
                onClick = { onBrowseModeChange(QuranBrowseMode.JUZ) },
                modifier = Modifier.weight(1f),
                shape = tabShape
            )
            QuranBrowseTab(
                label = mushafLabel,
                selected = browseMode == QuranBrowseMode.MUSHAF,
                onClick = { onBrowseModeChange(QuranBrowseMode.MUSHAF) },
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp)
) {
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "tabText"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.background(
                        Brush.linearGradient(
                            listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)
                        )
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun ContinueReadingCard(
    session: ReadingSession,
    chapter: QuranChapter?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onTap,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AlKhatibColors.Gold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = AlKhatibColors.GoldDeep,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.continue_reading),
                    style = MaterialTheme.typography.labelSmall,
                    color = AlKhatibColors.GoldDeep,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = chapter?.displayComplexName ?: stringResource(R.string.surah_number, session.chapterNumber),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.verse_number, session.verseNumber),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: QuranChapter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChapterNumberBadge(number = chapter.id)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = chapter.displayComplexName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (chapter.displayTranslatedName.isNotEmpty()) {
                    Text(
                        text = chapter.displayTranslatedName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (chapter.revelationLabel.isNotEmpty()) {
                        AlKhatibRevelationChip(
                            label = chapter.revelationLabel,
                            isMeccan = chapter.isMeccan
                        )
                    }
                    chapter.versesCount?.let { count ->
                        Text(
                            text = pluralStringResource(R.plurals.chapter_verse_count, count, count),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            chapter.nameArabic?.takeIf { it.isNotBlank() }?.let { arabic ->
                Spacer(Modifier.width(8.dp))
                Text(
                    text = arabic,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun JuzRow(
    juz: QuranJuz,
    chapter: QuranChapter?,
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
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChapterNumberBadge(number = juz.juzNumber)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.juz_number, juz.juzNumber),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                startLabel?.let { label ->
                    Text(
                        text = stringResource(R.string.juz_starts_at, label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                juz.versesCount?.let { count ->
                    Text(
                        text = pluralStringResource(R.plurals.juz_verse_count, count, count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterNumberBadge(number: Int) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
