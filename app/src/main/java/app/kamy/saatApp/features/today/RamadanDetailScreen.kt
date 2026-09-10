package app.kamy.saatApp.features.today

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.ui.feedback.rememberTapHaptic

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.navigationBarsPadding
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.ui.components.CoachMarkOverlay
import app.kamy.saatApp.ui.components.coachMarkTarget
import app.kamy.saatApp.ui.components.rememberCoachMarkState

@Composable
fun RamadanDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenJuz: (juzNumber: Int, verseKey: String?) -> Unit,
    onOpenTenLastNights: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: RamadanDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val performTapHaptic = rememberTapHaptic()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val onboardingStore = remember { OnboardingStore.from(context) }
    val coachMarkState = rememberCoachMarkState()

    LaunchedEffect(Unit) {
        if (!onboardingStore.hasShownRamadanDetailCoachMark()) {
            delay(600)
            coachMarkState.show()
            onboardingStore.markRamadanDetailCoachMarkShown()
        }
    }

    LaunchedEffect(coachMarkState.currentStep, coachMarkState.isVisible) {
        if (coachMarkState.isVisible) {
            when (coachMarkState.currentStep) {
                0 -> listState.animateScrollToItem(1)
                1 -> listState.animateScrollToItem(3)
                2 -> listState.animateScrollToItem(4)
                3 -> if (state.isTenLastNightsVisible) listState.animateScrollToItem(5)
            }
        }
    }

    fun showCustomToast(msg: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 90
        }
    }

    val topBarTitleAlpha by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else ((listState.firstVisibleItemScrollOffset - 80f) / 70f).coerceIn(0f, 1f)
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = isScrolled
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SaatColors.HomeBg)
    ) {
        // Parallax Header Image Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(393f / 290f)
                .graphicsLayer {
                    val offset = if (listState.firstVisibleItemIndex == 0) {
                        listState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        1000f
                    }
                    val progress = (offset / 200f).coerceIn(0f, 1f)
                    alpha = (1f - progress * 0.9f).coerceIn(0.1f, 1f)
                    translationY = -offset * 0.45f
                    scaleX = 1f + (offset * 0.0003f)
                    scaleY = 1f + (offset * 0.0003f)
                }
        ) {
            Image(
                painter = painterResource(R.drawable.ramadan_header),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Smooth bottom fade into app theme background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                SaatColors.HomeBg.copy(alpha = 0.45f),
                                SaatColors.HomeBg
                            )
                        )
                    )
            )
        }

        // Scrollable Content
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 0.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Content Area
            item(key = "header_content") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Spacer for top bar clearance
                    Spacer(modifier = Modifier.height(48.dp))

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title & Hijri Date
                    Text(
                        text = stringResource(R.string.ramadan_mubarak),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = state.hijriLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = Color.White.copy(alpha = 0.95f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.ramadan_greeting_caption),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp
                        ),
                        color = Color.White.copy(alpha = 0.90f),
                        modifier = Modifier.fillMaxWidth(0.75f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Card 1: Fasting Today
            item(key = "fasting_today_card") {
                FastingTodayCard(
                    state = state,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .coachMarkTarget(
                            coachMarkState,
                            0,
                            R.string.coach_mark_ramadan_header_title,
                            R.string.coach_mark_ramadan_header_desc
                        )
                )
            }

            // Card 2: Ramadan Progress
            item(key = "ramadan_progress_card") {
                RamadanProgressCard(
                    dayNumber = state.dayNumber,
                    totalDays = state.totalDays,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Card 3: Equal Height Side-by-Side Qur'an Goal & Taraweeh Cards
            item(key = "quran_and_taraweeh_cards") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuranGoalCard(
                        currentJuz = state.currentReadingJuz,
                        completedJuzCount = state.completedJuzCount,
                        totalJuz = state.quranTotalJuz,
                        onContinueReading = {
                            performTapHaptic()
                            onOpenJuz(state.currentReadingJuz, state.lastReadVerseKey)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    TaraweehCard(
                        isDone = state.isTarawihDone,
                        onToggle = {
                            performTapHaptic()
                            val isCompleted = viewModel.toggleTarawihDone()
                            val msg = if (isCompleted) {
                                context.getString(R.string.toast_taraweeh_completed)
                            } else {
                                context.getString(R.string.toast_taraweeh_uncompleted)
                            }
                            showCustomToast(msg)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .coachMarkTarget(
                                coachMarkState,
                                1,
                                R.string.coach_mark_ramadan_tarawih_title,
                                R.string.coach_mark_ramadan_tarawih_desc
                            )
                    )
                }
            }

            // Card 4: Today's Ramadan Checklist (Clean Today's Journey Style + Toasts)
            item(key = "ramadan_checklist_card") {
                RamadanChecklistCard(
                    checklistDoneMap = state.checklistDoneMap,
                    onToggle = { item, label ->
                        performTapHaptic()
                        val isDone = viewModel.toggleChecklistItem(item)
                        val msg = if (isDone) {
                            context.getString(R.string.toast_habit_completed, label)
                        } else {
                            context.getString(R.string.toast_habit_uncompleted, label)
                        }
                        showCustomToast(msg)
                    },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .coachMarkTarget(
                            coachMarkState,
                            2,
                            R.string.coach_mark_ramadan_checklist_title,
                            R.string.coach_mark_ramadan_checklist_desc
                        )
                )
            }

            // Card 5: 10 Malam Terakhir (Only shown during the last 10 nights or when testing toggle is enabled)
            if (state.isTenLastNightsVisible) {
                item(key = "ten_last_nights_card") {
                    TenLastNightsCard(
                        onClick = {
                            performTapHaptic()
                            onOpenTenLastNights()
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .coachMarkTarget(
                                coachMarkState,
                                3,
                                R.string.coach_mark_ramadan_ten_nights_title,
                                R.string.coach_mark_ramadan_ten_nights_desc
                            )
                    )
                }
            }
        }

        // Custom Today Journey style Snackbar / Toast
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) { snackbarData ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SaatColors.HomeDarkGreen,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, SaatColors.ArcGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .background(SaatColors.HomeDarkGreen)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE6F4EA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Notification",
                            tint = SaatColors.HomeDarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Sticky Parallax Top Bar with Smooth Transition
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = if (isScrolled) SaatColors.HomeBg.copy(alpha = 0.96f) else Color.Transparent,
            shadowElevation = if (isScrolled) 2.dp else 0.dp,
            border = if (isScrolled) BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.8f)) else null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isScrolled) Color(0xFFE2E8F0).copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.35f))
                        .clickable {
                            performTapHaptic()
                            onNavigateBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isScrolled) Color(0xFF1E293B) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Sticky Centered Title (Fades in smoothly when scrolled)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = topBarTitleAlpha },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.ramadan_mubarak),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = state.hijriLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = SaatColors.HomeDarkGreen
                        )
                    }
                }
            }
        }

        CoachMarkOverlay(state = coachMarkState, onDismiss = { coachMarkState.skip() })
    }
}

