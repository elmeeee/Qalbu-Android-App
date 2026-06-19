package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal

enum class DeceasedGender { MALE, FEMALE }

data class DeceasedProfile(
    val gender: DeceasedGender,
    val netEstate: BigDecimal,
    val name: String = "",
    val estate: EstateComputation? = null,
    val madhhab: FaraidhMadhhab = FaraidhMadhhab.SHAFII,
    val bornOutOfWedlock: Boolean = false
)

data class DisqualifiedHeir(
    val type: HeirType,
    val count: Int,
    val reason: BlockingReasonKey
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
    val maternalSisterCount: Int = 0,
    val disqualifiedHeirs: List<DisqualifiedHeir> = emptyList()
) {
    fun hasAnyHeir(): Boolean = listOf(
        husbandCount, wifeCount, fatherCount, motherCount,
        sonCount, daughterCount, grandsonCount, granddaughterCount,
        fullBrotherCount, fullSisterCount,
        paternalBrotherCount, paternalSisterCount,
        maternalBrotherCount, maternalSisterCount
    ).any { it > 0 } || disqualifiedHeirs.isNotEmpty()
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
    MATERNAL_SIBLING,
    STEP_CHILD,
    UNBORN_FETUS
}

enum class BlockingReasonKey {
    BY_SON,
    BY_CHILDREN,
    BY_FATHER,
    BY_GRANDCHILDREN_SUBSTITUTE,
    GENDER_MISMATCH,
    NO_SHARE_REMAINDER,
    OUT_OF_WEDLOCK,
    HOMICIDE,
    DIFFERENCE_OF_RELIGION,
    SIMULTANEOUS_DEATH
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
    val proofKeys: List<String>,
    val heirId: String = type.name.lowercase()
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
    val displayName: String = "",
    val shareFraction: String? = null,
    val sharePercentage: String? = null,
    val shareAmount: BigDecimal? = null
)

enum class GraphNodeStatus {
    ACTIVE,
    MAHJUB_NUQSAN,
    MAHJUB_HIRMAN,
    EXCLUDED_BY_LAW,
    FROZEN_RESERVE
}

data class FaraidhGraphNode(
    val id: String,
    val displayName: String,
    val generationLevel: Int,
    val relationType: HeirType?,
    val status: GraphNodeStatus,
    val marriageLinkId: String? = null,
    val baseShareFraction: String? = null,
    val finalPercentage: Double = 0.0,
    val cashValue: BigDecimal = BigDecimal.ZERO,
    val disqualificationReasonId: String? = null,
    val visualColorHex: String
)

enum class GraphEdgeType {
    PARENT_CHILD,
    MARRIAGE,
    MOTHER_ONLY_MATERNAL
}

data class FaraidhGraphEdge(
    val fromId: String,
    val toId: String,
    val type: GraphEdgeType
)

data class FaraidhFamilyGraph(
    val rootDeceasedId: String,
    val nodes: Map<String, FaraidhGraphNode>,
    val edges: List<FaraidhGraphEdge>
)

data class MunasakhatNode(
    val deceasedId: String,
    val deceasedName: String,
    val netPersonalEstate: BigDecimal,
    val input: HeirInput,
    val subHeirs: Map<String, MunasakhatNode> = emptyMap()
)

data class ContingencyInput(
    val fetusCount: Int = 0,
    val missingHeirType: HeirType? = null,
    val missingHeirCount: Int = 0
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
    val madhhabNoteKey: String? = null,
    val familyGraph: FaraidhFamilyGraph? = null
)
