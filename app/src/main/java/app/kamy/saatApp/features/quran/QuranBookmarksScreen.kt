package app.kamy.saatApp.features.quran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.SaatEmptyState
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
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
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SaatColors.DeepEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaatColors.ScreenBackground
                )
            )
        },
        containerColor = SaatColors.ScreenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = SaatSpacing.screenHorizontal,
                    vertical = SaatSpacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "hero") {
                    QuranLibraryHero(counts = counts)
                }

                item(key = "tabs") {
                    QuranSayaTabSelector(
                        selectedTab = tab,
                        onTabSelected = { tab = it },
                        bookmarksCount = bookmarks.size,
                        notesCount = notes.size,
                        hifzCount = hifzSummary.total
                    )
                }

                when (tab) {
                    0 -> {
                        if (bookmarks.isEmpty()) {
                            item(key = "empty_bookmarks") {
                                LibraryEmptyState(
                                    drawableResId = R.drawable.ic_bookmark_custom,
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
                                    drawableResId = R.drawable.ic_personalnote_custom,
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
                                    drawableResId = R.drawable.ic_memorization_custom,
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
private fun QuranSayaTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    bookmarksCount: Int,
    notesCount: Int,
    hifzCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        SaatColors.DeepEmerald.copy(alpha = 0.08f),
                        SaatColors.GoldDeep.copy(alpha = 0.06f)
                    )
                )
            )
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuranSayaTabChip(
                drawableResId = R.drawable.ic_bookmark_custom,
                label = stringResource(R.string.bookmarks_title),
                count = bookmarksCount,
                selected = selectedTab == 0,
                activeColor = SaatColors.GoldDeep,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            QuranSayaTabChip(
                drawableResId = R.drawable.ic_personalnote_custom,
                label = stringResource(R.string.notes_title),
                count = notesCount,
                selected = selectedTab == 1,
                activeColor = SaatColors.DeepEmerald,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            QuranSayaTabChip(
                drawableResId = R.drawable.ic_memorization_custom,
                label = stringResource(R.string.hifz_title),
                count = hifzCount,
                selected = selectedTab == 2,
                activeColor = SaatColors.IndigoDeep,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuranSayaTabChip(
    drawableResId: Int,
    label: String,
    count: Int,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else SaatColors.Slate700,
        animationSpec = tween(200),
        label = "tabContent"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(drawableResId),
                contentDescription = null,
                tint = if (selected) Color.White else activeColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (count > 0) {
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) Color.White.copy(alpha = 0.25f) else activeColor.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else activeColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryEmptyState(
    drawableResId: Int,
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SaatColors.DeepEmerald.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(drawableResId),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun BookmarkRow(
    bookmark: VerseBookmark,
    onClick: () -> Unit
) {
    LibraryItemCard(
        drawableResId = R.drawable.ic_bookmark_custom,
        iconTint = SaatColors.GoldDeep,
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
        drawableResId = R.drawable.ic_personalnote_custom,
        iconTint = SaatColors.DeepEmerald,
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
        HifzStatus.LEARNING -> stringResource(R.string.hifz_learning) to SaatColors.Gold
        HifzStatus.MEMORIZED -> stringResource(R.string.hifz_memorized) to SaatColors.DeepEmerald
        HifzStatus.NEEDS_REVIEW -> stringResource(R.string.hifz_review) to Color(0xFFC2410C)
        HifzStatus.NONE -> "" to SaatColors.Slate500
    }
    LibraryItemCard(
        drawableResId = R.drawable.ic_memorization_custom,
        iconTint = SaatColors.IndigoDeep,
        title = "${entry.chapterNumber}:${entry.verseNumber}",
        subtitle = stringResource(R.string.quran_library_hifz_row_hint),
        badge = badge,
        badgeTint = tint,
        onClick = onClick
    )
}

@Composable
private fun LibraryItemCard(
    drawableResId: Int,
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
        shape = RoundedCornerShape(18.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(drawableResId),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
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
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                    badge?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
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
                    color = SaatColors.Slate500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SaatColors.Slate500,
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
        HifzStatChip(stringResource(R.string.hifz_learning), summary.learning, SaatColors.Gold)
        HifzStatChip(stringResource(R.string.hifz_memorized), summary.memorized, SaatColors.DeepEmerald)
        HifzStatChip(stringResource(R.string.hifz_review), summary.needsReview, Color(0xFFC2410C))
    }
}

@Composable
private fun HifzStatChip(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
