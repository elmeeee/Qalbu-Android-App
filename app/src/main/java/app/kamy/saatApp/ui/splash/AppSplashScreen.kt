package app.kamy.saatApp.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPLASH_VISIBLE_MS = 1400L
private val SplashBackground = Color(0xFFF4F5F0)

@Composable
fun AppSplashScreen(onFinished: () -> Unit) {
    val opacity = remember { Animatable(0f) }
    val scale = remember { Animatable(0.5f) }
    val textYOffset = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch {
            opacity.animateTo(1f, animationSpec = tween(400))
        }
        launch {
            scale.animateTo(
                1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
        }
        launch {
            textYOffset.animateTo(0f, animationSpec = tween(500))
        }
        
        delay(SPLASH_VISIBLE_MS)
        
        opacity.animateTo(0f, animationSpec = tween(300))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .statusBarsPadding()
            .alpha(opacity.value),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.splash_icon_display),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .width(180.dp)
                    .height(180.dp)
                    .scale(scale.value),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(32.dp))
            Box(modifier = Modifier.offset(y = textYOffset.value.dp)) {
                Text(
                    text = "S  Ā  A  T",
                    modifier = Modifier.padding(horizontal = 32.dp),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp,
                        letterSpacing = 8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AlKhatibColors.DeepEmerald,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
