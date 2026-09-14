package app.kamy.saatApp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalRadioCatalog
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.infrastructure.audio.AudioPlaybackState

object FloatingAudioBarMetrics {
    val barHeight = 52.dp
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
    val context = LocalContext.current
    val openPlaybackInteractionSource = remember { MutableInteractionSource() }
    val radioStations = remember(context) { LocalRadioCatalog.getStations(context) }
    val activeStation = remember(state.currentUrl, state.trackTitle, radioStations) {
        radioStations.firstOrNull { it.streamUrl == state.currentUrl || it.name == state.trackTitle }
    }
    val isRadioStream = state.reciterName == "Radio Quran" || activeStation != null

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp + reserveTrailingSpace)
                .height(52.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                ),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Audio Artwork / Station Icon
                if (activeStation != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SaatColors.Gold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(activeStation.iconRes),
                            contentDescription = activeStation.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isRadioStream) R.drawable.ic_radio_custom else R.drawable.ic_quran_on
                            ),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Audio Info (Title & Subtitle/Progress)
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
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isRadioStream) {
                            MiniLiveWaveEqualizer(isPlaying = state.isPlaying)
                            Text(
                                text = if (state.isPlaying) "Live Radio" else "Paused",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (state.isPlaying) SaatColors.Gold else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        } else {
                            if (state.reciterName.isNotBlank()) {
                                Text(
                                    text = state.reciterName,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { state.progress.coerceIn(0f, 1f) },
                                color = SaatColors.DeepEmerald,
                                trackColor = SaatColors.SoftGrey.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(2.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }

                // Play / Pause Circle Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
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

                // Stop / Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.stop),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniLiveWaveEqualizer(isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "miniWave")
    val b1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )
    val b2 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )
    val b3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(10.dp)
    ) {
        val f1 = if (isPlaying) b1 else 0.25f
        val f2 = if (isPlaying) b2 else 0.45f
        val f3 = if (isPlaying) b3 else 0.25f

        listOf(f1, f2, f3).forEach { f ->
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((10 * f).dp)
                    .background(SaatColors.Gold, RoundedCornerShape(1.dp))
            )
        }
    }
}

