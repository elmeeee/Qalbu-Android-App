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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.OptionalWorshipHabit
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.PrayerDayProgress
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

@Composable
fun PrayerTrackerCalendarScreen(
    onBack: () -> Unit,
    vm: PrayerTrackerCalendarViewModel = hiltViewModel(),
    trackerVm: PrayerTrackerViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val trackerState by trackerVm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentLang = remember(context) { AppLanguageStore.from(context).current() }

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

    // 3-Language Localization
    val titleText = when (currentLang) {
        AppLanguage.MALAY -> "Jurnal & Kalendar Solat"
        AppLanguage.ENGLISH -> "Daily Prayer Journal & Calendar"
        else -> "Jurnal & Kalender Shalat"
    }

    val subtitleText = when (currentLang) {
        AppLanguage.MALAY -> "Pantau konsistensi solat fardu & amalan sunah harian"
        AppLanguage.ENGLISH -> "Track daily obligatory prayers & sunnah habits"
        else -> "Pantau konsistensi shalat fardhu & amalan sunnah harian"
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

    val journalSectionTitle = when (currentLang) {
        AppLanguage.MALAY -> "Jurnal Solat & Amalan Hari Ini"
        AppLanguage.ENGLISH -> "Today's Prayer & Sunnah Journal"
        else -> "Jurnal Shalat & Amalan Sunnah Hari Ini"
    }

    val fardhuTitle = when (currentLang) {
        AppLanguage.MALAY -> "Solat Fardu 5 Waktu"
        AppLanguage.ENGLISH -> "5 Obligatory Prayers"
        else -> "Shalat Fardhu 5 Waktu"
    }

    val sunnahTitle = when (currentLang) {
        AppLanguage.MALAY -> "Amalan Sunah Hari Ini"
        AppLanguage.ENGLISH -> "Today's Sunnah Acts"
        else -> "Amalan Sunnah Hari Ini"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SaatColors.ScreenBackground,
                        SaatColors.SageMist.copy(alpha = 0.4f),
                        SaatColors.ScreenBackground
                    )
                )
            )
    ) {
        // Sticky Header Bar
        Row(
            Modifier
                .fillMaxWidth()
                .background(SaatColors.ScreenBackground.copy(alpha = 0.95f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SaatColors.DeepEmerald
                )
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✦ ",
                        fontSize = 14.sp,
                        color = SaatColors.GoldDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = titleText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
                Text(
                    text = subtitleText,
                    fontSize = 11.sp,
                    color = SaatColors.Slate500
                )
            }
        }

        HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Surface Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SaatColors.PureWhite,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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

            // Month Navigation Bar
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = vm::previousMonth) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month", tint = SaatColors.DeepEmerald)
                    }
                    Text(
                        text = monthLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    IconButton(onClick = vm::nextMonth) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month", tint = SaatColors.DeepEmerald)
                    }
                }
            }

            // Weekday Headers
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
                            fontSize = 11.sp,
                            color = SaatColors.Slate500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Calendar Grid Container (Fixed height grid inside item)
            item {
                val totalCells = leadingBlanks + state.days.size
                val rows = (totalCells + 6) / 7
                val gridHeight = (rows * 48).dp

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
                            isToday = day.dayKey == todayKey
                        )
                    }
                }
            }

            // Today's Journal & Sunnah Acts Breakdown Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SaatColors.PureWhite,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_prayer_rug),
                                contentDescription = null,
                                tint = SaatColors.DeepEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = journalSectionTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }

                        HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

                        // 1. Shalat Fardhu Status
                        Text(
                            text = fardhuTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PrayerTrackerStore.TRACKED_PRAYERS.forEach { prayer ->
                                val done = trackerState.completedPrayers.contains(prayer)
                                val enabled = done || prayer in trackerState.availablePrayers
                                CompactDetailPrayerTile(
                                    prayer = prayer,
                                    label = AppNotificationCopy.prayerDisplayName(context, prayer.aladhanKey),
                                    completed = done,
                                    enabled = enabled,
                                    onClick = { trackerVm.togglePrayer(prayer) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 2. Amalan Sunnah Status (Horizontal Scroll)
                        if (trackerState.optionalHabits.isNotEmpty()) {
                            HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "✦ ",
                                        fontSize = 11.sp,
                                        color = SaatColors.GoldDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sunnahTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.DeepEmerald
                                    )
                                }
                                val completedCount = trackerState.optionalHabits.count { it.completed }
                                Surface(
                                    shape = CircleShape,
                                    color = SaatColors.DeepEmerald.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = "$completedCount/${trackerState.optionalHabits.size}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.DeepEmerald
                                    )
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(trackerState.optionalHabits, key = { it.habit.name }) { item ->
                                    DetailOptionalHabitChip(
                                        item = item,
                                        onClick = { trackerVm.toggleOptionalHabit(item.habit) }
                                    )
                                }
                            }
                        }
                    }
                }
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
                tint = SaatColors.GoldDeep,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SaatColors.DeepEmerald
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = SaatColors.Slate500,
            fontWeight = FontWeight.Medium
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
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    complete -> Brush.linearGradient(
                        listOf(SaatColors.DeepEmerald, SaatColors.Teal)
                    )
                    partial -> Brush.linearGradient(
                        listOf(
                            SaatColors.Teal.copy(alpha = 0.25f),
                            SaatColors.Teal.copy(alpha = 0.12f)
                        )
                    )
                    else -> Brush.linearGradient(
                        listOf(SaatColors.LightGrey, SaatColors.LightGrey)
                    )
                }
            )
            .then(
                if (isToday) {
                    Modifier.border(2.dp, SaatColors.GoldDeep, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNumber.toString(),
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    complete -> Color.White
                    partial -> SaatColors.DeepEmerald
                    else -> SaatColors.Slate500
                }
            )
            if (complete) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            } else if (partial) {
                Text(
                    text = "${progress.completedCount}/${progress.totalCount}",
                    fontSize = 9.sp,
                    color = SaatColors.DeepEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompactDetailPrayerTile(
    prayer: PrayerType,
    label: String,
    completed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val performTapHaptic = rememberTapHaptic()

    val iconRes = when (prayer) {
        PrayerType.FAJR -> R.drawable.ic_prayer_fajr
        PrayerType.DHUHR -> R.drawable.ic_prayer_dhuhr
        PrayerType.ASR -> R.drawable.ic_prayer_asr
        PrayerType.MAGHRIB -> R.drawable.ic_prayer_maghrib
        PrayerType.ISHA -> R.drawable.ic_prayer_isha
        else -> R.drawable.ic_prayer_rug
    }

    // Clean MintWash tile background (Not solid green!)
    val tileBg by animateColorAsState(
        targetValue = when {
            enabled || completed -> SaatColors.MintWash
            else -> SaatColors.LightGrey.copy(alpha = 0.4f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "detailTileBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            completed -> SaatColors.DeepEmerald
            enabled -> SaatColors.DeepEmerald.copy(alpha = 0.25f)
            else -> SaatColors.SoftGrey.copy(alpha = 0.4f)
        },
        label = "detailTileBorder"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            enabled || completed -> SaatColors.DeepEmerald
            else -> SaatColors.Slate500.copy(alpha = 0.5f)
        },
        label = "detailContentColor"
    )

    Surface(
        modifier = modifier
            .alpha(if (enabled || completed) 1f else 0.55f)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (enabled || completed) {
                    Modifier.clickable {
                        performTapHaptic()
                        onClick()
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = tileBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (completed) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(3.dp))

            // Checkmark vs Uncheck indicator
            if (completed) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_custom),
                    contentDescription = "Done",
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .border(1.dp, SaatColors.Slate500.copy(alpha = 0.4f), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun DetailOptionalHabitChip(
    item: OptionalHabitUiItem,
    onClick: () -> Unit
) {
    val performTapHaptic = rememberTapHaptic()

    val habitIconRes = when (item.habit) {
        OptionalWorshipHabit.QIYAMUL_LAIL -> R.drawable.ic_qiyam
        OptionalWorshipHabit.MONDAY_THURSDAY_FAST,
        OptionalWorshipHabit.AYYAMUL_BIDH_SAHUR -> R.drawable.ic_rice
        OptionalWorshipHabit.DHIKR_MORNING,
        OptionalWorshipHabit.DHIKR_EVENING -> R.drawable.ic_dhikr
        OptionalWorshipHabit.READ_QURAN -> R.drawable.ic_quran_off
        OptionalWorshipHabit.DAILY_CHARITY -> R.drawable.ic_cash_saving
        OptionalWorshipHabit.DHUHA,
        OptionalWorshipHabit.RAWATIB -> R.drawable.ic_prayer_rug
    }

    val bg by animateColorAsState(
        targetValue = if (item.completed) SaatColors.DeepEmerald.copy(alpha = 0.12f) else SaatColors.LightGrey,
        label = "detailHabitBg"
    )
    val border by animateColorAsState(
        targetValue = if (item.completed) SaatColors.DeepEmerald else SaatColors.SoftGrey,
        label = "detailHabitBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (item.completed) SaatColors.DeepEmerald else SaatColors.Slate800,
        label = "detailHabitText"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                performTapHaptic()
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(habitIconRes),
                contentDescription = null,
                tint = if (item.completed) SaatColors.DeepEmerald else SaatColors.Slate500,
                modifier = Modifier.size(13.dp)
            )

            Spacer(Modifier.width(5.dp))

            Text(
                text = stringResource(item.labelRes),
                fontSize = 10.sp,
                color = textColor,
                fontWeight = if (item.completed) FontWeight.Bold else FontWeight.Normal
            )

            if (item.completed) {
                Spacer(Modifier.width(5.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_check_custom),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(11.dp)
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
