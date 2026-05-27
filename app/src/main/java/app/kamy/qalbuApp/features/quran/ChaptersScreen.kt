package app.kamy.qalbuApp.features.quran

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.design.components.AlKhatibPullToRefresh
import app.kamy.qalbuApp.design.components.ChapterRowSkeleton
import app.kamy.qalbuApp.design.components.AlKhatibRevelationChip
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.ReadingSession
import app.kamy.qalbuApp.ui.layout.floatingNavBottomPadding
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    onOpenChapter: (chapter: QuranChapter, initialVerse: Int?) -> Unit
) {
    val vm: ChaptersViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    var isPullRefreshing by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val listBottomPadding = floatingNavBottomPadding()
    val filteredChapters = remember(state.chapters, searchQuery) {
        state.chapters.filteredBySearch(searchQuery)
    }
    val isSearching = searchQuery.isNotBlank()

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
                        searchEnabled = false,
                        resultCount = null,
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
            state.error != null && state.chapters.isEmpty() ->
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            else -> AlKhatibPullToRefresh(
                isRefreshing = isPullRefreshing,
                onRefresh = {
                    scope.launch {
                        isPullRefreshing = true
                        runCatching { vm.refresh(force = true) }
                        isPullRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(Modifier.fillMaxSize()) {
                    QuranListHeader(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchEnabled = true,
                        resultCount = if (isSearching) filteredChapters.size else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = listBottomPadding)
                    ) {
                        if (!isSearching) {
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

                        if (isSearching && filteredChapters.isEmpty()) {
                            item(key = "search_empty") {
                                QuranSearchEmptyState(query = searchQuery)
                            }
                        }

                        items(filteredChapters, key = { it.id }) { chapter ->
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
                }
            }
        }
    }
}

@Composable
private fun QuranListHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchEnabled: Boolean,
    resultCount: Int?,
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
                text = "Quran",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        val subtitle = when (resultCount) {
            null -> "114 Surahs · The Noble Quran"
            0 -> "No matches"
            1 -> "1 surah found"
            else -> "$resultCount surahs found"
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
        QuranChapterSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            enabled = searchEnabled
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
                    text = "Continue reading",
                    style = MaterialTheme.typography.labelSmall,
                    color = AlKhatibColors.GoldDeep,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = chapter?.displayComplexName ?: "Surah ${session.chapterNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ayah ${session.verseNumber}",
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
                    chapter.versesCountLabel?.let {
                        Text(
                            text = it,
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
