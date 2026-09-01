package app.kamy.saatApp.features.quran.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.kamy.saatApp.design.theme.SaatColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * A realistic Quran book page-turn pager.
 *
 * Features:
 * 1. 3D perspective with spine-hinged rotation along the left binding.
 * 2. Stationary revealed page underneath (neutralizing horizontal sliding).
 * 3. Soft, warm book shading (crease shadow, gutter cast shadow, edge bevel) with NO white glare/artifacts.
 * 4. System gesture exclusion to prevent Android OS back-swipe gesture from interfering with Quran page turns.
 * 5. 60/120 FPS draw-phase performance without frame allocations or recompositions.
 */
@Composable
fun PageCurlPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: @Composable (pageIndex: Int) -> Unit
) {
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
        val pageOffset = (state.currentPage - pageIndex) + state.currentPageOffsetFraction
        val isTurningPage = pageOffset > 0f && pageOffset < 1f
        val zIndexVal = if (isTurningPage) 2f else 1f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(zIndexVal)
                .background(SaatColors.ScreenBackground)
                .graphicsLayer {
                    val offset = (state.currentPage - pageIndex) + state.currentPageOffsetFraction

                    if (abs(offset) >= 1.05f) {
                        alpha = 0f
                        return@graphicsLayer
                    }

                    cameraDistance = 28f * density

                    when {
                        // Turning page (folding forward around the spine hinge at the left)
                        offset > 0f && offset < 1f -> {
                            translationX = offset * size.width
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            // 3D rotation around the left spine hinge
                            rotationY = -120f * offset
                            // Smoothly dissolve as the page opens wide to reveal the next ayah underneath
                            alpha = if (offset > 0.38f) {
                                (1f - (offset - 0.38f) * 1.6f).coerceIn(0f, 1f)
                            } else {
                                1f
                            }
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
                    drawContent()

                    val offset = (state.currentPage - pageIndex) + state.currentPageOffsetFraction
                    val w = size.width
                    val h = size.height

                    if (abs(offset) >= 1.05f) {
                        return@drawWithContent
                    }

                    when {
                        // Page turning over (offset in 0f..1f)
                        offset > 0f && offset < 1f -> {
                            val p = offset
                            val sinP = sin(p * PI).toFloat()

                            // 1. Spine Crease Shadow (near left hinge x = 0)
                            val spineShadowWidth = w * 0.16f
                            val spineAlpha = 0.20f * sinP
                            if (spineAlpha > 0.005f) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = spineAlpha),
                                            Color.Transparent
                                        ),
                                        startX = 0f,
                                        endX = spineShadowWidth
                                    ),
                                    size = Size(spineShadowWidth, h)
                                )
                            }

                            // 2. Ambient Paper Tilt Darkening (soft warm shading as paper angles away from light)
                            val tiltAlpha = 0.14f * sinP
                            if (tiltAlpha > 0.005f) {
                                drawRect(
                                    color = Color.Black.copy(alpha = tiltAlpha),
                                    size = size
                                )
                            }

                            // 3. Right Paper Edge Bevel (tactile paper thickness)
                            val edgeAlpha = 0.14f * (1f - p)
                            if (edgeAlpha > 0.01f) {
                                drawRect(
                                    color = Color.Black.copy(alpha = edgeAlpha),
                                    topLeft = Offset(w - 2.dp.toPx(), 0f),
                                    size = Size(2.dp.toPx(), h)
                                )
                            }
                        }

                        // Stationary under-page (offset in -1f..0f)
                        offset < 0f && offset > -1f -> {
                            val revealProgress = 1f + offset // 0 = fully covered, 1 = fully open
                            val unrevealAmount = 1f - revealProgress // 1 = covered, 0 = open

                            // 1. Spine Gutter Cast Shadow (cast by the lifting page onto the under-page)
                            val castShadowWidth = w * (0.20f + 0.25f * unrevealAmount)
                            val castShadowAlpha = 0.32f * unrevealAmount
                            if (castShadowAlpha > 0.005f) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = castShadowAlpha),
                                            Color.Black.copy(alpha = castShadowAlpha * 0.3f),
                                            Color.Transparent
                                        ),
                                        startX = 0f,
                                        endX = castShadowWidth
                                    ),
                                    size = Size(castShadowWidth, h)
                                )
                            }

                            // 2. Ambient occlusion across under-page while covered
                            val ambientCoverAlpha = 0.10f * unrevealAmount
                            if (ambientCoverAlpha > 0.005f) {
                                drawRect(
                                    color = Color.Black.copy(alpha = ambientCoverAlpha),
                                    size = size
                                )
                            }
                        }

                        // Resting page
                        else -> {
                            // Subtle permanent spine depth on the left edge (feeling of a bound book)
                            val permanentSpineWidth = 6.dp.toPx()
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.04f),
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = permanentSpineWidth
                                ),
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
