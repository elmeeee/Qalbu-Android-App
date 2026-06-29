package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.domain.model.ArabicTextType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

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

    private val _showTransliteration = MutableStateFlow(prefs.getBoolean(KEY_SHOW_LATIN, false))
    val showTransliteration: StateFlow<Boolean> = _showTransliteration.asStateFlow()

    private val _isTajweedEnabled = MutableStateFlow(prefs.getBoolean(KEY_TAJWEED_ENABLED, true))
    val isTajweedEnabled: StateFlow<Boolean> = _isTajweedEnabled.asStateFlow()

    private val _arabicTextType = MutableStateFlow(loadArabicTextType())
    val arabicTextType: StateFlow<ArabicTextType> = _arabicTextType.asStateFlow()

    private val _recitationId = MutableStateFlow(loadRecitationId())
    val recitationId: StateFlow<Int> = _recitationId.asStateFlow()

    fun currentTranslationId(): Int = _translationId.value

    fun currentRecitationId(): Int = _recitationId.value

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

    fun setShowTransliteration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_LATIN, enabled).apply()
        _showTransliteration.value = enabled
    }

    fun setTajweedEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TAJWEED_ENABLED, enabled).apply()
        _isTajweedEnabled.value = enabled
    }

    fun setArabicTextType(type: ArabicTextType) {
        prefs.edit().putString(KEY_ARABIC_TEXT_TYPE, type.name).apply()
        _arabicTextType.value = type
    }

    fun setRecitation(id: Int) {
        if (id <= 0) return
        prefs.edit().putInt(KEY_RECITATION_ID, id).apply()
        _recitationId.value = id
    }

    private fun loadTranslationId(): Int {
        val saved = prefs.getInt(KEY_ID, 0)
        val raw = if (saved > 0) saved else AppConfig.defaultTranslationId
        return LocalQuranConfig.normalizeTranslationId(raw)
    }

    private fun loadRecitationId(): Int {
        val saved = prefs.getInt(KEY_RECITATION_ID, 0)
        val raw = if (saved > 0) saved else DEFAULT_RECITATION_ID
        return LocalQuranConfig.normalizeRecitationId(raw)
    }

    private fun loadArabicTextType(): ArabicTextType {
        val saved = prefs.getString(KEY_ARABIC_TEXT_TYPE, null)
        return runCatching { ArabicTextType.valueOf(saved!!) }.getOrDefault(ArabicTextType.TAJWEED)
    }

    companion object {
        const val DEFAULT_RECITATION_ID = LocalQuranConfig.DEFAULT_RECITATION_ID
        private const val PREFS_NAME = "saat_reader_prefs"
        private const val KEY_ID = "chapterReaderTranslationId"
        private const val KEY_NAME = "chapterReaderTranslationName"
        private const val KEY_SHOW = "chapterReaderShowTranslation"
        private const val KEY_SHOW_LATIN = "chapterReaderShowTransliteration"
        private const val KEY_RECITATION_ID = "chapterReaderRecitationId"
        private const val KEY_ARABIC_TEXT_TYPE = "chapterReaderArabicTextType"
        private const val KEY_TAJWEED_ENABLED = "chapterReaderTajweedEnabled"
    }
}
