package app.kamy.saatApp.domain.faraidh

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

enum class FaraidhMadhhab {
    HANAFI,
    MALIKI,
    SHAFII,
    HANBALI;

    /** Hanafi includes spouses in radd; the other three exclude them (jumhur). */
    fun raddIncludesSpouses(): Boolean = this == HANAFI
}

@Serializable
data class FaraidhPropertyItem(
    val id: String,
    val name: String,
    val sizeSqm: String = "",
    val value: String = ""
)

@Serializable
data class EstateAssetInput(
    val cashSavings: String = "",
    val goldJewelry: String = "",
    val goldWeightGrams: String = "",
    val goldPricePerGram: String = "",
    val inputGoldByGrams: Boolean = false,
    val propertyValue: String = "",
    val properties: List<FaraidhPropertyItem> = emptyList(),
    val inputPropertyDetailed: Boolean = false,
    val businessAssets: String = "",
    val otherAssets: String = "",
    val hasResidentialProperty: Boolean = false,
    val propertyNotes: String = "",
    val debts: String = "",
    val funeralCosts: String = "",
    val unpaidZakat: String = "",
    val bequestWasiat: String = ""
)

data class EstateComputation(
    val grossAssets: BigDecimal,
    val cashComponent: BigDecimal,
    val goldComponent: BigDecimal,
    val propertyComponent: BigDecimal,
    val businessComponent: BigDecimal,
    val otherComponent: BigDecimal,
    val funeralCosts: BigDecimal,
    val debts: BigDecimal,
    val unpaidZakat: BigDecimal,
    val afterFuneral: BigDecimal,
    val afterDebts: BigDecimal,
    val afterZakat: BigDecimal,
    val maxWasiat: BigDecimal,
    val wasiatApplied: BigDecimal,
    val netEstate: BigDecimal,
    val hasResidentialProperty: Boolean,
    val propertyNotes: String
)

object FaraidhEstateCalculator {

    fun parseAmount(raw: String): BigDecimal = MoneyInputFormatter.parseAmount(raw)

    fun compute(input: EstateAssetInput): EstateComputation {
        val cash = parseAmount(input.cashSavings)
        val gold = if (input.inputGoldByGrams) {
            val weight = input.goldWeightGrams.replace(',', '.').toBigDecimalOrNull() ?: BigDecimal.ZERO
            val price = parseAmount(input.goldPricePerGram)
            weight.multiply(price).setScale(2, RoundingMode.HALF_UP)
        } else {
            parseAmount(input.goldJewelry)
        }
        val property = if (input.inputPropertyDetailed && input.properties.isNotEmpty()) {
            input.properties.fold(BigDecimal.ZERO) { acc, item ->
                acc.add(parseAmount(item.value))
            }
        } else {
            parseAmount(input.propertyValue)
        }
        val business = parseAmount(input.businessAssets)
        val other = parseAmount(input.otherAssets)
        val funeral = parseAmount(input.funeralCosts).max(BigDecimal.ZERO)
        val debts = parseAmount(input.debts).max(BigDecimal.ZERO)
        val zakat = parseAmount(input.unpaidZakat).max(BigDecimal.ZERO)
        val requestedWasiat = parseAmount(input.bequestWasiat).max(BigDecimal.ZERO)

        val gross = cash.add(gold).add(property).add(business).add(other).max(BigDecimal.ZERO)
        val afterFuneral = gross.subtract(funeral).max(BigDecimal.ZERO)
        val afterDebts = afterFuneral.subtract(debts).max(BigDecimal.ZERO)
        val afterZakat = afterDebts.subtract(zakat).max(BigDecimal.ZERO)
        val maxWasiat = afterZakat.divide(BigDecimal(3), 2, RoundingMode.HALF_UP)
        val wasiatApplied = requestedWasiat.min(maxWasiat)
        val net = afterZakat.subtract(wasiatApplied).max(BigDecimal.ZERO)

        return EstateComputation(
            grossAssets = gross,
            cashComponent = cash,
            goldComponent = gold,
            propertyComponent = property,
            businessComponent = business,
            otherComponent = other,
            funeralCosts = funeral,
            debts = debts,
            unpaidZakat = zakat,
            afterFuneral = afterFuneral,
            afterDebts = afterDebts,
            afterZakat = afterZakat,
            maxWasiat = maxWasiat,
            wasiatApplied = wasiatApplied,
            netEstate = net,
            hasResidentialProperty = input.hasResidentialProperty,
            propertyNotes = input.propertyNotes.trim()
        )
    }
}
