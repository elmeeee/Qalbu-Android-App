package app.kamy.saatApp.domain.faraidh

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.zip.GZIPInputStream

@Serializable
data class RuleCitation(
    val type: CitationType,
    val source: String,
    val textAr: String,
    val textEn: String,
    val textId: String,
    val textMs: String
)

@Serializable
enum class CitationType { QURAN, HADITH, IJMA, QIYAS, FIQH }

@Serializable
data class MultilingualText(
    val en: String,
    val id: String,
    val ms: String
)

@Serializable
data class ApplicabilityFlags(
    val munasakhat: Boolean = false,
    val waladZina: Boolean = false,
    val aul: Boolean = false,
    val radd: Boolean = false,
    val janin: Boolean = false,
    val mafqud: Boolean = false
)

@Serializable
data class InheritanceRule(
    val id: String,
    val category: String,
    val title: MultilingualText,
    val description: MultilingualText,
    val edgeCaseExplanation: MultilingualText,
    val citations: List<RuleCitation> = emptyList(),
    val tags: List<String> = emptyList(),
    val applicability: ApplicabilityFlags = ApplicabilityFlags()
)

@Serializable
data class InheritanceRuleBook(
    val rules: List<InheritanceRule>
)

object LocalFaraidhRuleBook {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    fun load(context: Context, assetName: String = "faraidh/inheritance_rules.json.gz"): InheritanceRuleBook {
        val payload = GZIPInputStream(context.assets.open(assetName)).bufferedReader().use { it.readText() }
        return json.decodeFromString(InheritanceRuleBook.serializer(), payload)
    }

    fun findRule(book: InheritanceRuleBook, ruleId: String): InheritanceRule? =
        book.rules.firstOrNull { it.id == ruleId }
}
