package app.kamy.saatApp.features.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.domain.faraidh.MoneyInputFormatter
import app.kamy.saatApp.domain.tools.GoldPriceQuote
import app.kamy.saatApp.domain.tools.ZakatCalculationResult
import app.kamy.saatApp.domain.tools.ZakatCalculator
import app.kamy.saatApp.domain.tools.ZakatCountry
import app.kamy.saatApp.domain.tools.ZakatType
import app.kamy.saatApp.infrastructure.repository.GoldPriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Maps device locale country code to the corresponding [ZakatCountry]. Falls back to [ZakatCountry.INDONESIA]. */
private fun detectZakatCountry(): ZakatCountry = when (Locale.getDefault().country.uppercase()) {
    "MY" -> ZakatCountry.MALAYSIA
    "SG" -> ZakatCountry.SINGAPORE
    "BN" -> ZakatCountry.BRUNEI
    else -> ZakatCountry.INDONESIA
}

data class ZakatUiState(
    val selectedType: ZakatType = ZakatType.MAAL,
    val cash: String = "",
    val goldGrams: String = "",
    val silverGrams: String = "",
    val investments: String = "",
    val debts: String = "",
    val manualGoldPrice: String = "",
    val familyMembers: String = "",
    val ricePricePerKg: String = "",
    val priceQuote: GoldPriceQuote? = null,
    val priceLoading: Boolean = false,
    val priceError: Boolean = false,
    val result: ZakatCalculationResult? = null,
    /** Country selected by the user (or auto-detected from locale) for the zakat body directory. */
    val selectedZakatCountry: ZakatCountry = detectZakatCountry()
)

@HiltViewModel
class ZakatViewModel @Inject constructor(
    private val goldPriceRepository: GoldPriceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ZakatUiState())
    val state: StateFlow<ZakatUiState> = _state.asStateFlow()

    init {
        refreshPrices()
    }

    fun refreshPrices() {
        viewModelScope.launch {
            _state.update { it.copy(priceLoading = true, priceError = false) }
            val quote = goldPriceRepository.fetchQuote("IDR")
            _state.update {
                it.copy(
                    priceLoading = false,
                    priceQuote = quote,
                    priceError = quote == null
                )
            }
            recompute()
        }
    }

    fun updateType(type: ZakatType) {
        _state.update { it.copy(selectedType = type) }
        recompute()
    }

    fun updateCash(v: String) { _state.update { it.copy(cash = v) }; recompute() }
    fun updateGoldGrams(v: String) { _state.update { it.copy(goldGrams = v) }; recompute() }
    fun updateSilverGrams(v: String) { _state.update { it.copy(silverGrams = v) }; recompute() }
    fun updateInvestments(v: String) { _state.update { it.copy(investments = v) }; recompute() }
    fun updateDebts(v: String) { _state.update { it.copy(debts = v) }; recompute() }
    fun updateManualGoldPrice(v: String) { _state.update { it.copy(manualGoldPrice = v) }; recompute() }
    fun updateFamilyMembers(v: String) { _state.update { it.copy(familyMembers = v) }; recompute() }
    fun updateRicePricePerKg(v: String) { _state.update { it.copy(ricePricePerKg = v) }; recompute() }
    fun updateZakatCountry(country: ZakatCountry) { _state.update { it.copy(selectedZakatCountry = country) } }

    private fun recompute() {
        val s = _state.value
        val result: ZakatCalculationResult? = when (s.selectedType) {
            ZakatType.MAAL -> {
                val goldPrice = s.priceQuote?.goldPerGramIdr?.takeIf { it > 0 } ?: MoneyInputFormatter.parseAmount(s.manualGoldPrice).toDouble()
                if (goldPrice <= 0.0) {
                    null
                } else {
                    val silverPrice = s.priceQuote?.silverPerGramIdr ?: ZakatCalculator.silverPriceFromGold(goldPrice)
                    ZakatCalculator.calculate(
                        cash = MoneyInputFormatter.parseAmount(s.cash).toDouble(),
                        goldGrams = s.goldGrams.toDoubleOrNull() ?: 0.0,
                        silverGrams = s.silverGrams.toDoubleOrNull() ?: 0.0,
                        investments = MoneyInputFormatter.parseAmount(s.investments).toDouble(),
                        debts = MoneyInputFormatter.parseAmount(s.debts).toDouble(),
                        goldPricePerGram = goldPrice,
                        silverPricePerGram = silverPrice
                    )
                }
            }
            ZakatType.FITRAH -> {
                val members = s.familyMembers.toIntOrNull()
                val ricePrice = MoneyInputFormatter.parseAmount(s.ricePricePerKg).toDouble()
                if (members == null || members <= 0 || ricePrice <= 0.0) {
                    null
                } else {
                    ZakatCalculator.calculateFitrah(members, ricePrice)
                }
            }
        }

        _state.update { it.copy(result = result) }
    }
}
