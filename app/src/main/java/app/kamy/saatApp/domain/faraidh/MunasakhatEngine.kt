package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal

object MunasakhatEngine {

    /**
     * Collapses a recursive Munasakhat chain into a flat map of surviving sub-heirs and their total cash portion.
     * @param primaryResult The primary Faraidh calculation result of the original deceased.
     * @param rootSubTree Nested map of heirs who died, pointing to their sub-heir profile calculations.
     */
    fun calculate(
        primaryResult: FaraidhResult,
        rootSubTree: Map<String, MunasakhatNode>
    ): Map<String, BigDecimal> {
        val finalAllocations = mutableMapOf<String, BigDecimal>()

        // 1. Initialize allocations with primary survivors
        for (share in primaryResult.activeShares) {
            val heirId = share.heirId
            finalAllocations[heirId] = share.cashAmount
        }

        // 2. Cascade down the chain for deceased heirs
        for ((deadHeirId, subTree) in rootSubTree) {
            val inheritedCash = finalAllocations.remove(deadHeirId) ?: BigDecimal.ZERO
            val totalDeceasedEstate = inheritedCash.add(subTree.netPersonalEstate)

            // Execute sub-Faraidh for the deceased heir
            val deceasedGender = if (subTree.input.husbandCount > 0) DeceasedGender.FEMALE else DeceasedGender.MALE
            val subProfile = DeceasedProfile(
                gender = deceasedGender,
                netEstate = totalDeceasedEstate,
                bornOutOfWedlock = false
            )
            val subResult = FaraidhEngine.calculate(subProfile, subTree.input)

            // Merge shares recursively
            mergeSubResult(subResult, subTree.subHeirs, finalAllocations)
        }

        return finalAllocations
    }

    private fun mergeSubResult(
        result: FaraidhResult,
        downstreamSubTrees: Map<String, MunasakhatNode>,
        globalAllocations: MutableMap<String, BigDecimal>
    ) {
        for (share in result.activeShares) {
            val id = share.heirId
            val cash = share.cashAmount

            if (downstreamSubTrees.containsKey(id)) {
                // The sub-heir is also deceased; cascade recursively
                val nestedNode = downstreamSubTrees[id]!!
                val nestedDeceasedEstate = cash.add(nestedNode.netPersonalEstate)
                val nestedGender = if (nestedNode.input.husbandCount > 0) DeceasedGender.FEMALE else DeceasedGender.MALE
                val nestedProfile = DeceasedProfile(
                    gender = nestedGender,
                    netEstate = nestedDeceasedEstate,
                    bornOutOfWedlock = false
                )
                val nestedResult = FaraidhEngine.calculate(nestedProfile, nestedNode.input)
                mergeSubResult(nestedResult, nestedNode.subHeirs, globalAllocations)
            } else {
                // Heir is alive; add to their global total
                val current = globalAllocations[id] ?: BigDecimal.ZERO
                globalAllocations[id] = current.add(cash)
            }
        }
    }
}
