package app.kamy.saatApp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import app.kamy.saatApp.R
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.infrastructure.audio.AudioPlaybackState

object FloatingAudioBarMetrics {
    val barHeight = 68.dp
    val bottomGap = 8.dp
}

@Composable
fun FloatingAudioBar(
    state: AudioPlaybackState,
    visible: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onOpenPlayback: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    reserveTrailingSpace: Dp = 0.dp,
) {
    val openPlaybackInteractionSource = remember { MutableInteractionSource() }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp + reserveTrailingSpace),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SaatColors.DeepEmerald
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
                    .then(
                        if (onOpenPlayback != null) {
                            Modifier.clickable(
                                interactionSource = openPlaybackInteractionSource,
                                indication = null,
                                onClick = onOpenPlayback
                            )
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pauseLabel = stringResource(R.string.pause)
                val audioLabel = stringResource(R.string.audio)
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isPlaying) pauseLabel else audioLabel,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = state.trackTitle.ifBlank { stringResource(R.string.playing) },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = androidx.compose.ui.graphics.Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val ayahLine = buildString {
                        if (state.trackSubtitle.isNotBlank()) append(state.trackSubtitle)
                        if (state.reciterName.isNotBlank()) {
                            if (isNotEmpty()) append(" · ")
                            append(state.reciterName)
                        }
                    }
                    if (ayahLine.isNotBlank()) {
                        Text(
                            text = ayahLine,
                            style = MaterialTheme.typography.labelMedium,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.stop),
                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
