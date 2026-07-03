package app.kamy.saatApp.features.tools.wudhu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.WudhuItem
import app.kamy.saatApp.infrastructure.local.LocalWudhuDataSource
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WudhuUiState(
    val loading: Boolean = true,
    val steps: List<WudhuItem> = emptyList(),
    val language: AppLanguage = AppLanguage.INDONESIAN,
    val error: String? = null
)

@HiltViewModel
class WudhuViewModel @Inject constructor(
    private val dataSource: LocalWudhuDataSource,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {

    private val _state = MutableStateFlow(WudhuUiState())
    val state: StateFlow<WudhuUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadSteps()
            appLanguageStore.currentFlow.collect { lang ->
                _state.update { it.copy(language = lang) }
            }
        }
    }

    private suspend fun loadSteps() {
        _state.update { it.copy(loading = true) }
        try {
            val steps = dataSource.getWudhuSteps()
            _state.update { it.copy(loading = false, steps = steps, error = null) }
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, error = e.localizedMessage ?: "Failed to load steps") }
        }
    }
}
