package app.kamy.saatApp.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors

enum class CoachMarkGesture {
    TAP,
    SWIPE_HORIZONTAL
}

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
    @StringRes val descriptionRes: Int,
    val gesture: CoachMarkGesture = CoachMarkGesture.TAP
)

@Composable
fun rememberCoachMarkState(): CoachMarkState {
    return remember { CoachMarkState() }
}

fun Modifier.coachMarkTarget(
    state: CoachMarkState,
    step: Int,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    gesture: CoachMarkGesture = CoachMarkGesture.TAP
): Modifier = this.onGloballyPositioned { coordinates ->
    if (coordinates.isAttached) {
        val bounds = coordinates.boundsInRoot()
        if (!bounds.isEmpty && bounds.width > 0 && bounds.height > 0) {
            state.targets[step] = CoachMarkTarget(bounds, titleRes, descriptionRes, gesture)
        }
    }
}

private val HIGHLIGHT_PADDING = 8.dp
private val HIGHLIGHT_CORNER = 16.dp

@Composable
fun CoachMarkOverlay(
    state: CoachMarkState,
    onDismiss: () -> Unit
) {
    if (!state.isVisible) return

    val target = state.targets[state.currentStep]
    if (target == null) {
        LaunchedEffect(state.currentStep) { state.next() }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coachMarkOverlay")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val currentTarget = state.targets[state.currentStep]
    val targetBounds = currentTarget?.bounds ?: Rect.Zero

    val animLeft by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetBounds.left,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "animLeft"
    )
    val animTop by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetBounds.top,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "animTop"
    )
    val animRight by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetBounds.right,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "animRight"
    )
    val animBottom by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetBounds.bottom,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "animBottom"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state.currentStep) {
                detectTapGestures {
                    if (state.isLastStep()) {
                        state.skip()
                        onDismiss()
                    } else {
                        state.next()
                    }
                }
            }
    ) {
        // Static dark scrim with smoothly animated punched-out highlight & pulsating ring (No blinking!)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
        ) {
            drawRect(Color.Black.copy(alpha = 0.82f))

            if (animRight > animLeft && animBottom > animTop) {
                val padPx = HIGHLIGHT_PADDING.toPx()
                val cornerPx = HIGHLIGHT_CORNER.toPx()
                val targetRect = Rect(
                    animLeft - padPx,
                    animTop - padPx,
                    animRight + padPx,
                    animBottom + padPx
                )

                // Punch out clear hole
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = targetRect.topLeft,
                    size = targetRect.size,
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    blendMode = BlendMode.Clear
                )

                // Themed animated glowing stroke ring around target
                drawRoundRect(
                    color = Color.White.copy(alpha = pulseAlpha * 0.75f),
                    topLeft = targetRect.topLeft,
                    size = targetRect.size,
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Top Header Bar: Step Pill & Skip Button (Always visible & smooth)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step Indicator Badge
            Box(
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${state.stepIndex()} / ${state.totalSteps()}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = Color.White
                )
            }

            // Skip Button
            TextButton(
                onClick = { state.skip(); onDismiss() },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.coach_mark_skip).uppercase(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Animated Gesture Visual & Text Container
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220)))
                    .togetherWith(fadeOut(tween(140)) + scaleOut(targetScale = 0.98f, animationSpec = tween(140)))
            },
            label = "coachMarkContent",
            modifier = Modifier.fillMaxSize()
        ) { step ->
            val stepTarget = state.targets[step] ?: return@AnimatedContent

            val density = LocalDensity.current
            val config = LocalConfiguration.current
            val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

            val targetCenterY = (stepTarget.bounds.top + stepTarget.bounds.bottom) / 2f
            val targetCenterXPx = (stepTarget.bounds.left + stepTarget.bounds.right) / 2f
            val showAbove = targetCenterY > screenHeightPx / 2f

            val padPx = with(density) { HIGHLIGHT_PADDING.toPx() }
            val highlightTopPx = stepTarget.bounds.top - padPx
            val highlightBottomPx = stepTarget.bounds.bottom + padPx

            Box(modifier = Modifier.fillMaxSize()) {
                if (stepTarget.gesture == CoachMarkGesture.SWIPE_HORIZONTAL) {
                    // Swipe Left/Right gesture overlay (<── 👆 ──>)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(
                                if (showAbove) Alignment.TopCenter else Alignment.BottomCenter
                            )
                            .padding(
                                top = if (showAbove) 90.dp else 24.dp,
                                bottom = if (showAbove) 24.dp else 80.dp,
                                start = 28.dp,
                                end = 28.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SwipeGestureVisual()

                        Text(
                            text = stringResource(stepTarget.titleRes).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(stepTarget.descriptionRes),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    // Tap gesture with pointer arrow
                    if (showAbove) {
                        // Target is in bottom area -> Text & Hand placed ABOVE target
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = with(density) { (highlightTopPx * 0.38f).toDp().coerceAtLeast(80.dp) },
                                    start = 28.dp,
                                    end = 28.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(stepTarget.titleRes).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(stepTarget.descriptionRes),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        TapGestureVisual()
                    }

                    // Connecting pointer line from hand down to target top edge
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val handBottomY = highlightTopPx * 0.74f
                        val arrowEndY = highlightTopPx - 6.dp.toPx()

                        if (arrowEndY > handBottomY) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.75f),
                                start = Offset(targetCenterXPx, handBottomY),
                                end = Offset(targetCenterXPx, arrowEndY),
                                strokeWidth = 1.8.dp.toPx()
                            )

                            val arrowSize = 7.dp.toPx()
                            val arrowPath = Path().apply {
                                moveTo(targetCenterXPx, arrowEndY)
                                lineTo(targetCenterXPx - arrowSize, arrowEndY - arrowSize * 1.3f)
                                lineTo(targetCenterXPx + arrowSize, arrowEndY - arrowSize * 1.3f)
                                close()
                            }
                            drawPath(arrowPath, Color.White)
                        }
                    }
                } else {
                    // Target is in top area -> Text & Hand placed BELOW target
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = with(density) { (highlightBottomPx + 85.dp.toPx()).toDp() },
                                start = 28.dp,
                                end = 28.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TapGestureVisual()

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = stringResource(stepTarget.titleRes).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(stepTarget.descriptionRes),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Connecting pointer line from hand up to target bottom edge
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val handTopY = highlightBottomPx + 74.dp.toPx()
                        val arrowEndY = highlightBottomPx + 6.dp.toPx()

                        if (handTopY > arrowEndY) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.75f),
                                start = Offset(targetCenterXPx, handTopY),
                                end = Offset(targetCenterXPx, arrowEndY),
                                strokeWidth = 1.8.dp.toPx()
                            )

                            val arrowSize = 7.dp.toPx()
                            val arrowPath = Path().apply {
                                moveTo(targetCenterXPx, arrowEndY)
                                lineTo(targetCenterXPx - arrowSize, arrowEndY + arrowSize * 1.3f)
                                lineTo(targetCenterXPx + arrowSize, arrowEndY + arrowSize * 1.3f)
                                close()
                            }
                            drawPath(arrowPath, Color.White)
                        }
                    }
                }
            }
        }
    }

    // Bottom Tap Anywhere to Continue Hint
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (state.isLastStep()) stringResource(R.string.coach_mark_finish) else "KETUK DI MANA SAJA UNTUK LANJUT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = Color.White.copy(alpha = pulseAlpha * 0.85f)
        )
    }
}
}

