package app.kamy.saatApp.features.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.design.components.AlKhatibSettingsNavigationRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import app.kamy.saatApp.infrastructure.offline.MurottalOfflineStore

@Composable
fun OfflineQuranDownloadRow() {
    val context = LocalContext.current
    var sheetVisible by remember { mutableStateOf(false) }
    val recitationId = remember { MurottalOfflineStore.activeReciter(context) }
    var progress by remember {
        mutableIntStateOf(MurottalOfflineStore.downloadedCount(context, recitationId))
    }
    var downloading by remember { mutableStateOf(MurottalOfflineStore.isInProgress(context)) }

    LaunchedEffect(Unit) {
        MurottalOfflineStore.recoverStaleProgress(context)
        downloading = MurottalOfflineStore.isInProgress(context)
        progress = MurottalOfflineStore.downloadedCount(context, recitationId)
    }

    val complete = progress >= 6236
    val subtitle = when {
        complete -> stringResource(R.string.offline_murottal_complete)
        downloading -> stringResource(R.string.offline_murottal_downloading, progress, 6236)
        progress > 0 -> stringResource(R.string.offline_murottal_partial, progress, 6236)
        else -> stringResource(R.string.offline_murottal_subtitle_short)
    }

    AlKhatibSettingsNavigationRow(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = stringResource(R.string.offline_murottal_title),
        subtitle = subtitle,
        onClick = { sheetVisible = true }
    )

    if (sheetVisible) {
        OfflineQuranDownloadSheet(
            onDismiss = {
                sheetVisible = false
                val active = MurottalOfflineStore.activeReciter(context)
                downloading = MurottalOfflineStore.isInProgress(context)
                progress = MurottalOfflineStore.downloadedCount(context, active)
            }
        )
    }
}
