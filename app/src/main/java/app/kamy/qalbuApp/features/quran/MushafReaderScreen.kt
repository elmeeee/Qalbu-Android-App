package app.kamy.qalbuApp.features.quran

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.components.AlKhatibErrorState
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.design.theme.TajweedFontFamily
import app.kamy.qalbuApp.domain.model.MushafLine
import app.kamy.qalbuApp.domain.model.RandomAyahPayload
import app.kamy.qalbuApp.infrastructure.preferences.MushafReadingStore
import app.kamy.qalbuApp.ui.common.buildMushafLineAnnotatedString
import app.kamy.qalbuApp.ui.common.rememberErrorDisplay
import app.kamy.qalbuApp.ui.common.toVerseTranslationPlainText
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.distinctUntilChanged

private const val VIRTUAL_CENTER_INDEX = 1
private const val VIRTUAL_PAGE_COUNT = 3

private val MushafPaper = Color(0xFFFAF6EE)
private val MushafPaperEdge = Color(0xFFE8DFD0)
private val MushafDesk = Color(0xFF1A1208)
private val MushafDeskGlow = Color(0xFF2C2419)

@Composable
fun MushafReaderScreen(
    onBack: () -> Unit,
    vm: MushafReaderViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val totalPages = MushafReadingStore.totalPages
    var anchorPage by remember { mutableIntStateOf(state.currentPage) }
    var isRecentering by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        initialPage = VIRTUAL_CENTER_INDEX,
        pageCount = { VIRTUAL_PAGE_COUNT }
    )
    var pagerReady by remember { mutableStateOf(false) }

    LaunchedEffect(state.isResolvingStartPage, state.currentPage) {
        if (!state.isResolvingStartPage) {
            anchorPage = state.currentPage
        }
    }

    LaunchedEffect(state.isResolvingStartPage) {
        if (!state.isResolvingStartPage && !pagerReady) {
            pagerState.scrollToPage(VIRTUAL_CENTER_INDEX)
            pagerReady = true
        }
    }

    LaunchedEffect(pagerState, pagerReady, state.isResolvingStartPage) {
        if (!pagerReady || state.isResolvingStartPage) return@LaunchedEffect
        snapshotFlow { pagerState.isScrollInProgress to pagerState.settledPage }
            .distinctUntilChanged()
            .collect { (inProgress, settled) ->
                if (inProgress || isRecentering || settled == VIRTUAL_CENTER_INDEX) return@collect
                while (pagerState.isScrollInProgress) {
                    kotlinx.coroutines.yield()
                }
                val targetPage = when (settled) {
                    0 -> anchorPage - 1
                    2 -> anchorPage + 1
                    else -> return@collect
                }
                isRecentering = true
                try {
                    if (targetPage !in 1..totalPages) {
                        pagerState.scrollToPage(VIRTUAL_CENTER_INDEX)
                        return@collect
                    }
                    anchorPage = targetPage
                    vm.onPageChanged(targetPage)
                    pagerState.scrollToPage(VIRTUAL_CENTER_INDEX)
                } finally {
                    isRecentering = false
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MushafDesk, MushafDeskGlow, Color(0xFF0F0A06))
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            MushafReaderTopBar(
                currentPage = state.currentPage,
                totalPages = state.totalPages,
                pageInfo = state.pageInfoLabel,
                showTranslation = state.showTranslation,
                onBack = onBack,
                onToggleTranslation = vm::toggleTranslation
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                beyondViewportPageCount = 1,
                userScrollEnabled = !state.isResolvingStartPage && !isRecentering
            ) { index ->
                val pageNumber = anchorPage + (index - VIRTUAL_CENTER_INDEX)
                if (pageNumber !in 1..totalPages) {
                    MushafBlankPage(modifier = Modifier.mushafPageTurnEffect(pagerState, index))
                } else {
                    val pageState = state.pages[pageNumber] ?: MushafPageState(isLoading = true)
                    MushafBookPage(
                        pageNumber = pageNumber,
                        pageState = pageState,
                        showTranslation = state.showTranslation,
                        onRetry = { vm.retryPage(pageNumber) },
                        modifier = Modifier.mushafPageTurnEffect(pagerState, index)
                    )
                }
            }
        }

        if (state.showSwipeHint) {
            MushafSwipeHintOverlay(
                onDismiss = vm::dismissSwipeHint,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}

private fun Modifier.mushafPageTurnEffect(pagerState: PagerState, pageIndex: Int): Modifier =
    graphicsLayer {
        if (pagerState.isScrollInProgress && size.width <= 0f) return@graphicsLayer
        val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
        if (size.width <= 0f || size.height <= 0f) return@graphicsLayer
        val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)
        cameraDistance = 14f * density
        rotationY = lerp(0f, -28f, pageOffset.coerceIn(-1f, 1f))
        translationX = size.width * 0.08f * pageOffset
        alpha = lerp(1f, 0.88f, absOffset)
    }

@Composable
private fun MushafBlankPage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MushafPaper.copy(alpha = 0.35f))
    )
}

