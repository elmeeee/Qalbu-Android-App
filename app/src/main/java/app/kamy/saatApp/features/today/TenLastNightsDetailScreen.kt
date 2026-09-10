package app.kamy.saatApp.features.today

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import app.kamy.saatApp.infrastructure.preferences.NightWorshipItem
import app.kamy.saatApp.infrastructure.repository.LailatulQadrGuideItem
import app.kamy.saatApp.infrastructure.repository.TenLastNightsDuaItem
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenLastNightsDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TenLastNightsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val performTapHaptic = rememberTapHaptic()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val onboardingStore = remember { OnboardingStore.from(context) }
    val coachMarkState = rememberCoachMarkState()

    LaunchedEffect(Unit) {
        if (!onboardingStore.hasShownTenLastNightsCoachMark()) {
            delay(600)
            coachMarkState.show()
            onboardingStore.markTenLastNightsCoachMarkShown()
        }
    }

    LaunchedEffect(coachMarkState.currentStep, coachMarkState.isVisible) {
        if (coachMarkState.isVisible) {
            when (coachMarkState.currentStep) {
                0 -> listState.animateScrollToItem(1)
                1 -> listState.animateScrollToItem(2)
                2 -> listState.animateScrollToItem(3)
                3 -> listState.animateScrollToItem(4)
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
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40 }
    }
    val topBarTitleAlpha by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / 120f).coerceIn(0f, 1f)
        }
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isScrolled
        }
    }

    var showGuideSheet by remember { mutableStateOf(false) }
    var showDuasSheet by remember { mutableStateOf(false) }

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

            // Smooth bottom fade
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
                    Spacer(modifier = Modifier.height(48.dp))
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.ten_last_nights_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.ten_last_nights_header_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp
                        ),
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Card 1: Progress 10 Malam Terakhir (Horizontal Night Selector: 21–30)
            item(key = "ten_nights_progress_card") {
                TenNightsProgressCard(
                    selectedNight = state.selectedNight,
                    onSelectNight = { night ->
                        performTapHaptic()
                        viewModel.selectNight(night)
                    },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .coachMarkTarget(
                            coachMarkState,
                            0,
                            R.string.coach_mark_ten_nights_selector_title,
                            R.string.coach_mark_ten_nights_selector_desc
                        )
                )
            }

            // Card 2 & 3: Two Side-by-Side Equal Height Cards (Malam ke-X Kimi Quote & Jadwal Malam Ini)
            item(key = "quote_and_schedule_cards") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(IntrinsicSize.Max)
                        .coachMarkTarget(
                            coachMarkState,
                            1,
                            R.string.coach_mark_ten_nights_header_title,
                            R.string.coach_mark_ten_nights_header_desc
                        ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KimiNightCard(
                        nightNumber = state.selectedNight,
                        quoteText = state.quote.quote,
                        description = state.quote.description,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    NightScheduleCard(
                        isyaTime = state.isyaTime,
                        midnightTime = state.midnightTime,
                        lastThirdTime = state.lastThirdTime,
                        subuhTime = state.subuhTime,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            // Card 4: Fokus Ibadah Malam Ini (Checklist Grid with Persistence & Toasts)
            item(key = "worship_focus_card") {
                WorshipFocusCard(
                    worshipDoneMap = state.worshipDoneMap,
                    onToggleWorship = { item, label ->
                        performTapHaptic()
                        val isDone = viewModel.toggleWorship(item)
                        val msg = if (isDone) {
                            context.getString(R.string.toast_worship_completed, label)
                        } else {
                            context.getString(R.string.toast_worship_uncompleted, label)
                        }
                        showCustomToast(msg)
                    },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .coachMarkTarget(
                            coachMarkState,
                            2,
                            R.string.coach_mark_ten_nights_worship_title,
                            R.string.coach_mark_ten_nights_worship_desc
                        )
                )
            }

            // Card 5 & 6: Action Cards (Doa Malam Ini & Tentang Lailatul Qadr)
            item(key = "action_cards") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(IntrinsicSize.Max)
                        .coachMarkTarget(
                            coachMarkState,
                            3,
                            R.string.coach_mark_ten_nights_actions_title,
                            R.string.coach_mark_ten_nights_actions_desc
                        ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionSubcard(
                        iconRes = R.drawable.ramadan_quran_goal,
                        title = stringResource(R.string.doa_malam_ini_title),
                        description = stringResource(R.string.doa_malam_ini_desc),
                        buttonLabel = stringResource(R.string.open_doa_btn),
                        onClick = {
                            performTapHaptic()
                            showDuasSheet = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    ActionSubcard(
                        iconRes = R.drawable.taraweh_icon,
                        title = stringResource(R.string.about_lailatul_qadr_title),
                        description = stringResource(R.string.about_lailatul_qadr_desc),
                        buttonLabel = stringResource(R.string.learn_btn),
                        onClick = {
                            performTapHaptic()
                            showGuideSheet = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            // Card 7: Seeking Lailatul Qadr Banner
            item(key = "seeking_banner_card") {
                SeekingLailatulQadrBanner(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
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

                // Sticky Centered Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = topBarTitleAlpha },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.ten_last_nights_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = stringResource(R.string.night_format, state.selectedNight),
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

        CoachMarkOverlay(state = coachMarkState, onDismiss = { coachMarkState.skip() })
    }

    // Detail Bottom Sheet: Lailatul Qadr Guide
    if (showGuideSheet) {
        LailatulQadrGuideBottomSheet(
            guide = state.guide,
            onDismiss = { showGuideSheet = false }
        )
    }

    // Detail Bottom Sheet: Authentic Duas
    if (showDuasSheet) {
        DuasBottomSheet(
            duas = state.duas,
            onDismiss = { showDuasSheet = false }
        )
    }
}

@Composable
private fun TenNightsProgressCard(
    selectedNight: Int,
    onSelectNight: (Int) -> Unit,
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
            // Header Row: Crescent Icon + Title + Malam ke-X Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌙", fontSize = 15.sp)
                    }
                    Text(
                        text = stringResource(R.string.progress_ten_last_nights_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F8F0),
                    border = BorderStroke(1.dp, Color(0xFFB8E0C4))
                ) {
                    Text(
                        text = stringResource(R.string.night_format, selectedNight),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = SaatColors.HomeDarkGreen,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Selector for Nights 21 to 30
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (night in 21..30) {
                    val isSelected = night == selectedNight
                    NightBadgeSelectorItem(
                        nightNumber = night,
                        isSelected = isSelected,
                        onClick = { onSelectNight(night) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NightBadgeSelectorItem(
    nightNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) SaatColors.HomeDarkGreen else Color(0xFFF7F5EE),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "nightBg"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF475569),
        label = "nightTextColor"
    )
    val animatedDotColor by animateColorAsState(
        targetValue = if (isSelected) SaatColors.HomeDarkGreen else Color(0xFFCBD5E1),
        label = "nightDotColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(animatedBg)
                .border(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = if (isSelected) Color.Transparent else Color(0xFFE5E0D4),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$nightNumber",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = animatedTextColor
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .size(if (isSelected) 5.dp else 4.dp)
                .clip(CircleShape)
                .background(animatedDotColor)
        )
    }
}

@Composable
private fun KimiNightCard(
    nightNumber: Int,
    quoteText: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val imageRes = when (nightNumber) {
        21 -> R.drawable.malam_21
        22 -> R.drawable.malam_22
        23 -> R.drawable.malam_23
        24 -> R.drawable.malam_24
        25 -> R.drawable.malam_25
        26 -> R.drawable.malam_26
        27 -> R.drawable.malam_27
        28 -> R.drawable.mascot_lentera
        29 -> R.drawable.mascot_bawa_lentera
        30 -> R.drawable.mascot_ramadan
        else -> R.drawable.mascot_prayer
    }

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
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.night_format, nightNumber),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = Color(0xFF1E293B),
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kīmi Quote Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8F6F0),
                border = BorderStroke(1.dp, Color(0xFFECE7DC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "\"$quoteText\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "— Kīmi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        ),
                        color = SaatColors.HomeDarkGreen,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun NightScheduleCard(
    isyaTime: String,
    midnightTime: String,
    lastThirdTime: String,
    subuhTime: String,
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
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_daily_time_custom),
                        contentDescription = null,
                        tint = SaatColors.HomeDarkGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.night_schedule_title),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    ),
                    color = Color(0xFF1E293B),
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NightScheduleRow(
                    icon = "🌙",
                    label = stringResource(R.string.checklist_isya),
                    time = isyaTime
                )
                NightScheduleRow(
                    icon = "🌑",
                    label = stringResource(R.string.night_time_midnight),
                    time = midnightTime
                )
                NightScheduleRow(
                    icon = "🌌",
                    label = stringResource(R.string.night_time_last_third),
                    time = lastThirdTime
                )
                NightScheduleRow(
                    icon = "🌅",
                    label = stringResource(R.string.checklist_subuh),
                    time = subuhTime
                )
            }
        }
    }
}

@Composable
private fun NightScheduleRow(
    icon: String,
    label: String,
    time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(text = icon, fontSize = 11.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF475569),
                softWrap = false
            )
        }

        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF0F172A),
            softWrap = false
        )
    }
}

