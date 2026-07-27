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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.OptionalWorshipHabit
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.features.today.OptionalHabitUiItem
import app.kamy.saatApp.features.today.PrayerTrackerUiState
import app.kamy.saatApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.ui.feedback.rememberTapHaptic

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayerTrackerCard(
    state: PrayerTrackerUiState,
    onTogglePrayer: (PrayerType) -> Unit,
    onToggleOptional: (OptionalWorshipHabit) -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrowRotation"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.5f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.prayer_tracker_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Text(
                        text = stringResource(
                            R.string.prayer_tracker_subtitle,
                            state.todayProgress.completedCount,
                            state.todayProgress.totalCount
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = SaatColors.Slate500
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SaatColors.AmberWash)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = state.streak.toString(),
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
                IconButton(onClick = onOpenCalendar, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.month_icon),
                        contentDescription = stringResource(R.string.prayer_tracker_open_calendar),
                        tint = SaatColors.Teal,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = SaatColors.Slate500,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(arrowRotation)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Progress bar stays ALWAYS VISIBLE
            LinearProgressIndicator(
                progress = { state.todayProgress.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SaatColors.Teal,
                trackColor = SaatColors.LightGrey,
                strokeCap = StrokeCap.Round
            )

            // Chips section expands/collapses smoothly
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PrayerTrackerStore.TRACKED_PRAYERS.forEach { prayer ->
                            val done = state.completedPrayers.contains(prayer)
                            val enabled = done || prayer in state.availablePrayers
                            PrayerCheckChip(
                                label = AppNotificationCopy.prayerDisplayName(context, prayer.aladhanKey),
                                completed = done,
                                enabled = enabled,
                                onClick = { onTogglePrayer(prayer) }
                            )
                        }
                    }

                    if (state.optionalHabits.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.optionalHabits.forEach { item ->
                                OptionalHabitChip(
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
private fun OptionalHabitChip(
    item: OptionalHabitUiItem,
    onClick: () -> Unit
) {
    val bg = if (item.completed) SaatColors.DeepEmerald.copy(alpha = 0.12f) else SaatColors.LightGrey
    val border = if (item.completed) SaatColors.DeepEmerald else SaatColors.SoftGrey
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = null,
            tint = if (item.completed) SaatColors.DeepEmerald else Color.Transparent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = if (item.completed) SaatColors.DeepEmerald else SaatColors.Slate800,
            fontWeight = if (item.completed) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun PrayerCheckChip(
    label: String,
    completed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val performTapHaptic = rememberTapHaptic()
    val bg by animateColorAsState(
        targetValue = when {
            completed -> SaatColors.DeepEmerald
            enabled -> Color.Transparent
            else -> SaatColors.LightGrey.copy(alpha = 0.35f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            completed -> SaatColors.DeepEmerald
            enabled -> SaatColors.SoftGrey
            else -> SaatColors.SoftGrey.copy(alpha = 0.45f)
        },
        label = "chipBorder"
    )
    val labelColor by animateColorAsState(
        targetValue = when {
            completed -> SaatColors.DeepEmerald
            enabled -> SaatColors.Slate500
            else -> SaatColors.Slate500.copy(alpha = 0.45f)
        },
        label = "chipLabel"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (enabled || completed) 1f else 0.55f)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (enabled || completed) {
                    Modifier.clickable {
                        performTapHaptic()
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .then(
                    if (completed) {
                        Modifier.background(
                            Brush.linearGradient(
                                listOf(SaatColors.DeepEmerald, SaatColors.Teal)
                            )
                        )
                    } else {
                        Modifier.background(bg)
                    }
                )
                .then(
                    if (!completed) {
                        Modifier.border(2.dp, borderColor, CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (completed) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor,
            maxLines = 1
        )
    }
}
