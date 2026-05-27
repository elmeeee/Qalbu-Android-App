package app.kamy.qalbuApp.features.today.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.features.today.PrayerUiState

/**
 * Prayer dashboard with the resting goat mascot (iOS Today parity).
 * Mascot overlaps the top-right of the prayer card.
 */
@Composable
fun TodayPrayerMascotSection(
    state: PrayerUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        PrayerDashboardCard(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp)
        )
        Image(
            painter = painterResource(R.drawable.today_mascot),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 2.dp)
                .offset(y = (-4).dp)
                .width(132.dp)
                .height(108.dp)
        )
    }
}
