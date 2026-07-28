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

            val messageText = env.empathyText
                .trim()
                .ifBlank { fallbackCompanionText(currentLang, verseData, doaData) }

            return@withContext SaatChatMessage(
                id = UUID.randomUUID().toString(),
                sender = ChatSender.AI,
                text = messageText,
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
            1. First answer the user's actual question directly in 4 to 6 sentences. Be practical, emotionally intelligent, and specific to the user's situation.
            2. Do NOT begin by quoting, summarizing, or immediately recommending a Quran verse.
            3. Only recommend ONE Quranic verse OR ONE doa if it truly helps after you have already answered the user directly.
            4. If no verse or doa is necessary, leave them null.
            5. Do not sound preachy, robotic, or generic. Sound like a thoughtful Muslim companion.
            3. Respond STRICTLY in raw JSON format matching this schema:
               {
                 "empathyText": "Your direct, supportive answer in $langName language",
                 "chapterNumber": 65,
                 "verseNumber": 3,
                 "doaSlug": "daily"
               }
            6. "empathyText" MUST be written 100% in $langName language.
            7. "chapterNumber" must be null or an integer between 1 and 114.
            8. "verseNumber" must be null or a valid integer verse number for that chapter.
            9. "doaSlug" must be null or a valid slug.
            10. Output ONLY the JSON object without markdown ``` tags or extra commentary.
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

        val messageText = createDirectFallbackAnswer(q, lang)

        return SaatChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.AI,
            text = messageText,
            verseData = verseData,
            doaData = null
        )
    }

    private fun createDirectFallbackAnswer(query: String, lang: AppLanguage): String = when {
        query.contains("kerja") || query.contains("job") || query.contains("karir") || query.contains("career") ||
            query.contains("interview") || query.contains("usaha") || query.contains("bisnis") ->
            when (lang) {
                AppLanguage.INDONESIAN ->
                    "Kalau urusan kerja sedang berat, fokus dulu pada langkah yang paling dekat: rapikan ikhtiar harian, kirim peluang baru secara konsisten, dan jangan biarkan penolakan membuatmu menilai dirimu gagal. Rezeki sering datang setelah proses yang melelahkan, jadi jaga ritme, evaluasi strategi, dan tetap minta pertolongan Allah dengan hati yang tenang. Aku sertakan ayat pendamping kalau kamu ingin merenungkannya setelah ini."
                AppLanguage.ENGLISH ->
                    "If work or career feels heavy right now, focus on the nearest concrete step: refine your daily effort, apply consistently, and do not let rejection define your worth. Provision often arrives after a tiring process, so keep your rhythm, review your strategy, and ask Allah for help with a steady heart. I included a supporting verse in case you want to reflect on it afterward."
                AppLanguage.MALAY ->
                    "Jika urusan kerja terasa berat sekarang, fokus dahulu pada langkah yang paling dekat: kemaskan usaha harian, mohon peluang secara konsisten, dan jangan biarkan penolakan menentukan nilai dirimu. Rezeki sering hadir selepas proses yang meletihkan, jadi jaga rentak, nilai semula strategi, dan mohon pertolongan Allah dengan hati yang tenang. Saya sertakan ayat pendamping jika anda mahu merenunginya selepas ini."
            }

        query.contains("jodoh") || query.contains("nikah") || query.contains("pasangan") || query.contains("suami") ||
            query.contains("istri") || query.contains("cinta") ->
            when (lang) {
                AppLanguage.INDONESIAN ->
                    "Kalau hatimu sedang lelah soal jodoh atau hubungan, jangan paksa jawaban yang belum Allah bukakan. Fokuslah pada menjaga harga diri, kejernihan niat, dan kualitas dirimu, karena pasangan yang baik juga butuh diri yang siap. Ambil keputusan pelan-pelan, lihat akhlak dan ketenangan yang hadir, bukan cuma rasa takut kehilangan."
                AppLanguage.ENGLISH ->
                    "If your heart feels tired about marriage or relationships, do not force an answer that Allah has not opened yet. Focus on your dignity, clarity of intention, and personal readiness, because a good partner also requires a prepared self. Move slowly, and judge by character and inner calm, not only by fear of losing someone."
                AppLanguage.MALAY ->
                    "Jika hati anda penat tentang jodoh atau hubungan, jangan paksa jawapan yang Allah belum bukakan. Fokus pada maruah diri, kejelasan niat, dan kesiapan peribadi, kerana pasangan yang baik juga memerlukan diri yang bersedia. Buat keputusan perlahan-lahan, dan nilai melalui akhlak serta ketenangan, bukan sekadar takut kehilangan."
            }

        query.contains("cemas") || query.contains("khawatir") || query.contains("anxious") || query.contains("risau") ||
            query.contains("gelisah") || query.contains("takut") || query.contains("stres") || query.contains("panik") ->
            when (lang) {
                AppLanguage.INDONESIAN ->
                    "Kalau kamu sedang cemas, jangan tuntut dirimu menyelesaikan seluruh masa depan hari ini. Kecilkan fokus ke hal yang bisa kamu kendalikan dalam beberapa jam ke depan, perlambat napas, lalu beri nama pada ketakutanmu satu per satu supaya pikiran tidak kabur. Setelah hati sedikit stabil, baru ambil satu keputusan kecil yang paling masuk akal."
                AppLanguage.ENGLISH ->
                    "If you are anxious, do not demand that you solve your entire future today. Narrow your focus to what you can control in the next few hours, slow your breathing, and name your fears one by one so your mind stops spinning. Once your heart settles a little, take the smallest sensible next step."
                AppLanguage.MALAY ->
                    "Jika anda sedang cemas, jangan paksa diri menyelesaikan seluruh masa depan hari ini. Kecilkan fokus kepada perkara yang boleh anda kawal dalam beberapa jam akan datang, perlahankan nafas, dan namakan ketakutan anda satu per satu supaya fikiran tidak berserabut. Setelah hati sedikit tenang, ambil satu langkah kecil yang paling munasabah."
            }

        query.contains("sedih") || query.contains("kecewa") || query.contains("sad") || query.contains("duka") ||
            query.contains("menangis") || query.contains("patah") || query.contains("hancur") ->
            when (lang) {
                AppLanguage.INDONESIAN ->
                    "Kalau kamu sedang sedih, tidak apa-apa mengakui bahwa ini memang berat. Jangan terburu-buru memaksa dirimu terlihat kuat; beri ruang untuk pulih, bicara pada orang yang aman, dan lakukan hal kecil yang menjaga tubuhmu tetap terurus. Kesedihan tidak selalu hilang cepat, tapi hati yang dijaga perlahan akan kembali lapang."
                AppLanguage.ENGLISH ->
                    "If you are hurting, it is okay to admit that this really is heavy. Do not rush to force yourself to look strong; give yourself room to recover, speak to someone safe, and do small things that keep your body cared for. Sadness may not leave quickly, but a protected heart slowly becomes spacious again."
                AppLanguage.MALAY ->
                    "Jika anda sedang sedih, tidak mengapa mengakui bahawa ini memang berat. Jangan tergesa-gesa memaksa diri kelihatan kuat; beri ruang untuk pulih, bercakap dengan orang yang selamat, dan lakukan hal kecil yang menjaga tubuh anda. Kesedihan mungkin tidak hilang segera, tetapi hati yang dijaga perlahan-lahan akan kembali lapang."
            }

        else -> when (lang) {
            AppLanguage.INDONESIAN ->
                "Aku mendengarmu. Coba jelaskan bagian yang paling berat atau paling membingungkan dari situasimu, lalu kita pecah jadi langkah yang lebih kecil dan lebih jernih. Kalau perlu, aku bisa menemanimu dengan jawaban langsung dulu, baru setelah itu ayat atau doa yang paling relevan."
            AppLanguage.ENGLISH ->
                "I hear you. Try telling me which part of your situation feels heaviest or most confusing, and we can break it into smaller, clearer steps. If needed, I can stay with direct guidance first, then offer a verse or dua only afterward."
            AppLanguage.MALAY ->
                "Saya mendengarmu. Cuba jelaskan bahagian yang paling berat atau paling mengelirukan dalam situasi anda, dan kita akan pecahkan kepada langkah yang lebih kecil dan jelas. Jika perlu, saya akan jawab secara langsung dahulu, kemudian barulah ayat atau doa yang paling relevan."
        }
    }

    private fun fallbackCompanionText(
        lang: AppLanguage,
        verseData: SaatVerseCardData?,
        doaData: DoaItem?
    ): String {
        verseData?.translationText
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        doaData?.translation
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        return when (lang) {
            AppLanguage.INDONESIAN ->
                "Aku mendengarmu. Coba tenangkan hati sejenak, lalu renungkan ayat dan doa yang kupilih untuk menemanimu."
            AppLanguage.ENGLISH ->
                "I hear you. Take a quiet breath, then reflect on the verse and dua I selected to accompany you."
            AppLanguage.MALAY ->
                "Saya mendengarmu. Tenangkan hati seketika, lalu renungkan ayat dan doa yang dipilih untuk menemanimu."
        }
    }

    companion object {
        private const val GROQ_CHAT_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val DEFAULT_GROQ_MODEL = "openai/gpt-oss-20b"
        private val JSON_MEDIA = "application/json".toMediaType()
    }
}
