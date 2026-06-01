package app.kamy.qalbuApp.ui.permissions

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.kamy.qalbuApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.qalbuApp.infrastructure.notifications.PrayerNotificationCoordinator

/**
 * Android 12+ requires user consent for exact alarms — apps cannot grant this silently.
 * We auto-open the one-tap system screen on first launch, then reschedule when allowed.
 */
@Composable
fun ExactAlarmPermissionGate() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var autoPromptedThisSession by remember { mutableStateOf(false) }

    fun rescheduleIfAllowed() {
        if (!context.canScheduleExactAlarms()) return
        runCatching { DailyVerseNotificationScheduler.reschedule(context) }
        runCatching { PrayerNotificationCoordinator.rescheduleFromCache(context) }
    }

    LaunchedEffect(Unit) {
        if (context.canScheduleExactAlarms()) {
            rescheduleIfAllowed()
            return@LaunchedEffect
        }
        if (!autoPromptedThisSession) {
            autoPromptedThisSession = true
            context.openExactAlarmSettings()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                rescheduleIfAllowed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
