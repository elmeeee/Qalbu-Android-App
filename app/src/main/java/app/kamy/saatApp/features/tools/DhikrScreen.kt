package app.kamy.saatApp.features.tools

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.infrastructure.preferences.DhikrPreset
import app.kamy.saatApp.infrastructure.preferences.DhikrStore
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DhikrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    val preset = DhikrStore.presets[selectedIndex.coerceIn(DhikrStore.presets.indices)]
    var count by remember(preset.id) { mutableIntStateOf(DhikrStore.sessionCount(context, preset.id)) }
    var pulseKey by remember { mutableIntStateOf(0) }
    val scale by animateFloatAsState(
        targetValue = if (pulseKey == 0) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        finishedListener = { pulseKey = 0 },
        label = "tasbih_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(AlKhatibColors.ScreenBackground, AlKhatibColors.SageMist)
                )
            )
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = stringResource(R.string.dhikr_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DhikrStore.presets.forEachIndexed { index, item ->
                Text(
                    text = dhikrLabel(context, item),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Medium,
                    color = if (index == selectedIndex) AlKhatibColors.DeepEmerald else AlKhatibColors.Slate500,
                    modifier = Modifier
                        .clickable {
                            selectedIndex = index
                            count = DhikrStore.sessionCount(context, item.id)
                        }
                        .padding(vertical = 8.dp)
                )
            }
        }

        Text(
            text = preset.arabic,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp, lineHeight = 40.sp),
            color = AlKhatibColors.DeepEmerald,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    count = DhikrStore.increment(context, preset.id)
                    pulseKey++
                },
            contentAlignment = Alignment.Center
        ) {
            TasbihRosary(
                count = count,
                target = preset.target,
                modifier = Modifier.fillMaxWidth()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.Slate900
                )
                Text(
                    text = stringResource(R.string.dhikr_of_target, preset.target),
                    style = MaterialTheme.typography.labelMedium,
                    color = AlKhatibColors.Slate500
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = {
                DhikrStore.resetSession(context, preset.id)
                count = 0
            }) {
                Text(stringResource(R.string.dhikr_reset))
            }
            Text(
                text = stringResource(R.string.dhikr_total, DhikrStore.totalCount(context, preset.id)),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500,
                modifier = Modifier.padding(top = 14.dp, start = 12.dp)
            )
        }
    }
}

@Composable
private fun TasbihRosary(
    count: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    val beadCount = target.coerceIn(11, 99)
    val activeIndex = (count - 1).coerceAtLeast(-1) % beadCount
    val density = LocalDensity.current

    Canvas(modifier = modifier.height(240.dp)) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.82f
        val radius = size.width * 0.38f
        val beadRadius = with(density) { 7.dp.toPx() }

        for (i in 0 until beadCount) {
            val angle = Math.PI + (Math.PI * i / (beadCount - 1).coerceAtLeast(1))
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()
            val filled = i <= activeIndex
            drawCircle(
                color = if (filled) Color(0xFFD97706) else Color(0xFFCBD5E1),
                radius = if (i == activeIndex) beadRadius * 1.35f else beadRadius,
                center = Offset(x, y)
            )
            if (i == activeIndex) {
                drawCircle(
                    color = Color(0x55D97706),
                    radius = beadRadius * 2.2f,
                    center = Offset(x, y)
                )
            }
        }
        drawCircle(
            color = Color(0xFF064E3B),
            radius = beadRadius * 1.6f,
            center = Offset(centerX, centerY + beadRadius)
        )
    }
}

private fun dhikrLabel(context: android.content.Context, preset: DhikrPreset): String =
    when (preset.labelResKey) {
        "dhikr_subhanallah" -> context.getString(R.string.dhikr_subhanallah)
        "dhikr_alhamdulillah" -> context.getString(R.string.dhikr_alhamdulillah)
        "dhikr_allahuakbar" -> context.getString(R.string.dhikr_allahuakbar)
        "dhikr_istighfar" -> context.getString(R.string.dhikr_istighfar)
        "dhikr_salawat" -> context.getString(R.string.dhikr_salawat)
        else -> preset.id
    }
