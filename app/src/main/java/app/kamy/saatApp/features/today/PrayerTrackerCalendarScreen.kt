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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.features.tools.components.SpiritualToolTopBar
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─── Theme tokens (shared cream / dark-green look used across Tools screens) ──

private val CardCream = Color(0xFFFFFDF7)
private val CardLine = Color(0xFFF0E9DC)
private val CellIdle = SaatColors.ArcInactive
private val CellPartial = Color(0xFFE4F2EA)
private val TextMuted = Color(0xFF7A8580)

@Composable
fun PrayerTrackerCalendarScreen(
    onBack: () -> Unit,
    onOpenSunnahPrayer: () -> Unit = {},
    onOpenQuran: () -> Unit = {},
    onOpenDhikr: () -> Unit = {},
    vm: PrayerTrackerCalendarViewModel = hiltViewModel(),
    trackerVm: PrayerTrackerViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentLang = remember(context) { AppLanguageStore.from(context).current() }
    val strings = remember(currentLang) { TrackerCalendarStrings.forLanguage(currentLang) }

    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        vm.refresh()
        onPauseOrDispose {}
    }

    val monthLabel = remember(state.year, state.month) {
        val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
        val monthName = symbols.months.getOrNull(state.month - 1).orEmpty()
        "$monthName ${state.year}"
    }

    val todayKey = PrayerTrackerStore.todayKey()
    var selectedDayKey by remember { mutableStateOf(todayKey) }

    val leadingBlanks = remember(state.year, state.month) {
        Calendar.getInstance().apply {
            set(state.year, state.month - 1, 1)
        }.get(Calendar.DAY_OF_WEEK).let { dow ->
            (dow - Calendar.getInstance().firstDayOfWeek + 7) % 7
        }
    }

    val perfectDays = remember(state.days) { state.days.count { it.isPerfectDay } }
    val activeDays = remember(state.days) { state.days.count { it.completedCount > 0 } }
    val completion = remember(state.days, todayKey) {
        val elapsed = state.days.filter { it.dayKey <= todayKey }
        if (elapsed.isEmpty()) 0 else {
            val total = elapsed.sumOf { it.totalCount }.coerceAtLeast(1)
            (elapsed.sumOf { it.completedCount } * 100 / total)
        }
    }

    val selectedDay = remember(state.days, selectedDayKey) {
        state.days.firstOrNull { it.dayKey == selectedDayKey }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(SaatColors.HomeBg)
    ) {
        SpiritualToolTopBar(
            title = strings.title,
            subtitle = strings.subtitle,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StreakHeroCard(
                    streak = state.streak,
                    bestStreak = state.bestStreak,
                    perfectDays = perfectDays,
                    completion = completion,
                    strings = strings
                )
            }

            item {
                MonthCalendarCard(
                    monthLabel = monthLabel,
                    activeDays = activeDays,
                    leadingBlanks = leadingBlanks,
                    days = state.days,
                    todayKey = todayKey,
                    selectedDayKey = selectedDayKey,
                    onPrevious = vm::previousMonth,
                    onNext = vm::nextMonth,
                    onSelectDay = { selectedDayKey = it },
                    strings = strings
                )
            }

            item {
                AnimatedContent(
                    targetState = selectedDay,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "selected_day_card"
                ) { day ->
                    if (day != null) {
                        SelectedDayCard(
                            day = day,
                            isToday = day.dayKey == todayKey,
                            strings = strings,
                            onOpenQuran = onOpenQuran,
                            onOpenDhikr = onOpenDhikr,
                            onOpenSunnahPrayer = onOpenSunnahPrayer
                        )
                    } else {
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

// ─── Streak hero ────────────────────────────────────────────────────────────

@Composable
private fun StreakHeroCard(
    streak: Int,
    bestStreak: Int,
    perfectDays: Int,
    completion: Int,
    strings: TrackerCalendarStrings
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(SaatColors.PrimaryGradientColors))
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 240.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_streak_custom),
                        contentDescription = null,
                        tint = SaatColors.ArcGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = strings.currentStreak,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = streak.toString(),
                            fontSize = 36.sp,
                            lineHeight = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = strings.days,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.ArcGold,
                            modifier = Modifier.padding(bottom = 7.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeroStatTile(value = "$bestStreak", label = strings.bestStreak, modifier = Modifier.weight(1f))
                HeroStatTile(value = "$perfectDays", label = strings.perfectDays, modifier = Modifier.weight(1f))
                HeroStatTile(value = "$completion%", label = strings.completion, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 2
        )
    }
}

// ─── Month calendar card ────────────────────────────────────────────────────

@Composable
private fun MonthCalendarCard(
    monthLabel: String,
    activeDays: Int,
    leadingBlanks: Int,
    days: List<PrayerDayProgress>,
    todayKey: String,
    selectedDayKey: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectDay: (String) -> Unit,
    strings: TrackerCalendarStrings
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
                        color = SaatColors.HomeDarkGreen
                    )
                    Text(
                        text = strings.activeDaysFormat(activeDays),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                MonthNavButton(icon = Icons.Filled.ChevronRight, onClick = onNext)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth()) {
                weekdayHeaders().forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            val cells: List<PrayerDayProgress?> = remember(leadingBlanks, days) {
                val list = mutableListOf<PrayerDayProgress?>()
                repeat(leadingBlanks) { list.add(null) }
                list.addAll(days)
                while (list.size % 7 != 0) list.add(null)
                list
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { day ->
                            Box(Modifier.weight(1f)) {
                                if (day == null) {
                                    Spacer(Modifier.aspectRatio(1f))
                                } else {
                                    val dayNumber = day.dayKey.takeLast(2).toIntOrNull() ?: 0
                                    CalendarDayCell(
                                        dayNumber = dayNumber,
                                        progress = day,
                                        isToday = day.dayKey == todayKey,
                                        isSelected = day.dayKey == selectedDayKey,
                                        isFuture = day.dayKey > todayKey,
                                        onClick = { onSelectDay(day.dayKey) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = CardLine)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(color = SaatColors.HomeDarkGreen, label = strings.legendPerfect)
                LegendDot(color = CellPartial, label = strings.legendPartial, border = Color(0xFFBFD9CA))
                LegendDot(color = CellIdle, label = strings.legendNone, border = CardLine)
            }
        }
    }
}

@Composable
private fun MonthNavButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
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

@Composable
private fun LegendDot(color: Color, label: String, border: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .then(if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(3.dp)) else Modifier)
        )
        Spacer(Modifier.width(5.dp))
        Text(text = label, fontSize = 10.5.sp, color = TextMuted, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    progress: PrayerDayProgress,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val performTapHaptic = rememberTapHaptic()
    val complete = progress.isPerfectDay
    val partial = !complete && progress.completedCount > 0

    val cellBgColor = when {
        complete -> SaatColors.HomeDarkGreen
        partial -> CellPartial
        isFuture -> Color.Transparent
        else -> CellIdle
    }
    val cellTextColor = when {
        complete -> Color.White
        partial -> SaatColors.HomeDarkGreen
        isFuture -> Color(0xFFC4BDAF)
        else -> Color(0xFF9A948A)
    }
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(cellBgColor)
            .then(
                when {
                    isSelected -> Modifier.border(2.dp, SaatColors.ArcGold, shape)
                    isToday -> Modifier.border(1.5.dp, SaatColors.HomeDarkGreen, shape)
                    else -> Modifier
                }
            )
            .clickable {
                performTapHaptic()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNumber.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected || complete) FontWeight.Bold else FontWeight.Medium,
                color = cellTextColor
            )
            when {
                complete -> Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = SaatColors.ArcGold,
                    modifier = Modifier.size(11.dp)
                )
                partial -> {
                    Spacer(Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(progress.totalCount) { index ->
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < progress.completedCount) SaatColors.HomeDarkGreen
                                        else Color(0xFFBFD9CA)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Selected day detail ────────────────────────────────────────────────────

@Composable
private fun SelectedDayCard(
    day: PrayerDayProgress,
    isToday: Boolean,
    strings: TrackerCalendarStrings,
    onOpenQuran: () -> Unit,
    onOpenDhikr: () -> Unit,
    onOpenSunnahPrayer: () -> Unit
) {
    val dateLabel = remember(day.dayKey) { formatDayKey(day.dayKey) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardCream,
        border = BorderStroke(1.dp, CardLine)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isToday) strings.today else dateLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.HomeDarkGreen
                    )
                    if (isToday) {
                        Text(text = dateLabel, fontSize = 12.sp, color = TextMuted)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (day.isPerfectDay) SaatColors.HomeDarkGreen else SaatColors.ArcInactive
                ) {
                    Text(
                        text = "${day.completedCount}/${day.totalCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (day.isPerfectDay) SaatColors.ArcGold else SaatColors.HomeDarkGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            HabitRow(
                iconRes = R.drawable.ic_prayer_rug,
                label = stringResource(R.string.journey_badge_prayer),
                done = day.isPrayerDone,
                strings = strings,
                onOpen = null
            )
            HabitRow(
                iconRes = R.drawable.ic_quran_on,
                label = stringResource(R.string.journey_badge_quran),
                done = day.isQuranDone,
                strings = strings,
                onOpen = onOpenQuran
            )
            HabitRow(
                iconRes = R.drawable.ic_dhikr,
                label = stringResource(R.string.journey_badge_dhikr),
                done = day.isDhikrDone,
                strings = strings,
                onOpen = onOpenDhikr
            )
            HabitRow(
                iconRes = R.drawable.ic_live_mosque,
                label = stringResource(R.string.journey_badge_sunnah),
                done = day.isSunnahDone,
                strings = strings,
                onOpen = onOpenSunnahPrayer,
                showDivider = false
            )
        }
    }
}

@Composable
private fun HabitRow(
    iconRes: Int,
    label: String,
    done: Boolean,
    strings: TrackerCalendarStrings,
    onOpen: (() -> Unit)?,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (done) SaatColors.HomeDarkGreen else SaatColors.ArcInactive),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = if (done) SaatColors.ArcGold else SaatColors.HomeDarkGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2A24)
            )
            Text(
                text = if (done) strings.done else strings.notYet,
                fontSize = 11.5.sp,
                color = if (done) SaatColors.HomeDarkGreen else TextMuted
            )
        }
        if (done) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE4F2EA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = SaatColors.HomeDarkGreen,
                    modifier = Modifier.size(15.dp)
                )
            }
        } else if (onOpen != null) {
            Surface(
                shape = CircleShape,
                color = SaatColors.PureWhite,
                border = BorderStroke(1.dp, CardLine),
                modifier = Modifier.clickable(onClick = onOpen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.open,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.HomeDarkGreen
                    )
                    Spacer(Modifier.width(3.dp))
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = SaatColors.HomeDarkGreen,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
    if (showDivider) HorizontalDivider(color = CardLine)
}

// ─── Helpers ────────────────────────────────────────────────────────────────

private fun formatDayKey(dayKey: String): String = runCatching {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey)
    SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(parsed!!)
}.getOrDefault(dayKey)

