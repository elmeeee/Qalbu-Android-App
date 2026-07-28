package app.kamy.saatApp.infrastructure.repository

import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.ChatSender
import app.kamy.saatApp.domain.model.DoaItem
import app.kamy.saatApp.domain.model.GroqChatMessage
import app.kamy.saatApp.domain.model.GroqChatRequest
import app.kamy.saatApp.domain.model.GroqChatResponse
import app.kamy.saatApp.domain.model.SaatAiResponseEnvelope
import app.kamy.saatApp.domain.model.SaatChatMessage
import app.kamy.saatApp.domain.model.SaatVerseCardData
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TanyaSaatRepository @Inject constructor(
    @Named("groq") private val httpClient: OkHttpClient,
    private val json: Json,
    private val contentRepository: ContentRepository,
    private val doaRepository: DoaRepository,
    private val appLanguageStore: AppLanguageStore
) {
    suspend fun processUserQuery(userQuery: String): SaatChatMessage = withContext(Dispatchers.IO) {
        val currentLang = appLanguageStore.current()
        val apiKey = AppConfig.groqApiKey
        val aiModelName = AppConfig.aiModel.ifBlank { DEFAULT_GROQ_MODEL }

        val systemPrompt = buildSystemPrompt(currentLang)
        var envelope: SaatAiResponseEnvelope? = null

        if (!apiKey.isNullOrBlank()) {
            runCatching {
                val requestBody = json.encodeToString(
                    GroqChatRequest.serializer(),
                    GroqChatRequest(
                        model = aiModelName,
                        messages = listOf(
                            GroqChatMessage(role = "system", content = systemPrompt),
                            GroqChatMessage(role = "user", content = userQuery)
                        ),
                        temperature = 0.2
                    )
                )

                val request = Request.Builder()
                    .url(GROQ_CHAT_URL)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody(JSON_MEDIA))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val rawResponse = response.body?.string().orEmpty()
                        val parsedResponse = json.decodeFromString(GroqChatResponse.serializer(), rawResponse)
                        val content = parsedResponse.choices.firstOrNull()?.message?.content.orEmpty()
                        val cleanedJson = sanitizeJsonOutput(content)
                        envelope = json.decodeFromString(SaatAiResponseEnvelope.serializer(), cleanedJson)
                    }
                }
            }
        }

        // If AI call succeeded, resolve authentic verse and doa cards from local SQLite database (qurannew.db)
        if (envelope != null) {
            val env = envelope!!
            var verseData: SaatVerseCardData? = null
            if (env.chapterNumber != null && env.verseNumber != null) {
                val key = "${env.chapterNumber}:${env.verseNumber}"
                runCatching {
                    val verse = contentRepository.getVerseByKey(key)
                    if (verse != null) {
                        val chapters = contentRepository.getChapters()
                        val chapterMeta = chapters.firstOrNull { it.id == env.chapterNumber }
                        val uthmani = verse.textUthmani.orEmpty()
                        val indopak = verse.textIndopak.orEmpty()
                        val arabic = uthmani.ifBlank { indopak }
                        val translation = verse.translations?.firstOrNull()?.text.orEmpty()

                        verseData = SaatVerseCardData(
                            chapterNumber = env.chapterNumber!!,
                            verseNumber = env.verseNumber!!,
                            surahName = chapterMeta?.nameSimple ?: "Surah ${env.chapterNumber}",
                            arabicText = arabic,
                            translationText = translation,
                            verseKey = key
                        )
                    }
                }
            }

            var doaData: DoaItem? = null
            if (!env.doaSlug.isNullOrBlank()) {
                runCatching {
                    val slug = env.doaSlug!!
                    val dailyDoas = doaRepository.getDailyDoas()
                    doaData = dailyDoas.firstOrNull { it.id == slug || it.category == slug }
                        ?: doaRepository.getDoas(slug).firstOrNull()
                }
            }

            return@withContext SaatChatMessage(
                id = UUID.randomUUID().toString(),
                sender = ChatSender.AI,
                text = env.empathyText,
                verseData = verseData,
                doaData = doaData
            )
        }

        // Dynamic fallback from local SQLite database (qurannew.db) if network/API is offline
        return@withContext createDynamicDatabaseFallback(userQuery, currentLang)
    }

    private fun buildSystemPrompt(lang: AppLanguage): String {
        val langName = lang.aiPromptLanguage

        return """
            You are "Sahabat Sāat", an exceptionally wise, warm, empathetic, and knowledgeable Islamic spiritual companion inside the Sāat app.
            Your task is to listen attentively to ANY question, feeling, or life situation shared by the user (such as looking for a job/career, joblessness, marriage/jodoh, anxiety, grief, sadness, financial struggles, exams, health/illness, family, or general life guidance).

            CORE INSTRUCTIONS:
            1. Write a personalized, deeply comforting, and empathetic advice response addressing the user's specific query in 3 to 4 sentences.
            2. Recommend ONE specific, highly relevant Quranic verse (provide chapterNumber from 1 to 114, and verseNumber) or Doa category slug.
            3. Respond STRICTLY in raw JSON format matching this schema:
               {
                 "empathyText": "Your empathetic, inspiring, personalized advice in $langName language",
                 "chapterNumber": 65,
                 "verseNumber": 3,
                 "doaSlug": "daily"
               }
            4. "empathyText" MUST be written 100% in $langName language.
            5. "chapterNumber" must be an integer between 1 and 114.
            6. "verseNumber" must be a valid integer verse number for that chapter.
            7. Output ONLY the JSON object without markdown ``` tags or extra commentary.
        """.trimIndent()
    }

    private fun sanitizeJsonOutput(raw: String): String {
        val cleaned = raw
            .replace(Regex("(?is)<think>.*?</think>"), "")
            .replace(Regex("(?is)\\[think\\].*?\\[/think\\]"), "")
            .replace("```json", "")
            .replace("```", "")
            .trim()
        val jsonMatch = Regex("(?s)\\{.*\\}").find(cleaned)
        return jsonMatch?.value ?: cleaned
    }

    private suspend fun createDynamicDatabaseFallback(userQuery: String, lang: AppLanguage): SaatChatMessage {
        val q = userQuery.lowercase()
        val (chap, ayah) = when {
            q.contains("kerja") || q.contains("job") || q.contains("karir") || q.contains("career") ||
            q.contains("rezeki") || q.contains("rejeki") || q.contains("usaha") || q.contains("interview") ||
            q.contains("gaji") || q.contains("bisnis") -> Pair(65, 3)

            q.contains("jodoh") || q.contains("nikah") || q.contains("pasangan") || q.contains("suami") ||
            q.contains("istri") || q.contains("cinta") -> Pair(30, 21)

            q.contains("hutang") || q.contains("utang") || q.contains("uang") || q.contains("rugi") ||
            q.contains("ekonomi") || q.contains("miskin") -> Pair(2, 286)

            q.contains("cemas") || q.contains("khawatir") || q.contains("anxious") || q.contains("risau") ||
            q.contains("gelisah") || q.contains("takut") || q.contains("stres") || q.contains("panik") -> Pair(13, 28)

            q.contains("sedih") || q.contains("kecewa") || q.contains("sad") || q.contains("duka") ||
            q.contains("menangis") || q.contains("patah") || q.contains("hancur") -> Pair(94, 6)

            q.contains("sakit") || q.contains("sehat") || q.contains("sick") || q.contains("heal") ||
            q.contains("demam") || q.contains("obat") || q.contains("sembuh") -> Pair(26, 80)

            q.contains("syukur") || q.contains("grateful") || q.contains("thank") || q.contains("nikmat") ||
            q.contains("alhamdulillah") || q.contains("bahagia") -> Pair(14, 7)

            q.contains("bingung") || q.contains("petunjuk") || q.contains("solusi") || q.contains("ragu") -> Pair(2, 186)

            q.contains("marah") || q.contains("emosi") || q.contains("angry") || q.contains("kesal") -> Pair(3, 134)

            q.contains("ujian") || q.contains("exam") || q.contains("sekolah") || q.contains("belajar") -> Pair(94, 5)

            q.contains("dosa") || q.contains("tobat") || q.contains("taubat") || q.contains("ampun") -> Pair(39, 53)

            else -> {
                val fallbackPool = listOf(
                    Pair(2, 186), Pair(94, 5), Pair(3, 139), Pair(8, 30),
                    Pair(62, 10), Pair(9, 129), Pair(35, 2), Pair(57, 22),
                    Pair(93, 5), Pair(2, 153), Pair(65, 2)
                )
                val idx = (userQuery.hashCode() and 0x7FFFFFFF) % fallbackPool.size
                fallbackPool[idx]
            }
        }

        val key = "$chap:$ayah"
        val verse = runCatching { contentRepository.getVerseByKey(key) }.getOrNull()
        val chapters = runCatching { contentRepository.getChapters() }.getOrNull().orEmpty()
        val chapterMeta = chapters.firstOrNull { it.id == chap }

        val verseData = if (verse != null) {
            val uthmani = verse.textUthmani.orEmpty()
            val indopak = verse.textIndopak.orEmpty()
            val arabic = uthmani.ifBlank { indopak }
            val translation = verse.translations?.firstOrNull()?.text.orEmpty()

            SaatVerseCardData(
                chapterNumber = chap,
                verseNumber = ayah,
                surahName = chapterMeta?.nameSimple ?: "Surah $chap",
                arabicText = arabic,
                translationText = translation,
                verseKey = key
            )
        } else null

        val messageText = verseData?.translationText?.ifBlank { null }
            ?: when (lang) {
                AppLanguage.INDONESIAN -> "Semoga Allah memberikan petunjuk dan ketenangan untuk hatimu melalui ayat Al-Qur'an."
                AppLanguage.ENGLISH -> "May Allah grant guidance and comfort to your heart through the verses of the Quran."
                AppLanguage.MALAY -> "Semoga Allah kurniakan petunjuk dan ketenangan buat hati anda melalui ayat Al-Qur'an."
            }

        return SaatChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.AI,
            text = messageText,
            verseData = verseData,
            doaData = null
        )
    }

    companion object {
        private const val GROQ_CHAT_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val DEFAULT_GROQ_MODEL = "openai/gpt-oss-20b"
        private val JSON_MEDIA = "application/json".toMediaType()
    }
}
