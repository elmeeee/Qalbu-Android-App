package app.kamy.qalbuApp.features.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.qalbuApp.infrastructure.preferences.PrayerTrackerStore
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@Composable
fun PrayerTrackerCalendarScreen(
    onBack: () -> Unit,
    vm: PrayerTrackerCalendarViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val monthLabel = remember(state.year, state.month) {
        val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
        val monthName = symbols.months.getOrNull(state.month - 1).orEmpty()
        "$monthName ${state.year}"
    }
    val todayKey = PrayerTrackerStore.todayKey()
    val leadingBlanks = remember(state.year, state.month) {
        Calendar.getInstance().apply {
            set(state.year, state.month - 1, 1)
        }.get(Calendar.DAY_OF_WEEK).let { dow ->
            (dow - Calendar.getInstance().firstDayOfWeek + 7) % 7
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.prayer_tracker_calendar_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
                Text(
                    text = stringResource(R.string.prayer_tracker_calendar_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChallengeStat(
                    icon = { Icon(Icons.Filled.LocalFireDepartment, null, tint = AlKhatibColors.GoldDeep) },
                    value = state.streak.toString(),
                    label = stringResource(R.string.prayer_streak_current)
                )
                ChallengeStat(
                    value = state.bestStreak.toString(),
                    label = stringResource(R.string.prayer_streak_best)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = vm::previousMonth) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.previous_month))
            }
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.DeepEmerald
            )
            IconButton(onClick = vm::nextMonth) {
                Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.next_month))
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdayHeaders().forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = AlKhatibColors.Slate500,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(count = leadingBlanks) {
                Spacer(Modifier.aspectRatio(1f))
            }
            itemsIndexed(state.days) { index, day ->
                val dayNumber = index + 1
                CalendarDayCell(
                    dayNumber = dayNumber,
                    progress = day,
                    isToday = day.dayKey == todayKey
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ChallengeStat(
    value: String,
    label: String,
    icon: @Composable (() -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.invoke()
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald,
                modifier = Modifier.padding(start = if (icon != null) 4.dp else 0.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AlKhatibColors.Slate500
        )
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    progress: PrayerDayProgress,
    isToday: Boolean
) {
    val complete = progress.isPerfectDay
    val partial = progress.completedCount > 0
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    complete -> Brush.linearGradient(
                        listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.Teal)
                    )
                    partial -> Brush.linearGradient(
                        listOf(
                            AlKhatibColors.Teal.copy(alpha = 0.25f),
                            AlKhatibColors.Teal.copy(alpha = 0.12f)
                        )
                    )
                    else -> Brush.linearGradient(
                        listOf(AlKhatibColors.LightGrey, AlKhatibColors.LightGrey)
                    )
                }
            )
            .then(
                if (isToday) {
                    Modifier.border(2.dp, AlKhatibColors.GoldBright, RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    complete -> Color.White
                    partial -> AlKhatibColors.DeepEmerald
                    else -> AlKhatibColors.Slate500
                }
            )
            if (complete) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            } else if (partial) {
                Text(
                    text = "${progress.completedCount}/${progress.totalCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AlKhatibColors.DeepEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun weekdayHeaders(): List<String> {
    val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
    val weekdays = symbols.shortWeekdays
    val first = Calendar.getInstance().firstDayOfWeek
    return (0 until 7).map { offset ->
        weekdays[(first + offset - 1) % 7 + 1]
    }
}
