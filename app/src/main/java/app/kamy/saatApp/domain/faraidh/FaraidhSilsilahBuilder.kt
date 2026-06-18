package app.kamy.saatApp.domain.faraidh

object FaraidhSilsilahBuilder {

    private const val DECEASED_ID = "deceased"

    fun build(
        profile: DeceasedProfile,
        input: HeirInput,
        activeShares: List<HeirShare>,
        blocked: List<BlockedHeir>
    ): List<SilsilahNode> {
        val nodes = mutableListOf<SilsilahNode>()
        val activeTypes = activeShares.map { it.type }.toSet()
        val blockedMap = blocked.groupBy { it.type }

        fun isBlocked(type: HeirType): Boolean = blockedMap.containsKey(type)
        fun inherits(type: HeirType): Boolean = activeTypes.contains(type)

        nodes += SilsilahNode(
            id = DECEASED_ID,
            parentNodeId = null,
            generationLevel = 0,
            type = HeirType.SON,
            labelKey = "faraidh_node_deceased",
            headCount = 1,
            inherits = false,
            blocked = false,
            blockReason = null
        )

        if (input.fatherCount > 0) {
            nodes += node("father", DECEASED_ID, -1, HeirType.FATHER, "faraidh_heir_father", 1, inherits(HeirType.FATHER), false, null)
        }
        if (input.motherCount > 0) {
            nodes += node("mother", DECEASED_ID, -1, HeirType.MOTHER, "faraidh_heir_mother", 1, inherits(HeirType.MOTHER), false, null)
        }

        if (profile.gender == DeceasedGender.MALE && input.wifeCount > 0) {
            repeat(input.wifeCount) { i ->
                nodes += node(
                    "wife_$i", DECEASED_ID, 0, HeirType.WIFE, "faraidh_heir_wife",
                    1, inherits(HeirType.WIFE), false, null
                )
            }
        }
        if (profile.gender == DeceasedGender.FEMALE && input.husbandCount > 0) {
            nodes += node(
                "husband", DECEASED_ID, 0, HeirType.HUSBAND, "faraidh_heir_husband",
                input.husbandCount, inherits(HeirType.HUSBAND), false, null
            )
        }

        addChildBranch(nodes, input, activeTypes, blockedMap)

        addSiblingBranch(nodes, input, activeTypes, blockedMap, "full_bro", HeirType.FULL_BROTHER, input.fullBrotherCount)
        addSiblingBranch(nodes, input, activeTypes, blockedMap, "full_sis", HeirType.FULL_SISTER, input.fullSisterCount)
        addSiblingBranch(nodes, input, activeTypes, blockedMap, "paternal_bro", HeirType.PATERNAL_BROTHER, input.paternalBrotherCount)
        addSiblingBranch(nodes, input, activeTypes, blockedMap, "paternal_sis", HeirType.PATERNAL_SISTER, input.paternalSisterCount)
        val maternalHeads = input.maternalBrotherCount + input.maternalSisterCount
        if (maternalHeads > 0) {
            val blockedReason = blockedMap[HeirType.MATERNAL_SIBLING]?.firstOrNull()?.reason
            nodes += node(
                "maternal_siblings", DECEASED_ID, 0, HeirType.MATERNAL_SIBLING,
                "faraidh_heir_maternal_sibling", maternalHeads,
                inherits(HeirType.MATERNAL_SIBLING),
                isBlocked(HeirType.MATERNAL_SIBLING),
                blockedReason
            )
        }

        return nodes
    }

    private fun addChildBranch(
        nodes: MutableList<SilsilahNode>,
        input: HeirInput,
        activeTypes: Set<HeirType>,
        blockedMap: Map<HeirType, List<BlockedHeir>>
    ) {
        if (input.sonCount > 0) {
            nodes += node("sons", DECEASED_ID, 1, HeirType.SON, "faraidh_heir_son", input.sonCount, activeTypes.contains(HeirType.SON), false, null)
        }
        if (input.daughterCount > 0) {
            nodes += node("daughters", DECEASED_ID, 1, HeirType.DAUGHTER, "faraidh_heir_daughter", input.daughterCount, activeTypes.contains(HeirType.DAUGHTER), false, null)
        }
        if (input.grandsonCount > 0) {
            val reason = blockedMap[HeirType.GRANDSON]?.firstOrNull()?.reason
            nodes += node("grandsons", "sons", 2, HeirType.GRANDSON, "faraidh_heir_grandson", input.grandsonCount, activeTypes.contains(HeirType.GRANDSON), reason != null, reason)
        }
        if (input.granddaughterCount > 0) {
            val reason = blockedMap[HeirType.GRANDDAUGHTER]?.firstOrNull()?.reason
            nodes += node("granddaughters", "sons", 2, HeirType.GRANDDAUGHTER, "faraidh_heir_granddaughter", input.granddaughterCount, activeTypes.contains(HeirType.GRANDDAUGHTER), reason != null, reason)
        }
    }

    private fun addSiblingBranch(
        nodes: MutableList<SilsilahNode>,
        input: HeirInput,
        activeTypes: Set<HeirType>,
        blockedMap: Map<HeirType, List<BlockedHeir>>,
        id: String,
        type: HeirType,
        count: Int
    ) {
        if (count <= 0) return
        val reason = blockedMap[type]?.firstOrNull()?.reason
        nodes += node(id, DECEASED_ID, 0, type, heirLabelKey(type), count, activeTypes.contains(type), reason != null, reason)
    }

    private fun heirLabelKey(type: HeirType): String = when (type) {
        HeirType.HUSBAND -> "faraidh_heir_husband"
        HeirType.WIFE -> "faraidh_heir_wife"
        HeirType.FATHER -> "faraidh_heir_father"
        HeirType.MOTHER -> "faraidh_heir_mother"
        HeirType.SON -> "faraidh_heir_son"
        HeirType.DAUGHTER -> "faraidh_heir_daughter"
        HeirType.GRANDSON -> "faraidh_heir_grandson"
        HeirType.GRANDDAUGHTER -> "faraidh_heir_granddaughter"
        HeirType.FULL_BROTHER -> "faraidh_heir_full_brother"
        HeirType.FULL_SISTER -> "faraidh_heir_full_sister"
        HeirType.PATERNAL_BROTHER -> "faraidh_heir_paternal_brother"
        HeirType.PATERNAL_SISTER -> "faraidh_heir_paternal_sister"
        HeirType.MATERNAL_SIBLING -> "faraidh_heir_maternal_sibling"
    }

    private fun node(
        id: String,
        parentId: String,
        generation: Int,
        type: HeirType,
        labelKey: String,
        heads: Int,
        inherits: Boolean,
        blocked: Boolean,
        blockReason: BlockingReasonKey?
    ) = SilsilahNode(id, parentId, generation, type, labelKey, heads, inherits, blocked, blockReason)
}
