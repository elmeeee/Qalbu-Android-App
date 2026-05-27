package app.kamy.qalbuApp.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.ui.components.FloatingAudioBarMetrics

/** Matches [app.kamy.qalbuApp.ui.components.FloatingTabBar] so lists scroll behind the pill. */
object FloatingNavBarMetrics {
    val barHeight = 72.dp
    val outerVerticalPadding = 8.dp
    val extraScrollGap = 12.dp

    val contentClearance: Dp
        @Composable get() = barHeight + outerVerticalPadding * 2 + extraScrollGap
}

@Composable
fun floatingNavBottomPadding(): Dp =
    FloatingNavBarMetrics.contentClearance +
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/** Extra bottom inset when the global mini audio player is visible. */
@Composable
fun floatingAudioBarExtraPadding(audioBarVisible: Boolean): Dp =
    if (audioBarVisible) {
        FloatingAudioBarMetrics.barHeight + FloatingAudioBarMetrics.bottomGap
    } else {
        0.dp
    }

@Composable
fun floatingNavAndAudioBottomPadding(audioBarVisible: Boolean): Dp =
    floatingNavBottomPadding() + floatingAudioBarExtraPadding(audioBarVisible)

@Composable
fun floatingNavListPadding(
    extraTop: Dp = 0.dp,
    extraBottom: Dp = 0.dp
): PaddingValues {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return PaddingValues(
        top = statusTop + extraTop,
        bottom = floatingNavBottomPadding() + extraBottom
    )
}

@Composable
fun Modifier.tabContentBottomInset(): Modifier =
    padding(bottom = floatingNavBottomPadding())

@Composable
fun Modifier.tabContentStatusBarInset(): Modifier =
    padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
