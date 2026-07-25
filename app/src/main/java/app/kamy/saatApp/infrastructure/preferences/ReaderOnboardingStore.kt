package app.kamy.saatApp.infrastructure.preferences

import android.content.Context

class ReaderOnboardingStore(context: Context) {
    private val prefs = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasShownScrollHint(): Boolean = prefs.getBoolean(KEY_SCROLL_HINT, false)

    fun markScrollHintShown() {
        prefs.edit().putBoolean(KEY_SCROLL_HINT, true).apply()
    }

    fun hasShownQuranCoachMark(): Boolean = prefs.getBoolean(KEY_QURAN_COACH_MARK, false)

    fun markQuranCoachMarkShown() {
        prefs.edit().putBoolean(KEY_QURAN_COACH_MARK, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "saat_reader_onboarding"
        private const val KEY_SCROLL_HINT = "chapter_scroll_hint_shown"
        private const val KEY_QURAN_COACH_MARK = "quran_coach_mark_shown"

        fun from(context: Context): ReaderOnboardingStore = ReaderOnboardingStore(context)
    }
}
