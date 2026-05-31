package app.kamy.qalbuApp.infrastructure.preferences

import android.content.Context
import app.kamy.qalbuApp.domain.adhan.AdhanVoice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdhanPreferencesStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _selectedVoice = MutableStateFlow(currentVoice())
    val selectedVoice: StateFlow<AdhanVoice> = _selectedVoice.asStateFlow()

    fun currentVoice(): AdhanVoice = AdhanVoice.fromId(prefs.getString(KEY_VOICE_ID, null))

    fun setVoice(voice: AdhanVoice) {
        prefs.edit().putString(KEY_VOICE_ID, voice.id).apply()
        _selectedVoice.value = voice
    }

    companion object {
        private const val PREFS_NAME = "qalbu_adhan_prefs"
        private const val KEY_VOICE_ID = "selectedAdhanVoiceId"

        fun from(context: Context): AdhanPreferencesStoreBase =
            AdhanPreferencesStoreBase(
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            )
    }
}

class AdhanPreferencesStoreBase(private val prefs: android.content.SharedPreferences) {
    fun currentVoice(): AdhanVoice = AdhanVoice.fromId(prefs.getString(KEY_VOICE_ID, null))

    companion object {
        private const val KEY_VOICE_ID = "selectedAdhanVoiceId"
    }
}
