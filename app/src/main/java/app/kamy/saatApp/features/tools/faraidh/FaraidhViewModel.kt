package app.kamy.saatApp.features.tools.faraidh

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.domain.faraidh.EstateAssetInput
import app.kamy.saatApp.domain.faraidh.EstateComputation
import app.kamy.saatApp.domain.faraidh.FaraidhEstateCalculator
import app.kamy.saatApp.domain.faraidh.FaraidhGlossaryItem
import app.kamy.saatApp.domain.faraidh.FaraidhMadhhab
import app.kamy.saatApp.domain.faraidh.DeceasedGender
import app.kamy.saatApp.domain.faraidh.DeceasedProfile
import app.kamy.saatApp.domain.faraidh.FaraidhEngine
import app.kamy.saatApp.domain.faraidh.FaraidhParticipantNames
import app.kamy.saatApp.domain.faraidh.FaraidhProofItem
import app.kamy.saatApp.domain.faraidh.FaraidhResult
import app.kamy.saatApp.domain.faraidh.HeirInput
import app.kamy.saatApp.domain.faraidh.resizeNameList
import app.kamy.saatApp.infrastructure.faraidh.FaraidhReferenceRepository
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.FaraidhScenarioData
import app.kamy.saatApp.infrastructure.preferences.FaraidhScenarioMeta
import app.kamy.saatApp.infrastructure.preferences.FaraidhScenarioStore
import app.kamy.saatApp.infrastructure.repository.GoldPriceRepository
import app.kamy.saatApp.domain.faraidh.MoneyInputFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

data class FaraidhUiState(
    val gender: DeceasedGender = DeceasedGender.MALE,
    val madhhab: FaraidhMadhhab = FaraidhMadhhab.SHAFII,
    val deceasedBornOutOfWedlock: Boolean = false,
    val estate: EstateAssetInput = EstateAssetInput(),
    val estateComputation: EstateComputation? = null,
    val netEstate: String = "",
    val names: FaraidhParticipantNames = FaraidhParticipantNames(),
    val husbandCount: Int = 0,
    val wifeCount: Int = 0,
    val fatherCount: Int = 0,
    val grandfatherCount: Int = 0,
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
    val showInputSheet: Boolean = false,
    val result: FaraidhResult? = null,
    val proofs: List<FaraidhProofItem> = emptyList(),
    val glossary: List<FaraidhGlossaryItem> = emptyList(),
    val pdfExporting: Boolean = false,
    val pdfUri: Uri? = null,
    val errorMessage: String? = null,
    val savedScenarios: List<FaraidhScenarioMeta> = emptyList(),
    val scenarioMessage: String? = null,
    val liveGoldPrice: String? = null
)

