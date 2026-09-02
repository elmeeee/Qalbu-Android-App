package app.kamy.saatApp.core.analytics

import com.posthog.PostHog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Centralized, high-performance, asynchronous analytics tracking engine for Qalbu / Sāat.
 *
 * Guarantees:
 * 1. Zero Main-Thread overhead: all analytics capture operations execute asynchronously on [Dispatchers.IO].
 * 2. Fail-safe: all invocations are protected with [runCatching] so analytics failures can NEVER crash the app.
 * 3. Type-safe events for Al-Qur'an, Audio, Worship tools, Prayers, and Tanya Sāat AI.
 */
object AppAnalytics {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun capture(event: String, properties: Map<String, Any?> = emptyMap()) {
        scope.launch {
            runCatching {
                val nonNullProps = mutableMapOf<String, Any>()
                properties.forEach { (k, v) ->
                    if (v != null) nonNullProps[k] = v
                }
                if (nonNullProps.isEmpty()) {
                    PostHog.capture(event)
                } else {
                    PostHog.capture(event, properties = nonNullProps)
                }
            }
        }
    }

    // ==========================================
    // 📖 AL-QUR'AN & TAFSIR ANALYTICS
    // ==========================================

    fun trackSurahOpened(surahNumber: Int, surahName: String, startingAyah: Int = 1) {
        capture(
            event = "quran_surah_opened",
            properties = mapOf(
                "surah_number" to surahNumber,
                "surah_name" to surahName,
                "starting_ayah" to startingAyah
            )
        )
    }

    fun trackAyahBookmarked(surahNumber: Int, ayahNumber: Int) {
        capture(
            event = "quran_ayah_bookmarked",
            properties = mapOf(
                "surah_number" to surahNumber,
                "ayah_number" to ayahNumber
            )
        )
    }

    fun trackAyahShared(surahNumber: Int, ayahNumber: Int, cardStyle: String = "default") {
        capture(
            event = "quran_ayah_shared",
            properties = mapOf(
                "surah_number" to surahNumber,
                "ayah_number" to ayahNumber,
                "card_style" to cardStyle
            )
        )
    }

    fun trackTafsirOpened(surahNumber: Int, ayahNumber: Int, tafsirSource: String = "kemenag") {
        capture(
            event = "quran_tafsir_opened",
            properties = mapOf(
                "surah_number" to surahNumber,
                "ayah_number" to ayahNumber,
                "tafsir_source" to tafsirSource
            )
        )
    }

    fun trackQuranAudioPlayback(
        action: String, // "play", "pause", "next", "prev", "complete"
        surahNumber: Int,
        ayahNumber: Int? = null,
        reciterName: String? = null
    ) {
        capture(
            event = "quran_audio_playback",
            properties = mapOf(
                "action" to action,
                "surah_number" to surahNumber,
                "ayah_number" to ayahNumber,
                "reciter_name" to reciterName
            )
        )
    }

    fun trackQuranSearch(query: String, resultsCount: Int) {
        capture(
            event = "quran_search",
            properties = mapOf(
                "query_length" to query.length,
                "results_count" to resultsCount
            )
        )
    }

    fun trackTajweedGuideOpened() {
        capture(event = "quran_tajweed_guide_opened")
    }

    // ==========================================
    // 🕌 WORSHIP TOOLS & SPIRITUAL ANALYTICS
    // ==========================================

    fun trackToolOpened(toolKey: String, title: String? = null) {
        capture(
            event = "worship_tool_opened",
            properties = mapOf(
                "tool_key" to toolKey,
                "tool_title" to title
            )
        )
    }

    fun trackDhikrSessionCompleted(
        title: String,
        targetCount: Int,
        actualCount: Int,
        durationSeconds: Long? = null
    ) {
        capture(
            event = "dhikr_session_completed",
            properties = mapOf(
                "dhikr_title" to title,
                "target_count" to targetCount,
                "actual_count" to actualCount,
                "duration_seconds" to durationSeconds
            )
        )
    }

    fun trackZakatCalculated(
        zakatType: String,
        isNisabReached: Boolean,
        totalZakatAmount: Double
    ) {
        capture(
            event = "zakat_calculated",
            properties = mapOf(
                "zakat_type" to zakatType,
                "is_nisab_reached" to isNisabReached,
                "has_amount" to (totalZakatAmount > 0.0)
            )
        )
    }

    fun trackFaraidhCalculated(
        madhhab: String,
        heirCount: Int,
        hasResult: Boolean
    ) {
        capture(
            event = "faraidh_calculated",
            properties = mapOf(
                "madhhab" to madhhab,
                "heir_count" to heirCount,
                "has_result" to hasResult
            )
        )
    }

    fun trackFidyahCalculated(
        fidyahType: String,
        missedDays: Int,
        yearsDelayed: Int
    ) {
        capture(
            event = "fidyah_calculated",
            properties = mapOf(
                "fidyah_type" to fidyahType,
                "missed_days" to missedDays,
                "years_delayed" to yearsDelayed
            )
        )
    }

    fun trackRadioStreamStarted(stationName: String) {
        capture(
            event = "quran_radio_played",
            properties = mapOf(
                "station_name" to stationName
            )
        )
    }

    fun trackQiblaCompassOpened() {
        capture(event = "qibla_compass_opened")
    }

    // ==========================================
    // ⏰ PRAYER TIMES & TRACKER ANALYTICS
    // ==========================================

    fun trackPrayerChecked(prayerName: String, isChecked: Boolean) {
        capture(
            event = "prayer_tracker_toggled",
            properties = mapOf(
                "prayer_name" to prayerName,
                "is_checked" to isChecked
            )
        )
    }

    fun trackPrayerCalendarOpened() {
        capture(event = "prayer_calendar_opened")
    }

    // ==========================================
    // 🤖 TANYA SĀAT (AI) ANALYTICS
    // ==========================================

    fun trackTanyaSaatPromptSent(promptLength: Int, model: String) {
        capture(
            event = "tanya_saat_prompt_sent",
            properties = mapOf(
                "prompt_length" to promptLength,
                "model" to model
            )
        )
    }

    fun trackTanyaSaatResponseReceived(durationMs: Long, isSuccess: Boolean) {
        capture(
            event = "tanya_saat_response_received",
            properties = mapOf(
                "duration_ms" to durationMs,
                "is_success" to isSuccess
            )
        )
    }

    // ==========================================
    // 🤝 COLLABORATION & GENERAL
    // ==========================================

    fun trackMasjidkuBannerClicked() {
        capture(
            event = "masjidku_collaboration_clicked",
            properties = mapOf(
                "target_url" to "https://masjidku.app"
            )
        )
    }

    // ==========================================
    // 🎛️ REMOTE CONFIGURATION & FEATURE FLAGS
    // ==========================================

    fun isFeatureEnabled(key: String, defaultValue: Boolean = true): Boolean {
        return runCatching {
            PostHog.isFeatureEnabled(key, defaultValue = defaultValue)
        }.getOrDefault(defaultValue)
    }

    fun getFeatureFlagPayload(key: String): Any? {
        return runCatching {
            PostHog.getFeatureFlagPayload(key)
        }.getOrNull()
    }

    fun reloadFeatureFlags(onFeatureFlags: (() -> Unit)? = null) {
        runCatching {
            PostHog.reloadFeatureFlags(onFeatureFlags = onFeatureFlags)
        }
    }
}
