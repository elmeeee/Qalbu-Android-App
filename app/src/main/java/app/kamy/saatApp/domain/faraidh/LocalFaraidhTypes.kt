package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal

enum class RelativeType {
    HUSBAND,
    WIFE,
    FATHER,
    GRANDFATHER,
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

data class RelativeInput(
    val type: RelativeType,
    val count: Int = 0,
    val isAlive: Boolean = true,
    val isMuslim: Boolean = true,
    val isBornInWedlock: Boolean = true,
    val isSimultaneousDeath: Boolean = false,
    val isMafqud: Boolean = false,
    val isMurderer: Boolean = false,
    val isWaladZina: Boolean = false,
    val isJanin: Boolean = false
)

enum class BlockReason {
    BY_SON,
    BY_FATHER,
    BY_GRANDFATHER,
    BY_CHILDREN,
    NO_SHARE,
    INVALID_RELATION,
    OUT_OF_WEDLOCK,
    MURDER,
    SIMULTANEOUS_DEATH,
    MAFQUD
}

data class BlockedReason(
    val type: RelativeType,
    val count: Int,
    val reason: BlockReason
)

data class LocalHeirShare(
    val type: RelativeType,
    val heads: Int,
    val fraction: FaraidhFraction,
    val isAsabah: Boolean,
    val proofKeys: List<String> = emptyList()
)

data class LocalFaraidhResult(
    val shares: List<LocalHeirShare>,
    val blockedReasons: List<BlockedReason>,
    val ruleCitations: List<String> = emptyList(),
    val notes: List<String> = emptyList()
)

data class LocalEngineContext(
    val hasChildren: Boolean,
    val hasGrandchildren: Boolean,
    val hasFather: Boolean,
    val hasGrandfather: Boolean,
    val hasMother: Boolean,
    val hasSiblings: Boolean,
    val hasJanin: Boolean,
    val hasMafqud: Boolean,
    val simultaneousDeath: Boolean,
    val waladZina: Boolean,
    val ruleCitations: List<String> = emptyList()
) {
    fun hasTwoOrMoreSiblings() = hasSiblings && !hasChildren && !hasFather && !hasGrandfather
}

fun List<RelativeInput>.countOf(type: RelativeType): Int =
    filter { it.type == type }.sumOf { it.count }
