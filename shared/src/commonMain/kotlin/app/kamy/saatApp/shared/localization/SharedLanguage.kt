package app.kamy.saatApp.shared.localization

enum class SharedLanguage(val code: String, val displayName: String) {
    INDONESIAN("id", "Bahasa Indonesia"),
    MALAY("ms", "Bahasa Melayu"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): SharedLanguage = when (code.lowercase()) {
            "id", "in", "indonesia", "indonesian" -> INDONESIAN
            "ms", "my", "melayu", "malay" -> MALAY
            else -> ENGLISH
        }
    }
}
