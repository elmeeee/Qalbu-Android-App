package app.kamy.qalbuApp.features.quran

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafReaderScreen(
    onBack: () -> Unit,
    vm: MushafReaderViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = (state.currentPage - 1).coerceAtLeast(0),
        pageCount = { MushafReadingStore.totalPages }
    )

    LaunchedEffect(state.currentPage, state.isResolvingStartPage) {
        if (state.isResolvingStartPage) return@LaunchedEffect
        val target = (state.currentPage - 1).coerceIn(0, MushafReadingStore.totalPages - 1)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    LaunchedEffect(pagerState, state.isResolvingStartPage) {
        if (state.isResolvingStartPage) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { pageIndex ->
                vm.onPageChanged(pageIndex + 1)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E8))
    ) {
        Column(Modifier.fillMaxSize()) {
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
            ) { index ->
                val pageNumber = index + 1
                val pageState = state.pages[pageNumber] ?: MushafPageState(isLoading = true)
                MushafPageView(
                    pageNumber = pageNumber,
                    pageState = pageState,
                    showTranslation = state.showTranslation,
                    onRetry = { vm.goToPage(pageNumber) }
                )
            }
        }

        if (state.showSwipeHint) {
            MushafSwipeHintOverlay(
                onDismiss = vm::dismissSwipeHint,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
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
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        shape = RoundedCornerShape(20.dp),
        color = AlKhatibColors.DeepEmerald.copy(alpha = 0.94f),
        shadowElevation = 8.dp
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
            .background(
                Brush.verticalGradient(
                    listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.mushaf_page_label, currentPage, totalPages),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            pageInfo?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        IconButton(onClick = onToggleTranslation) {
            Icon(
                Icons.Filled.Translate,
                contentDescription = stringResource(R.string.show_translation),
                tint = if (showTranslation) AlKhatibColors.GoldBright else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MushafPageView(
    pageNumber: Int,
    pageState: MushafPageState,
    showTranslation: Boolean,
    onRetry: () -> Unit
) {
    val errorDisplay = pageState.error?.rememberErrorDisplay(R.string.mushaf_load_failed)
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        when {
            pageState.isLoading && pageState.lines.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AlKhatibColors.DeepEmerald)
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
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    pageState.lines.forEach { line ->
                        MushafLineView(line = line, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                    }
                    if (showTranslation) {
                        Spacer(Modifier.height(16.dp))
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
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = pageNumber.toString(),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = AlKhatibColors.Slate500
                    )
                }
            }
        }
    }
}

@Composable
private fun MushafLineView(
    line: MushafLine,
    modifier: Modifier = Modifier
) {
    if (line.words.isEmpty()) return
    val markerFontSize = (26 * 0.58f).sp
    val annotated = remember(line.words) {
        buildMushafLineAnnotatedString(
            words = line.words,
            baseColor = AlKhatibColors.Slate900,
            markerFontSize = markerFontSize,
            markerFontFamily = TajweedFontFamily
        )
    }
    if (annotated.text.isBlank()) return

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = annotated,
            modifier = modifier.padding(vertical = 2.dp),
            fontFamily = TajweedFontFamily,
            fontSize = 26.sp,
            lineHeight = (26 * 2.05f).sp,
            color = AlKhatibColors.Slate900,
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
