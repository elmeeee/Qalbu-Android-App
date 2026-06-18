package app.kamy.saatApp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors

@Composable
fun LastSyncBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AlKhatibColors.SageMist.copy(alpha = 0.65f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Schedule,
            contentDescription = null,
            tint = AlKhatibColors.Slate500,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium,
            color = AlKhatibColors.Slate500
        )
    }
}

@Composable
fun PrayerLastSyncBanner(
    lastSyncMillis: Long?,
    modifier: Modifier = Modifier
) {
    if (lastSyncMillis == null) return
    val time = android.text.format.DateFormat.getTimeFormat(androidx.compose.ui.platform.LocalContext.current)
        .format(java.util.Date(lastSyncMillis))
    LastSyncBanner(
        message = stringResource(R.string.prayer_last_sync, time),
        modifier = modifier
    )
}
