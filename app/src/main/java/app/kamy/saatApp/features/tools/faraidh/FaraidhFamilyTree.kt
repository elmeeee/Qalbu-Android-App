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
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.domain.faraidh.SilsilahNode
import java.text.NumberFormat

@Composable
fun FaraidhFamilyTree(
    nodes: List<SilsilahNode>,
    nodeTitle: @Composable (SilsilahNode) -> String,
    nodeSubtitle: @Composable (SilsilahNode) -> String,
    currency: NumberFormat,
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
            TreeLevelRow(parents, nodeTitle, nodeSubtitle, currency)
            VerticalConnector()
        }

        if (centerRow.isNotEmpty()) {
            TreeLevelRow(centerRow, nodeTitle, nodeSubtitle, currency, highlightDeceased = true)
        }

        if (children.isNotEmpty()) {
            VerticalConnector()
            TreeLevelRow(children, nodeTitle, nodeSubtitle, currency)
        }

        if (grandchildren.isNotEmpty()) {
            VerticalConnector()
            TreeLevelRow(grandchildren, nodeTitle, nodeSubtitle, currency)
        }
    }
}

@Composable
private fun TreeLevelRow(
    nodes: List<SilsilahNode>,
    nodeTitle: @Composable (SilsilahNode) -> String,
    nodeSubtitle: @Composable (SilsilahNode) -> String,
    currency: NumberFormat,
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
                currency = currency,
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
            .width(16.dp)
            .height(30.dp)
    ) {
        drawLine(color, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 2f)
        drawCircle(color, radius = 4f, center = Offset(size.width / 2, size.height / 2))
    }
}

@Composable
private fun TreeNodeCard(
    node: SilsilahNode,
    nodeTitle: @Composable (SilsilahNode) -> String,
    nodeSubtitle: @Composable (SilsilahNode) -> String,
    currency: NumberFormat,
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
        modifier = Modifier.width(if (isDeceased) 160.dp else if (compact) 120.dp else 140.dp),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        shadowElevation = if (isDeceased || node.inherits) 4.dp else 1.dp,
        border = androidx.compose.foundation.BorderStroke(if (isDeceased) 2.dp else 1.dp, border)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isDeceased) {
                val capsuleBg = when {
                    node.blocked -> AlKhatibColors.Danger.copy(alpha = 0.1f)
                    node.inherits -> AlKhatibColors.Teal.copy(alpha = 0.1f)
                    else -> AlKhatibColors.SoftGrey.copy(alpha = 0.5f)
                }
                val capsuleFg = when {
                    node.blocked -> AlKhatibColors.Danger
                    node.inherits -> AlKhatibColors.Teal
                    else -> AlKhatibColors.Slate500
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = capsuleBg,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = nodeSubtitle(node).split(" · ").firstOrNull().orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = capsuleFg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AlKhatibColors.Gold.copy(alpha = 0.2f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "PEWARIS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.GoldBright,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                nodeTitle(node),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (node.inherits && node.shareFraction != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = node.shareFraction,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AlKhatibColors.Teal,
                    textAlign = TextAlign.Center
                )
                node.sharePercentage?.let { pct ->
                    Text(
                        text = pct,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AlKhatibColors.Slate500
                    )
                }
                node.shareAmount?.let { amt ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currency.format(amt),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.DeepEmerald,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else if (node.blocked) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Dihijab",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.Danger
                )
            } else if (!isDeceased) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Non-waris",
                    style = MaterialTheme.typography.labelSmall,
                    color = AlKhatibColors.Slate500
                )
            }
        }
    }
}

fun formatFaraidhPercent(value: java.math.BigDecimal): String {
    val scaled = value.setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
    val sep = java.text.DecimalFormatSymbols.getInstance().decimalSeparator
    val num = scaled.toPlainString().replace('.', sep)
    return "$num%"
}
