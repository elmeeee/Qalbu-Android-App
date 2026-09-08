package app.kamy.saatApp.domain.prayer

enum class PrayerCalculationMethod(val rawValue: String) {
    MUHAMMADIYAH("muhammadiyah"),
    KEMENAG("kemenag"),
    MUIS("muis"),
    JAKIM("jakim"),
    BRUNEI("brunei"),
    KARACHI("karachi"),
    TEHRAN("tehran"),
    JAFARI("jafari"),
    ISNA("isna"),
    MWL("mwl"),
    UMM_AL_QURA("ummAlQura"),
    EGYPTIAN("egyptian"),
    MCW("mcw"),
    GULF("gulf"),
    KUWAIT("kuwait"),
    QATAR("qatar"),
    DUBAI("dubai"),
    TUNISIA("tunisia"),
    ALGERIA("algeria"),
    MOROCCO("morocco"),
    JORDAN("jordan"),
    FRANCE("france"),
    TURKEY("turkey"),
    RUSSIA("russia"),
    LISBON("lisbon");

    val displayName: String
        get() = when (this) {
            MUHAMMADIYAH -> "Muhammadiyah"
            KEMENAG -> "Kementerian Agama RI (Kemenag)"
            MUIS -> "Majlis Ugama Islam Singapura (MUIS)"
            JAKIM -> "Jabatan Kemajuan Islam Malaysia (JAKIM)"
            BRUNEI -> "Majlis Ugama Islam Brunei (MUIB)"
            KARACHI -> "University of Islamic Sciences, Karachi"
            TEHRAN -> "Institute of Geophysics, University of Tehran"
            JAFARI -> "Shia Ithna-Ashari (Leva Institute, Qom)"
            ISNA -> "Islamic Society of North America (ISNA)"
            MWL -> "Muslim World League (MWL)"
            UMM_AL_QURA -> "Umm Al-Qura University, Makkah"
            EGYPTIAN -> "Egyptian General Authority of Survey"
            MCW -> "Moonsighting Committee Worldwide"
            GULF -> "Gulf Region (90 min Isha)"
            KUWAIT -> "Ministry of Awqaf & Islamic Affairs, Kuwait"
            QATAR -> "Ministry of Awqaf & Islamic Affairs, Qatar"
            DUBAI -> "Islamic Affairs (IACAD), Dubai"
            TUNISIA -> "Ministère des Affaires Religieuses, Tunisie"
            ALGERIA -> "Ministère des Affaires Religieuses, Algérie"
            MOROCCO -> "Ministère des Habous et des Affaires Islamiques, Maroc"
            JORDAN -> "Ministry of Awqaf & Islamic Affairs, Jordan"
            FRANCE -> "Union des Organisations Islamiques de France (UOIF)"
            TURKEY -> "Diyanet İşleri Başkanlığı, Türkiye"
            RUSSIA -> "Spiritual Administration of Muslims of Russia (DUM RF)"
            LISBON -> "Comunidade Islâmica de Lisboa, Portugal"
        }

    val organization: String
        get() = when (this) {
            MUHAMMADIYAH -> "Persyarikatan Muhammadiyah"
            KEMENAG -> "Kementerian Agama Republik Indonesia"
            MUIS -> "Majlis Ugama Islam Singapura"
            JAKIM -> "Jabatan Kemajuan Islam Malaysia"
            BRUNEI -> "Kementerian Hal Ehwal Ugama, Brunei Darussalam"
            KARACHI -> "Jamia Uloom-ul-Islamia, Karachi, Pakistan"
            TEHRAN -> "Institute of Geophysics, University of Tehran, Iran"
            JAFARI -> "Leva Research Institute, Qom, Iran"
            ISNA -> "Islamic Society of North America"
            MWL -> "Muslim World League (Rabitah al-Alam al-Islami)"
            UMM_AL_QURA -> "Umm Al-Qura University, Makkah al-Mukarramah"
            EGYPTIAN -> "Egyptian General Authority of Survey (Al-Hai'ah Al-Misriyyah)"
            MCW -> "Moonsighting Committee Worldwide"
            GULF -> "Gulf Region Prayer Calculation (90 min interval)"
            KUWAIT -> "Ministry of Awqaf and Islamic Affairs, Kuwait"
            QATAR -> "Ministry of Awqaf and Islamic Affairs, Qatar"
            DUBAI -> "Islamic Affairs & Charitable Activities Department, Dubai"
            TUNISIA -> "Ministère des Affaires Religieuses, République Tunisienne"
            ALGERIA -> "Ministère des Affaires Religieuses et des Wakfs, Algérie"
            MOROCCO -> "Ministère des Habous et des Affaires Islamiques, Royaume du Maroco"
            JORDAN -> "Ministry of Awqaf, Islamic Affairs and Holy Places, Jordan"
            FRANCE -> "Musulmans de France (ex-UOIF)"
            TURKEY -> "T.C. Diyanet İşleri Başkanlığı"
            RUSSIA -> "Spiritual Administration of Muslims of the Russian Federation"
            LISBON -> "Comunidade Islâmica de Lisboa, Portugal"
        }

