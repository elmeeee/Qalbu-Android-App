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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.design.theme.AlKhatibColors
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
            .background(AlKhatibColors.ScreenBackground)
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
                    modifier = Modifier.align(Alignment.Center)
                )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { ChaptersHeader() }

                state.continueReading?.let { session ->
                    item {
                        ContinueReadingCard(
                            session = session,
                            chapter = state.chapters.firstOrNull { it.id == session.chapterNumber },
                            onTap = {
                                vm.continueReadingTarget()?.let { (c, v) -> onOpenChapter(c, v) }
                            }
                        )
                    }
                }

                items(state.chapters, key = { it.id }) { chapter ->
                    ChapterRow(chapter = chapter, onClick = { onOpenChapter(chapter, null) })
                }
            }
        }
    }
}

@Composable
private fun ChaptersHeader() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = "⭐ Quran",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.DeepEmerald
        )
        Text(
            text = "114 Surahs • The Noble Quran",
            style = MaterialTheme.typography.bodyMedium,
            color = AlKhatibColors.Slate500
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(80.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(AlKhatibColors.Gold, AlKhatibColors.Gold.copy(alpha = 0f))
                    )
                )
        )
    }
}

@Composable
private fun ContinueReadingCard(
    session: ReadingSession,
    chapter: QuranChapter?,
    onTap: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color.White, AlKhatibColors.AmberWash)))
            .border(1.dp, AlKhatibColors.Gold.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable(onClick = onTap)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Bookmark,
            contentDescription = null,
            tint = AlKhatibColors.Gold
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "CONTINUE READING",
                style = MaterialTheme.typography.labelSmall,
                color = AlKhatibColors.Gold,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = chapter?.displayTitle ?: "Surah ${session.chapterNumber}",
                style = MaterialTheme.typography.titleMedium,
                color = AlKhatibColors.DeepEmerald,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ayah ${session.verseNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )
        }
    }
}

@Composable
private fun ChapterRow(chapter: QuranChapter, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, AlKhatibColors.SoftGrey, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        // Rotated diamond badge with chapter number
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.Teal))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chapter.id.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = chapter.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                color = AlKhatibColors.Slate900,
                fontWeight = FontWeight.SemiBold
            )
            Row {
                if (chapter.revelationLabel.isNotEmpty()) {
                    Text(
                        text = chapter.revelationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = AlKhatibColors.Gold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AlKhatibColors.AmberWash)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                chapter.versesCountLabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = AlKhatibColors.Slate500
                    )
                }
            }
        }
        chapter.nameArabic?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleLarge,
                color = AlKhatibColors.DeepEmerald,
                textAlign = TextAlign.End
            )
        }
    }
}
