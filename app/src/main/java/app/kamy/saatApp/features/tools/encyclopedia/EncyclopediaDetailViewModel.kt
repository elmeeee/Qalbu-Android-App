package app.kamy.saatApp.features.tools.encyclopedia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.EncyclopediaTopic
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.repository.EncyclopediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EncyclopediaDetailUiState(
    val topic: EncyclopediaTopic? = null,
    val isLoading: Boolean = true,
    val currentLanguage: AppLanguage = AppLanguage.INDONESIAN
)

@HiltViewModel
class EncyclopediaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EncyclopediaRepository,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {

    private val topicId: String = checkNotNull(savedStateHandle["topicId"])

    private val _state = MutableStateFlow(EncyclopediaDetailUiState())
    val state: StateFlow<EncyclopediaDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appLanguageStore.currentFlow.collect { lang ->
                _state.update { it.copy(currentLanguage = lang) }
            }
        }
        loadTopic()
    }

    private fun loadTopic() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val topic = repository.getTopicById(topicId)
            _state.update { it.copy(topic = topic, isLoading = false) }
        }
    }
}
