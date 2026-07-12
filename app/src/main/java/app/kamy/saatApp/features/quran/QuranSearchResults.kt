package app.kamy.saatApp.features.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.design.components.SaatInlineError
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.model.QuranChapter
import app.kamy.saatApp.domain.model.SearchVerseResult
import app.kamy.saatApp.ui.common.rememberErrorDisplay

@Composable
fun QuranSearchSectionLabel(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(
            horizontal = SaatSpacing.screenHorizontal,
            vertical = 10.dp
        ),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = SaatColors.Slate500
    )
}

@Composable
fun QuranSearchLoadingRow(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .padding(vertical = 20.dp)
            .size(24.dp),
        color = SaatColors.DeepEmerald,
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
        SaatInlineError(
            display = display,
            onRetry = onRetry,
            modifier = modifier.padding(horizontal = SaatSpacing.screenHorizontal)
        )
    }
}

@Composable
fun SearchVerseResultRow(
    result: SearchVerseResult,
    chapter: QuranChapter?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surahLabel = chapter?.displayComplexName
        ?: stringResource(R.string.surah_number, result.chapterNumber)
    val title = stringResource(
        R.string.search_verse_result_title,
        surahLabel,
        result.ayahNumber
    )
    QuranSearchResultRow(
        title = title,
        subtitle = result.name,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun VerseReferenceResultRow(
    reference: VerseReference,
    chapter: QuranChapter?,
    onOpen: (QuranChapter, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val surahLabel = chapter?.displayComplexName
        ?: stringResource(R.string.surah_number, reference.chapter)
    val label = stringResource(R.string.search_verse_goto, surahLabel, reference.ayah)
    QuranSearchResultRow(
        title = label,
        subtitle = stringResource(R.string.search_verse_goto_hint),
        enabled = chapter != null,
        onClick = { chapter?.let { onOpen(it, reference.ayah) } },
        modifier = modifier,
        emphasized = true
    )
}

@Composable
fun JuzReferenceResultRow(
    juzNumber: Int,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuranSearchResultRow(
        title = stringResource(R.string.search_juz_goto, juzNumber),
        subtitle = stringResource(R.string.search_juz_goto_hint),
        enabled = true,
        onClick = onOpen,
        modifier = modifier,
        emphasized = true
    )
}

@Composable
private fun QuranSearchResultRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = SaatSpacing.screenHorizontal,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) {
                    if (emphasized) SaatColors.DeepEmerald else MaterialTheme.colorScheme.onSurface
                } else {
                    SaatColors.Slate500
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (enabled) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (emphasized) SaatColors.Teal else SaatColors.Slate500,
                modifier = Modifier.size(18.dp)
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
            .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            tint = SaatColors.Teal.copy(alpha = 0.7f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(14.dp))
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
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun QuranSearchResultDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = SaatSpacing.screenHorizontal),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}
