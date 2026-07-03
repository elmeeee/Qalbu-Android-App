package app.kamy.saatApp.domain.faraidh

import kotlinx.serialization.Serializable

@Serializable
data class FaraidhDictionaryBundle(
    val standard_heirs: Map<String, FaraidhDictionaryItem> = emptyMap(),
    val classical_cases: Map<String, FaraidhDictionaryItem> = emptyMap(),
    val anomalies_and_legal_rules: Map<String, FaraidhDictionaryItem> = emptyMap(),
    val disqualifications: Map<String, FaraidhDictionaryItem> = emptyMap()
)

@Serializable
data class FaraidhDictionaryItem(
    val id: String,
    val arabic_term: String? = null,
    val display_name: Map<String, String> = emptyMap(),
    val glossary_definition: Map<String, String> = emptyMap(),
    val dalil: List<FaraidhDictionaryDalil> = emptyList()
)

@Serializable
data class FaraidhDictionaryDalil(
    val source: String,
    val reference_citation: Map<String, String> = emptyMap(),
    val arabic_text: String? = null,
    val transliteration: Map<String, String>? = null,
    val translation: Map<String, String>? = null
)
