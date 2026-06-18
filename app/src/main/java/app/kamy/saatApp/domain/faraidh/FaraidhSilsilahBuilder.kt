package app.kamy.saatApp.domain.faraidh

object FaraidhSilsilahBuilder {

    private const val DECEASED_ID = "deceased"

    fun build(
        profile: DeceasedProfile,
        input: HeirInput,
        activeShares: List<HeirShare>,
        blocked: List<BlockedHeir>,
        names: FaraidhParticipantNames = FaraidhParticipantNames()
    ): List<SilsilahNode> {
        val nodes = mutableListOf<SilsilahNode>()
        val activeTypes = activeShares.map { it.type }.toSet()
        val blockedMap = blocked.groupBy { it.type }

        fun inherits(type: HeirType): Boolean = activeTypes.contains(type)

        val deceasedLabel = profile.name.ifBlank { "" }
        nodes += SilsilahNode(
            id = DECEASED_ID,
            parentNodeId = null,
            generationLevel = 0,
            type = HeirType.SON,
            labelKey = "faraidh_node_deceased",
            headCount = 1,
            inherits = false,
            blocked = false,
            blockReason = null,
            displayName = deceasedLabel
        )

        if (input.fatherCount > 0) {
            nodes += node(
                "father", DECEASED_ID, -1, HeirType.FATHER, "faraidh_heir_father", 1,
                inherits(HeirType.FATHER), false, null, names.fatherName
            )
        }
        if (input.motherCount > 0) {
            nodes += node(
                "mother", DECEASED_ID, -1, HeirType.MOTHER, "faraidh_heir_mother", 1,
                inherits(HeirType.MOTHER), false, null, names.motherName
            )
        }

        if (profile.gender == DeceasedGender.MALE && input.wifeCount > 0) {
            repeat(input.wifeCount) { i ->
                val wifeName = names.wifeNames.getOrNull(i).orEmpty()
                nodes += node(
                    "wife_$i", DECEASED_ID, 0, HeirType.WIFE, "faraidh_heir_wife",
                    1, inherits(HeirType.WIFE), false, null, wifeName
                )
            }
        }
        if (profile.gender == DeceasedGender.FEMALE && input.husbandCount > 0) {
            nodes += node(
                "husband", DECEASED_ID, 0, HeirType.HUSBAND, "faraidh_heir_husband",
                1, inherits(HeirType.HUSBAND), false, null, names.husbandName
            )
        }

        addChildBranch(nodes, input, activeTypes, blockedMap, names)
        addSiblingBranch(nodes, input, activeTypes, blockedMap, names)

        return nodes
    }

    private fun addChildBranch(
        nodes: MutableList<SilsilahNode>,
        input: HeirInput,
        activeTypes: Set<HeirType>,
        blockedMap: Map<HeirType, List<BlockedHeir>>,
        names: FaraidhParticipantNames
    ) {
        if (input.sonCount > 0) {
            repeat(input.sonCount) { i ->
                nodes += node(
                    "son_$i", DECEASED_ID, 1, HeirType.SON, "faraidh_heir_son", 1,
                    activeTypes.contains(HeirType.SON), false, null,
                    names.sonNames.getOrNull(i).orEmpty()
                )
            }
        }
        if (input.daughterCount > 0) {
            repeat(input.daughterCount) { i ->
                nodes += node(
                    "daughter_$i", DECEASED_ID, 1, HeirType.DAUGHTER, "faraidh_heir_daughter", 1,
                    activeTypes.contains(HeirType.DAUGHTER), false, null,
                    names.daughterNames.getOrNull(i).orEmpty()
                )
            }
        }
        if (input.grandsonCount > 0) {
            val reason = blockedMap[HeirType.GRANDSON]?.firstOrNull()?.reason
            repeat(input.grandsonCount) { i ->
                nodes += node(
                    "grandson_$i", "son_0", 2, HeirType.GRANDSON, "faraidh_heir_grandson", 1,
                    activeTypes.contains(HeirType.GRANDSON), reason != null, reason,
                    names.grandsonNames.getOrNull(i).orEmpty()
                )
            }
        }
        if (input.granddaughterCount > 0) {
            val reason = blockedMap[HeirType.GRANDDAUGHTER]?.firstOrNull()?.reason
            repeat(input.granddaughterCount) { i ->
                nodes += node(
                    "granddaughter_$i", "son_0", 2, HeirType.GRANDDAUGHTER, "faraidh_heir_granddaughter", 1,
                    activeTypes.contains(HeirType.GRANDDAUGHTER), reason != null, reason,
                    names.granddaughterNames.getOrNull(i).orEmpty()
                )
            }
        }
    }

    private fun addSiblingBranch(
        nodes: MutableList<SilsilahNode>,
        input: HeirInput,
        activeTypes: Set<HeirType>,
        blockedMap: Map<HeirType, List<BlockedHeir>>,
        names: FaraidhParticipantNames
    ) {
        addIndividualSiblings(nodes, input.fullBrotherCount, names.fullBrotherNames, "full_bro", HeirType.FULL_BROTHER, activeTypes, blockedMap)
        addIndividualSiblings(nodes, input.fullSisterCount, names.fullSisterNames, "full_sis", HeirType.FULL_SISTER, activeTypes, blockedMap)
        addIndividualSiblings(nodes, input.paternalBrotherCount, names.paternalBrotherNames, "paternal_bro", HeirType.PATERNAL_BROTHER, activeTypes, blockedMap)
        addIndividualSiblings(nodes, input.paternalSisterCount, names.paternalSisterNames, "paternal_sis", HeirType.PATERNAL_SISTER, activeTypes, blockedMap)
        repeat(input.maternalBrotherCount) { i ->
            val reason = blockedMap[HeirType.MATERNAL_SIBLING]?.firstOrNull()?.reason
            nodes += node(
                "maternal_bro_$i", DECEASED_ID, 0, HeirType.MATERNAL_SIBLING, "faraidh_heir_maternal_sibling", 1,
                activeTypes.contains(HeirType.MATERNAL_SIBLING), reason != null, reason,
                names.maternalBrotherNames.getOrNull(i).orEmpty()
            )
        }
        repeat(input.maternalSisterCount) { i ->
            val reason = blockedMap[HeirType.MATERNAL_SIBLING]?.firstOrNull()?.reason
            nodes += node(
                "maternal_sis_$i", DECEASED_ID, 0, HeirType.MATERNAL_SIBLING, "faraidh_heir_maternal_sibling", 1,
                activeTypes.contains(HeirType.MATERNAL_SIBLING), reason != null, reason,
                names.maternalSisterNames.getOrNull(i).orEmpty()
            )
        }
    }

    private fun addIndividualSiblings(
        nodes: MutableList<SilsilahNode>,
        count: Int,
        nameList: List<String>,
        idPrefix: String,
        type: HeirType,
        activeTypes: Set<HeirType>,
        blockedMap: Map<HeirType, List<BlockedHeir>>
    ) {
        if (count <= 0) return
        val reason = blockedMap[type]?.firstOrNull()?.reason
        repeat(count) { i ->
            nodes += node(
                "${idPrefix}_$i", DECEASED_ID, 0, type, heirLabelKey(type), 1,
                activeTypes.contains(type), reason != null, reason,
                nameList.getOrNull(i).orEmpty()
            )
        }
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
        blockReason: BlockingReasonKey?,
        displayName: String = ""
    ) = SilsilahNode(id, parentId, generation, type, labelKey, heads, inherits, blocked, blockReason, displayName)
}
