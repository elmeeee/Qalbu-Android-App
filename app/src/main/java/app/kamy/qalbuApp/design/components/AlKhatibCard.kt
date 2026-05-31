package app.kamy.qalbuApp.design.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AlKhatibCardStyle {
    Elevated,
    Filled,
    Outlined
}

@Composable
fun AlKhatibCard(
    modifier: Modifier = Modifier,
    style: AlKhatibCardStyle = AlKhatibCardStyle.Elevated,
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
    val padding = Modifier.padding(AlKhatibCardDefaults.ContentPadding)

    when (style) {
        AlKhatibCardStyle.Elevated -> {
            if (onClick != null) {
                ElevatedCard(
                    onClick = onClick,
                    modifier = modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                    elevation = elevation
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
        AlKhatibCardStyle.Filled -> {
            if (onClick != null) {
                Card(
                    onClick = onClick,
                    modifier = modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = containerColor)
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
        AlKhatibCardStyle.Outlined -> {
            if (onClick != null) {
                OutlinedCard(
                    onClick = onClick,
                    modifier = modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
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

object AlKhatibCardDefaults {
    val ContentPadding = 16.dp
}
