package app.kamy.saatApp.features.account

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.SaatSettingsGroup
import app.kamy.saatApp.design.components.SaatSettingsNavigationRow

@Composable
fun AccountToolsSection(
    onOpenQibla: () -> Unit,
    onOpenDhikr: () -> Unit,
    onOpenZakat: () -> Unit,
    onOpenQiyam: () -> Unit
) {
    SaatSettingsGroup {
        SaatSettingsNavigationRow(
            icon = Icons.Filled.Explore,
            title = stringResource(R.string.qibla_title),
            subtitle = stringResource(R.string.qibla_account_subtitle),
            onClick = onOpenQibla
        )
        SaatSettingsNavigationRow(
            icon = Icons.Filled.AutoStories,
            title = stringResource(R.string.doa_zikir_title),
            subtitle = stringResource(R.string.doa_zikir_account_subtitle),
            onClick = onOpenDhikr
        )
        SaatSettingsNavigationRow(
            icon = Icons.Filled.NightsStay,
            title = stringResource(R.string.qiyam_title),
            subtitle = stringResource(R.string.qiyam_account_subtitle),
            onClick = onOpenQiyam
        )
        SaatSettingsNavigationRow(
            icon = Icons.Filled.Calculate,
            title = stringResource(R.string.zakat_title),
            subtitle = stringResource(R.string.zakat_account_subtitle),
            onClick = onOpenZakat
        )
    }
}
