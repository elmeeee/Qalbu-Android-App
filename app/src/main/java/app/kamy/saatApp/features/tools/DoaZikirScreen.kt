package app.kamy.saatApp.features.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.TajweedFontFamily
import app.kamy.saatApp.domain.model.DhikrBundle
import app.kamy.saatApp.domain.model.DhikrContentItem
import app.kamy.saatApp.domain.model.DoaCatalogKind
import app.kamy.saatApp.features.tools.dhikr.PremiumTasbihCounter
import app.kamy.saatApp.ui.feedback.rememberConfirmHaptic
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class SessionDhikrItem(
    val bundleTitle: String?,
    val arabic: String,
    val latin: String,
    val translation: String,
    val fawaid: String?,
    val notes: String?,
    val source: String?,
    val repeatCount: Int
)

@Composable
fun DoaZikirScreen(
    onBack: () -> Unit,
    viewModel: DoaZikirViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val inDetail = state.selectedSlug != null

    val selectedCategory = state.catalog.firstOrNull { it.slug == state.selectedSlug }
    val isDhikrCategory = selectedCategory?.kind == DoaCatalogKind.DHIKR
    var activeTab by remember(state.selectedSlug) { mutableStateOf("daftar") }

    if (inDetail) {
        BackHandler {
            if (isDhikrCategory && activeTab == "mulai_dzikir") {
                activeTab = "daftar"
            } else {
                viewModel.clearSelection()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        DoaZikirTopBar(
            inDetail = inDetail,
            title = if (inDetail) {
                state.selectedTitle.orEmpty()
            } else {
                stringResource(R.string.doa_zikir_title)
            },
            onBack = {
                if (inDetail) {
                    if (isDhikrCategory && activeTab == "mulai_dzikir") {
                        activeTab = "daftar"
                    } else {
                        viewModel.clearSelection()
                    }
                } else {
                    onBack()
                }
            }
        )

        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SaatColors.DeepEmerald)
            }
            return
        }

        if (inDetail && isDhikrCategory && activeTab == "mulai_dzikir") {
            ZikirSessionContainer(
                state = state,
                onClose = { viewModel.clearSelection() }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = if (inDetail && isDhikrCategory) 96.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!inDetail) {
                        items(state.catalog, key = { it.slug }) { entry ->
                            CatalogRow(
                                title = entry.title,
                                kindLabel = if (entry.kind == DoaCatalogKind.DHIKR) {
                                    stringResource(R.string.doa_zikir_kind_dhikr)
                                } else {
                                    stringResource(R.string.doa_zikir_kind_doa)
                                },
                                isDhikr = entry.kind == DoaCatalogKind.DHIKR,
                                onClick = { viewModel.selectCategory(entry.slug) }
                            )
                        }
                    } else {
                        if (state.doaItems.isNotEmpty()) {
                            items(state.doaItems) { doa ->
                                PremiumDoaCard(
                                    title = doa.title.orEmpty(),
                                    arabic = doa.arabic.orEmpty(),
                                    latin = doa.latin.orEmpty(),
                                    translation = doa.translation.orEmpty(),
                                    reference = doa.fawaid?.takeIf { it.isNotBlank() && it != "-" }
                                )
                            }
                        }
                        state.dhikrBundles.forEachIndexed { bundleIndex, bundle ->
                            val showBundleTitle = state.dhikrBundles.size > 1 ||
                                bundle.title?.equals(state.selectedTitle, ignoreCase = true) == false
                            if (showBundleTitle) {
                                item(key = "bundle-${bundle.title ?: ""}-$bundleIndex") {
                                    bundle.title?.let { title ->
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SaatColors.TealDark,
                                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                            items(bundle.content.orEmpty()) { item ->
                                PremiumDoaCard(
                                    title = "",
                                    arabic = item.arabic.orEmpty(),
                                    latin = item.latin.orEmpty(),
                                    translation = item.translation.orEmpty(),
                                    reference = item.source?.takeIf { it.isNotBlank() && it != "-" }
                                        ?: item.fawaid?.takeIf { it.isNotBlank() && it != "-" }
                                )
                            }
                        }
                    }
                }

                if (inDetail && isDhikrCategory) {
                    val tapHaptic = rememberTapHaptic()
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 24.dp, end = 24.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = SaatColors.DeepEmerald,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            SaatColors.DeepEmerald,
                                            SaatColors.Teal
                                        )
                                    )
                                )
                                .clickable {
                                    tapHaptic()
                                    activeTab = "mulai_dzikir"
                                }
                                .padding(horizontal = 22.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = SaatColors.PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Mulai Zikir",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.PureWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZikirSessionContainer(
    state: DoaZikirUiState,
    onClose: () -> Unit
) {
    val tapHaptic = rememberTapHaptic()
    val confirmHaptic = rememberConfirmHaptic()
    val scope = rememberCoroutineScope()

    val sessionItems = remember(state.dhikrBundles) {
        state.dhikrBundles.flatMap { bundle ->
            bundle.content.orEmpty().map { item ->
                SessionDhikrItem(
                    bundleTitle = bundle.title,
                    arabic = item.arabic.orEmpty(),
                    latin = item.latin.orEmpty(),
                    translation = item.translation.orEmpty(),
                    fawaid = item.fawaid,
                    notes = item.notes,
                    source = item.source,
                    repeatCount = item.repeatCount ?: 1
                )
            }
        }
    }

    if (sessionItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Zikir kosong",
                style = MaterialTheme.typography.bodyLarge,
                color = SaatColors.Slate500
            )
        }
        return
    }

    var currentItemIndex by remember(state.selectedSlug) { mutableIntStateOf(0) }
    var currentCount by remember(state.selectedSlug, currentItemIndex) { mutableIntStateOf(0) }
    var isCompleted by remember(state.selectedSlug) { mutableStateOf(false) }
    var pulseKey by remember { mutableIntStateOf(0) }

    if (isCompleted) {
        ZikirCompletionScreen(
            onReset = {
                currentItemIndex = 0
                currentCount = 0
                isCompleted = false
            },
            onClose = onClose
        )
        return
    }

    val activeItem = sessionItems[currentItemIndex]
    val progressPercent = (currentItemIndex.toFloat() / sessionItems.size.toFloat()).coerceIn(0f, 1f)

    fun incrementCount() {
        if (isCompleted || currentCount >= activeItem.repeatCount) return
        val nextCount = currentCount + 1
        currentCount = nextCount
        pulseKey++
        tapHaptic()
        if (nextCount == activeItem.repeatCount) {
            confirmHaptic()
            scope.launch {
                delay(320)
                if (currentItemIndex < sessionItems.size - 1) {
                    currentItemIndex++
                    currentCount = 0
                } else {
                    isCompleted = true
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Session progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dzikir ${currentItemIndex + 1} dari ${sessionItems.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = SaatColors.DeepEmerald,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = SaatColors.Slate500
                )
            }
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SaatColors.DeepEmerald,
                trackColor = Color(0xFFE2E8F0)
            )
            Spacer(Modifier.height(16.dp))

            // Zikir Content Card (Scrollable)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentItemIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "zikir_card_transition"
                ) { targetIndex ->
                    val item = sessionItems[targetIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .clip(RoundedCornerShape(24.dp))
                            .background(SaatColors.PureWhite)
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(SaatColors.Teal.copy(0.25f), SaatColors.Gold.copy(0.2f))
                                ),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        if (!item.bundleTitle.isNullOrBlank() && item.bundleTitle != state.selectedTitle) {
                            Text(
                                text = item.bundleTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = SaatColors.Teal,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        if (item.arabic.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SaatColors.DeepEmerald.copy(alpha = 0.05f))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = item.arabic,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        lineHeight = 44.sp,
                                        fontSize = 26.sp,
                                        fontFamily = TajweedFontFamily,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    textAlign = TextAlign.End,
                                    color = SaatColors.Slate900,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        if (item.latin.isNotBlank()) {
                            Text(
                                text = item.latin.replace("\r\n", "\n"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = SaatColors.Slate500,
                                lineHeight = 24.sp
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        if (item.translation.isNotBlank()) {
                            Text(
                                text = item.translation.replace("\r\n", "\n"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = SaatColors.Slate800,
                                lineHeight = 26.sp
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // Reference/Narrative Source Tag
                        val referenceSource = item.source?.takeIf { it.isNotBlank() && it != "-" }
                            ?: item.fawaid?.takeIf { it.isNotBlank() && it != "-" }

                        if (!referenceSource.isNullOrBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SaatColors.GoldDeep)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = referenceSource,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SaatColors.Slate700,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Extra scroll padding so content scrolls past the floating counter
                        Spacer(Modifier.height(110.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Bottom Navigation & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentItemIndex > 0) {
                            currentItemIndex--
                            currentCount = 0
                        }
                    },
                    enabled = currentItemIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Sebelumnya"
                    )
                }

                // Reset button
                IconButton(
                    onClick = { currentCount = 0 }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = SaatColors.Slate500
                    )
                }

                // Skip button
                IconButton(
                    onClick = {
                        if (currentItemIndex < sessionItems.size - 1) {
                            currentItemIndex++
                            currentCount = 0
                        } else {
                            isCompleted = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Lewati"
                    )
                }
            }
        }

        // Floating interactive circular progress zikir counter
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    incrementCount()
                }
        ) {
            PremiumTasbihCounter(
                count = currentCount,
                target = activeItem.repeatCount,
                pulseKey = pulseKey,
                subtitle = "${activeItem.repeatCount}x",
                counterSize = 88.dp
            )
        }
    }
}

@Composable
private fun ZikirCompletionScreen(
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = SaatColors.DeepEmerald.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Alhamdulillah",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SaatColors.DeepEmerald
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Anda telah menyelesaikan seluruh rangkaian zikir dengan baik. Semoga Allah menerima amal ibadah Anda.",
            style = MaterialTheme.typography.bodyLarge,
            color = SaatColors.Slate800,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
        Spacer(Modifier.height(36.dp))
        androidx.compose.material3.Button(
            onClick = onReset,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = SaatColors.DeepEmerald
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Ulangi Zikir",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SaatColors.PureWhite
            )
        }
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = onClose,
            border = BorderStroke(1.dp, SaatColors.DeepEmerald),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Kembali ke Menu",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )
        }
    }
}