@Composable
private fun weekdayHeaders(): List<String> {
    val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
    val weekdays = symbols.shortWeekdays
    val first = Calendar.getInstance().firstDayOfWeek
    return (0 until 7).map { offset ->
        weekdays[(first + offset - 1) % 7 + 1]
    }
}

private data class TrackerCalendarStrings(
    val title: String,
    val subtitle: String,
    val currentStreak: String,
    val bestStreak: String,
    val perfectDays: String,
    val completion: String,
    val days: String,
    val legendPerfect: String,
    val legendPartial: String,
    val legendNone: String,
    val today: String,
    val done: String,
    val notYet: String,
    val open: String,
    val activeDaysFormat: (Int) -> String
) {
    companion object {
        fun forLanguage(lang: AppLanguage): TrackerCalendarStrings = when (lang) {
            AppLanguage.ENGLISH -> TrackerCalendarStrings(
                title = "Worship Journal & Calendar",
                subtitle = "Track your daily worship streak & progress",
                currentStreak = "Current streak",
                bestStreak = "Best streak",
                perfectDays = "Perfect days",
                completion = "Completion",
                days = "days",
                legendPerfect = "Complete",
                legendPartial = "Partial",
                legendNone = "Empty",
                today = "Today",
                done = "Completed",
                notYet = "Not yet",
                open = "Open",
                activeDaysFormat = { n -> "$n active ${if (n == 1) "day" else "days"} this month" }
            )
            AppLanguage.MALAY -> TrackerCalendarStrings(
                title = "Jurnal & Kalendar Ibadah",
                subtitle = "Rekod streak & pencapaian ibadah harian",
                currentStreak = "Streak semasa",
                bestStreak = "Rekod terbaik",
                perfectDays = "Hari sempurna",
                completion = "Pencapaian",
                days = "hari",
                legendPerfect = "Lengkap",
                legendPartial = "Sebahagian",
                legendNone = "Kosong",
                today = "Hari ini",
                done = "Selesai",
                notYet = "Belum",
                open = "Buka",
                activeDaysFormat = { n -> "$n hari aktif bulan ini" }
            )
            AppLanguage.INDONESIAN -> TrackerCalendarStrings(
                title = "Jurnal & Kalender Ibadah",
                subtitle = "Catatan streak & pencapaian ibadah harian",
                currentStreak = "Streak saat ini",
                bestStreak = "Rekor terbaik",
                perfectDays = "Hari sempurna",
                completion = "Pencapaian",
                days = "hari",
                legendPerfect = "Lengkap",
                legendPartial = "Sebagian",
                legendNone = "Kosong",
                today = "Hari ini",
                done = "Selesai",
                notYet = "Belum",
                open = "Buka",
                activeDaysFormat = { n -> "$n hari aktif bulan ini" }
            )
        }
    }
}
