package app.kamy.qalbuApp.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.design.components.AlKhatibInlineError
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.infrastructure.repository.PrayerCalendarDay
import app.kamy.qalbuApp.ui.common.rememberErrorDisplay
import java.text.DateFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerCalendarSheet(
    visible: Boolean,
    year: Int,
    month: Int,
    days: List<PrayerCalendarDay>,
    loading: Boolean,
    error: AppError?,
    onDismiss: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRetry: () -> Unit
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val monthLabel = DateFormatSymbols.getInstance(Locale.getDefault()).months.getOrNull(month - 1).orEmpty()
    val errorDisplay = error.rememberErrorDisplay(R.string.prayer_calendar_load_failed)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                }
                Text(
                    text = "$monthLabel $year",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
            Spacer(Modifier.height(8.dp))
            when {
                loading && days.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AlKhatibColors.DeepEmerald)
                    }
                }
                errorDisplay != null && days.isEmpty() -> {
                    AlKhatibInlineError(display = errorDisplay, onRetry = onRetry)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(days, key = { it.day }) { day ->
                            PrayerCalendarDayRow(day = day)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PrayerCalendarDayRow(day: PrayerCalendarDay) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AlKhatibColors.LightGrey, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = day.gregorianLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate900
        )
        day.hijriLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = AlKhatibColors.Slate500
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalendarTimeChip(label = stringResource(R.string.prayer_fajr), time = day.fajr)
            CalendarTimeChip(label = stringResource(R.string.prayer_dhuhr), time = day.dhuhr)
            CalendarTimeChip(label = stringResource(R.string.prayer_maghrib), time = day.maghrib)
            CalendarTimeChip(label = stringResource(R.string.prayer_isha), time = day.isha)
        }
    }
}

@Composable
private fun CalendarTimeChip(label: String, time: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500)
        Text(text = time, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