@Composable
private fun FastingTodayCard(
    state: RamadanDetailUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.8f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Icon + Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.fasting_ramadan_icon),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.fasting_today),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                }

                // Fasting status badge
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F8F0),
                    border = BorderStroke(1.dp, Color(0xFFB8E0C4))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SaatColors.HomeDarkGreen)
                        )
                        Text(
                            text = stringResource(if (state.isFastingToday) R.string.you_are_fasting else R.string.not_fasting),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = SaatColors.HomeDarkGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Prayer Timeline Progress Track (Matching TodayScreen prayer progress)
            val milestoneSlots = listOf(
                Pair(stringResource(R.string.ramadan_imsak), state.imsakTime),
                Pair(stringResource(R.string.checklist_subuh), state.subuhTime),
                Pair(stringResource(R.string.checklist_maghrib), state.maghribTime),
                Pair(stringResource(R.string.checklist_isya), state.isyaTime)
            )

            FastingTimelineTrack(
                progress = state.fastingProgressFraction,
                totalSlots = milestoneSlots.size,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Milestone Labels & Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                milestoneSlots.forEach { (label, time) ->
                    FastingMilestoneSlot(label = label, time = time, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Countdown Subcard (Full Width, Vertical Layout)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF1F8F4),
                border = BorderStroke(1.dp, Color(0xFFDDEEE3)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD6EDE0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_daily_time_custom),
                                contentDescription = null,
                                tint = Color(0xFF1B4D3E),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Text(
                            text = stringResource(if (state.isFastingCountdown) R.string.time_until_iftar else R.string.time_until_imsak),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF64748B)
                        )
                    }

                    Text(
                        text = state.countdownIftar,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Daily Ramadan Quote Subcard (Full Width, Vertical Layout)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFFDF7),
                border = BorderStroke(1.dp, Color(0xFFF0EBE1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "\"${state.quote?.quote ?: stringResource(R.string.ramadan_quote_banner)}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.5.sp,
                            lineHeight = 17.5.sp
                        ),
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.quote?.reference ?: "Al-Baqarah 2:184",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SaatColors.HomeDarkGreen
                        )

                        state.quote?.sourceType?.let { source ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE6F4EA)
                            ) {
                                Text(
                                    text = source,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.HomeDarkGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FastingTimelineTrack(
    progress: Float,
    totalSlots: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0.05f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0.05f, 1f),
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "FastingTrackPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 7.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        val width = size.width
        val centerY = size.height / 2f
        val slotWidth = width / totalSlots.toFloat()

        val startX = slotWidth / 2f
        val endX = width - (slotWidth / 2f)
        val trackTotalWidth = (endX - startX).coerceAtLeast(1f)
        val currentProgress = animatedProgress.value
        val activeX = (startX + currentProgress * trackTotalWidth).coerceIn(startX, endX)

        // 1. Inactive background track
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 2. Active green progress track
        drawLine(
            color = Color(0xFF176345),
            start = Offset(startX, centerY),
            end = Offset(activeX, centerY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 3. Dynamic moving pulsing head
        drawCircle(
            color = Color(0xFF176345).copy(alpha = pulseAlpha),
            radius = pulseRadius.dp.toPx(),
            center = Offset(activeX, centerY)
        )
        drawCircle(
            color = Color(0xFF176345),
            radius = 3.5.dp.toPx(),
            center = Offset(activeX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = 1.5.dp.toPx(),
            center = Offset(activeX, centerY)
        )

        // 4. Milestone dots at slot centers
        for (i in 0 until totalSlots) {
            val dotX = startX + (i.toFloat() / (totalSlots - 1).toFloat()) * trackTotalWidth
            val isPassed = dotX <= activeX + 2f
            val dotColor = if (isPassed) Color(0xFF176345) else Color(0xFFCBD5E1)

            drawCircle(
                color = dotColor,
                radius = 4.dp.toPx(),
                center = Offset(dotX, centerY)
            )
            if (isPassed) {
                drawCircle(
                    color = Color.White,
                    radius = 1.8.dp.toPx(),
                    center = Offset(dotX, centerY)
                )
            }
        }
    }
}

@Composable
private fun FastingMilestoneSlot(
    label: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun RamadanProgressCard(
    dayNumber: Int,
    totalDays: Int,
    modifier: Modifier = Modifier
) {
    val progressFraction = remember(dayNumber, totalDays) {
        if (totalDays > 0) (dayNumber.toFloat() / totalDays.toFloat()).coerceIn(0.03f, 1f) else 0.4f
    }
    val percentage = (progressFraction * 100).toInt()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.8f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.calendar_ramadan_icon),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.ramadan_progress_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                }

                Text(
                    text = stringResource(R.string.ramadan_progress_days_format, dayNumber, totalDays),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF34D399),
                                        Color(0xFF059669)
                                    )
                                )
                            )
                    )
                }

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun QuranGoalCard(
    currentJuz: Int,
    completedJuzCount: Int,
    totalJuz: Int,
    onContinueReading: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = (completedJuzCount.toFloat() / totalJuz.toFloat()).coerceIn(0.03f, 1f)
    val percent = (fraction * 100).toInt()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.8f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ramadan_quran_goal),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = stringResource(R.string.quran_goal_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.quran_goal_juz_completed, completedJuzCount),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    ),
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(SaatColors.HomeDarkGreen)
                        )
                    }

                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                onClick = onContinueReading,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEAF6F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.continue_reading_btn),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = SaatColors.HomeDarkGreen
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = SaatColors.HomeDarkGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaraweehCard(
    isDone: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.8f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.taraweh_icon),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = stringResource(R.string.taraweeh_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF1E293B)
                        )
                    }
                    Text(text = "🌙", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.taraweeh_desc),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    ),
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val btnBg by animateColorAsState(if (isDone) Color(0xFFDCFCE7) else Color(0xFFEAF6F0), label = "taraweehBtnBg")
            val btnTextColor by animateColorAsState(if (isDone) SaatColors.HomeDarkGreen else Color(0xFF065F46), label = "taraweehBtnTextColor")

            Surface(
                onClick = onToggle,
                shape = RoundedCornerShape(12.dp),
                color = btnBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(if (isDone) R.string.completed_btn else R.string.mark_as_done_btn),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = btnTextColor
                    )
                    Icon(
                        imageVector = if (isDone) Icons.Default.Check else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = btnTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RamadanChecklistCard(
    checklistDoneMap: Map<RamadanChecklistItem, Boolean>,
    onToggle: (RamadanChecklistItem, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Pair(RamadanChecklistItem.PUASA, stringResource(R.string.checklist_puasa)),
        Pair(RamadanChecklistItem.SUBUH, stringResource(R.string.checklist_subuh)),
        Pair(RamadanChecklistItem.DZUHUR, stringResource(R.string.checklist_dzuhur)),
        Pair(RamadanChecklistItem.ASHAR, stringResource(R.string.checklist_ashar)),
        Pair(RamadanChecklistItem.MAGHRIB, stringResource(R.string.checklist_maghrib)),
        Pair(RamadanChecklistItem.ISYA, stringResource(R.string.checklist_isya)),
        Pair(RamadanChecklistItem.TARAWEEH, stringResource(R.string.checklist_taraweeh)),
        Pair(RamadanChecklistItem.QURAN, stringResource(R.string.checklist_quran)),
        Pair(RamadanChecklistItem.DHIKR, stringResource(R.string.checklist_dhikr)),
        Pair(RamadanChecklistItem.DOA, stringResource(R.string.checklist_doa)),
        Pair(RamadanChecklistItem.SEDEKAH, stringResource(R.string.checklist_sedekah))
    )

    val completedCount = items.count { (key, _) -> checklistDoneMap[key] == true }
    val row1 = items.take(6)
    val row2 = items.drop(6)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.8f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Icon + Title + Completed Counter Pill (NO See All)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ramadan_checklits),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.ramadan_checklist_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F8F0),
                    border = BorderStroke(1.dp, Color(0xFFB8E0C4))
                ) {
                    Text(
                        text = "$completedCount/${items.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = SaatColors.HomeDarkGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Row 1 of Habit Badges (6 items: Puasa, Subuh, Dzuhur, Ashar, Maghrib, Isya')
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                row1.forEach { (item, label) ->
                    val isDone = checklistDoneMap[item] == true
                    HabitBadgeItem(
                        label = label,
                        isDone = isDone,
                        onClick = { onToggle(item, label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2 of Habit Badges (5 items: Tarawih, Qur'an, Dzikir, Doa, Sedekah)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                row2.forEach { (item, label) ->
                    val isDone = checklistDoneMap[item] == true
                    HabitBadgeItem(
                        label = label,
                        isDone = isDone,
                        onClick = { onToggle(item, label) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Spacer for symmetry with row 1 (which has 6 items)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HabitBadgeItem(
    label: String,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isDone) SaatColors.HomeDarkGreen else Color(0xFFF7F5EE),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "badgeBg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isDone) SaatColors.HomeDarkGreen else Color(0xFFE2DDD3),
        label = "badgeBorder"
    )

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(animatedBg)
                .border(1.5.dp, animatedBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isDone) SaatColors.HomeDarkGreen else Color(0xFF334155),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TenLastNightsCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF9F6F0),
        shadowElevation = 0.5.dp,
        border = BorderStroke(1.dp, Color(0xFFEAE3D5)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.icon_10_malam_terakhir),
                contentDescription = null,
                modifier = Modifier.size(52.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.ten_last_nights_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color(0xFF1E3A2F)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.ten_last_nights_desc),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = Color(0xFF6B7A70)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAE4D6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF2D3748),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

