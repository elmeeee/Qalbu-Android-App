package app.kamy.saatApp.core.error

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ApiErrorBody(
    val message: String? = null,
    val type: String? = null,
    val success: Boolean? = null
)

private val apiErrorJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

fun parseApiErrorBody(raw: String?): ApiErrorBody? {
    val text = raw?.trim().orEmpty()
    if (text.isEmpty() || !text.startsWith("{")) return null
    return runCatching { apiErrorJson.decodeFromString<ApiErrorBody>(text) }.getOrNull()
}
