package app.kamy.saatApp.ui.permissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri
import java.util.Locale

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
    return pm.isIgnoringBatteryOptimizations(packageName)
}

fun hasAggressiveOemBatteryManagement(): Boolean {
    // Disabled (returns false) because elderly users get confused by complex OEM menus.
    // We will only rely on the simple Android system popup.
    return false
}

fun Context.requestIgnoreBatteryOptimizations(): Boolean {
    if (isIgnoringBatteryOptimizations()) return true
    val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = "package:$packageName".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (startActivitySafely(direct)) return true
    return openBatteryOptimizationList()
}

fun Context.openManufacturerBatterySettings(): Boolean {
    val pkg = packageName
    val candidates = buildList {
        val m = Build.MANUFACTURER.lowercase(Locale.US)
        when {
            "samsung" in m -> {
                add(
                    componentIntent(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                    )
                )
                add(
                    componentIntent(
                        "com.samsung.android.sm",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                    )
                )
            }
            "xiaomi" in m || "redmi" in m || "poco" in m -> {
                add(
                    Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                        putExtra("extra_pkgname", pkg)
                    }
                )
                add(
                    componentIntent(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                )
                add(
                    componentIntent(
                        "com.miui.securitycenter",
                        "com.miui.powercenter.PowerSettings"
                    )
                )
            }
            "oppo" in m || "realme" in m -> {
                add(
                    componentIntent(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                )
                add(
                    componentIntent(
                        "com.oppo.safe",
                        "com.oppo.safe.permission.startup.StartupAppListActivity"
                    )
                )
            }
            "vivo" in m -> {
                add(
                    componentIntent(
                        "com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                    )
                )
                add(
                    componentIntent(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                )
            }
            "huawei" in m || "honor" in m -> {
                add(
                    componentIntent(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                )
            }
            "oneplus" in m -> {
                add(
                    componentIntent(
                        "com.oneplus.security",
                        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                    )
                )
            }
        }
        add(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", pkg, null)
            }
        )
    }
    return candidates.any { startActivitySafely(it) }
}

fun Context.openBackgroundReliabilitySettings() {
    if (isIgnoringBatteryOptimizations()) {
        if (hasAggressiveOemBatteryManagement()) {
            openManufacturerBatterySettings()
        }
        return
    }
    if (!requestIgnoreBatteryOptimizations() && hasAggressiveOemBatteryManagement()) {
        openManufacturerBatterySettings()
    }
}

private fun Context.openBatteryOptimizationList(): Boolean {
    val list = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (startActivitySafely(list)) return true
    val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:$packageName".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return startActivitySafely(details)
}

private fun componentIntent(pkg: String, cls: String): Intent =
    Intent().setComponent(ComponentName(pkg, cls)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

private fun Context.startActivitySafely(intent: Intent): Boolean =
    runCatching {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val canHandle = intent.resolveActivity(packageManager) != null
        if (canHandle) startActivity(intent)
        canHandle
    }.getOrDefault(false)
