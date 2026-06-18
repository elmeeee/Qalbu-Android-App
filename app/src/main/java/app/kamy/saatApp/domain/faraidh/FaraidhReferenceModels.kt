package app.kamy.saatApp.domain.faraidh

import kotlinx.serialization.Serializable

@Serializable
data class FaraidhReferenceBundle(
    val verses: List<FaraidhVerseRef> = emptyList(),
    val hadiths: List<FaraidhHadithRef> = emptyList(),
    val proofMappings: Map<String, List<String>> = emptyMap()
)

@Serializable
data class FaraidhVerseRef(
    val id: String,
    val surah: Int,
    val ayah: Int,
    val arabic: String,
    val textEn: String,
    val textId: String,
    val textMs: String,
    val topicKeys: List<String> = emptyList()
)

@Serializable
data class FaraidhHadithRef(
    val id: String,
    val collection: String,
    val number: Int,
    val arabic: String? = null,
    val textEn: String,
    val textId: String,
    val textMs: String,
    val externalUrl: String,
    val topicKeys: List<String> = emptyList()
)

data class FaraidhProofItem(
    val id: String,
    val kind: FaraidhProofKind,
    val title: String,
    val body: String,
    val arabic: String?,
    val externalUrl: String?,
    val surah: Int? = null,
    val ayah: Int? = null
)

enum class FaraidhProofKind { QURAN, HADITH, NOTE }
