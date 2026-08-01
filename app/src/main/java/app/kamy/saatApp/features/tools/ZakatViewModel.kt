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
import app.kamy.saatApp.domain.tools.ZakatNumberFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore

private fun detectZakatCountry(countryCode: String): ZakatCountry = when (countryCode.uppercase()) {
    "ID" -> ZakatCountry.INDONESIA
    "MY" -> ZakatCountry.MALAYSIA
    "SG" -> ZakatCountry.SINGAPORE
    "BN" -> ZakatCountry.BRUNEI
    "US" -> ZakatCountry.USA
    "GB" -> ZakatCountry.UK
    else -> ZakatCountry.GLOBAL
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
    val userCurrencySymbol: String = "Rp",
    val userCurrencyCode: String = "IDR",
    val selectedZakatCountry: ZakatCountry = ZakatCountry.INDONESIA
)

@HiltViewModel
class ZakatViewModel @Inject constructor(
    private val goldPriceRepository: GoldPriceRepository,
    private val locationPrefs: LocationPreferencesStore
) : ViewModel() {

    private val _state = MutableStateFlow(ZakatUiState())
    val state: StateFlow<ZakatUiState> = _state.asStateFlow()

    init {
        val cCode = detectCountryCode()
        val cSym = resolveCurrencySymbol(cCode)
        val cCur = resolveCurrencyCode(cCode)
        _state.update {
            it.copy(
                userCurrencySymbol = cSym,
                userCurrencyCode = cCur,
                selectedZakatCountry = detectZakatCountry(cCode)
            )
        }
        refreshPrices()
    }

    private fun detectCountryCode(): String {
        return locationPrefs.manualLocation()?.countryCode
            ?: locationPrefs.gpsLocation()?.countryCode
            ?: Locale.getDefault().country
            ?: "ID"
    }

    private fun resolveCurrencyCode(countryCode: String): String = when (countryCode.uppercase()) {
        "ID" -> "IDR"
        "MY" -> "MYR"
        "SG" -> "SGD"
        "BN" -> "BND"
        "US" -> "USD"
        "GB" -> "GBP"
        "JP" -> "JPY"
        "SA" -> "SAR"
        "AE" -> "AED"
        "AU" -> "AUD"
        "CA" -> "CAD"
        else -> try {
            java.util.Currency.getInstance(Locale.Builder().setRegion(countryCode.uppercase()).build()).currencyCode
        } catch (_: Throwable) {
            "IDR"
        }
    }

    private fun resolveCurrencySymbol(countryCode: String): String {
        val currencyCode = resolveCurrencyCode(countryCode)
        return when (currencyCode) {
            "IDR" -> "Rp"
            "MYR" -> "RM"
            "SGD" -> "S$"
            "BND" -> "B$"
            "USD" -> "$"
            "GBP" -> "£"
            "JPY" -> "¥"
            "EUR" -> "€"
            "SAR" -> "SR"
            "AED" -> "AED"
            else -> try {
                java.util.Currency.getInstance(currencyCode).symbol
            } catch (_: Throwable) {
                currencyCode
            }
        }
    }

    fun refreshPrices() {
        viewModelScope.launch {
            _state.update { it.copy(priceLoading = true, priceError = false) }
            val cCode = detectCountryCode()
            val cCur = resolveCurrencyCode(cCode)
            val cSym = resolveCurrencySymbol(cCode)
            val quote = goldPriceRepository.fetchQuote(cCur)
            _state.update {
                it.copy(
                    priceLoading = false,
                    priceQuote = quote,
                    priceError = quote == null,
                    userCurrencySymbol = cSym,
                    userCurrencyCode = cCur
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
                val goldPrice = s.priceQuote?.goldPerGramIdr?.takeIf { it > 0 } ?: ZakatNumberFormatter.parseMoney(s.manualGoldPrice)
                if (goldPrice <= 0.0) {
                    null
                } else {
                    val silverPrice = s.priceQuote?.silverPerGramIdr ?: ZakatCalculator.silverPriceFromGold(goldPrice)
                    ZakatCalculator.calculate(
                        cash = ZakatNumberFormatter.parseMoney(s.cash),
                        goldGrams = ZakatNumberFormatter.parseDecimal(s.goldGrams),
                        silverGrams = ZakatNumberFormatter.parseDecimal(s.silverGrams),
                        investments = ZakatNumberFormatter.parseMoney(s.investments),
                        debts = ZakatNumberFormatter.parseMoney(s.debts),
                        goldPricePerGram = goldPrice,
                        silverPricePerGram = silverPrice
                    )
                }
            }
            ZakatType.FITRAH -> {
                val members = s.familyMembers.toIntOrNull()
                val ricePrice = ZakatNumberFormatter.parseMoney(s.ricePricePerKg)
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
