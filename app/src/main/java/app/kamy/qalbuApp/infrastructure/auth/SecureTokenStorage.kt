package app.kamy.qalbuApp.infrastructure.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * EncryptedSharedPreferences-backed secure storage for OAuth tokens. Mirrors
 * the role of iOS Keychain in `QFUserSession`.
 *
 * Backed by AES256-GCM master key + AES256-GCM value encryption (Android Keystore).
 */
@Singleton
class SecureTokenStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = createPrefs(context)

    companion object {
        private const val TAG = "SecureTokenStorage"
        private const val PREFS_NAME = "qalbu_secure_prefs"
        private const val FALLBACK_PREFS_NAME = "qalbu_secure_prefs_fallback"

        private fun createPrefs(context: Context): SharedPreferences = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (t: Throwable) {
            // Keystore / EncryptedSharedPreferences can fail on some emulators and devices.
            Log.w(TAG, "Encrypted prefs unavailable; using private fallback", t)
            context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun read(key: String): String? = prefs.getString(key, null)?.takeIf { it.isNotEmpty() }

    fun write(key: String, value: String?) {
        prefs.edit().apply {
            if (value.isNullOrEmpty()) remove(key) else putString(key, value)
        }.apply()
    }

    fun remove(vararg keys: String) {
        prefs.edit().apply { keys.forEach { remove(it) } }.apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    object Keys {
        const val USER_ACCESS_TOKEN = "qf.user.accessToken"
        const val USER_REFRESH_TOKEN = "qf.user.refreshToken"
        const val USER_ID_TOKEN = "qf.user.idToken"
        const val USER_ACCESS_EXPIRY = "qf.user.accessExpiryEpochSec"
        const val CONTENT_ACCESS_TOKEN = "qf.content.accessToken"
        const val CONTENT_ACCESS_EXPIRY = "qf.content.accessExpiryEpochSec"
    }
}
