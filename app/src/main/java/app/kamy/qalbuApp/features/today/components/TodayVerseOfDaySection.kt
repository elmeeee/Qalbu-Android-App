package app.kamy.qalbuApp.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.components.AlKhatibCard
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.ui.common.TajweedHtmlView
import app.kamy.qalbuApp.ui.common.buildTajweedHtmlFragment

@Composable
fun TodayVerseOfDaySection(
    verse: RandomAyahPayload?,
    referenceLabel: String?,
    isLoading: Boolean,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit,
    onShare: () -> Unit,
    onReflect: () -> Unit,
    onTafsir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = AlKhatibSpacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "✦",
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.width(AlKhatibSpacing.sm))
            Text(
                text = "Quran of the Day",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        referenceLabel?.let { label ->
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 18.dp, top = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(AlKhatibSpacing.md))

        AlKhatibCard(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            if (verse != null) {
                TajweedHtmlView(
                    htmlFragment = buildTajweedHtmlFragment(
                        verse.textUthmaniTajweed ?: verse.textUthmani,
                        verse.resolvedVerseNumber
                    ),
                    fontSizeSp = 28,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 220.dp)
                )

                verse.translations?.firstOrNull()?.text?.let { translation ->
                    val clean = translation.replace(Regex("<[^>]+>"), "").trim()
                    if (clean.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "“$clean”",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

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
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            } else {
                Text(
                    text = "No verse available. Pull to retry.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
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
    FilledTonalButton(
        onClick = data.onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = data.tint.copy(alpha = 0.14f),
            contentColor = data.tint
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 12.dp,
            vertical = 10.dp
        )
    ) {
        Icon(imageVector = data.icon, contentDescription = data.label, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = data.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
