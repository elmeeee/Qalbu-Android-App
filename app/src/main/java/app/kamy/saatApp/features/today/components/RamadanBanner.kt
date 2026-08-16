package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.infrastructure.preferences.RamadanPreferencesStore

@Composable
fun RamadanBanner(
    modifier: Modifier = Modifier,
    onToggleTarawih: () -> Unit = {}
) {
    val context = LocalContext.current
    if (!RamadanPreferencesStore.isModeEnabled(context)) return

    var tarawihDone by remember {
        mutableStateOf(RamadanPreferencesStore.isTarawihDone(context))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SaatColors.Gold.copy(alpha = 0.12f))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.ramadan_mode_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = SaatColors.GoldDeep
        )
        Text(
            text = stringResource(R.string.ramadan_mode_body),
            style = MaterialTheme.typography.bodySmall,
            color = SaatColors.Slate800,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    tarawihDone = RamadanPreferencesStore.toggleTarawihDone(context)
                    onToggleTarawih()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ramadan_tarawih),
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate800
            )
            Switch(
                checked = tarawihDone,
                onCheckedChange = {
                    tarawihDone = RamadanPreferencesStore.toggleTarawihDone(context)
                    onToggleTarawih()
                }
            )
        }
    }
}
