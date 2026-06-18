package app.kamy.saatApp.features.quran

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.error.toAppError
import app.kamy.saatApp.core.error.invalidateIfAuthenticationFailure
import app.kamy.saatApp.core.error.isAuthenticationFailure
import app.kamy.saatApp.domain.model.HadithReference
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.RecitationPayload
import app.kamy.saatApp.domain.model.TafsirPayload
import app.kamy.saatApp.domain.share.VerseShareTextComposer
import app.kamy.saatApp.infrastructure.audio.AudioQueueItem
import app.kamy.saatApp.infrastructure.audio.AudioPlayerController
import app.kamy.saatApp.infrastructure.auth.UserSession
import app.kamy.saatApp.domain.model.HifzStatus
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.saatApp.infrastructure.repository.ContentRepository
import app.kamy.saatApp.infrastructure.repository.ReadingSessionRepository
import app.kamy.saatApp.infrastructure.repository.ReflectRepository
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
    val juzNumber: Int? = null,
    val chapterDisplayName: String? = null,
    val chapterLookup: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val verses: List<RandomAyahPayload> = emptyList(),
    val recitations: List<RecitationPayload> = emptyList(),
    val selectedRecitationId: Int = TranslationPreferencesStore.DEFAULT_RECITATION_ID,
    val playbackMode: AyahPlaybackMode = AyahPlaybackMode.CONTINUOUS,
    val fontScale: Float = 1.0f,
    val showTranslation: Boolean = true,
    val showTransliteration: Boolean = false,
    val selectedTranslationId: Int = LocalQuranConfig.DEFAULT_TRANSLATION_ID,
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
    val publishMessage: String? = null,
    val hifzModeEnabled: Boolean = false,
    val currentVerseBookmarked: Boolean = false,
    val currentVerseHasNote: Boolean = false,
    val currentVerseHifzStatus: HifzStatus = HifzStatus.NONE,
    val noteVisible: Boolean = false,
    val noteDraft: String = "",
    val hifzPickerVisible: Boolean = false
)

enum class AyahPlaybackMode {
    SINGLE,
    CONTINUOUS
}

