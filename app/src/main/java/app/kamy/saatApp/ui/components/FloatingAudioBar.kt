package app.kamy.saatApp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.infrastructure.audio.AudioPlaybackState

object FloatingAudioBarMetrics {
    val barHeight = 48.dp
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
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp + reserveTrailingSpace)
                .height(48.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                ),
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.45f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SaatColors.DeepEmerald)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggle
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (state.isPlaying) stringResource(R.string.pause) else stringResource(R.string.audio),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Audio Info & Linear Progress
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 2.dp)
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
                    verticalArrangement = Arrangement.Center
                ) {
                    val surahTitle = state.trackTitle.ifBlank { stringResource(R.string.playing) }
                    val ayahLabel = if (state.ayahNumber != null && state.ayahNumber > 0) {
                        "Ayat ${state.ayahNumber}"
                    } else if (state.trackSubtitle.isNotBlank()) {
                        state.trackSubtitle
                    } else ""

                    Text(
                        text = if (ayahLabel.isNotBlank()) "$surahTitle · $ayahLabel" else surahTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (state.reciterName.isNotBlank()) {
                        Text(
                            text = state.reciterName,
                            fontSize = 10.sp,
                            color = SaatColors.Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { state.progress.coerceIn(0f, 1f) },
                        color = SaatColors.DeepEmerald,
                        trackColor = SaatColors.SoftGrey.copy(alpha = 0.5f),
                        modifier = Modifier
                            .width(60.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                    )
                }

                // Stop / Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.stop),
                        tint = SaatColors.Slate500,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
