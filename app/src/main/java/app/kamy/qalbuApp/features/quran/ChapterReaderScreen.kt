package app.kamy.qalbuApp.features.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.HadithReference
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.features.today.components.TafsirSheet
import app.kamy.qalbuApp.infrastructure.audio.AudioPlayerController
import app.kamy.qalbuApp.ui.common.TajweedHtmlView
import app.kamy.qalbuApp.ui.common.buildTajweedHtmlFragment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderScreen(
    audioPlayer: AudioPlayerController,
    initialVerseNumber: Int? = null,
    onBack: () -> Unit
) {
    val vm: ChapterReaderViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val audioState by audioPlayer.state.collectAsState()
    val listState = rememberLazyListState()
    var settingsVisible by remember { mutableStateOf(false) }

    // Trigger pagination when scrolled near bottom.
    val firstVisibleIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    LaunchedEffect(firstVisibleIndex) {
        vm.loadMoreIfNeeded(firstVisibleIndex)
        state.verses.getOrNull(firstVisibleIndex)?.resolvedVerseNumber?.let { vm.logScrollPosition(it) }
    }

    // Jump to requested initial verse.
    LaunchedEffect(state.verses.isNotEmpty(), initialVerseNumber) {
        if (state.verses.isNotEmpty() && initialVerseNumber != null) {
            val idx = state.verses.indexOfFirst { it.resolvedVerseNumber == initialVerseNumber }
            if (idx >= 0) listState.animateScrollToItem(idx)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AlKhatibColors.ScreenBackground)) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(AlKhatibColors.DeepEmerald)
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = state.chapterDisplayName ?: "Surah ${state.chapterNumber}",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = { settingsVisible = true }) {
                Icon(Icons.Filled.Settings, contentDescription = "Reading settings", tint = Color.White)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.isLoading && state.verses.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AlKhatibColors.DeepEmerald
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    flingBehavior = rememberSnapFlingBehavior(listState)
                ) {
                    itemsIndexed(state.verses, key = { _, v -> v.listIdentity }) { _, verse ->
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            AyahCard(
                                verse = verse,
                                fontScale = state.fontScale,
                                showTranslation = state.showTranslation,
                                isPlaying = audioPlayer.isPlayingUrl(verse.audio?.url),
                                onPlay = {
                                    val url = verse.audio?.url ?: return@AyahCard
                                    if (audioPlayer.isPlayingUrl(url)) {
                                        audioPlayer.toggle()
                                    } else {
                                        audioPlayer.playVerse(
                                            url = url,
                                            surahTitle = state.chapterDisplayName
                                                ?: "Surah ${state.chapterNumber}",
                                            ayahLabel = verse.verseKey.orEmpty(),
                                            reciterName = state.recitations
                                                .firstOrNull { it.id == state.selectedRecitationId }
                                                ?.displayName.orEmpty()
                                        )
                                    }
                                },
                                onTafsir = { verse.verseKey?.let(vm::openTafsir) },
                                onHadith = { verse.verseKey?.let(vm::openHadith) }
                            )
                        }
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AlKhatibColors.DeepEmerald)
                            }
                        }
                    }
                }
            }

            // Sticky play-entire-surah button.
            IconButton(
                onClick = {
                    val items = vm.audioQueueItems()
                    if (items.isNotEmpty()) {
                        audioPlayer.playSequence(
                            items = items,
                            surahTitle = state.chapterDisplayName
                                ?: "Surah ${state.chapterNumber}",
                            reciterName = state.recitations
                                .firstOrNull { it.id == state.selectedRecitationId }
                                ?.displayName.orEmpty()
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.Teal))
                    )
            ) {
                Icon(
                    imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play entire surah",
                    tint = Color.White
                )
            }
        }
    }

    if (settingsVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(onDismissRequest = { settingsVisible = false }, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Reading settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
                Text("Text size", style = MaterialTheme.typography.labelLarge, color = AlKhatibColors.Slate500)
                Slider(
                    value = state.fontScale,
                    onValueChange = vm::setFontScale,
                    valueRange = 0.85f..1.35f,
                    steps = 9
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Show translation", modifier = Modifier.weight(1f), color = AlKhatibColors.Slate900)
                    Switch(checked = state.showTranslation, onCheckedChange = vm::toggleTranslation)
                }
                Text("Reciter", style = MaterialTheme.typography.labelLarge, color = AlKhatibColors.Slate500)
                state.recitations.forEach { r ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (r.id == state.selectedRecitationId)
                                    AlKhatibColors.DeepEmerald.copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .padding(12.dp)
                    ) {
                        Text(text = r.displayName, modifier = Modifier.weight(1f), color = AlKhatibColors.Slate900)
                        if (r.id == state.selectedRecitationId) Text("✓", color = AlKhatibColors.DeepEmerald)
                    }
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AlKhatibColors.SoftGrey))
                }
            }
        }
    }

    TafsirSheet(
        isVisible = state.tafsirVisible,
        isLoading = state.tafsirLoading,
        tafsir = state.tafsir,
        onDismiss = { vm.dismissTafsir() }
    )

    HadithSheet(
        isVisible = state.hadithVisible,
        isLoading = state.hadithLoading,
        hadiths = state.hadiths,
        onDismiss = { vm.dismissHadith() }
    )
}

@Composable
private fun AyahCard(
    verse: RandomAyahPayload,
    fontScale: Float,
    showTranslation: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onTafsir: () -> Unit,
    onHadith: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AlKhatibColors.DeepEmerald),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (verse.resolvedVerseNumber ?: 0).toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = verse.verseKey.orEmpty(),
                color = AlKhatibColors.Slate500,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onTafsir) { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Tafsir", tint = AlKhatibColors.IndigoAccent) }
            IconButton(onClick = onHadith) { Icon(Icons.Filled.Forum, contentDescription = "Hadith", tint = AlKhatibColors.Gold) }
            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = AlKhatibColors.DeepEmerald
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        TajweedHtmlView(
            htmlFragment = buildTajweedHtmlFragment(
                verse.textUthmaniTajweed ?: verse.textUthmani,
                verse.resolvedVerseNumber
            ),
            fontSizeSp = (28 * fontScale).toInt(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 240.dp)
        )
        if (showTranslation) {
            verse.translations?.firstOrNull()?.text?.let { translation ->
                val clean = translation.replace(Regex("<[^>]+>"), "").trim()
                if (clean.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = clean,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AlKhatibColors.Slate800,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithSheet(
    isVisible: Boolean,
    isLoading: Boolean,
    hadiths: List<HadithReference>,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Hadith references", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
            when {
                isLoading -> CircularProgressIndicator(color = AlKhatibColors.DeepEmerald)
                hadiths.isEmpty() -> Text("No hadith for this ayah.", color = AlKhatibColors.Slate500)
                else -> hadiths.forEach { h ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AlKhatibColors.LightGrey)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${h.collection.orEmpty()} ${h.bookNumber.orEmpty()}".trim(),
                            color = AlKhatibColors.Gold,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = h.hadith?.firstOrNull()?.body?.replace(Regex("<[^>]+>"), "")?.trim().orEmpty(),
                            color = AlKhatibColors.Slate900,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
