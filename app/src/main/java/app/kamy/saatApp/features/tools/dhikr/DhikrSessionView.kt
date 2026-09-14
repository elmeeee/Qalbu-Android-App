@file:Suppress("SpellCheckingInspection")

package app.kamy.saatApp.features.tools.dhikr

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import app.kamy.saatApp.R
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.TajweedFontFamily
import app.kamy.saatApp.features.tools.DoaZikirUiState
import app.kamy.saatApp.ui.feedback.rememberConfirmHaptic
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import app.kamy.saatApp.ui.feedback.rememberTasbihSoundPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

data class SessionDhikrItem(
    val bundleTitle: String?,
    val title: String? = null,
    val arabic: String,
    val latin: String,
    val translation: String,
    val fawaid: String?,
    val notes: String?,
    val source: String?,
    val repeatCount: Int
)

@Composable
fun DhikrSessionView(
    state: DoaZikirUiState,
    onClose: () -> Unit
) {
    val tapHaptic = rememberTapHaptic()
    val confirmHaptic = rememberConfirmHaptic()
    val soundPlayer = rememberTasbihSoundPlayer()
    val scope = rememberCoroutineScope()

    val sessionItems = remember(state.dhikrBundles) {
        state.dhikrBundles.flatMap { bundle ->
            bundle.content.orEmpty().map { item ->
                SessionDhikrItem(
                    bundleTitle = bundle.title,
                    title = item.title,
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
                .navigationBarsPadding()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.dhikr_session_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = SaatColors.Slate500
            )
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0) { sessionItems.size }
    val currentItemIndex = pagerState.currentPage
    val currentCountState = remember(currentItemIndex) { mutableIntStateOf(0) }
    val isCompletedState = remember(state.selectedSlug) { mutableStateOf(false) }

    var currentCount by currentCountState
    var isCompleted by isCompletedState
    var pulseKey by remember { mutableIntStateOf(0) }

    val stepperListState = rememberLazyListState()

    // Scroll stepper to active item
    LaunchedEffect(currentItemIndex) {
        stepperListState.animateScrollToItem(currentItemIndex)
    }

    if (isCompleted) {
        DhikrCompletionScreen(
            onReset = {
                scope.launch { pagerState.scrollToPage(0) }
                currentCountState.intValue = 0
                isCompletedState.value = false
            },
            onClose = onClose
        )
        return
    }

    val activeItem = sessionItems.getOrNull(currentItemIndex) ?: sessionItems.first()
    val progressPercent = ((currentItemIndex.toFloat() + (currentCount.toFloat() / activeItem.repeatCount.coerceAtLeast(1))) / sessionItems.size.toFloat()).coerceIn(0f, 1f)

    fun incrementCount() {
        if (isCompleted) return
        if (currentCount >= activeItem.repeatCount) {
            confirmHaptic()
            soundPlayer.playStop()
            scope.launch {
                if (currentItemIndex < sessionItems.size - 1) {
                    pagerState.animateScrollToPage(currentItemIndex + 1)
                } else {
                    isCompletedState.value = true
                }
            }
            return
        }
        val nextCount = currentCount + 1
        currentCountState.intValue = nextCount
        pulseKey++
        tapHaptic()
        soundPlayer.playClick()

        if (nextCount == activeItem.repeatCount) {
            confirmHaptic()
            soundPlayer.playStop()
            scope.launch {
                delay(320)
                if (currentItemIndex < sessionItems.size - 1) {
                    pagerState.animateScrollToPage(currentItemIndex + 1)
                } else {
                    isCompletedState.value = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                incrementCount()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Overall Session Progress Bar & Count Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dhikr_session_counter_format, currentItemIndex + 1, sessionItems.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = SaatColors.DeepEmerald,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dhikr_session_completed_format, (progressPercent * 100).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = SaatColors.Teal,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SaatColors.DeepEmerald,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(Modifier.height(14.dp))

            // Active Zikir Highlight Stepper Bar
            LazyRow(
                state = stepperListState,
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(sessionItems) { index, item ->
                    val isActive = index == currentItemIndex
                    val isDone = index < currentItemIndex

                    val chipBg = when {
                        isActive -> SaatColors.DeepEmerald
                        isDone -> SaatColors.SageMist
                        else -> SaatColors.PureWhite
                    }
                    val chipContentColor = when {
                        isActive -> SaatColors.PureWhite
                        isDone -> SaatColors.DeepEmerald
                        else -> SaatColors.Slate700
                    }
                    val borderStroke = when {
                        isActive -> BorderStroke(1.5.dp, SaatColors.GoldDeep)
                        isDone -> BorderStroke(1.dp, SaatColors.Teal.copy(0.3f))
                        else -> BorderStroke(1.dp, Color(0xFFE2E8F0))
                    }

                    val defaultDhikrTitle = stringResource(R.string.dhikr_title)
                    val titleLabel = remember(item, defaultDhikrTitle) {
                        val titleRaw = item.title?.trim()
                        if (!titleRaw.isNullOrBlank()) {
                            if (titleRaw.length > 25) {
                                titleRaw.take(25).trimEnd('-', ' ') + "…"
                            } else {
                                titleRaw
                            }
                        } else {
                            val latinClean = item.latin.trim()
                            if (latinClean.length > 22) {
                                latinClean.take(22).trimEnd('-', ' ') + "…"
                            } else {
                                latinClean.ifBlank { "$defaultDhikrTitle ${index + 1}" }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = chipBg,
                        border = borderStroke,
                        shadowElevation = if (isActive) 4.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SaatColors.DeepEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) SaatColors.GoldDeep else Color(0xFFCBD5E1)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isActive) SaatColors.Slate900 else SaatColors.Slate700,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                            }

                            Text(
                                text = titleLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = chipContentColor,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (item.repeatCount >= 3) {
                                Text(
                                    text = " (${item.repeatCount}x)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isActive) SaatColors.GoldBright else SaatColors.Slate500,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Main Active Zikir Card - SWIPE LEFT/RIGHT OR TAP TO INCREMENT
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val item = sessionItems[pageIndex]
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            incrementCount()
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = SaatColors.PureWhite,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top Brand Decorative Gradient Strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            SaatColors.DeepEmerald,
                                            SaatColors.Teal,
                                            SaatColors.GoldDeep,
                                            SaatColors.GoldBright
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            // Header Meta Info: Category/Title & Target Count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category / Title Badge
                                val categoryTitle = item.bundleTitle?.takeIf { it.isNotBlank() } ?: "Dzikir"
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = SaatColors.DeepEmerald.copy(alpha = 0.08f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(SaatColors.GoldDeep)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = categoryTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SaatColors.DeepEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Target Repeat Badge (only if >= 3)
                                    if (item.repeatCount >= 3) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = SaatColors.GoldDeep.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.dhikr_target_format, item.repeatCount),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SaatColors.GoldDeep,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Item Index
                                    Text(
                                        text = "${pageIndex + 1}/${sessionItems.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SaatColors.Slate500,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Arabic Display Sanctuary Box
                            if (item.arabic.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    SaatColors.DeepEmerald.copy(alpha = 0.04f),
                                                    SaatColors.MintWash
                                                )
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            SaatColors.DeepEmerald.copy(alpha = 0.10f),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 20.dp, vertical = 22.dp)
                                ) {
                                    Text(
                                        text = item.arabic,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            lineHeight = 48.sp,
                                            fontSize = 26.sp,
                                            fontFamily = TajweedFontFamily,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        textAlign = TextAlign.End,
                                        color = SaatColors.Slate900,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                            }

                            // Ornamental Divider
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    SaatColors.GoldDeep.copy(alpha = 0.35f)
                                                )
                                            )
                                        )
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SaatColors.GoldDeep)
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    SaatColors.GoldDeep.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // Latin Transliteration Box
                            if (item.latin.isNotBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SaatColors.SageMist.copy(alpha = 0.55f))
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Text(
                                        text = item.latin.replace("\r\n", "\n"),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontStyle = FontStyle.Italic
                                        ),
                                        color = SaatColors.TealDark,
                                        lineHeight = 24.sp
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                            }

                            // Translation
                            if (item.translation.isNotBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = item.translation.replace("\r\n", "\n"),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = SaatColors.Slate800,
                                        lineHeight = 26.sp,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                            }

                            // Reference / Source Tag
                            val referenceSource = item.source?.takeIf { it.isNotBlank() && it != "-" }
                                ?: item.fawaid?.takeIf { it.isNotBlank() && it != "-" }

                            if (!referenceSource.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SaatColors.PrayerCreamWarm.copy(alpha = 0.40f),
                                    border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.20f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(SaatColors.GoldDeep)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = referenceSource,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SaatColors.Slate700,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            // Bottom Clearance for floating controls
                            Spacer(Modifier.height(88.dp))
                        }
                    }
                }
            }
        }

        // Bottom Controls Container: Next/Finish Button + Tasbih Counter FAB (Positioned above system navbar)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            if (activeItem.repeatCount >= 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Next / Finish Button
                    Button(
                        onClick = {
                            if (currentItemIndex < sessionItems.size - 1) {
                                scope.launch { pagerState.animateScrollToPage(currentItemIndex + 1) }
                            } else {
                                isCompleted = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaatColors.DeepEmerald,
                            contentColor = SaatColors.PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (currentItemIndex < sessionItems.size - 1) R.string.dhikr_session_next
                                else R.string.dhikr_session_finish_title
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Tasbih Counter Floating Button
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                incrementCount()
                            },
                        shape = CircleShape,
                        color = Color.Transparent,
                        shadowElevation = 8.dp
                    ) {
                        PremiumTasbihCounter(
                            count = currentCount,
                            target = activeItem.repeatCount,
                            pulseKey = pulseKey,
                            subtitle = "${activeItem.repeatCount}x",
                            counterSize = 80.dp
                        )
                    }
                }
            } else {
                // Full width Next / Finish Button when repeat count < 3 (no tasbih counter)
                Button(
                    onClick = {
                        if (currentItemIndex < sessionItems.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(currentItemIndex + 1) }
                        } else {
                            isCompleted = true
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaatColors.DeepEmerald,
                        contentColor = SaatColors.PureWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (currentItemIndex < sessionItems.size - 1) R.string.dhikr_session_next
                            else R.string.dhikr_session_finish_title
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DhikrCompletionScreen(
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
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
            text = stringResource(R.string.dhikr_session_finish_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SaatColors.DeepEmerald
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.dhikr_session_finish_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = SaatColors.Slate800,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(Modifier.height(36.dp))

        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = SaatColors.DeepEmerald
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = stringResource(R.string.dhikr_session_repeat_button),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SaatColors.PureWhite
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onClose,
            border = BorderStroke(1.dp, SaatColors.DeepEmerald),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = stringResource(R.string.dhikr_session_menu_button),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )
        }
    }
}
