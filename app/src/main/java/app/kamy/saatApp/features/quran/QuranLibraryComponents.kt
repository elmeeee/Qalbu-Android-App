package app.kamy.saatApp.features.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
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
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.45f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            SaatColors.DeepEmerald.copy(alpha = 0.04f),
                            SaatColors.GoldDeep.copy(alpha = 0.03f)
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                SaatColors.DeepEmerald.copy(alpha = 0.12f),
                                SaatColors.GoldDeep.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.9f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark_custom),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.bookmarks_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                    if (counts.bookmarks > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SaatColors.DeepEmerald)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${counts.bookmarks}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.PureWhite,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.bookmarks_card_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500,
                    lineHeight = 18.sp
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SaatColors.Slate500,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun LibraryCountChip(
    drawableResId: Int,
    count: Int,
    tint: Color
) {
    if (count <= 0) return
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tint.copy(alpha = 0.12f))
            .border(0.8.dp, tint.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            painter = painterResource(drawableResId),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
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
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        SaatColors.DeepEmerald.copy(alpha = 0.12f),
                        SaatColors.GoldDeep.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = SaatColors.DeepEmerald.copy(alpha = 0.18f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SaatColors.GoldDeep.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark_custom),
                    contentDescription = null,
                    tint = SaatColors.GoldDeep,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.quran_library_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate900
                )
                Text(
                    text = stringResource(R.string.quran_library_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LibraryFeaturePill(
                drawableResId = R.drawable.ic_bookmark_custom,
                label = stringResource(R.string.bookmarks_title),
                count = counts.bookmarks,
                tint = SaatColors.GoldDeep,
                modifier = Modifier.weight(1f)
            )
            LibraryFeaturePill(
                drawableResId = R.drawable.ic_personalnote_custom,
                label = stringResource(R.string.notes_title),
                count = counts.notes,
                tint = SaatColors.DeepEmerald,
                modifier = Modifier.weight(1f)
            )
            LibraryFeaturePill(
                drawableResId = R.drawable.ic_memorization_custom,
                label = stringResource(R.string.hifz_title),
                count = counts.hifz,
                tint = SaatColors.IndigoDeep,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
        QuranLibraryLegend()
    }
}

@Composable
fun QuranLibraryLegend(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.quran_library_legend),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = SaatColors.Slate800
        )
        LegendRow(
            drawableResId = R.drawable.ic_bookmark_custom,
            tint = SaatColors.GoldDeep,
            text = stringResource(R.string.quran_library_intro_bookmarks)
        )
        LegendRow(
            drawableResId = R.drawable.ic_personalnote_custom,
            tint = SaatColors.DeepEmerald,
            text = stringResource(R.string.quran_library_intro_notes)
        )
        LegendRow(
            drawableResId = R.drawable.ic_memorization_custom,
            tint = SaatColors.IndigoDeep,
            text = stringResource(R.string.quran_library_intro_hifz)
        )
    }
}

@Composable
private fun LegendRow(
    drawableResId: Int,
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
            Icon(
                painter = painterResource(drawableResId),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = SaatColors.Slate500,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LibraryFeaturePill(
    drawableResId: Int,
    label: String,
    count: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .border(1.dp, tint.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(drawableResId),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SaatColors.Slate900
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SaatColors.Slate500,
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
                drawableResId = R.drawable.ic_bookmark_custom,
                label = stringResource(R.string.verse_has_bookmark),
                tint = SaatColors.GoldDeep,
                compact = compact
            )
            Spacer(Modifier.width(6.dp))
        }
        if (hasNote) {
            VerseBadge(
                drawableResId = R.drawable.ic_personalnote_custom,
                label = stringResource(R.string.verse_has_note),
                tint = SaatColors.DeepEmerald,
                compact = compact
            )
            Spacer(Modifier.width(6.dp))
        }
        if (hifzStatus != HifzStatus.NONE) {
            val (label, tint) = when (hifzStatus) {
                HifzStatus.LEARNING -> stringResource(R.string.hifz_learning) to SaatColors.Gold
                HifzStatus.MEMORIZED -> stringResource(R.string.hifz_memorized) to SaatColors.DeepEmerald
                HifzStatus.NEEDS_REVIEW -> stringResource(R.string.hifz_review) to Color(0xFFC2410C)
                HifzStatus.NONE -> "" to Color.Transparent
            }
            VerseBadge(
                drawableResId = R.drawable.ic_memorization_custom,
                label = label,
                tint = tint,
                compact = compact
            )
        }
    }
}

@Composable
private fun VerseBadge(
    drawableResId: Int,
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
        Icon(
            painter = painterResource(drawableResId),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
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
