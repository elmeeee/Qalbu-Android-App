package app.kamy.qalbuApp.features.quran

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.design.components.AlKhatibInlineError
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.domain.model.QuranChapter
import app.kamy.qalbuApp.domain.model.SearchNavResult
import app.kamy.qalbuApp.domain.model.SearchVerseResult
import app.kamy.qalbuApp.ui.common.rememberErrorDisplay

@Composable
fun QuranSearchSectionLabel(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun QuranSearchLoadingRow(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .padding(vertical = 16.dp)
            .size(28.dp),
        color = AlKhatibColors.DeepEmerald,
        strokeWidth = 2.dp
    )
}

@Composable
fun QuranSearchErrorRow(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val display = error.rememberErrorDisplay(R.string.quran_search_failed)
    if (display != null) {
        AlKhatibInlineError(
            display = display,
            onRetry = onRetry,
            modifier = modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal)
        )
    }
}

@Composable
fun SearchNavResultRow(
    result: SearchNavResult,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeLabel = when (result.type) {
        "surah" -> stringResource(R.string.search_result_surah)
        "page" -> stringResource(R.string.search_result_page)
        "juz" -> stringResource(R.string.search_result_juz)
        else -> result.type.replaceFirstChar { it.uppercase() }
    }
    SearchResultRow(
        title = result.name,
        subtitle = typeLabel,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun SearchVerseResultRow(
    result: SearchVerseResult,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchResultRow(
        title = result.verseKey,
        subtitle = result.name,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun SearchResultRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.DeepEmerald,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VerseReferenceResultRow(
    reference: VerseReference,
    chapter: QuranChapter?,
    onOpen: (QuranChapter, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(R.string.verse_reference_result, reference.chapter, reference.ayah)
    Surface(
        onClick = { chapter?.let { onOpen(it, reference.ayah) } },
        enabled = chapter != null,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.DeepEmerald
            )
            chapter?.let {
                Text(
                    text = it.displayComplexName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlKhatibColors.Slate500
                )
            } ?: Text(
                text = stringResource(R.string.verse_reference_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )
        }
    }
}

@Composable
fun MushafPageResultRow(
    page: Int,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.mushaf_open_page_result, page),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.DeepEmerald
            )
            Text(
                text = stringResource(R.string.mushaf_open_page_hint),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )
        }
    }
}

@Composable
fun QuranSearchEmptyState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 48.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_matches),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.search_empty_message, query.normalizedSearchQuery()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
