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
import app.kamy.saatApp.core.error.userFacingAuthOrApiMessage
import app.kamy.saatApp.domain.model.HadithReference
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.RecitationPayload
import app.kamy.saatApp.domain.model.TafsirPayload
import app.kamy.saatApp.domain.model.ArabicTextType
import app.kamy.saatApp.domain.share.VerseShareTextComposer
import app.kamy.saatApp.infrastructure.audio.AudioQueueItem
import app.kamy.saatApp.infrastructure.audio.AudioPlayerController
import app.kamy.saatApp.infrastructure.audio.AudioPlaybackState
import app.kamy.saatApp.infrastructure.auth.UserSession
import app.kamy.saatApp.domain.model.HifzStatus
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.saatApp.infrastructure.repository.ContentRepository
import app.kamy.saatApp.infrastructure.repository.ReadingSessionRepository
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
    val isTajweedEnabled: Boolean = true,
    val arabicTextType: ArabicTextType = ArabicTextType.MADANI,
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
    val hifzPickerVisible: Boolean = false,
    val personalDataRevision: Int = 0,
    val bismillahPre: Boolean = false
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
    private val translationStore: TranslationPreferencesStore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val juzNumber: Int? = savedStateHandle.get<Any>("juzNumber")?.toString()?.toIntOrNull()
    private val parsedChapter: Int = savedStateHandle.get<Any>("chapter")?.toString()?.toIntOrNull() ?: 1

    private val _state = MutableStateFlow(
        ChapterReaderUiState(
            chapterNumber = parsedChapter,
            juzNumber = juzNumber
        )
    )
    val state: StateFlow<ChapterReaderUiState> = _state.asStateFlow()
    val audioPlaybackState: StateFlow<AudioPlaybackState> = audioPlayer.state

    private val _events = MutableSharedFlow<ReaderEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var lastStartedVerseKey: String? = null
    private var wasPlaying: Boolean = false
    private var lastLoggedReadingKey: String? = null
    private var pendingScrollVerseKey: String? =
        savedStateHandle.get<String>("verseKey")?.takeIf { it.isNotBlank() }
            ?: run {
                val ayahVal = savedStateHandle.get<Any>("ayah")?.toString()?.toIntOrNull()?.takeIf { it > 0 }
                if (ayahVal != null && juzNumber == null) {
                    "$parsedChapter:$ayahVal"
                } else null
            }
    private var initialScrollPending: Boolean = pendingScrollVerseKey != null

    init {
        val isJuzMode = juzNumber != null
        _state.update {
            it.copy(
                showTranslation = if (isJuzMode) false else translationStore.showTranslation.value,
                showTransliteration = if (isJuzMode) false else translationStore.showTransliteration.value,
                selectedRecitationId = translationStore.currentRecitationId(),
                isTajweedEnabled = translationStore.isTajweedEnabled.value,
                arabicTextType = translationStore.arabicTextType.value,
                selectedTranslationId = LocalQuranConfig.normalizeTranslationId(
                    translationStore.currentTranslationId()
                ),
                hifzModeEnabled = QuranPersonalStore.isHifzModeEnabled(appContext),
                fontScale = translationStore.fontScale.value
            )
        }
        loadChapterMeta()
        loadInitial()
        loadRecitations()
        app.kamy.saatApp.infrastructure.review.AppReviewManager.recordReadSession(appContext)
        viewModelScope.launch {
            translationStore.translationId.drop(1).collect { id ->
                _state.update {
                    it.copy(selectedTranslationId = LocalQuranConfig.normalizeTranslationId(id))
                }
                loadInitial()
                if (_state.value.tafsirVisible) {
                    reloadTafsir()
                }
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
        viewModelScope.launch {
            translationStore.isTajweedEnabled.collect { enabled ->
                _state.update { it.copy(isTajweedEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            translationStore.arabicTextType.collect { type ->
                _state.update { it.copy(arabicTextType = type) }
            }
        }
        viewModelScope.launch {
            translationStore.fontScale.collect { scale ->
                _state.update { it.copy(fontScale = scale) }
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
            _events.tryEmit(ReaderEvent.AutoAdvanceToPage(lastIndex, nextIndex))
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
                    val chapter = chapters.find { it.id == _state.value.chapterNumber }
                    val name = chapter?.displayComplexName
                    _state.update { 
                        it.copy(
                            chapterDisplayName = name, 
                            chapterLookup = lookup,
                            bismillahPre = chapter?.bismillahPre ?: false
                        ) 
                    }
                }
            }
        }
    }

    fun loadInitial(
        targetVerseKey: String? = null,
        targetVerseIndex: Int = 0,
        autoPlayAfterLoad: Boolean = false
    ) {
        _state.update {
            it.copy(
                isLoading = true,
                error = null,
                verses = emptyList(),
                currentVerseIndex = targetVerseIndex,
                loadedApiPage = 0,
                hasMore = true
            )
        }
        viewModelScope.launch {
            runCatching {
                fetchVersePage(page = 1)
            }.onSuccess { resp ->
                val trans = translationStore.showTranslation.value
                val translit = translationStore.showTransliteration.value
                val tajweed = translationStore.isTajweedEnabled.value
                val arabicType = translationStore.arabicTextType.value
                val rec = translationStore.currentRecitationId()
                val tid = LocalQuranConfig.normalizeTranslationId(translationStore.currentTranslationId())

                val restoredIndex = if (targetVerseKey != null) {
                    resp.verses.indexOfFirst { it.verseKey == targetVerseKey }.takeIf { it >= 0 } ?: targetVerseIndex
                } else {
                    targetVerseIndex
                }.coerceIn(0, (resp.verses.size - 1).coerceAtLeast(0))

                _state.update { 
                    it.copy(
                        isLoading = false,
                        verses = resp.verses,
                        currentVerseIndex = restoredIndex,
                        loadedApiPage = resp.pagination?.currentPage ?: 1,
                        hasMore = resp.pagination?.hasNextPage ?: false,
                        selectedTranslationId = tid,
                        showTranslation = trans,
                        showTransliteration = translit,
                        isTajweedEnabled = tajweed,
                        arabicTextType = arabicType,
                        selectedRecitationId = rec
                    ) 
                }
                tryScrollToPendingVerse()
                if (pendingScrollVerseKey == null && !initialScrollPending) {
                    logCurrentVerseReading(force = true)
                }
                refreshPersonalVerseState(restoredIndex)

                if (autoPlayAfterLoad) {
                    val page = resp.verses.getOrNull(restoredIndex)
                    if (page != null) {
                        playAyahAtIndex(restoredIndex, page)
                    }
                }
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
            initialScrollPending = false
            _state.update { it.copy(currentVerseIndex = idx) }
            _events.tryEmit(ReaderEvent.AnimateToPage(idx))
            logCurrentVerseReading(force = true)
            refreshPersonalVerseState(idx)
            return
        }
        if (s.hasMore && !s.isLoadingMore) {
            loadNextPage()
        } else if (!s.hasMore) {
            pendingScrollVerseKey = null
            initialScrollPending = false
            logCurrentVerseReading(force = true)
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
        val currentKey = _state.value.currentlyPlayingVerseKey
            ?: _state.value.verses.getOrNull(_state.value.currentVerseIndex)?.verseKey
        val activeIndex = _state.value.currentVerseIndex
        val wasPlaying = audioPlayer.state.value.isPlaying || audioPlayer.state.value.currentUrl != null

        translationStore.setRecitation(id)
        _state.update { it.copy(selectedRecitationId = id) }

        loadInitial(
            targetVerseKey = currentKey,
            targetVerseIndex = activeIndex,
            autoPlayAfterLoad = wasPlaying
        )
    }

    fun setFontScale(scale: Float) {
        translationStore.setFontScale(scale)
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

    fun toggleTajweed(enabled: Boolean) {
        translationStore.setTajweedEnabled(enabled)
        _state.update { it.copy(isTajweedEnabled = enabled) }
    }

    fun setArabicTextType(type: ArabicTextType) {
        translationStore.setArabicTextType(type)
        _state.update { it.copy(arabicTextType = type) }
    }

    fun onPageChanged(index: Int) {
        val s = _state.value
        if (index !in s.verses.indices) return
        val page = s.verses[index]
        _state.update { it.copy(currentVerseIndex = index) }
        refreshPersonalVerseState(index)

        // Suppress logging while the initial scroll to the target verse is still pending.
        // Without this guard, opening a reader with a deep-link (e.g. search → ayat kursi)
        // would immediately log verse 1 as the reading position before the pager scrolls
        // to the actual target verse.
        if (initialScrollPending) return

        val chapterNum = page.chapterNumber ?: s.chapterNumber
        page.resolvedVerseNumber?.let { logScrollPosition(chapterNum, it) }

        // Track last read Juz and VerseKey for Khatam progress
        val jNum = page.juzNumber ?: s.juzNumber
        val vKey = page.verseKey
        if (jNum != null && vKey != null) {
            QuranPersonalStore.updateLastReadJuz(appContext, jNum, vKey)
        }

        // Auto-mark chapter or juz as read when user reaches the last verse
        val isLastVerse = !s.hasMore && index == s.verses.size - 1
        if (isLastVerse) {
            if (s.juzNumber != null) {
                QuranPersonalStore.markJuzRead(appContext, s.juzNumber)
            } else {
                QuranPersonalStore.markChapterRead(appContext, chapterNum)
            }
        }
    }

    fun onPageSettled(index: Int) {
        val s = _state.value
        if (index !in s.verses.indices) return
        _state.update { it.copy(currentVerseIndex = index) }
        logCurrentVerseReading()
        refreshPersonalVerseState(index)
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

        if (!app.kamy.saatApp.infrastructure.network.NetworkMonitor.isOnline(appContext)) {
            android.widget.Toast.makeText(
                appContext,
                appContext.getString(R.string.error_no_internet_title),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

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
                noteDraft = QuranPersonalStore.noteFor(appContext, key)?.text.orEmpty(),
                personalDataRevision = it.personalDataRevision + 1
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

    fun loadNextSurahOrJuz() {
        val s = _state.value
        if (s.juzNumber != null) {
            val nextJuz = s.juzNumber + 1
            if (nextJuz <= 30) {
                savedStateHandle.set("juzNumber", nextJuz)
                _state.update {
                    it.copy(
                        juzNumber = nextJuz,
                        verses = emptyList(),
                        currentVerseIndex = 0,
                        loadedApiPage = 0,
                        hasMore = true,
                        isLoading = true,
                        error = null
                    )
                }
                loadChapterMeta()
                loadInitial()
            }
        } else {
            val nextChapter = s.chapterNumber + 1
            if (nextChapter <= 114) {
                savedStateHandle.set("chapter", nextChapter)
                _state.update {
                    it.copy(
                        chapterNumber = nextChapter,
                        verses = emptyList(),
                        currentVerseIndex = 0,
                        loadedApiPage = 0,
                        hasMore = true,
                        isLoading = true,
                        error = null
                    )
                }
                loadChapterMeta()
                loadInitial()
            }
        }
    }

    fun loadPreviousSurahOrJuz() {
        val s = _state.value
        if (s.juzNumber != null) {
            val prevJuz = s.juzNumber - 1
            if (prevJuz >= 1) {
                savedStateHandle.set("juzNumber", prevJuz)
                _state.update {
                    it.copy(
                        juzNumber = prevJuz,
                        verses = emptyList(),
                        currentVerseIndex = 0,
                        loadedApiPage = 0,
                        hasMore = true,
                        isLoading = true,
                        error = null
                    )
                }
                loadChapterMeta()
                loadInitial()
            }
        } else {
            val prevChapter = s.chapterNumber - 1
            if (prevChapter >= 1) {
                savedStateHandle.set("chapter", prevChapter)
                _state.update {
                    it.copy(
                        chapterNumber = prevChapter,
                        verses = emptyList(),
                        currentVerseIndex = 0,
                        loadedApiPage = 0,
                        hasMore = true,
                        isLoading = true,
                        error = null
                    )
                }
                loadChapterMeta()
                loadInitial()
            }
        }
    }
}

sealed interface ReaderEvent {
    data class AnimateToPage(val index: Int) : ReaderEvent
    data class AutoAdvanceToPage(val previousIndex: Int, val nextIndex: Int) : ReaderEvent
}
