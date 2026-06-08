package app.kamy.qalbuApp.features.today.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.kamy.qalbuApp.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.components.AlKhatibCard
import app.kamy.qalbuApp.design.components.AlKhatibErrorStateCompact
import app.kamy.qalbuApp.design.components.TodayVerseCardSkeleton
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.ui.common.rememberErrorDisplay
import app.kamy.qalbuApp.ui.common.TajweedHtmlView
import app.kamy.qalbuApp.ui.common.toVerseTranslationPlainText

@Composable
fun TodayVerseOfDaySection(
    verse: RandomAyahPayload?,
    referenceLabel: String?,
    isLoading: Boolean,
    error: AppError? = null,
    isPlaying: Boolean,
    aiShareLoading: Boolean = false,
    onPlayAudio: () -> Unit,
    onAiShare: () -> Unit,
    onTafsir: () -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val errorDisplay = error.rememberErrorDisplay(R.string.verse_of_day_load_failed)
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
                text = stringResource(R.string.verse_of_day),
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TajweedHtmlView(
                        textUthmani = verse.textUthmani,
                        ayahNumber = verse.resolvedVerseNumber,
                        fontSizeSp = 28,
                        compact = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    verse.translations?.firstOrNull()?.text?.let { translation ->
                        val clean = translation.toVerseTranslationPlainText()
                        if (clean.isNotEmpty()) {
                            Text(
                                text = "“$clean”",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.15f,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                softWrap = true
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                val pauseLabel = stringResource(R.string.pause)
                val audioLabel = stringResource(R.string.audio)
                val tafsirLabel = stringResource(R.string.tafsir)
                val aiLabel = stringResource(R.string.ai_label)
                val actionPills = listOf(
                    ActionPillData(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        if (isPlaying) pauseLabel else audioLabel,
                        AlKhatibColors.DeepEmerald
                    ) { onPlayAudio() },
                    ActionPillData(
                        Icons.Filled.AutoAwesome,
                        if (aiShareLoading) "…" else aiLabel,
                        AlKhatibColors.Gold,
                        onAiShare
                    ),
                    ActionPillData(Icons.Filled.AutoStories, tafsirLabel, AlKhatibColors.IndigoAccent, onTafsir)
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
                TodayVerseCardSkeleton(modifier = Modifier.fillMaxWidth())
            } else if (errorDisplay != null) {
                AlKhatibErrorStateCompact(
                    display = errorDisplay,
                    onRetry = onRetry
                )
            } else {
                Text(
                    text = stringResource(R.string.no_verse_retry),
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
