package app.kamy.saatApp.features.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.OptionalWorshipHabit
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.features.today.OptionalHabitUiItem
import app.kamy.saatApp.features.today.PrayerTrackerUiState
import app.kamy.saatApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.ui.feedback.rememberTapHaptic

@Composable
fun PrayerTrackerCard(
    state: PrayerTrackerUiState,
    onTogglePrayer: (PrayerType) -> Unit,
    onToggleOptional: (OptionalWorshipHabit) -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = remember(context) { AppLanguageStore.from(context).current() }
    var isExpanded by remember { mutableStateOf(true) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrowRotation"
    )

    val completedFardhuCount = state.completedPrayers.size
    val totalFardhuCount = PrayerTrackerStore.TRACKED_PRAYERS.size
    val percentInt = if (totalFardhuCount > 0) ((completedFardhuCount.toFloat() / totalFardhuCount.toFloat()) * 100).toInt() else 0

    // 3-Language Localization
    val titleText = when (currentLang) {
        AppLanguage.MALAY -> "Jurnal Solat Harian"
        AppLanguage.ENGLISH -> "Daily Prayer Journal"
        else -> "Jurnal Shalat Harian"
    }

    val subtitleText = when (currentLang) {
        AppLanguage.MALAY -> "$completedFardhuCount/$totalFardhuCount Solat Fardu ($percentInt%)"
        AppLanguage.ENGLISH -> "$completedFardhuCount/$totalFardhuCount Obligatory ($percentInt%)"
        else -> "$completedFardhuCount/$totalFardhuCount Shalat Fardhu ($percentInt%)"
    }

    val streakUnitText = when (currentLang) {
        AppLanguage.ENGLISH -> "Days"
        else -> "Hari"
    }

    val fardhuSectionTitle = when (currentLang) {
        AppLanguage.MALAY -> "Solat Fardu"
        AppLanguage.ENGLISH -> "Obligatory Prayers"
        else -> "Shalat Fardhu"
    }

    val fardhuSubTitle = when (currentLang) {
        AppLanguage.MALAY -> "5 Waktu"
        AppLanguage.ENGLISH -> "5 Prayers"
        else -> "5 Waktu"
    }

    val sunnahSectionTitle = when (currentLang) {
        AppLanguage.MALAY -> "Amalan Sunah Hari Ini"
        AppLanguage.ENGLISH -> "Today's Sunnah Acts"
        else -> "Amalan Sunnah Hari Ini"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header Row (Compact & Sleek)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SaatColors.DeepEmerald.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_prayer_rug),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald,
                            fontSize = 13.sp
                        )
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate500,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Streak Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SaatColors.AmberWash,
                        border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_streak_custom),
                                contentDescription = null,
                                tint = SaatColors.GoldDeep,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "${state.streak} $streakUnitText",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.GoldDeep,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Calendar Button
                    IconButton(onClick = onOpenCalendar, modifier = Modifier.size(30.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.month_icon),
                            contentDescription = stringResource(R.string.prayer_tracker_open_calendar),
                            tint = SaatColors.Teal,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Expand/Collapse Arrow
                    IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = SaatColors.Slate500,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Linear Progress Indicator
            LinearProgressIndicator(
                progress = { state.todayProgress.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SaatColors.Teal,
                trackColor = SaatColors.LightGrey,
                strokeCap = StrokeCap.Round
            )

            // Content Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(10.dp))

                    // 1. Shalat Fardhu Header & Compact Horizontal Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fardhuSectionTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        Text(
                            text = fardhuSubTitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = SaatColors.Slate500
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // Ultra-compact 5-prayer horizontal row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        PrayerTrackerStore.TRACKED_PRAYERS.forEach { prayer ->
                            val done = state.completedPrayers.contains(prayer)
                            val enabled = done || prayer in state.availablePrayers
                            CompactPrayerTile(
                                prayer = prayer,
                                label = AppNotificationCopy.prayerDisplayName(context, prayer.aladhanKey),
                                completed = done,
                                enabled = enabled,
                                onClick = { onTogglePrayer(prayer) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 2. Amalan Sunnah Section (Horizontal Scrollable LazyRow)
                    if (state.optionalHabits.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))

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
                                    text = sunnahSectionTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.DeepEmerald
                                )
                            }
                            val completedCount = state.optionalHabits.count { it.completed }
                            Surface(
                                shape = CircleShape,
                                color = SaatColors.DeepEmerald.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "$completedCount/${state.optionalHabits.size}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.DeepEmerald
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Horizontal Scroll LazyRow (Does not wrap vertically!)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(state.optionalHabits, key = { it.habit.name }) { item ->
                                HorizontalOptionalHabitChip(
                                    item = item,
                                    onClick = { onToggleOptional(item.habit) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactPrayerTile(
    prayer: PrayerType,
    label: String,
    completed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val performTapHaptic = rememberTapHaptic()

    val circleBg by animateColorAsState(
        targetValue = when {
            completed -> SaatColors.DeepEmerald
            enabled -> SaatColors.MintWash
            else -> SaatColors.LightGrey.copy(alpha = 0.4f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "compactCircleBg"
    )

    val circleBorderColor by animateColorAsState(
        targetValue = when {
            completed -> SaatColors.DeepEmerald
            enabled -> SaatColors.DeepEmerald.copy(alpha = 0.35f)
            else -> SaatColors.SoftGrey.copy(alpha = 0.4f)
        },
        label = "compactCircleBorder"
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            completed -> SaatColors.DeepEmerald
            enabled -> SaatColors.Slate800
            else -> SaatColors.Slate500.copy(alpha = 0.5f)
        },
        label = "compactLabelColor"
    )

    Column(
        modifier = modifier
            .alpha(if (enabled || completed) 1f else 0.55f)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (enabled || completed) {
                    Modifier.clickable {
                        performTapHaptic()
                        onClick()
                    }
                } else Modifier
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Circle Checkbox (Top)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(circleBg)
                .border(1.5.dp, circleBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = SaatColors.PureWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // 2. Prayer Name (Bottom)
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (completed) FontWeight.Bold else FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HorizontalOptionalHabitChip(
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
        label = "horizHabitBg"
    )
    val border by animateColorAsState(
        targetValue = if (item.completed) SaatColors.DeepEmerald else SaatColors.SoftGrey,
        label = "horizHabitBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (item.completed) SaatColors.DeepEmerald else SaatColors.Slate800,
        label = "horizHabitText"
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
