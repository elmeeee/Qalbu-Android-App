package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.kamy.saatApp.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.TafsirPayload
import app.kamy.saatApp.features.reader.ReaderEmptyState
import app.kamy.saatApp.features.reader.ReaderErrorState
import app.kamy.saatApp.features.reader.ReaderKnowledgeSheetBackground
import app.kamy.saatApp.features.reader.ReaderLoadingSkeleton
import app.kamy.saatApp.features.reader.ReaderSheetDivider
import app.kamy.saatApp.features.reader.ReaderSheetScrollBody
import app.kamy.saatApp.features.reader.ReaderSheetTopBar
import app.kamy.saatApp.features.reader.VerseContextHeader
import app.kamy.saatApp.ui.common.ReaderHtmlView
import app.kamy.saatApp.ui.common.looksLikeHtml
import app.kamy.saatApp.ui.common.stripHtmlTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirSheet(
    isVisible: Boolean,
    isLoading: Boolean,
    tafsir: TafsirPayload?,
    verseReference: String,
    error: AppError? = null,
    onDismiss: () -> Unit,
    onReload: () -> Unit = {}
) {
    if (!isVisible) return
    val commentaryLabel = stringResource(R.string.commentary)
    val tafsirEmptyDetail = stringResource(R.string.tafsir_empty_detail)
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
            ReaderSheetTopBar(title = stringResource(R.string.tafsir), onDone = onDismiss)
            VerseContextHeader(
                verseReference = verseReference,
                iconRes = R.drawable.ic_tafsir,
                subtitle = when {
                    isLoading -> null
                    tafsir?.resourceName != null -> tafsir.resourceName
                    else -> commentaryLabel
                }
            )
            ReaderSheetDivider()

            when {
                isLoading -> ReaderSheetScrollBody { ReaderLoadingSkeleton() }
                error != null -> ReaderSheetScrollBody {
                    ReaderErrorState(
                        error = error,
                        onRetry = onReload,
                        featureTitleRes = R.string.tafsir_load_error
                    )
                }
                commentaryUnavailable -> ReaderSheetScrollBody {
                    ReaderEmptyState(
                        title = stringResource(R.string.tafsir_empty),
                        description = tafsirEmptyDetail
                    )
                }
                else -> ReaderSheetScrollBody { TafsirBody(rawText) }
            }
        }
    }
}

@Composable
private fun TafsirBody(rawHtml: String) {
    if (rawHtml.looksLikeHtml()) {
        ReaderHtmlView(htmlBody = rawHtml, modifier = Modifier.fillMaxWidth())
    } else {
        Text(
            text = rawHtml.stripHtmlTags(),
            color = SaatColors.Slate900,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.35f
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        )
    }
}
