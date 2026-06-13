package app.kamy.qalbuApp.core.config

import app.kamy.qalbuApp.BuildConfig

object AppConfig {

    // ---- Quran Foundation base URLs ----
    val qfApiBaseUrl: String = BuildConfig.QF_API_BASE_URL
    val qfOauthAuthorizeUrl: String = BuildConfig.QF_OAUTH_AUTHORIZE_URL
    val qfOauthTokenUrl: String = BuildConfig.QF_OAUTH_TOKEN_URL
    val qfOauthCallbackUrl: String = BuildConfig.QF_OAUTH_CALLBACK_URL
    val qfOauthAppCallbackUrl: String = BuildConfig.QF_OAUTH_APP_CALLBACK_URL

    // ---- OAuth client ----
    val qfOauthClientId: String = BuildConfig.QF_OAUTH_CLIENT_ID
    val qfOauthClientSecret: String? =
        BuildConfig.QF_OAUTH_CLIENT_SECRET.ifBlank { null }
    val qfOauthScopes: String = BuildConfig.QF_OAUTH_SCOPES

    // Client-credentials token for Content API (chapters, verses, juz, etc.)
    const val qfClientCredentialsScopes = "content"

    // ---- Content settings ----
    val defaultTranslationId: Int = BuildConfig.QF_DEFAULT_TRANSLATION_ID

    // ---- External services (non-QF) ----
    val versesWebBase: String = BuildConfig.QF_VERSES_WEB_BASE
    val alAdhanRoot: String = BuildConfig.QF_ALADHAN_ROOT

    // ---- Optional AI provider ----
    val groqApiKey: String? = BuildConfig.API_KEY_GROQ.ifBlank { null }
    val aiModel: String = BuildConfig.AI_MODEL

    object Prefix {
        const val contentAPI = "content/api/v4"
        const val searchAPI = "search/api/v1"
        const val quranReflect = "quran-reflect/v1"
        const val authV1 = "auth/v1"
    }

    object OAuth {
        const val directory = "oauth2"
        const val token = "token"
        const val introspect = "introspect"
    }

    object Content {
        const val chapters = "chapters"
        const val juzs = "juzs"
        fun juzById(id: Int) = "juzs/$id"
        const val versesRandom = "verses/random"
        const val resourcesRecitations = "resources/recitations"
        const val resourcesTranslations = "resources/translations"
        fun versesByChapter(chapterNumber: Int) = "verses/by_chapter/$chapterNumber"
        fun versesByJuz(juzNumber: Int) = "verses/by_juz/$juzNumber"
        fun verseByKey(key: String) = "verses/by_key/$key"
        fun hadithsByAyah(ayahKey: String) = "hadith_references/by_ayah/$ayahKey/hadiths"
        fun tafsirByAyah(resourceId: String, ayahKey: String) =
            "tafsirs/$resourceId/by_ayah/$ayahKey"
    }

    object Reflect {
        const val activityDays = "activity_days"
        const val posts = "posts"
        const val postsFeed = "posts/feed"
        const val postsMyPosts = "posts/my-posts"
        const val userProfile = "users/profile"
        fun postToggleLike(postId: String) = "posts/$postId/toggle-like"
    }

    object AuthV1 {
        const val readingSessions = "reading-sessions"
    }

    fun absoluteVerseMediaUrl(raw: String): String = when {
        raw.startsWith("//") -> "https:$raw"
        raw.startsWith("http") -> raw
        else -> "$versesWebBase/$raw"
    }
}
