package app.kamy.qalbuApp.features.today.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.heightIn
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.ui.common.TajweedHtmlView
import app.kamy.qalbuApp.ui.common.buildTajweedHtmlFragment

/**
 * Mirrors iOS Features/Discovery/Components/TodayVerseOfDaySectionView.swift.
 */
@Composable
fun TodayVerseOfDaySection(
    verse: RandomAyahPayload?,
    isLoading: Boolean,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit,
    onShare: () -> Unit,
    onReflect: () -> Unit,
    onTafsir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✦", color = AlKhatibColors.Gold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Verse of the Day",
                color = AlKhatibColors.DeepEmerald,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        if (verse?.verseKey != null) {
            Text(
                text = verse.verseKey,
                color = AlKhatibColors.DeepEmerald.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 22.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(listOf(Color.White, AlKhatibColors.MintWash))
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            AlKhatibColors.DeepEmerald.copy(alpha = 0.08f),
                            AlKhatibColors.Gold.copy(alpha = 0.15f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            if (verse != null) {
                // Verse key chip
                verse.verseKey?.let {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AlKhatibColors.DeepEmerald)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = it,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Arabic tajweed block
                TajweedHtmlView(
                    htmlFragment = buildTajweedHtmlFragment(
                        verse.textUthmaniTajweed ?: verse.textUthmani,
                        verse.resolvedVerseNumber
                    ),
                    fontSizeSp = 30,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 220.dp)
                )

                // Translation
                verse.translations?.firstOrNull()?.text?.let { translation ->
                    val clean = translation.replace(Regex("<[^>]+>"), "").trim()
                    if (clean.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "“$clean”",
                            color = AlKhatibColors.Slate800,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Fixed 2×2 grid — must not use LazyVerticalGrid inside TodayScreen's verticalScroll.
                val actionPills = listOf(
                    ActionPillData(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        if (isPlaying) "Pause" else "Audio",
                        AlKhatibColors.DeepEmerald
                    ) { onPlayAudio() },
                    ActionPillData(Icons.Filled.Share, "Share", AlKhatibColors.BlueLink, onShare),
                    ActionPillData(Icons.Filled.Edit, "Reflect", AlKhatibColors.Gold, onReflect),
                    ActionPillData(Icons.Filled.AutoStories, "Tafsir", AlKhatibColors.IndigoAccent, onTafsir)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    actionPills.chunked(2).forEach { rowPills ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPills.forEach { pill ->
                                ActionPill(
                                    data = pill,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(AlKhatibColors.PanelGrey, RoundedCornerShape(12.dp))
                )
            } else {
                Text(
                    text = "No verse available. Pull to retry.",
                    color = AlKhatibColors.Slate500,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class ActionPillData(
    val icon: ImageVector,
    val label: String,
    val tint: Color,
    val onClick: () -> Unit
)

@Composable
private fun ActionPill(
    data: ActionPillData,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(data.tint.copy(alpha = 0.08f))
            .border(1.dp, data.tint.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(vertical = 10.dp, horizontal = 12.dp)
            .pointerInput(data) { detectTapGestures(onTap = { data.onClick() }) }
    ) {
        Icon(imageVector = data.icon, contentDescription = data.label, tint = data.tint)
        Spacer(Modifier.width(6.dp))
        Text(
            text = data.label,
            color = data.tint,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
