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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val trackerState by trackerVm.state.collectAsStateWithLifecycle()
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
        AppLanguage.ENGLISH -> "Daily Prayer Journal & Calendar"
        else -> "Jurnal & Kalender Shalat"
    }

    val subtitleText = when (currentLang) {
        AppLanguage.MALAY -> "Pantau rekod solat fardu & hub amalan sunah"
        AppLanguage.ENGLISH -> "View prayer logs & sunnah practice hub"
        else -> "Lihat catatan shalat & hub amalan sunnah"
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

    val practiceHubTitle = when (currentLang) {
        AppLanguage.MALAY -> "Panduan & Amalan Sunah"
        AppLanguage.ENGLISH -> "Sunnah Practice Hub & Guides"
        else -> "Panduan & Amalan Sunnah"
    }

    val selectedDayProgress = remember(selectedDayKey, state.days) {
        state.days.find { it.dayKey == selectedDayKey } ?: PrayerDayProgress(
            dayKey = selectedDayKey,
            completedCount = PrayerTrackerStore.completedCount(context, selectedDayKey),
            totalCount = 5
        )
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
            .tabContentStatusBarInset()
    ) {
        // Sticky Clean Header Bar (Clean padding below status bar)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Stats Surface Card
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

            // 2. Month Navigation Bar
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
                            fontSize = 11.sp,
                            color = SaatColors.Slate500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Monthly Calendar Grid
            item {
                val totalCells = leadingBlanks + state.days.size
                val rows = (totalCells + 6) / 7
                val gridHeight = (rows * 50).dp

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

            // 5. Selected Day Read-Only Log Card
            item {
                SelectedDaySummaryCard(
                    dayProgress = selectedDayProgress,
                    isToday = selectedDayKey == todayKey,
                    language = currentLang
                )
            }

            // 6. Sunnah Practice Hub & Dedicated Feature Navigators (Not duplicated checkboxes)
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "✦ ",
                                fontSize = 12.sp,
                                color = SaatColors.GoldDeep,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = practiceHubTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }

                        HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

                        SunnahPracticeNavigationRow(
                            iconRes = R.drawable.ic_prayer_rug,
                            title = when (currentLang) {
                                AppLanguage.MALAY -> "Solat Sunah (Dhuha, Rawatib, Tahajud)"
                                AppLanguage.ENGLISH -> "Sunnah Prayers (Dhuha, Rawatib, Qiyam)"
                                else -> "Shalat Sunnah (Dhuha, Rawatib, Tahajud)"
                            },
                            subtitle = when (currentLang) {
                                AppLanguage.MALAY -> "Buka panduan niat & tatacara solat sunah"
                                AppLanguage.ENGLISH -> "Open intention & step-by-step guides"
                                else -> "Buka panduan niat & tata cara shalat sunnah"
                            },
                            onClick = onOpenSunnahPrayer
                        )

                        SunnahPracticeNavigationRow(
                            iconRes = R.drawable.ic_quran_off,
                            title = when (currentLang) {
                                AppLanguage.MALAY -> "Membaca Al-Quran Harian"
                                AppLanguage.ENGLISH -> "Daily Quran Recitation"
                                else -> "Membaca Al-Qur'an Harian"
                            },
                            subtitle = when (currentLang) {
                                AppLanguage.MALAY -> "Buka senarai surah & pembacaan Quran"
                                AppLanguage.ENGLISH -> "Open Quran reader & surah list"
                                else -> "Buka daftar surah & pembacaan Qur'an"
                            },
                            onClick = onOpenQuran
                        )

                        SunnahPracticeNavigationRow(
                            iconRes = R.drawable.ic_dhikr,
                            title = when (currentLang) {
                                AppLanguage.MALAY -> "Zikir Pagi & Petang"
                                AppLanguage.ENGLISH -> "Morning & Evening Dhikr"
                                else -> "Dzikir Pagi & Petang"
                            },
                            subtitle = when (currentLang) {
                                AppLanguage.MALAY -> "Tasbih digital & himpunan zikir"
                                AppLanguage.ENGLISH -> "Digital tasbih & dhikr collection"
                                else -> "Tasbih digital & kumpulan dzikir"
                            },
                            onClick = onOpenDhikr
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
            .clickable {
                performTapHaptic()
                onClick()
            }
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, SaatColors.GoldDeep, RoundedCornerShape(12.dp))
                } else if (isToday) {
                    Modifier.border(1.5.dp, SaatColors.DeepEmerald, RoundedCornerShape(12.dp))
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
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
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
                    text = "${progress.completedCount}/5",
                    fontSize = 9.sp,
                    color = SaatColors.DeepEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SelectedDaySummaryCard(
    dayProgress: PrayerDayProgress,
    isToday: Boolean,
    language: AppLanguage
) {
    val context = LocalContext.current
    val headerTitle = if (isToday) {
        when (language) {
            AppLanguage.MALAY -> "Ringkasan Solat Hari Ini"
            AppLanguage.ENGLISH -> "Today's Prayer Summary"
            else -> "Ringkasan Shalat Hari Ini"
        }
    } else {
        when (language) {
            AppLanguage.MALAY -> "Ringkasan Solat Tarikh Terpilih"
            AppLanguage.ENGLISH -> "Selected Date Summary"
            else -> "Ringkasan Shalat Tanggal Terpilih"
        }
    }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_prayer_rug),
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = headerTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (dayProgress.isPerfectDay) SaatColors.GoldDeep.copy(alpha = 0.15f) else SaatColors.DeepEmerald.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "${dayProgress.completedCount}/${dayProgress.totalCount} ${stringResource(R.string.done)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dayProgress.isPerfectDay) SaatColors.GoldDeep else SaatColors.DeepEmerald
                    )
                }
            }

            HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PrayerTrackerStore.TRACKED_PRAYERS.forEach { prayer ->
                    val done = PrayerTrackerStore.isCompleted(context, prayer, dayProgress.dayKey)
                    ReadOnlyPrayerBadge(
                        prayerName = AppNotificationCopy.prayerDisplayName(context, prayer.aladhanKey),
                        completed = done,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyPrayerBadge(
    prayerName: String,
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (completed) SaatColors.MintWash else SaatColors.LightGrey.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (completed) SaatColors.DeepEmerald else SaatColors.SoftGrey.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = prayerName,
                fontSize = 10.sp,
                fontWeight = if (completed) FontWeight.Bold else FontWeight.Medium,
                color = if (completed) SaatColors.DeepEmerald else SaatColors.Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

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
                        .background(SaatColors.SoftGrey)
                )
            }
        }
    }
}

@Composable
private fun SunnahPracticeNavigationRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val performTapHaptic = rememberTapHaptic()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                performTapHaptic()
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        color = SaatColors.MintWash,
        border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = SaatColors.Slate500
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = SaatColors.DeepEmerald,
                modifier = Modifier.size(20.dp)
            )
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
private fun weekdayHeaders(): List<String> {
    val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
    val weekdays = symbols.shortWeekdays
    val first = Calendar.getInstance().firstDayOfWeek
    return (0 until 7).map { offset ->
        weekdays[(first + offset - 1) % 7 + 1]
    }
}
