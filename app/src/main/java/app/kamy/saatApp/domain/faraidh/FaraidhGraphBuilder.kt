package app.kamy.saatApp.domain.faraidh

import java.math.BigDecimal

object FaraidhGraphBuilder {

    fun build(
        profile: DeceasedProfile,
        input: HeirInput,
        activeShares: List<HeirShare>,
        blockedHeirs: List<BlockedHeir>
    ): FaraidhFamilyGraph {
        val nodes = mutableMapOf<String, FaraidhGraphNode>()
        val edges = mutableListOf<FaraidhGraphEdge>()

        val rootId = "deceased_root"
        val deceasedName = profile.name.ifBlank { "Deceased" }
        nodes[rootId] = FaraidhGraphNode(
            id = rootId,
            displayName = deceasedName,
            generationLevel = 0,
            relationType = null,
            status = GraphNodeStatus.ACTIVE,
            visualColorHex = "#1E293B"
        )

        // 1. Spouses (Generation 0)
        if (input.husbandCount > 0) {
            val id = "husband_1"
            val share = activeShares.find { it.type == HeirType.HUSBAND }
            nodes[id] = FaraidhGraphNode(
                id = id,
                displayName = "Husband",
                generationLevel = 0,
                relationType = HeirType.HUSBAND,
                status = if (share != null) GraphNodeStatus.ACTIVE else GraphNodeStatus.MAHJUB_HIRMAN,
                baseShareFraction = share?.fraction?.toDisplayString(),
                finalPercentage = share?.percentage?.toDouble() ?: 0.0,
                cashValue = share?.cashAmount ?: BigDecimal.ZERO,
                visualColorHex = if (share != null) "#10B981" else "#EF4444"
            )
            edges.add(FaraidhGraphEdge(rootId, id, GraphEdgeType.MARRIAGE))
        }

        for (i in 1..input.wifeCount) {
            val id = "wife_$i"
            val share = activeShares.find { it.type == HeirType.WIFE }
            nodes[id] = FaraidhGraphNode(
                id = id,
                displayName = "Wife $i",
                generationLevel = 0,
                relationType = HeirType.WIFE,
                status = if (share != null) GraphNodeStatus.ACTIVE else GraphNodeStatus.MAHJUB_HIRMAN,
                baseShareFraction = share?.fraction?.toDisplayString(),
                finalPercentage = (share?.percentage?.toDouble() ?: 0.0) / input.wifeCount,
                cashValue = (share?.cashAmount ?: BigDecimal.ZERO).divide(BigDecimal(input.wifeCount), 2, java.math.RoundingMode.HALF_UP),
                visualColorHex = if (share != null) "#10B981" else "#EF4444"
            )
            edges.add(FaraidhGraphEdge(rootId, id, GraphEdgeType.MARRIAGE))
        }

        // 2. Parents (Generation +1)
        if (input.fatherCount > 0) {
            val id = "father_1"
            val share = activeShares.find { it.type == HeirType.FATHER }
            val blocked = blockedHeirs.find { it.type == HeirType.FATHER }
            val status = when {
                blocked?.reason == BlockingReasonKey.OUT_OF_WEDLOCK -> GraphNodeStatus.EXCLUDED_BY_LAW
                blocked != null -> GraphNodeStatus.MAHJUB_HIRMAN
                else -> GraphNodeStatus.ACTIVE
            }
            nodes[id] = FaraidhGraphNode(
                id = id,
                displayName = "Father",
                generationLevel = 1,
                relationType = HeirType.FATHER,
                status = status,
                baseShareFraction = share?.fraction?.toDisplayString(),
                finalPercentage = share?.percentage?.toDouble() ?: 0.0,
                cashValue = share?.cashAmount ?: BigDecimal.ZERO,
                disqualificationReasonId = blocked?.reason?.name?.lowercase(),
                visualColorHex = if (status == GraphNodeStatus.ACTIVE) "#10B981" else "#EF4444"
            )
            edges.add(FaraidhGraphEdge(id, rootId, GraphEdgeType.PARENT_CHILD))
        }

        if (input.motherCount > 0) {
            val id = "mother_1"
            val share = activeShares.find { it.type == HeirType.MOTHER }
            nodes[id] = FaraidhGraphNode(
                id = id,
                displayName = "Mother",
                generationLevel = 1,
                relationType = HeirType.MOTHER,
                status = if (share != null) GraphNodeStatus.ACTIVE else GraphNodeStatus.MAHJUB_HIRMAN,
                baseShareFraction = share?.fraction?.toDisplayString(),
                finalPercentage = share?.percentage?.toDouble() ?: 0.0,
                cashValue = share?.cashAmount ?: BigDecimal.ZERO,
                visualColorHex = if (share != null) "#10B981" else "#EF4444"
            )
            edges.add(FaraidhGraphEdge(id, rootId, GraphEdgeType.PARENT_CHILD))
        }

        // 3. Children (Generation -1)
        for (i in 1..input.sonCount) {
            val id = "son_$i"
            val share = activeShares.find { it.type == HeirType.SON }
            nodes[id] = FaraidhGraphNode(
                id = id,
                displayName = "Son $i",
                generationLevel = -1,
                relationType = HeirType.SON,
                status = if (share != null) GraphNodeStatus.ACTIVE else GraphNodeStatus.MAHJUB_HIRMAN,
                baseShareFraction = share?.fraction?.toDisplayString(),
                finalPercentage = (share?.percentage?.toDouble() ?: 0.0) / input.sonCount,
                cashValue = (share?.cashAmount ?: BigDecimal.ZERO).divide(BigDecimal(input.sonCount), 2, java.math.RoundingMode.HALF_UP),
                visualColorHex = if (share != null) "#10B981" else "#EF4444"
            )
            edges.add(FaraidhGraphEdge(rootId, id, GraphEdgeType.PARENT_CHILD))
        }

        for (i in 1..input.daughterCount) {
            val id = "daughter_$i"
            val share = activeShares.find { it.type == HeirType.DAUGHTER }
            nodes[id] = FaraidhGraphNode(
                id = id,
                displayName = "Daughter $i",
                generationLevel = -1,
                relationType = HeirType.DAUGHTER,
                status = if (share != null) GraphNodeStatus.ACTIVE else GraphNodeStatus.MAHJUB_HIRMAN,
                baseShareFraction = share?.fraction?.toDisplayString(),
                finalPercentage = (share?.percentage?.toDouble() ?: 0.0) / input.daughterCount,
                cashValue = (share?.cashAmount ?: BigDecimal.ZERO).divide(BigDecimal(input.daughterCount), 2, java.math.RoundingMode.HALF_UP),
                visualColorHex = if (share != null) "#10B981" else "#EF4444"
            )
            edges.add(FaraidhGraphEdge(rootId, id, GraphEdgeType.PARENT_CHILD))
        }

        // 4. Disqualified heirs
        for ((idx, dq) in input.disqualifiedHeirs.withIndex()) {
            val id = "dq_${dq.type.name.lowercase()}_$idx"
            nodes[id] = FaraidhGraphNode(
                id = id,
                displayName = "${dq.type.name.lowercase().replaceFirstChar { it.titlecase() }} (Excluded)",
                generationLevel = if (dq.type == HeirType.SON || dq.type == HeirType.DAUGHTER) -1 else 0,
                relationType = dq.type,
                status = GraphNodeStatus.EXCLUDED_BY_LAW,
                disqualificationReasonId = dq.reason.name.lowercase(),
                visualColorHex = "#EF4444"
            )
            edges.add(FaraidhGraphEdge(rootId, id, GraphEdgeType.PARENT_CHILD))
        }

        return FaraidhFamilyGraph(
            rootDeceasedId = rootId,
            nodes = nodes,
            edges = edges
        )
    }
}
