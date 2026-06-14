package app.kamy.qalbuApp.features.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.domain.tools.GoldPriceQuote
import app.kamy.qalbuApp.domain.tools.ZakatCalculationResult
import app.kamy.qalbuApp.domain.tools.ZakatCalculator
import app.kamy.qalbuApp.infrastructure.repository.GoldPriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ZakatUiState(
    val cash: String = "",
    val goldGrams: String = "",
    val silverGrams: String = "",
    val investments: String = "",
    val debts: String = "",
    val priceQuote: GoldPriceQuote? = null,
    val priceLoading: Boolean = false,
    val priceError: Boolean = false,
    val result: ZakatCalculationResult? = null
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

    fun updateCash(v: String) { _state.update { it.copy(cash = v) }; recompute() }
    fun updateGoldGrams(v: String) { _state.update { it.copy(goldGrams = v) }; recompute() }
    fun updateSilverGrams(v: String) { _state.update { it.copy(silverGrams = v) }; recompute() }
    fun updateInvestments(v: String) { _state.update { it.copy(investments = v) }; recompute() }
    fun updateDebts(v: String) { _state.update { it.copy(debts = v) }; recompute() }

    private fun recompute() {
        val s = _state.value
        val quote = s.priceQuote ?: return
        _state.update {
            it.copy(
                result = ZakatCalculator.calculate(
                    cash = s.cash.toDoubleOrNull() ?: 0.0,
                    goldGrams = s.goldGrams.toDoubleOrNull() ?: 0.0,
                    silverGrams = s.silverGrams.toDoubleOrNull() ?: 0.0,
                    investments = s.investments.toDoubleOrNull() ?: 0.0,
                    debts = s.debts.toDoubleOrNull() ?: 0.0,
                    goldPricePerGram = quote.goldPerGramIdr,
                    silverPricePerGram = quote.silverPerGramIdr
                )
            )
        }
    }
}
