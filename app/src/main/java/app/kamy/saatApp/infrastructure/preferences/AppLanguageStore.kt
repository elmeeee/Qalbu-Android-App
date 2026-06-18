package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.core.locale.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLanguageStore(context: Context) {

    // applicationContext is null during Application.attachBaseContext; fall back to base context.
    private val prefs = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentFlow = MutableStateFlow(current())

    val currentFlow: StateFlow<AppLanguage> = _currentFlow.asStateFlow()

    fun current(): AppLanguage = AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, null))

    fun set(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).apply()
        _currentFlow.value = language
    }

    companion object {
        private const val PREFS_NAME = "saat_app_language"
        private const val KEY_LANGUAGE = "language"

        fun from(context: Context): AppLanguageStore = AppLanguageStore(context)
    }
}
