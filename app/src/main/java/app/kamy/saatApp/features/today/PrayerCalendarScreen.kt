package app.kamy.saatApp.features.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.SaatErrorState
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.KhgtTodayInfo
import app.kamy.saatApp.features.today.components.TodayImportantDayBanner
import app.kamy.saatApp.features.tools.components.SpiritualToolTopBar
import app.kamy.saatApp.infrastructure.repository.PrayerCalendarDay
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

// ─── Theme tokens (shared cream / dark-green look used across Tools screens) ──

private val CardCream = Color(0xFFFFFDF7)
private val CardLine = Color(0xFFF0E9DC)
private val TextMuted = Color(0xFF7A8580)
private val TextInk = Color(0xFF1F2A24)

@Composable
fun PrayerCalendarScreen(
    onBack: () -> Unit,
    vm: PrayerCalendarViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
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
    val hijriHeaderLabel = remember(state.days) { buildHijriRangeLabel(state.days) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.HomeBg)
    ) {
        SpiritualToolTopBar(
            title = stringResource(R.string.prayer_calendar_title),
            subtitle = state.cityName,
            onBack = onBack
        )

        when {
            showInitialLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SaatColors.HomeDarkGreen)
                }
            }
            errorDisplay != null && state.days.isEmpty() -> {
                SaatErrorState(
                    display = errorDisplay,
                    onRetry = vm::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                )
            }
            else -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MonthCard(
                        monthLabel = "$monthLabel ${state.year}",
                        hijriLabel = hijriHeaderLabel,
                        cells = gridCells,
                        days = state.days,
                        isCurrentMonth = isCurrentMonth,
                        todayDay = todayDay,
                        selectedDay = state.selectedDay,
                        onPrevious = { vm.shiftMonth(-1) },
                        onNext = { vm.shiftMonth(1) },
                        onSelectDay = vm::selectDay
                    )

                    AnimatedContent(
                        targetState = selectedDayData,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "selected_prayer_day"
                    ) { day ->
                        when {
                            day != null -> SelectedDayPrayerCard(
                                day = day,
                                isToday = isCurrentMonth && day.day == todayDay,
                                cityName = state.cityName
                            )
                            state.isLoading -> SelectedDayLoadingCard()
                            else -> Spacer(Modifier.height(1.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Month card ─────────────────────────────────────────────────────────────

@Composable
private fun MonthCard(
    monthLabel: String,
    hijriLabel: String?,
    cells: List<MonthCell?>,
    days: List<PrayerCalendarDay>,
    isCurrentMonth: Boolean,
    todayDay: Int,
    selectedDay: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectDay: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardCream,
        border = BorderStroke(1.dp, CardLine)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthNavButton(icon = Icons.Filled.ChevronLeft, onClick = onPrevious)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.HomeDarkGreen,
                        textAlign = TextAlign.Center
                    )
                    if (!hijriLabel.isNullOrBlank()) {
                        Text(
                            text = hijriLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.Gold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                MonthNavButton(icon = Icons.Filled.ChevronRight, onClick = onNext)
            }

            Spacer(Modifier.height(12.dp))
            WeekdayHeaderRow()
            Spacer(Modifier.height(6.dp))

            MonthGrid(
                cells = cells,
                days = days,
                isCurrentMonth = isCurrentMonth,
                todayDay = todayDay,
                selectedDay = selectedDay,
                onSelectDay = onSelectDay
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = CardLine)
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(SaatColors.GoldDeep)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.prayer_calendar_hint),
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun MonthNavButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SaatColors.PureWhite)
            .border(1.dp, CardLine, CircleShape)
    ) {
        Icon(icon, contentDescription = null, tint = SaatColors.HomeDarkGreen, modifier = Modifier.size(20.dp))
    }
}

private data class MonthCell(val day: Int)

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

private fun buildHijriRangeLabel(days: List<PrayerCalendarDay>): String? {
    if (days.isEmpty()) return null
    val firstHijri = days.firstOrNull { !it.hijriLabel.isNullOrBlank() }?.hijriLabel
    val lastHijri = days.lastOrNull { !it.hijriLabel.isNullOrBlank() }?.hijriLabel

    fun extractMonthYear(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val parts = raw.trim().split(" ")
        return if (parts.size >= 3) parts.drop(1).joinToString(" ") else raw
    }

    val firstMonthYear = extractMonthYear(firstHijri) ?: return null
    val lastMonthYear = extractMonthYear(lastHijri)

    return when {
        lastMonthYear == null || firstMonthYear == lastMonthYear -> "$firstMonthYear H"
        else -> {
            val firstParts = firstMonthYear.split(" ")
            val lastParts = lastMonthYear.split(" ")
            if (firstParts.size >= 2 && lastParts.size >= 2 && firstParts.last() == lastParts.last()) {
                val firstMonth = firstParts.dropLast(1).joinToString(" ")
                val lastMonth = lastParts.dropLast(1).joinToString(" ")
                "$firstMonth - $lastMonth ${lastParts.last()} H"
            } else {
                "$firstMonthYear - $lastMonthYear H"
            }
        }
    }
}

@Composable
private fun MonthGrid(
    cells: List<MonthCell?>,
    days: List<PrayerCalendarDay>,
    isCurrentMonth: Boolean,
    todayDay: Int,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit
) {
    val padded = remember(cells) {
        val list = cells.toMutableList()
        while (list.size % 7 != 0) list.add(null)
        list
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        padded.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { cell ->
                    Box(Modifier.weight(1f)) {
                        if (cell == null) {
                            Spacer(Modifier.aspectRatio(1f))
                        } else {
                            val dayData = remember(days, cell.day) { days.firstOrNull { it.day == cell.day } }
                            CalendarDayCell(
                                day = cell.day,
                                hijriDay = dayData?.hijriDay,
                                isToday = isCurrentMonth && cell.day == todayDay,
                                isSelected = cell.day == selectedDay,
                                isImportantDay = dayData?.isImportantDay == true,
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
        val weekdays = DateFormatSymbols.getInstance(Locale.getDefault()).shortWeekdays
        listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        ).map { weekdays[it] }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 11.sp,
                color = if (index == 4) SaatColors.HomeDarkGreen else TextMuted,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
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
    hijriDay: Int?,
    isToday: Boolean,
    isSelected: Boolean,
    isImportantDay: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val background = when {
        isSelected -> SaatColors.HomeDarkGreen
        isToday -> Color(0xFFE4F2EA)
        else -> SaatColors.ArcInactive
    }
    val textColor = when {
        isSelected -> Color.White
        isToday -> SaatColors.HomeDarkGreen
        else -> TextInk
    }
    val hijriColor = when {
        isSelected -> SaatColors.ArcGold
        else -> SaatColors.GoldDeep
    }
    val arabicDayStr = remember(hijriDay) { if (hijriDay != null) toEasternArabicDigits(hijriDay) else "" }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(background)
            .then(
                when {
                    isSelected -> Modifier.border(2.dp, SaatColors.ArcGold, shape)
                    isToday -> Modifier.border(1.5.dp, SaatColors.HomeDarkGreen, shape)
                    else -> Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = day.toString(),
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
            if (arabicDayStr.isNotEmpty()) {
                Text(
                    text = arabicDayStr,
                    fontSize = 9.5.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = hijriColor
                )
            }
            if (isImportantDay) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SaatColors.ArcGold else SaatColors.GoldDeep)
                )
            }
        }
    }
}

// ─── Selected day card ──────────────────────────────────────────────────────

@Composable
private fun SelectedDayLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardCream,
        border = BorderStroke(1.dp, CardLine)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = SaatColors.HomeDarkGreen,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.prayer_calendar_loading_day),
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SelectedDayPrayerCard(
    day: PrayerCalendarDay,
    isToday: Boolean,
    cityName: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardCream,
        border = BorderStroke(1.dp, CardLine)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.gregorianLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.HomeDarkGreen
                    )
                    day.hijriLabel?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.Gold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                if (isToday) {
                    Surface(shape = RoundedCornerShape(10.dp), color = SaatColors.HomeDarkGreen) {
                        Text(
                            text = stringResource(R.string.today_label),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.ArcGold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            cityName?.takeIf { it.isNotBlank() }?.let { city ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = city,
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
            Triple(stringResource(R.string.prayer_fajr), day.fajr, true),
            Triple(stringResource(R.string.prayer_sunrise), day.sunrise, false),
            Triple(stringResource(R.string.prayer_dhuhr), day.dhuhr, true)
        ),
        listOf(
            Triple(stringResource(R.string.prayer_asr), day.asr, true),
            Triple(stringResource(R.string.prayer_maghrib), day.maghrib, true),
            Triple(stringResource(R.string.prayer_isha), day.isha, true)
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, time, isObligatory) ->
                    PrayerTimeTile(
                        label = label,
                        time = time,
                        isObligatory = isObligatory,
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
    isObligatory: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isObligatory) SaatColors.PureWhite else SaatColors.ArcInactive)
            .border(1.dp, CardLine, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(if (isObligatory) R.drawable.ic_live_mosque else R.drawable.ic_daily_time_custom),
                contentDescription = null,
                tint = if (isObligatory) SaatColors.HomeDarkGreen else SaatColors.GoldDeep,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = time,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isObligatory) TextInk else TextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
