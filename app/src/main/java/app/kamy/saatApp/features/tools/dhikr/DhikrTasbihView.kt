package app.kamy.saatApp.features.tools.dhikr

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.design.theme.AlKhatibColors
import kotlin.math.cos
import kotlin.math.sin

private const val BEADS_PER_RING = 33

@Composable
fun PremiumTasbihCounter(
    count: Int,
    target: Int,
    pulseKey: Int,
    modifier: Modifier = Modifier,
    subtitle: String
) {
    val scale by animateFloatAsState(
        targetValue = if (pulseKey == 0) 1f else 0.96f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "tasbih_tap"
    )
    val progress = (count.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    val round = if (count == 0) 0 else (count - 1) / BEADS_PER_RING + 1
    val beadIndex = if (count == 0) -1 else (count - 1) % BEADS_PER_RING

    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerRadius = size.minDimension * 0.42f
            val beadRadius = size.minDimension * 0.018f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0D9488).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = outerRadius * 1.15f
                ),
                radius = outerRadius * 1.15f,
                center = Offset(cx, cy)
            )

            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(cx - outerRadius, cy - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = size.minDimension * 0.012f, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(AlKhatibColors.GoldDeep, AlKhatibColors.Teal, AlKhatibColors.GoldBright)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(cx - outerRadius, cy - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = size.minDimension * 0.014f, cap = StrokeCap.Round)
                )
            }

            val stringRadius = outerRadius * 0.88f
            for (i in 0 until BEADS_PER_RING) {
                val angle = Math.toRadians(-90.0 + (360.0 * i / BEADS_PER_RING))
                val x = cx + stringRadius * cos(angle).toFloat()
                val y = cy + stringRadius * sin(angle).toFloat()
                val isImam = i == 0
                val filled = i <= beadIndex
                val active = i == beadIndex
                val r = when {
                    isImam -> beadRadius * 1.55f
                    active -> beadRadius * 1.35f
                    else -> beadRadius
                }
                if (active) {
                    drawCircle(
                        color = AlKhatibColors.GoldDeep.copy(alpha = 0.25f),
                        radius = r * 2.4f,
                        center = Offset(x, y)
                    )
                }
                drawCircle(
                    color = when {
                        isImam -> AlKhatibColors.DeepEmerald
                        filled -> AlKhatibColors.GoldDeep
                        else -> Color(0xFFCBD5E1)
                    },
                    radius = r,
                    center = Offset(x, y)
                )
            }
        }

        Surface(
            modifier = Modifier.size(148.dp),
            shape = CircleShape,
            color = AlKhatibColors.PureWhite,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.linearGradient(listOf(AlKhatibColors.Teal.copy(0.35f), AlKhatibColors.Gold.copy(0.35f)))
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        count.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.Slate900,
                        fontSize = 44.sp
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = AlKhatibColors.Slate500
                    )
                    if (round > 0) {
                        Text(
                            "×$round",
                            style = MaterialTheme.typography.labelSmall,
                            color = AlKhatibColors.Teal,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}
