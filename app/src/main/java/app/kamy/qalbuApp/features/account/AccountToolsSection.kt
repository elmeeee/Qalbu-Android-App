package app.kamy.qalbuApp.features.account

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.components.AlKhatibSettingsGroup
import app.kamy.qalbuApp.design.components.AlKhatibSettingsNavigationRow

@Composable
fun AccountToolsSection(
    onOpenQibla: () -> Unit,
    onOpenDhikr: () -> Unit,
    onOpenZakat: () -> Unit,
    onOpenQiyam: () -> Unit
) {
    AlKhatibSettingsGroup {
        AlKhatibSettingsNavigationRow(
            icon = Icons.Filled.Explore,
            title = stringResource(R.string.qibla_title),
            subtitle = stringResource(R.string.qibla_account_subtitle),
            onClick = onOpenQibla
        )
        AlKhatibSettingsNavigationRow(
            icon = Icons.Filled.Favorite,
            title = stringResource(R.string.dhikr_title),
            subtitle = stringResource(R.string.dhikr_account_subtitle),
            onClick = onOpenDhikr
        )
        AlKhatibSettingsNavigationRow(
            icon = Icons.Filled.NightsStay,
            title = stringResource(R.string.qiyam_title),
            subtitle = stringResource(R.string.qiyam_account_subtitle),
            onClick = onOpenQiyam
        )
        AlKhatibSettingsNavigationRow(
            icon = Icons.Filled.Calculate,
            title = stringResource(R.string.zakat_title),
            subtitle = stringResource(R.string.zakat_account_subtitle),
            onClick = onOpenZakat
        )
    }
}
