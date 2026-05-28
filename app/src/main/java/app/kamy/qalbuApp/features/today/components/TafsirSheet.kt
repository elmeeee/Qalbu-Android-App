package app.kamy.qalbuApp.features.today.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.features.reader.ReaderEmptyState
import app.kamy.qalbuApp.features.reader.ReaderErrorState
import app.kamy.qalbuApp.features.reader.ReaderKnowledgeSheetBackground
import app.kamy.qalbuApp.features.reader.ReaderLoadingSkeleton
import app.kamy.qalbuApp.features.reader.ReaderSheetDivider
import app.kamy.qalbuApp.features.reader.ReaderSheetTopBar
import app.kamy.qalbuApp.features.reader.VerseContextHeader
import app.kamy.qalbuApp.ui.common.ReaderHtmlView
import app.kamy.qalbuApp.ui.common.looksLikeHtml
import app.kamy.qalbuApp.ui.common.stripHtmlTags

/**
 * Bottom sheet for Ibn Kathir (resource 169) tafsir — mirrors iOS TafsirReaderSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirSheet(
    isVisible: Boolean,
    isLoading: Boolean,
    tafsir: TafsirPayload?,
    verseReference: String,
    error: String? = null,
    onDismiss: () -> Unit,
    onReload: () -> Unit = {}
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rawText = tafsir?.text?.trim().orEmpty()
    val commentaryUnavailable = !isLoading && error == null && rawText.isEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        ReaderKnowledgeSheetBackground(
            modifier = Modifier.fillMaxHeight(0.92f)
        ) {
            ReaderSheetTopBar(title = "Tafsir", onDone = onDismiss)
            VerseContextHeader(
                verseReference = verseReference,
                icon = Icons.AutoMirrored.Filled.MenuBook,
                subtitle = when {
                    isLoading -> null
                    tafsir?.resourceName != null -> tafsir.resourceName
                    else -> "Commentary"
                }
            )
            ReaderSheetDivider()

            when {
                isLoading -> ReaderLoadingSkeleton()
                error != null -> ReaderErrorState(
                    title = "Couldn't load tafsir",
                    description = error,
                    onRetry = onReload
                )
                commentaryUnavailable -> ReaderEmptyState(
                    title = "No commentary here",
                    description = "This verse doesn't include tafsir text for this source yet."
                )
                else -> TafsirBody(rawText)
            }
        }
    }
}

@Composable
private fun TafsirBody(rawHtml: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (rawHtml.looksLikeHtml()) {
            ReaderHtmlView(htmlBody = rawHtml, modifier = Modifier.fillMaxWidth())
        } else {
            Text(
                text = rawHtml.stripHtmlTags(),
                color = AlKhatibColors.Slate900,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    lineHeight = androidx.compose.material3.MaterialTheme.typography.bodyLarge.lineHeight * 1.35f
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}