@Composable
private fun WorshipFocusCard(
    worshipDoneMap: Map<NightWorshipItem, Boolean>,
    onToggleWorship: (NightWorshipItem, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Pair(NightWorshipItem.TAHAJUD, stringResource(R.string.worship_tahajud)),
        Pair(NightWorshipItem.DZIKIR, stringResource(R.string.worship_dzikir)),
        Pair(NightWorshipItem.WITIR, stringResource(R.string.worship_witir)),
        Pair(NightWorshipItem.PERBANYAK_DOA, stringResource(R.string.worship_doa)),
        Pair(NightWorshipItem.BACA_QURAN, stringResource(R.string.worship_quran)),
        Pair(NightWorshipItem.ITIKAF, stringResource(R.string.worship_itikaf))
    )

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ramadan_checklits),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.worship_focus_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = stringResource(R.string.worship_focus_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2-Column Grid (3 rows of 2 items)
            val chunked = items.chunked(2)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                chunked.forEach { rowPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPair.forEach { (item, label) ->
                            val isDone = worshipDoneMap[item] == true
                            WorshipFocusPillItem(
                                label = label,
                                isDone = isDone,
                                onClick = { onToggleWorship(item, label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorshipFocusPillItem(
    label: String,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isDone) Color(0xFFE8F8F0) else Color(0xFFF9F8F5),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pillBg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isDone) Color(0xFFB8E0C4) else Color(0xFFECE7DC),
        label = "pillBorder"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = animatedBg,
        border = BorderStroke(1.dp, animatedBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isDone) SaatColors.HomeDarkGreen else Color.Transparent)
                    .border(
                        width = if (isDone) 0.dp else 1.5.dp,
                        color = if (isDone) Color.Transparent else Color(0xFFCBD5E1),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = if (isDone) SaatColors.HomeDarkGreen else Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun ActionSubcard(
    iconRes: Int,
    title: String,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit,
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
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = Color(0xFF1E293B)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEAF6F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buttonLabel,
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
private fun SeekingLailatulQadrBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.8f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.lailatul_qadar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = 0.25f }
            )

            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = SaatColors.HomeDarkGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.seeking_lailatul_qadr_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF1E3A2F)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.seeking_lailatul_qadr_desc),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.seeking_lailatul_qadr_quote),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = SaatColors.HomeDarkGreen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LailatulQadrGuideBottomSheet(
    guide: LailatulQadrGuideItem,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = guide.title.ifEmpty { stringResource(R.string.about_lailatul_qadr_title) },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color(0xFF0F172A)
            )
            Text(
                text = guide.subtitle.ifEmpty { stringResource(R.string.about_lailatul_qadr_desc) },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(guide.sections, key = { it.id }) { section ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF9F8F5),
                        border = BorderStroke(1.dp, Color(0xFFECE7DC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = section.heading,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                ),
                                color = SaatColors.HomeDarkGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = section.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                ),
                                color = Color(0xFF334155)
                            )

                            section.quranReference?.let { qRef ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEAF6F0),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                             text = qRef.verse,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.End
                                            ),
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = qRef.translation,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontStyle = FontStyle.Italic,
                                                fontSize = 11.5.sp
                                            ),
                                            color = Color(0xFF334155)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = qRef.reference,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            ),
                                            color = SaatColors.HomeDarkGreen
                                        )
                                    }
                                }
                            }

                            section.hadithReference?.let { hRef ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFFDF7),
                                    border = BorderStroke(1.dp, Color(0xFFF0EBE1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = hRef.text,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontStyle = FontStyle.Italic,
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp
                                            ),
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = hRef.reference,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            ),
                                            color = SaatColors.HomeDarkGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuasBottomSheet(
    duas: List<TenLastNightsDuaItem>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.doa_malam_ini_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color(0xFF0F172A)
            )
            Text(
                text = stringResource(R.string.doa_malam_ini_desc),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(duas, key = { it.id }) { dua ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF9F8F5),
                        border = BorderStroke(1.dp, Color(0xFFECE7DC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = dua.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                ),
                                color = SaatColors.HomeDarkGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dua.arabic,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.End,
                                    lineHeight = 26.sp
                                ),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dua.transliteration,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = Color(0xFF0F766E)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dua.translation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dua.reference,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = SaatColors.HomeDarkGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
