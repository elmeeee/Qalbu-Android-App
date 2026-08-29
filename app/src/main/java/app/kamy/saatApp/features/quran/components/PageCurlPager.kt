package app.kamy.saatApp.features.quran.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import app.kamy.saatApp.design.theme.SaatColors
import kotlin.math.abs

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
                    val pageOffset = (state.currentPage - pageIndex) + state.currentPageOffsetFraction

                    if (abs(pageOffset) >= 1f) {
                        alpha = 0f
                        return@graphicsLayer
                    }

                    cameraDistance = 22f * density

                    if (pageOffset > 0f && pageOffset < 1f) {
                        // Current page flipping forward: pivots along the left spine hinge
                        translationX = pageOffset * size.width
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        rotationY = -120f * pageOffset
                        // Cleanly fade out as the page turns open
                        alpha = (1f - pageOffset * 1.35f).coerceIn(0f, 1f)
                    } else if (pageOffset < 0f && pageOffset > -1f) {
                        // Next page underneath: stationary, waiting to be revealed cleanly
                        translationX = pageOffset * size.width
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        rotationY = 0f
                        alpha = 1f
                    } else {
                        // Resting page
                        translationX = 0f
                        rotationY = 0f
                        alpha = 1f
                    }
                }
        ) {
            content(pageIndex)
        }
    }
}
