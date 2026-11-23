package app.kamy.saatApp.features.quran.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun PageCurlPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    excludeSystemGestures: Boolean = false,
    instantSnapWhenAnimationsDisabled: Boolean = false,
    content: @Composable (pageIndex: Int) -> Unit
) {
    val contentLayoutDirection = LocalLayoutDirection.current
    val viewConfiguration = LocalViewConfiguration.current
    var settledPage by remember(state) { mutableIntStateOf(state.currentPage) }
    var turningPage by remember(state) { mutableIntStateOf(state.currentPage) }
    var turnDirection by remember(state) { mutableIntStateOf(0) }
    var isPointerDown by remember(state) { mutableStateOf(false) }
    var isUserDragging by remember(state) { mutableStateOf(false) }
    var gestureStartX by remember(state) { mutableFloatStateOf(0f) }
    var gestureX by remember(state) { mutableFloatStateOf(0f) }
    var gestureStartY by remember(state) { mutableFloatStateOf(0f) }
    var gestureY by remember(state) { mutableFloatStateOf(0f) }
    var isCornerDrag by remember(state) { mutableStateOf(false) }
    var isTopCornerDrag by remember(state) { mutableStateOf(false) }

    LaunchedEffect(state) {
        launch {
            state.interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is DragInteraction.Start -> isUserDragging = true
                    is DragInteraction.Stop, is DragInteraction.Cancel -> isUserDragging = false
                }
            }
        }

        snapshotFlow {
            Triple(state.isScrollInProgress, state.currentPage, state.targetPage)
        }.collect { (isScrolling, currentPage, targetPage) ->
            if (!isScrolling) {
                settledPage = currentPage
                turningPage = currentPage
                turnDirection = 0
                isCornerDrag = false
            } else if (turnDirection == 0 && targetPage != settledPage) {
                val distance = targetPage - settledPage
                if (abs(distance) == 1) {
                    turningPage = settledPage
                    turnDirection = distance
                }
            }
        }
    }

    val flingBehavior = if (
        instantSnapWhenAnimationsDisabled && !ValueAnimator.areAnimatorsEnabled()
    ) {
        PagerDefaults.flingBehavior(
            state = state,
            snapAnimationSpec = tween(durationMillis = 0)
        )
    } else {
        PagerDefaults.flingBehavior(state = state)
    }
    val pagerModifier = modifier
        .fillMaxSize()
        .background(Color.Transparent)
        .then(if (excludeSystemGestures) Modifier.systemGestureExclusion() else Modifier)

    Box(
        modifier = pagerModifier.pointerInput(state, userScrollEnabled, viewConfiguration) {
            awaitEachGesture {
                val down = awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial
                )
                isPointerDown = true
                val startX = down.position.x
                val startY = down.position.y
                val screenWidth = size.width.toFloat()
                val screenHeight = size.height.toFloat()
                val systemEdgeWidth = 24.dp.toPx()
                val isSystemBackEdge = (
                    (startX < systemEdgeWidth || startX > screenWidth - systemEdgeWidth) &&
                        startY > screenHeight * 0.08f &&
                        startY < screenHeight * 0.92f
                    )
                val wasSettlingAtDown = state.isScrollInProgress && !isUserDragging
                var recognized = false

                do {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                    val deltaX = pointer.position.x - startX
                    val deltaY = pointer.position.y - startY

                    if (
                        wasSettlingAtDown &&
                        abs(deltaX) > viewConfiguration.touchSlop &&
                        abs(deltaX) > abs(deltaY) * 1.1f
                    ) {
                        pointer.consume()
                    }

                    if (
                        !recognized &&
                        userScrollEnabled &&
                        !state.isScrollInProgress &&
                        !isSystemBackEdge &&
                        abs(deltaX) > viewConfiguration.touchSlop &&
                        abs(deltaX) > abs(deltaY) * 1.1f
                    ) {
                        val direction = if (deltaX < 0f) 1 else -1
                        val sourcePage = settledPage
                        val targetPage = sourcePage + direction
                        if (targetPage in 0 until state.pageCount) {
                            turningPage = sourcePage
                            turnDirection = direction
                            gestureStartX = startX
                            gestureX = pointer.position.x
                            gestureStartY = startY
                            gestureY = pointer.position.y
                            isCornerDrag = if (direction > 0) {
                                startX >= screenWidth * 0.72f &&
                                    (startY <= screenHeight * 0.2f || startY >= screenHeight * 0.8f)
                            } else {
                                startX <= screenWidth * 0.28f &&
                                    (startY <= screenHeight * 0.2f || startY >= screenHeight * 0.8f)
                            }
                            isTopCornerDrag = startY < screenHeight * 0.5f
                            recognized = true
                        }
                    } else if (recognized) {
                        gestureX = pointer.position.x
                        gestureY = pointer.position.y
                    }
                } while (pointer.pressed)

                isPointerDown = false
            }
        }
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            HorizontalPager(
                state = state,
                modifier = Modifier.fillMaxSize(),
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled,
                reverseLayout = false,
                beyondViewportPageCount = 1,
                key = { it }
            ) { pageIndex ->
                val isCurling = turnDirection != 0 &&
                    (isPointerDown || isUserDragging || state.isScrollInProgress)
                val isTurningPage = isCurling && pageIndex == turningPage
                val isAdjacentPage = isCurling && pageIndex == turningPage + turnDirection
                val isActivePage = if (turnDirection == 0) {
                    pageIndex == state.currentPage
                } else {
                    !isCurling || isTurningPage || isAdjacentPage
                }
                val paperBack = MaterialTheme.colorScheme.surface

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(
                            when {
                                isTurningPage -> 2f
                                isAdjacentPage -> 1f
                                pageIndex == state.currentPage -> 2f
                                else -> 0f
                            }
                        )
                        .graphicsLayer {
                            translationX = if (isCurling) {
                                val pageOffset = (pageIndex - state.currentPage) -
                                    state.currentPageOffsetFraction
                                -pageOffset * size.width
                            } else {
                                0f
                            }
                            alpha = if (isActivePage) 1f else 0f
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                        .semantics {
                            if (isCurling && (!isActivePage || pageIndex != turningPage)) {
                                hideFromAccessibility()
                            }
                        }
                        .drawWithContent {
                            if (!isCurling || !state.isScrollInProgress) {
                                drawContent()
                                return@drawWithContent
                            }

                            val pageWidth = size.width
                            val pageHeight = size.height
                            if (pageWidth <= 0f || pageHeight <= 0f) {
                                drawContent()
                                return@drawWithContent
                            }

                            val sourceOffset = (turningPage - state.currentPage) -
                                state.currentPageOffsetFraction
                            val progress = abs(sourceOffset).coerceIn(0f, 1f)
                            if (progress <= 0.001f) {
                                drawContent()
                                return@drawWithContent
                            }

                            val forward = turnDirection > 0
                            val direction = if (forward) 1f else -1f
                            val edgeX = if (forward) {
                                pageWidth * (1f - progress)
                            } else {
                                pageWidth * progress
                            }
                            val dragProgress = sin(progress * PI).toFloat()
                            val originBias = (
                                (gestureStartY / pageHeight) - 0.5f
                                ) * pageWidth * 0.04f * dragProgress
                            val verticalPull = (
                                (gestureY - gestureStartY) * 0.2f * dragProgress
                                ).coerceIn(-pageWidth * 0.05f, pageWidth * 0.05f)
                            val curveAmount = originBias + verticalPull
                            var topX = edgeX - direction * curveAmount
                            var bottomX = edgeX + direction * curveAmount

                            if (isCornerDrag) {
                                val cornerX = gestureX.coerceIn(0f, pageWidth)
                                if (isTopCornerDrag) {
                                    topX = cornerX
                                } else {
                                    bottomX = cornerX
                                }
                            }

                            topX = topX.coerceIn(0f, pageWidth)
                            bottomX = bottomX.coerceIn(0f, pageWidth)
                            val curlRadius = 8.dp.toPx() + pageWidth * 0.035f * dragProgress
                            val foldLine = Path().apply {
                                moveTo(topX, 0f)
                                cubicTo(
                                    topX - direction * curlRadius,
                                    pageHeight * 0.34f,
                                    bottomX + direction * curlRadius,
                                    pageHeight * 0.66f,
                                    bottomX,
                                    pageHeight
                                )
                            }
                            val frontPath = Path().apply {
                                if (forward) {
                                    moveTo(0f, 0f)
                                    lineTo(topX, 0f)
                                    cubicTo(
                                        topX - direction * curlRadius,
                                        pageHeight * 0.34f,
                                        bottomX + direction * curlRadius,
                                        pageHeight * 0.66f,
                                        bottomX,
                                        pageHeight
                                    )
                                    lineTo(0f, pageHeight)
                                } else {
                                    moveTo(topX, 0f)
                                    lineTo(pageWidth, 0f)
                                    lineTo(pageWidth, pageHeight)
                                    lineTo(bottomX, pageHeight)
                                    cubicTo(
                                        bottomX + direction * curlRadius,
                                        pageHeight * 0.66f,
                                        topX - direction * curlRadius,
                                        pageHeight * 0.34f,
                                        topX,
                                        0f
                                    )
                                }
                                close()
                            }
                            val bandWidth = (6.dp.toPx() + curlRadius * 0.45f)
                                .coerceAtMost(pageWidth * 0.12f)
                            val undersidePath = Path().apply {
                                val side = direction
                                moveTo(topX, 0f)
                                lineTo((topX + side * bandWidth).coerceIn(0f, pageWidth), 0f)
                                cubicTo(
                                    topX + side * (bandWidth + curlRadius),
                                    pageHeight * 0.34f,
                                    bottomX + side * (bandWidth - curlRadius),
                                    pageHeight * 0.66f,
                                    (bottomX + side * bandWidth).coerceIn(0f, pageWidth),
                                    pageHeight
                                )
                                lineTo(bottomX, pageHeight)
                                cubicTo(
                                    bottomX + side * curlRadius,
                                    pageHeight * 0.66f,
                                    topX - side * curlRadius,
                                    pageHeight * 0.34f,
                                    topX,
                                    0f
                                )
                                close()
                            }
                            when {
                                isTurningPage -> {
                                    clipPath(frontPath) {
                                        this@drawWithContent.drawContent()
                                    }
                                    drawPath(
                                        path = undersidePath,
                                        brush = Brush.horizontalGradient(
                                            colors = if (forward) {
                                                listOf(
                                                    Color.Black.copy(alpha = 0.16f),
                                                    paperBack,
                                                    Color.White.copy(alpha = 0.28f)
                                                )
                                            } else {
                                                listOf(
                                                    Color.White.copy(alpha = 0.28f),
                                                    paperBack,
                                                    Color.Black.copy(alpha = 0.16f)
                                                )
                                            },
                                            startX = (edgeX - bandWidth).coerceAtLeast(0f),
                                            endX = (edgeX + bandWidth).coerceAtMost(pageWidth)
                                        )
                                    )
                                    drawPath(
                                        path = foldLine,
                                        color = Color.Black.copy(alpha = 0.12f * dragProgress),
                                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawPath(
                                        path = foldLine,
                                        color = Color.White.copy(alpha = 0.48f * dragProgress),
                                        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                isAdjacentPage -> {
                                    drawContent()
                                    drawPath(
                                        path = foldLine,
                                        brush = Brush.horizontalGradient(
                                            colors = if (forward) {
                                                listOf(
                                                    Color.Black.copy(alpha = 0.14f * dragProgress),
                                                    Color.Black.copy(alpha = 0.06f * dragProgress),
                                                    Color.Transparent
                                                )
                                            } else {
                                                listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.06f * dragProgress),
                                                    Color.Black.copy(alpha = 0.14f * dragProgress)
                                                )
                                            },
                                            startX = (edgeX - curlRadius).coerceAtLeast(0f),
                                            endX = (edgeX + curlRadius).coerceAtMost(pageWidth)
                                        ),
                                        style = Stroke(
                                            width = bandWidth * 1.4f,
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }

                                else -> drawContent()
                            }
                        }
                ) {
                    CompositionLocalProvider(
                        LocalLayoutDirection provides contentLayoutDirection
                    ) {
                        content(pageIndex)
                    }
                }
            }
        }

    }
}
