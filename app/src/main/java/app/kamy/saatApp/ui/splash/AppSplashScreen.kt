package app.kamy.saatApp.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_VISIBLE_MS = 1400L
private val SplashBgCenter = Color(0xFFFFFFFF)
private val SplashBgEdge = Color(0xFFF4F5F0)

@Composable
fun AppSplashScreen(onFinished: () -> Unit) {
    val opacity = remember { Animatable(0f) }
    val scale = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        launch {
            opacity.animateTo(1f, animationSpec = tween(600))
        }
        launch {
            scale.animateTo(
                1.0f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.6f,
                    stiffness = 250f
                )
            )
        }
        
        delay(SPLASH_VISIBLE_MS)
        
        opacity.animateTo(0f, animationSpec = tween(400))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(SplashBgCenter, SplashBgEdge),
                    radius = 800f
                )
            )
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.splash_icon_display),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .scale(scale.value)
                .alpha(opacity.value),
            contentScale = ContentScale.Fit
        )
    }
}
