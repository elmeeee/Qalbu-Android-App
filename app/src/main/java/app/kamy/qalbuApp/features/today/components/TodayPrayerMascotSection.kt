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
                // Push card down so only the mascot's feet touch the top edge
                // of the card, instead of the full body sitting inside it.
                .padding(top = 68.dp)
        )
        Image(
            painter = painterResource(R.drawable.today_mascot),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                // Pull mascot slightly outside the card so only the lower legs overlap.
                .offset(x = 6.dp, y = 4.dp)
                .width(132.dp)
                .height(108.dp)
        )
    }
}
