package app.kamy.qalbuApp.ui.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

fun View.performClickHaptic() {
    performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
}

fun Context.performSuccessHaptic() {
    val vibrator = vibratorOrNull() ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(20L)
    }
}

fun Context.performConfirmHaptic() {
    val vibrator = vibratorOrNull() ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(35L)
    }
}

private fun Context.vibratorOrNull(): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = getSystemService(VibratorManager::class.java)
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }?.takeIf { it.hasVibrator() }
}
