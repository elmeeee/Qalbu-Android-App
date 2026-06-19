package app.kamy.saatApp.domain.faraidh

import kotlinx.serialization.Serializable

@Serializable
data class FaraidhParticipantNames(
    val deceasedName: String = "",
    val husbandName: String = "",
    val wifeNames: List<String> = emptyList(),
    val fatherName: String = "",
    val motherName: String = "",
    val sonNames: List<String> = emptyList(),
    val daughterNames: List<String> = emptyList(),
    val grandsonNames: List<String> = emptyList(),
    val granddaughterNames: List<String> = emptyList(),
    val fullBrotherNames: List<String> = emptyList(),
    val fullSisterNames: List<String> = emptyList(),
    val paternalBrotherNames: List<String> = emptyList(),
    val paternalSisterNames: List<String> = emptyList(),
    val maternalBrotherNames: List<String> = emptyList(),
    val maternalSisterNames: List<String> = emptyList()
)

fun resizeNameList(current: List<String>, count: Int): List<String> = when {
    count <= 0 -> emptyList()
    current.size >= count -> current.take(count)
    else -> current + List(count - current.size) { "" }
}

object FaraidhNameLabels {
    fun namesForType(names: FaraidhParticipantNames, type: HeirType): List<String> = when (type) {
        HeirType.HUSBAND -> listOfNotNull(names.husbandName.takeIf { it.isNotBlank() })
        HeirType.WIFE -> names.wifeNames.filter { it.isNotBlank() }
        HeirType.FATHER -> listOfNotNull(names.fatherName.takeIf { it.isNotBlank() })
        HeirType.MOTHER -> listOfNotNull(names.motherName.takeIf { it.isNotBlank() })
        HeirType.SON -> names.sonNames
        HeirType.DAUGHTER -> names.daughterNames
        HeirType.GRANDSON -> names.grandsonNames
        HeirType.GRANDDAUGHTER -> names.granddaughterNames
        HeirType.FULL_BROTHER -> names.fullBrotherNames
        HeirType.FULL_SISTER -> names.fullSisterNames
        HeirType.PATERNAL_BROTHER -> names.paternalBrotherNames
        HeirType.PATERNAL_SISTER -> names.paternalSisterNames
        HeirType.MATERNAL_SIBLING -> (names.maternalBrotherNames + names.maternalSisterNames).filter { it.isNotBlank() }
        HeirType.STEP_CHILD, HeirType.UNBORN_FETUS -> emptyList()
    }

    fun displayList(
        type: HeirType,
        roleLabel: String,
        names: FaraidhParticipantNames,
        headCount: Int
    ): List<String> {
        val entered = namesForType(names, type).filter { it.isNotBlank() }
        if (entered.isNotEmpty()) return entered
        return List(headCount.coerceAtLeast(1)) { index ->
            if (headCount > 1) "$roleLabel ${index + 1}" else roleLabel
        }
    }
}
