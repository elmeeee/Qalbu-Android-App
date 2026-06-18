package app.kamy.qalbuApp.features.today.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.components.AlKhatibErrorStateDark
import app.kamy.qalbuApp.design.components.AlKhatibSkeletonOnDark
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.features.today.PrayerTheme
import app.kamy.qalbuApp.features.today.PrayerUiState
import app.kamy.qalbuApp.infrastructure.repository.PrayerEntry
import app.kamy.qalbuApp.ui.common.rememberErrorDisplay
import java.text.SimpleDateFormat
import java.util.Locale

private val WidgetGold = Color(0xFFD4AF37)
private val WidgetGoldLabel = Color(0xFFE8D5A3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerDashboardCard(
    state: PrayerUiState,
    onRetry: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val fetchErrorDisplay = state.error
        ?.takeIf { !state.needsPermission }
        ?.rememberErrorDisplay(R.string.error_prayer_fetch_title)
    val background = when (state.theme) {
        PrayerTheme.DAYLIGHT -> Brush.linearGradient(
            listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)
        )
        PrayerTheme.NIGHT -> Brush.linearGradient(
            listOf(AlKhatibColors.EmeraldNight, AlKhatibColors.DeepEmerald)
        )
    }
    val slots = remember(state.timings) {
        PrayerType.ADZAN_NOTIFICATION_PRAYERS.mapNotNull { type ->
            state.timings.find { it.type == type }
        }
    }
    val nextLine = rememberNextLine(state)
    val compactCountdown = remember(state.countdown) { formatCountdownCompact(state.countdown) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        onClick = onOpenLocation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(background)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.06f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            when {
                state.isLoading && state.timings.isEmpty() -> PrayerWidgetLoading()
                fetchErrorDisplay != null && state.timings.isEmpty() -> AlKhatibErrorStateDark(
                    display = fetchErrorDisplay,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
                else -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.15f)
                            .padding(end = 6.dp)
                    ) {
                        Text(
                            text = when {
                                state.needsPermission -> stringResource(R.string.prayer_allow_location)
                                state.isGracePeriod -> stringResource(R.string.prayer_widget_in_progress)
                                else -> stringResource(R.string.prayer_widget_next_label)
                            },
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = nextLine,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.weight(2.2f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (slots.isEmpty()) {
                            repeat(5) {
                                PrayerSlotSkeleton(modifier = Modifier.weight(1f))
                            }
                        } else {
                            slots.forEach { entry ->
                                WidgetPrayerSlot(
                                    entry = entry,
                                    isActive = entry.type == state.activePrayer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = compactCountdown,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .widthIn(min = 68.dp),
                        transitionSpec = {
                            (fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                                scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                                .togetherWith(
                                    fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                                        scaleOut(targetScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                                )
                        },
                        label = "widgetCountdown"
                    ) { value ->
                        Text(
                            text = value,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberNextLine(state: PrayerUiState): String {
    val scheduleFallback = stringResource(R.string.prayer_schedule)
    val fajr = stringResource(R.string.prayer_fajr)
    val dhuhr = stringResource(R.string.prayer_dhuhr)
    val asr = stringResource(R.string.prayer_asr)
    val maghrib = stringResource(R.string.prayer_maghrib)
    val isha = stringResource(R.string.prayer_isha)
    fun name(type: PrayerType) = when (type) {
        PrayerType.FAJR -> fajr
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
        PrayerType.SUNRISE -> ""
    }
    return remember(
        state.isGracePeriod,
        state.activePrayer,
        state.nextPrayer,
        state.countdownSubtitle,
        state.timings,
        state.needsPermission,
        state.khgtToday?.eventTitle,
        scheduleFallback,
        fajr,
        dhuhr,
        asr,
        maghrib,
        isha
    ) {
        when {
            state.needsPermission -> scheduleFallback
            state.khgtToday?.eventTitle?.isNotBlank() == true -> state.khgtToday!!.eventTitle!!
            state.isGracePeriod && state.activePrayer != null -> name(state.activePrayer!!)
            state.nextPrayer != null -> {
                val prayerName = name(state.nextPrayer!!)
                val time = state.timings.find { it.type == state.nextPrayer }?.date?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                } ?: "--:--"
                "$prayerName · $time"
            }
            else -> state.countdownSubtitle
        }
    }
}

@Composable
private fun WidgetPrayerSlot(
    entry: PrayerEntry,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val timeText = remember(entry.date) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(entry.date)
    }
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.06f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "slotScale"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isActive) WidgetGoldLabel else Color.White.copy(alpha = 0.78f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "slotLabel"
    )
    val timeColor by animateColorAsState(
        targetValue = if (isActive) WidgetGold else Color.White,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "slotTime"
    )
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isActive) {
                    Modifier.background(Color.White.copy(alpha = 0.12f))
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = prayerDisplayShort(entry.type),
            color = labelColor,
            fontSize = 8.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = timeText,
            color = timeColor,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PrayerWidgetLoading() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1.15f)) {
            AlKhatibSkeletonOnDark(Modifier.fillMaxWidth(0.7f).padding(vertical = 4.dp))
            AlKhatibSkeletonOnDark(Modifier.fillMaxWidth(0.9f).padding(top = 4.dp))
        }
        repeat(5) {
            PrayerSlotSkeleton(Modifier.weight(1f))
        }
        AlKhatibSkeletonOnDark(Modifier.widthIn(min = 56.dp))
    }
}

@Composable
private fun PrayerSlotSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlKhatibSkeletonOnDark(Modifier.padding(vertical = 2.dp))
        AlKhatibSkeletonOnDark(Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun prayerDisplayShort(type: PrayerType): String = when (type) {
    PrayerType.FAJR -> stringResource(R.string.prayer_fajr)
    PrayerType.DHUHR -> stringResource(R.string.prayer_dhuhr)
    PrayerType.ASR -> stringResource(R.string.prayer_asr)
    PrayerType.MAGHRIB -> stringResource(R.string.prayer_maghrib)
    PrayerType.ISHA -> stringResource(R.string.prayer_isha)
    PrayerType.SUNRISE -> ""
}

private fun formatCountdownCompact(raw: String): String {
    val parts = raw.split(":").mapNotNull { it.toIntOrNull() }
    if (parts.size != 3) return raw
    val (hours, minutes, seconds) = parts
    return if (hours > 0) {
        "%d:%02d".format(hours, minutes)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
