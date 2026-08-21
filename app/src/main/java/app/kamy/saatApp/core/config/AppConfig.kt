package app.kamy.saatApp.core.config

import app.kamy.saatApp.BuildConfig

object AppConfig {

    // ---- AI provider ----
    val groqApiKey: String? = BuildConfig.API_KEY_GROQ.ifBlank { null }
    val aiModel: String = BuildConfig.AI_MODEL

    fun formatAudioUrl(raw: String): String = when {
        raw.startsWith("//") -> "https:$raw"
        else -> raw
    }
}
