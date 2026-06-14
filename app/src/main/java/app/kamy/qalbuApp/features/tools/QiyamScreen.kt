package app.kamy.qalbuApp.features.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.infrastructure.preferences.QiyamTrackerStore
import app.kamy.qalbuApp.ui.layout.floatingNavBottomPadding
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset

@Composable
fun QiyamScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var loggedTonight by remember { mutableStateOf(QiyamTrackerStore.isLogged(context)) }
    val snapshot = remember(loggedTonight) { QiyamTrackerStore.snapshot(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset()
            .verticalScroll(rememberScrollState())
            .padding(bottom = floatingNavBottomPadding())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = stringResource(R.string.qiyam_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.qiyam_explain),
                style = MaterialTheme.typography.bodyLarge,
                color = AlKhatibColors.Slate800
            )
            Text(
                text = stringResource(R.string.qiyam_explain_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AlKhatibColors.DeepEmerald.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.qiyam_tonight_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.qiyam_tonight_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                }
                Switch(
                    checked = loggedTonight,
                    onCheckedChange = {
                        loggedTonight = QiyamTrackerStore.toggleTonight(context)
                    }
                )
            }

            Text(
                text = stringResource(R.string.qiyam_stats, snapshot.nightsThisMonth, snapshot.streak),
                style = MaterialTheme.typography.labelLarge,
                color = AlKhatibColors.DeepEmerald,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
