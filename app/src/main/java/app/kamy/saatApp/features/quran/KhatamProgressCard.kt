package app.kamy.saatApp.features.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.infrastructure.preferences.KhatamProgressStore
import app.kamy.saatApp.infrastructure.preferences.KhatamUiSnapshot

@Composable
fun KhatamProgressCard(
    modifier: Modifier = Modifier,
    onAdjustGoal: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val snapshot = remember { KhatamProgressStore.snapshot(context) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AlKhatibColors.DeepEmerald.copy(alpha = 0.06f))
            .padding(horizontal = 4.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.khatam_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            if (snapshot.streakDays > 0) {
                Text(
                    text = stringResource(R.string.khatam_streak, snapshot.streakDays),
                    style = MaterialTheme.typography.labelMedium,
                    color = AlKhatibColors.GoldDeep
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        KhatamProgressLine(
            label = stringResource(R.string.khatam_today),
            progress = snapshot.todayProgressFraction,
            detail = stringResource(
                R.string.khatam_pages_today,
                snapshot.pagesReadToday,
                snapshot.dailyPageGoal
            )
        )
        Spacer(Modifier.height(10.dp))
        KhatamProgressLine(
            label = stringResource(R.string.khatam_overall),
            progress = snapshot.overallProgressFraction,
            detail = stringResource(
                R.string.khatam_overall_pages,
                (snapshot.overallProgressFraction * app.kamy.saatApp.infrastructure.preferences.MushafReadingStore.totalPages).toInt(),
                app.kamy.saatApp.infrastructure.preferences.MushafReadingStore.totalPages
            )
        )
    }
}

@Composable
private fun KhatamProgressLine(label: String, progress: Float, detail: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AlKhatibColors.Slate800
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelMedium,
                color = AlKhatibColors.Slate500
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = AlKhatibColors.Gold,
            trackColor = AlKhatibColors.SoftGrey,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun KhatamTodayCard(modifier: Modifier = Modifier) {
    KhatamProgressCard(modifier = modifier)
}