@Composable
private fun DoaZikirTopBar(
    inDetail: Boolean,
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = SaatColors.DeepEmerald
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SaatColors.DeepEmerald,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CatalogRow(
    title: String,
    kindLabel: String,
    isDhikr: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(SaatColors.PureWhite, SaatColors.MintWash.copy(alpha = 0.45f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        SaatColors.Teal.copy(alpha = 0.35f),
                        SaatColors.SoftGrey.copy(alpha = 0.55f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isDhikr) SaatColors.DeepEmerald.copy(alpha = 0.12f)
                    else SaatColors.AmberWash
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDhikr) Icons.Filled.Favorite else Icons.Filled.AutoStories,
                contentDescription = null,
                tint = if (isDhikr) SaatColors.DeepEmerald else SaatColors.GoldDeep,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SaatColors.Slate800
            )
            Text(
                text = kindLabel,
                style = MaterialTheme.typography.labelSmall,
                color = SaatColors.Slate500,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SaatColors.Teal,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun PremiumDoaCard(
    title: String,
    arabic: String,
    latin: String,
    translation: String,
    reference: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SaatColors.PureWhite)
            .border(
                width = 1.dp,
                color = SaatColors.SoftGrey.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(SaatColors.DeepEmerald, SaatColors.Teal, SaatColors.Gold.copy(alpha = 0.6f))
                    )
                )
        )
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SaatColors.DeepEmerald
                )
                Spacer(Modifier.height(14.dp))
            }
            if (arabic.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SaatColors.DeepEmerald.copy(alpha = 0.05f))
                        .padding(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = arabic,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            lineHeight = 40.sp,
                            fontSize = 24.sp,
                            fontFamily = TajweedFontFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        color = SaatColors.Slate900
                    )
                }
            }
            if (latin.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = latin.replace("\r\n", "\n"),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = SaatColors.Slate500,
                    lineHeight = 24.sp
                )
            }
            if (translation.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = translation.replace("\r\n", "\n"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SaatColors.Slate800,
                    lineHeight = 26.sp
                )
            }
            reference?.let {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SaatColors.LightGrey.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SaatColors.Teal)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = SaatColors.Slate500
                    )
                }
            }
        }
    }
}
