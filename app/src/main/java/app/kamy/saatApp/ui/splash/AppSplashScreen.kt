package app.kamy.saatApp.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import kotlinx.coroutines.delay

private const val SPLASH_VISIBLE_MS = 2_400L
private const val FADE_IN_MS = 900
private const val FADE_OUT_MS = 450

@Composable
fun AppSplashScreen(onFinished: () -> Unit) {
    val opacity = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        opacity.animateTo(1f, animationSpec = tween(FADE_IN_MS))
        delay(SPLASH_VISIBLE_MS)
        opacity.animateTo(0f, animationSpec = tween(FADE_OUT_MS))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AlKhatibColors.OffWhite,
                        AlKhatibColors.SageMist,
                        AlKhatibColors.MintWash.copy(alpha = 0.6f)
                    )
                )
            )
            .statusBarsPadding()
            .alpha(opacity.value),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.splash_greeting_arabic),
            modifier = Modifier.padding(horizontal = 32.dp),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 28.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Medium
            ),
            color = AlKhatibColors.DeepEmerald,
            textAlign = TextAlign.Center
        )
    }
}
