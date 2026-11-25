package app.kamy.saatApp.design.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class SaatCardStyle {
    Elevated,
    Filled,
    Outlined
}

@Composable
fun SaatCard(
    modifier: Modifier = Modifier,
    style: SaatCardStyle = SaatCardStyle.Elevated,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large
    val elevation = CardDefaults.elevatedCardElevation(
        defaultElevation = 1.dp,
        pressedElevation = 4.dp,
        focusedElevation = 2.dp,
        hoveredElevation = 3.dp
    )
    val padding = Modifier.padding(SaatCardDefaults.ContentPadding)

    when (style) {
        SaatCardStyle.Elevated -> {
            if (onClick != null) {
                val interactionSource = remember { MutableInteractionSource() }
                ElevatedCard(
                    onClick = onClick,
                    modifier = modifier.fillMaxWidth().pressScale(interactionSource, 0.98f),
                    shape = shape,
                    colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                    elevation = elevation,
                    interactionSource = interactionSource
                ) {
                    Column(padding, content = content)
                }
            } else {
                ElevatedCard(
                    modifier = modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                    elevation = elevation
                ) {
                    Column(padding, content = content)
                }
            }
        }
        SaatCardStyle.Filled -> {
            if (onClick != null) {
                val interactionSource = remember { MutableInteractionSource() }
                Card(
                    onClick = onClick,
                    modifier = modifier.fillMaxWidth().pressScale(interactionSource, 0.98f),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    interactionSource = interactionSource
                ) {
                    Column(padding, content = content)
                }
            } else {
                Card(
                    modifier = modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = containerColor)
                ) {
                    Column(padding, content = content)
                }
            }
        }
        SaatCardStyle.Outlined -> {
            if (onClick != null) {
                val interactionSource = remember { MutableInteractionSource() }
                OutlinedCard(
                    onClick = onClick,
                    modifier = modifier.fillMaxWidth().pressScale(interactionSource, 0.98f),
                    shape = shape,
                    colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                    interactionSource = interactionSource
                ) {
                    Column(padding, content = content)
                }
            } else {
                OutlinedCard(
                    modifier = modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
                ) {
                    Column(padding, content = content)
                }
            }
        }
    }
}

object SaatCardDefaults {
    val ContentPadding = 16.dp
}
