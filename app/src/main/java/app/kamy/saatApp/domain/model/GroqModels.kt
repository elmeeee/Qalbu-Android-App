package app.kamy.saatApp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GroqChatRequest(
    val model: String,
    val messages: List<GroqChatMessage>,
    val temperature: Double = 0.75,
    val maxTokens: Int = 600
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
