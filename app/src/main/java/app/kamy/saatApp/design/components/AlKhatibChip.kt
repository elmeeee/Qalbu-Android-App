package app.kamy.saatApp.design.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.kamy.saatApp.design.theme.SaatColors

@Composable
fun SaatVerseReferenceChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 2.dp
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SaatRevelationChip(
    label: String,
    isMeccan: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = if (isMeccan) SaatColors.Gold else SaatColors.BlueLink
    SuggestionChip(
        onClick = {},
        enabled = false,
        modifier = modifier,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            disabledContainerColor = accent.copy(alpha = 0.12f),
            disabledLabelColor = accent
        ),
        border = null
    )
}

@Composable
fun SaatFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}
