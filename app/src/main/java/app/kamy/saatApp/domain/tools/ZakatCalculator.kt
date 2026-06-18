package app.kamy.saatApp.domain.tools

import kotlin.math.floor
import kotlin.math.min

data class ZakatCalculationResult(
    val zakatableWealth: Double,
    val nisabGoldGrams: Double,
    val nisabSilverGrams: Double,
    val nisabGoldValue: Double,
    val nisabSilverValue: Double,
    val effectiveNisab: Double,
    val zakatDue: Double,
    val meetsNisab: Boolean,
    val usedSilverNisab: Boolean
)

data class GoldPriceQuote(
    val goldPerGramIdr: Double,
    val silverPerGramIdr: Double,
    val sourceLabel: String,
    val fetchedAtMillis: Long
)

object ZakatCalculator {
    const val NISAB_GOLD_GRAMS = 85.0
    const val NISAB_SILVER_GRAMS = 595.0
    const val ZAKAT_RATE = 0.025
    private const val TROY_OZ_GRAMS = 31.1034768

    fun goldUsdPerGram(usdPerTroyOz: Double): Double = usdPerTroyOz / TROY_OZ_GRAMS

    fun calculate(
        cash: Double,
        goldGrams: Double,
        silverGrams: Double,
        investments: Double,
        debts: Double,
        goldPricePerGram: Double,
        silverPricePerGram: Double
    ): ZakatCalculationResult {
        val goldValue = goldGrams * goldPricePerGram
        val silverValue = silverGrams * silverPricePerGram
        val net = (cash + goldValue + silverValue + investments - debts).coerceAtLeast(0.0)
        val nisabGoldValue = NISAB_GOLD_GRAMS * goldPricePerGram
        val nisabSilverValue = NISAB_SILVER_GRAMS * silverPricePerGram
        val effectiveNisab = min(nisabGoldValue, nisabSilverValue)
        val usedSilver = nisabSilverValue < nisabGoldValue
        val meetsNisab = net >= effectiveNisab
        val zakatDue = if (meetsNisab) floor(net * ZAKAT_RATE * 100) / 100.0 else 0.0
        return ZakatCalculationResult(
            zakatableWealth = net,
            nisabGoldGrams = NISAB_GOLD_GRAMS,
            nisabSilverGrams = NISAB_SILVER_GRAMS,
            nisabGoldValue = nisabGoldValue,
            nisabSilverValue = nisabSilverValue,
            effectiveNisab = effectiveNisab,
            zakatDue = zakatDue,
            meetsNisab = meetsNisab,
            usedSilverNisab = usedSilver
        )
    }
}
