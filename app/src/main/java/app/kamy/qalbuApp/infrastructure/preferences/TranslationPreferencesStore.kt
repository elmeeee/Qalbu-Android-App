package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.core.config.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Mirrors iOS ChapterReaderPreferences — selected Quran translation for content API. */
@Singleton
class TranslationPreferencesStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _translationId = MutableStateFlow(loadTranslationId())
    val translationId: StateFlow<Int> = _translationId.asStateFlow()

    private val _translationName = MutableStateFlow(prefs.getString(KEY_NAME, "").orEmpty())
    val translationName: StateFlow<String> = _translationName.asStateFlow()

    private val _showTranslation = MutableStateFlow(prefs.getBoolean(KEY_SHOW, true))
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()

    fun currentTranslationId(): Int = _translationId.value

    fun setTranslation(id: Int, displayName: String) {
        prefs.edit()
            .putInt(KEY_ID, id)
            .putString(KEY_NAME, displayName)
            .apply()
        _translationId.value = id
        _translationName.value = displayName
    }

    fun setShowTranslation(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW, enabled).apply()
        _showTranslation.value = enabled
    }

    private fun loadTranslationId(): Int {
        val saved = prefs.getInt(KEY_ID, 0)
        return if (saved > 0) saved else AppConfig.defaultTranslationId
    }

    companion object {
        private const val PREFS_NAME = "qalbu_reader_prefs"
        private const val KEY_ID = "chapterReaderTranslationId"
        private const val KEY_NAME = "chapterReaderTranslationName"
        private const val KEY_SHOW = "chapterReaderShowTranslation"
    }
}
