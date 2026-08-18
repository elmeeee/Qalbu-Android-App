package app.kamy.saatApp.ui.permissions

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

fun Context.canScheduleExactAlarms(): Boolean {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return true
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    if (alarmManager.canScheduleExactAlarms()) return true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
    }
    return false
}

fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = "package:$packageName".toUri()
    }
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { startActivity(intent) }.onFailure {
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$packageName".toUri()
            if (this@openExactAlarmSettings !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        runCatching { startActivity(fallback) }
    }
}
