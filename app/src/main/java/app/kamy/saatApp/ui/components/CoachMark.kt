package app.kamy.saatApp.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors

class CoachMarkState {
    var isVisible by mutableStateOf(false)
    var currentStep by mutableStateOf(0)
    val targets = mutableStateMapOf<Int, CoachMarkTarget>()

    fun show() {
        if (targets.isNotEmpty()) {
            currentStep = targets.keys.minOrNull() ?: 0
            isVisible = true
        }
    }

    fun next() {
        val sortedKeys = targets.keys.sorted()
        val nextKey = sortedKeys.firstOrNull { it > currentStep }
        if (nextKey != null) {
            currentStep = nextKey
        } else {
            isVisible = false
        }
    }

    fun skip() {
        isVisible = false
    }

    fun isLastStep(): Boolean {
        val maxKey = targets.keys.maxOrNull()
        return currentStep == maxKey || maxKey == null
    }

    fun stepIndex(): Int {
        val sorted = targets.keys.sorted()
        return sorted.indexOf(currentStep) + 1
    }

    fun totalSteps(): Int = targets.size
}

data class CoachMarkTarget(
    val bounds: Rect,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

@Composable
fun rememberCoachMarkState(): CoachMarkState {
    return remember { CoachMarkState() }
}

fun Modifier.coachMarkTarget(
    state: CoachMarkState,
    step: Int,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int
): Modifier = this.onGloballyPositioned { coordinates ->
    if (coordinates.isAttached) {
        val bounds = coordinates.boundsInRoot()
        if (!bounds.isEmpty && bounds.width > 0 && bounds.height > 0) {
            state.targets[step] = CoachMarkTarget(bounds, titleRes, descriptionRes)
        }
    }
}

private val TOOLTIP_HORIZONTAL_PADDING = 20.dp
private val TOOLTIP_VERTICAL_GAP = 12.dp
private val ARROW_HEIGHT = 10.dp
private val ARROW_HALF_WIDTH = 10.dp
private val HIGHLIGHT_PADDING = 6.dp
private val HIGHLIGHT_CORNER = 16.dp

@Composable
fun CoachMarkOverlay(
    state: CoachMarkState,
    onDismiss: () -> Unit
) {
    if (!state.isVisible) return

    val target = state.targets[state.currentStep]
    if (target == null) {
        // No bounds registered for this step — auto-advance
        LaunchedEffect(state.currentStep) { state.next() }
        return
    }

    AnimatedContent(
        targetState = state.currentStep,
        transitionSpec = {
            (slideInVertically { it / 6 } + fadeIn()) togetherWith
                    (slideOutVertically { -it / 6 } + fadeOut())
        },
        label = "coachMarkStep"
    ) { step ->
        val stepTarget = state.targets[step] ?: return@AnimatedContent

        val density = LocalDensity.current
        val config = LocalConfiguration.current
        val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

        val targetCenterY = (stepTarget.bounds.top + stepTarget.bounds.bottom) / 2f
        val showAbove = targetCenterY > screenHeightPx / 2f

        val targetCenterXPx = (stepTarget.bounds.left + stepTarget.bounds.right) / 2f
        val arrowXDp: Dp = with(density) {
            targetCenterXPx.toDp()
                .coerceIn(40.dp, config.screenWidthDp.dp - 40.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(step) {
                    detectTapGestures { tapOffset ->
                        // Tap on highlighted element → advance to next
                        val padPx = HIGHLIGHT_PADDING.toPx()
                        val expanded = Rect(
                            stepTarget.bounds.left - padPx,
                            stepTarget.bounds.top - padPx,
                            stepTarget.bounds.right + padPx,
                            stepTarget.bounds.bottom + padPx
                        )
                        if (expanded.contains(tapOffset)) {
                            if (state.isLastStep()) { state.skip(); onDismiss() }
                            else state.next()
                        }
                        // Taps elsewhere are blocked (no-op)
                    }
                }
        ) {
            // Dark scrim with punched-out highlight
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.99f)
            ) {
                drawRect(Color.Black.copy(alpha = 0.75f))
                val padPx    = HIGHLIGHT_PADDING.toPx()
                val cornerPx = HIGHLIGHT_CORNER.toPx()
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(
                        stepTarget.bounds.left - padPx,
                        stepTarget.bounds.top - padPx
                    ),
                    size = Size(
                        stepTarget.bounds.width + padPx * 2,
                        stepTarget.bounds.height + padPx * 2
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    blendMode = BlendMode.Clear
                )
            }

            // Tooltip + arrow
            val highlightTopDp    = with(density) { stepTarget.bounds.top.toDp() }
            val highlightBottomDp = with(density) { stepTarget.bounds.bottom.toDp() }
            val screenHeightDp    = config.screenHeightDp.dp
            val estimatedTooltipHeight = 200.dp
            val gapTotal = TOOLTIP_VERTICAL_GAP + ARROW_HEIGHT + HIGHLIGHT_PADDING

            val arrowOffsetX: Dp = (arrowXDp - ARROW_HALF_WIDTH - TOOLTIP_HORIZONTAL_PADDING)
                .coerceIn(0.dp, config.screenWidthDp.dp - TOOLTIP_HORIZONTAL_PADDING * 2 - ARROW_HALF_WIDTH * 2)

            if (showAbove) {
                val tooltipBottom = (highlightTopDp - gapTotal)
                    .coerceAtLeast(estimatedTooltipHeight + 24.dp)
                val tooltipTop = (tooltipBottom - estimatedTooltipHeight).coerceAtLeast(24.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = tooltipTop,
                            start = TOOLTIP_HORIZONTAL_PADDING,
                            end = TOOLTIP_HORIZONTAL_PADDING
                        )
                ) {
                    TooltipCard(state = state, step = step, onDismiss = onDismiss)
                    // Arrow pointing DOWN
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Canvas(
                            modifier = Modifier
                                .width(ARROW_HALF_WIDTH * 2)
                                .height(ARROW_HEIGHT)
                                .align(Alignment.TopStart)
                                .offset(x = arrowOffsetX)
                        ) {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, 0f)
                                lineTo(size.width / 2f, size.height)
                                close()
                            }
                            drawPath(path, Color.White)
                        }
                    }
                }
            } else {
                val tooltipTop = (highlightBottomDp + gapTotal)
                    .coerceAtMost(screenHeightDp - estimatedTooltipHeight - 24.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = tooltipTop,
                            start = TOOLTIP_HORIZONTAL_PADDING,
                            end = TOOLTIP_HORIZONTAL_PADDING
                        )
                ) {
                    // Arrow pointing UP
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Canvas(
                            modifier = Modifier
                                .width(ARROW_HALF_WIDTH * 2)
                                .height(ARROW_HEIGHT)
                                .align(Alignment.TopStart)
                                .offset(x = arrowOffsetX)
                        ) {
                            val path = Path().apply {
                                moveTo(0f, size.height)
                                lineTo(size.width, size.height)
                                lineTo(size.width / 2f, 0f)
                                close()
                            }
                            drawPath(path, Color.White)
                        }
                    }
                    TooltipCard(state = state, step = step, onDismiss = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun TooltipCard(
    state: CoachMarkState,
    step: Int,
    onDismiss: () -> Unit
) {
    val stepTarget = state.targets[step] ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(stepTarget.titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = SaatColors.Slate900,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${state.stepIndex()}/${state.totalSteps()}",
                style = MaterialTheme.typography.labelSmall,
                color = SaatColors.Slate500,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(stepTarget.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = SaatColors.Slate700
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { state.skip(); onDismiss() }
            ) {
                Text(stringResource(R.string.coach_mark_skip), color = SaatColors.Slate500)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (state.isLastStep()) { state.skip(); onDismiss() }
                    else state.next()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald)
            ) {
                Text(
                    if (state.isLastStep()) stringResource(R.string.coach_mark_finish)
                    else stringResource(R.string.coach_mark_next)
                )
            }
        }
    }
}
