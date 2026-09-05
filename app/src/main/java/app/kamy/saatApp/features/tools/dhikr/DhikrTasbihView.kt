package app.kamy.saatApp.features.tools.dhikr

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val TOTAL_BEADS = 33

@Composable
fun TasbeehCounterWidget(
    count: Int,
    pulseKey: Int,
    modifier: Modifier = Modifier,
    counterWidth: Dp = 320.dp,
    counterHeight: Dp = 370.dp,
    showCenterText: Boolean = true,
    onTap: () -> Unit
) {
    val activeIndex = remember(count) {
        if (count > 0) (count - 1) % TOTAL_BEADS else 0
    }

    val roundCount = remember(count) {
        if (count > 0) ((count - 1) / TOTAL_BEADS) + 1 else 1
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (pulseKey > 0) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "bead_pulse"
    )

    var accumulatedDragDistance by remember { mutableFloatStateOf(0f) }
    val dragThresholdPx = 36f
    val densityVal = LocalDensity.current.density
    val shouldDisplayCenterText = showCenterText && counterWidth >= 140.dp

    BoxWithConstraints(
        modifier = modifier
            .size(width = counterWidth, height = counterHeight)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, dragAmount ->
                        val distance = dragAmount.x + dragAmount.y
                        accumulatedDragDistance += distance
                        if (abs(accumulatedDragDistance) >= dragThresholdPx) {
                            accumulatedDragDistance = 0f
                            onTap()
                        }
                    },
                    onDragEnd = { accumulatedDragDistance = 0f },
                    onDragCancel = { accumulatedDragDistance = 0f }
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.TopStart
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val sizePx = min(widthPx, heightPx)

        val centerX = widthPx / 2f
        val centerY = heightPx * 0.42f
        val radius = sizePx * 0.31f
        val beadSizePx = sizePx * 0.08f
        val beadSizeDp = (beadSizePx / densityVal).dp

        val deltaGap = 0.32f // radians (~18 degrees gap at bottom)
        val startAngle = Math.PI / 2 + deltaGap
        val totalSpan = 2 * Math.PI - (2 * deltaGap)

        // Precompute coordinates of 33 beads along the circular loop
        val beadCoords = remember(centerX, centerY, radius) {
            List(TOTAL_BEADS) { i ->
                val fraction = i.toFloat() / (TOTAL_BEADS - 1)
                val angle = startAngle + fraction * totalSpan
                val bx = centerX + cos(angle).toFloat() * radius
                val by = centerY + sin(angle).toFloat() * radius
                Offset(bx, by)
            }
        }

        // 1. LAYER 1 (BACK): Connecting Rope/String
        val ropeColor = Color(0xFF6E4723)
        val strokeWidthPx = 3.5f * densityVal
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            if (beadCoords.isNotEmpty()) {
                path.moveTo(beadCoords.first().x, beadCoords.first().y)
                for (i in 1 until beadCoords.size) {
                    path.lineTo(beadCoords[i].x, beadCoords[i].y)
                }
                val bottomCenterY = centerY + radius
                path.lineTo(centerX, bottomCenterY)
                path.lineTo(beadCoords.first().x, beadCoords.first().y)
            }
            drawPath(
                path = path,
                color = ropeColor,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // 2. LAYER 2 (MIDDLE): 33 Beads
        for (i in 0 until TOTAL_BEADS) {
            val coord = beadCoords[i]
            val leftDp = ((coord.x - beadSizePx / 2f) / densityVal).dp
            val topDp = ((coord.y - beadSizePx / 2f) / densityVal).dp

            val isActive = i == activeIndex

            Box(
                modifier = Modifier
                    .offset(x = leftDp, y = topDp)
                    .size(beadSizeDp)
                    .scale(if (isActive) pulseScale * 1.20f else 1.0f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        if (isActive) R.drawable.bead_active else R.drawable.bead
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // 3. LAYER 3 (FRONT): Seamless Compact Connector & Tassel at Bottom
        val bottomBeadY = centerY + radius

        val ballSizePx = sizePx * 0.085f
        val ballSizeDp = (ballSizePx / densityVal).dp
        val ballLeftDp = ((centerX - ballSizePx / 2f) / densityVal).dp
        val ballTopDp = ((bottomBeadY - ballSizePx * 0.40f) / densityVal).dp

        val capWidthPx = sizePx * 0.080f
        val capHeightPx = capWidthPx * 0.70f
        val capWidthDp = (capWidthPx / densityVal).dp
        val capHeightDp = (capHeightPx / densityVal).dp
        val capLeftDp = ((centerX - capWidthPx / 2f) / densityVal).dp
        val capTopDp = ((bottomBeadY + ballSizePx * 0.30f) / densityVal).dp

        val tasselCapWidthPx = sizePx * 0.075f
        val tasselCapHeightPx = tasselCapWidthPx * 0.65f
        val tasselCapWidthDp = (tasselCapWidthPx / densityVal).dp
        val tasselCapHeightDp = (tasselCapHeightPx / densityVal).dp
        val tasselCapLeftDp = ((centerX - tasselCapWidthPx / 2f) / densityVal).dp
        val tasselCapTopDp = ((bottomBeadY + ballSizePx * 0.30f + capHeightPx * 0.65f) / densityVal).dp

        val tasselWidthPx = sizePx * 0.14f
        val tasselHeightPx = tasselWidthPx * 1.25f
        val tasselWidthDp = (tasselWidthPx / densityVal).dp
        val tasselHeightDp = (tasselHeightPx / densityVal).dp
        val tasselLeftDp = ((centerX - tasselWidthPx / 2f) / densityVal).dp
        val tasselTopDp = ((bottomBeadY + ballSizePx * 0.30f + capHeightPx * 0.65f + tasselCapHeightPx * 0.50f) / densityVal).dp

        // Gold Connector Ball
        Image(
            painter = painterResource(R.drawable.connector_ball),
            contentDescription = null,
            modifier = Modifier
                .offset(x = ballLeftDp, y = ballTopDp)
                .size(ballSizeDp),
            contentScale = ContentScale.Fit
        )

        // Gold Connector Cap
        Image(
            painter = painterResource(R.drawable.connector_cap),
            contentDescription = null,
            modifier = Modifier
                .offset(x = capLeftDp, y = capTopDp)
                .size(width = capWidthDp, height = capHeightDp),
            contentScale = ContentScale.Fit
        )

        // Tassel Cap
        Image(
            painter = painterResource(R.drawable.tassel_cap),
            contentDescription = null,
            modifier = Modifier
                .offset(x = tasselCapLeftDp, y = tasselCapTopDp)
                .size(width = tasselCapWidthDp, height = tasselCapHeightDp),
            contentScale = ContentScale.Fit
        )

        // Green Tassel
        Image(
            painter = painterResource(R.drawable.tassel),
            contentDescription = null,
            modifier = Modifier
                .offset(x = tasselLeftDp, y = tasselTopDp)
                .size(width = tasselWidthDp, height = tasselHeightDp),
            contentScale = ContentScale.Fit
        )

        // 4. LAYER 4: Counter Number & Instruction Text WRAPPED INSIDE THE CENTER OF THE RING (Only shown when shouldDisplayCenterText is true)
        if (shouldDisplayCenterText) {
            val maxTextWidthDp = (radius * 1.45f / densityVal).dp
            val centerBoxTopDp = ((centerY - (radius * 0.55f)) / densityVal).dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = centerBoxTopDp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Round Badge for 100x / multi-round dhikr (shows if roundCount > 1)
                if (roundCount > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF124C31).copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Putaran $roundCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF124C31)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }

                // Big Counter Number
                Text(
                    text = count.toString(),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF124C31),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(2.dp))

                // Instruction Text Wrapped Inside Ring Center
                Text(
                    text = stringResource(R.string.tasbih_tap_instruction),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier
                        .widthIn(max = maxTextWidthDp)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumTasbihCounter(
    count: Int,
    target: Int,
    pulseKey: Int,
    modifier: Modifier = Modifier,
    subtitle: String,
    counterSize: Dp = 88.dp,
    showCenterText: Boolean = false
) {
    TasbeehCounterWidget(
        count = count,
        pulseKey = pulseKey,
        modifier = modifier,
        counterWidth = counterSize,
        counterHeight = counterSize * 1.2f,
        showCenterText = showCenterText,
        onTap = {}
    )
}
