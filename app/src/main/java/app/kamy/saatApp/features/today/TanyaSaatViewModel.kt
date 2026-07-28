package app.kamy.saatApp.features.today

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.ChatSender
import app.kamy.saatApp.domain.model.DoaItem
import app.kamy.saatApp.domain.model.SaatChatMessage
import app.kamy.saatApp.domain.model.SaatMood
import app.kamy.saatApp.domain.model.SaatVerseCardData
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import app.kamy.saatApp.infrastructure.repository.TanyaSaatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import app.kamy.saatApp.core.locale.AppLanguage
import java.util.UUID
import javax.inject.Inject

data class TanyaSaatUiState(
    val messages: List<SaatChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val activeMood: SaatMood? = null,
    val inputText: String = "",
    val isSheetVisible: Boolean = false,
    val toastMessage: String? = null
)

@HiltViewModel
class TanyaSaatViewModel @Inject constructor(
    private val repository: TanyaSaatRepository,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {

    private val _state = MutableStateFlow(TanyaSaatUiState())
    val state: StateFlow<TanyaSaatUiState> = _state.asStateFlow()

    fun openSheet() {
        _state.update { it.copy(isSheetVisible = true) }
    }

    fun closeSheet() {
        _state.update { it.copy(isSheetVisible = false) }
    }

    fun onInputTextChanged(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun onMoodSelected(mood: SaatMood) {
        _state.update { it.copy(activeMood = mood, inputText = mood.promptQuery) }
        sendMessage(mood.promptQuery)
    }

    fun sendMessage(customQuery: String? = null) {
        val query = customQuery ?: _state.value.inputText.trim()
        if (query.isBlank() || _state.value.isLoading) return

        val userMessage = SaatChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.USER,
            text = query
        )

        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            val aiResponse = runCatching {
                repository.processUserQuery(query)
            }.getOrElse {
                SaatChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = ChatSender.AI,
                    text = fallbackErrorMessage()
                )
            }
            _state.update {
                it.copy(
                    messages = it.messages + aiResponse,
                    isLoading = false
                )
            }
        }
    }

    fun bookmarkVerse(context: Context, verseData: SaatVerseCardData) {
        val key = verseData.verseKey
        QuranPersonalStore.toggleBookmark(
            context = context,
            verseKey = key,
            chapterNumber = verseData.chapterNumber,
            verseNumber = verseData.verseNumber,
            surahLabel = verseData.surahName
        )
        _state.update {
            it.copy(toastMessage = context.getString(R.string.doa_saved_toast))
        }
    }

    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }

    private fun fallbackErrorMessage(): String = when (appLanguageStore.current()) {
        AppLanguage.INDONESIAN ->
            "Balasan Sahabat Saat belum berhasil dimuat. Coba kirim lagi dalam beberapa saat."
        AppLanguage.ENGLISH ->
            "Sahabat Saat could not load a reply yet. Please try sending your message again shortly."
        AppLanguage.MALAY ->
            "Balasan Sahabat Saat belum berjaya dimuat. Cuba hantar semula sebentar lagi."
    }
}
