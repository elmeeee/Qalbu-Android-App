package app.kamy.saatApp.features.quran.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.design.theme.SaatColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * High-performance, realistic 3D Quran book page-turn pager.
 *
 * Implements a physical paper page turn with:
 * 1. 3D perspective and spine-hinged rotation along the left binding ($0^\circ \to -180^\circ$).
 * 2. Stationary revealed page underneath (neutralizing horizontal sliding).
 * 3. Front-face lifting & dynamic curl lighting ($0^\circ \to -90^\circ$).
 * 4. Back-face paper rendering with spine crease ($ -90^\circ \to -180^\circ$).
 * 5. Dynamic gutter cast shadow on the revealed page.
 * 6. Zero-allocation draw pass for 60/120 FPS buttery smooth performance.
 * 7. System gesture exclusion to prevent OS back-swipe interference.
 */
@Composable
fun PageCurlPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: @Composable (pageIndex: Int) -> Unit
) {
    // Pre-create reusable brushes once to avoid allocations on draw passes
    val spineShadowBrush = remember {
        Brush.horizontalGradient(
            listOf(Color.Black.copy(alpha = 0.22f), Color.Transparent)
        )
    }
    val gutterCastBrush = remember {
        Brush.horizontalGradient(
            listOf(
                Color.Black.copy(alpha = 0.32f),
                Color.Black.copy(alpha = 0.10f),
                Color.Transparent
            )
        )
    }
    val permanentSpineBrush = remember {
        Brush.horizontalGradient(
            listOf(Color.Black.copy(alpha = 0.05f), Color.Transparent)
        )
    }
    val backSpineBrush = remember {
        Brush.horizontalGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f))
        )
    }

    HorizontalPager(
        state = state,
        modifier = modifier
            .fillMaxSize()
            .systemGestureExclusion()
            .background(SaatColors.ScreenBackground),
        userScrollEnabled = userScrollEnabled,
        beyondViewportPageCount = 1,
        key = { it }
    ) { pageIndex ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SaatColors.ScreenBackground)
                .graphicsLayer {
                    val offset = (state.currentPage - pageIndex) + state.currentPageOffsetFraction

                    if (abs(offset) >= 1.05f) {
                        alpha = 0f
                        return@graphicsLayer
                    }

                    cameraDistance = 32f * density

                    when {
                        // Turning page (folding forward around the spine hinge at the left)
                        offset > 0f && offset < 1f -> {
                            translationX = offset * size.width
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            // Full physical 3D rotation: 0 deg to -180 deg
                            rotationY = -180f * offset
                            alpha = 1f
                        }
                        // Stationary page underneath being revealed
                        offset < 0f && offset > -1f -> {
                            // Cancel horizontal slide so the under-page stays stationary
                            translationX = offset * size.width
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            rotationY = 0f
                            alpha = 1f
                        }
                        // Resting page
                        else -> {
                            translationX = 0f
                            rotationY = 0f
                            alpha = 1f
                            transformOrigin = TransformOrigin(0f, 0.5f)
                        }
                    }
                }
                .drawWithContent {
                    val offset = (state.currentPage - pageIndex) + state.currentPageOffsetFraction
                    val w = size.width
                    val h = size.height

                    if (abs(offset) >= 1.05f) {
                        return@drawWithContent
                    }

                    when {
                        // Turning page (offset in 0f..1f)
                        offset > 0f && offset < 1f -> {
                            val p = offset
                            if (p <= 0.5f) {
                                // Front face of turning page (0 deg -> -90 deg)
                                drawContent()

                                val sinP = sin(p * PI).toFloat()

                                // 1. Spine Crease Shadow (near left hinge x = 0)
                                val spineWidth = w * 0.16f
                                drawRect(
                                    brush = spineShadowBrush,
                                    topLeft = Offset.Zero,
                                    size = Size(spineWidth, h),
                                    alpha = sinP
                                )

                                // 2. Ambient Tilt Darkening (as the paper tilts away from direct light)
                                val tiltAlpha = 0.18f * sinP
                                if (tiltAlpha > 0.005f) {
                                    drawRect(
                                        color = Color.Black.copy(alpha = tiltAlpha),
                                        size = size
                                    )
                                }

                                // 3. Tactile Right Edge Bevel (paper thickness)
                                val edgeAlpha = 0.16f * (1f - p * 2f)
                                if (edgeAlpha > 0.01f) {
                                    drawRect(
                                        color = Color.Black.copy(alpha = edgeAlpha),
                                        topLeft = Offset(w - 2.dp.toPx(), 0f),
                                        size = Size(2.dp.toPx(), h)
                                    )
                                }
                            } else {
                                // Back face of turning page (-90 deg -> -180 deg)
                                // Draw pristine paper background to simulate the physical back-side
                                val backP = (p - 0.5f) * 2f // 0 to 1

                                drawRect(
                                    color = SaatColors.ScreenBackground,
                                    size = size
                                )

                                // Spine crease shadow on the right side of the back-face
                                val backSpineWidth = w * 0.18f
                                val backSpineAlpha = 1f - backP
                                drawRect(
                                    brush = backSpineBrush,
                                    topLeft = Offset(w - backSpineWidth, 0f),
                                    size = Size(backSpineWidth, h),
                                    alpha = backSpineAlpha
                                )

                                // Ambient shadow as the back face flattens onto the left
                                val backAmbient = 0.15f * (1f - backP)
                                if (backAmbient > 0.005f) {
                                    drawRect(
                                        color = Color.Black.copy(alpha = backAmbient),
                                        size = size
                                    )
                                }

                                // Left edge subtle paper stroke
                                if (backP > 0.1f) {
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.12f * backP),
                                        topLeft = Offset.Zero,
                                        size = Size(2.dp.toPx(), h)
                                    )
                                }
                            }
                        }

                        // Stationary under-page (offset in -1f..0f)
                        offset < 0f && offset > -1f -> {
                            drawContent()

                            val revealProgress = 1f + offset // 0 = fully covered, 1 = fully open
                            val unrevealAmount = 1f - revealProgress // 1 = covered, 0 = open

                            // 1. Spine Gutter Cast Shadow (cast by the lifting page onto the under-page)
                            val castShadowWidth = w * (0.22f + 0.25f * unrevealAmount)
                            drawRect(
                                brush = gutterCastBrush,
                                topLeft = Offset.Zero,
                                size = Size(castShadowWidth, h),
                                alpha = unrevealAmount
                            )

                            // 2. Ambient contact shadow across under-page while partially covered
                            val ambientCoverAlpha = 0.12f * unrevealAmount
                            if (ambientCoverAlpha > 0.005f) {
                                drawRect(
                                    color = Color.Black.copy(alpha = ambientCoverAlpha),
                                    size = size
                                )
                            }
                        }

                        // Resting page
                        else -> {
                            drawContent()

                            // Subtle permanent spine depth on the left edge (bound book feel)
                            val permanentSpineWidth = 6.dp.toPx()
                            drawRect(
                                brush = permanentSpineBrush,
                                topLeft = Offset.Zero,
                                size = Size(permanentSpineWidth, h)
                            )
                        }
                    }
                }
        ) {
            content(pageIndex)
        }
    }
}
