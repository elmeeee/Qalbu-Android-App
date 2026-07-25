package app.kamy.saatApp.infrastructure.preferences

import android.content.Context
import app.kamy.saatApp.domain.adhan.AdhanVoice
import app.kamy.saatApp.domain.adhan.FajrAdhanVoice
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

    private val _selectedFajrVoice = MutableStateFlow(currentFajrVoice())
    val selectedFajrVoice: StateFlow<FajrAdhanVoice> = _selectedFajrVoice.asStateFlow()

    fun currentVoice(): AdhanVoice = AdhanVoice.fromId(prefs.getString(KEY_VOICE_ID, null))
    fun currentFajrVoice(): FajrAdhanVoice = FajrAdhanVoice.fromId(prefs.getString(KEY_FAJR_VOICE_ID, null))

    fun setVoice(voice: AdhanVoice) {
        prefs.edit().putString(KEY_VOICE_ID, voice.id).apply()
        _selectedVoice.value = voice
    }

    fun setFajrVoice(voice: FajrAdhanVoice) {
        prefs.edit().putString(KEY_FAJR_VOICE_ID, voice.id).apply()
        _selectedFajrVoice.value = voice
    }

    companion object {
        private const val PREFS_NAME = "saat_adhan_prefs"
        private const val KEY_VOICE_ID = "selectedAdhanVoiceId"
        private const val KEY_FAJR_VOICE_ID = "selectedFajrAdhanVoiceId"

        fun from(context: Context): AdhanPreferencesStoreBase =
            AdhanPreferencesStoreBase(
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            )
    }
}

class AdhanPreferencesStoreBase(private val prefs: android.content.SharedPreferences) {
    fun currentVoice(): AdhanVoice = AdhanVoice.fromId(prefs.getString(KEY_VOICE_ID, null))
    fun currentFajrVoice(): FajrAdhanVoice = FajrAdhanVoice.fromId(prefs.getString(KEY_FAJR_VOICE_ID, null))

    companion object {
        private const val KEY_VOICE_ID = "selectedAdhanVoiceId"
        private const val KEY_FAJR_VOICE_ID = "selectedFajrAdhanVoiceId"
    }
}

