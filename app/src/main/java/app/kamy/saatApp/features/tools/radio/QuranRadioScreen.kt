package app.kamy.saatApp.features.tools.radio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalRadioCatalog
import app.kamy.saatApp.core.config.QuranRadioStation
import app.kamy.saatApp.core.config.RadioCategory
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.infrastructure.audio.AudioPlaybackState
import app.kamy.saatApp.infrastructure.audio.AudioPlayerController
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.layout.floatingNavAndAudioBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

@Composable
fun QuranRadioScreen(
    audioPlayer: AudioPlayerController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage = remember(context) { AppLanguageStore.from(context).current() }
    val playbackState by audioPlayer.state.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf(RadioCategory.ALL) }

    val allStations = remember(context) { LocalRadioCatalog.getStations(context) }
    val filteredStations = remember(selectedCategory, allStations) {
        if (selectedCategory == RadioCategory.ALL) {
            allStations
        } else {
            allStations.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        // Sticky Header bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SaatColors.ScreenBackground,
            shadowElevation = 0.5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "✦",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.radio_quran_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))

                // Live Indicator Badge in Header
                LiveStatusBadge(isLive = playbackState.isPlaying && playbackState.reciterName == "Radio Quran")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = SaatSpacing.screenHorizontal,
                end = SaatSpacing.screenHorizontal,
                top = 12.dp,
                bottom = floatingNavAndAudioBottomPadding(audioBarVisible = playbackState.currentUrl != null) + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero / Now Playing Card
            item {
                RadioHeroPlayerCard(
                    playbackState = playbackState,
                    onToggle = { audioPlayer.toggle() },
                    onStop = { audioPlayer.stop() }
                )
            }

            // Category Filter Pills
            item {
                CategoryFilterBar(
                    appLanguage = appLanguage,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it }
                )
            }

            // Radio Stations List Header
            item {
                Text(
                    text = stringResource(R.string.radio_quran_list_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // Radio Station Cards
            items(filteredStations, key = { it.id }) { station ->
                val isCurrentPlaying = playbackState.currentUrl == station.streamUrl && playbackState.isPlaying

                RadioStationCard(
                    station = station,
                    appLanguage = appLanguage,
                    isPlaying = isCurrentPlaying,
                    onPlayClick = {
                        if (isCurrentPlaying) {
                            audioPlayer.toggle()
                        } else {
                            audioPlayer.playRadioStation(
                                stationName = station.name,
                                countryName = "${station.countryFlag} ${station.country(appLanguage)}",
                                url = station.streamUrl
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LiveStatusBadge(isLive: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isLive) Color(0xFFE57373).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isLive) Color(0xFFE53935) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isLive) Color(0xFFE53935) else Color.Gray)
            )
            Text(
                text = if (isLive) stringResource(R.string.radio_quran_live_badge) else stringResource(R.string.radio_quran_offline_badge),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isLive) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RadioHeroPlayerCard(
    playbackState: AudioPlaybackState,
    onToggle: () -> Unit,
    onStop: () -> Unit
) {
    val isRadioActive = playbackState.reciterName == "Radio Quran" && playbackState.currentUrl != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                colors = listOf(SaatColors.Gold, SaatColors.Gold.copy(alpha = 0.3f))
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SaatColors.DeepEmerald,
                            Color(0xFF0F4435),
                            Color(0xFF0A2E24)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Row: Header Badge & Live Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SaatColors.Gold.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Radio,
                                    contentDescription = null,
                                    tint = SaatColors.GoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "RADIO QURAN LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.GoldBright,
                            letterSpacing = 1.2.sp
                        )
                    }

                    if (isRadioActive) {
                        LiveStatusBadge(isLive = playbackState.isPlaying)
                    }
                }

                if (isRadioActive) {
                    // Active Radio Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = playbackState.trackTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = playbackState.trackSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.width(12.dp))
                        SoundEqualizerAnimation(isPlaying = playbackState.isPlaying)
                    }

                    // Player Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                onClick = onToggle,
                                shape = CircleShape,
                                color = SaatColors.Gold,
                                shadowElevation = 6.dp,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(
                                            if (playbackState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                        ),
                                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                        tint = SaatColors.DeepEmerald,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = onStop,
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = if (playbackState.isPlaying) "HD Live Stream" else "Paused",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (playbackState.isPlaying) SaatColors.GoldBright else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.radio_quran_hero_idle_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.radio_quran_hero_idle_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundEqualizerAnimation(isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    
    val bar1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(18.dp)
    ) {
        val active1 = if (isPlaying) bar1 else 0.2f
        val active2 = if (isPlaying) bar2 else 0.4f
        val active3 = if (isPlaying) bar3 else 0.2f

        Box(
            modifier = Modifier
                .width(4.dp)
                .height((18 * active1).dp)
                .background(SaatColors.Gold, RoundedCornerShape(2.dp))
        )
        Box(
            modifier = Modifier
                .width(4.dp)
                .height((18 * active2).dp)
                .background(SaatColors.Gold, RoundedCornerShape(2.dp))
        )
        Box(
            modifier = Modifier
                .width(4.dp)
                .height((18 * active3).dp)
                .background(SaatColors.Gold, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun CategoryFilterBar(
    appLanguage: AppLanguage,
    selectedCategory: RadioCategory,
    onSelectCategory: (RadioCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = category.label(appLanguage),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun RadioStationCard(
    station: QuranRadioStation,
    appLanguage: AppLanguage,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    val cardBg = if (isPlaying) {
        SaatColors.DeepEmerald
    } else {
        MaterialTheme.colorScheme.surface
    }
    val titleColor = if (isPlaying) Color.White else MaterialTheme.colorScheme.onSurface
    val descColor = if (isPlaying) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            1.dp,
            if (isPlaying) SaatColors.Gold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 6.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isPlaying) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = station.countryFlag,
                        fontSize = 22.sp
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isPlaying) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SaatColors.Gold
                        ) {
                            Text(
                                text = "PLAYING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = station.description(appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = descColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isPlaying) SaatColors.Gold else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClick = onPlayClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) SaatColors.DeepEmerald else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
