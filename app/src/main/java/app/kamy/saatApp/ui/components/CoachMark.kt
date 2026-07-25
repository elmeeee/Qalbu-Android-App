package app.kamy.saatApp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import app.kamy.saatApp.design.theme.SaatColors

class CoachMarkState {
    var isVisible by mutableStateOf(false)
    var currentStep by mutableStateOf(0)
    val targets = mutableStateMapOf<Int, CoachMarkTarget>()

    fun show() {
        if (targets.isNotEmpty()) {
            currentStep = 0
            isVisible = true
        }
    }

    fun next() {
        if (currentStep < targets.size - 1) {
            currentStep++
        } else {
            isVisible = false
        }
    }

    fun skip() {
        isVisible = false
    }
}

data class CoachMarkTarget(
    val bounds: Rect,
    val title: String,
    val description: String
)

@Composable
fun rememberCoachMarkState(): CoachMarkState {
    return remember { CoachMarkState() }
}

fun Modifier.coachMarkTarget(
    state: CoachMarkState,
    step: Int,
    title: String,
    description: String
): Modifier = this.onGloballyPositioned { coordinates ->
    val bounds = coordinates.boundsInWindow()
    if (bounds.width > 0 && bounds.height > 0) {
        state.targets[step] = CoachMarkTarget(bounds, title, description)
    }
}

@Composable
fun CoachMarkOverlay(
    state: CoachMarkState,
    onDismiss: () -> Unit
) {
    if (!state.isVisible) return

    val target = state.targets[state.currentStep]
    if (target == null) {
        state.isVisible = false
        return
    }

    Popup(
        properties = PopupProperties(
            focusable = true,
            excludeFromSystemGesture = true
        ),
        onDismissRequest = { /* Prevent dismiss on outside tap */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { /* Block touches to underlying UI */ }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.99f)
            ) {
                drawRect(Color.Black.copy(alpha = 0.8f))

                val padding = 12.dp.toPx()
                val corner = 16.dp.toPx()
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(target.bounds.left - padding, target.bounds.top - padding),
                    size = Size(target.bounds.width + padding * 2, target.bounds.height + padding * 2),
                    cornerRadius = CornerRadius(corner, corner),
                    blendMode = BlendMode.Clear
                )
            }

            val density = LocalDensity.current
            val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
            val topDp = with(density) { target.bounds.top.toDp() }
            val bottomDp = with(density) { target.bounds.bottom.toDp() }

            val showAbove = topDp > (screenHeightDp / 2)
            val yPadding = if (showAbove) {
                (topDp - 220.dp).coerceAtLeast(40.dp)
            } else {
                (bottomDp + 24.dp).coerceAtMost(screenHeightDp - 220.dp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = yPadding)
                    .padding(horizontal = 24.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = target.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = SaatColors.Slate900,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = target.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaatColors.Slate700
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            state.skip()
                            onDismiss()
                        }
                    ) {
                        Text("Skip", color = SaatColors.Slate500)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (state.currentStep == state.targets.size - 1) {
                                state.skip()
                                onDismiss()
                            } else {
                                state.next()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaatColors.DeepEmerald
                        )
                    ) {
                        Text(if (state.currentStep == state.targets.size - 1) "Selesai" else "Lanjut")
                    }
                }
            }
        }
    }
}
