package app.kamy.qalbuApp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Used by both OAuth client-credentials (Content API) and the refresh-token grant
 * response. Mirrors iOS Domain/Models/TokenResponse.swift.
 *
 * Field naming uses explicit @SerialName because OAuth servers can vary between
 * snake_case and camelCase; we accept the canonical RFC 6749 snake_case form.
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int = 3600,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("scope") val scope: String? = null
)
