@file:Suppress("SpellCheckingInspection")

package app.kamy.saatApp.features.quran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.AlKhatibErrorState
import app.kamy.saatApp.design.components.AlKhatibPullToRefresh
import app.kamy.saatApp.design.components.ChapterRowSkeleton
import app.kamy.saatApp.design.components.AlKhatibRevelationChip
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
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
                            item(key = "bookmarks_link") {
                                MyQuranLibraryCard(
                                    onClick = onOpenBookmarks,
                                    modifier = Modifier.padding(
                                        horizontal = AlKhatibSpacing.screenHorizontal,
                                        vertical = AlKhatibSpacing.sm
                                    )
                                )
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
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
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
                        } else when (state.browseMode) {
                            QuranBrowseMode.SURAH -> {
                                items(state.chapters, key = { "surah_${it.id}" }) { chapter ->
                                    ChapterRow(
                                        chapter = chapter,
                                        onClick = { onOpenChapter(chapter, null) },
                                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
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
                        color = AlKhatibColors.DeepEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        if (showSuggestions && searchEnabled) {
            Spacer(Modifier.height(AlKhatibSpacing.sm))
            QuranSearchSuggestionChips(onSuggestionClick = onSuggestionClick)
        }
        if (showBrowseTabs) {
            Spacer(Modifier.height(AlKhatibSpacing.md))
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
        shape = RoundedCornerShape(18.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, AlKhatibColors.Teal.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AlKhatibColors.MintWash.copy(alpha = 0.5f),
                            AlKhatibColors.PureWhite
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AlKhatibColors.Teal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = AlKhatibColors.Teal,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.continue_reading).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.5.sp
                        ),
                        color = AlKhatibColors.Teal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = chapter?.displayComplexName ?: stringResource(R.string.surah_number, session.chapterNumber),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = stringResource(R.string.verse_number, session.verseNumber),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlKhatibColors.Slate500
                    )
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AlKhatibColors.Teal.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: QuranChapter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val meanings = stringArrayResource(R.array.chapter_meanings)
    val meaning = remember(meanings, chapter.id) {
        meanings.getOrNull(chapter.id - 1) ?: chapter.displayTranslatedName
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChapterNumberBadge(number = chapter.id)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = chapter.displayComplexName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.Slate900
                )
                if (meaning.isNotEmpty()) {
                    Text(
                        text = meaning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlKhatibColors.Slate500,
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
                        AlKhatibRevelationChip(
                            label = chapter.revelationLabel,
                            isMeccan = chapter.isMeccan
                        )
                    }
                    chapter.versesCount?.let { count ->
                        Text(
                            text = pluralStringResource(R.plurals.chapter_verse_count, count, count),
                            style = MaterialTheme.typography.labelMedium,
                            color = AlKhatibColors.Slate500
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AlKhatibColors.Teal.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
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
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.5f))
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
                Text(
                    text = stringResource(R.string.juz_number, juz.juzNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.Slate900
                )
                startLabel?.let { label ->
                    Text(
                        text = stringResource(R.string.juz_starts_at, label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlKhatibColors.Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                juz.versesCount?.let { count ->
                    Text(
                        text = pluralStringResource(R.plurals.juz_verse_count, count, count),
                        style = MaterialTheme.typography.labelMedium,
                        color = AlKhatibColors.Teal,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AlKhatibColors.Teal.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
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
