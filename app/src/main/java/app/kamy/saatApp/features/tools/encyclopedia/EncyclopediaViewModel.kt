package app.kamy.saatApp.features.tools.encyclopedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.EncyclopediaCategory
import app.kamy.saatApp.domain.model.EncyclopediaTopic
import app.kamy.saatApp.domain.model.GlossaryTerm
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.repository.EncyclopediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EncyclopediaUiState(
    val selectedCategory: EncyclopediaCategory = EncyclopediaCategory.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val topics: List<EncyclopediaTopic> = emptyList(),
    val glossaryTerms: List<GlossaryTerm> = emptyList(),
    val currentLanguage: AppLanguage = AppLanguage.INDONESIAN
)

@HiltViewModel
class EncyclopediaViewModel @Inject constructor(
    private val repository: EncyclopediaRepository,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {

    private val _state = MutableStateFlow(EncyclopediaUiState())
    val state: StateFlow<EncyclopediaUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appLanguageStore.currentFlow.collect { lang ->
                _state.update { it.copy(currentLanguage = lang) }
                loadData()
            }
        }
    }

    fun selectCategory(category: EncyclopediaCategory) {
        _state.update { it.copy(selectedCategory = category) }
        loadData()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val currentLang = appLanguageStore.current()
            val category = _state.value.selectedCategory
            val query = _state.value.searchQuery

            val topics = repository.getTopicsByCategory(category, query, currentLang)
            val glossary = if (category == EncyclopediaCategory.GLOSSARY || category == EncyclopediaCategory.ALL) {
                repository.getGlossaryTerms(query, currentLang)
            } else {
                emptyList()
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    topics = topics,
                    glossaryTerms = glossary
                )
            }
        }
    }
}
