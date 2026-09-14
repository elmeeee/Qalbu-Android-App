package app.kamy.saatApp.features.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.features.today.RamadanDayProgressItem
import app.kamy.saatApp.features.today.RamadanFastingStats
import app.kamy.saatApp.infrastructure.preferences.RamadanPreferencesStore
import app.kamy.saatApp.ui.feedback.rememberTapHaptic

private fun Int.toArabicNumerals(): String {
    val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { if (it in '0'..'9') digits[it - '0'] else it }.joinToString("")
}

@Composable
fun RamadanCalendarCard(
    calendarDays: List<RamadanDayProgressItem>,
    stats: RamadanFastingStats,
    currentDayNumber: Int,
    totalDays: Int,
    onToggleDay: (dayNumber: Int) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenFidyahTracker: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val performTapHaptic = rememberTapHaptic()

    val progressFraction = remember(stats.totalFasted, totalDays) {
        if (totalDays > 0) (stats.totalFasted.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f) else 0f
    }
    val percentage = (progressFraction * 100).toInt()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.8f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Icon + Title + Progress Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.calendar_ramadan_icon),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.ramadan_calendar_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = stringResource(R.string.ramadan_calendar_subtitle),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Progress Badge (% Completion)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F8F0),
                    border = BorderStroke(1.dp, Color(0xFFB8E0C4))
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = SaatColors.HomeDarkGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Linear Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction.coerceAtLeast(0.02f))
                        .height(7.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF34D399),
                                    Color(0xFF059669)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Stats Summary Pills (Centered, Proportional, Streak with App Icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FastingStatPill(
                    label = stringResource(R.string.ramadan_stat_fasted),
                    value = "${stats.totalFasted}",
                    color = Color(0xFF059669),
                    bgColor = Color(0xFFE8F8F0),
                    modifier = Modifier.weight(1f)
                )
                FastingStatPill(
                    label = stringResource(R.string.ramadan_stat_missed),
                    value = "${stats.totalMissed}",
                    color = if (stats.totalMissed > 0) Color(0xFFDC2626) else Color(0xFF64748B),
                    bgColor = if (stats.totalMissed > 0) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f)
                )
                FastingStatPill(
                    label = stringResource(R.string.ramadan_stat_remaining),
                    value = "${stats.totalRemaining}",
                    color = Color(0xFF334155),
                    bgColor = Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f)
                )
                FastingStatPill(
                    label = stringResource(R.string.ramadan_stat_streak),
                    value = "${stats.currentStreak}",
                    color = Color(0xFFD97706),
                    bgColor = Color(0xFFFEF3C7),
                    iconRes = R.drawable.ic_streak_custom,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 30-Day Grid (6 Columns x 5 Rows)
            val columns = 6
            val totalItems = calendarDays.size.coerceAtLeast(totalDays)
            val rows = (totalItems + columns - 1) / columns

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (colIndex in 0 until columns) {
                            val itemIndex = rowIndex * columns + colIndex
                            if (itemIndex < totalItems) {
                                val dayItem = calendarDays.getOrNull(itemIndex) ?: RamadanDayProgressItem(
                                    dayNumber = itemIndex + 1,
                                    state = RamadanPreferencesStore.FastingDayState.UNRECORDED,
                                    isToday = (itemIndex + 1) == currentDayNumber,
                                    isPassed = (itemIndex + 1) < currentDayNumber,
                                    isFuture = (itemIndex + 1) > currentDayNumber
                                )

                                FastingDayGridCell(
                                    item = dayItem,
                                    onClick = {
                                        performTapHaptic()
                                        if (dayItem.isFuture) {
                                            onShowMessage(
                                                context.getString(
                                                    R.string.ramadan_day_future_hint,
                                                    dayItem.dayNumber
                                                )
                                            )
                                        } else {
                                            onToggleDay(dayItem.dayNumber)
                                            val stateName = if (dayItem.state == RamadanPreferencesStore.FastingDayState.FASTED) {
                                                context.getString(R.string.ramadan_mark_missed)
                                            } else {
                                                context.getString(R.string.ramadan_mark_fasted)
                                            }
                                            onShowMessage(
                                                context.getString(
                                                    R.string.ramadan_toast_status_updated,
                                                    dayItem.dayNumber,
                                                    stateName
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Qadha Reminder Banner & Fidyah Tracker CTA if user has missed days
            if (stats.totalMissed > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = onOpenFidyahTracker != null) {
                            performTapHaptic()
                            onOpenFidyahTracker?.invoke()
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.ramadan_qadha_info, stats.totalMissed),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                ),
                                color = Color(0xFF991B1B),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (onOpenFidyahTracker != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = stringResource(R.string.ramadan_open_fidyah_tracker) + " ➔",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
private fun FastingStatPill(
    label: String,
    value: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    iconRes: Int? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 2.dp)
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = color.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FastingDayGridCell(
    item: RamadanDayProgressItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "DayScale"
    )

    // Glowing pulse for today
    val infiniteTransition = rememberInfiniteTransition(label = "TodayPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val isFasted = item.state == RamadanPreferencesStore.FastingDayState.FASTED
    val isMissed = item.state == RamadanPreferencesStore.FastingDayState.NOT_FASTED

    val targetBgColor = when {
        isFasted -> Color(0xFFE8F8F0)
        isMissed -> Color(0xFFFEE2E2)
        item.isToday -> Color(0xFFF0FDF4)
        else -> Color(0xFFF8FAFC)
    }
    val animatedBgColor by animateColorAsState(targetValue = targetBgColor, label = "CellBg")

    val targetBorderColor = when {
        item.isToday -> SaatColors.ArcGold.copy(alpha = pulseAlpha)
        isFasted -> Color(0xFF10B981)
        isMissed -> Color(0xFFEF4444)
        else -> Color(0xFFE2E8F0)
    }
    val animatedBorderColor by animateColorAsState(targetValue = targetBorderColor, label = "CellBorder")

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBgColor)
            .border(
                width = if (item.isToday) 1.8.dp else 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 3.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Arabic Numeral (top)
            Text(
                text = item.dayNumber.toArabicNumerals(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = when {
                    isFasted -> Color(0xFF047857)
                    isMissed -> Color(0xFFB91C1C)
                    item.isToday -> SaatColors.HomeDarkGreen
                    else -> Color(0xFF94A3B8)
                },
                textAlign = TextAlign.Center
            )

            // Latin Day Number (middle)
            Text(
                text = "${item.dayNumber}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (item.isToday || isFasted || isMissed) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = 9.5.sp
                ),
                color = when {
                    isFasted -> Color(0xFF065F46)
                    isMissed -> Color(0xFF991B1B)
                    item.isToday -> Color(0xFF0F172A)
                    else -> Color(0xFF64748B)
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(1.dp))

            // Status Icon Indicator (bottom)
            if (isFasted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Fasted",
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(10.dp)
                )
            } else if (isMissed) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Missed",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(10.dp)
                )
            } else if (item.isToday) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(SaatColors.ArcGold)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCBD5E1))
                )
            }
        }
    }
}
