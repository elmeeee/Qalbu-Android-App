package app.kamy.saatApp.ui.permissions

import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.kamy.saatApp.R
import app.kamy.saatApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.saatApp.infrastructure.notifications.PrayerNotificationCoordinator

/**
 * Android 12–12L (API 31–32) requires user consent for [SCHEDULE_EXACT_ALARM].
 * Android 13+ uses [USE_EXACT_ALARM], granted automatically at install like the Clock app.
 */
@Composable
fun ExactAlarmPermissionGate() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var autoPromptedThisSession by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    val needsExactAlarmPrompt = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun reschedule() {
        runCatching { DailyVerseNotificationScheduler.reschedule(context) }
        runCatching { PrayerNotificationCoordinator.rescheduleFromCache(context) }
    }

    LaunchedEffect(Unit) {
        reschedule()
        if (needsExactAlarmPrompt && !context.canScheduleExactAlarms()) {
            if (!autoPromptedThisSession) {
                autoPromptedThisSession = true
                showRationale = true
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringResource(R.string.exact_alarm_rationale_title)) },
            text = { Text(stringResource(R.string.exact_alarm_rationale)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    context.openExactAlarmSettings()
                }) {
                    Text(stringResource(R.string.action_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text(stringResource(R.string.action_later))
                }
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reschedule()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
