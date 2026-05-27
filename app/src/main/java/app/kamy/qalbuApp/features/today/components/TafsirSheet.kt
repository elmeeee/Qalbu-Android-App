package app.kamy.qalbuApp.features.today.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.TafsirPayload

/**
 * Mirrors iOS Design/Organisms/Reader/TafsirReaderSheet.swift.
 * Bottom sheet showing Ibn Kathir (resource 169) commentary for the current ayah.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirSheet(
    isVisible: Boolean,
    isLoading: Boolean,
    tafsir: TafsirPayload?,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .heightIn(min = 280.dp)
        ) {
            Text(
                text = tafsir?.resourceName ?: "Tafsir",
                style = MaterialTheme.typography.titleLarge,
                color = AlKhatibColors.DeepEmerald,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap to dismiss",
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )
            Text(
                text = when {
                    isLoading -> "Loading…"
                    tafsir?.text.isNullOrBlank() -> "No tafsir available for this verse."
                    else -> tafsir!!.text!!.replace(Regex("<[^>]+>"), "").trim()
                },
                color = AlKhatibColors.Slate900,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