    @get:androidx.annotation.DrawableRes
    val iconRes: Int
        get() = when (this) {
            MUHAMMADIYAH -> app.kamy.saatApp.R.drawable.institution_muhammadiyah
            KEMENAG -> app.kamy.saatApp.R.drawable.institution_kemenag
            MUIS -> app.kamy.saatApp.R.drawable.institution_muis
            JAKIM -> app.kamy.saatApp.R.drawable.institution_jakim
            BRUNEI -> app.kamy.saatApp.R.drawable.institution_muib
            KARACHI -> app.kamy.saatApp.R.drawable.institution_karachi
            TEHRAN -> app.kamy.saatApp.R.drawable.institution_tehran
            JAFARI -> app.kamy.saatApp.R.drawable.institution_jafari
            ISNA -> app.kamy.saatApp.R.drawable.institution_isna
            MWL -> app.kamy.saatApp.R.drawable.institution_mwl
            UMM_AL_QURA -> app.kamy.saatApp.R.drawable.institution_umm_al_qura
            EGYPTIAN -> app.kamy.saatApp.R.drawable.institution_egyptian
            MCW -> app.kamy.saatApp.R.drawable.institution_mcw
            GULF -> app.kamy.saatApp.R.drawable.institution_gulf
            KUWAIT -> app.kamy.saatApp.R.drawable.institution_kuwait
            QATAR -> app.kamy.saatApp.R.drawable.institution_qatar
            DUBAI -> app.kamy.saatApp.R.drawable.institution_dubai
            TUNISIA -> app.kamy.saatApp.R.drawable.institution_tunisia
            ALGERIA -> app.kamy.saatApp.R.drawable.institution_algeria
            MOROCCO -> app.kamy.saatApp.R.drawable.institution_morocco
            JORDAN -> app.kamy.saatApp.R.drawable.institution_jordan
            FRANCE -> app.kamy.saatApp.R.drawable.institution_france
            TURKEY -> app.kamy.saatApp.R.drawable.institution_turkey
            RUSSIA -> app.kamy.saatApp.R.drawable.institution_russia
            LISBON -> app.kamy.saatApp.R.drawable.institution_lisbon
        }

    val aladhanMethodId: Int
        get() = when (this) {
            JAFARI -> 0
            KARACHI -> 1
            ISNA -> 2
            MWL -> 3
            UMM_AL_QURA -> 4
            EGYPTIAN -> 5
            TEHRAN -> 7
            GULF -> 8
            KUWAIT -> 9
            QATAR -> 10
            MUIS -> 11
            FRANCE -> 12
            TURKEY -> 13
            RUSSIA -> 14
            MCW -> 15
            DUBAI -> 16
            JAKIM, BRUNEI -> 17
            TUNISIA -> 18
            ALGERIA -> 19
            KEMENAG -> 20
            MOROCCO -> 21
            LISBON -> 22
            JORDAN -> 23
            MUHAMMADIYAH -> 99
        }

