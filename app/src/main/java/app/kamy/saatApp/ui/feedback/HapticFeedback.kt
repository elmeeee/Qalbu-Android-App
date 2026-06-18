package app.kamy.saatApp.ui.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/** Returns a crash-safe haptic callback for light taps (prayer check, toggles). */
@Composable
fun rememberTapHaptic(): () -> Unit {
    val view = LocalView.current
    return remember(view) { { view.performTapHaptic() } }
}

/** Returns a crash-safe haptic callback for confirmation (qibla aligned). */
@Composable
fun rememberConfirmHaptic(): () -> Unit {
    val view = LocalView.current
    return remember(view) { { view.performConfirmHaptic() } }
}

private fun View.performTapHaptic() {
    runCatching {
        performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}

private fun View.performConfirmHaptic() {
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}
