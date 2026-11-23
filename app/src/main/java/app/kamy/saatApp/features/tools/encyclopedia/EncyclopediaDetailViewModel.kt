package app.kamy.saatApp.features.tools.encyclopedia

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.model.EncyclopediaCategory
import app.kamy.saatApp.domain.model.EncyclopediaTopic
import app.kamy.saatApp.domain.model.QuranReference
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.repository.EncyclopediaRepository
import app.kamy.saatApp.infrastructure.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Immutable
data class StoryChapterUiModel(
    val index: Int,
    val title: String,
    val paragraphsBefore: List<String>,
    val verses: List<QuranReference>,
    val paragraphsAfter: List<String>
)

@Immutable
data class StoryHikmahUiModel(
    val title: String,
    val lessons: List<String>,
    val conclusion: String
)

@Immutable
data class EncyclopediaDetailUiState(
    val topic: EncyclopediaTopic? = null,
    val chapters: List<StoryChapterUiModel> = emptyList(),
    val hikmah: StoryHikmahUiModel? = null,
    val nextTopicId: String? = null,
    val nextTopicTitle: String? = null,
    val prevTopicId: String? = null,
    val prevTopicTitle: String? = null,
    val isLoading: Boolean = true,
    val currentLanguage: AppLanguage = AppLanguage.INDONESIAN
)

