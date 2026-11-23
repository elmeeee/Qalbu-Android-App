package app.kamy.saatApp.features.tools.encyclopedia

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.EncyclopediaTopic
import app.kamy.saatApp.domain.model.QuranReference
import app.kamy.saatApp.features.today.TanyaSaatViewModel
import app.kamy.saatApp.ui.components.TanyaSaatFullScreen
import kotlinx.coroutines.launch

private val WarmSand = Color(0xFFE8E2D5)
private val Slate400 = Color(0xFF94A3B8)
private val Slate600 = Color(0xFF475569)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaDetailScreen(
    onBack: () -> Unit,
    onOpenVerse: (surahNumber: Int, ayahNumber: Int) -> Unit = { _, _ -> },
    viewModel: EncyclopediaDetailViewModel = hiltViewModel(),
    tanyaSaatVm: TanyaSaatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tanyaSaatState by tanyaSaatVm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val topic = state.topic
    val title = topic?.localizedTitle(state.currentLanguage).orEmpty()
    val subtitle = topic?.localizedSubtitle(state.currentLanguage).orEmpty()

    val chapters = state.chapters
    val hasHikmah = state.hikmah != null
    // Page 0: Cover/Overview, Pages 1..N: Chapters, Page N+1: Hikmah (if present)
    val totalPages = remember(chapters.size, hasHikmah) {
        1 + chapters.size + (if (hasHikmah) 1 else 0)
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { totalPages }
    )

    var showTocSheet by remember { mutableStateOf(false) }

    LaunchedEffect(topic?.id) {
        if (topic != null && pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                app.kamy.saatApp.features.tools.components.SpiritualToolTopBar(
                    title = title.ifBlank { stringResource(R.string.encyclopedia_title) },
                    subtitle = if (pagerState.currentPage == 0) {
                        subtitle.takeIf { it.isNotBlank() } ?: stringResource(R.string.encyclopedia_cover_label)
                    } else if (pagerState.currentPage <= chapters.size) {
                        val ch = chapters.getOrNull(pagerState.currentPage - 1)
                        ch?.title ?: stringResource(R.string.encyclopedia_chapter_badge, pagerState.currentPage, chapters.size)
                    } else {
                        stringResource(R.string.encyclopedia_hikmah_badge)
                    },
                    onBack = onBack,
                    actions = {
                        if (topic != null) {
                            IconButton(
                                onClick = { showTocSheet = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, WarmSand, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = stringResource(R.string.encyclopedia_toc_button),
                                    tint = SaatColors.DeepEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (topic != null && !state.isLoading && totalPages > 1) {
                    BookBottomNavBar(
                        currentPage = pagerState.currentPage,
                        totalPages = totalPages,
                        chapters = chapters,
                        hasHikmah = hasHikmah,
                        onPrev = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        onNext = {
                            if (pagerState.currentPage < totalPages - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.encyclopedia_finish_reading),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onOpenToc = { showTocSheet = true }
                    )
                }
            },
            containerColor = SaatColors.HomeBg
        ) { innerPadding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SaatColors.DeepEmerald)
                }
            } else if (topic == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.encyclopedia_empty_title),
                        color = SaatColors.Slate500
                    )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    beyondViewportPageCount = 1,
                    key = { page -> "${topic.id}_page_$page" }
                ) { pageIndex ->
                    when {
                        // Page 0: Cover & Sinopsis & Daftar Bab
                        pageIndex == 0 -> {
                            StoryCoverPage(
                                topic = topic,
                                state = state,
                                chapters = chapters,
                                onStartReading = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                },
                                onSelectChapter = { targetPage ->
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(targetPage)
                                    }
                                }
                            )
                        }
                        // Pages 1..N: Chapters with narrative and interspersed verses
                        pageIndex <= chapters.size -> {
                            val chapter = chapters[pageIndex - 1]
                            StoryChapterPage(
                                chapter = chapter,
                                state = state,
                                isLastChapter = pageIndex == chapters.size && !hasHikmah,
                                onOpenVerse = onOpenVerse,
                                onNextPage = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pageIndex + 1)
                                    }
                                },
                                onCopyVerse = { text ->
                                    clipboardManager.setText(AnnotatedString(text))
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.encyclopedia_copied_toast),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                        // Final Page: Hikmah & Pelajaran
                        else -> {
                            val hikmah = state.hikmah
                            if (hikmah != null) {
                                StoryHikmahPage(
                                    hikmah = hikmah,
                                    topic = topic,
                                    state = state,
                                    onAskAi = {
                                        tanyaSaatVm.onInputTextChanged(
                                            "Bisa ceritakan lebih dalam mengenai hikmah dan teladan dari ${topic.localizedTitle(state.currentLanguage)}?"
                                        )
                                        tanyaSaatVm.openSheet()
                                    },
                                    onSelectTopic = { newTopicId ->
                                        viewModel.selectTopic(newTopicId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Table of Contents Bottom Sheet
        if (showTocSheet && topic != null) {
            TableOfContentsSheet(
                topicTitle = title,
                chapters = chapters,
                hikmah = state.hikmah,
                currentPage = pagerState.currentPage,
                onSelectPage = { targetPage ->
                    showTocSheet = false
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(targetPage)
                    }
                },
                onDismiss = { showTocSheet = false }
            )
        }

        // TanyaSaat AI FullScreen Sheet
        if (tanyaSaatState.isSheetVisible) {
            TanyaSaatFullScreen(
                state = tanyaSaatState,
                onDismiss = { tanyaSaatVm.closeSheet() },
                onModelSelected = tanyaSaatVm::onModelSelected,
                onMoodSelected = tanyaSaatVm::onMoodSelected,
                onInputChanged = tanyaSaatVm::onInputTextChanged,
                onSendMessage = { tanyaSaatVm.sendMessage() },
                onOpenVerseInReader = { chapter, verse ->
                    tanyaSaatVm.closeSheet()
                    onOpenVerse(chapter, verse)
                },
                onBookmarkVerse = { verseData ->
                    tanyaSaatVm.bookmarkVerse(context, verseData)
                },
                onClearToast = tanyaSaatVm::clearToast,
                onFollowUp = tanyaSaatVm::askFollowUp,
                onNewChat = tanyaSaatVm::startNewChat,
                onRetry = tanyaSaatVm::retryLast
            )
        }
    }
}

// =============================================================================
// Page 0: Cover & Overview Page
// =============================================================================

@Composable
private fun StoryCoverPage(
    topic: EncyclopediaTopic,
    state: EncyclopediaDetailUiState,
    chapters: List<StoryChapterUiModel>,
    onStartReading: () -> Unit,
    onSelectChapter: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val title = topic.localizedTitle(state.currentLanguage)
    val subtitle = topic.localizedSubtitle(state.currentLanguage)
    val summary = topic.localizedSummary(state.currentLanguage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Book Cover Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.25f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Decorative gradient wash
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SaatColors.DeepEmerald.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category Badge Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SaatColors.DeepEmerald.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.20f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = SaatColors.DeepEmerald
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.encyclopedia_cat_prophets).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Decorative Story Title
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SaatColors.GoldDeep,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Bar (Chapters, Time, Verses)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SaatColors.HomeBg)
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            value = "${chapters.size}",
                            label = stringResource(R.string.encyclopedia_toc_button)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(WarmSand)
                        )
                        StatItem(
                            value = "${topic.readTimeMinutes} mnt",
                            label = "Bacaan"
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(WarmSand)
                        )
                        StatItem(
                            value = "${topic.quranReferences.size}",
                            label = "Dalil Ayat"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sinopsis / Summary Box
                    if (summary.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = WarmSand.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Sinopsis Kisah",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.DeepEmerald,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = summary,
                                    fontSize = 13.5.sp,
                                    color = SaatColors.Slate700,
                                    lineHeight = 21.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Start Reading Button
                    Button(
                        onClick = onStartReading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaatColors.DeepEmerald,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.encyclopedia_start_reading),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Table of Contents Section
        Text(
            text = stringResource(R.string.encyclopedia_table_of_contents),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SaatColors.DeepEmerald,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                chapters.forEachIndexed { index, ch ->
                    val pageTarget = index + 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectChapter(pageTarget) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SaatColors.DeepEmerald.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${ch.index}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ch.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SaatColors.Slate900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (ch.verses.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "📜 ${ch.verses.size} Dalil Al-Qur'an ditengah cerita",
                                    fontSize = 11.5.sp,
                                    color = SaatColors.GoldDeep
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Slate400
                        )
                    }

                    if (index < chapters.lastIndex || state.hikmah != null) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = WarmSand.copy(alpha = 0.5f)
                        )
                    }
                }

                if (state.hikmah != null) {
                    val hikmahPageTarget = chapters.size + 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectChapter(hikmahPageTarget) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SaatColors.GoldDeep.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = SaatColors.GoldDeep
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.hikmah.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mutiara hikmah, iktibar & penutup",
                                fontSize = 11.5.sp,
                                color = SaatColors.Slate500
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Slate400
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SaatColors.DeepEmerald
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = SaatColors.Slate500
        )
    }
}

// =============================================================================
// Pages 1..N: Chapter Page with Verses Interspersed in the Narrative
// =============================================================================

@Composable
private fun StoryChapterPage(
    chapter: StoryChapterUiModel,
    state: EncyclopediaDetailUiState,
    isLastChapter: Boolean,
    onOpenVerse: (surahNumber: Int, ayahNumber: Int) -> Unit,
    onNextPage: () -> Unit,
    onCopyVerse: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(0.5.dp, WarmSand)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Chapter Badge Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SaatColors.GoldDeep.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(R.string.encyclopedia_chapter_badge, chapter.index, state.chapters.size).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.GoldDeep,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chapter Title
                Text(
                    text = chapter.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = WarmSand.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // First half of narrative paragraphs (leading up to the verse)
                chapter.paragraphsBefore.forEachIndexed { idx, para ->
                    StoryParagraph(text = para)
                    if (idx < chapter.paragraphsBefore.lastIndex) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // =============================================================
                // QUR'AN VERSES: Interspersed right in the middle of the story!
                // =============================================================
                if (chapter.verses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    chapter.verses.forEach { qRef ->
                        QuranVerseEmbeddedCard(
                            qRef = qRef,
                            language = state.currentLanguage,
                            onOpenVerse = onOpenVerse,
                            onCopy = onCopyVerse
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Second half of narrative paragraphs (continuation after verse)
                if (chapter.paragraphsAfter.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    chapter.paragraphsAfter.forEachIndexed { idx, para ->
                        StoryParagraph(text = para)
                        if (idx < chapter.paragraphsAfter.lastIndex) {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Chapter Bottom Hint / Next Page Button
                if (!isLastChapter) {
                    OutlinedButton(
                        onClick = onNextPage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SaatColors.DeepEmerald
                        ),
                        border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = stringResource(R.string.encyclopedia_next_page),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StoryParagraph(text: String) {
    val isDialogue = text.startsWith("”") || text.startsWith("\"") || text.startsWith("‘") || text.startsWith("'")
    Text(
        text = text,
        fontSize = 15.sp,
        color = if (isDialogue) SaatColors.Slate900 else Color(0xFF1E293B),
        lineHeight = 25.sp,
        fontWeight = if (isDialogue) FontWeight.Medium else FontWeight.Normal
    )
}

// =============================================================================
// Quran Verse Card with Arabic + Latin Transliteration + Translation
// =============================================================================

@Composable
private fun QuranVerseEmbeddedCard(
    qRef: QuranReference,
    language: AppLanguage,
    onOpenVerse: (surahNumber: Int, ayahNumber: Int) -> Unit,
    onCopy: (String) -> Unit
) {
    val startAyah = remember(qRef.ayahRange) {
        qRef.ayahRange
            .split("-", ":", "–", "—")
            .firstOrNull()
            ?.trim()
            ?.filter { it.isDigit() }
            ?.toIntOrNull() ?: 1
    }

    val surahName = qRef.localizedSurahName(language)
    val verseLatin = qRef.localizedVerseLatin(language)
    val verseTranslation = qRef.localizedVerseTranslation(language)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F9F6)
        ),
        border = BorderStroke(1.2.dp, SaatColors.DeepEmerald.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Surah Label & Open in Quran Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SaatColors.DeepEmerald.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(R.string.encyclopedia_surah_label, surahName, qRef.surahNumber, qRef.ayahRange),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable { onOpenVerse(qRef.surahNumber, startAyah) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.encyclopedia_open_verse),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.DeepEmerald
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = SaatColors.DeepEmerald
                        )
                    }
                }
            }

            // Arabic Verse Text (Prominent & Clear)
            if (qRef.verseTextAr.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = qRef.verseTextAr,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate900,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Latin Transliteration (Mandatory Requirement)
            if (verseLatin.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(0.5.dp, WarmSand)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SaatColors.GoldDeep.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = stringResource(R.string.encyclopedia_transliteration_label),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.GoldDeep,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = verseLatin,
                            fontSize = 13.5.sp,
                            fontStyle = FontStyle.Italic,
                            color = SaatColors.DeepEmerald,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Translation (Arti / Terjemahan)
            if (verseTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "\"$verseTranslation\"",
                    fontSize = 13.5.sp,
                    color = SaatColors.Slate700,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Copy Verse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    border = BorderStroke(0.5.dp, WarmSand),
                    modifier = Modifier.clickable {
                        val shareText = buildString {
                            append("QS. $surahName : ${qRef.ayahRange}\n\n")
                            if (qRef.verseTextAr.isNotBlank()) append("${qRef.verseTextAr}\n\n")
                            if (verseLatin.isNotBlank()) append("$verseLatin\n\n")
                            if (verseTranslation.isNotBlank()) append("\"$verseTranslation\"")
                        }
                        onCopy(shareText)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.encyclopedia_copy_verse),
                            modifier = Modifier.size(13.dp),
                            tint = Slate600
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.encyclopedia_copy_verse),
                            fontSize = 11.5.sp,
                            color = Slate600
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Final Page: Hikmah, Lessons & Reflection Page
// =============================================================================

@Composable
private fun StoryHikmahPage(
    hikmah: StoryHikmahUiModel,
    topic: EncyclopediaTopic,
    state: EncyclopediaDetailUiState,
    onAskAi: () -> Unit,
    onSelectTopic: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SaatColors.GoldDeep.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SaatColors.GoldDeep
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.encyclopedia_hikmah_badge).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.GoldDeep,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = hikmah.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Refleksi dan intisari berharga dari perjalanan ${topic.localizedTitle(state.currentLanguage)}",
                    fontSize = 13.5.sp,
                    color = SaatColors.Slate500
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Lessons List Cards
        hikmah.lessons.forEachIndexed { idx, lesson ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SaatColors.DeepEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = lesson,
                        fontSize = 14.sp,
                        color = SaatColors.Slate800,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        if (hikmah.conclusion.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WarmSand.copy(alpha = 0.25f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tadabbur Penutup",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = hikmah.conclusion,
                        fontSize = 13.5.sp,
                        color = SaatColors.Slate700,
                        lineHeight = 21.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TanyaSaat AI Discussion Button
        Button(
            onClick = onAskAi,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SaatColors.DeepEmerald,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.encyclopedia_ask_ai),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Previous and Next Prophet Navigation
        if (state.nextTopicId != null || state.prevTopicId != null) {
            Spacer(modifier = Modifier.height(16.dp))

            if (state.nextTopicId != null && state.nextTopicTitle != null) {
                OutlinedButton(
                    onClick = { onSelectTopic(state.nextTopicId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SaatColors.DeepEmerald),
                    border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = stringResource(R.string.encyclopedia_read_next_topic, state.nextTopicTitle),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (state.prevTopicId != null && state.prevTopicTitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { onSelectTopic(state.prevTopicId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = SaatColors.Slate500
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.encyclopedia_read_prev_topic, state.prevTopicTitle),
                        fontSize = 13.sp,
                        color = SaatColors.Slate500
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// =============================================================================
// Book Bottom Navigation Bar (Pager Control)
// =============================================================================

@Composable
private fun BookBottomNavBar(
    currentPage: Int,
    totalPages: Int,
    chapters: List<StoryChapterUiModel>,
    hasHikmah: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onOpenToc: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, WarmSand.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous Button
            TextButton(
                onClick = onPrev,
                enabled = currentPage > 0,
                modifier = Modifier.widthIn(min = 80.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = SaatColors.DeepEmerald,
                    disabledContentColor = Slate400.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.encyclopedia_prev_page),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.encyclopedia_prev_page),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Center: Page indicator & progress
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenToc() }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.encyclopedia_page_indicator, currentPage + 1, totalPages),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )

                Spacer(modifier = Modifier.height(4.dp))

                val progress = remember(currentPage, totalPages) {
                    ((currentPage + 1).toFloat() / totalPages).coerceIn(0f, 1f)
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(90.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SaatColors.DeepEmerald,
                    trackColor = WarmSand
                )
            }

            // Next / Finish Button
            Button(
                onClick = onNext,
                modifier = Modifier.widthIn(min = 80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentPage == totalPages - 1) SaatColors.GoldDeep else SaatColors.DeepEmerald,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val nextLabel = when (currentPage) {
                    0 -> stringResource(R.string.encyclopedia_start_reading)
                    totalPages - 1 -> stringResource(R.string.encyclopedia_finish_reading)
                    else -> stringResource(R.string.encyclopedia_next_page)
                }
                Text(
                    text = nextLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (currentPage == totalPages - 1) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

// =============================================================================
// Table of Contents Modal Bottom Sheet
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableOfContentsSheet(
    topicTitle: String,
    chapters: List<StoryChapterUiModel>,
    hikmah: StoryHikmahUiModel?,
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = stringResource(R.string.encyclopedia_table_of_contents),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = topicTitle,
                fontSize = 13.sp,
                color = SaatColors.Slate500
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Page 0: Cover
            TocItemRow(
                pageIndex = 0,
                title = stringResource(R.string.encyclopedia_cover_label),
                subtitle = "Ringkasan dan informasi awal kisah",
                isSelected = currentPage == 0,
                onClick = { onSelectPage(0) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 0.5.dp,
                color = WarmSand.copy(alpha = 0.5f)
            )

            // Chapters 1..N
            chapters.forEachIndexed { index, chapter ->
                val pageIndex = index + 1
                TocItemRow(
                    pageIndex = pageIndex,
                    title = "Bab ${chapter.index}: ${chapter.title}",
                    subtitle = if (chapter.verses.isNotEmpty()) "${chapter.verses.size} Dalil Ayat Al-Qur'an" else null,
                    isSelected = currentPage == pageIndex,
                    onClick = { onSelectPage(pageIndex) }
                )
            }

            // Hikmah Page
            if (hikmah != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 0.5.dp,
                    color = WarmSand.copy(alpha = 0.5f)
                )

                val hikmahPage = chapters.size + 1
                TocItemRow(
                    pageIndex = hikmahPage,
                    title = hikmah.title,
                    subtitle = "Hikmah, iktibar dan refleksi akhir",
                    isSelected = currentPage == hikmahPage,
                    onClick = { onSelectPage(hikmahPage) }
                )
            }
        }
    }
}

@Composable
private fun TocItemRow(
    pageIndex: Int,
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SaatColors.DeepEmerald.copy(alpha = 0.10f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SaatColors.DeepEmerald else WarmSand),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$pageIndex",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else SaatColors.Slate700
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) SaatColors.DeepEmerald else SaatColors.Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = if (isSelected) SaatColors.DeepEmerald.copy(alpha = 0.8f) else SaatColors.Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = SaatColors.DeepEmerald
                )
            }
        }
    }
}
