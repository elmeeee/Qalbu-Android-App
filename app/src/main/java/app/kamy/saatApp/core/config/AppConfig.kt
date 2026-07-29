package app.kamy.saatApp.core.config

import app.kamy.saatApp.BuildConfig

object AppConfig {

    // ---- Quran Foundation Content API ----
    // Quran text is bundled locally; only tafsir and hadith-by-ayah still call the API.
    val qfApiBaseUrl: String = BuildConfig.QF_API_BASE_URL
    val qfOauthTokenUrl: String = BuildConfig.QF_OAUTH_TOKEN_URL

    // ---- OAuth (client-credentials only; there is no user sign-in) ----
    val qfOauthClientId: String = BuildConfig.QF_OAUTH_CLIENT_ID
    val qfOauthClientSecret: String? =
        BuildConfig.QF_OAUTH_CLIENT_SECRET.ifBlank { null }

    const val qfClientCredentialsScopes = "content"

    // ---- Content settings ----
    val defaultTranslationId: Int = BuildConfig.QF_DEFAULT_TRANSLATION_ID

    // ---- External services ----
    val versesWebBase: String = BuildConfig.QF_VERSES_WEB_BASE

    // ---- AI provider ----
    val groqApiKey: String? = BuildConfig.API_KEY_GROQ.ifBlank { null }
    val aiModel: String = BuildConfig.AI_MODEL

    object Prefix {
        const val contentAPI = "content/api/v4"
    }

    fun absoluteVerseMediaUrl(raw: String): String = when {
        raw.startsWith("//") -> "https:$raw"
        raw.startsWith("http") -> raw
        else -> "$versesWebBase/$raw"
    }
}