@HiltViewModel
class EncyclopediaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EncyclopediaRepository,
    private val quranRepository: QuranRepository,
    private val appLanguageStore: AppLanguageStore
) : ViewModel() {

    private var activeTopicId: String = checkNotNull(savedStateHandle["topicId"])

    private val _state = MutableStateFlow(EncyclopediaDetailUiState())
    val state: StateFlow<EncyclopediaDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            appLanguageStore.currentFlow.collect { lang ->
                _state.update { it.copy(currentLanguage = lang) }
                val currentTopic = _state.value.topic
                if (currentTopic != null) {
                    processTopic(currentTopic, lang)
                }
            }
        }
        loadTopic(activeTopicId)
    }

    fun selectTopic(topicId: String) {
        activeTopicId = topicId
        loadTopic(topicId)
    }

    private fun loadTopic(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val topic = repository.getTopicById(id)
            if (topic != null) {
                processTopic(topic, _state.value.currentLanguage)
            } else {
                _state.update { it.copy(topic = null, isLoading = false) }
            }
        }
    }

    private suspend fun processTopic(topic: EncyclopediaTopic, language: AppLanguage) {
        val enrichedReferences = enrichQuranReferences(topic.quranReferences, language)
        val enrichedTopic = topic.copy(quranReferences = enrichedReferences)

        val (chapters, hikmah) = withContext(Dispatchers.Default) {
            buildStoryChaptersAndHikmah(enrichedTopic, language, enrichedReferences)
        }

        // Determine next and previous topics if in same category (e.g. Prophets series)
        val (prevId, prevTitle, nextId, nextTitle) = withContext(Dispatchers.IO) {
            val category = EncyclopediaCategory.fromId(topic.categoryId)
            val categoryTopics = repository.getTopicsByCategory(category, language = language)
            val currentIndex = categoryTopics.indexOfFirst { it.id.equals(topic.id, ignoreCase = true) }
            if (currentIndex >= 0) {
                val prev = if (currentIndex > 0) categoryTopics[currentIndex - 1] else null
                val next = if (currentIndex < categoryTopics.lastIndex) categoryTopics[currentIndex + 1] else null
                Quadruple(
                    prev?.id,
                    prev?.localizedTitle(language),
                    next?.id,
                    next?.localizedTitle(language)
                )
            } else {
                Quadruple(null, null, null, null)
            }
        }

        _state.update {
            it.copy(
                topic = enrichedTopic,
                chapters = chapters,
                hikmah = hikmah,
                prevTopicId = prevId,
                prevTopicTitle = prevTitle,
                nextTopicId = nextId,
                nextTopicTitle = nextTitle,
                isLoading = false
            )
        }
    }

    private suspend fun enrichQuranReferences(
        references: List<QuranReference>,
        lang: AppLanguage
    ): List<QuranReference> = withContext(Dispatchers.IO) {
        references.map { ref ->
            if (ref.localizedVerseLatin(lang).isNotBlank()) {
                ref
            } else {
                val (start, end) = parseAyahRange(ref.ayahRange)
                val latin = runCatching {
                    val verses = quranRepository.getVersesByRange(ref.surahNumber, start, end)
                    verses.mapNotNull {
                        if (lang == AppLanguage.ENGLISH) {
                            it.transliterationEn?.takeIf { s -> s.isNotBlank() } ?: it.transliterationId
                        } else {
                            it.transliterationId?.takeIf { s -> s.isNotBlank() } ?: it.transliterationEn
                        }
                    }.filter { it.isNotBlank() }.joinToString(" ")
                }.getOrNull().orEmpty()

                if (lang == AppLanguage.ENGLISH) {
                    ref.copy(verseTextLatinEn = latin.ifBlank { ref.verseTextLatinEn })
                } else {
                    ref.copy(verseTextLatin = latin.ifBlank { ref.verseTextLatin })
                }
            }
        }
    }

    private fun parseAyahRange(range: String): Pair<Int, Int> {
        val clean = range.trim()
        val parts = clean.split("-", ":", "–", "—")
        val first = parts.firstOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 1
        val second = if (parts.size > 1) {
            parts[1].filter { it.isDigit() }.toIntOrNull() ?: first
        } else first
        return Pair(first, maxOf(first, second))
    }

    private fun buildStoryChaptersAndHikmah(
        topic: EncyclopediaTopic,
        language: AppLanguage,
        references: List<QuranReference>
    ): Pair<List<StoryChapterUiModel>, StoryHikmahUiModel?> {
        val content = topic.localizedContent(language)
        val rawParas = content.split("\n\n").map { it.trim() }.filter { it.isNotBlank() }
        if (rawParas.isEmpty()) {
            return Pair(emptyList(), null)
        }

        // 1. Detect Hikmah / Lessons section at the end
        var storyParas = rawParas
        var hikmahUi: StoryHikmahUiModel? = null

        for (i in (rawParas.size / 2) until rawParas.size) {
            val p = rawParas[i]
            val isHikmahHeader = (
                p.contains("pelajaran dari", ignoreCase = true) ||
                p.contains("pengajaran yang", ignoreCase = true) ||
                p.contains("hikmah dan", ignoreCase = true) ||
                p.contains("key lessons", ignoreCase = true) ||
                p.contains("iktibar utama", ignoreCase = true) ||
                p.contains("nasihat-nasihat emas", ignoreCase = true) ||
                p.contains("mutiara nasihat", ignoreCase = true)
            ) && p.length < 65 && !p.contains("\n")

            if (isHikmahHeader) {
                val title = p.removePrefix("#").trim().trimEnd(':', '.')
                val remaining = rawParas.subList(i + 1, rawParas.size)
                val lessons = mutableListOf<String>()
                val otherText = StringBuilder()

                for (item in remaining) {
                    val trimmed = item.trim()
                    if (trimmed.matches(Regex("""^(\d+[\.\)]|[-•*])\s+.*""")) || (trimmed.length < 160 && trimmed.contains(":"))) {
                        lessons.add(trimmed)
                    } else {
                        if (otherText.isNotEmpty()) otherText.append("\n\n")
                        otherText.append(trimmed)
                    }
                }

                hikmahUi = StoryHikmahUiModel(
                    title = title.ifBlank { "Hikmah & Pelajaran" },
                    lessons = lessons.ifEmpty { remaining },
                    conclusion = otherText.toString()
                )
                storyParas = rawParas.subList(0, i)
                break
            }
        }

        if (storyParas.isEmpty()) {
            storyParas = rawParas
            hikmahUi = null
        }

        // If no explicit hikmah found, build a reflective closing hikmah
        if (hikmahUi == null) {
            val summary = topic.localizedSummary(language)
            val reflectionText = if (storyParas.size > 8) {
                // Take the last 2-3 concluding paragraphs
                val tailCount = 2
                val tail = storyParas.takeLast(tailCount)
                storyParas = storyParas.dropLast(tailCount)
                tail.joinToString("\n\n")
            } else {
                summary
            }
            hikmahUi = StoryHikmahUiModel(
                title = when (language) {
                    AppLanguage.ENGLISH -> "Lessons & Reflection"
                    AppLanguage.MALAY -> "Hikmah & Pengajaran"
                    else -> "Hikmah & Pelajaran"
                },
                lessons = listOf(summary),
                conclusion = reflectionText
            )
        }

        // 2. Identify natural sections in storyParas
        val rawSections = mutableListOf<Pair<String, MutableList<String>>>()
        var curTitle = ""
        var curList = mutableListOf<String>()

        for ((idx, p) in storyParas.withIndex()) {
            val isHeader = p.length < 55 && !p.contains("\n") && (
                p.endsWith(":") || p.endsWith(".") || p.startsWith("#") ||
                (p.length < 45 && !p.contains(" adalah ", ignoreCase = true) && !p.contains(" yang ", ignoreCase = true) && !p.contains(" untuk ", ignoreCase = true))
            ) && idx > 0 && !p.contains("”") && !p.contains("\"")

            if (isHeader) {
                if (curList.isNotEmpty()) {
                    rawSections.add(Pair(curTitle, curList))
                    curList = mutableListOf()
                }
                curTitle = p.removePrefix("#").trim().trimEnd(':', '.')
            } else {
                curList.add(p)
            }
        }
        if (curList.isNotEmpty()) {
            rawSections.add(Pair(curTitle, curList))
        }

        // 3. Balance into 3 to 6 chapters
        val chaptersList = mutableListOf<Pair<String, List<String>>>()
        if (rawSections.size in 3..6) {
            chaptersList.addAll(rawSections)
        } else if (rawSections.size > 6) {
            val chunkSize = (rawSections.size + 4) / 5
            for (i in rawSections.indices step chunkSize) {
                val chunk = rawSections.subList(i, minOf(i + chunkSize, rawSections.size))
                val title = chunk.first().first.ifBlank {
                    defaultChapterTitle(language, chaptersList.size + 1)
                }
                val combinedParas = chunk.flatMap { it.second }
                chaptersList.add(Pair(title, combinedParas))
            }
        } else {
            // Very few or single section: split into 3 chapters
            val allParas = rawSections.flatMap { it.second }
            if (allParas.size >= 6) {
                val count = 3
                val size = (allParas.size + count - 1) / count
                for (i in 0 until count) {
                    val start = i * size
                    val end = minOf((i + 1) * size, allParas.size)
                    if (start < allParas.size) {
                        val slice = allParas.subList(start, end)
                        val defaultTitle = when (i) {
                            0 -> when (language) {
                                AppLanguage.ENGLISH -> "Beginning of the Journey & Calling"
                                AppLanguage.MALAY -> "Awal Perjalanan & Dakwah"
                                else -> "Awal Perjalanan & Dakwah"
                            }
                            1 -> when (language) {
                                AppLanguage.ENGLISH -> "Trials & Steadfast Struggle"
                                AppLanguage.MALAY -> "Ujian & Ketabahan Perjuangan"
                                else -> "Ujian & Ketabahan Perjuangan"
                            }
                            else -> when (language) {
                                AppLanguage.ENGLISH -> "Divine Help & Triumph"
                                AppLanguage.MALAY -> "Pertolongan Allah & Kemenangan"
                                else -> "Pertolongan Allah & Kemenangan"
                            }
                        }
                        chaptersList.add(Pair(defaultTitle, slice))
                    }
                }
            } else {
                val title = when (language) {
                    AppLanguage.ENGLISH -> "Story & Journey"
                    AppLanguage.MALAY -> "Kisah & Perjalanan"
                    else -> "Kisah & Perjalanan"
                }
                chaptersList.add(Pair(title, allParas))
            }
        }

        // 4. Distribute QuranReferences across chapters
        val assignedVerses = Array(chaptersList.size) { mutableListOf<QuranReference>() }
        for ((rIdx, ref) in references.withIndex()) {
            var targetCh = -1
            for ((cIdx, ch) in chaptersList.withIndex()) {
                val combined = (ch.first + " " + ch.second.joinToString(" ")).lowercase()
                if (combined.contains(ref.surahName.lowercase())) {
                    targetCh = cIdx
                    break
                }
            }
            if (targetCh == -1) {
                targetCh = (rIdx * chaptersList.size / references.size).coerceIn(0, chaptersList.lastIndex)
            }
            assignedVerses[targetCh].add(ref)
        }

        // 5. Construct StoryChapterUiModel with paragraphsBefore, verses (in the middle!), paragraphsAfter
        val resultChapters = chaptersList.mapIndexed { idx, (title, paras) ->
            val chVerses = assignedVerses[idx]
            val cleanTitle = title.ifBlank { defaultChapterTitle(language, idx + 1) }
            if (paras.isEmpty()) {
                StoryChapterUiModel(
                    index = idx + 1,
                    title = cleanTitle,
                    paragraphsBefore = emptyList(),
                    verses = chVerses,
                    paragraphsAfter = emptyList()
                )
            } else {
                val mid = (paras.size / 2).coerceIn(1, (paras.size - 1).coerceAtLeast(1))
                StoryChapterUiModel(
                    index = idx + 1,
                    title = cleanTitle,
                    paragraphsBefore = paras.take(mid),
                    verses = chVerses,
                    paragraphsAfter = paras.drop(mid)
                )
            }
        }

        return Pair(resultChapters, hikmahUi)
    }

    private fun defaultChapterTitle(language: AppLanguage, index: Int): String =
        when (language) {
            AppLanguage.ENGLISH -> "Chapter $index"
            AppLanguage.MALAY, AppLanguage.INDONESIAN -> "Bab $index"
        }

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
