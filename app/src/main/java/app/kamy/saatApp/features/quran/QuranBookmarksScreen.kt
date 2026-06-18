package app.kamy.saatApp.features.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.AlKhatibEmptyState
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.domain.model.HifzEntry
import app.kamy.saatApp.domain.model.HifzStatus
import app.kamy.saatApp.domain.model.VerseBookmark
import app.kamy.saatApp.domain.model.VerseNote
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranBookmarksScreen(
    onBack: () -> Unit,
    onOpenVerse: (chapter: Int, ayah: Int) -> Unit
) {
    val context = LocalContext.current
    val counts = rememberQuranLibraryCounts()
    var tab by remember { mutableIntStateOf(0) }
    val bookmarks = remember(counts) { QuranPersonalStore.bookmarks(context) }
    val notes = remember(counts) { QuranPersonalStore.notes(context) }
    val hifz = remember(counts) { QuranPersonalStore.hifzEntries(context) }
    val hifzSummary = remember(counts) { QuranPersonalStore.hifzSummary(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.quran_library_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AlKhatibColors.ScreenBackground
                )
            )
        },
        containerColor = AlKhatibColors.ScreenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = AlKhatibSpacing.screenHorizontal,
                    vertical = AlKhatibSpacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "hero") {
                    QuranLibraryHero(counts = counts)
                }

                item(key = "tabs") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = tab == 0,
                            onClick = { tab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            label = {
                                LibraryTabLabel(
                                    icon = Icons.Filled.Bookmark,
                                    label = stringResource(R.string.bookmarks_title),
                                    count = bookmarks.size
                                )
                            }
                        )
                        SegmentedButton(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            label = {
                                LibraryTabLabel(
                                    icon = Icons.AutoMirrored.Filled.Notes,
                                    label = stringResource(R.string.notes_title),
                                    count = notes.size
                                )
                            }
                        )
                        SegmentedButton(
                            selected = tab == 2,
                            onClick = { tab = 2 },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            label = {
                                LibraryTabLabel(
                                    icon = Icons.Filled.Psychology,
                                    label = stringResource(R.string.hifz_title),
                                    count = hifzSummary.total
                                )
                            }
                        )
                    }
                }

                when (tab) {
                    0 -> {
                        if (bookmarks.isEmpty()) {
                            item(key = "empty_bookmarks") {
                                LibraryEmptyState(
                                    icon = Icons.Filled.Bookmark,
                                    title = stringResource(R.string.bookmarks_empty),
                                    body = stringResource(R.string.quran_library_empty_bookmarks_body)
                                )
                            }
                        } else {
                            items(bookmarks, key = { "bm_${it.verseKey}" }) { bookmark ->
                                BookmarkRow(
                                    bookmark = bookmark,
                                    onClick = { onOpenVerse(bookmark.chapterNumber, bookmark.verseNumber) }
                                )
                            }
                        }
                    }
                    1 -> {
                        if (notes.isEmpty()) {
                            item(key = "empty_notes") {
                                LibraryEmptyState(
                                    icon = Icons.AutoMirrored.Filled.Notes,
                                    title = stringResource(R.string.notes_empty),
                                    body = stringResource(R.string.quran_library_empty_notes_body)
                                )
                            }
                        } else {
                            items(notes, key = { "note_${it.verseKey}" }) { note ->
                                NoteRow(
                                    note = note,
                                    onClick = { onOpenVerse(note.chapterNumber, note.verseNumber) }
                                )
                            }
                        }
                    }
                    else -> {
                        if (hifz.isEmpty()) {
                            item(key = "empty_hifz") {
                                LibraryEmptyState(
                                    icon = Icons.Filled.Psychology,
                                    title = stringResource(R.string.hifz_empty),
                                    body = stringResource(R.string.quran_library_empty_hifz_body)
                                )
                            }
                        } else {
                            if (hifzSummary.total > 0) {
                                item(key = "hifz_stats") {
                                    HifzStatsRow(summary = hifzSummary)
                                }
                            }
                            items(hifz, key = { "hifz_${it.verseKey}" }) { entry ->
                                HifzRow(
                                    entry = entry,
                                    onClick = { onOpenVerse(entry.chapterNumber, entry.verseNumber) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryTabLabel(
    icon: ImageVector,
    label: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (count > 0) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LibraryEmptyState(
    icon: ImageVector,
    title: String,
    body: String
) {
    AlKhatibEmptyState(
        icon = icon,
        title = title,
        body = body,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    )
}

@Composable
private fun BookmarkRow(
    bookmark: VerseBookmark,
    onClick: () -> Unit
) {
    LibraryItemCard(
        icon = Icons.Filled.Bookmark,
        iconTint = AlKhatibColors.GoldDeep,
        title = bookmark.surahLabel?.let { "$it ${bookmark.verseNumber}" }
            ?: "${bookmark.chapterNumber}:${bookmark.verseNumber}",
        subtitle = stringResource(R.string.verse_has_bookmark),
        badge = null,
        onClick = onClick
    )
}

@Composable
private fun NoteRow(
    note: VerseNote,
    onClick: () -> Unit
) {
    LibraryItemCard(
        icon = Icons.Filled.EditNote,
        iconTint = AlKhatibColors.DeepEmerald,
        title = "${note.chapterNumber}:${note.verseNumber}",
        subtitle = note.text,
        badge = stringResource(R.string.verse_has_note),
        onClick = onClick
    )
}

@Composable
private fun HifzRow(
    entry: HifzEntry,
    onClick: () -> Unit
) {
    val status = runCatching { HifzStatus.valueOf(entry.status) }.getOrDefault(HifzStatus.NONE)
    val (badge, tint) = when (status) {
        HifzStatus.LEARNING -> stringResource(R.string.hifz_learning) to AlKhatibColors.Gold
        HifzStatus.MEMORIZED -> stringResource(R.string.hifz_memorized) to AlKhatibColors.DeepEmerald
        HifzStatus.NEEDS_REVIEW -> stringResource(R.string.hifz_review) to Color(0xFFC2410C)
        HifzStatus.NONE -> "" to AlKhatibColors.Slate500
    }
    LibraryItemCard(
        icon = Icons.Filled.Psychology,
        iconTint = AlKhatibColors.IndigoDeep,
        title = "${entry.chapterNumber}:${entry.verseNumber}",
        subtitle = stringResource(R.string.quran_library_hifz_row_hint),
        badge = badge,
        badgeTint = tint,
        onClick = onClick
    )
}

@Composable
private fun LibraryItemCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String?,
    onClick: () -> Unit,
    badgeTint: Color = iconTint
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AlKhatibColors.Slate900
                    )
                    badge?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeTint,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(badgeTint.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AlKhatibColors.Slate500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HifzStatsRow(summary: QuranPersonalStore.HifzSummary) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
      HifzStatChip(stringResource(R.string.hifz_learning), summary.learning, AlKhatibColors.Gold)
      HifzStatChip(stringResource(R.string.hifz_memorized), summary.memorized, AlKhatibColors.DeepEmerald)
      HifzStatChip(stringResource(R.string.hifz_review), summary.needsReview, Color(0xFFC2410C))
  }
}

@Composable
private fun HifzStatChip(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
