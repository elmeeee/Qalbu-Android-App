package app.kamy.saatApp.infrastructure.preferences

import android.content.Context

object KhgtWidgetCache {
    private const val PREFS = "saat_khgt_widget"
    private const val KEY_EVENT_TITLE = "event_title"
    private const val KEY_HIJRI_LABEL = "hijri_label"
    private const val KEY_PASARAN = "pasaran"

    fun save(context: Context, hijriLabel: String?, pasaran: String?, eventTitle: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HIJRI_LABEL, hijriLabel)
            .putString(KEY_PASARAN, pasaran)
            .putString(KEY_EVENT_TITLE, eventTitle)
            .apply()
    }

    fun eventTitle(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_EVENT_TITLE, null)
            ?.takeIf { it.isNotBlank() }

    fun hijriLabel(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_HIJRI_LABEL, null)
            ?.takeIf { it.isNotBlank() }
}