/**
 * Animated Tap gesture visual: Hand icon with expanding touch ripple wave.
 */
@Composable
private fun TapGestureVisual(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "tapAnimation")

    val rippleRadius by transition.animateFloat(
        initialValue = 4f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleRadius"
    )

    val rippleAlpha by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    val handScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handScale"
    )

    Box(
        modifier = modifier.size(68.dp),
        contentAlignment = Alignment.Center
    ) {
        // Concentric Ripple Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width * 0.48f, size.height * 0.28f)
            drawCircle(
                color = Color.White.copy(alpha = rippleAlpha),
                radius = rippleRadius.dp.toPx(),
                center = centerOffset,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Animated Hand Pointer Icon
        HandTouchIcon(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer {
                    scaleX = handScale
                    scaleY = handScale
                },
            tint = Color.White
        )
    }
}

/**
 * Animated Horizontal Swipe gesture visual: <── 👆 ──>
 */
@Composable
private fun SwipeGestureVisual(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "swipeAnimation")

    val swipeOffset by transition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeOffset"
    )

    Column(
        modifier = modifier.width(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Track line with Left & Right Arrowheads
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            val arrowSize = 6.dp.toPx()
            val strokeW = 1.8.dp.toPx()
            val y = size.height / 2f

            // Left Arrow
            val leftPath = Path().apply {
                moveTo(arrowSize * 1.4f, y - arrowSize)
                lineTo(0f, y)
                lineTo(arrowSize * 1.4f, y + arrowSize)
            }
            drawPath(leftPath, Color.White, style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Main horizontal line
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeW
            )

            // Right Arrow
            val rightPath = Path().apply {
                moveTo(size.width - arrowSize * 1.4f, y - arrowSize)
                lineTo(size.width, y)
                lineTo(size.width - arrowSize * 1.4f, y + arrowSize)
            }
            drawPath(rightPath, Color.White, style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        // Sliding Hand Pointer
        HandTouchIcon(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    translationX = swipeOffset.dp.toPx()
                },
            tint = Color.White
        )
    }
}

/**
 * Clean vector hand pointer touch icon.
 */
@Composable
private fun HandTouchIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.2.dp.toPx()

        val path = Path().apply {
            // Index finger pointing up
            moveTo(w * 0.44f, h * 0.42f)
            lineTo(w * 0.44f, h * 0.12f)
            cubicTo(w * 0.44f, h * 0.04f, w * 0.56f, h * 0.04f, w * 0.56f, h * 0.12f)
            lineTo(w * 0.56f, h * 0.44f)

            // Middle finger
            cubicTo(w * 0.56f, h * 0.36f, w * 0.67f, h * 0.36f, w * 0.67f, h * 0.44f)
            lineTo(w * 0.67f, h * 0.52f)

            // Ring finger
            cubicTo(w * 0.67f, h * 0.46f, w * 0.77f, h * 0.46f, w * 0.77f, h * 0.52f)
            lineTo(w * 0.77f, h * 0.60f)

            // Pinky finger
            cubicTo(w * 0.77f, h * 0.54f, w * 0.86f, h * 0.54f, w * 0.86f, h * 0.60f)
            lineTo(w * 0.86f, h * 0.74f)

            // Palm base
            cubicTo(w * 0.86f, h * 0.92f, w * 0.38f, h * 0.94f, w * 0.28f, h * 0.82f)

            // Thumb
            lineTo(w * 0.18f, h * 0.68f)
            cubicTo(w * 0.12f, h * 0.60f, w * 0.22f, h * 0.52f, w * 0.28f, h * 0.58f)
            lineTo(w * 0.38f, h * 0.68f)
            lineTo(w * 0.38f, h * 0.42f)
            close()
        }

        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
