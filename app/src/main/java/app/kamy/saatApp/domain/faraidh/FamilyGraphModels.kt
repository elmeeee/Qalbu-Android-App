package app.kamy.saatApp.domain.faraidh

data class FamilyNode(
    val id: String,
    val type: RelativeType,
    val displayName: String,
    val generation: Int,
    val status: FamilyStatus,
    val shareFraction: String,
    val sharePercentage: Double,
    val hexColor: String,
    val blockedReason: BlockReason? = null,
    val isActive: Boolean = true
)

enum class FamilyStatus {
    ACTIVE,
    BLOCKED,
    EXCLUDED,
    UNKNOWN
}

data class FamilyEdge(
    val fromId: String,
    val toId: String,
    val relationship: RelationType,
    val lineColor: String = "#B0BEC5",
    val thickness: Float = 1f
)

enum class RelationType {
    PARENT,
    CHILD,
    SIBLING,
    SPOUSE,
    DESCENDANT,
    MISC
}

object FamilyGraphBuilder {
    fun build(
        shares: List<LocalHeirShare>,
        blocked: List<BlockedReason>,
        displayNames: Map<RelativeType, String>
    ): Pair<List<FamilyNode>, List<FamilyEdge>> {
        val nodes = shares.map { share ->
            FamilyNode(
                id = share.type.name,
                type = share.type,
                displayName = displayNames[share.type].orEmpty(),
                generation = generationLevel(share.type),
                status = if (blocked.any { it.type == share.type }) FamilyStatus.BLOCKED else FamilyStatus.ACTIVE,
                shareFraction = share.fraction.toDisplayString(),
                sharePercentage = share.fraction.toPercentage().toDouble(),
                hexColor = nodeColor(share.type, blocked),
                blockedReason = blocked.firstOrNull { it.type == share.type }?.reason,
                isActive = blocked.none { it.type == share.type }
            )
        }
        val edges = nodes.flatMap { node -> parentEdges(node, nodes) }
        return nodes to edges
    }

    private fun generationLevel(type: RelativeType): Int = when (type) {
        RelativeType.GRANDFATHER -> -1
        RelativeType.FATHER, RelativeType.MOTHER -> 0
        RelativeType.HUSBAND, RelativeType.WIFE -> 0
        RelativeType.SON, RelativeType.DAUGHTER,
        RelativeType.FULL_BROTHER, RelativeType.FULL_SISTER,
        RelativeType.PATERNAL_BROTHER, RelativeType.PATERNAL_SISTER,
        RelativeType.MATERNAL_SIBLING, RelativeType.UNBORN_FETUS -> 1
        RelativeType.GRANDSON, RelativeType.GRANDDAUGHTER -> 2
        RelativeType.STEP_CHILD -> 1
    }

    private fun nodeColor(type: RelativeType, blocked: List<BlockedReason>): String {
        if (blocked.any { it.type == type }) return "#D32F2F"
        return when (type) {
            RelativeType.HUSBAND, RelativeType.WIFE -> "#0D9488"
            RelativeType.FATHER, RelativeType.MOTHER -> "#2563EB"
            RelativeType.SON, RelativeType.DAUGHTER -> "#4CAF50"
            RelativeType.GRANDSON, RelativeType.GRANDDAUGHTER -> "#7C4DFF"
            else -> "#64748B"
        }
    }

    private fun parentEdges(node: FamilyNode, nodes: List<FamilyNode>): List<FamilyEdge> = when (node.type) {
        RelativeType.SON, RelativeType.DAUGHTER -> nodes.find { it.type == RelativeType.FATHER }
            ?.let { listOf(FamilyEdge(it.id, node.id, RelationType.PARENT)) } ?: emptyList()
        RelativeType.GRANDSON, RelativeType.GRANDDAUGHTER -> nodes.find { it.type == RelativeType.FATHER }
            ?.let { listOf(FamilyEdge(it.id, node.id, RelationType.PARENT)) } ?: emptyList()
        else -> emptyList()
    }
}
