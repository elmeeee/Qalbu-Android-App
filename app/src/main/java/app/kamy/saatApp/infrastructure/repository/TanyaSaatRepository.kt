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

        val systemPrompt = buildSystemPrompt(currentLang)
        
        var envelope: SaatAiResponseEnvelope? = null

        if (!apiKey.isNullOrBlank()) {
            runCatching {
                val requestBody = json.encodeToString(
                    GroqChatRequest.serializer(),
                    GroqChatRequest(
                        model = AppConfig.aiModel,
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

        // Smart Fallback envelope if AI call failed, returned null, or missing API Key
        val resolvedEnvelope = envelope ?: createSmartFallbackEnvelope(userQuery, currentLang)

        // Resolve authentic verse from Local ContentRepository if chapter & verse returned
        var verseData: SaatVerseCardData? = null
        if (resolvedEnvelope.chapterNumber != null && resolvedEnvelope.verseNumber != null) {
            val key = "${resolvedEnvelope.chapterNumber}:${resolvedEnvelope.verseNumber}"
            runCatching {
                val verse = contentRepository.getVerseByKey(key)
                if (verse != null) {
                    val chapters = contentRepository.getChapters()
                    val chapterMeta = chapters.firstOrNull { it.id == resolvedEnvelope.chapterNumber }
                    val uthmani = verse.textUthmani.orEmpty()
                    val indopak = verse.textIndopak.orEmpty()
                    val arabic = uthmani.ifBlank { indopak }
                    val translation = verse.translations?.firstOrNull()?.text.orEmpty()

                    verseData = SaatVerseCardData(
                        chapterNumber = resolvedEnvelope.chapterNumber,
                        verseNumber = resolvedEnvelope.verseNumber,
                        surahName = chapterMeta?.nameSimple ?: "Surah ${resolvedEnvelope.chapterNumber}",
                        arabicText = arabic,
                        translationText = translation,
                        verseKey = key
                    )
                }
            }
        }

        // Resolve authentic doa from DoaRepository if slug provided
        var doaData: DoaItem? = null
        if (!resolvedEnvelope.doaSlug.isNullOrBlank()) {
            runCatching {
                val slug = resolvedEnvelope.doaSlug
                val dailyDoas = doaRepository.getDailyDoas()
                doaData = dailyDoas.firstOrNull { it.id == slug || it.category == slug }
                    ?: doaRepository.getDoas(slug).firstOrNull()
            }
        }

        // Fallback default verse (Surah Ar-Ra'd 13:28) if no verse resolved
        if (verseData == null && doaData == null) {
            runCatching {
                val defaultVerse = contentRepository.getVerseByKey("13:28")
                if (defaultVerse != null) {
                    val uthmani = defaultVerse.textUthmani.orEmpty()
                    val indopak = defaultVerse.textIndopak.orEmpty()
                    val arabic = uthmani.ifBlank { indopak }
                    val translation = defaultVerse.translations?.firstOrNull()?.text.orEmpty()

                    verseData = SaatVerseCardData(
                        chapterNumber = 13,
                        verseNumber = 28,
                        surahName = "Ar-Ra'd",
                        arabicText = arabic,
                        translationText = translation,
                        verseKey = "13:28"
                    )
                }
            }
        }

        SaatChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.AI,
            text = resolvedEnvelope.empathyText.ifBlank { getFallbackEmpathyText(currentLang) },
            verseData = verseData,
            doaData = doaData
        )
    }

    private fun buildSystemPrompt(lang: AppLanguage): String {
        val langName = lang.aiPromptLanguage

        return """
            You are "Tanya Sāat AI", a deeply wise, empathetic, uplifting, and respectful Islamic spiritual companion inside the Sāat app.
            Your mission is to listen attentively to ANY question, feelings, or life situation shared by the user (such as job searching, career, financial difficulty, marriage/jodoh, anxiety, grief, health, exams, temptation, or daily motivation) and always respond with warm advice and ONE specific authentic Quranic verse reference (chapterNumber and verseNumber).

            CORE INSTRUCTIONS:
            1. No matter what the user asks or talks about, ALWAYS answer thoughtfully and offer spiritual comfort.
            2. Match the topic to a relevant Quranic verse:
               - Job search / Career / Rezeki -> Recommend Surah At-Talaq (Chapter 65, Verse 2 or 3) or Surah Al-Jumu'ah (Chapter 62, Verse 10).
               - Marriage / Jodoh / Love -> Recommend Surah Ar-Rum (Chapter 30, Verse 21) or Surah Al-Furqan (Chapter 25, Verse 74).
               - Hardship / Trials -> Recommend Surah Ash-Sharh (Chapter 94, Verse 5 or 6) or Surah Al-Baqarah (Chapter 2, Verse 153 or 286).
               - Anxiety / Fear -> Recommend Surah Ar-Ra'd (Chapter 13, Verse 28).
               - Forgiveness / Repentance -> Recommend Surah Az-Zumar (Chapter 39, Verse 53).
               - Health / Healing -> Recommend Surah Ash-Shu'ara (Chapter 26, Verse 80).
            3. Respond STRICTLY in valid JSON matching this schema:
               {
                 "empathyText": "Warm, inspiring advice written in $langName language (3-4 sentences)",
                 "chapterNumber": 65,
                 "verseNumber": 3,
                 "doaSlug": "daily",
                 "keywords": ["job", "rezeki", "hope"]
               }
            4. "empathyText" MUST be written 100% in $langName.
            5. "chapterNumber" must be an integer (1 to 114).
            6. "verseNumber" must be a valid integer verse number.
            7. Return ONLY the raw JSON string without markdown wrapped ``` block or extra commentary.
        """.trimIndent()
    }

    private fun sanitizeJsonOutput(raw: String): String {
        return raw
            .replace(Regex("(?is)<think>.*?</think>"), "")
            .replace(Regex("(?is)\\[think\\].*?\\[/think\\]"), "")
            .replace("```json", "")
            .replace("```", "")
            .trim()
    }

    private fun createSmartFallbackEnvelope(userQuery: String, lang: AppLanguage): SaatAiResponseEnvelope {
        val q = userQuery.lowercase()
        return when {
            // Job search / Career / Rezeki / Work
            q.contains("kerja") || q.contains("job") || q.contains("karir") || q.contains("career") ||
            q.contains("rezeki") || q.contains("rejeki") || q.contains("usaha") || q.contains("ikhtiar") ||
            q.contains("lulus") || q.contains("interview") -> {
                SaatAiResponseEnvelope(
                    empathyText = getEmpathyForJobSearch(lang),
                    chapterNumber = 65,
                    verseNumber = 3
                )
            }
            // Marriage / Jodoh / Spouse
            q.contains("jodoh") || q.contains("nikah") || q.contains("kawin") || q.contains("pasangan") ||
            q.contains("spouse") || q.contains("suami") || q.contains("istri") || q.contains("isteri") -> {
                SaatAiResponseEnvelope(
                    empathyText = getEmpathyForMarriage(lang),
                    chapterNumber = 30,
                    verseNumber = 21
                )
            }
            // Financial / Debt / Money
            q.contains("hutang") || q.contains("utang") || q.contains("uang") || q.contains("rugi") ||
            q.contains("miskin") || q.contains("debt") || q.contains("money") || q.contains("ekonomi") -> {
                SaatAiResponseEnvelope(
                    empathyText = getEmpathyForFinancial(lang),
                    chapterNumber = 2,
                    verseNumber = 286
                )
            }
            // Anxiety / Worry / Restless
            q.contains("cemas") || q.contains("khawatir") || q.contains("anxious") || q.contains("risau") ||
            q.contains("gelisah") || q.contains("takut") || q.contains("fear") -> {
                SaatAiResponseEnvelope(
                    empathyText = getEmpathyForAnxiety(lang),
                    chapterNumber = 13,
                    verseNumber = 28
                )
            }
            // Sadness / Grief / Broken Heart
            q.contains("sedih") || q.contains("kecewa") || q.contains("sad") || q.contains("hurt") ||
            q.contains("duka") || q.contains("menangis") || q.contains("patah") -> {
                SaatAiResponseEnvelope(
                    empathyText = getEmpathyForSadness(lang),
                    chapterNumber = 94,
                    verseNumber = 6
                )
            }
            // Health / Healing / Sickness
            q.contains("sakit") || q.contains("sehat") || q.contains("sick") || q.contains("heal") ||
            q.contains("demam") || q.contains("obat") || q.contains("cure") -> {
                SaatAiResponseEnvelope(
                    empathyText = getEmpathyForHealth(lang),
                    chapterNumber = 26,
                    verseNumber = 80
                )
            }
            // Gratitude / Thankful
            q.contains("syukur") || q.contains("grateful") || q.contains("thank") || q.contains("nikmat") -> {
                SaatAiResponseEnvelope(
                    empathyText = getEmpathyForGratitude(lang),
                    chapterNumber = 14,
                    verseNumber = 7
                )
            }
            else -> {
                SaatAiResponseEnvelope(
                    empathyText = getFallbackEmpathyText(lang),
                    chapterNumber = 13,
                    verseNumber = 28
                )
            }
        }
    }

    private fun getEmpathyForJobSearch(lang: AppLanguage): String = when (lang) {
        AppLanguage.INDONESIAN -> "Tetaplah berikhtiar dan bertakwa, wahai saudaraku. Pencarian kerja adalah proses perjuangan. Yakinlah bahwa Allah telah menyiapkan pintu rezeki terbaik yang akan terbuka pada waktu paling indah."
        AppLanguage.ENGLISH -> "Keep searching and trusting in Allah, dear friend. Looking for a job is a noble journey. Have faith that Allah has already decreed the best sustenance for you at the perfect time."
        AppLanguage.MALAY -> "Teruskan berikhtiar dan bertawakal, wahai saudaraku. Mencarikan rezeki adalah satu perjuangan mulia. Yakinlah bahawa Allah telah menyediakan rezeki terbaik yang akan dibuka pada masa yang paling tepat."
    }

    private fun getEmpathyForMarriage(lang: AppLanguage): String = when (lang) {
        AppLanguage.INDONESIAN -> "Pencarian pasangan hidup membutuhkan kesabaran dan doa yang tak putus. Yakinlah Allah akan mempertemukanmu dengan jodoh terbaik yang menjadi penyejuk hatimu."
        AppLanguage.ENGLISH -> "Finding a life partner requires patience and heartfelt prayer. Trust that Allah will bring the right soulmate to comfort your heart at the best time."
        AppLanguage.MALAY -> "Mencari pasangan hidup memerlukan kesabaran dan doa yang berterusan. Yakinlah Allah akan pertemukan anda dengan jodoh terbaik sebagai penenang hati."
    }

    private fun getEmpathyForFinancial(lang: AppLanguage): String = when (lang) {
        AppLanguage.INDONESIAN -> "Allah tidak memperbebani seseorang melainkan sesuai dengan kesanggupannya. Percayalah bahwa setiap kesulitan keuangan akan Allah gantikan dengan jalan keluar dan kelapangan rezeki."
        AppLanguage.ENGLISH -> "Allah does not burden a soul beyond what it can bear. Believe that after financial hardship, Allah will grant ease and unexpected sustenance."
        AppLanguage.MALAY -> "Allah tidak membebani seseorang melainkan mengikut kesanggupannya. Percayalah bahawa di sebalik kesukaran kewangan, Allah akan kurniakan kelapangan rezeki."
    }

    private fun getEmpathyForHealth(lang: AppLanguage): String = when (lang) {
        AppLanguage.INDONESIAN -> "Semoga Allah memberikan kesembuhan, kekuatan, dan mengangkat segala rasa sakitmu. Sesungguhnya Allah lah Yang Maha Menyembuhkan hamba-Nya."
        AppLanguage.ENGLISH -> "May Allah grant you complete healing, strength, and ease. Truly, Allah is the ultimate Healer of all ailments."
        AppLanguage.MALAY -> "Semoga Allah kurniakan kesembuhan, kekuatan, dan diangkatkan segala kesakitan anda. Sesungguhnya Allah jua Yang Maha Penyembuh."
    }

    private fun getEmpathyForAnxiety(lang: AppLanguage): String = when (lang) {
        AppLanguage.INDONESIAN -> "Ingatlah bahwa kecemasan adalah hal yang manusiawi. Serahkan segala kekhawatiranmu kepada Allah, karena Dia Maha Mengatur segala urusan dengan sempurna."
        AppLanguage.ENGLISH -> "Remember that anxiety is human. Trust Allah with your worries, for He is the Best Planner and always near."
        AppLanguage.MALAY -> "Ingatlah bahawa kegelisahan itu lumrah. Serahkan segala kerisauan anda kepada Allah, kerana Dia sebaik-baik Perancang."
    }

    private fun getEmpathyForSadness(lang: AppLanguage): String = when (lang) {
        AppLanguage.INDONESIAN -> "Kesedihan yang kamu rasakan tidak akan bertahan selamanya. Di setiap kesulitan yang melanda, Allah selalu menyertakan kemudahan."
        AppLanguage.ENGLISH -> "The sadness you feel will not last forever. With every hardship, Allah always promises relief."
        AppLanguage.MALAY -> "Kesedihan yang anda alami tidak akan berkekalan. Di sebalik setiap kesukaran, Allah sentiasa menjanjikan kesenangan."
    }

    private fun getEmpathyForGratitude(lang: AppLanguage): String = when (lang) {
        AppLanguage.INDONESIAN -> "Alhamdulillah. Hati yang senantiasa bersyukur akan selalu diliputi ketenangan dan ditambah nikmatnya oleh Allah SWT."
        AppLanguage.ENGLISH -> "Alhamdulillah. A grateful heart is blessed with inner peace, and Allah increases His favors upon those who thank Him."
        AppLanguage.MALAY -> "Alhamdulillah. Hati yang sentiasa bersyukur akan dilimpahi ketenangan dan ditambah nikmat oleh Allah SWT."
    }

    private fun getFallbackEmpathyText(lang: AppLanguage): String = when (lang) {
        AppLanguage.INDONESIAN -> "Semoga Allah memberikan ketenangan dan petunjuk terbaik untuk setiap urusan hatimu."
        AppLanguage.ENGLISH -> "May Allah grant peace, clarity, and guidance to your heart in every step you take."
        AppLanguage.MALAY -> "Semoga Allah kurniakan ketenangan dan petunjuk terbaik buat hati anda."
    }

    companion object {
        private const val GROQ_CHAT_URL = "https://api.groq.com/openai/v1/chat/completions"
        private val JSON_MEDIA = "application/json".toMediaType()
    }
}
