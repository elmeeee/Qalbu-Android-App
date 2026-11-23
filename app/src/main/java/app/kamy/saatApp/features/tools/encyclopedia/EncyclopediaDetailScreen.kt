package app.kamy.saatApp.features.tools.encyclopedia

import android.animation.ValueAnimator
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.EncyclopediaTopic
import app.kamy.saatApp.domain.model.QuranReference
import app.kamy.saatApp.features.quran.components.PageCurlPager
import app.kamy.saatApp.features.quran.components.QuranReaderHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged

private val WarmSand = Color(0xFFE8E2D5)
private val Slate400 = Color(0xFF94A3B8)
private val Slate600 = Color(0xFF475569)
private val BookCanvas = Color(0xFFEDE5D8)
private val BookPaper = Color(0xFFFFFBF2)
private val BookInk = Color(0xFF30271F)
private val BookRule = Color(0xFFD8C9AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaDetailScreen(
    onBack: () -> Unit,
    onOpenVerse: (surahNumber: Int, ayahNumber: Int) -> Unit = { _, _ -> },
    viewModel: EncyclopediaDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
    val readerBackdropLayer = rememberGraphicsLayer()
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var readerHeaderHeight by remember(statusBarInset) { mutableStateOf(statusBarInset + 60.dp) }
    val headerContentInset = readerHeaderHeight

    var settledPage by remember(topic?.id) { mutableIntStateOf(0) }
    var restoredTopicId by remember { mutableStateOf<String?>(null) }
    var showTocSheet by remember { mutableStateOf(false) }

    LaunchedEffect(topic?.id, totalPages, state.isLoading) {
        val topicId = topic?.id ?: return@LaunchedEffect
        if (state.isLoading) return@LaunchedEffect

        restoredTopicId = null
        val savedPage = viewModel.loadReadingPage(topicId).coerceIn(0, (totalPages - 1).coerceAtLeast(0))
        pagerState.scrollToPage(savedPage)
        settledPage = savedPage
        restoredTopicId = topicId
    }

    LaunchedEffect(pagerState, topic?.id, totalPages) {
        val topicId = topic?.id ?: return@LaunchedEffect
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { (page, isScrolling) ->
                if (!isScrolling && restoredTopicId == topicId) {
                    val completedPage = page.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
                    val didTurnPage = completedPage != settledPage
                    settledPage = completedPage
                    if (didTurnPage) {
                        viewModel.saveReadingPage(topicId, completedPage)
                    }
                }
            }
    }

    val headerSubtitle = if (pagerState.currentPage == 0) {
        subtitle.takeIf { it.isNotBlank() } ?: stringResource(R.string.encyclopedia_cover_label)
    } else if (pagerState.currentPage <= chapters.size) {
        val chapter = chapters.getOrNull(pagerState.currentPage - 1)
        chapter?.title ?: stringResource(R.string.encyclopedia_chapter_badge, pagerState.currentPage, chapters.size)
    } else {
        stringResource(R.string.encyclopedia_hikmah_badge)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BookCanvas)
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SaatColors.DeepEmerald)
                }
            }

            topic == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.encyclopedia_empty_title),
                        color = SaatColors.Slate500
                    )
                }
            }

            else -> {
                val pageActionsEnabled = restoredTopicId == topic.id &&
                    !pagerState.isScrollInProgress
                val currentPageDescription = stringResource(
                    R.string.encyclopedia_page_indicator,
                    settledPage + 1,
                    totalPages
                )
                PageCurlPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .drawWithContent {
                            if (pagerState.isScrollInProgress) {
                                drawContent()
                            } else {
                                readerBackdropLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(readerBackdropLayer)
                            }
                        }
                        .semantics {
                            stateDescription = currentPageDescription
                            liveRegion = LiveRegionMode.Polite
                            customActions = buildList {
                                if (pageActionsEnabled && settledPage > 0) {
                                    add(
                                        CustomAccessibilityAction(
                                            label = context.getString(R.string.encyclopedia_accessibility_previous_page)
                                        ) {
                                            requestPageTurn(
                                                pagerState,
                                                settledPage - 1,
                                                coroutineScope
                                            )
                                            true
                                        }
                                    )
                                }
                                if (pageActionsEnabled && settledPage < totalPages - 1) {
                                    add(
                                        CustomAccessibilityAction(
                                            label = context.getString(R.string.encyclopedia_accessibility_next_page)
                                        ) {
                                            requestPageTurn(
                                                pagerState,
                                                settledPage + 1,
                                                coroutineScope
                                            )
                                            true
                                        }
                                    )
                                }
                            }
                        },
                    userScrollEnabled = restoredTopicId == topic.id,
                    excludeSystemGestures = false,
                    instantSnapWhenAnimationsDisabled = true
                ) { pageIndex ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                if (pageIndex != settledPage) hideFromAccessibility()
                            }
                    ) {
                        when {
                            pageIndex == 0 -> {
                                StoryCoverPage(
                                    topic = topic,
                                    state = state,
                                    chapters = chapters,
                                    contentTopInset = headerContentInset,
                                    onStartReading = {
                                        requestPageTurn(pagerState, 1, coroutineScope)
                                    },
                                    onSelectChapter = { targetPage ->
                                        requestPageTurn(pagerState, targetPage, coroutineScope)
                                    }
                                )
                            }
                            pageIndex <= chapters.size -> {
                                val chapter = chapters[pageIndex - 1]
                                StoryChapterPage(
                                    chapter = chapter,
                                    state = state,
                                    bookTitle = topic.localizedTitle(state.currentLanguage),
                                    pageNumber = pageIndex + 1,
                                    contentTopInset = headerContentInset,
                                    onOpenVerse = onOpenVerse,
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
                            else -> {
                                val hikmah = state.hikmah
                                if (hikmah != null) {
                                    StoryHikmahPage(
                                        hikmah = hikmah,
                                        topic = topic,
                                        state = state,
                                        pageNumber = pageIndex + 1,
                                        contentTopInset = headerContentInset
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        QuranReaderHeader(
            title = title.ifBlank { stringResource(R.string.encyclopedia_title) },
            subtitle = headerSubtitle,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter),
            backdropLayer = readerBackdropLayer,
            isPageTurning = pagerState.isScrollInProgress,
            onHeightChanged = { readerHeaderHeight = it }
        ) {
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

        // Table of Contents Bottom Sheet
        if (showTocSheet && topic != null) {
            TableOfContentsSheet(
                topicTitle = title,
                chapters = chapters,
                hikmah = state.hikmah,
                currentPage = pagerState.currentPage,
                onSelectPage = { targetPage ->
                    showTocSheet = false
                    requestPageTurn(pagerState, targetPage, coroutineScope)
                },
                onDismiss = { showTocSheet = false }
            )
        }

    }
}

private fun requestPageTurn(
    pagerState: PagerState,
    targetPage: Int,
    coroutineScope: CoroutineScope
) {
    if (pagerState.isScrollInProgress || targetPage !in 0 until pagerState.pageCount) return

    coroutineScope.launch {
        if (ValueAnimator.areAnimatorsEnabled()) {
            pagerState.animateScrollToPage(targetPage)
        } else {
            pagerState.scrollToPage(targetPage)
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
    contentTopInset: Dp,
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
            .padding(
                start = 20.dp,
                top = contentTopInset + 16.dp,
                end = 20.dp,
                bottom = 16.dp
            )
    ) {
        // Book Cover Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = SaatColors.DeepEmerald),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.5.dp, SaatColors.GoldDeep.copy(alpha = 0.7f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SaatColors.GoldDeep.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(12.dp)
                        .border(
                            1.dp,
                            SaatColors.GoldDeep.copy(alpha = 0.45f),
                            RoundedCornerShape(2.dp)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category Badge Pill
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = SaatColors.GoldDeep.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFE6A6)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.encyclopedia_cat_prophets).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFE6A6),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Decorative Story Title
                    Text(
                        text = title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        color = BookPaper,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp
                    )

                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = BookPaper.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Bar (Chapters, Time, Verses)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            value = "${chapters.size}",
                            label = stringResource(R.string.encyclopedia_toc_button),
                            valueColor = BookPaper,
                            labelColor = BookPaper.copy(alpha = 0.72f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(SaatColors.GoldDeep.copy(alpha = 0.45f))
                        )
                        StatItem(
                            value = "${topic.readTimeMinutes}",
                            label = stringResource(R.string.encyclopedia_reading_minutes),
                            valueColor = BookPaper,
                            labelColor = BookPaper.copy(alpha = 0.72f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(SaatColors.GoldDeep.copy(alpha = 0.45f))
                        )
                        StatItem(
                            value = "${topic.quranReferences.size}",
                            label = stringResource(R.string.encyclopedia_quran_reference_count),
                            valueColor = BookPaper,
                            labelColor = BookPaper.copy(alpha = 0.72f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sinopsis / Summary Box
                    if (summary.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = Color.White.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.encyclopedia_story_overview),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFE6A6),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = summary,
                                    fontSize = 13.5.sp,
                                    color = BookPaper.copy(alpha = 0.88f),
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
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaatColors.GoldDeep,
                            contentColor = SaatColors.DeepEmerald
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
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif,
            color = SaatColors.DeepEmerald,
            modifier = Modifier
                .widthIn(max = 520.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(3.dp),
            colors = CardDefaults.cardColors(containerColor = BookPaper),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, BookRule)
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
                                .clip(RoundedCornerShape(2.dp))
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
                                    text = stringResource(
                                        R.string.encyclopedia_chapter_quran_references,
                                        ch.verses.size
                                    ),
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
                                text = stringResource(R.string.encyclopedia_hikmah_summary),
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
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color = SaatColors.DeepEmerald,
    labelColor: Color = SaatColors.Slate500
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = labelColor
        )
    }
}

// =============================================================================
// Pages 1..N: Chapter Page with Verses Interspersed in the Narrative
// =============================================================================

@Composable
private fun BookPageSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(3.dp),
        color = BookPaper,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, BookRule)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            content = content
        )
    }
}

@Composable
private fun BookPageHeader(bookTitle: String, pageNumber: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = bookTitle.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = SaatColors.Slate500,
            letterSpacing = 1.2.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = pageNumber.toString(),
            fontSize = 10.sp,
            fontFamily = FontFamily.Serif,
            color = SaatColors.Slate500
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(thickness = 0.7.dp, color = BookRule)
}

@Composable
private fun StoryChapterPage(
    chapter: StoryChapterUiModel,
    state: EncyclopediaDetailUiState,
    bookTitle: String,
    pageNumber: Int,
    contentTopInset: Dp,
    onOpenVerse: (surahNumber: Int, ayahNumber: Int) -> Unit,
    onCopyVerse: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                start = 16.dp,
                top = contentTopInset + 12.dp,
                end = 16.dp,
                bottom = 12.dp
            )
    ) {
        BookPageSheet(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            BookPageHeader(bookTitle, pageNumber)

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(
                    R.string.encyclopedia_chapter_badge,
                    chapter.index,
                    state.chapters.size
                ).uppercase(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = SaatColors.GoldDeep,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = chapter.title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 27.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif,
                color = BookInk,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 36.dp),
                thickness = 1.dp,
                color = BookRule
            )
            Spacer(modifier = Modifier.height(22.dp))

            chapter.paragraphsBefore.forEachIndexed { idx, para ->
                StoryParagraph(text = para)
                if (idx < chapter.paragraphsBefore.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (chapter.verses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                chapter.verses.forEach { qRef ->
                    QuranVerseEmbeddedCard(
                        qRef = qRef,
                        language = state.currentLanguage,
                        onOpenVerse = onOpenVerse,
                        onCopy = onCopyVerse
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            if (chapter.paragraphsAfter.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                chapter.paragraphsAfter.forEachIndexed { idx, para ->
                    StoryParagraph(text = para)
                    if (idx < chapter.paragraphsAfter.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
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
        fontSize = 16.sp,
        fontFamily = FontFamily.Serif,
        color = if (isDialogue) SaatColors.Slate900 else BookInk,
        lineHeight = 28.sp,
        textAlign = if (isDialogue) TextAlign.Start else TextAlign.Justify,
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
        shape = RoundedCornerShape(3.dp),
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
                    shape = RoundedCornerShape(2.dp),
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
                    shape = RoundedCornerShape(2.dp),
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
                    shape = RoundedCornerShape(2.dp),
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
    pageNumber: Int,
    contentTopInset: Dp
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                start = 20.dp,
                top = contentTopInset + 16.dp,
                end = 20.dp,
                bottom = 16.dp
            )
    ) {
        BookPageSheet(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            BookPageHeader(topic.localizedTitle(state.currentLanguage), pageNumber)

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.encyclopedia_hikmah_badge).uppercase(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = SaatColors.GoldDeep,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = hikmah.title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 27.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif,
                color = BookInk,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.encyclopedia_hikmah_intro,
                    topic.localizedTitle(state.currentLanguage)
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = SaatColors.Slate500
            )
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 36.dp),
                thickness = 1.dp,
                color = BookRule
            )
            Spacer(modifier = Modifier.height(20.dp))

            hikmah.lessons.forEachIndexed { idx, lesson ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${idx + 1}.",
                        modifier = Modifier.width(30.dp),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        color = SaatColors.GoldDeep
                    )
                    Text(
                        text = lesson,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        color = BookInk,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Justify
                    )
                }
                if (idx < hikmah.lessons.lastIndex) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            if (hikmah.conclusion.isNotBlank()) {
                Spacer(modifier = Modifier.height(22.dp))
                HorizontalDivider(thickness = 0.7.dp, color = BookRule)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.encyclopedia_closing_reflection).uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaatColors.GoldDeep,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hikmah.conclusion,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    color = BookInk,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Justify
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
                subtitle = stringResource(R.string.encyclopedia_cover_toc_description),
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
                    title = stringResource(
                        R.string.encyclopedia_toc_chapter_title,
                        chapter.index,
                        chapter.title
                    ),
                    subtitle = if (chapter.verses.isNotEmpty()) {
                        stringResource(
                            R.string.encyclopedia_chapter_quran_references,
                            chapter.verses.size
                        )
                    } else null,
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
                    subtitle = stringResource(R.string.encyclopedia_hikmah_summary),
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
