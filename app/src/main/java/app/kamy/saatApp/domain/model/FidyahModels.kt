package app.kamy.saatApp.domain.model

enum class FidyahMadhhab(
    val id: String,
    val titleId: String,
    val titleMs: String,
    val titleEn: String
) {
    SYAFII("syafii", "Syafi'i", "Syafi'i", "Shafi'i"),
    HANAFI("hanafi", "Hanafi", "Hanafi", "Hanafi"),
    MALIKI("maliki", "Maliki", "Maliki", "Maliki"),
    HANBALI("hanbali", "Hanbali", "Hanbali", "Hanbali");

    companion object {
        fun fromId(id: String): FidyahMadhhab =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SYAFII
    }
}

enum class FidyahReason(
    val id: String,
    val titleId: String,
    val titleMs: String,
    val titleEn: String
) {
    HAID_NIFAS(
        "haid_nifas",
        "Haid / Nifas (Qadha Puasa Saja)",
        "Haid / Nifas (Qada Puasa Sahaja)",
        "Menstruation / Postpartum (Qadha Fast Only)"
    ),
    SICK_TEMPORARY(
        "sick_temporary",
        "Sakit Sementara (Qadha Puasa Saja)",
        "Sakit Sementara (Qada Puasa Sahaja)",
        "Temporary Illness (Qadha Fast Only)"
    ),
    TRAVELER_MUSAFIR(
        "traveler_musafir",
        "Musafir / Perjalanan (Qadha Puasa Saja)",
        "Musafir / Perjalanan (Qada Puasa Sahaja)",
        "Traveler (Qadha Fast Only)"
    ),
    ELDERLY_CHRONIC(
        "elderly_chronic",
        "Tua Renta / Sakit Menahun (Fidyah Saja)",
        "Warga Emas / Sakit Berpanjangan (Fidyah Sahaja)",
        "Elderly / Chronic Illness (Fidyah Only)"
    ),
    PREGNANT_NURSING_CHILD(
        "pregnant_nursing_child",
        "Ibu Hamil / Menyusui (Khawatir Anak)",
        "Ibu Hamil / Menyusukan (Khawatirkan Anak)",
        "Pregnant / Nursing (Fearing for Child)"
    ),
    PREGNANT_NURSING_SELF(
        "pregnant_nursing_self",
        "Ibu Hamil / Menyusui (Khawatir Diri Sendiri)",
        "Ibu Hamil / Menyusukan (Khawatirkan Diri)",
        "Pregnant / Nursing (Fearing for Self)"
    ),
    LATE_QADHA(
        "late_qadha",
        "Keterlambatan Qadha (Melewati Ramadan)",
        "Kelewatan Qada (Melampaui Ramadan)",
        "Delayed Qadha (Across Ramadan)"
    ),
    DECEASED_BY_HEIR(
        "deceased_by_heir",
        "Fidyah untuk Almarhum / Orang Tua",
        "Fidyah untuk Arwah / Ibu Bapa",
        "Fidyah for Deceased / Parents"
    );

    companion object {
        fun fromId(id: String): FidyahReason =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: HAID_NIFAS
    }
}

data class FidyahCalculationResult(
    val madhhab: FidyahMadhhab,
    val reason: FidyahReason,
    val missedDays: Int,
    val delayedYears: Int,
    val fidyahDaysCount: Int,
    val totalFidyahDaysMultiplier: Int,
    val riceWeightKg: Double,
    val requiredQadhaDays: Int,
    val isFidyahRequired: Boolean,
    val fiqhExplanationId: String,
    val fiqhExplanationMs: String,
    val fiqhExplanationEn: String
)

data class FidyahRecord(
    val id: String,
    val hijriYear: String,
    val reason: FidyahReason,
    val madhhab: FidyahMadhhab,
    val missedDays: Int,
    val delayedYears: Int,
    val paidDays: Int,
    val amountPaid: Double,
    val currencySymbol: String,
    val isFullyPaid: Boolean,
    val completedQadhaDays: Int = 0,
    val isQadhaCompleted: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
