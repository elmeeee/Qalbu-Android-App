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

    val currentItemIndexState = remember(state.selectedSlug) { mutableIntStateOf(0) }
    val currentCountState = remember(state.selectedSlug) { mutableIntStateOf(0) }
    val isCompletedState = remember(state.selectedSlug) { mutableStateOf(false) }

    var currentItemIndex by currentItemIndexState
    var currentCount by currentCountState
    var isCompleted by isCompletedState
    var pulseKey by remember { mutableIntStateOf(0) }

    val stepperListState = rememberLazyListState()

    // Scroll stepper to active item
    LaunchedEffect(currentItemIndex) {
        stepperListState.animateScrollToItem((currentItemIndex - 1).coerceAtLeast(0))
    }

    if (isCompleted) {
        DhikrCompletionScreen(
            onReset = {
                currentItemIndexState.intValue = 0
                currentCountState.intValue = 0
                isCompletedState.value = false
            },
            onClose = onClose
        )
        return
    }

    val activeItem = sessionItems[currentItemIndex]
    val progressPercent = ((currentItemIndex.toFloat() + (currentCount.toFloat() / activeItem.repeatCount.coerceAtLeast(1))) / sessionItems.size.toFloat()).coerceIn(0f, 1f)

    fun incrementCount() {
        if (isCompleted || currentCount >= activeItem.repeatCount) return
        val nextCount = currentCount + 1
        currentCountState.intValue = nextCount
        pulseKey++
        tapHaptic()

        if (nextCount == activeItem.repeatCount) {
            confirmHaptic()
            scope.launch {
                delay(320)
                if (currentItemIndex < sessionItems.size - 1) {
                    currentItemIndexState.intValue = currentItemIndex + 1
                    currentCountState.intValue = 0
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
                                currentItemIndex = index
                                currentCount = 0
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

            Spacer(Modifier.height(14.dp))

            // Main Active Zikir Card - TAP ANYWHERE ON CARD TO INCREMENT
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            },
                            onDragEnd = {
                                if (abs(totalDrag) > 60f) {
                                    if (totalDrag > 0 && currentItemIndex > 0) {
                                        currentItemIndex--
                                        currentCount = 0
                                    } else if (totalDrag < 0 && currentItemIndex < sessionItems.size - 1) {
                                        currentItemIndex++
                                        currentCount = 0
                                    }
                                }
                                totalDrag = 0f
                            },
                            onDragCancel = {
                                totalDrag = 0f
                            }
                        )
                    }
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
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(SaatColors.Teal.copy(0.3f), SaatColors.Gold.copy(0.3f))
                                ),
                                RoundedCornerShape(24.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                incrementCount()
                            }
                            .padding(20.dp)
                    ) {
                        // Header info badge inside card - target count badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SaatColors.DeepEmerald.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = stringResource(R.string.dhikr_target_format, item.repeatCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SaatColors.DeepEmerald,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Arabic Display Box
                        if (item.arabic.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(SaatColors.DeepEmerald.copy(alpha = 0.05f))
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = item.arabic,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        lineHeight = 46.sp,
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

                        // Latin Transliteration
                        if (item.latin.isNotBlank()) {
                            Text(
                                text = item.latin.replace("\r\n", "\n"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = FontStyle.Italic
                                ),
                                color = SaatColors.TealDark,
                                lineHeight = 24.sp
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        // Translation
                        if (item.translation.isNotBlank()) {
                            Text(
                                text = item.translation.replace("\r\n", "\n"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = SaatColors.Slate800,
                                lineHeight = 26.sp
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // Reference / Source Tag
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
                                        .clip(CircleShape)
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

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Non-Overlapping Integrated Bottom Controller Bar (3 Clean Navigation Buttons)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = SaatColors.PureWhite,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Item Button
                    OutlinedButton(
                        onClick = {
                            if (currentItemIndex > 0) {
                                currentItemIndex--
                                currentCount = 0
                            }
                        },
                        enabled = currentItemIndex > 0,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.dhikr_session_prev),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Reset Count Button
                    OutlinedButton(
                        onClick = { currentCount = 0 },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.dhikr_session_reset_count),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Next / Finish Button
                    Button(
                        onClick = {
                            if (currentItemIndex < sessionItems.size - 1) {
                                currentItemIndex++
                                currentCount = 0
                            } else {
                                isCompleted = true
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaatColors.DeepEmerald,
                            contentColor = SaatColors.PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (currentItemIndex < sessionItems.size - 1) R.string.dhikr_session_next
                                else R.string.dhikr_session_finish_title
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Floating Action Tasbih Counter Button (FLOATING FAB)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
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
                counterSize = 84.dp
            )
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
