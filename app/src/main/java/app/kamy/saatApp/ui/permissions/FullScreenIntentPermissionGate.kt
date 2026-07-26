package app.kamy.saatApp.ui.permissions

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
import app.kamy.saatApp.infrastructure.notifications.PrayerNotificationCoordinator

@Composable
fun FullScreenIntentPermissionGate() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var autoPromptedThisSession by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (context.canUseFullScreenIntent()) {
            return@LaunchedEffect
        }
        if (!autoPromptedThisSession) {
            autoPromptedThisSession = true
            showRationale = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (context.canUseFullScreenIntent()) {
                    showRationale = false
                    runCatching { PrayerNotificationCoordinator.rescheduleFromCache(context) }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringResource(R.string.fullscreen_intent_rationale_title)) },
            text = { Text(stringResource(R.string.fullscreen_intent_rationale)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    context.openFullScreenIntentSettings()
                }) {
                    Text(stringResource(R.string.action_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text(stringResource(R.string.action_dont_allow))
                }
            }
        )
    }
}
