package app.kamy.qalbuApp.features.quran

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.core.config.AppConfig
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.core.error.toAppError
import app.kamy.qalbuApp.core.error.invalidateIfAuthenticationFailure
import app.kamy.qalbuApp.core.error.isAuthenticationFailure
import app.kamy.qalbuApp.domain.model.HadithReference
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.domain.model.RecitationPayload
import app.kamy.qalbuApp.domain.model.TafsirPayload
import app.kamy.qalbuApp.domain.share.VerseShareTextComposer
import app.kamy.qalbuApp.infrastructure.audio.AudioQueueItem
import app.kamy.qalbuApp.infrastructure.audio.AudioPlayerController
import app.kamy.qalbuApp.infrastructure.auth.UserSession
import app.kamy.qalbuApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.qalbuApp.infrastructure.repository.ContentRepository
import app.kamy.qalbuApp.infrastructure.repository.ReadingSessionRepository
import app.kamy.qalbuApp.infrastructure.repository.ReflectRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChapterReaderUiState(
    val chapterNumber: Int = 1,
    val chapterDisplayName: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val verses: List<RandomAyahPayload> = emptyList(),
    val recitations: List<RecitationPayload> = emptyList(),
    val selectedRecitationId: Int = TranslationPreferencesStore.DEFAULT_RECITATION_ID,
    val playbackMode: AyahPlaybackMode = AyahPlaybackMode.CONTINUOUS,
    val fontScale: Float = 1.0f,
    val showTranslation: Boolean = true,
    val currentVerseIndex: Int = 0,
    val loadedApiPage: Int = 0,
    val hasMore: Boolean = true,
    val error: AppError? = null,
    val tafsirVisible: Boolean = false,
    val tafsirLoading: Boolean = false,
    val tafsir: TafsirPayload? = null,
    val tafsirError: AppError? = null,
    val hadithVisible: Boolean = false,
    val hadithLoading: Boolean = false,
    val hadithLoadingMore: Boolean = false,
    val hadithHasMore: Boolean = false,
    val hadithPage: Int = 0,
    val hadithError: AppError? = null,
    val hadiths: List<HadithReference> = emptyList(),
    val activeAyahKey: String? = null,

    // When audio is paused/playing, keep current verse key so the UI can reflect it if needed.
    val currentlyPlayingVerseKey: String? = null,
    val aiShareVisible: Boolean = false,
    val aiShareLoading: Boolean = false,
    val aiShareDraft: String = "",
    val aiShareError: AppError? = null,
    val aiShareVerseIndex: Int? = null,
    val isPublishing: Boolean = false,
    val publishMessage: String? = null
)

enum class AyahPlaybackMode {
    SINGLE,
    CONTINUOUS
}

