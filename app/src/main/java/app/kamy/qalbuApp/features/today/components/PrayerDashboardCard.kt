package app.kamy.qalbuApp.features.today.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.components.AlKhatibSkeletonOnDark
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.features.today.PrayerTheme
import app.kamy.qalbuApp.features.today.PrayerUiState
import app.kamy.qalbuApp.infrastructure.repository.PrayerEntry
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PrayerDashboardCard(
    state: PrayerUiState,
    modifier: Modifier = Modifier
) {
    val brush = when (state.theme) {
        PrayerTheme.DAYLIGHT -> Brush.linearGradient(
            listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)
        )
        PrayerTheme.NIGHT -> Brush.linearGradient(
            listOf(AlKhatibColors.EmeraldNight, AlKhatibColors.DeepEmerald)
        )
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(brush)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.06f))
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        if (state.isLoading && state.timings.isEmpty()) {
            AlKhatibSkeletonOnDark(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.height(8.dp))
            AlKhatibSkeletonOnDark(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(12.dp)
            )
        } else {
            Text(
                text = state.countdown,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = when {
                    state.needsPermission -> "Allow location to see prayer times"
                    state.timings.isEmpty() -> "Prayer schedule"
                    else -> state.countdownSubtitle
                },
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.15f))
                .padding(vertical = 10.dp, horizontal = 4.dp),
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
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) Color.White.copy(alpha = 0.12f) else Color.Transparent)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeText,
            color = Color.White,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = prayerDisplay(entry.type),
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun PrayerColumnSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp),
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

private fun prayerDisplay(type: PrayerType): String = when (type) {
    PrayerType.FAJR -> "Fajr"
    PrayerType.SUNRISE -> "Sunrise"
    PrayerType.DHUHR -> "Dhuhr"
    PrayerType.ASR -> "Asr"
    PrayerType.MAGHRIB -> "Maghrib"
    PrayerType.ISHA -> "Isha"
}
