package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal

enum class DeceasedGender { MALE, FEMALE }

data class DeceasedProfile(
    val gender: DeceasedGender,
    val netEstate: BigDecimal,
    val name: String = "",
    val estate: EstateComputation? = null,
    val madhhab: FaraidhMadhhab = FaraidhMadhhab.SHAFII
)

data class HeirInput(
    val husbandCount: Int = 0,
    val wifeCount: Int = 0,
    val fatherCount: Int = 0,
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
    val maternalSisterCount: Int = 0
) {
    fun hasAnyHeir(): Boolean = listOf(
        husbandCount, wifeCount, fatherCount, motherCount,
        sonCount, daughterCount, grandsonCount, granddaughterCount,
        fullBrotherCount, fullSisterCount,
        paternalBrotherCount, paternalSisterCount,
        maternalBrotherCount, maternalSisterCount
    ).any { it > 0 }
}

enum class HeirType {
    HUSBAND,
    WIFE,
    FATHER,
    MOTHER,
    SON,
    DAUGHTER,
    GRANDSON,
    GRANDDAUGHTER,
    FULL_BROTHER,
    FULL_SISTER,
    PATERNAL_BROTHER,
    PATERNAL_SISTER,
    MATERNAL_SIBLING
}

enum class BlockingReasonKey {
    BY_SON,
    BY_CHILDREN,
    BY_FATHER,
    BY_GRANDCHILDREN_SUBSTITUTE,
    GENDER_MISMATCH,
    NO_SHARE_REMAINDER
}

enum class FaraidhAdjustment {
    NONE,
    AWL,
    RADD
}

data class HeirShare(
    val type: HeirType,
    val headCount: Int,
    val fraction: FaraidhFraction,
    val percentage: BigDecimal,
    val cashAmount: BigDecimal,
    val isAsabah: Boolean,
    val proofKeys: List<String>
)

data class BlockedHeir(
    val type: HeirType,
    val headCount: Int,
    val reason: BlockingReasonKey
)

data class SilsilahNode(
    val id: String,
    val parentNodeId: String?,
    val generationLevel: Int,
    val type: HeirType,
    val labelKey: String,
    val headCount: Int,
    val inherits: Boolean,
    val blocked: Boolean,
    val blockReason: BlockingReasonKey?,
    val displayName: String = ""
)

data class FaraidhResult(
    val deceased: DeceasedProfile,
    val input: HeirInput,
    val activeShares: List<HeirShare>,
    val blockedHeirs: List<BlockedHeir>,
    val silsilah: List<SilsilahNode>,
    val adjustment: FaraidhAdjustment,
    val adjustmentNoteKey: String?,
    val proofKeys: List<String>,
    val totalDistributed: BigDecimal,
    val remainderFraction: FaraidhFraction,
    val madhhab: FaraidhMadhhab = FaraidhMadhhab.SHAFII,
    val madhhabNoteKey: String? = null
)
