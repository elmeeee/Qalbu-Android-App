package app.kamy.saatApp.features.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.tools.ManzilSectionDef
import app.kamy.saatApp.domain.tools.manzilSections
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.saatApp.infrastructure.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManzilSectionUi(
    val def: ManzilSectionDef,
    val verses: List<RandomAyahPayload> = emptyList(),
    val loading: Boolean = true
)

data class ManzilUiState(
    val loading: Boolean = true,
    val translationId: Int = 0,
    val sections: List<ManzilSectionUi> = emptyList()
)

@HiltViewModel
class ManzilViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val translationStore: TranslationPreferencesStore,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {
    private val sectionDefs = manzilSections()

    private val _state = MutableStateFlow(
        ManzilUiState(
            sections = sectionDefs.map { ManzilSectionUi(def = it) }
        )
    )
    val state: StateFlow<ManzilUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadAll()
            combine(
                translationStore.translationId,
                appLanguageStore.currentFlow
            ) { _, _ -> }.drop(1).collect {
                loadAll()
            }
        }
    }

    private suspend fun loadAll() {
        val currentAppLang = appLanguageStore.current()
        val prefTranslationId = translationStore.currentTranslationId()
        val translationId = resolveEffectiveTranslationId(prefTranslationId, currentAppLang)

        _state.update { it.copy(loading = true, translationId = translationId) }
        val loaded = coroutineScope {
            sectionDefs.map { def ->
                async {
                    val verses = quranRepository.getVersesByRange(
                        chapterNumber = def.surah,
                        startAyah = def.startAyah,
                        endAyah = def.endAyah,
                        translationId = translationId
                    )
                    ManzilSectionUi(def = def, verses = verses, loading = false)
                }
            }.awaitAll()
        }
        _state.update { it.copy(loading = false, translationId = translationId, sections = loaded) }
    }

    private fun resolveEffectiveTranslationId(prefId: Int, appLang: AppLanguage): Int {
        val langForPref = LocalQuranConfig.appLanguageForTranslationId(prefId)
        return if (langForPref == appLang || langForPref == null) {
            prefId
        } else {
            LocalQuranConfig.translationForAppLanguage(appLang).id
        }
    }
}
