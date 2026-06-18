package app.kamy.qalbuApp.features.today.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.components.AlKhatibErrorStateDark
import app.kamy.qalbuApp.design.components.AlKhatibSkeletonOnDark
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.features.today.PrayerUiState
import app.kamy.qalbuApp.infrastructure.repository.PrayerEntry
import app.kamy.qalbuApp.ui.common.rememberErrorDisplay
import java.text.SimpleDateFormat
import java.util.Locale

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
    val nextPrayer = state.nextPrayer ?: state.activePrayer
    val background = masaPrayerBackground(nextPrayer)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        onClick = onOpenLocation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                PrayerCardHeader(
                    cityName = state.cityName,
                    gregorianLabel = state.gregorianLabel,
                    needsPermission = state.needsPermission
                )
                Spacer(Modifier.height(8.dp))

                if (state.isLoading && state.timings.isEmpty()) {
                    PrayerCardLoading()
                } else if (fetchErrorDisplay != null && state.timings.isEmpty()) {
                    AlKhatibErrorStateDark(
                        display = fetchErrorDisplay,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PrayerCountdownRow(
                        countdown = state.countdown,
                        subtitle = when {
                            state.needsPermission -> stringResource(R.string.prayer_allow_location)
                            state.timings.isEmpty() -> stringResource(R.string.prayer_schedule)
                            else -> state.countdownSubtitle
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(vertical = 8.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (state.isLoading || state.timings.isEmpty()) {
                        repeat(6) {
                            PrayerColumnSkeleton(modifier = Modifier.weight(1f))
                        }
                    } else {
                        state.timings.forEach { entry ->
                            PrayerTimeColumn(
                                entry = entry,
                                isActive = entry.type == state.activePrayer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerCardHeader(
    cityName: String?,
    gregorianLabel: String?,
    needsPermission: Boolean
) {
    val locating = stringResource(R.string.locating)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when {
                    needsPermission -> stringResource(R.string.prayer_allow_location)
                    !cityName.isNullOrBlank() -> cityName
                    else -> locating
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
            if (!gregorianLabel.isNullOrBlank()) {
                Text(
                    text = gregorianLabel,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PrayerCountdownRow(
    countdown: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = countdown,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Image(
            painter = painterResource(R.drawable.today_mascot),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(52.dp)
        )
    }
}

@Composable
private fun PrayerCardLoading() {
    Column(modifier = Modifier.fillMaxWidth()) {
        AlKhatibSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(28.dp),
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(Modifier.height(6.dp))
        AlKhatibSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(12.dp)
        )
    }
}

@Composable
private fun PrayerTimeColumn(
    entry: PrayerEntry,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val timeText = remember(entry.date) {
        SimpleDateFormat("HH:mm", Locale.US).format(entry.date)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color.White.copy(alpha = 0.15f) else Color.Transparent)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeText,
            color = Color.White,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = prayerDisplay(entry.type),
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.65f),
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun PrayerColumnSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlKhatibSkeletonOnDark(
            modifier = Modifier
                .width(28.dp)
                .height(10.dp)
        )
        Spacer(Modifier.height(4.dp))
        AlKhatibSkeletonOnDark(
            modifier = Modifier
                .width(24.dp)
                .height(8.dp)
        )
    }
}

@Composable
private fun prayerDisplay(type: PrayerType): String {
    val fajr = stringResource(R.string.prayer_fajr)
    val sunrise = stringResource(R.string.prayer_sunrise)
    val dhuhr = stringResource(R.string.prayer_dhuhr)
    val asr = stringResource(R.string.prayer_asr)
    val maghrib = stringResource(R.string.prayer_maghrib)
    val isha = stringResource(R.string.prayer_isha)
    return when (type) {
        PrayerType.FAJR -> fajr
        PrayerType.SUNRISE -> sunrise
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
    }
}
