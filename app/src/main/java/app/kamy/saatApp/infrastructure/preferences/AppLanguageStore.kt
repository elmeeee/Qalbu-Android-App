package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.defaults.DeviceLanguageDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLanguageStore(context: Context) {

    private val appContext = context.applicationContext ?: context

    // applicationContext is null during Application.attachBaseContext; fall back to base context.
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val prefChangeListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_LANGUAGE) {
            _sharedFlow.value = current()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
        _sharedFlow.value = current()
    }

    val currentFlow: StateFlow<AppLanguage> = _sharedFlow.asStateFlow()

    /** Uses the device language until the user/default initializer persists a choice. */
    fun current(): AppLanguage = prefs.getString(KEY_LANGUAGE, null)
        ?.let(AppLanguage::fromTag)
        ?: DeviceLanguageDetector.detect()

    fun set(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).commit()
        val locale = java.util.Locale.forLanguageTag(language.tag)
        java.util.Locale.setDefault(locale)
        try {
            val config = android.content.res.Configuration(appContext.resources.configuration).apply {
                setLocale(locale)
            }
            @Suppress("DEPRECATION")
            appContext.resources.updateConfiguration(config, appContext.resources.displayMetrics)
        } catch (_: Throwable) {}
        _sharedFlow.value = language
    }

    companion object {
        private const val PREFS_NAME = "saat_app_language"
        private const val KEY_LANGUAGE = "language"

        private val _sharedFlow = MutableStateFlow(DeviceLanguageDetector.detect())
        @Volatile private var instance: AppLanguageStore? = null

        fun from(context: Context): AppLanguageStore {
            return instance ?: synchronized(this) {
                instance ?: AppLanguageStore(context.applicationContext ?: context).also { store ->
                    _sharedFlow.value = store.current()
                    instance = store
                }
            }
        }
    }
}
