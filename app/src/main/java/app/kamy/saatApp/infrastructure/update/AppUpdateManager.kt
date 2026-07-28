package app.kamy.saatApp.infrastructure.update

import android.content.Context
import android.content.SharedPreferences
import app.kamy.saatApp.BuildConfig
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean = false,
    val isForceUpdate: Boolean = true,
    val latestVersionName: String = "",
    val minRequiredVersionCode: Int = 0
)

object AppUpdateManager {
    private const val PREFS_NAME = "saat_app_update_prefs"
    private const val KEY_MIN_VERSION_CODE = "min_required_version_code"
    private const val KEY_LATEST_VERSION_NAME = "latest_version_name"
    private const val KEY_IS_FORCE_UPDATE = "is_force_update"
    private const val KEY_SIMULATE_UPDATE = "simulate_update_available"

    private val _updateInfo = MutableStateFlow(AppUpdateInfo())
    val updateInfoFlow: StateFlow<AppUpdateInfo> = _updateInfo.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun checkUpdate(context: Context): AppUpdateInfo {
        val prefs = getPrefs(context)
        val simulate = prefs.getBoolean(KEY_SIMULATE_UPDATE, false)
        val currentVersionCode = BuildConfig.VERSION_CODE
        val currentVersionName = BuildConfig.VERSION_NAME

        val minCode = prefs.getInt(KEY_MIN_VERSION_CODE, if (simulate) currentVersionCode + 1 else 0)
        val latestName = prefs.getString(KEY_LATEST_VERSION_NAME, null)
            ?: if (simulate) "1.0.1" else currentVersionName
        val isForce = prefs.getBoolean(KEY_IS_FORCE_UPDATE, true)

        val isAvailable = simulate || (minCode > 0 && currentVersionCode < minCode)

        val info = AppUpdateInfo(
            isUpdateAvailable = isAvailable,
            isForceUpdate = isForce,
            latestVersionName = latestName,
            minRequiredVersionCode = minCode
        )
        _updateInfo.value = info
        return info
    }

    fun checkForUpdateAsync(context: Context, onResult: ((AppUpdateInfo) -> Unit)? = null) {
        val localInfo = checkUpdate(context)
        if (localInfo.isUpdateAvailable) {
            onResult?.invoke(localInfo)
            return
        }

        try {
            val playUpdateManager = AppUpdateManagerFactory.create(context)
            playUpdateManager.appUpdateInfo.addOnSuccessListener { playInfo ->
                val isAvailable = playInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                        playInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

                if (isAvailable) {
                    val availableVersionCode = playInfo.availableVersionCode()
                    val currentVersionCode = BuildConfig.VERSION_CODE
                    val isForce = playInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ||
                            availableVersionCode > currentVersionCode

                    val displayVersion = if (availableVersionCode > 0) "v$availableVersionCode" else "Versi Baru"

                    val info = AppUpdateInfo(
                        isUpdateAvailable = true,
                        isForceUpdate = isForce,
                        latestVersionName = displayVersion,
                        minRequiredVersionCode = availableVersionCode
                    )

                    getPrefs(context).edit()
                        .putInt(KEY_MIN_VERSION_CODE, availableVersionCode)
                        .putString(KEY_LATEST_VERSION_NAME, displayVersion)
                        .putBoolean(KEY_IS_FORCE_UPDATE, isForce)
                        .apply()

                    _updateInfo.value = info
                    onResult?.invoke(info)
                } else {
                    onResult?.invoke(localInfo)
                }
            }.addOnFailureListener {
                onResult?.invoke(localInfo)
            }
        } catch (e: Throwable) {
            onResult?.invoke(localInfo)
        }
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
        checkUpdate(context)
    }

    fun clearSimulatedUpdate(context: Context) {
        getPrefs(context).edit().clear().apply()
        checkUpdate(context)
    }
}