@HiltViewModel
class FaraidhViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val referenceRepository: FaraidhReferenceRepository,
    private val languageStore: AppLanguageStore,
    private val scenarioStore: FaraidhScenarioStore,
    private val goldPriceRepository: GoldPriceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FaraidhUiState())
    val state: StateFlow<FaraidhUiState> = _state.asStateFlow()

    init {
        loadGlossary()
        loadSavedScenarios()
        restoreAutoDraft()
        fetchLiveGoldPrice()
        recompute()
        viewModelScope.launch {
            languageStore.currentFlow.drop(1).collect {
                loadGlossary()
                recompute()
            }
        }
    }

    private fun fetchLiveGoldPrice() {
        viewModelScope.launch {
            runCatching {
                goldPriceRepository.fetchQuote("IDR")
            }.onSuccess { quote ->
                if (quote != null) {
                    val livePrice = quote.goldPerGramIdr.toLong().toString()
                    _state.update { current ->
                        val estate = if (current.estate.goldPricePerGram.isBlank()) {
                            current.estate.copy(goldPricePerGram = MoneyInputFormatter.format(livePrice))
                        } else current.estate
                        current.copy(liveGoldPrice = livePrice, estate = estate)
                    }
                }
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    private fun loadSavedScenarios() {
        _state.update { it.copy(savedScenarios = scenarioStore.listScenarios()) }
    }

    private fun restoreAutoDraft() {
        val draft = scenarioStore.loadAutoDraft() ?: return
        _state.update { applyScenarioData(it, draft) }
    }

    fun saveScenario(title: String) {
        val trimmed = title.trim().ifBlank {
            _state.value.names.deceasedName.trim().ifBlank { "Faraidh" }
        }
        val id = System.currentTimeMillis().toString()
        val data = toScenarioData(_state.value)
        scenarioStore.saveScenario(id, trimmed, data)
        scenarioStore.saveAutoDraft(data)
        _state.update { it.copy(savedScenarios = scenarioStore.listScenarios(), scenarioMessage = "saved") }
    }

    fun loadScenario(id: String) {
        val data = scenarioStore.loadScenario(id) ?: return
        _state.update { applyScenarioData(it, data) }
        recompute()
        _state.update { it.copy(scenarioMessage = "loaded") }
    }

    fun deleteScenario(id: String) {
        scenarioStore.deleteScenario(id)
        _state.update { it.copy(savedScenarios = scenarioStore.listScenarios()) }
    }

    fun clearScenarioMessage() {
        _state.update { it.copy(scenarioMessage = null) }
    }

    fun resetCalculation() {
        scenarioStore.clearAutoDraft()
        _state.value = FaraidhUiState(glossary = _state.value.glossary, savedScenarios = scenarioStore.listScenarios())
        recompute()
    }

    private fun persistDraft() {
        scenarioStore.saveAutoDraft(toScenarioData(_state.value))
    }

    private fun toScenarioData(state: FaraidhUiState): FaraidhScenarioData = FaraidhScenarioData(
        gender = state.gender.name,
        madhhab = state.madhhab.name,
        deceasedBornOutOfWedlock = state.deceasedBornOutOfWedlock,
        estate = state.estate,
        names = state.names,
        husbandCount = state.husbandCount,
        wifeCount = state.wifeCount,
        fatherCount = state.fatherCount,
        grandfatherCount = state.grandfatherCount,
        motherCount = state.motherCount,
        sonCount = state.sonCount,
        daughterCount = state.daughterCount,
        grandsonCount = state.grandsonCount,
        granddaughterCount = state.granddaughterCount,
        fullBrotherCount = state.fullBrotherCount,
        fullSisterCount = state.fullSisterCount,
        paternalBrotherCount = state.paternalBrotherCount,
        paternalSisterCount = state.paternalSisterCount,
        maternalBrotherCount = state.maternalBrotherCount,
        maternalSisterCount = state.maternalSisterCount
    )

    private fun applyScenarioData(state: FaraidhUiState, data: FaraidhScenarioData): FaraidhUiState =
        state.copy(
            gender = runCatching { DeceasedGender.valueOf(data.gender) }.getOrDefault(DeceasedGender.MALE),
            madhhab = runCatching { FaraidhMadhhab.valueOf(data.madhhab) }.getOrDefault(FaraidhMadhhab.SHAFII),
            deceasedBornOutOfWedlock = data.deceasedBornOutOfWedlock,
            estate = data.estate,
            names = data.names,
            husbandCount = data.husbandCount,
            wifeCount = data.wifeCount,
            fatherCount = data.fatherCount,
            grandfatherCount = data.grandfatherCount,
            motherCount = data.motherCount,
            sonCount = data.sonCount,
            daughterCount = data.daughterCount,
            grandsonCount = data.grandsonCount,
            granddaughterCount = data.granddaughterCount,
            fullBrotherCount = data.fullBrotherCount,
            fullSisterCount = data.fullSisterCount,
            paternalBrotherCount = data.paternalBrotherCount,
            paternalSisterCount = data.paternalSisterCount,
            maternalBrotherCount = data.maternalBrotherCount,
            maternalSisterCount = data.maternalSisterCount
        )

    private fun loadGlossary() {
        viewModelScope.launch {
            runCatching {
                referenceRepository.glossaryItems(languageStore.current())
            }.onSuccess { items ->
                _state.update { it.copy(glossary = items) }
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    fun setMadhhab(madhhab: FaraidhMadhhab) {
        _state.update { it.copy(madhhab = madhhab) }
        recompute()
    }

    fun setEstateField(update: EstateAssetInput.() -> EstateAssetInput) {
        _state.update { it.copy(estate = it.estate.update()) }
        recompute()
    }

    fun setDeceasedName(value: String) {
        _state.update { it.copy(names = it.names.copy(deceasedName = value)) }
        recompute()
    }

    fun setDeceasedBornOutOfWedlock(value: Boolean) {
        _state.update { it.copy(deceasedBornOutOfWedlock = value) }
        recompute()
    }

    fun setGender(gender: DeceasedGender) {
        _state.update { current ->
            val names = when (gender) {
                DeceasedGender.MALE -> current.names.copy(husbandName = "")
                DeceasedGender.FEMALE -> current.names.copy(wifeNames = emptyList())
            }
            current.copy(
                gender = gender,
                husbandCount = if (gender == DeceasedGender.MALE) 0 else current.husbandCount,
                wifeCount = if (gender == DeceasedGender.FEMALE) 0 else current.wifeCount,
                names = names
            )
        }
        recompute()
    }

    fun setNetEstate(value: String) {
        _state.update { it.copy(netEstate = value) }
        recompute()
    }

    fun setHeirName(field: HeirNameField, index: Int, value: String) {
        _state.update { state ->
            val names = when (field) {
                HeirNameField.DECEASED -> state.names.copy(deceasedName = value)
                HeirNameField.HUSBAND -> state.names.copy(husbandName = value)
                HeirNameField.WIFE -> state.names.copy(
                    wifeNames = state.names.wifeNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.FATHER -> state.names.copy(fatherName = value)
                HeirNameField.GRANDFATHER -> state.names.copy(grandfatherName = value)
                HeirNameField.MOTHER -> state.names.copy(motherName = value)
                HeirNameField.SON -> state.names.copy(
                    sonNames = state.names.sonNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.DAUGHTER -> state.names.copy(
                    daughterNames = state.names.daughterNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.GRANDSON -> state.names.copy(
                    grandsonNames = state.names.grandsonNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.GRANDDAUGHTER -> state.names.copy(
                    granddaughterNames = state.names.granddaughterNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.FULL_BROTHER -> state.names.copy(
                    fullBrotherNames = state.names.fullBrotherNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.FULL_SISTER -> state.names.copy(
                    fullSisterNames = state.names.fullSisterNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.PATERNAL_BROTHER -> state.names.copy(
                    paternalBrotherNames = state.names.paternalBrotherNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.PATERNAL_SISTER -> state.names.copy(
                    paternalSisterNames = state.names.paternalSisterNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.MATERNAL_BROTHER -> state.names.copy(
                    maternalBrotherNames = state.names.maternalBrotherNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
                HeirNameField.MATERNAL_SISTER -> state.names.copy(
                    maternalSisterNames = state.names.maternalSisterNames.toMutableList().apply {
                        while (size <= index) add("")
                        this[index] = value
                    }
                )
            }
            state.copy(names = names)
        }
        recompute()
    }

    fun setHeirCount(field: HeirCountField, count: Int) {
        val safe = count.coerceAtLeast(0)
        _state.update { state ->
            val updated = when (field) {
                HeirCountField.HUSBAND -> state.copy(husbandCount = safe.coerceAtMost(1))
                HeirCountField.WIFE -> state.copy(wifeCount = safe.coerceIn(0, 4))
                HeirCountField.FATHER -> state.copy(
                    fatherCount = safe.coerceAtMost(1),
                    // Father blocks grandfather: auto-reset grandfather when father is added
                    grandfatherCount = if (safe > 0) 0 else state.grandfatherCount
                )
                HeirCountField.GRANDFATHER -> state.copy(
                    grandfatherCount = safe.coerceAtMost(1),
                    // Grandfather can only exist if no father
                    fatherCount = if (safe > 0) 0 else state.fatherCount
                )
                HeirCountField.MOTHER -> state.copy(motherCount = safe.coerceAtMost(1))
                HeirCountField.SON -> state.copy(sonCount = safe)
                HeirCountField.DAUGHTER -> state.copy(daughterCount = safe)
                HeirCountField.GRANDSON -> state.copy(grandsonCount = safe)
                HeirCountField.GRANDDAUGHTER -> state.copy(granddaughterCount = safe)
                HeirCountField.FULL_BROTHER -> state.copy(fullBrotherCount = safe)
                HeirCountField.FULL_SISTER -> state.copy(fullSisterCount = safe)
                HeirCountField.PATERNAL_BROTHER -> state.copy(paternalBrotherCount = safe)
                HeirCountField.PATERNAL_SISTER -> state.copy(paternalSisterCount = safe)
                HeirCountField.MATERNAL_BROTHER -> state.copy(maternalBrotherCount = safe)
                HeirCountField.MATERNAL_SISTER -> state.copy(maternalSisterCount = safe)
            }
            updated.copy(names = syncNamesToCounts(updated))
        }
        recompute()
    }

    private fun syncNamesToCounts(state: FaraidhUiState): FaraidhParticipantNames = state.names.copy(
        wifeNames = resizeNameList(state.names.wifeNames, state.wifeCount),
        sonNames = resizeNameList(state.names.sonNames, state.sonCount),
        daughterNames = resizeNameList(state.names.daughterNames, state.daughterCount),
        grandsonNames = resizeNameList(state.names.grandsonNames, state.grandsonCount),
        granddaughterNames = resizeNameList(state.names.granddaughterNames, state.granddaughterCount),
        fullBrotherNames = resizeNameList(state.names.fullBrotherNames, state.fullBrotherCount),
        fullSisterNames = resizeNameList(state.names.fullSisterNames, state.fullSisterCount),
        paternalBrotherNames = resizeNameList(state.names.paternalBrotherNames, state.paternalBrotherCount),
        paternalSisterNames = resizeNameList(state.names.paternalSisterNames, state.paternalSisterCount),
        maternalBrotherNames = resizeNameList(state.names.maternalBrotherNames, state.maternalBrotherCount),
        maternalSisterNames = resizeNameList(state.names.maternalSisterNames, state.maternalSisterCount)
    )

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
        val state = _state.value
        val result = state.result ?: return
        viewModelScope.launch {
            _state.update { it.copy(pdfExporting = true, errorMessage = null) }
            runCatching {
                val language = languageStore.current()
                val proofs = referenceRepository.proofsForKeys(result.proofKeys, language)
                val uri = FaraidhPdfExporter.export(
                    context = context,
                    result = result,
                    estateInput = state.estate,
                    names = state.names,
                    proofs = proofs,
                    glossary = state.glossary,
                    language = language
                )
                _state.update { it.copy(pdfExporting = false, pdfUri = uri) }
                onReady(uri)
            }.onFailure { e ->
                _state.update { it.copy(pdfExporting = false, errorMessage = e.message) }
            }
        }
    }

    private fun recompute() {
        val s = _state.value
        val estateCalc = FaraidhEstateCalculator.compute(s.estate)
        val estate = estateCalc.netEstate
        val input = HeirInput(
            husbandCount = if (s.gender == DeceasedGender.FEMALE) s.husbandCount else 0,
            wifeCount = if (s.gender == DeceasedGender.MALE) s.wifeCount else 0,
            fatherCount = s.fatherCount,
            grandfatherCount = s.grandfatherCount,
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
        val profile = DeceasedProfile(
            gender = s.gender,
            netEstate = estate,
            name = s.names.deceasedName.trim(),
            estate = estateCalc,
            madhhab = s.madhhab,
            bornOutOfWedlock = s.deceasedBornOutOfWedlock
        )
        val result = FaraidhEngine.calculate(profile, input, s.names, s.madhhab)
        viewModelScope.launch {
            runCatching {
                referenceRepository.proofsForKeys(result.proofKeys, languageStore.current())
            }.onSuccess { proofs ->
                _state.update {
                    it.copy(
                        result = result,
                        proofs = proofs,
                        estateComputation = estateCalc,
                        netEstate = if (estateCalc.netEstate > BigDecimal.ZERO) {
                            estateCalc.netEstate.stripTrailingZeros().toPlainString()
                        } else {
                            it.netEstate
                        }
                    )
                }
                persistDraft()
            }.onFailure { e ->
                e.printStackTrace()
                _state.update {
                    it.copy(
                        result = result,
                        proofs = emptyList(),
                        estateComputation = estateCalc,
                        netEstate = estateCalc.netEstate.stripTrailingZeros().toPlainString()
                    )
                }
                persistDraft()
            }
        }
    }
}

enum class HeirCountField {
    HUSBAND, WIFE, FATHER, GRANDFATHER, MOTHER, SON, DAUGHTER,
    GRANDSON, GRANDDAUGHTER,
    FULL_BROTHER, FULL_SISTER,
    PATERNAL_BROTHER, PATERNAL_SISTER,
    MATERNAL_BROTHER, MATERNAL_SISTER
}

enum class HeirNameField {
    DECEASED, HUSBAND, WIFE, FATHER, GRANDFATHER, MOTHER, SON, DAUGHTER,
    GRANDSON, GRANDDAUGHTER,
    FULL_BROTHER, FULL_SISTER,
    PATERNAL_BROTHER, PATERNAL_SISTER,
    MATERNAL_BROTHER, MATERNAL_SISTER
}
