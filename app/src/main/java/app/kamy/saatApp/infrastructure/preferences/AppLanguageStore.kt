package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.defaults.DeviceLanguageDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLanguageStore(context: Context) {

    // applicationContext is null during Application.attachBaseContext; fall back to base context.
    private val prefs = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentFlow = MutableStateFlow(current())

    val currentFlow: StateFlow<AppLanguage> = _currentFlow.asStateFlow()

    /** Uses the device language until the user/default initializer persists a choice. */
    fun current(): AppLanguage = prefs.getString(KEY_LANGUAGE, null)
        ?.let(AppLanguage::fromTag)
        ?: DeviceLanguageDetector.detect()

    fun set(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).commit()
        _currentFlow.value = language
    }

    companion object {
        private const val PREFS_NAME = "saat_app_language"
        private const val KEY_LANGUAGE = "language"

        fun from(context: Context): AppLanguageStore = AppLanguageStore(context)
    }
}
