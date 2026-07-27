package app.kamy.saatApp.features.today.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.SaatErrorStateDark
import app.kamy.saatApp.design.components.SaatSkeletonOnDark
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.features.today.PrayerUiState
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import java.text.SimpleDateFormat
import java.util.Locale

@DrawableRes
private fun getPrayerCardDrawable(type: PrayerType?): Int {
    return when (type) {
        PrayerType.FAJR -> R.drawable.fajr_card
        PrayerType.DHUHR, PrayerType.ASR -> R.drawable.dhur_asr_card
        PrayerType.MAGHRIB -> R.drawable.maghrib_card
        PrayerType.ISHA -> R.drawable.isha_card
        else -> R.drawable.dhur_asr_card
    }
}

@DrawableRes
private fun getPrayerIconRes(type: PrayerType, isActive: Boolean): Int {
    return when (type) {
        PrayerType.FAJR -> if (isActive) R.drawable.ic_fajr_on else R.drawable.ic_fajr_off
        PrayerType.DHUHR -> if (isActive) R.drawable.ic_dhur_on else R.drawable.ic_dhur_off
        PrayerType.ASR -> if (isActive) R.drawable.ic_asr_on else R.drawable.ic_asr_off
        PrayerType.MAGHRIB -> if (isActive) R.drawable.ic_maghrib_on else R.drawable.ic_maghrib_off
        PrayerType.ISHA -> if (isActive) R.drawable.ic_isha_on else R.drawable.ic_isha_off
        else -> if (isActive) R.drawable.ic_dhur_on else R.drawable.ic_dhur_off
    }
}

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

    val slotEntries = remember(state.timings) {
        PrayerType.ADZAN_NOTIFICATION_PRAYERS.map { type ->
            state.timings.find { it.type == type }
        }
    }
    val headline = rememberHeadline(state)
    val targetPrayer = state.nextPrayer ?: state.activePrayer ?: PrayerType.DHUHR
    val cardDrawable = getPrayerCardDrawable(targetPrayer)

    val nextPrayerEntry = state.timings.find { it.type == targetPrayer }
    val formattedTime = nextPrayerEntry?.date?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: ""

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Card: Prayer Info Header Card with Illustration & Linear Gradient (#085E43 -> #F7DC8B)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            shadowElevation = 4.dp,
            onClick = onOpenCalendar
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF085E43),
                                Color(0xFFF7DC8B)
                            )
                        )
                    )
            ) {
                // Background Card Illustration Image (h = 99dp, aligned to bottom edge "mentok kebawah", starting at Y = 21dp)
                Image(
                    painter = painterResource(cardDrawable),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxWidth()
                        .height(99.dp)
                )

                // Text Overlay Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    when {
                        state.isLoading && state.timings.isEmpty() -> PrayerCardLoading()
                        fetchErrorDisplay != null && state.timings.isEmpty() -> SaatErrorStateDark(
                            display = fetchErrorDisplay,
                            onRetry = onRetry,
                            modifier = Modifier.fillMaxWidth()
                        )
                        else -> {
                            Column {
                                Text(
                                    text = headline.label,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = headline.title,
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (formattedTime.isNotBlank()) {
                                        Text(
                                            text = formattedTime,
                                            color = Color.White,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            if (state.countdown.isNotBlank()) {
                                AnimatedContent(
                                    targetState = state.countdown,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "prayerCountdown"
                                ) { countdown ->
                                    Text(
                                        text = if (countdown.endsWith("remaining")) countdown else "$countdown remaining",
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Bottom Card: 5 Prayer Schedule Slots Row with SVG Icons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrayerType.ADZAN_NOTIFICATION_PRAYERS.forEachIndexed { index, type ->
                    val entry = slotEntries[index]
                    // Highlight active ONLY when timings are loaded and entry matches activePrayer
                    val isActive = !state.isLoading && state.timings.isNotEmpty() && entry != null && state.activePrayer != null && entry.type == state.activePrayer
                    SchedulePrayerSlot(
                        type = type,
                        entry = entry,
                        isActive = isActive,
                        modifier = Modifier.weight(1f)
                    )
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

    val iconRes = getPrayerIconRes(type, isActive)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isActive) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF085E43),
                                Color(0xFF15AA7C)
                            )
                        )
                    )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = prayerDisplayShort(type),
                color = if (isActive) Color.White else Color(0xFF262626),
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = if (isActive) Color.White else Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = timeText,
                color = if (isActive) Color.White else Color(0xFF1E293B),
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PrayerCardLoading() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SaatSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(14.dp)
        )
        Spacer(Modifier.height(8.dp))
        SaatSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(28.dp)
        )
        Spacer(Modifier.height(16.dp))
        SaatSkeletonOnDark(
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
