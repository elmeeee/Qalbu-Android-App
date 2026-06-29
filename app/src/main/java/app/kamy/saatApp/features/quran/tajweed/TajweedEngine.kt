package app.kamy.saatApp.features.quran.tajweed

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

object TajweedEngine {

    private val rules = listOf(
        IkhfaRule(),
        QalqalahRule(),
        GhunnahRule(),
        IdghamBighunnahRule(),
        IdghamBilaghunnahRule(),
        IqlabRule(),
        IdghamMimiRule(),
        IkhfaSyafawiRule()
    )

    private val colorMap = mapOf(
        TajweedType.IKHFA to Color(0xFF9400A8), // Purple
        TajweedType.QALQALAH to Color(0xFFDD0008), // Red
        TajweedType.GHUNNA to Color(0xFFFF7E1E), // Orange
        TajweedType.IQLAB to Color(0xFF26BFFD), // Cyan
        TajweedType.IDGHAM_WITH_GHUNNA to Color(0xFF169200), // Green
        TajweedType.IDGHAM_WITHOUT_GHUNNA to Color(0xFFA1A1A1), // Grey
        TajweedType.IDGHAM_MIMI to Color(0xFF169200), // Green
        TajweedType.IKHFA_SYAFAWI to Color(0xFFD500B7) // Pink
    )

    fun applyTajweed(rawAyat: String, isTajweedEnabled: Boolean = true): AnnotatedString {
        if (!isTajweedEnabled) {
            return AnnotatedString(rawAyat)
        }

        val allResults = mutableListOf<TajweedResult>()
        for (rule in rules) {
            allResults.addAll(rule.findTajweed(rawAyat))
        }

        return buildAnnotatedString {
            append(rawAyat)
            
            for (result in allResults) {
                val color = colorMap[result.type] ?: Color.Unspecified
                val start = result.start.coerceAtLeast(0)
                val end = result.end.coerceAtMost(rawAyat.length)
                
                if (start < end) {
                    addStyle(
                        style = SpanStyle(color = color),
                        start = start,
                        end = end
                    )
                    addStringAnnotation(
                        tag = "TAJWEED",
                        annotation = result.type.name,
                        start = start,
                        end = end
                    )
                }
            }
        }
    }
}
