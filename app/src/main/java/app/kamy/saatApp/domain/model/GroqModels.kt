package app.kamy.saatApp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GroqChatRequest(
    val model: String,
    val messages: List<GroqChatMessage>,
    val temperature: Double = 0.75,
    val maxTokens: Int = 1100,
    val responseFormat: GroqResponseFormat? = null
)

@Serializable
data class GroqResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class GroqChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqChatResponse(
    val choices: List<GroqChatChoice> = emptyList()
)

@Serializable
data class GroqChatChoice(
    val message: GroqChatMessage? = null
)
