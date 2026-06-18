package app.kamy.saatApp.features.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.domain.model.HifzEntry
import app.kamy.saatApp.domain.model.HifzStatus
import app.kamy.saatApp.domain.model.VerseBookmark
import app.kamy.saatApp.domain.model.VerseNote
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

private enum class LibraryTab { BOOKMARKS, NOTES, HIFZ }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranBookmarksScreen(
    onBack: () -> Unit,
    onOpenVerse: (chapter: Int, ayah: Int) -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    var tab by remember { mutableStateOf(LibraryTab.BOOKMARKS) }

    val bookmarks = remember(refreshKey) { QuranPersonalStore.bookmarks(context) }
    val notes = remember(refreshKey) { QuranPersonalStore.notes(context) }
    val hifzEntries = remember(refreshKey) { QuranPersonalStore.hifzEntries(context) }
    val hifzSummary = remember(refreshKey) { QuranPersonalStore.hifzSummary(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = stringResource(R.string.quran_library_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal)
        ) {
            SegmentedButton(
                selected = tab == LibraryTab.BOOKMARKS,
                onClick = { tab = LibraryTab.BOOKMARKS },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) { Text(stringResource(R.string.bookmarks_title)) }
            SegmentedButton(
                selected = tab == LibraryTab.NOTES,
                onClick = { tab = LibraryTab.NOTES },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) { Text(stringResource(R.string.notes_title)) }
            SegmentedButton(
                selected = tab == LibraryTab.HIFZ,
                onClick = { tab = LibraryTab.HIFZ },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) { Text(stringResource(R.string.hifz_title)) }
        }

        if (tab == LibraryTab.HIFZ && hifzSummary.total > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HifzStatChip(stringResource(R.string.hifz_learning), hifzSummary.learning, AlKhatibColors.IndigoAccent)
                HifzStatChip(stringResource(R.string.hifz_memorized), hifzSummary.memorized, AlKhatibColors.DeepEmerald)
                HifzStatChip(stringResource(R.string.hifz_review), hifzSummary.needsReview, AlKhatibColors.GoldDeep)
            }
        }

        when (tab) {
            LibraryTab.BOOKMARKS -> LibraryList(
                emptyMessage = stringResource(R.string.bookmarks_empty),
                emptyIcon = Icons.Filled.Bookmark,
                isEmpty = bookmarks.isEmpty()
            ) {
                items(bookmarks, key = { it.verseKey }) { bookmark ->
                    BookmarkRow(
                        bookmark = bookmark,
                        onOpen = { onOpenVerse(bookmark.chapterNumber, bookmark.verseNumber) },
                        onDelete = {
                            QuranPersonalStore.removeBookmark(context, bookmark.verseKey)
                            refreshKey++
                        }
                    )
                    LibraryDivider()
                }
            }
            LibraryTab.NOTES -> LibraryList(
                emptyMessage = stringResource(R.string.notes_empty),
                emptyIcon = Icons.Filled.EditNote,
                isEmpty = notes.isEmpty()
            ) {
                items(notes, key = { it.verseKey }) { note ->
                    NoteRow(
                        note = note,
                        onOpen = { onOpenVerse(note.chapterNumber, note.verseNumber) },
                        onDelete = {
                            QuranPersonalStore.deleteNote(context, note.verseKey)
                            refreshKey++
                        }
                    )
                    LibraryDivider()
                }
            }
            LibraryTab.HIFZ -> LibraryList(
                emptyMessage = stringResource(R.string.hifz_empty),
                emptyIcon = Icons.Filled.Psychology,
                isEmpty = hifzEntries.isEmpty()
            ) {
                items(hifzEntries, key = { it.verseKey }) { entry ->
                    HifzRow(
                        entry = entry,
                        onOpen = { onOpenVerse(entry.chapterNumber, entry.verseNumber) },
                        onClear = {
                            QuranPersonalStore.setHifzStatus(
                                context,
                                entry.verseKey,
                                entry.chapterNumber,
                                entry.verseNumber,
                                HifzStatus.NONE
                            )
                            refreshKey++
                        }
                    )
                    LibraryDivider()
                }
            }
        }
    }
}

@Composable
private fun LibraryList(
    emptyMessage: String,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isEmpty: Boolean,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    if (isEmpty) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(emptyIcon, contentDescription = null, tint = AlKhatibColors.Gold.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            Text(emptyMessage, style = MaterialTheme.typography.bodyLarge, color = AlKhatibColors.Slate500)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = floatingNavBottomPadding())
        ) {
            content()
        }
    }
}

@Composable
private fun LibraryDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = AlKhatibColors.SoftGrey.copy(alpha = 0.6f)
    )
}

@Composable
private fun HifzStatChip(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = "$label · $count",
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun BookmarkRow(
    bookmark: VerseBookmark,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    LibraryItemRow(
        title = bookmark.surahLabel ?: stringResource(R.string.surah_number, bookmark.chapterNumber),
        subtitle = stringResource(R.string.verse_number, bookmark.verseNumber),
        onOpen = onOpen,
        onDelete = onDelete,
        deleteDescription = stringResource(R.string.remove_bookmark)
    )
}

@Composable
private fun NoteRow(
    note: VerseNote,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    LibraryItemRow(
        title = stringResource(R.string.surah_number, note.chapterNumber) + " · " +
            stringResource(R.string.verse_number, note.verseNumber),
        subtitle = note.text,
        onOpen = onOpen,
        onDelete = onDelete,
        deleteDescription = stringResource(R.string.delete_note)
    )
}

@Composable
private fun HifzRow(
    entry: HifzEntry,
    onOpen: () -> Unit,
    onClear: () -> Unit
) {
    val status = runCatching { HifzStatus.valueOf(entry.status) }.getOrDefault(HifzStatus.NONE)
    LibraryItemRow(
        title = stringResource(R.string.surah_number, entry.chapterNumber) + " · " +
            stringResource(R.string.verse_number, entry.verseNumber),
        subtitle = hifzStatusText(status),
        onOpen = onOpen,
        onDelete = onClear,
        deleteDescription = stringResource(R.string.hifz_clear)
    )
}

@Composable
private fun hifzStatusText(status: HifzStatus): String = when (status) {
    HifzStatus.LEARNING -> stringResource(R.string.hifz_learning)
    HifzStatus.MEMORIZED -> stringResource(R.string.hifz_memorized)
    HifzStatus.NEEDS_REVIEW -> stringResource(R.string.hifz_review)
    HifzStatus.NONE -> stringResource(R.string.hifz_mark)
}

@Composable
private fun LibraryItemRow(
    title: String,
    subtitle: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    deleteDescription: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.Slate900
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = deleteDescription, tint = AlKhatibColors.Danger)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AlKhatibColors.Slate500)
    }
}
