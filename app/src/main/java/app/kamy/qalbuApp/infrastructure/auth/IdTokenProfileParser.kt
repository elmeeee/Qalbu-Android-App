package app.kamy.qalbuApp.infrastructure.auth

import android.util.Base64
import org.json.JSONObject

/** Display fields parsed from the OIDC id_token (fallback before Reflect profile loads). */
data class IdTokenProfile(
    val displayName: String?,
    val username: String?,
    val pictureUrl: String?
)

object IdTokenProfileParser {
    fun parse(idToken: String): IdTokenProfile? = runCatching {
        val parts = idToken.split('.')
        if (parts.size < 2) return@runCatching null
        val decoded = Base64.decode(
            parts[1],
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        val json = JSONObject(String(decoded, Charsets.UTF_8))
        val name = json.optString("name").takeIf { it.isNotEmpty() }
        val given = json.optString("given_name").takeIf { it.isNotEmpty() }
        val family = json.optString("family_name").takeIf { it.isNotEmpty() }
        val composed = listOfNotNull(given, family).joinToString(" ").takeIf { it.isNotEmpty() }
        IdTokenProfile(
            displayName = name ?: composed,
            username = json.optString("preferred_username").takeIf { it.isNotEmpty() },
            pictureUrl = json.optString("picture").takeIf { it.isNotEmpty() }
        )
    }.getOrNull()
}
