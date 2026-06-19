package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal

object ContingencyEngine {

    data class ContingencyResult(
        val activeDistribution: Map<String, BigDecimal>,
        val frozenReserve: BigDecimal
    )

    /**
     * Executes pregnancy simulation passes to identify the safest amount of capital to freeze in trust.
     */
    fun calculatePregnancyReserve(
        profile: DeceasedProfile,
        baseInput: HeirInput,
        numberOfFetuses: Int = 1
    ): ContingencyResult {
        // Pass 1: Simulate no live births
        val resNoBirth = FaraidhEngine.calculate(profile, baseInput)

        // Pass 2: Simulate fetuses born as Males (Sons)
        val inputMales = baseInput.copy(sonCount = baseInput.sonCount + numberOfFetuses)
        val resMales = FaraidhEngine.calculate(profile, inputMales)

        // Pass 3: Simulate fetuses born as Females (Daughters)
        val inputFemales = baseInput.copy(daughterCount = baseInput.daughterCount + numberOfFetuses)
        val resFemales = FaraidhEngine.calculate(profile, inputFemales)

        // Determine guaranteed minimum shares for each active living heir type
        val activeDistribution = mutableMapOf<String, BigDecimal>()
        val heirTypes = listOf(
            HeirType.HUSBAND, HeirType.WIFE, HeirType.FATHER, HeirType.MOTHER,
            HeirType.SON, HeirType.DAUGHTER, HeirType.GRANDSON, HeirType.GRANDDAUGHTER,
            HeirType.FULL_BROTHER, HeirType.FULL_SISTER, HeirType.PATERNAL_BROTHER,
            HeirType.PATERNAL_SISTER, HeirType.MATERNAL_SIBLING
        )

        for (type in heirTypes) {
            val key = type.name.lowercase()
            val s1 = resNoBirth.activeShares.find { it.type == type }?.cashAmount ?: BigDecimal.ZERO
            val s2 = resMales.activeShares.find { it.type == type }?.cashAmount ?: BigDecimal.ZERO
            val s3 = resFemales.activeShares.find { it.type == type }?.cashAmount ?: BigDecimal.ZERO

            val guaranteed = s1.min(s2).min(s3)
            if (guaranteed > BigDecimal.ZERO) {
                activeDistribution[key] = guaranteed
            }
        }

        val totalDistributed = activeDistribution.values.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }
        val frozenReserve = profile.netEstate.subtract(totalDistributed).max(BigDecimal.ZERO)

        return ContingencyResult(activeDistribution, frozenReserve)
    }

    /**
     * Executes missing person simulation passes.
     */
    fun calculateMissingHeirReserve(
        profile: DeceasedProfile,
        baseInput: HeirInput,
        missingType: HeirType,
        missingCount: Int = 1
    ): ContingencyResult {
        // Pass 1: Simulate assuming the missing person is DEAD (input has no missing person)
        val resDead = FaraidhEngine.calculate(profile, baseInput)

        // Pass 2: Simulate assuming the missing person is ALIVE (input has the missing person added)
        val inputAlive = when (missingType) {
            HeirType.HUSBAND -> baseInput.copy(husbandCount = baseInput.husbandCount + missingCount)
            HeirType.WIFE -> baseInput.copy(wifeCount = baseInput.wifeCount + missingCount)
            HeirType.FATHER -> baseInput.copy(fatherCount = baseInput.fatherCount + missingCount)
            HeirType.MOTHER -> baseInput.copy(motherCount = baseInput.motherCount + missingCount)
            HeirType.SON -> baseInput.copy(sonCount = baseInput.sonCount + missingCount)
            HeirType.DAUGHTER -> baseInput.copy(daughterCount = baseInput.daughterCount + missingCount)
            HeirType.GRANDSON -> baseInput.copy(grandsonCount = baseInput.grandsonCount + missingCount)
            HeirType.GRANDDAUGHTER -> baseInput.copy(granddaughterCount = baseInput.granddaughterCount + missingCount)
            HeirType.FULL_BROTHER -> baseInput.copy(fullBrotherCount = baseInput.fullBrotherCount + missingCount)
            HeirType.FULL_SISTER -> baseInput.copy(fullSisterCount = baseInput.fullSisterCount + missingCount)
            HeirType.PATERNAL_BROTHER -> baseInput.copy(paternalBrotherCount = baseInput.paternalBrotherCount + missingCount)
            HeirType.PATERNAL_SISTER -> baseInput.copy(paternalSisterCount = baseInput.paternalSisterCount + missingCount)
            HeirType.MATERNAL_SIBLING -> baseInput.copy(maternalBrotherCount = baseInput.maternalBrotherCount + missingCount)
            else -> baseInput
        }
        val resAlive = FaraidhEngine.calculate(profile, inputAlive)

        val activeDistribution = mutableMapOf<String, BigDecimal>()
        val heirTypes = listOf(
            HeirType.HUSBAND, HeirType.WIFE, HeirType.FATHER, HeirType.MOTHER,
            HeirType.SON, HeirType.DAUGHTER, HeirType.GRANDSON, HeirType.GRANDDAUGHTER,
            HeirType.FULL_BROTHER, HeirType.FULL_SISTER, HeirType.PATERNAL_BROTHER,
            HeirType.PATERNAL_SISTER, HeirType.MATERNAL_SIBLING
        )

        for (type in heirTypes) {
            // Skip the missing heir from receiving active distribution
            if (type == missingType) continue

            val key = type.name.lowercase()
            val sDead = resDead.activeShares.find { it.type == type }?.cashAmount ?: BigDecimal.ZERO
            val sAlive = resAlive.activeShares.find { it.type == type }?.cashAmount ?: BigDecimal.ZERO

            val guaranteed = sDead.min(sAlive)
            if (guaranteed > BigDecimal.ZERO) {
                activeDistribution[key] = guaranteed
            }
        }

        val totalDistributed = activeDistribution.values.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }
        val frozenReserve = profile.netEstate.subtract(totalDistributed).max(BigDecimal.ZERO)

        return ContingencyResult(activeDistribution, frozenReserve)
    }
}
