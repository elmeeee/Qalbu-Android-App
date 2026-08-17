package app.kamy.saatApp.features.tools.dhikr

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R

@Composable
fun TasbeehCounterWidget(
    count: Int,
    pulseKey: Int,
    modifier: Modifier = Modifier,
    counterWidth: Dp = 270.dp,
    counterHeight: Dp = 326.dp,
    onTap: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (pulseKey == 0) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 650f),
        label = "tasbeeh_tap"
    )

    Box(
        modifier = modifier
            .size(width = counterWidth, height = counterHeight)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_tasbeeh_counter),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        val baseScale = (counterWidth / 270.dp).coerceAtLeast(0.2f)
        val fontSize = (24 * baseScale).sp
        val letterSpacing = (1.2f * baseScale).sp
        val cornerRadius = (12 * baseScale).dp
        val paddingH = (6 * baseScale).dp
        val paddingV = (2 * baseScale).dp

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = counterHeight * 0.12f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(counterWidth * 0.68f)
                    .height(counterHeight * 0.20f)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color(0xFF1E1E1E))
                    .padding(horizontal = paddingH, vertical = paddingV),
                contentAlignment = Alignment.Center
            ) {
                val safeCount = count.coerceIn(0, 999999)
                val formatted = String.format("%06d", safeCount)
                val activeDigits = safeCount.toString()
                val inactiveCount = (6 - activeDigits.length).coerceAtLeast(0)
                val inactiveStr = formatted.take(inactiveCount)
                val activeStr = formatted.drop(inactiveCount)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (inactiveStr.isNotEmpty()) {
                        Text(
                            text = inactiveStr,
                            color = Color(0xFF3E3E3E),
                            fontSize = fontSize,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = letterSpacing,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = activeStr,
                        color = Color.White,
                        fontSize = fontSize,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = letterSpacing,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumTasbihCounter(
    count: Int,
    target: Int,
    pulseKey: Int,
    modifier: Modifier = Modifier,
    subtitle: String,
    counterSize: Dp = 88.dp
) {
    TasbeehCounterWidget(
        count = count,
        pulseKey = pulseKey,
        modifier = modifier,
        counterWidth = counterSize,
        counterHeight = counterSize * 1.2f,
        onTap = {}
    )
}
