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
            KEMENAG -> "Ministry of Religious Affairs (Kemenag)"
            MUIS -> "Majlis Ugama Islam Singapura (MUIS)"
            JAKIM -> "Jabatan Kemajuan Islam Malaysia (JAKIM)"
            BRUNEI -> "Majlis Ugama Islam Brunei (MUIB)"
            KARACHI -> "Karachi"
            TEHRAN -> "Tehran"
            JAFARI -> "Jafari"
            ISNA -> "ISNA"
            MWL -> "Muslim World League"
            UMM_AL_QURA -> "Umm Al-Qura"
            EGYPTIAN -> "Egyptian"
            MCW -> "Moonsighting Committee"
            GULF -> "Gulf Region"
            KUWAIT -> "Kuwait"
            QATAR -> "Qatar"
            DUBAI -> "Dubai"
            TUNISIA -> "Tunisia"
            ALGERIA -> "Algeria"
            MOROCCO -> "Morocco"
            JORDAN -> "Jordan"
            FRANCE -> "UOIF France"
            TURKEY -> "Diyanet Turkey"
            RUSSIA -> "Russia"
            LISBON -> "Lisbon"
        }

    val organization: String
        get() = when (this) {
            MUHAMMADIYAH -> "Persyarikatan Muhammadiyah"
            KEMENAG -> "Ministry of Religious Affairs (Kemenag)"
            MUIS -> "Majlis Ugama Islam Singapura"
            JAKIM -> "Jabatan Kemajuan Islam Malaysia"
            BRUNEI -> "Majlis Ugama Islam Brunei"
            KARACHI -> "University of Islamic Sciences, Karachi"
            TEHRAN -> "Institute of Geophysics, University of Tehran"
            JAFARI -> "Shia Ithna-Ashari (Leva Institute, Qom)"
            ISNA -> "Islamic Society of North America"
            MWL -> "Muslim World League"
            UMM_AL_QURA -> "Umm Al-Qura University, Makkah"
            EGYPTIAN -> "Egyptian General Authority of Survey"
            MCW -> "Moonsighting Committee Worldwide"
            GULF -> "Gulf Region"
            KUWAIT -> "Kuwait"
            QATAR -> "Qatar"
            DUBAI -> "Dubai (experimental)"
            TUNISIA -> "Tunisia"
            ALGERIA -> "Algeria"
            MOROCCO -> "Morocco"
            JORDAN -> "Ministry of Awqaf, Jordan"
            FRANCE -> "Union Organization islamic de France"
            TURKEY -> "Diyanet İşleri Başkanlığı"
            RUSSIA -> "Spiritual Administration of Muslims of Russia"
            LISBON -> "Comunidade Islamica de Lisboa"
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
            else -> null
        }

    val aladhanSchool: Int get() = 0

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