@Composable
private fun MushafReaderTopBar(
    currentPage: Int,
    totalPages: Int,
    pageInfo: String?,
    showTranslation: Boolean,
    onBack: () -> Unit,
    onToggleTranslation: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White.copy(alpha = 0.92f)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.mushaf_tajweed_label),
                style = MaterialTheme.typography.labelSmall,
                color = AlKhatibColors.GoldBright.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.mushaf_reader_title),
                style = MaterialTheme.typography.labelMedium,
                color = AlKhatibColors.GoldBright.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Text(
                text = stringResource(R.string.mushaf_page_label, currentPage, totalPages),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            pageInfo?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
        IconButton(onClick = onToggleTranslation) {
            Icon(
                Icons.Filled.Translate,
                contentDescription = stringResource(R.string.show_translation),
                tint = if (showTranslation) AlKhatibColors.GoldBright else Color.White.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun MushafBookPage(
    pageNumber: Int,
    pageState: MushafPageState,
    showTranslation: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorDisplay = pageState.error?.rememberErrorDisplay(R.string.mushaf_load_failed)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(18.dp, RoundedCornerShape(6.dp), clip = false)
                .clip(RoundedCornerShape(6.dp))
                .background(MushafPaper)
                .border(1.dp, MushafPaperEdge, RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0x22000000), Color(0x38000000))
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x18000000), Color.Transparent)
                        )
                    )
            )

            when {
                pageState.isLoading && pageState.lines.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = AlKhatibColors.DeepEmerald,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                errorDisplay != null && pageState.lines.isEmpty() -> {
                    AlKhatibErrorState(
                        display = errorDisplay,
                        onRetry = onRetry,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AlKhatibSpacing.screenHorizontal)
                    )
                }
                else -> {
                    key(pageNumber) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            HorizontalDivider(
                                color = AlKhatibColors.Gold.copy(alpha = 0.35f),
                                thickness = 1.dp
                            )
                            Spacer(Modifier.height(14.dp))

                            pageState.lines.forEach { line ->
                                MushafLineView(line = line, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(4.dp))
                            }

                            if (showTranslation && pageState.verses.isNotEmpty()) {
                                Spacer(Modifier.height(20.dp))
                                HorizontalDivider(color = MushafPaperEdge)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.mushaf_translation_header),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AlKhatibColors.DeepEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                                pageState.verses.forEach { verse ->
                                    MushafVerseTranslation(verse = verse)
                                }
                            }

                            Spacer(Modifier.height(28.dp))
                            MushafPageNumberBadge(pageNumber = pageNumber)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MushafPageNumberBadge(pageNumber: Int) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.dp, AlKhatibColors.Gold.copy(alpha = 0.45f), CircleShape)
                .background(MushafPaper, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = pageNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.GoldDeep
            )
        }
    }
}

@Composable
private fun MushafLineView(
    line: MushafLine,
    modifier: Modifier = Modifier
) {
    if (line.words.isEmpty()) return
    val markerFontSize = (28 * 0.58f).sp
    val annotated = remember(line.words) {
        runCatching {
            buildMushafLineAnnotatedString(
                words = line.words,
                baseColor = Color(0xFF1A1510),
                markerFontSize = markerFontSize,
                markerFontFamily = TajweedFontFamily
            )
        }.getOrElse { androidx.compose.ui.text.AnnotatedString("") }
    }
    if (annotated.text.isBlank()) return

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = annotated,
            modifier = modifier.padding(vertical = 3.dp),
            fontFamily = TajweedFontFamily,
            fontSize = 28.sp,
            lineHeight = (28 * 2.15f).sp,
            color = Color(0xFF1A1510),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MushafVerseTranslation(verse: RandomAyahPayload) {
    val raw = verse.translations?.firstOrNull()?.text?.trim().orEmpty()
    val translation = raw.toVerseTranslationPlainText()
    if (translation.isEmpty()) return
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = verse.displayVerseReference ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = AlKhatibColors.GoldDeep,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = translation,
            style = MaterialTheme.typography.bodyMedium,
            color = AlKhatibColors.Slate800,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun MushafSwipeHintOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "mushaf_swipe")
    val offsetX by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipe_offset"
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xE6140E08),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = AlKhatibColors.GoldBright,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer { translationX = offsetX }
                )
                Text(
                    text = stringResource(R.string.mushaf_swipe_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AlKhatibColors.GoldBright,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer { translationX = -offsetX }
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.mushaf_swipe_hint_action),
                    color = AlKhatibColors.GoldBright,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
