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
 * Android 12–12L (API 31–32) requires user consent for [SCHEDULE_EXACT_ALARM].
 * Android 13+ uses [USE_EXACT_ALARM], granted automatically at install like the Clock app.
 */
@Composable
fun ExactAlarmPermissionGate() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var autoPromptedThisSession by remember { mutableStateOf(false) }
    val needsExactAlarmPrompt =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

    fun rescheduleIfAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !context.canScheduleExactAlarms()) {
            return
        }
        runCatching { DailyVerseNotificationScheduler.reschedule(context) }
        runCatching { PrayerNotificationCoordinator.rescheduleFromCache(context) }
    }

    LaunchedEffect(Unit) {
        if (context.canScheduleExactAlarms() || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            rescheduleIfAllowed()
            return@LaunchedEffect
        }
        if (needsExactAlarmPrompt && !autoPromptedThisSession) {
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
