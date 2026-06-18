package app.kamy.saatApp.features.tools.faraidh

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.domain.faraidh.SilsilahNode

@Composable
fun FaraidhFamilyTree(
    nodes: List<SilsilahNode>,
    nodeTitle: @Composable (SilsilahNode) -> String,
    nodeSubtitle: @Composable (SilsilahNode) -> String,
    modifier: Modifier = Modifier
) {
    val parents = nodes.filter { it.generationLevel == -1 }
    val centerRow = nodes
        .filter { it.generationLevel == 0 }
        .sortedWith(
            compareBy<SilsilahNode> {
                when {
                    it.id == "deceased" -> 1
                    it.type == app.kamy.saatApp.domain.faraidh.HeirType.HUSBAND ||
                        it.type == app.kamy.saatApp.domain.faraidh.HeirType.WIFE -> 2
                    else -> 0
                }
            }.thenBy { it.id }
        )
    val children = nodes.filter { it.generationLevel == 1 }
    val grandchildren = nodes.filter { it.generationLevel == 2 }
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (parents.isNotEmpty()) {
            TreeLevelRow(parents, nodeTitle, nodeSubtitle)
            VerticalConnector()
        }

        if (centerRow.isNotEmpty()) {
            TreeLevelRow(centerRow, nodeTitle, nodeSubtitle, highlightDeceased = true)
        }

        if (children.isNotEmpty()) {
            VerticalConnector()
            TreeLevelRow(children, nodeTitle, nodeSubtitle)
        }

        if (grandchildren.isNotEmpty()) {
            VerticalConnector()
            TreeLevelRow(grandchildren, nodeTitle, nodeSubtitle)
        }
    }
}

@Composable
private fun TreeLevelRow(
    nodes: List<SilsilahNode>,
    nodeTitle: @Composable (SilsilahNode) -> String,
    nodeSubtitle: @Composable (SilsilahNode) -> String,
    highlightDeceased: Boolean = false
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        nodes.forEach { node ->
            val isDeceased = highlightDeceased && node.id == "deceased"
            TreeNodeCard(
                node = node,
                nodeTitle = nodeTitle,
                nodeSubtitle = nodeSubtitle,
                isDeceased = isDeceased,
                compact = !isDeceased && nodes.size > 4
            )
        }
    }
}

@Composable
private fun VerticalConnector() {
    val color = AlKhatibColors.Teal.copy(alpha = 0.4f)
    Canvas(
        modifier = Modifier
            .width(2.dp)
            .height(24.dp)
    ) {
        drawLine(color, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 2f)
    }
}

@Composable
private fun TreeNodeCard(
    node: SilsilahNode,
    nodeTitle: @Composable (SilsilahNode) -> String,
    nodeSubtitle: @Composable (SilsilahNode) -> String,
    isDeceased: Boolean = false,
    compact: Boolean = false
) {
    val border = when {
        isDeceased -> AlKhatibColors.Gold
        node.blocked -> AlKhatibColors.Danger.copy(alpha = 0.5f)
        node.inherits -> AlKhatibColors.Teal.copy(alpha = 0.6f)
        else -> AlKhatibColors.SoftGrey
    }
    val bg = when {
        isDeceased -> AlKhatibColors.DeepEmerald
        node.blocked -> AlKhatibColors.LightGrey
        node.inherits -> AlKhatibColors.PrayerMint
        else -> AlKhatibColors.PureWhite
    }
    val titleColor = if (isDeceased) Color.White else AlKhatibColors.Slate900
    val subColor = if (isDeceased) Color.White.copy(alpha = 0.85f) else AlKhatibColors.Slate500

    Surface(
        modifier = Modifier.width(if (isDeceased) 148.dp else if (compact) 104.dp else 118.dp),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        shadowElevation = if (isDeceased || node.inherits) 3.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(if (isDeceased) 2.dp else 1.dp, border)
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                nodeTitle(node),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                nodeSubtitle(node),
                style = MaterialTheme.typography.labelSmall,
                color = subColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

fun formatFaraidhPercent(value: java.math.BigDecimal): String {
    val scaled = value.setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
    val sep = java.text.DecimalFormatSymbols.getInstance().decimalSeparator
    val num = scaled.toPlainString().replace('.', sep)
    return "$num%"
}
