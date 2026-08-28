package app.kamy.saatApp.features.today.components

import android.text.format.DateFormat
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.SaatErrorStateDark
import app.kamy.saatApp.design.components.SaatSkeletonOnDark
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.features.today.PrayerUiState
import app.kamy.saatApp.infrastructure.repository.PrayerEntry
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import java.text.SimpleDateFormat
import java.util.Locale

@DrawableRes
fun getPrayerCardDrawable(type: PrayerType?): Int {
    return when (type) {
        PrayerType.FAJR, PrayerType.SUNRISE, PrayerType.DHUHR -> R.drawable.day
        PrayerType.ASR, PrayerType.MAGHRIB -> R.drawable.sunset
        PrayerType.ISHA -> R.drawable.night
        null -> {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            when (hour) {
                in 5..14 -> R.drawable.day
                in 15..18 -> R.drawable.sunset
                else -> R.drawable.night
            }
        }
    }
}

@DrawableRes
private fun getPrayerIconRes(type: PrayerType): Int {
    return when (type) {
        PrayerType.FAJR -> R.drawable.ic_prayer_fajr
        PrayerType.SUNRISE -> R.drawable.ic_prayer_terbit
        PrayerType.DHUHR -> R.drawable.ic_prayer_dhuhr
        PrayerType.ASR -> R.drawable.ic_prayer_asr
        PrayerType.MAGHRIB -> R.drawable.ic_prayer_maghrib
        PrayerType.ISHA -> R.drawable.ic_prayer_isha
    }
}

private val DISPLAY_PRAYER_SLOTS = listOf(
    PrayerType.FAJR,
    PrayerType.SUNRISE,
    PrayerType.DHUHR,
    PrayerType.ASR,
    PrayerType.MAGHRIB,
    PrayerType.ISHA
)

