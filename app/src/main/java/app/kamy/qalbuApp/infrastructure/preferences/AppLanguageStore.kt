package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.core.locale.AppLanguage

class AppLanguageStore(context: Context) {

    // applicationContext is null during Application.attachBaseContext; fall back to base context.
    private val prefs = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun current(): AppLanguage = AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE, null))

    fun set(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.tag).apply()
    }

    companion object {
        private const val PREFS_NAME = "qalbu_app_language"
        private const val KEY_LANGUAGE = "language"

        fun from(context: Context): AppLanguageStore = AppLanguageStore(context)
    }
}
