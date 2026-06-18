package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerLocationSheet(
    visible: Boolean,
    query: String,
    saving: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onSave: () -> Unit,
    onUseGps: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.location_settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            Text(
                text = stringResource(R.string.location_settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.location_city_hint)) },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it) } }
            )
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving && query.isNotBlank()
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.location_save_city))
                }
            }
            OutlinedButton(
                onClick = onUseGps,
                modifier = Modifier.fillMaxWidth(),
                enabled = !saving
            ) {
                Text(stringResource(R.string.location_use_gps))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