    val aladhanMethodSettings: String?
        get() = when (this) {
            MUHAMMADIYAH -> "18,null,18"
            KEMENAG, MUIS, JAKIM, BRUNEI, JORDAN -> "20,null,18"
            KARACHI -> "18,null,18"
            ISNA -> "15,null,15"
            MWL -> "18,null,17"
            MCW -> "18,null,18"
            EGYPTIAN -> "19.5,null,17.5"
            UMM_AL_QURA -> "18.5,null,90 min"
            KUWAIT -> "18,null,17.5"
            QATAR -> "18,null,90 min"
            DUBAI -> "18.2,null,18.2"
            TUNISIA -> "18,null,18"
            ALGERIA -> "18,null,17"
            MOROCCO -> "19,null,17"
            FRANCE, LISBON -> "12,null,12"
            TURKEY -> "18,null,17"
            RUSSIA -> "16,null,15"
            TEHRAN -> "17.7,4.5,14"
            JAFARI -> "16,4,14"
            GULF -> "19.5,null,90 min"
        }

    val countryName: String
        get() = when (this) {
            MUHAMMADIYAH, KEMENAG -> "Indonesia"
            MUIS -> "Singapura (Singapore)"
            JAKIM -> "Malaysia"
            BRUNEI -> "Brunei Darussalam"
            KARACHI -> "Pakistan / India / Bangladesh"
            TEHRAN, JAFARI -> "Iran / Shia"
            ISNA -> "Amerika Utara (USA / Canada)"
            MWL, MCW -> "Internasional (Worldwide)"
            UMM_AL_QURA -> "Arab Saudi (Saudi Arabia)"
            EGYPTIAN -> "Mesir (Egypt)"
            GULF -> "Kawasan Teluk (Gulf Region)"
            KUWAIT -> "Kuwait"
            QATAR -> "Qatar"
            DUBAI -> "Uni Emirat Arab (UAE - Dubai)"
            TUNISIA -> "Tunisia"
            ALGERIA -> "Aljazair (Algeria)"
            MOROCCO -> "Maroko (Morocco)"
            JORDAN -> "Yordania (Jordan)"
            FRANCE -> "Perancis (France)"
            TURKEY -> "Turki (Turkey)"
            RUSSIA -> "Rusia (Russia)"
            LISBON -> "Portugal (Lisbon)"
        }

    val aladhanSchool: Int
        get() = when (this) {
            KARACHI, TURKEY, RUSSIA -> 1 // 1 = Hanafi (bayangan Ashar 2x)
            else -> 0 // 0 = Standar (Syafi'i, Maliki, Hanbali / bayangan Ashar 1x)
        }

    val aladhanTune: String
        get() = when (this) {
            MUHAMMADIYAH -> "0,2,-1,1,1,3,0,2,0"
            KEMENAG -> "0,0,-1,1,1,3,0,2,0"
            MUIS, JAKIM, BRUNEI -> "0,0,0,0,0,0,0,0,0"
            else -> "0,0,0,0,0,0,0,0,0"
        }

    companion object {
        val defaultMethod: PrayerCalculationMethod = KEMENAG

        fun fromRawValue(raw: String?): PrayerCalculationMethod =
            entries.firstOrNull { it.rawValue == raw } ?: defaultMethod

        fun fromAladhanId(id: Int): PrayerCalculationMethod =
            entries.firstOrNull { it.aladhanMethodId == id } ?: defaultMethod

        fun forCountryCode(code: String): PrayerCalculationMethod = when (code.uppercase()) {
            "ID" -> KEMENAG
            "SG" -> MUIS
            "MY" -> JAKIM
            "BN" -> BRUNEI
            "PK", "IN", "BD" -> KARACHI
            "SA" -> UMM_AL_QURA
            "EG" -> EGYPTIAN
            "TR" -> TURKEY
            "FR" -> FRANCE
            "US", "CA" -> ISNA
            "AE" -> DUBAI
            "KW" -> KUWAIT
            "QA" -> QATAR
            "MA" -> MOROCCO
            "DZ" -> ALGERIA
            "TN" -> TUNISIA
            "JO" -> JORDAN
            "IR" -> TEHRAN
            "RU" -> RUSSIA
            "PT" -> LISBON
            else -> MWL
        }
    }
}
