package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context

class ReaderOnboardingStore(context: Context) {
    private val prefs = (context.applicationContext ?: context)
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasShownScrollHint(): Boolean = prefs.getBoolean(KEY_SCROLL_HINT, false)

    fun markScrollHintShown() {
        prefs.edit().putBoolean(KEY_SCROLL_HINT, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "qalbu_reader_onboarding"
        private const val KEY_SCROLL_HINT = "chapter_scroll_hint_shown"

        fun from(context: Context): ReaderOnboardingStore = ReaderOnboardingStore(context)
    }
}
