package app.kamy.saatApp.infrastructure.ai

import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.domain.model.GroqChatMessage
import app.kamy.saatApp.domain.model.GroqChatRequest
import app.kamy.saatApp.domain.model.GroqChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

sealed class AiReflectionException(message: String) : Exception(message) {
    class MissingApiKey : AiReflectionException(
        "Add API_KEY_GROQ to local.properties to draft reflections with AI."
    )
    class EmptyResponse : AiReflectionException("AI returned an empty response. Try again.")
    class HttpError(code: Int, body: String?) : AiReflectionException(
        "AI request failed ($code)${body?.let { ": $it" } ?: ""}"
    )
}

@Singleton
class AiReflectionRepository @Inject constructor(
    @javax.inject.Named("groq") private val httpClient: OkHttpClient,
    private val json: Json
) {
    suspend fun complete(
        system: String,
        user: String,
        temperature: Double = 0.35
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = AppConfig.groqApiKey ?: return@withContext null
        val body = json.encodeToString(
            GroqChatRequest.serializer(),
            GroqChatRequest(
                model = AppConfig.aiModel,
                messages = listOf(
                    GroqChatMessage(role = "system", content = system),
                    GroqChatMessage(role = "user", content = user)
                ),
                temperature = temperature
            )
        )
        val request = Request.Builder()
            .url(GROQ_CHAT_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody(JSON_MEDIA))
            .build()
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw AiReflectionException.HttpError(response.code, raw.take(200))
                }
                val parsed = json.decodeFromString(GroqChatResponse.serializer(), raw)
                val cleaned = sanitizeModelOutput(
                    parsed.choices.firstOrNull()?.message?.content.orEmpty()
                )
                cleaned.ifBlank { null }
            }
        }.getOrNull()
    }

    private fun sanitizeModelOutput(raw: String): String =
        raw
            .replace(Regex("(?is)<think>.*?</think>"), "")
            .replace(Regex("(?is)\\[think\\].*?\\[/think\\]"), "")
            .replace("```", "")
            .trim()

    companion object {
        private const val GROQ_CHAT_URL = "https://api.groq.com/openai/v1/chat/completions"
        private val JSON_MEDIA = "application/json".toMediaType()
    }
}