@HiltViewModel
class ChapterReaderViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val audioPlayer: AudioPlayerController,
    private val contentRepository: ContentRepository,
    private val readingSessions: ReadingSessionRepository,
    private val shareComposer: VerseShareTextComposer,
    private val reflectRepository: ReflectRepository,
    private val userSession: UserSession,
    private val translationStore: TranslationPreferencesStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChapterReaderUiState(
            chapterNumber = savedStateHandle.get<Int>("chapter") ?: 1
        )
    )
    val state: StateFlow<ChapterReaderUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ReaderEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var lastStartedVerseKey: String? = null
    private var wasPlaying: Boolean = false

    init {
        _state.update {
            it.copy(
                showTranslation = translationStore.showTranslation.value,
                selectedRecitationId = translationStore.currentRecitationId()
            )
        }
        loadChapterMeta()
        loadInitial()
        loadRecitations()
        viewModelScope.launch {
            translationStore.translationId.drop(1).collect { loadInitial() }
        }
        viewModelScope.launch {
            translationStore.showTranslation.collect { enabled ->
                _state.update { it.copy(showTranslation = enabled) }
            }
        }

        // Sync ViewModel with player state so we can auto-advance on completion.
        viewModelScope.launch {
            audioPlayer.state.collect { audio ->
                val nextPlayingVerseKey =
                    if (audio.currentUrl != null) audio.trackSubtitle.ifBlank { null } else null

                _state.update { it.copy(currentlyPlayingVerseKey = nextPlayingVerseKey) }

                val ended = wasPlaying && !audio.isPlaying && audio.currentUrl == null
                if (ended) {
                    maybeAutoAdvanceAfterCompletion()
                }
                wasPlaying = audio.isPlaying
            }
        }
    }

    private fun maybeAutoAdvanceAfterCompletion() {
        val s = _state.value
        if (s.playbackMode != AyahPlaybackMode.CONTINUOUS) return
        val lastKey = lastStartedVerseKey ?: return

        val verses = s.verses
        val lastIndex = verses.indexOfFirst { it.verseKey == lastKey }
        if (lastIndex < 0) return

        val nextIndex = lastIndex + 1
        if (nextIndex in verses.indices) {
            val nextPage = verses[nextIndex]
            playAyahAtIndex(nextIndex, nextPage)
            _events.tryEmit(ReaderEvent.AnimateToPage(nextIndex))
        }
    }

    private fun loadChapterMeta() {
        viewModelScope.launch {
            runCatching {
                val chapters = contentRepository.getChapters()
                val name = chapters.find { it.id == _state.value.chapterNumber }?.displayComplexName
                _state.update { it.copy(chapterDisplayName = name) }
            }
        }
    }

    fun loadInitial() {
        _state.update {
            it.copy(
                isLoading = true,
                error = null,
                verses = emptyList(),
                currentVerseIndex = 0,
                loadedApiPage = 0,
                hasMore = true
            )
        }
        viewModelScope.launch {
            runCatching {
                contentRepository.getVersesByChapter(
                    chapterNumber = _state.value.chapterNumber,
                    page = 1,
                    audioRecitationId = _state.value.selectedRecitationId
                )
            }.onSuccess { resp ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        verses = resp.verses,
                        currentVerseIndex = 0,
                        loadedApiPage = resp.pagination?.currentPage ?: 1,
                        hasMore = resp.pagination?.hasNextPage ?: false
                    )
                }
            }.onFailure { t ->
                _state.update { it.copy(isLoading = false, error = t.toAppError()) }
            }
        }
    }

    fun loadMoreIfNeeded(currentIndex: Int) {
        val s = _state.value
        if (s.isLoadingMore || !s.hasMore || s.verses.isEmpty()) return
        if (currentIndex < s.verses.size - 3) return
        val nextApiPage = s.loadedApiPage + 1
        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            runCatching {
                contentRepository.getVersesByChapter(
                    chapterNumber = s.chapterNumber,
                    page = nextApiPage,
                    audioRecitationId = s.selectedRecitationId
                )
            }.onSuccess { resp ->
                if (resp.verses.isEmpty()) {
                    _state.update { it.copy(isLoadingMore = false, hasMore = false) }
                    return@onSuccess
                }
                _state.update {
                    val merged = (it.verses + resp.verses).distinctBy { verse -> verse.listIdentity }
                    it.copy(
                        isLoadingMore = false,
                        verses = merged,
                        loadedApiPage = resp.pagination?.currentPage ?: nextApiPage,
                        hasMore = resp.pagination?.hasNextPage ?: false
                    )
                }
            }.onFailure { t ->
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        publishMessage = t.toAppError().apiMessage
                            ?: appContext.getString(R.string.error_generic_body)
                    )
                }
            }
        }
    }

    private fun loadRecitations() {
        viewModelScope.launch {
            runCatching { contentRepository.getRecitations() }
                .onSuccess { rs -> _state.update { it.copy(recitations = rs) } }
        }
    }

    fun selectRecitation(id: Int) {
        if (id <= 0 || id == _state.value.selectedRecitationId) return
        translationStore.setRecitation(id)
        _state.update { it.copy(selectedRecitationId = id) }
        loadInitial()
    }

    fun setFontScale(scale: Float) {
        _state.update { it.copy(fontScale = scale.coerceIn(0.85f, 1.35f)) }
    }

    fun setPlaybackMode(mode: AyahPlaybackMode) {
        _state.update { it.copy(playbackMode = mode) }
    }

    fun toggleTranslation(enabled: Boolean) {
        translationStore.setShowTranslation(enabled)
    }

    fun onPageChanged(index: Int) {
        val s = _state.value
        if (index !in s.verses.indices) return
        val page = s.verses[index]
        _state.update { it.copy(currentVerseIndex = index) }
        page.resolvedVerseNumber?.let { logScrollPosition(it) }
    }

    fun onTapAyah(index: Int) {
        val s = _state.value
        if (index !in s.verses.indices) return

        val page = s.verses[index]
        val verseKey = page.verseKey
        val isSamePlaying = s.currentlyPlayingVerseKey != null && verseKey != null && verseKey == s.currentlyPlayingVerseKey

        val url = page.audio?.url
        if (url.isNullOrBlank()) return

        if (isSamePlaying) {
            audioPlayer.toggle()
        } else {
            playAyahAtIndex(index, page)
        }
    }

    private fun playAyahAtIndex(index: Int, page: RandomAyahPayload) {
        val s = _state.value
        val surahTitle = s.chapterDisplayName ?: appContext.getString(R.string.surah_number, s.chapterNumber)
        val reciterName = s.recitations
            .firstOrNull { it.id == s.selectedRecitationId }
            ?.displayName.orEmpty()

        val url = page.audio?.url ?: return
        lastStartedVerseKey = page.verseKey
        _state.update { it.copy(currentVerseIndex = index, currentlyPlayingVerseKey = page.verseKey) }
        audioPlayer.playVerse(
            url = url,
            surahTitle = surahTitle,
            ayahLabel = page.verseKey.orEmpty(),
            reciterName = reciterName,
            chapterNumber = s.chapterNumber,
            ayahNumber = page.resolvedVerseNumber
        )
    }

    fun openTafsir(ayahKey: String) {
        _state.update {
            it.copy(
                tafsirVisible = true,
                tafsirLoading = true,
                tafsir = null,
                tafsirError = null,
                activeAyahKey = ayahKey
            )
        }
        viewModelScope.launch {
            _state.value.verses.find { it.verseKey == ayahKey }?.let { verse ->
                shareComposer.prefetchShareTextIfNeeded(
                    verse,
                    verse.referenceLabel(_state.value.chapterDisplayName)
                )
            }
            loadTafsir(ayahKey)
        }
    }

    fun reloadTafsir() {
        val key = _state.value.activeAyahKey ?: return
        _state.update { it.copy(tafsirLoading = true, tafsirError = null) }
        viewModelScope.launch { loadTafsir(key) }
    }

    private suspend fun loadTafsir(ayahKey: String) {
        try {
            val t = contentRepository.getTafsirByAyah("169", ayahKey)
            _state.update { it.copy(tafsir = t, tafsirLoading = false, tafsirError = null) }
        } catch (e: Throwable) {
            _state.update {
                it.copy(tafsirLoading = false, tafsirError = e.toAppError())
            }
        }
    }

    fun dismissTafsir() {
        _state.update { it.copy(tafsirVisible = false, tafsirError = null) }
    }

    fun openHadith(ayahKey: String) {
        _state.update {
            it.copy(
                hadithVisible = true,
                hadithLoading = true,
                hadithLoadingMore = false,
                hadiths = emptyList(),
                hadithError = null,
                hadithHasMore = false,
                activeAyahKey = ayahKey
            )
        }
        viewModelScope.launch { loadHadith(ayahKey, page = 1, append = false) }
    }

    fun reloadHadith() {
        val key = _state.value.activeAyahKey ?: return
        _state.update { it.copy(hadithLoading = true, hadithError = null) }
        viewModelScope.launch { loadHadith(key, page = 1, append = false) }
    }

    fun loadMoreHadith() {
        val key = _state.value.activeAyahKey ?: return
        val s = _state.value
        if (s.hadithLoadingMore || !s.hadithHasMore) return
        viewModelScope.launch { loadHadith(key, page = s.hadithPage + 1, append = true) }
    }

    private suspend fun loadHadith(ayahKey: String, page: Int, append: Boolean) {
        if (append) {
            _state.update { it.copy(hadithLoadingMore = true) }
        }
        try {
            val resp = contentRepository.getHadithsByAyah(ayahKey, page = page, limit = HADITH_PAGE_LIMIT)
            val batch = resp.hadiths.orEmpty()
            _state.update { s ->
                s.copy(
                    hadiths = if (append) s.hadiths + batch else batch,
                    hadithLoading = false,
                    hadithLoadingMore = false,
                    hadithHasMore = resp.hasMore == true,
                    hadithPage = resp.page ?: page,
                    hadithError = null
                )
            }
        } catch (e: Throwable) {
            _state.update {
                it.copy(
                    hadithLoading = false,
                    hadithLoadingMore = false,
                    hadithError = e.toAppError()
                )
            }
        }
    }

    fun dismissHadith() {
        _state.update {
            it.copy(hadithVisible = false, hadithError = null, hadithLoadingMore = false)
        }
    }

    private companion object {
        const val HADITH_PAGE_LIMIT = 4
    }

    fun isSignedIn(): Boolean = userSession.isSignedIn.value

    fun openAiShare(verseIndex: Int) {
        if (verseIndex !in _state.value.verses.indices) return
        _state.update {
            it.copy(
                aiShareVisible = true,
                aiShareLoading = true,
                aiShareDraft = "",
                aiShareError = null,
                aiShareVerseIndex = verseIndex
            )
        }
        loadAiShareDraft(forceRefresh = false)
    }

    fun dismissAiShare() {
        _state.update {
            it.copy(
                aiShareVisible = false,
                aiShareLoading = false,
                aiShareError = null,
                aiShareVerseIndex = null
            )
        }
    }

    fun updateAiShareDraft(text: String) {
        _state.update { it.copy(aiShareDraft = text) }
    }

    fun regenerateAiShare() = loadAiShareDraft(forceRefresh = true)

    private fun loadAiShareDraft(forceRefresh: Boolean) {
        val index = _state.value.aiShareVerseIndex ?: return
        val verse = _state.value.verses.getOrNull(index) ?: return
        val reference = verse.referenceLabel(_state.value.chapterDisplayName)
        viewModelScope.launch {
            _state.update { it.copy(aiShareLoading = true, aiShareError = null) }
            runCatching {
                shareComposer.prepareShareText(verse, reference, forceRefresh = forceRefresh)
            }.onSuccess { text ->
                _state.update { it.copy(aiShareLoading = false, aiShareDraft = text) }
            }.onFailure { t ->
                _state.update {
                    it.copy(
                        aiShareLoading = false,
                        aiShareError = t.toAppError()
                    )
                }
            }
        }
    }

    fun publishAiReflection() {
        if (!userSession.isSignedIn.value) {
            _state.update { it.copy(publishMessage = appContext.getString(R.string.sign_in_to_publish_account)) }
            return
        }
        val index = _state.value.aiShareVerseIndex ?: return
        val verse = _state.value.verses.getOrNull(index) ?: return
        val verseKey = verse.verseKey ?: return
        val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val idempotencyKey = "reflect:$verseKey:$dayKey"
        val reference = verse.referenceLabel(_state.value.chapterDisplayName)

        viewModelScope.launch {
            val body = _state.value.aiShareDraft.trim().ifBlank {
                shareComposer.quickReflectionText(verse, reference)
            }
            if (body.isBlank()) return@launch
            _state.update { it.copy(isPublishing = true, publishMessage = null) }
            val authorId = try {
                reflectRepository.fetchMyProfile().id
            } catch (t: Throwable) {
                userSession.invalidateIfAuthenticationFailure(t)
                _state.update {
                    it.copy(
                        isPublishing = false,
                        publishMessage = if (t.isAuthenticationFailure()) appContext.getString(R.string.session_expired)
                        else appContext.getString(R.string.profile_load_failed)
                    )
                }
                return@launch
            }
            if (authorId.isNullOrBlank()) {
                _state.update {
                    it.copy(isPublishing = false, publishMessage = appContext.getString(R.string.profile_load_failed))
                }
                return@launch
            }
            runCatching {
                reflectRepository.createReflectionPost(body, verseKey, authorId, idempotencyKey)
            }.onSuccess {
                _state.update {
                    it.copy(isPublishing = false, publishMessage = appContext.getString(R.string.published_to_reflect))
                }
            }.onFailure { t ->
                userSession.invalidateIfAuthenticationFailure(t)
                _state.update {
                    it.copy(
                        isPublishing = false,
                        publishMessage = if (t.isAuthenticationFailure()) appContext.getString(R.string.session_expired)
                        else t.message ?: appContext.getString(R.string.publish_failed)
                    )
                }
            }
        }
    }

    fun clearPublishMessage() {
        _state.update { it.copy(publishMessage = null) }
    }

    fun audioQueueItems(): List<AudioQueueItem> = _state.value.verses.mapNotNull { v ->
        val url = v.audio?.url ?: return@mapNotNull null
        AudioQueueItem(
            verseKey = v.verseKey.orEmpty(),
            ayahNumber = v.resolvedVerseNumber ?: 0,
            url = url,
            label = v.verseKey.orEmpty()
        )
    }

    fun logScrollPosition(verseNumber: Int) {
        viewModelScope.launch {
            runCatching { readingSessions.logReadingSession(_state.value.chapterNumber, verseNumber) }
        }
    }
}

sealed interface ReaderEvent {
    data class AnimateToPage(val index: Int) : ReaderEvent
}