private fun android.content.Context.is24HourClock(): Boolean {
    try {
        val sysSetting = android.provider.Settings.System.getString(contentResolver, android.provider.Settings.System.TIME_12_24)
        if (sysSetting == "24") return true
        if (sysSetting == "12") return false
    } catch (_: Exception) {}

    try {
        val globalSetting = android.provider.Settings.Global.getString(contentResolver, "time_12_24")
        if (globalSetting == "24") return true
        if (globalSetting == "12") return false
    } catch (_: Exception) {}

    if (android.text.format.DateFormat.is24HourFormat(this)) return true
    if (android.text.format.DateFormat.is24HourFormat(applicationContext)) return true

    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerDashboardCard(
    state: PrayerUiState,
    onRetry: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val fetchErrorDisplay = state.error
        ?.takeIf { !state.needsPermission }
        ?.rememberErrorDisplay(R.string.error_prayer_fetch_title)

    val slotEntries = remember(state.timings) {
        DISPLAY_PRAYER_SLOTS.map { type ->
            state.timings.find { it.type == type }
        }
    }
    val headline = rememberHeadline(state)
    val targetPrayer = state.nextPrayer ?: state.activePrayer ?: PrayerType.DHUHR

    val singleActiveType = remember(state.activePrayer, state.nextPrayer, state.timings) {
        state.nextPrayer ?: state.activePrayer ?: PrayerType.DHUHR
    }

    val activeIndex = remember(singleActiveType) {
        DISPLAY_PRAYER_SLOTS.indexOf(singleActiveType).coerceAtLeast(0)
    }

    val progressRatio = remember(state.timings, state.nextPrayer, state.activePrayer, state.countdown) {
        calculatePrayerProgress(state)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading && state.timings.isEmpty() -> {
                    PrayerCardLoading()
                }
                fetchErrorDisplay != null && state.timings.isEmpty() -> {
                    SaatErrorStateDark(
                        display = fetchErrorDisplay,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    // Top Row: Next Prayer Name & Circular Arc Countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.prayer_widget_next_label),
                                color = Color(0xFF64748B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = headline.title,
                                color = Color(0xFF0F172A),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Right: Circular Arc Countdown Widget
                        PrayerArcCountdown(
                            countdown = if (state.countdown.isNotBlank() && state.countdown != "--:--:--") state.countdown else "00:00:00",
                            prayerType = targetPrayer,
                            progress = progressRatio
                        )
                    }

                    // Timeline & Prayer Slots Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Horizontal Timeline Progress Track
                        PrayerTimelineProgressTrack(
                            activeIndex = activeIndex,
                            totalSlots = DISPLAY_PRAYER_SLOTS.size,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 6 Prayer Slots
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isLoading && state.timings.isEmpty()) {
                                repeat(6) {
                                    SchedulePrayerSlotSkeleton(modifier = Modifier.weight(1f))
                                }
                            } else {
                                DISPLAY_PRAYER_SLOTS.forEachIndexed { index, type ->
                                    val entry = slotEntries[index]
                                    val isActive = !state.isLoading && state.timings.isNotEmpty() && entry != null && (type == singleActiveType)
                                    SchedulePrayerSlot(
                                        type = type,
                                        entry = entry,
                                        isActive = isActive,
                                        modifier = Modifier.weight(1f)
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

private fun calculatePrayerProgress(state: PrayerUiState): Float {
    val timings = state.timings.ifEmpty { return 0.5f }
    val now = System.currentTimeMillis()
    val nextType = state.nextPrayer ?: return 0.5f
    val nextEntry = timings.find { it.type == nextType } ?: return 0.5f

    val sorted = timings.sortedBy { it.date.time }
    val nextIndex = sorted.indexOfFirst { it.type == nextType }
    val prevEntry = if (nextIndex > 0) sorted[nextIndex - 1] else null

    val nextTime = nextEntry.date.time
    val prevTime = prevEntry?.date?.time ?: (nextTime - 3 * 3600 * 1000L)

    val totalMs = (nextTime - prevTime).coerceAtLeast(1L)
    val elapsedMs = (now - prevTime).coerceAtLeast(0L)

    return (elapsedMs.toFloat() / totalMs.toFloat()).coerceIn(0.08f, 1f)
}

@Composable
private fun PrayerArcCountdown(
    countdown: String,
    prayerType: PrayerType? = null,
    progress: Float = 0.5f,
    modifier: Modifier = Modifier
) {
    val untilPrayerText = stringResource(R.string.prayer_countdown_until_label)
    val iconRes = getPrayerIconRes(prayerType ?: PrayerType.DHUHR)
    val sweepAngle = (180f * progress).coerceIn(12f, 180f)

    val density = androidx.compose.ui.platform.LocalDensity.current
    val iconOffsetX = remember { androidx.compose.runtime.mutableStateOf(0.dp) }
    val iconOffsetY = remember { androidx.compose.runtime.mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .width(135.dp)
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 3.5.dp.toPx()
            val w = size.width
            val diameter = w - strokePx
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
            val topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2, strokePx / 2)

            // Inactive top arch (180deg)
            drawArc(
                color = SaatColors.ArcInactive,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokePx,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            // Active golden top arch (progress-driven sweep angle)
            drawArc(
                color = SaatColors.ArcGold,
                startAngle = 180f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokePx,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            // Calculate precise icon position at endpoint of active arc
            val rad = Math.toRadians((180f + sweepAngle).toDouble())
            val radius = diameter / 2f
            val cx = w / 2f
            val cy = strokePx / 2f + radius
            val endX = cx + radius * Math.cos(rad).toFloat()
            val endY = cy + radius * Math.sin(rad).toFloat()

            with(density) {
                iconOffsetX.value = (endX - 9.dp.toPx()).toDp()
                iconOffsetY.value = (endY - 9.dp.toPx()).toDp()
            }
        }

        // Dynamic icon placement centered on active arc endpoint
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = iconOffsetX.value, y = iconOffsetY.value)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = SaatColors.ArcGold,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 22.dp)
        ) {
            Text(
                text = countdown,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = untilPrayerText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun PrayerTimelineProgressTrack(
    activeIndex: Int,
    totalSlots: Int = 6,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val slotWidth = width / totalSlots.toFloat()

        val startX = slotWidth / 2f
        val endX = width - (slotWidth / 2f)
        val activeX = (startX + activeIndex * slotWidth).coerceIn(startX, endX)

        // 1. Unfilled track line (from startX to endX)
        drawLine(
            color = Color(0xFFD1D5DB),
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 2. Active dark green track line (from startX to activeX)
        if (activeIndex > 0) {
            drawLine(
                color = Color(0xFF176345),
                start = Offset(startX, centerY),
                end = Offset(activeX, centerY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 3. Dots at slot centers
        for (i in 0 until totalSlots) {
            val dotX = startX + i * slotWidth
            val isPassedOrActive = i <= activeIndex
            val dotColor = if (isPassedOrActive) Color(0xFF176345) else Color(0xFFCBD5E1)
            val dotRadius = if (i == activeIndex) 5.dp.toPx() else 3.5.dp.toPx()

            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(dotX, centerY)
            )
            if (i == activeIndex) {
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(dotX, centerY)
                )
            }
        }
    }
}

private data class PrayerHeadline(
    val label: String,
    val title: String,
    val subtitle: String
)

@Composable
private fun rememberHeadline(state: PrayerUiState): PrayerHeadline {
    val context = LocalContext.current
    val is24Hour = context.is24HourClock()
    val nextLabel = stringResource(R.string.prayer_widget_next_label)
    val inProgress = stringResource(R.string.prayer_widget_in_progress)
    val schedule = stringResource(R.string.prayer_schedule)
    val allowLocation = stringResource(R.string.prayer_allow_location)
    val fajr = stringResource(R.string.prayer_fajr)
    val dhuhr = stringResource(R.string.prayer_dhuhr)
    val asr = stringResource(R.string.prayer_asr)
    val maghrib = stringResource(R.string.prayer_maghrib)
    val isha = stringResource(R.string.prayer_isha)
    val sunrise = stringResource(R.string.prayer_sunrise)
    fun name(type: PrayerType) = when (type) {
        PrayerType.FAJR -> fajr
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
        PrayerType.SUNRISE -> sunrise
    }
    return remember(
        state.needsPermission,
        state.isGracePeriod,
        state.activePrayer,
        state.nextPrayer,
        state.countdownSubtitle,
        state.timings,
        state.khgtToday?.eventTitle,
        is24Hour
    ) {
        when {
            state.needsPermission -> PrayerHeadline(
                label = schedule,
                title = allowLocation,
                subtitle = ""
            )
            state.isGracePeriod && state.activePrayer != null -> PrayerHeadline(
                label = inProgress,
                title = name(state.activePrayer!!),
                subtitle = state.countdownSubtitle
            )
            state.nextPrayer != null -> {
                val prayerName = name(state.nextPrayer!!)
                val time = state.timings.find { it.type == state.nextPrayer }?.date?.let {
                    val pattern = if (is24Hour) "HH.mm" else "hh.mm a"
                    SimpleDateFormat(pattern, Locale.getDefault()).format(it)
                } ?: "--.--"
                PrayerHeadline(
                    label = nextLabel,
                    title = prayerName,
                    subtitle = "$prayerName · $time"
                )
            }
            else -> PrayerHeadline(
                label = nextLabel,
                title = state.countdownSubtitle.ifBlank { schedule },
                subtitle = state.khgtToday?.eventTitle.orEmpty()
            )
        }
    }
}

@Composable
private fun SchedulePrayerSlot(
    type: PrayerType,
    entry: PrayerEntry?,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val is24Hour = context.is24HourClock()
    val timeText = entry?.date?.let {
        val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
        SimpleDateFormat(pattern, Locale.getDefault()).format(it)
    } ?: "--:--"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isActive) {
                    Modifier
                        .background(Color(0xFFFCFBF9), RoundedCornerShape(12.dp))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF145A43),
                                    Color(0xFFF4EFE2)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = prayerDisplayShort(type),
                color = Color(0xFF1E293B),
                fontSize = 11.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = timeText,
                color = Color(0xFF0F172A),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PrayerCardLoading() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SaatSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(14.dp)
        )
        Spacer(Modifier.height(8.dp))
        SaatSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(28.dp)
        )
        Spacer(Modifier.height(16.dp))
        SaatSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun SchedulePrayerSlotSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp, horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SaatSkeletonOnDark(
                modifier = Modifier
                    .width(26.dp)
                    .height(10.dp)
            )
            SaatSkeletonOnDark(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
            )
            SaatSkeletonOnDark(
                modifier = Modifier
                    .width(28.dp)
                    .height(11.dp)
            )
        }
    }
}

@Composable
private fun prayerDisplayShort(type: PrayerType): String = when (type) {
    PrayerType.FAJR -> stringResource(R.string.prayer_fajr)
    PrayerType.SUNRISE -> stringResource(R.string.prayer_sunrise)
    PrayerType.DHUHR -> stringResource(R.string.prayer_dhuhr)
    PrayerType.ASR -> stringResource(R.string.prayer_asr)
    PrayerType.MAGHRIB -> stringResource(R.string.prayer_maghrib)
    PrayerType.ISHA -> stringResource(R.string.prayer_isha)
}
