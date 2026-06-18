package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.domain.model.KhgtTodayInfo

@Composable
fun TodayImportantDayBanner(
    info: KhgtTodayInfo?,
    modifier: Modifier = Modifier
) {
    val event = info?.eventTitle ?: return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AlKhatibColors.PrayerCreamWarm.copy(alpha = 0.35f))
            .padding(14.dp)
    ) {
        Icon(
            Icons.Filled.Event,
            contentDescription = null,
            tint = AlKhatibColors.GoldDeep,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = stringResource(R.string.khgt_important_day),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.GoldDeep
        )
        Text(
            text = event,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.Slate800,
            modifier = Modifier.padding(top = 4.dp)
        )
        info.pasaran?.let { pasaran ->
            Text(
                text = stringResource(R.string.khgt_pasaran_label, pasaran),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
