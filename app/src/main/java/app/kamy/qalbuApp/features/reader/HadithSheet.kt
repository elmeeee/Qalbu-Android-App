package app.kamy.qalbuApp.features.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.HadithReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithSheet(
    isVisible: Boolean,
    isLoading: Boolean,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    hadiths: List<HadithReference>,
    verseReference: String,
    error: String? = null,
    onDismiss: () -> Unit,
    onReload: () -> Unit = {},
    onLoadMore: () -> Unit = {}
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val items = hadiths.toDisplayItems()
    val empty = !isLoading && error == null && items.isEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        ReaderKnowledgeSheetBackground(
            modifier = Modifier.fillMaxHeight(0.92f)
        ) {
            ReaderSheetTopBar(title = "Hadith", onDone = onDismiss)
            VerseContextHeader(
                verseReference = verseReference,
                icon = Icons.Filled.Forum,
                subtitle = when {
                    isLoading -> null
                    items.isNotEmpty() -> "${items.size} hadith${if (items.size == 1) "" else "s"}"
                    else -> null
                }
            )
            ReaderSheetDivider()

            when {
                isLoading -> ReaderSheetScrollBody {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(3) { HadithCardSkeleton() }
                    }
                }
                error != null -> ReaderSheetScrollBody {
                    ReaderErrorState(
                        title = "Couldn't load hadith",
                        description = error,
                        onRetry = onReload
                    )
                }
                empty -> ReaderSheetScrollBody {
                    ReaderEmptyState(
                        title = "No hadith here",
                        description = "No hadith references are linked to this ayah yet.",
                        icon = Icons.Filled.Forum
                    )
                }
                else -> ReaderSheetLazyBody {
                    items(items, key = { it.id }) { item ->
                        HadithCard(item)
                    }
                    if (hasMore) {
                        item(key = "load_more") {
                            FilledTonalButton(
                                onClick = onLoadMore,
                                enabled = !isLoadingMore,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isLoadingMore) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = AlKhatibColors.DeepEmerald
                                    )
                                } else {
                                    Text("Load more", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HadithCard(item: HadithDisplayItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .readerKnowledgeCard()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = item.sourceName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.DeepEmerald,
                modifier = Modifier.weight(1f)
            )
            item.referenceLabel?.let { ref ->
                Text(
                    text = ref,
                    style = MaterialTheme.typography.labelMedium,
                    color = AlKhatibColors.Slate500,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        item.chapterTitle?.let { chapter ->
            Text(
                text = chapter,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.Gold
            )
        }
        Text(
            text = item.body,
            style = MaterialTheme.typography.bodyLarge,
            color = AlKhatibColors.Slate900,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.35f,
            modifier = Modifier.fillMaxWidth()
        )
        item.gradeLines.forEach { line ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = AlKhatibColors.DeepEmerald.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.DeepEmerald.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
