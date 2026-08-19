package app.kamy.saatApp.features.tools.hajj.model

import app.kamy.saatApp.core.locale.AppLanguage

data class LocalizedText(
    val id: String,
    val en: String,
    val ms: String
) {
    fun get(language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> en
            AppLanguage.MALAY -> ms
            else -> id
        }
    }
}

enum class ManasikType {
    UMRAH,
    HAJJ_TAMATTU,
    HAJJ_IFRAD,
    HAJJ_QIRAN
}

data class ManasikStep(
    val id: String,
    val stepNumber: Int,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val location: LocalizedText,
    val timeOrDay: LocalizedText,
    val isRukun: Boolean,
    val description: LocalizedText,
    val detailedSteps: List<LocalizedText>,
    val doaRefId: String? = null,
    val dalilQuran: String? = null,
    val dalilHadits: String? = null,
    val prohibitions: List<LocalizedText> = emptyList(),
    val commonMistakes: List<LocalizedText> = emptyList(),
    val practicalTips: List<LocalizedText> = emptyList()
)

data class HajjDoaItem(
    val id: String,
    val title: LocalizedText,
    val category: LocalizedText,
    val arabic: String,
    val latin: String,
    val translation: LocalizedText,
    val contextAndBenefits: LocalizedText,
    val reference: String,
    val occasions: LocalizedText
)

data class HajjDalilItem(
    val id: String,
    val title: LocalizedText,
    val category: LocalizedText,
    val isQuran: Boolean,
    val surahOrNarrator: String,
    val arabic: String,
    val latin: String? = null,
    val translation: LocalizedText,
    val tafsirExplanation: LocalizedText,
    val keyLessons: List<LocalizedText>
)

data class MadhhabRuling(
    val id: String,
    val topic: LocalizedText,
    val generalExplanation: LocalizedText,
    val syafii: LocalizedText,
    val hanafi: LocalizedText,
    val maliki: LocalizedText,
    val hanbali: LocalizedText,
    val rajihConclusion: LocalizedText
)

data class DamRuleItem(
    val id: String,
    val violation: LocalizedText,
    val category: LocalizedText,
    val penalty: LocalizedText,
    val alternatives: LocalizedText,
    val dalil: String
)

data class MiqatLocation(
    val id: String,
    val name: String,
    val arabicName: String,
    val distanceFromMakkah: String,
    val direction: LocalizedText,
    val dedicatedFor: LocalizedText,
    val description: LocalizedText,
    val facilities: List<LocalizedText>
)

data class HistoricZiarahSite(
    val id: String,
    val name: LocalizedText,
    val city: LocalizedText,
    val historicalSignificance: LocalizedText,
    val adabAndDoa: LocalizedText
)

data class HajjChecklistCategory(
    val id: String,
    val title: LocalizedText,
    val items: List<HajjChecklistItem>
)

data class HajjChecklistItem(
    val id: String,
    val label: LocalizedText,
    val note: LocalizedText? = null,
    val isCrucial: Boolean = false
)
