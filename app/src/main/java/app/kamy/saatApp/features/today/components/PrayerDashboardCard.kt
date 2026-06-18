package app.kamy.saatApp.features.today.components

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.AlKhatibErrorStateDark
import app.kamy.saatApp.design.components.AlKhatibSkeletonOnDark
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.features.today.PrayerTheme
import app.kamy.saatApp.features.today.PrayerUiState
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import app.kamy.saatApp.ui.common.rememberErrorDisplay
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
    val slotEntries = remember(state.timings) {
        PrayerType.ADZAN_NOTIFICATION_PRAYERS.map { type ->
            state.timings.find { it.type == type }
        }
    }
    val headline = rememberHeadline(state)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        onClick = onOpenCalendar
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(background)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.08f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                when {
                    state.isLoading && state.timings.isEmpty() -> PrayerCardLoading()
                    fetchErrorDisplay != null && state.timings.isEmpty() -> AlKhatibErrorStateDark(
                        display = fetchErrorDisplay,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = headline.label,
                                    color = WidgetGoldLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.6.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = headline.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (headline.subtitle.isNotBlank()) {
                                    Text(
                                        text = headline.subtitle,
                                        color = Color.White.copy(alpha = 0.82f),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            AnimatedContent(
                                targetState = state.countdown,
                                transitionSpec = {
                                    (fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                                        scaleIn(initialScale = 0.94f))
                                        .togetherWith(fadeOut() + scaleOut(targetScale = 0.94f))
                                },
                                label = "prayerCountdown"
                            ) { countdown ->
                                Text(
                                    text = countdown,
                                    color = WidgetGold,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PrayerType.ADZAN_NOTIFICATION_PRAYERS.forEachIndexed { index, type ->
                                val entry = slotEntries[index]
                                SchedulePrayerSlot(
                                    type = type,
                                    entry = entry,
                                    isActive = entry?.type == state.activePrayer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PrayerHeadline(
    val label: String,
    val title: String,
    val subtitle: String
)

@Composable
private fun rememberHeadline(state: PrayerUiState): PrayerHeadline {
    val nextLabel = stringResource(R.string.prayer_widget_next_label)
    val inProgress = stringResource(R.string.prayer_widget_in_progress)
    val schedule = stringResource(R.string.prayer_schedule)
    val allowLocation = stringResource(R.string.prayer_allow_location)
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
        state.needsPermission,
        state.isGracePeriod,
        state.activePrayer,
        state.nextPrayer,
        state.countdownSubtitle,
        state.timings,
        state.khgtToday?.eventTitle
    ) {
        when {
            state.needsPermission -> PrayerHeadline(
                label = schedule,
                title = allowLocation,
                subtitle = ""
            )
            state.isGracePeriod && state.activePrayer != null -> PrayerHeadline(
                label = inProgress,
                title = name(state.activePrayer!!),
                subtitle = state.countdownSubtitle
            )
            state.nextPrayer != null -> {
                val prayerName = name(state.nextPrayer!!)
                val time = state.timings.find { it.type == state.nextPrayer }?.date?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                } ?: "--:--"
                PrayerHeadline(
                    label = nextLabel,
                    title = prayerName,
                    subtitle = "$prayerName · $time"
                )
            }
            else -> PrayerHeadline(
                label = nextLabel,
                title = state.countdownSubtitle.ifBlank { schedule },
                subtitle = state.khgtToday?.eventTitle.orEmpty()
            )
        }
    }
}

@Composable
private fun SchedulePrayerSlot(
    type: PrayerType,
    entry: PrayerEntry?,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val timeText = entry?.date?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
    } ?: "--:--"
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "slotScale"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isActive) WidgetGoldLabel else Color.White.copy(alpha = 0.72f),
        label = "slotLabel"
    )
    val timeColor by animateColorAsState(
        targetValue = if (isActive) WidgetGold else Color.White,
        label = "slotTime"
    )
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (isActive) Modifier.background(Color.White.copy(alpha = 0.14f)) else Modifier
            )
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = prayerDisplayShort(type),
            color = labelColor,
            fontSize = 10.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = timeText,
            color = timeColor,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PrayerCardLoading() {
    Column(modifier = Modifier.fillMaxWidth()) {
        AlKhatibSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(14.dp)
        )
        Spacer(Modifier.height(8.dp))
        AlKhatibSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(28.dp)
        )
        Spacer(Modifier.height(16.dp))
        AlKhatibSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        )
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
