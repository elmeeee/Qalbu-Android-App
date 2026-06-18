package app.kamy.qalbuApp.features.today.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.kamy.qalbuApp.features.today.PrayerUiState

@Composable
fun TodayPrayerMascotSection(
    state: PrayerUiState,
    onRetry: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    PrayerDashboardCard(
        state = state,
        onRetry = onRetry,
        onOpenCalendar = onOpenCalendar,
        onOpenLocation = onOpenLocation,
        modifier = modifier
    )
}
