package app.kamy.qalbuApp.features.quran

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.design.components.AlKhatibCard
import app.kamy.qalbuApp.design.components.AlKhatibCardStyle
import app.kamy.qalbuApp.design.components.AlKhatibRevelationChip
import app.kamy.qalbuApp.design.components.AlKhatibSectionHeader
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.ReadingSession

/**
 * Mirrors iOS Features/Chapter/Views/ChaptersView.swift.
 */
@Composable
fun ChaptersScreen(
    onOpenChapter: (chapter: QuranChapter, initialVerse: Int?) -> Unit
) {
    val vm: ChaptersViewModel = hiltViewModel()
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading && state.chapters.isEmpty() ->
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AlKhatibColors.DeepEmerald
                )
            state.error != null && state.chapters.isEmpty() ->
                Text(
                    text = state.error.orEmpty(),
                    color = AlKhatibColors.Danger,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    AlKhatibSectionHeader(
                        title = "Quran",
                        subtitle = "114 Surahs • The Noble Quran"
                    )
                }

                state.continueReading?.let { session ->
                    item {
                        ContinueReadingCard(
                            session = session,
                            chapter = state.chapters.firstOrNull { it.id == session.chapterNumber },
                            onTap = {
                                vm.continueReadingTarget()?.let { (c, v) -> onOpenChapter(c, v) }
                            },
                            modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                        )
                    }
                }

                items(state.chapters, key = { it.id }) { chapter ->
                    ChapterRow(
                        chapter = chapter,
                        onClick = { onOpenChapter(chapter, null) },
                        modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                    )
                }

                item { Spacer(Modifier.height(AlKhatibSpacing.bottomNavClearance)) }
            }
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
    AlKhatibCard(
        modifier = modifier,
        style = AlKhatibCardStyle.Elevated,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        onClick = onTap
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AlKhatibColors.Gold.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = AlKhatibColors.GoldDeep)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "CONTINUE READING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.Gold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = chapter?.displayComplexName ?: "Surah ${session.chapterNumber}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Ayah ${session.verseNumber}",
                fontSize = 14.sp,
                color = AlKhatibColors.Slate500
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(24.dp)
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
    AlKhatibCard(
        modifier = modifier,
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(verticalAlignment = Alignment.Top) {
        ChapterDiamondBadge(number = chapter.id)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = chapter.displayComplexName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AlKhatibColors.Slate900
                    )
                    if (chapter.displayTranslatedName.isNotEmpty()) {
                        Text(
                            text = chapter.displayTranslatedName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AlKhatibColors.DeepEmerald.copy(alpha = 0.75f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
                chapter.nameArabic?.takeIf { it.isNotBlank() }?.let { arabic ->
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = arabic,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = AlKhatibColors.DeepEmerald,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AlKhatibColors.DeepEmerald.copy(alpha = 0.05f))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chapter.revelationLabel.isNotEmpty()) {
                    AlKhatibRevelationChip(
                        label = chapter.revelationLabel,
                        isMeccan = chapter.isMeccan
                    )
                }
                chapter.versesCountLabel?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AlKhatibColors.Slate500
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ChapterDiamondBadge(number: Int) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)
                    )
                )
        )
        Text(
            text = number.toString(),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
