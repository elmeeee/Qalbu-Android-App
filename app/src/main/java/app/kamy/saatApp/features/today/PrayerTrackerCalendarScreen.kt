package app.kamy.saatApp.features.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

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

    // 3-Language Localization
    val titleText = when (currentLang) {
        AppLanguage.MALAY -> "Jurnal & Kalendar Solat"
        AppLanguage.ENGLISH -> "Daily Worship Journal & Calendar"
        else -> "Jurnal & Kalender Ibadah"
    }

    val subtitleText = when (currentLang) {
        AppLanguage.MALAY -> "Rekod streak & pencapaian ibadah harian"
        AppLanguage.ENGLISH -> "Track daily worship streak & progress"
        else -> "Catatan streak & pencapaian ibadah harian"
    }

    val currentStreakLabel = when (currentLang) {
        AppLanguage.MALAY -> "Streak Semasa"
        AppLanguage.ENGLISH -> "Current Streak"
        else -> "Streak Saat Ini"
    }

    val bestStreakLabel = when (currentLang) {
        AppLanguage.MALAY -> "Rekod Terbaik"
        AppLanguage.ENGLISH -> "Best Streak"
        else -> "Rekor Terbaik"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(SaatColors.HomeBg)
            .tabContentStatusBarInset()
    ) {
        // Sticky Clean Header Bar
        Row(
            Modifier
                .fillMaxWidth()
                .background(SaatColors.HomeBg)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SaatColors.HomeDarkGreen
                )
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text = titleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.HomeDarkGreen
                )
                Text(
                    text = subtitleText,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE2DCD2).copy(alpha = 0.6f))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Stats Surface Card (Streak Counter)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFFFFDF7),
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, Color(0xFFF3EDE2))
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ChallengeStat(
                            iconRes = R.drawable.ic_streak_custom,
                            value = "${state.streak} ${if (currentLang == AppLanguage.ENGLISH) "Days" else "Hari"}",
                            label = currentStreakLabel
                        )
                        ChallengeStat(
                            iconRes = R.drawable.month_icon,
                            value = "${state.bestStreak} ${if (currentLang == AppLanguage.ENGLISH) "Days" else "Hari"}",
                            label = bestStreakLabel
                        )
                    }
                }
            }

            // 2. Month Navigation Bar
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = vm::previousMonth) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month", tint = SaatColors.HomeDarkGreen)
                    }
                    Text(
                        text = monthLabel,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.HomeDarkGreen
                    )
                    IconButton(onClick = vm::nextMonth) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month", tint = SaatColors.HomeDarkGreen)
                    }
                }
            }

            // 3. Weekday Headers
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdayHeaders().forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Monthly Calendar Grid (Streak Days)
            item {
                val totalCells = leadingBlanks + state.days.size
                val rows = (totalCells + 6) / 7
                val gridHeight = (rows * 52).dp

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    userScrollEnabled = false
                ) {
                    items(count = leadingBlanks) {
                        Spacer(Modifier.aspectRatio(1f))
                    }
                    itemsIndexed(state.days) { index, day ->
                        val dayNumber = index + 1
                        CalendarDayCell(
                            dayNumber = dayNumber,
                            progress = day,
                            isToday = day.dayKey == todayKey,
                            isSelected = day.dayKey == selectedDayKey,
                            onClick = { selectedDayKey = day.dayKey }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    progress: PrayerDayProgress,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val performTapHaptic = rememberTapHaptic()
    val complete = progress.isPerfectDay || progress.completedCount >= progress.totalCount
    val partial = progress.completedCount > 0

    val cellBgColor = when {
        complete -> SaatColors.HomeDarkGreen
        partial -> Color(0xFFE6F4EA)
        else -> Color(0xFFFAF6F0)
    }

    val cellTextColor = when {
        complete -> Color.White
        partial -> SaatColors.HomeDarkGreen
        else -> Color(0xFF94A3B8)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(cellBgColor)
            .clickable {
                performTapHaptic()
                onClick()
            }
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, SaatColors.ArcGold, RoundedCornerShape(14.dp))
                } else if (isToday) {
                    Modifier.border(1.5.dp, SaatColors.HomeDarkGreen, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNumber.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected || complete) FontWeight.Bold else FontWeight.Medium,
                color = cellTextColor
            )
            if (complete) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = SaatColors.ArcGold,
                    modifier = Modifier.size(12.dp)
                )
            } else if (partial) {
                Text(
                    text = "${progress.completedCount}/${progress.totalCount}",
                    fontSize = 9.5.sp,
                    color = SaatColors.HomeDarkGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChallengeStat(
    iconRes: Int,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = SaatColors.HomeDarkGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SaatColors.HomeDarkGreen
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
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
