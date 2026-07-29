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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.SaatErrorState
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.infrastructure.repository.PrayerCalendarDay
import app.kamy.saatApp.features.today.components.TodayImportantDayBanner
import app.kamy.saatApp.domain.model.KhgtTodayInfo
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
                            color = SaatColors.DeepEmerald
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
                val hijriHeaderLabel = remember(state.days) {
                    state.days.firstOrNull { !it.hijriLabel.isNullOrBlank() }?.hijriLabel?.let { raw ->
                        val parts = raw.trim().split(" ")
                        if (parts.size >= 3) {
                            "${parts.drop(1).joinToString(" ")} H"
                        } else {
                            "$raw H"
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.shiftMonth(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$monthLabel ${state.year}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (!hijriHeaderLabel.isNullOrBlank()) {
                            Text(
                                text = hijriHeaderLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SaatColors.DeepEmerald,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    IconButton(onClick = { vm.shiftMonth(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
                Text(
                    text = stringResource(R.string.prayer_calendar_hint),
                    modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal),
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
                    CircularProgressIndicator(color = SaatColors.DeepEmerald)
                }
            }
            errorDisplay != null && state.days.isEmpty() -> {
                SaatErrorState(
                    display = errorDisplay,
                    onRetry = vm::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = SaatSpacing.screenHorizontal)
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
                        days = state.days,
                        isCurrentMonth = isCurrentMonth,
                        todayDay = todayDay,
                        selectedDay = state.selectedDay,
                        onSelectDay = vm::selectDay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SaatSpacing.screenHorizontal)
                    )

                    when {
                        selectedDayData != null -> {
                            SelectedDayPrayerCard(
                                day = selectedDayData,
                                modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal)
                            )
                        }
                        state.isLoading -> {
                            SelectedDayLoadingCard(
                                modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal)
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
    days: List<PrayerCalendarDay>,
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
                            val isImportant = remember(days, cell.day) {
                                days.firstOrNull { it.day == cell.day }?.isImportantDay == true
                            }
                            CalendarDayCell(
                                day = cell.day,
                                isToday = isToday,
                                isSelected = isSelected,
                                isImportantDay = isImportant,
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
            .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 4.dp)
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

private fun toEasternArabicDigits(number: Int): String {
    val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { ch ->
        if (ch in '0'..'9') digits[ch - '0'] else ch
    }.joinToString("")
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isImportantDay: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isSelected -> SaatColors.DeepEmerald
        isToday -> SaatColors.DeepEmerald.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> androidx.compose.ui.graphics.Color.White
        isToday -> SaatColors.DeepEmerald
        else -> MaterialTheme.colorScheme.onSurface
    }
    val arabicTextColor = when {
        isSelected -> androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
        isToday -> SaatColors.DeepEmerald.copy(alpha = 0.9f)
        else -> SaatColors.GoldDeep // High-contrast gold color distinct from background surface
    }

    val arabicDayStr = remember(day) { toEasternArabicDigits(day) }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.5.dp, SaatColors.DeepEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
            Text(
                text = arabicDayStr,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = arabicTextColor
            )
            if (isImportantDay) {
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) androidx.compose.ui.graphics.Color.White else SaatColors.GoldDeep)
                )
            }
        }
    }
}

@Composable
private fun SelectedDayLoadingCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.LightGrey
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = SaatColors.DeepEmerald,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.prayer_calendar_loading_day),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500
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
        color = SaatColors.LightGrey
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = day.gregorianLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )
            day.hijriLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            day.khgtEventTitle?.let { title ->
                Spacer(Modifier.height(12.dp))
                TodayImportantDayBanner(
                    info = KhgtTodayInfo(
                        hijriLabel = day.hijriLabel.orEmpty(),
                        gregorianLabel = day.gregorianLabel,
                        pasaran = null,
                        eventTitle = title,
                        isImportantDay = true
                    ),
                    modifier = Modifier.fillMaxWidth()
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
            color = SaatColors.Slate500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = time,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = SaatColors.Slate900,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
