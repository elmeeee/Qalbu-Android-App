package app.kamy.qalbuApp.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.theme.AlKhatibColors

@Composable
fun AlKhatibSkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color? = null
) {
    val base = color ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(base.copy(alpha = alpha))
    )
}

@Composable
fun AlKhatibSkeletonLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 14.dp
) {
    AlKhatibSkeletonBlock(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height),
        shape = RoundedCornerShape(6.dp)
    )
}

@Composable
fun AlKhatibSkeletonCircle(size: Dp) {
    AlKhatibSkeletonBlock(
        modifier = Modifier.size(size),
        shape = CircleShape
    )
}

@Composable
fun AlKhatibSkeletonOnDark(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    val transition = rememberInfiniteTransition(label = "skeletonDark")
    val alpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonDarkAlpha"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = alpha))
    )
}

@Composable
fun TodayVerseCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlKhatibSkeletonLine(height = 28.dp, widthFraction = 0.92f)
        Spacer(Modifier.height(12.dp))
        AlKhatibSkeletonLine(height = 28.dp, widthFraction = 0.78f)
        Spacer(Modifier.height(12.dp))
        AlKhatibSkeletonLine(height = 28.dp, widthFraction = 0.65f)
        Spacer(Modifier.height(20.dp))
        AlKhatibSkeletonLine(height = 16.dp, widthFraction = 0.88f)
        Spacer(Modifier.height(8.dp))
        AlKhatibSkeletonLine(height = 16.dp, widthFraction = 0.7f)
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(2) {
                AlKhatibSkeletonBlock(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(2) {
                AlKhatibSkeletonBlock(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}

@Composable
fun ChapterRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlKhatibSkeletonBlock(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            AlKhatibSkeletonLine(widthFraction = 0.55f, height = 16.dp)
            Spacer(Modifier.height(8.dp))
            AlKhatibSkeletonLine(widthFraction = 0.35f, height = 12.dp)
        }
    }
}

@Composable
fun ReflectPostSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AlKhatibColors.ForestDark.copy(alpha = 0.6f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlKhatibSkeletonOnDark(modifier = Modifier.size(36.dp), shape = CircleShape)
            Spacer(Modifier.width(10.dp))
            Column {
                AlKhatibSkeletonOnDark(
                    modifier = Modifier
                        .width(100.dp)
                        .height(12.dp)
                )
                Spacer(Modifier.height(6.dp))
                AlKhatibSkeletonOnDark(
                    modifier = Modifier
                        .width(60.dp)
                        .height(10.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        AlKhatibSkeletonOnDark(modifier = Modifier.fillMaxWidth().height(14.dp))
        Spacer(Modifier.height(8.dp))
        AlKhatibSkeletonOnDark(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(14.dp)
        )
    }
}
