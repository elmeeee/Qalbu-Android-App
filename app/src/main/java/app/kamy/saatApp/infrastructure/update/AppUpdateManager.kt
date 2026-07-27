package app.kamy.saatApp.infrastructure.update

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import app.kamy.saatApp.BuildConfig

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean,
    val isForceUpdate: Boolean,
    val latestVersionName: String,
    val minRequiredVersionCode: Int
)

object AppUpdateManager {
    private const val PREFS_NAME = "saat_app_update_prefs"
    private const val KEY_MIN_VERSION_CODE = "min_required_version_code"
    private const val KEY_LATEST_VERSION_NAME = "latest_version_name"
    private const val KEY_IS_FORCE_UPDATE = "is_force_update"
    private const val KEY_SIMULATE_UPDATE = "simulate_update_available"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun checkUpdate(context: Context): AppUpdateInfo {
        val prefs = getPrefs(context)
        val simulate = prefs.getBoolean(KEY_SIMULATE_UPDATE, false)
        val currentVersionCode = BuildConfig.VERSION_CODE
        val currentVersionName = BuildConfig.VERSION_NAME

        // If simulation is enabled, or if remote config has set a higher minimum version code
        val minCode = prefs.getInt(KEY_MIN_VERSION_CODE, if (simulate) currentVersionCode + 1 else 0)
        val latestName = prefs.getString(KEY_LATEST_VERSION_NAME, null)
            ?: if (simulate) "1.0.1" else currentVersionName
        val isForce = prefs.getBoolean(KEY_IS_FORCE_UPDATE, true)

        val isAvailable = simulate || currentVersionCode < minCode

        return AppUpdateInfo(
            isUpdateAvailable = isAvailable,
            isForceUpdate = isForce,
            latestVersionName = latestName,
            minRequiredVersionCode = minCode
        )
    }

    fun setSimulatedUpdate(
        context: Context,
        available: Boolean,
        force: Boolean = true,
        versionName: String = "1.0.1",
        minVersionCode: Int = BuildConfig.VERSION_CODE + 1
    ) {
        getPrefs(context).edit()
            .putBoolean(KEY_SIMULATE_UPDATE, available)
            .putBoolean(KEY_IS_FORCE_UPDATE, force)
            .putString(KEY_LATEST_VERSION_NAME, versionName)
            .putInt(KEY_MIN_VERSION_CODE, minVersionCode)
            .apply()
    }

    fun clearSimulatedUpdate(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
