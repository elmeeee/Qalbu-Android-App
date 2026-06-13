package app.kamy.qalbuApp.features.today.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.PrayerType
import app.kamy.qalbuApp.features.today.PrayerTrackerUiState
import app.kamy.qalbuApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.qalbuApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.qalbuApp.infrastructure.preferences.PrayerTrackerStore
import java.util.Calendar
import java.util.Locale

@Composable
fun PrayerTrackerCard(
    state: PrayerTrackerUiState,
    onTogglePrayer: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.prayer_tracker_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.DeepEmerald
                    )
                    Text(
                        text = stringResource(
                            R.string.prayer_tracker_subtitle,
                            state.todayProgress.completedCount,
                            state.todayProgress.totalCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                }
                if (state.streak > 0) {
                    StreakBadge(streak = state.streak)
                }
            }

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { state.todayProgress.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AlKhatibColors.Teal,
                trackColor = AlKhatibColors.LightGrey,
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PrayerTrackerStore.TRACKED_PRAYERS.forEach { prayer ->
                    val done = state.completedPrayers.contains(prayer)
                    PrayerCheckChip(
                        label = AppNotificationCopy.prayerDisplayName(context, prayer.aladhanKey),
                        completed = done,
                        onClick = { onTogglePrayer(prayer) }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            WeekProgressRow(weekProgress = state.weekProgress)
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AlKhatibColors.AmberWash)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = AlKhatibColors.GoldDeep,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = streak.toString(),
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.GoldDeep
        )
    }
}

@Composable
private fun PrayerCheckChip(
    label: String,
    completed: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (completed) AlKhatibColors.DeepEmerald else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (completed) AlKhatibColors.DeepEmerald else AlKhatibColors.SoftGrey,
        label = "chipBorder"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bg)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (completed) FontWeight.SemiBold else FontWeight.Normal,
            color = if (completed) AlKhatibColors.DeepEmerald else AlKhatibColors.Slate500
        )
    }
}

@Composable
private fun WeekProgressRow(
    weekProgress: List<PrayerDayProgress>
) {
    val dayLabels = rememberWeekDayLabels()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekProgress.forEachIndexed { index, day ->
            val isToday = index == weekProgress.lastIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayLabels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) AlKhatibColors.DeepEmerald else AlKhatibColors.Slate500,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(if (isToday) 28.dp else 24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                day.completedCount >= day.totalCount ->
                                    Brush.linearGradient(
                                        listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.Teal)
                                    )
                                day.completedCount > 0 -> Brush.linearGradient(
                                    listOf(
                                        AlKhatibColors.Teal.copy(alpha = 0.4f),
                                        AlKhatibColors.Teal.copy(alpha = 0.2f)
                                    )
                                )
                                else -> Brush.linearGradient(
                                    listOf(AlKhatibColors.LightGrey, AlKhatibColors.LightGrey)
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (day.completedCount >= day.totalCount) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (day.completedCount > 0) {
                        Text(
                            text = day.completedCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AlKhatibColors.DeepEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberWeekDayLabels(): List<String> {
    val symbols = java.text.DateFormatSymbols.getInstance(Locale.getDefault())
    val weekdays = symbols.shortWeekdays
    val cal = Calendar.getInstance()
    return (6 downTo 0).map { offset ->
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        weekdays[cal.get(Calendar.DAY_OF_WEEK)]
    }
}
