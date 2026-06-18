package app.kamy.saatApp.features.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.domain.model.DhikrBundle
import app.kamy.saatApp.domain.model.DoaCatalogEntry
import app.kamy.saatApp.domain.model.DoaCatalogKind
import app.kamy.saatApp.domain.model.DoaItem
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.repository.DoaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoaZikirUiState(
    val loading: Boolean = true,
    val catalog: List<DoaCatalogEntry> = emptyList(),
    val selectedSlug: String? = null,
    val selectedTitle: String? = null,
    val doaItems: List<DoaItem> = emptyList(),
    val dhikrBundles: List<DhikrBundle> = emptyList()
)

@HiltViewModel
class DoaZikirViewModel @Inject constructor(
    private val repository: DoaRepository,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {
    private val _state = MutableStateFlow(DoaZikirUiState())
    val state: StateFlow<DoaZikirUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadCatalog()
            appLanguageStore.currentFlow.drop(1).collect {
                repository.invalidateLocaleCache()
                reloadForLanguage()
            }
        }
    }

    fun selectCategory(slug: String) {
        val entry = _state.value.catalog.firstOrNull { it.slug == slug } ?: return
        _state.update {
            it.copy(
                loading = true,
                selectedSlug = slug,
                selectedTitle = entry.title,
                doaItems = emptyList(),
                dhikrBundles = emptyList()
            )
        }
        viewModelScope.launch {
            if (entry.kind == DoaCatalogKind.DHIKR) {
                val bundles = repository.getDhikr(slug)
                _state.update { it.copy(loading = false, dhikrBundles = bundles) }
            } else {
                val doas = repository.getDoas(slug)
                _state.update { it.copy(loading = false, doaItems = doas) }
            }
        }
    }

    fun clearSelection() {
        _state.update {
            it.copy(selectedSlug = null, selectedTitle = null, doaItems = emptyList(), dhikrBundles = emptyList())
        }
    }

    private suspend fun loadCatalog() {
        val catalog = repository.getCatalog()
        _state.update { it.copy(loading = false, catalog = catalog) }
    }

    private suspend fun reloadForLanguage() {
        val slug = _state.value.selectedSlug
        _state.update { it.copy(loading = true) }
        val catalog = repository.getCatalog()
        if (slug == null) {
            _state.update { it.copy(loading = false, catalog = catalog) }
            return
        }
        val entry = catalog.firstOrNull { it.slug == slug } ?: run {
            _state.update { it.copy(loading = false, catalog = catalog) }
            return
        }
        if (entry.kind == DoaCatalogKind.DHIKR) {
            val bundles = repository.getDhikr(slug)
            _state.update {
                it.copy(loading = false, catalog = catalog, dhikrBundles = bundles, doaItems = emptyList())
            }
        } else {
            val doas = repository.getDoas(slug)
            _state.update {
                it.copy(loading = false, catalog = catalog, doaItems = doas, dhikrBundles = emptyList())
            }
        }
    }
}
