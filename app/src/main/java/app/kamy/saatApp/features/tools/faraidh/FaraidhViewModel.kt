package app.kamy.saatApp.features.tools.faraidh

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.domain.faraidh.DeceasedGender
import app.kamy.saatApp.domain.faraidh.DeceasedProfile
import app.kamy.saatApp.domain.faraidh.FaraidhEngine
import app.kamy.saatApp.domain.faraidh.FaraidhProofItem
import app.kamy.saatApp.domain.faraidh.FaraidhResult
import app.kamy.saatApp.domain.faraidh.HeirInput
import app.kamy.saatApp.infrastructure.faraidh.FaraidhReferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FaraidhUiState(
    val gender: DeceasedGender = DeceasedGender.MALE,
    val netEstate: String = "",
    val husbandCount: Int = 0,
    val wifeCount: Int = 0,
    val fatherCount: Int = 0,
    val motherCount: Int = 0,
    val sonCount: Int = 0,
    val daughterCount: Int = 0,
    val grandsonCount: Int = 0,
    val granddaughterCount: Int = 0,
    val fullBrotherCount: Int = 0,
    val fullSisterCount: Int = 0,
    val paternalBrotherCount: Int = 0,
    val paternalSisterCount: Int = 0,
    val maternalBrotherCount: Int = 0,
    val maternalSisterCount: Int = 0,
    val selectedTab: Int = 0,
    val showInputSheet: Boolean = true,
    val result: FaraidhResult? = null,
    val proofs: List<FaraidhProofItem> = emptyList(),
    val pdfExporting: Boolean = false,
    val pdfUri: Uri? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class FaraidhViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val referenceRepository: FaraidhReferenceRepository,
    private val languageStore: AppLanguageStore
) : ViewModel() {

    private val _state = MutableStateFlow(FaraidhUiState())
    val state: StateFlow<FaraidhUiState> = _state.asStateFlow()

    init {
        recompute()
    }

    fun setGender(gender: DeceasedGender) {
        _state.update { it.copy(gender = gender) }
        recompute()
    }

    fun setNetEstate(value: String) {
        _state.update { it.copy(netEstate = value) }
        recompute()
    }

    fun setHeirCount(field: HeirCountField, count: Int) {
        val safe = count.coerceAtLeast(0)
        _state.update {
            when (field) {
                HeirCountField.HUSBAND -> it.copy(husbandCount = safe.coerceAtMost(1))
                HeirCountField.WIFE -> it.copy(wifeCount = safe.coerceIn(0, 4))
                HeirCountField.FATHER -> it.copy(fatherCount = safe.coerceAtMost(1))
                HeirCountField.MOTHER -> it.copy(motherCount = safe.coerceAtMost(1))
                HeirCountField.SON -> it.copy(sonCount = safe)
                HeirCountField.DAUGHTER -> it.copy(daughterCount = safe)
                HeirCountField.GRANDSON -> it.copy(grandsonCount = safe)
                HeirCountField.GRANDDAUGHTER -> it.copy(granddaughterCount = safe)
                HeirCountField.FULL_BROTHER -> it.copy(fullBrotherCount = safe)
                HeirCountField.FULL_SISTER -> it.copy(fullSisterCount = safe)
                HeirCountField.PATERNAL_BROTHER -> it.copy(paternalBrotherCount = safe)
                HeirCountField.PATERNAL_SISTER -> it.copy(paternalSisterCount = safe)
                HeirCountField.MATERNAL_BROTHER -> it.copy(maternalBrotherCount = safe)
                HeirCountField.MATERNAL_SISTER -> it.copy(maternalSisterCount = safe)
            }
        }
        recompute()
    }

    fun selectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    fun toggleInputSheet(show: Boolean) {
        _state.update { it.copy(showInputSheet = show) }
    }

    fun clearPdfUri() {
        _state.update { it.copy(pdfUri = null) }
    }

    fun exportPdf(onReady: (Uri) -> Unit) {
        val result = _state.value.result ?: return
        viewModelScope.launch {
            _state.update { it.copy(pdfExporting = true, errorMessage = null) }
            runCatching {
                val language = languageStore.current()
                val proofs = referenceRepository.proofsForKeys(result.proofKeys, language)
                val uri = FaraidhPdfExporter.export(context, result, proofs, language)
                _state.update { it.copy(pdfExporting = false, pdfUri = uri) }
                onReady(uri)
            }.onFailure { e ->
                _state.update { it.copy(pdfExporting = false, errorMessage = e.message) }
            }
        }
    }

    private fun recompute() {
        val s = _state.value
        val estate = s.netEstate.replace(",", "").toBigDecimalOrNull() ?: BigDecimal.ZERO
        val input = HeirInput(
            husbandCount = s.husbandCount,
            wifeCount = s.wifeCount,
            fatherCount = s.fatherCount,
            motherCount = s.motherCount,
            sonCount = s.sonCount,
            daughterCount = s.daughterCount,
            grandsonCount = s.grandsonCount,
            granddaughterCount = s.granddaughterCount,
            fullBrotherCount = s.fullBrotherCount,
            fullSisterCount = s.fullSisterCount,
            paternalBrotherCount = s.paternalBrotherCount,
            paternalSisterCount = s.paternalSisterCount,
            maternalBrotherCount = s.maternalBrotherCount,
            maternalSisterCount = s.maternalSisterCount
        )
        val profile = DeceasedProfile(s.gender, estate)
        val result = FaraidhEngine.calculate(profile, input)
        viewModelScope.launch {
            runCatching {
                referenceRepository.proofsForKeys(result.proofKeys, languageStore.current())
            }.onSuccess { proofs ->
                _state.update { it.copy(result = result, proofs = proofs) }
            }.onFailure {
                _state.update { it.copy(result = result, proofs = emptyList()) }
            }
        }
    }
}

enum class HeirCountField {
    HUSBAND, WIFE, FATHER, MOTHER, SON, DAUGHTER,
    GRANDSON, GRANDDAUGHTER,
    FULL_BROTHER, FULL_SISTER,
    PATERNAL_BROTHER, PATERNAL_SISTER,
    MATERNAL_BROTHER, MATERNAL_SISTER
}
