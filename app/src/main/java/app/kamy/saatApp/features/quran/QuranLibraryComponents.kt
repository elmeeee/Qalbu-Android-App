package app.kamy.saatApp.features.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.domain.model.HifzStatus
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore

data class QuranLibraryCounts(
    val bookmarks: Int,
    val notes: Int,
    val hifz: Int
) {
    val total: Int get() = bookmarks + notes + hifz
}

fun readQuranLibraryCounts(context: android.content.Context): QuranLibraryCounts {
    val hifzSummary = QuranPersonalStore.hifzSummary(context)
    return QuranLibraryCounts(
        bookmarks = QuranPersonalStore.bookmarks(context).size,
        notes = QuranPersonalStore.notes(context).size,
        hifz = hifzSummary.total
    )
}

@Composable
fun rememberQuranLibraryCounts(): QuranLibraryCounts {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var counts by remember { mutableStateOf(readQuranLibraryCounts(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                counts = readQuranLibraryCounts(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return counts
}

@Composable
fun MyQuranLibraryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val counts = rememberQuranLibraryCounts()

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AlKhatibColors.DeepEmerald.copy(alpha = 0.18f),
                                AlKhatibColors.GoldDeep.copy(alpha = 0.14f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = AlKhatibColors.GoldDeep,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.quran_library_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AlKhatibColors.Slate900
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.quran_library_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500,
                    lineHeight = 18.sp
                )
                if (counts.total > 0) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LibraryCountChip(
                            icon = Icons.Filled.Bookmark,
                            count = counts.bookmarks,
                            tint = AlKhatibColors.GoldDeep
                        )
                        LibraryCountChip(
                            icon = Icons.AutoMirrored.Filled.Notes,
                            count = counts.notes,
                            tint = AlKhatibColors.DeepEmerald
                        )
                        LibraryCountChip(
                            icon = Icons.Filled.Psychology,
                            count = counts.hifz,
                            tint = AlKhatibColors.IndigoDeep
                        )
                    }
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AlKhatibColors.Slate500,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun LibraryCountChip(
    icon: ImageVector,
    count: Int,
    tint: Color
) {
    if (count <= 0) return
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tint.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = tint
        )
    }
}

@Composable
fun QuranLibraryHero(
    counts: QuranLibraryCounts,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        AlKhatibColors.DeepEmerald.copy(alpha = 0.12f),
                        AlKhatibColors.GoldDeep.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = AlKhatibColors.DeepEmerald.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = stringResource(R.string.quran_library_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.Slate900
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.quran_library_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AlKhatibColors.Slate500,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LibraryFeaturePill(
                icon = Icons.Filled.Bookmark,
                label = stringResource(R.string.bookmarks_title),
                count = counts.bookmarks,
                tint = AlKhatibColors.GoldDeep,
                modifier = Modifier.weight(1f)
            )
            LibraryFeaturePill(
                icon = Icons.AutoMirrored.Filled.Notes,
                label = stringResource(R.string.notes_title),
                count = counts.notes,
                tint = AlKhatibColors.DeepEmerald,
                modifier = Modifier.weight(1f)
            )
            LibraryFeaturePill(
                icon = Icons.Filled.Psychology,
                label = stringResource(R.string.hifz_title),
                count = counts.hifz,
                tint = AlKhatibColors.IndigoDeep,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(14.dp))
        QuranLibraryLegend()
    }
}

@Composable
fun QuranLibraryLegend(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.quran_library_legend),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate800
        )
        LegendRow(
            icon = Icons.Filled.Bookmark,
            tint = AlKhatibColors.GoldDeep,
            text = stringResource(R.string.quran_library_intro_bookmarks)
        )
        LegendRow(
            icon = Icons.Filled.EditNote,
            tint = AlKhatibColors.DeepEmerald,
            text = stringResource(R.string.quran_library_intro_notes)
        )
        LegendRow(
            icon = Icons.Filled.Psychology,
            tint = AlKhatibColors.IndigoDeep,
            text = stringResource(R.string.quran_library_intro_hifz)
        )
    }
}

@Composable
private fun LegendRow(
    icon: ImageVector,
    tint: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = AlKhatibColors.Slate500,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LibraryFeaturePill(
    icon: ImageVector,
    label: String,
    count: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.Slate900
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AlKhatibColors.Slate500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun VersePersonalBadges(
    verseKey: String?,
    personalDataRevision: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (verseKey.isNullOrBlank()) return
    val context = LocalContext.current
    val bookmarked = remember(verseKey, personalDataRevision) {
        QuranPersonalStore.isBookmarked(context, verseKey)
    }
    val hasNote = remember(verseKey, personalDataRevision) {
        QuranPersonalStore.noteFor(context, verseKey) != null
    }
    val hifzStatus = remember(verseKey, personalDataRevision) {
        QuranPersonalStore.hifzStatus(context, verseKey)
    }
    if (!bookmarked && !hasNote && hifzStatus == HifzStatus.NONE) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bookmarked) {
            VerseBadge(
                icon = Icons.Filled.Bookmark,
                label = stringResource(R.string.verse_has_bookmark),
                tint = AlKhatibColors.GoldDeep,
                compact = compact
            )
            Spacer(Modifier.width(6.dp))
        }
        if (hasNote) {
            VerseBadge(
                icon = Icons.Filled.EditNote,
                label = stringResource(R.string.verse_has_note),
                tint = AlKhatibColors.DeepEmerald,
                compact = compact
            )
            Spacer(Modifier.width(6.dp))
        }
        if (hifzStatus != HifzStatus.NONE) {
            val (label, tint) = when (hifzStatus) {
                HifzStatus.LEARNING -> stringResource(R.string.hifz_learning) to AlKhatibColors.Gold
                HifzStatus.MEMORIZED -> stringResource(R.string.hifz_memorized) to AlKhatibColors.DeepEmerald
                HifzStatus.NEEDS_REVIEW -> stringResource(R.string.hifz_review) to Color(0xFFC2410C)
                HifzStatus.NONE -> "" to Color.Transparent
            }
            VerseBadge(
                icon = Icons.Filled.Psychology,
                label = label,
                tint = tint,
                compact = compact
            )
        }
    }
}

@Composable
private fun VerseBadge(
    icon: ImageVector,
    label: String,
    tint: Color,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        if (!compact) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1
            )
        }
    }
}