@HiltViewModel
class ChapterReaderViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val audioPlayer: AudioPlayerController,
    private val contentRepository: ContentRepository,
    private val readingSessions: ReadingSessionRepository,
    private val shareComposer: VerseShareTextComposer,
    private val reflectRepository: ReflectRepository,
    private val userSession: UserSession,
    private val translationStore: TranslationPreferencesStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val juzNumber: Int? = savedStateHandle.get<Int>("juzNumber")

    private val _state = MutableStateFlow(
        ChapterReaderUiState(
            chapterNumber = savedStateHandle.get<Int>("chapter") ?: 1,
            juzNumber = juzNumber
        )
    )
    val state: StateFlow<ChapterReaderUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ReaderEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var lastStartedVerseKey: String? = null
    private var wasPlaying: Boolean = false
    private var lastLoggedReadingKey: String? = null
    private var pendingScrollVerseKey: String? =
        savedStateHandle.get<String>("verseKey")?.takeIf { it.isNotBlank() }
            ?: savedStateHandle.get<Int>("ayah")
                ?.takeIf { it > 0 && savedStateHandle.get<Int>("juzNumber") == null }
                ?.let { ayah ->
                    val chapter = savedStateHandle.get<Int>("chapter") ?: return@let null
                    "$chapter:$ayah"
                }

    init {
        val isJuzMode = juzNumber != null
        _state.update {
            it.copy(
                showTranslation = if (isJuzMode) false else translationStore.showTranslation.value,
                showTransliteration = if (isJuzMode) false else translationStore.showTransliteration.value,
                selectedRecitationId = translationStore.currentRecitationId(),
                selectedTranslationId = LocalQuranConfig.normalizeTranslationId(
                    translationStore.currentTranslationId()
                ),
                hifzModeEnabled = QuranPersonalStore.isHifzModeEnabled(appContext)
            )
        }
        loadChapterMeta()
        loadInitial()
        loadRecitations()
        viewModelScope.launch {
            translationStore.translationId.drop(1).collect { id ->
                _state.update {
                    it.copy(selectedTranslationId = LocalQuranConfig.normalizeTranslationId(id))
                }
                loadInitial()
            }
        }
        viewModelScope.launch {
            translationStore.showTranslation.collect { enabled ->
                if (juzNumber == null) {
                    _state.update { it.copy(showTranslation = enabled) }
                }
            }
        }
        viewModelScope.launch {
            translationStore.showTransliteration.collect { enabled ->
                if (juzNumber == null) {
                    _state.update { it.copy(showTransliteration = enabled) }
                }
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
                val lookup = chapters.associate { it.id to it.displayComplexName }
                if (juzNumber != null) {
                    _state.update {
                        it.copy(
                            chapterLookup = lookup,
                            chapterDisplayName = appContext.getString(R.string.juz_number, juzNumber)
                        )
                    }
                } else {
                    val name = chapters.find { it.id == _state.value.chapterNumber }?.displayComplexName
                    _state.update { it.copy(chapterDisplayName = name, chapterLookup = lookup) }
                }
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
                fetchVersePage(page = 1)
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
                tryScrollToPendingVerse()
                logCurrentVerseReading(force = true)
                refreshPersonalVerseState(0)
            }.onFailure { t ->
                _state.update { it.copy(isLoading = false, error = t.toAppError()) }
            }
        }
    }

    fun loadMoreIfNeeded(currentIndex: Int) {
        val s = _state.value
        if (s.isLoadingMore || !s.hasMore || s.verses.isEmpty()) return
        if (pendingScrollVerseKey == null && currentIndex < s.verses.size - 3) return
        loadNextPage()
    }

    private fun loadNextPage() {
        val s = _state.value
        if (s.isLoadingMore || !s.hasMore || s.verses.isEmpty()) return
        val nextApiPage = s.loadedApiPage + 1
        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            runCatching {
                fetchVersePage(page = nextApiPage)
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
                tryScrollToPendingVerse()
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

    private suspend fun fetchVersePage(page: Int) =
        if (juzNumber != null) {
            contentRepository.getVersesByJuz(
                juzNumber = juzNumber,
                page = page,
                audioRecitationId = _state.value.selectedRecitationId
            )
        } else {
            contentRepository.getVersesByChapter(
                chapterNumber = _state.value.chapterNumber,
                page = page,
                audioRecitationId = _state.value.selectedRecitationId
            )
        }

    private fun tryScrollToPendingVerse() {
        val key = pendingScrollVerseKey ?: return
        val s = _state.value
        val idx = s.verses.indexOfFirst { it.verseKey == key }
        if (idx >= 0) {
            pendingScrollVerseKey = null
            _events.tryEmit(ReaderEvent.AnimateToPage(idx))
            return
        }
        if (s.hasMore && !s.isLoadingMore) {
            loadNextPage()
        } else if (!s.hasMore) {
            pendingScrollVerseKey = null
        }
    }

    private fun chapterDisplayNameFor(verse: RandomAyahPayload): String? {
        val chapterNum = verse.chapterNumber ?: return _state.value.chapterDisplayName
        return _state.value.chapterLookup[chapterNum]
            ?: _state.value.chapterDisplayName
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

    fun toggleTransliteration(enabled: Boolean) {
        translationStore.setShowTransliteration(enabled)
    }

    fun onPageChanged(index: Int) {
        val s = _state.value
        if (index !in s.verses.indices) return
        val page = s.verses[index]
        _state.update { it.copy(currentVerseIndex = index) }
        refreshPersonalVerseState(index)
        val chapterNum = page.chapterNumber ?: s.chapterNumber
        page.resolvedVerseNumber?.let { logScrollPosition(chapterNum, it) }
    }

    private fun logCurrentVerseReading(force: Boolean = false) {
        val s = _state.value
        val index = s.currentVerseIndex.coerceIn(0, (s.verses.size - 1).coerceAtLeast(0))
        val page = s.verses.getOrNull(index) ?: return
        val chapterNum = page.chapterNumber ?: s.chapterNumber
        val ayah = page.resolvedVerseNumber ?: return
        logScrollPosition(chapterNum, ayah, force = force)
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
        val chapterNum = page.chapterNumber ?: s.chapterNumber
        val surahTitle = chapterDisplayNameFor(page)
            ?: appContext.getString(R.string.surah_number, chapterNum)
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
            chapterNumber = chapterNum,
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
            val t = contentRepository.getTafsirByAyah(ayahKey)
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
        val reference = verse.referenceLabel(chapterDisplayNameFor(verse))
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
        val reference = verse.referenceLabel(chapterDisplayNameFor(verse))

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

    fun logScrollPosition(chapterNumber: Int, verseNumber: Int, force: Boolean = false) {
        if (chapterNumber <= 0 || verseNumber <= 0) return
        val key = "$chapterNumber:$verseNumber"
        if (!force && key == lastLoggedReadingKey) return
        lastLoggedReadingKey = key
        viewModelScope.launch {
            runCatching { readingSessions.logReadingSession(chapterNumber, verseNumber) }
        }
    }

    fun refreshPersonalVerseState(index: Int = _state.value.currentVerseIndex) {
        val verse = _state.value.verses.getOrNull(index) ?: return
        val key = versePersonalKey(verse, _state.value.chapterNumber) ?: return
        _state.update {
            it.copy(
                hifzModeEnabled = QuranPersonalStore.isHifzModeEnabled(appContext),
                currentVerseBookmarked = QuranPersonalStore.isBookmarked(appContext, key),
                currentVerseHasNote = QuranPersonalStore.noteFor(appContext, key) != null,
                currentVerseHifzStatus = QuranPersonalStore.hifzStatus(appContext, key),
                noteDraft = QuranPersonalStore.noteFor(appContext, key)?.text.orEmpty()
            )
        }
    }

    fun toggleBookmark(index: Int = _state.value.currentVerseIndex) {
        val verse = _state.value.verses.getOrNull(index) ?: return
        val key = versePersonalKey(verse, _state.value.chapterNumber) ?: return
        val chapter = verse.chapterNumber ?: _state.value.chapterNumber
        val ayah = verse.resolvedVerseNumber ?: return
        val surah = _state.value.chapterDisplayName
        val added = QuranPersonalStore.toggleBookmark(appContext, key, chapter, ayah, surah)
        refreshPersonalVerseState(index)
        _state.update {
            it.copy(
                publishMessage = appContext.getString(
                    if (added) R.string.bookmark_added else R.string.bookmark_removed
                )
            )
        }
    }

    fun openHifzPicker(index: Int = _state.value.currentVerseIndex) {
        refreshPersonalVerseState(index)
        _state.update { it.copy(hifzPickerVisible = true) }
    }

    fun dismissHifzPicker() {
        _state.update { it.copy(hifzPickerVisible = false) }
    }

    fun setHifzStatus(status: HifzStatus, index: Int = _state.value.currentVerseIndex) {
        val verse = _state.value.verses.getOrNull(index) ?: return
        val key = versePersonalKey(verse, _state.value.chapterNumber) ?: return
        val chapter = verse.chapterNumber ?: _state.value.chapterNumber
        val ayah = verse.resolvedVerseNumber ?: return
        QuranPersonalStore.setHifzStatus(appContext, key, chapter, ayah, status)
        refreshPersonalVerseState(index)
        _state.update {
            it.copy(
                hifzPickerVisible = false,
                publishMessage = when (status) {
                    HifzStatus.NONE -> appContext.getString(R.string.hifz_cleared)
                    HifzStatus.LEARNING -> appContext.getString(R.string.hifz_marked_learning)
                    HifzStatus.MEMORIZED -> appContext.getString(R.string.hifz_marked_memorized)
                    HifzStatus.NEEDS_REVIEW -> appContext.getString(R.string.hifz_marked_review)
                }
            )
        }
    }

    fun cycleHifzStatus(index: Int = _state.value.currentVerseIndex) {
        openHifzPicker(index)
    }

    fun toggleHifzMode(enabled: Boolean) {
        QuranPersonalStore.setHifzModeEnabled(appContext, enabled)
        _state.update { it.copy(hifzModeEnabled = enabled) }
    }

    fun openNote(index: Int = _state.value.currentVerseIndex) {
        refreshPersonalVerseState(index)
        _state.update { it.copy(noteVisible = true) }
    }

    fun dismissNote() {
        _state.update { it.copy(noteVisible = false) }
    }

    fun updateNoteDraft(text: String) {
        _state.update { it.copy(noteDraft = text) }
    }

    fun saveNote(index: Int = _state.value.currentVerseIndex) {
        val verse = _state.value.verses.getOrNull(index) ?: return
        val key = versePersonalKey(verse, _state.value.chapterNumber) ?: return
        val chapter = verse.chapterNumber ?: _state.value.chapterNumber
        val ayah = verse.resolvedVerseNumber ?: return
        val draft = _state.value.noteDraft
        QuranPersonalStore.saveNote(appContext, key, chapter, ayah, draft)
        refreshPersonalVerseState(index)
        _state.update {
            it.copy(
                noteVisible = false,
                publishMessage = if (draft.isBlank()) {
                    appContext.getString(R.string.note_deleted)
                } else {
                    appContext.getString(R.string.note_saved)
                }
            )
        }
    }

    fun deleteNote(index: Int = _state.value.currentVerseIndex) {
        val verse = _state.value.verses.getOrNull(index) ?: return
        val key = versePersonalKey(verse, _state.value.chapterNumber) ?: return
        QuranPersonalStore.deleteNote(appContext, key)
        refreshPersonalVerseState(index)
        _state.update {
            it.copy(
                noteVisible = false,
                noteDraft = "",
                publishMessage = appContext.getString(R.string.note_deleted)
            )
        }
    }

    private fun versePersonalKey(verse: RandomAyahPayload, chapterFallback: Int): String? =
        verse.verseKey?.takeIf { it.isNotBlank() }
            ?: verse.displayVerseReference
            ?: verse.resolvedVerseNumber?.let { ayah ->
                val chapter = verse.chapterNumber ?: chapterFallback
                "$chapter:$ayah"
            }
}

sealed interface ReaderEvent {
    data class AnimateToPage(val index: Int) : ReaderEvent
}
