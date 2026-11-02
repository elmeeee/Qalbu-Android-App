package app.kamy.saatApp.features.today

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.ChatSender
import app.kamy.saatApp.domain.model.KimiAiModel
import app.kamy.saatApp.domain.model.SaatChatMessage
import app.kamy.saatApp.domain.model.SaatMood
import app.kamy.saatApp.domain.model.SaatVerseCardData
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.KimiModelPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import app.kamy.saatApp.infrastructure.repository.TanyaSaatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class TanyaSaatUiState(
    val messages: List<SaatChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val selectedModel: KimiAiModel = KimiAiModel.GPT_OSS_120B,
    val activeMood: SaatMood? = null,
    val inputText: String = "",
    val isSheetVisible: Boolean = false,
    val toastMessage: String? = null
)

@HiltViewModel
class TanyaSaatViewModel @Inject constructor(
    private val repository: TanyaSaatRepository,
    private val appLanguageStore: AppLanguageStore,
    private val kimiModelPreferencesStore: KimiModelPreferencesStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(TanyaSaatUiState())
    val state: StateFlow<TanyaSaatUiState> = _state.asStateFlow()

    private var lastUserQuery: String? = null

    init {
        viewModelScope.launch {
            kimiModelPreferencesStore.selectedModel.collect { model ->
                _state.update { it.copy(selectedModel = model) }
            }
        }
    }

    fun onModelSelected(model: KimiAiModel) {
        kimiModelPreferencesStore.setModel(model)
    }

    fun openSheet() {
        _state.update { it.copy(isSheetVisible = true) }
    }

    fun closeSheet() {
        _state.update { it.copy(isSheetVisible = false) }
    }

    fun onInputTextChanged(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    /** Starts a brand new conversation (clears history and input). */
    fun startNewChat() {
        if (_state.value.isLoading) return
        lastUserQuery = null
        _state.update {
            it.copy(messages = emptyList(), activeMood = null, inputText = "")
        }
    }

    /**
     * Suggestion chips resolve their prompt through a context pinned to the in-app language,
     * so an English UI always sends an English question even when the device locale differs.
     */
    fun onMoodSelected(mood: SaatMood) {
        val prompt = localizedContext().getString(mood.promptRes)
        _state.update { it.copy(activeMood = mood) }
        sendMessage(prompt)
    }

    fun askFollowUp(question: String) {
        sendMessage(question)
    }

    /** Re-sends the last user question (used after a failed reply). */
    fun retryLast() {
        val query = lastUserQuery ?: return
        if (_state.value.isLoading) return
        // Drop the trailing error bubble so the retry replaces it.
        _state.update { state ->
            val trimmed = state.messages.dropLastWhile { it.sender == ChatSender.AI && it.isError }
            state.copy(messages = trimmed)
        }
        requestAnswer(query, appendUserMessage = false)
    }

    fun sendMessage(customQuery: String? = null) {
        val query = (customQuery ?: _state.value.inputText).trim()
        if (query.isBlank() || _state.value.isLoading) return
        requestAnswer(query, appendUserMessage = true)
    }

    private fun requestAnswer(query: String, appendUserMessage: Boolean) {
        lastUserQuery = query
        val history = _state.value.messages

        _state.update { state ->
            val withUser = if (appendUserMessage) {
                state.messages + SaatChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = ChatSender.USER,
                    text = query
                )
            } else {
                state.messages
            }
            state.copy(messages = withUser, inputText = "", isLoading = true)
        }

        viewModelScope.launch {
            val aiResponse = runCatching {
                repository.processUserQuery(
                    userQuery = query,
                    history = history,
                    model = _state.value.selectedModel
                )
            }.getOrElse {
                SaatChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = ChatSender.AI,
                    text = fallbackErrorMessage(),
                    isError = true
                )
            }
            _state.update {
                it.copy(messages = it.messages + aiResponse, isLoading = false)
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

    fun showToast(message: String) {
        _state.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }

    private fun localizedContext(): Context {
        val locale = Locale.forLanguageTag(appLanguageStore.current().tag)
        val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return runCatching { context.createConfigurationContext(config) }.getOrDefault(context)
    }

    private fun fallbackErrorMessage(): String = when (appLanguageStore.current()) {
        AppLanguage.INDONESIAN ->
            "Balasan Kīmi belum berhasil dimuat. Coba kirim lagi dalam beberapa saat."
        AppLanguage.ENGLISH ->
            "Kīmi could not load a reply yet. Please try sending your message again shortly."
        AppLanguage.MALAY ->
            "Balasan Kīmi belum berjaya dimuat. Cuba hantar semula sebentar lagi."
    }
}
