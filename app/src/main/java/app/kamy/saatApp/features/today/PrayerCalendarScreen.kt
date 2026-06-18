package app.kamy.saatApp.features.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.AlKhatibErrorState
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.infrastructure.repository.PrayerCalendarDay
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerCalendarScreen(
    onBack: () -> Unit,
    vm: PrayerCalendarViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val monthLabel = DateFormatSymbols.getInstance(Locale.getDefault()).months
        .getOrNull(state.month - 1).orEmpty()
    val today = remember { Calendar.getInstance() }
    val isCurrentMonth = today.get(Calendar.YEAR) == state.year &&
        today.get(Calendar.MONTH) + 1 == state.month
    val todayDay = today.get(Calendar.DAY_OF_MONTH)
    val gridCells = remember(state.year, state.month) { buildMonthGrid(state.year, state.month) }
    val selectedDayData = if (state.daysReady) {
        state.days.firstOrNull { it.day == state.selectedDay }
    } else {
        null
    }
    val showInitialLoading = state.isLoading && state.loadedYear == null && state.days.isEmpty()
    val errorDisplay = state.error.rememberErrorDisplay(R.string.prayer_calendar_load_failed)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.prayer_calendar_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AlKhatibColors.DeepEmerald
                        )
                        state.cityName?.let { city ->
                            Text(
                                text = city,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.shiftMonth(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                    Text(
                        text = "$monthLabel ${state.year}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { vm.shiftMonth(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
                Text(
                    text = stringResource(R.string.prayer_calendar_hint),
                    modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    ) { padding ->
        when {
            showInitialLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AlKhatibColors.DeepEmerald)
                }
            }
            errorDisplay != null && state.days.isEmpty() -> {
                AlKhatibErrorState(
                    display = errorDisplay,
                    onRetry = vm::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = AlKhatibSpacing.screenHorizontal)
                )
            }
            else -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    WeekdayHeaderRow()
                    MonthGrid(
                        cells = gridCells,
                        isCurrentMonth = isCurrentMonth,
                        todayDay = todayDay,
                        selectedDay = state.selectedDay,
                        onSelectDay = vm::selectDay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AlKhatibSpacing.screenHorizontal)
                    )

                    when {
                        selectedDayData != null -> {
                            SelectedDayPrayerCard(
                                day = selectedDayData,
                                modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                            )
                        }
                        state.isLoading -> {
                            SelectedDayLoadingCard(
                                modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class MonthCell(val day: Int) {
    val key: String get() = "day_$day"
}

private fun buildMonthGrid(year: Int, month: Int): List<MonthCell?> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val mondayBasedOffset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return buildList {
        repeat(mondayBasedOffset) { add(null) }
        repeat(daysInMonth) { add(MonthCell(it + 1)) }
    }
}

@Composable
private fun MonthGrid(
    cells: List<MonthCell?>,
    isCurrentMonth: Boolean,
    todayDay: Int,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val padded = remember(cells) {
        val list = cells.toMutableList()
        while (list.size % 7 != 0) list.add(null)
        list
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        padded.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { cell ->
                    Box(Modifier.weight(1f)) {
                        if (cell == null) {
                            Spacer(Modifier.aspectRatio(1f))
                        } else {
                            val isToday = isCurrentMonth && cell.day == todayDay
                            val isSelected = cell.day == selectedDay
                            CalendarDayCell(
                                day = cell.day,
                                isToday = isToday,
                                isSelected = isSelected,
                                onClick = { onSelectDay(cell.day) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val labels = remember {
        val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
        val weekdays = symbols.shortWeekdays
        listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
            .map { weekdays[it] }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 4.dp)
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isSelected -> AlKhatibColors.DeepEmerald
        isToday -> AlKhatibColors.DeepEmerald.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> androidx.compose.ui.graphics.Color.White
        isToday -> AlKhatibColors.DeepEmerald
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .background(background)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, AlKhatibColors.DeepEmerald.copy(alpha = 0.35f), CircleShape)
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun SelectedDayLoadingCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AlKhatibColors.LightGrey
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AlKhatibColors.DeepEmerald,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.prayer_calendar_loading_day),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )
        }
    }
}

@Composable
private fun SelectedDayPrayerCard(
    day: PrayerCalendarDay,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AlKhatibColors.LightGrey
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = day.gregorianLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.Slate900
            )
            day.hijriLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            PrayerTimeGrid(day = day)
        }
    }
}

@Composable
private fun PrayerTimeGrid(day: PrayerCalendarDay) {
    val rows = listOf(
        listOf(
            stringResource(R.string.prayer_fajr) to day.fajr,
            stringResource(R.string.prayer_sunrise) to day.sunrise,
            stringResource(R.string.prayer_dhuhr) to day.dhuhr
        ),
        listOf(
            stringResource(R.string.prayer_asr) to day.asr,
            stringResource(R.string.prayer_maghrib) to day.maghrib,
            stringResource(R.string.prayer_isha) to day.isha
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, time) ->
                    PrayerTimeTile(
                        label = label,
                        time = time,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeTile(
    label: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AlKhatibColors.Slate500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = time,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate900,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
