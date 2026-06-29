package app.kamy.saatApp.features.quran.tajweed

enum class TajweedType {
    GHUNNA,
    IDGHAM_WITHOUT_GHUNNA,
    IDGHAM_WITH_GHUNNA,
    IDGHAM_MIMI,
    IQLAB,
    IKHFA,
    IKHFA_SYAFAWI,
    QALQALAH
}

data class TajweedResult(
    val type: TajweedType,
    val start: Int,
    val end: Int
)
