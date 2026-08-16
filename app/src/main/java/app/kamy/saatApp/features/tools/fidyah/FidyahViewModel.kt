package app.kamy.saatApp.features.tools.fidyah

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.domain.model.FidyahCalculationResult
import app.kamy.saatApp.domain.model.FidyahMadhhab
import app.kamy.saatApp.domain.model.FidyahReason
import app.kamy.saatApp.domain.model.FidyahRecord
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.FidyahStore
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Currency
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class FidyahUiState(
    val selectedMadhhab: FidyahMadhhab = FidyahMadhhab.SYAFII,
    val selectedReason: FidyahReason = FidyahReason.ELDERLY_CHRONIC,
    val missedDays: Int = 10,
    val delayedYears: Int = 1,
    val pricePerDay: Double = 45000.0,
    val userCurrencySymbol: String = "Rp",
    val userCurrencyCode: String = "IDR",
    val selectedHijriYear: String = "1447 H",
    val calculationResult: FidyahCalculationResult? = null,
    val records: List<FidyahRecord> = emptyList(),
    val showDuaDialog: Boolean = false
)

@HiltViewModel
class FidyahViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val fidyahStore = FidyahStore.from(application)
    private val locationStore = LocationPreferencesStore.from(application)

    private val _uiState = MutableStateFlow(FidyahUiState())
    val uiState: StateFlow<FidyahUiState> = _uiState.asStateFlow()

    init {
        resolveCurrencyAndDefaults()
        loadRecords()
    }

    private fun resolveCurrencyAndDefaults() {
        viewModelScope.launch {
            val savedMadhhab = fidyahStore.getPreferredMadhhab()
            val countryCode = locationStore.manualLocation()?.countryCode ?: Locale.getDefault().country
            val cCode = resolveCurrencyCode(countryCode)
            val cSym = resolveCurrencySymbol(countryCode)
            val defaultPrice = defaultPriceForCurrency(cCode)
            val customPrice = fidyahStore.getCustomStaplePrice() ?: defaultPrice

            _uiState.update { state ->
                state.copy(
                    selectedMadhhab = savedMadhhab,
                    userCurrencyCode = cCode,
                    userCurrencySymbol = cSym,
                    pricePerDay = customPrice
                )
            }
            recalculate()
        }
    }

    fun loadRecords() {
        val list = fidyahStore.getRecords()
        _uiState.update { it.copy(records = list) }
        app.kamy.saatApp.infrastructure.notifications.FidyahReminderScheduler.scheduleWeeklyReminder(getApplication())
    }

    fun setMadhhab(madhhab: FidyahMadhhab) {
        fidyahStore.setPreferredMadhhab(madhhab)
        _uiState.update { it.copy(selectedMadhhab = madhhab) }
        recalculate()
    }

    fun setReason(reason: FidyahReason) {
        _uiState.update { it.copy(selectedReason = reason) }
        recalculate()
    }

    fun setMissedDays(days: Int) {
        val validDays = days.coerceIn(1, 365)
        _uiState.update { it.copy(missedDays = validDays) }
        recalculate()
    }

    fun setDelayedYears(years: Int) {
        val validYears = years.coerceIn(1, 50)
        _uiState.update { it.copy(delayedYears = validYears) }
        recalculate()
    }

    fun setPricePerDay(price: Double) {
        val validPrice = price.coerceAtLeast(0.0)
        fidyahStore.setCustomStaplePrice(validPrice)
        _uiState.update { it.copy(pricePerDay = validPrice) }
        recalculate()
    }

    fun setHijriYear(year: String) {
        _uiState.update { it.copy(selectedHijriYear = year) }
    }

    fun setShowDuaDialog(show: Boolean) {
        _uiState.update { it.copy(showDuaDialog = show) }
    }

    fun saveCurrentCalculation() {
        val res = _uiState.value.calculationResult ?: return
        val totalAmount = res.totalFidyahDaysMultiplier * _uiState.value.pricePerDay
        val record = FidyahRecord(
            id = UUID.randomUUID().toString(),
            hijriYear = _uiState.value.selectedHijriYear,
            reason = _uiState.value.selectedReason,
            madhhab = _uiState.value.selectedMadhhab,
            missedDays = _uiState.value.missedDays,
            delayedYears = _uiState.value.delayedYears,
            paidDays = if (res.isFidyahRequired) 0 else _uiState.value.missedDays,
            amountPaid = 0.0,
            currencySymbol = _uiState.value.userCurrencySymbol,
            isFullyPaid = false,
            updatedAtMillis = System.currentTimeMillis()
        )
        fidyahStore.saveRecord(record)
        loadRecords()
    }

    fun toggleRecordPaid(record: FidyahRecord) {
        val updated = record.copy(
            isFullyPaid = !record.isFullyPaid,
            paidDays = if (!record.isFullyPaid) record.missedDays else 0,
            amountPaid = if (!record.isFullyPaid) record.missedDays * record.delayedYears * _uiState.value.pricePerDay else 0.0,
            updatedAtMillis = System.currentTimeMillis()
        )
        fidyahStore.saveRecord(updated)
        loadRecords()
    }

    fun incrementQadhaDay(record: FidyahRecord) {
        val nextCompleted = (record.completedQadhaDays + 1).coerceAtMost(record.missedDays)
        val isDone = nextCompleted >= record.missedDays
        val updated = record.copy(
            completedQadhaDays = nextCompleted,
            isQadhaCompleted = isDone,
            updatedAtMillis = System.currentTimeMillis()
        )
        fidyahStore.saveRecord(updated)
        loadRecords()
    }

    fun toggleQadhaCompleted(record: FidyahRecord) {
        val isNowCompleted = !record.isQadhaCompleted
        val updated = record.copy(
            isQadhaCompleted = isNowCompleted,
            completedQadhaDays = if (isNowCompleted) record.missedDays else 0,
            updatedAtMillis = System.currentTimeMillis()
        )
        fidyahStore.saveRecord(updated)
        loadRecords()
    }

    fun deleteRecord(recordId: String) {
        fidyahStore.deleteRecord(recordId)
        loadRecords()
    }

    private fun recalculate() {
        val state = _uiState.value
        val madhhab = state.selectedMadhhab
        val reason = state.selectedReason
        val days = state.missedDays
        val years = state.delayedYears

        var isRequired = true
        var multiplier = 1
        var qadhaDays = 0
        var explanationId = ""
        var explanationMs = ""
        var explanationEn = ""

        if (reason == FidyahReason.HAID_NIFAS || reason == FidyahReason.SICK_TEMPORARY || reason == FidyahReason.TRAVELER_MUSAFIR) {
            isRequired = false
            multiplier = 0
            qadhaDays = days
            explanationId = "Wajib me-Qadha puasa sebanyak $days hari. Tidak ada kewajiban membayar Fidyah."
            explanationMs = "Wajib meng-Qada puasa sebanyak $days hari. Tiada kewajipan membayar Fidyah."
            explanationEn = "Required to make up (Qadha) $days fast days. No Fidyah required."
            val result = FidyahCalculationResult(
                madhhab = madhhab,
                reason = reason,
                missedDays = days,
                delayedYears = years,
                fidyahDaysCount = days,
                totalFidyahDaysMultiplier = 0,
                riceWeightKg = 0.0,
                requiredQadhaDays = qadhaDays,
                isFidyahRequired = false,
                fiqhExplanationId = explanationId,
                fiqhExplanationMs = explanationMs,
                fiqhExplanationEn = explanationEn
            )
            _uiState.update { it.copy(calculationResult = result) }
            return
        }

        when (madhhab) {
            FidyahMadhhab.SYAFII -> {
                when (reason) {
                    FidyahReason.ELDERLY_CHRONIC -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = 0
                        explanationId = "Menurut Mazhab Syafi'i: Wajib membayar fidyah 1 Mud (0,75 kg) per hari puasa yang ditinggalkan. Tidak ada kewajiban Qadha."
                        explanationMs = "Mengikut Mazhab Syafi'i: Wajib membayar fidyah 1 Mud (0,75 kg) per hari puasa yang ditinggalkan. Tiada kewajipan Qada."
                        explanationEn = "According to the Shafi'i school: Obligatory to pay 1 Mud (0.75 kg) fidyah per missed fast day. No Qadha required."
                    }
                    FidyahReason.PREGNANT_NURSING_CHILD -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Syafi'i: Wajib membayar fidyah 1 Mud per hari DAN wajib me-Qadha puasa karena mengkhawatirkan bayi."
                        explanationMs = "Mengikut Mazhab Syafi'i: Wajib membayar fidyah 1 Mud per hari DAN wajib meng-Qada puasa kerana mengkhawatirkan anak."
                        explanationEn = "According to the Shafi'i school: Obligatory to pay 1 Mud fidyah per day AND make up (Qadha) the fast due to fearing for the child."
                    }
                    FidyahReason.PREGNANT_NURSING_SELF -> {
                        isRequired = false
                        multiplier = 0
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Syafi'i: Wajib Qadha puasa saja (Tanpa Fidyah) karena khawatir terhadap diri sendiri."
                        explanationMs = "Mengikut Mazhab Syafi'i: Wajib meng-Qada puasa sahaja (Tanpa Fidyah) kerana mengkhawatirkan diri sendiri."
                        explanationEn = "According to the Shafi'i school: Qadha fast only (No Fidyah) due to fearing for oneself."
                    }
                    FidyahReason.LATE_QADHA -> {
                        isRequired = true
                        multiplier = years
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Syafi'i: Fidyah melipat ganda (1 Mud × $years tahun keterlambatan × $days hari) + wajib Qadha puasa."
                        explanationMs = "Mengikut Mazhab Syafi'i: Fidyah gandaan (1 Mud × $years tahun kelewatan × $days hari) + wajib Qada puasa."
                        explanationEn = "According to the Shafi'i school: Fidyah multiplies (1 Mud × $years delayed years × $days days) + Qadha fast required."
                    }
                    FidyahReason.DECEASED_BY_HEIR -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = 0
                        explanationId = "Menurut Mazhab Syafi'i: Ahli waris mengeluarkan fidyah 1 Mud per hari utang puasa almarhum/ah."
                        explanationMs = "Mengikut Mazhab Syafi'i: Waris mengeluarkan fidyah 1 Mud per hari hutang puasa arwah."
                        explanationEn = "According to the Shafi'i school: Heirs pay 1 Mud fidyah per missed fast day of the deceased."
                    }
                    else -> {}
                }
            }
            FidyahMadhhab.HANAFI -> {
                when (reason) {
                    FidyahReason.ELDERLY_CHRONIC -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = 0
                        explanationId = "Menurut Mazhab Hanafi: Wajib bayar fidyah 1 Sa' gandum / 1/2 Sa' kurma atau berupa nominal uang tunai per hari."
                        explanationMs = "Mengikut Mazhab Hanafi: Wajib bayar fidyah 1 Sa' gandum / 1/2 Sa' kurma atau nilai wang tunai per hari."
                        explanationEn = "According to the Hanafi school: Obligatory to pay 1 Sa' wheat / 1/2 Sa' dates or equivalent cash value per day."
                    }
                    FidyahReason.PREGNANT_NURSING_CHILD, FidyahReason.PREGNANT_NURSING_SELF -> {
                        isRequired = false
                        multiplier = 0
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Hanafi: Ibu hamil/menyusui HANYA wajib Qadha puasa saja, TANPA Fidyah."
                        explanationMs = "Mengikut Mazhab Hanafi: Ibu hamil/menyusukan HANYA wajib meng-Qada puasa sahaja, TANPA Fidyah."
                        explanationEn = "According to the Hanafi school: Pregnant/nursing mothers ONLY need to make up (Qadha) fast, NO Fidyah required."
                    }
                    FidyahReason.LATE_QADHA -> {
                        isRequired = false
                        multiplier = 0
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Hanafi: Keterlambatan Qadha menyeberang tahun HANYA wajib Qadha saja, TANPA Fidyah dan tanpa penggandaan."
                        explanationMs = "Mengikut Mazhab Hanafi: Kelewatan Qada melampaui tahun HANYA wajib Qada sahaja, TANPA Fidyah."
                        explanationEn = "According to the Hanafi school: Delayed Qadha across years ONLY requires Qadha, NO Fidyah or year multiplier."
                    }
                    FidyahReason.DECEASED_BY_HEIR -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = 0
                        explanationId = "Menurut Mazhab Hanafi: Fidyah diselesaikan dari harta peninggalan almarhum jika diwasiatkan."
                        explanationMs = "Mengikut Mazhab Hanafi: Fidyah diselesaikan daripada harta peninggalan arwah jika diwasiatkan."
                        explanationEn = "According to the Hanafi school: Fidyah is settled from the estate of the deceased if bequeathed."
                    }
                    else -> {}
                }
            }
            FidyahMadhhab.MALIKI -> {
                when (reason) {
                    FidyahReason.ELDERLY_CHRONIC -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = 0
                        explanationId = "Menurut Mazhab Maliki: Sunnah Muakkadah (Sangat dianjurkan) membayar fidyah 1 Mud per hari bagi lansia/sakit menahun."
                        explanationMs = "Mengikut Mazhab Maliki: Sunnah Muakkad membayar fidyah 1 Mud per hari bagi warga emas/sakit berpanjangan."
                        explanationEn = "According to the Maliki school: Highly recommended (Mustahabb) to pay 1 Mud fidyah per day for elderly/chronic illness."
                    }
                    FidyahReason.PREGNANT_NURSING_CHILD -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Maliki: Ibu menyusui (khawatir anak) wajib Fidyah 1 Mud + Qadha. Ibu hamil HANYA wajib Qadha saja."
                        explanationMs = "Mengikut Mazhab Maliki: Ibu menyusukan wajib Fidyah 1 Mud + Qada. Ibu hamil HANYA wajib Qada sahaja."
                        explanationEn = "According to the Maliki school: Nursing mother (fearing child) pays 1 Mud Fidyah + Qadha. Pregnant mother Qadha only."
                    }
                    FidyahReason.PREGNANT_NURSING_SELF -> {
                        isRequired = false
                        multiplier = 0
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Maliki: Wajib Qadha puasa saja, TANPA Fidyah."
                        explanationMs = "Mengikut Mazhab Maliki: Wajib meng-Qada puasa sahaja, TANPA Fidyah."
                        explanationEn = "According to the Maliki school: Qadha fast only, NO Fidyah."
                    }
                    FidyahReason.LATE_QADHA -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Maliki: Wajib Fidyah 1 Mud per hari + Qadha puasa (Fidyah TIDAK melipat ganda walau terlambat bertahun-tahun)."
                        explanationMs = "Mengikut Mazhab Maliki: Wajib Fidyah 1 Mud per hari + Qada puasa (Fidyah TIDAK berganda walau lewat bertahun-tahun)."
                        explanationEn = "According to the Maliki school: 1 Mud Fidyah per day + Qadha fast (Fidyah DOES NOT multiply across delayed years)."
                    }
                    FidyahReason.DECEASED_BY_HEIR -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = 0
                        explanationId = "Menurut Mazhab Maliki: Fidyah 1 Mud per hari dibayarkan dari harta peninggalan almarhum."
                        explanationMs = "Mengikut Mazhab Maliki: Fidyah 1 Mud per hari dibayarkan daripada harta arwah."
                        explanationEn = "According to the Maliki school: 1 Mud Fidyah per day paid from the deceased's estate."
                    }
                    else -> {}
                }
            }
            FidyahMadhhab.HANBALI -> {
                when (reason) {
                    FidyahReason.ELDERLY_CHRONIC -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = 0
                        explanationId = "Menurut Mazhab Hanbali: Wajib membayar fidyah 1 Mud gandum / 1/2 Sa' makanan pokok per hari. Tanpa Qadha."
                        explanationMs = "Mengikut Mazhab Hanbali: Wajib membayar fidyah 1 Mud gandum / 1/2 Sa' per hari. Tanpa Qada."
                        explanationEn = "According to the Hanbali school: Obligatory to pay 1 Mud wheat / 1/2 Sa' staple food per day. No Qadha."
                    }
                    FidyahReason.PREGNANT_NURSING_CHILD -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Hanbali: Ibu hamil/menyusui (khawatir anak) wajib Fidyah 1 Mud per hari DAN Qadha puasa."
                        explanationMs = "Mengikut Mazhab Hanbali: Ibu hamil/menyusukan (khawatirkan anak) wajib Fidyah 1 Mud per hari DAN Qada puasa."
                        explanationEn = "According to the Hanbali school: Pregnant/nursing mother (fearing child) pays 1 Mud Fidyah per day AND Qadha fast."
                    }
                    FidyahReason.PREGNANT_NURSING_SELF -> {
                        isRequired = false
                        multiplier = 0
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Hanbali: Wajib Qadha puasa saja, TANPA Fidyah."
                        explanationMs = "Mengikut Mazhab Hanbali: Wajib meng-Qada puasa sahaja, TANPA Fidyah."
                        explanationEn = "According to the Hanbali school: Qadha fast only, NO Fidyah."
                    }
                    FidyahReason.LATE_QADHA -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = days
                        explanationId = "Menurut Mazhab Hanbali: Wajib Fidyah 1 Mud per hari + Qadha puasa (Fidyah TIDAK melipat ganda walau lewat bertahun-tahun)."
                        explanationMs = "Mengikut Mazhab Hanbali: Wajib Fidyah 1 Mud per hari + Qada puasa (Fidyah TIDAK berganda walau lewat bertahun-tahun)."
                        explanationEn = "According to the Hanbali school: 1 Mud Fidyah per day + Qadha fast (Fidyah DOES NOT multiply across delayed years)."
                    }
                    FidyahReason.DECEASED_BY_HEIR -> {
                        isRequired = true
                        multiplier = 1
                        qadhaDays = 0
                        explanationId = "Menurut Mazhab Hanbali: Ahli waris mengeluarkan fidyah 1 Mud per hari puasa yang ditinggalkan almarhum."
                        explanationMs = "Mengikut Mazhab Hanbali: Waris mengeluarkan fidyah 1 Mud per hari puasa arwah."
                        explanationEn = "According to the Hanbali school: Heirs pay 1 Mud Fidyah per missed fast day of the deceased."
                    }
                    else -> {}
                }
            }
        }

        val totalDaysMultiplier = if (isRequired) days * multiplier else 0
        val riceKg = totalDaysMultiplier * 0.75

        val result = FidyahCalculationResult(
            madhhab = madhhab,
            reason = reason,
            missedDays = days,
            delayedYears = years,
            fidyahDaysCount = days,
            totalFidyahDaysMultiplier = totalDaysMultiplier,
            riceWeightKg = riceKg,
            requiredQadhaDays = qadhaDays,
            isFidyahRequired = isRequired,
            fiqhExplanationId = explanationId,
            fiqhExplanationMs = explanationMs,
            fiqhExplanationEn = explanationEn
        )

        _uiState.update { it.copy(calculationResult = result) }
    }

    private fun resolveCurrencyCode(countryCode: String): String = when (countryCode.uppercase()) {
        "ID" -> "IDR"
        "MY" -> "MYR"
        "SG" -> "SGD"
        "BN" -> "BND"
        "US", "GB", "CA", "AU" -> "USD"
        else -> runCatching {
            Currency.getInstance(Locale.Builder().setRegion(countryCode.uppercase()).build()).currencyCode
        }.getOrDefault("IDR")
    }

    private fun resolveCurrencySymbol(countryCode: String): String {
        val currencyCode = resolveCurrencyCode(countryCode)
        return when (currencyCode) {
            "IDR" -> "Rp"
            "MYR" -> "RM"
            "SGD" -> "S$"
            "BND" -> "B$"
            "USD" -> "$"
            else -> runCatching { Currency.getInstance(currencyCode).symbol }.getOrDefault(currencyCode)
        }
    }

    private fun defaultPriceForCurrency(currencyCode: String): Double = when (currencyCode) {
        "IDR" -> 45000.0
        "MYR" -> 7.0
        "SGD" -> 7.50
        "BND" -> 7.00
        "USD" -> 5.00
        else -> 45000.0
    }
}
