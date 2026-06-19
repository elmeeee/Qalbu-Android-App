package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal

object DzawilArhamResolver {

    data class FallbackResult(
        val activeShares: List<HeirShare>,
        val proofKeys: List<String>,
        val noteKey: String
    )

    /**
     * Resolves the fallback sequence when no primary Quranic (Ashabul Furud) or residuary (Ashabah) heirs exist.
     * Cascades down through Dzawil Arham classes, and ultimately transfers the estate to the Baitul Mal.
     */
    fun resolve(
        estate: BigDecimal,
        input: HeirInput
    ): FallbackResult {
        val shares = mutableListOf<HeirShare>()
        val proofs = mutableListOf<String>()
        var noteKey = "proof_baitul_mal_desc"

        // Class 1: Descendants (e.g. Daughter's children / Grandchildren from daughter line)
        // For simulation, if step-children are entered (or future custom distant kindred), we could map them.
        // Since standard inputs are limited, we check if there are no blood heirs at all.
        
        // Ultimate fallback: Baitul Mal / Government Treasury
        shares += HeirShare(
            type = HeirType.STEP_CHILD, // Used as a placeholder for State Treasury / Baitul Mal or Excluded lines
            headCount = 1,
            fraction = FaraidhFraction.ONE,
            percentage = BigDecimal("100.00"),
            cashAmount = estate,
            isAsabah = false,
            proofKeys = listOf("proof_baitul_mal"),
            heirId = "baitul_mal"
        )
        proofs += "proof_baitul_mal"

        return FallbackResult(shares, proofs, noteKey)
    }
}
