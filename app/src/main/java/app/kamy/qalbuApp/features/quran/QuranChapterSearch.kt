package app.kamy.qalbuApp.features.quran

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.domain.model.QuranChapter

fun List<QuranChapter>.filteredBySearch(query: String): List<QuranChapter> {
    val q = query.trim()
    if (q.isEmpty()) return this
    if (q.all { it.isDigit() }) {
        q.toIntOrNull()?.let { num ->
            return filter { it.id == num }
        }
    }
    val lower = q.lowercase()
    return filter { chapter ->
        chapter.id.toString() == q ||
            chapter.displayComplexName.lowercase().contains(lower) ||
            chapter.nameSimple.orEmpty().lowercase().contains(lower) ||
            chapter.displayTranslatedName.lowercase().contains(lower) ||
            chapter.nameArabic.orEmpty().contains(q) ||
            chapter.revelationLabel.lowercase().contains(lower) ||
            chapter.revelationPlace.orEmpty().lowercase().contains(lower)
    }
}

@Composable
fun QuranChapterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Search surah, number, or place…"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 6f else 1f,
        animationSpec = tween(200),
        label = "searchElevation"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            isFocused -> MaterialTheme.colorScheme.tertiary
            query.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(200),
        label = "searchBorder"
    )
    val fieldShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { shadowElevation = elevation }
            .shadow(
                elevation = if (isFocused) 8.dp else 2.dp,
                shape = fieldShape,
                ambientColor = AlKhatibColors.DeepEmerald.copy(alpha = 0.12f),
                spotColor = AlKhatibColors.Teal.copy(alpha = 0.18f)
            )
            .clip(fieldShape)
            .background(
                if (isFocused) {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            AlKhatibColors.SageMist.copy(alpha = 0.65f)
                        )
                    )
                }
            )
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = borderColor,
                shape = fieldShape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                interactionSource = interactionSource,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }
                        inner()
                    }
                }
            )
            if (query.isNotEmpty() && enabled) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No surah found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Nothing matches “$query”. Try another name, Arabic title, or surah number.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
