package app.kamy.saatApp.features.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.features.tools.dhikr.TasbeehCounterWidget
import app.kamy.saatApp.infrastructure.preferences.DhikrPreset
import app.kamy.saatApp.infrastructure.preferences.DhikrStore
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.ui.components.CoachMarkOverlay
import app.kamy.saatApp.ui.components.coachMarkTarget
import app.kamy.saatApp.ui.components.rememberCoachMarkState
import app.kamy.saatApp.ui.feedback.rememberConfirmHaptic
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import app.kamy.saatApp.ui.feedback.rememberTasbihSoundPlayer
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val onboardingStore = remember(context) { OnboardingStore.from(context) }
    val coachMarkState = rememberCoachMarkState()
    val tapHaptic = rememberTapHaptic()
    val confirmHaptic = rememberConfirmHaptic()
    val soundPlayer = rememberTasbihSoundPlayer()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { DhikrStore.presets.size }
    val selectedIndex = pagerState.currentPage
    val preset = DhikrStore.presets[selectedIndex]
    var sessionVersion by remember { mutableIntStateOf(0) }
    val currentPresetCount = remember(preset.id, sessionVersion) { DhikrStore.sessionCount(context, preset.id) }
    var pulseKey by remember { mutableIntStateOf(0) }
    val chipListState = rememberLazyListState()

    var showResetBottomSheet by remember { mutableStateOf(false) }
    var showEndBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!onboardingStore.hasShownDhikrCoachMark()) {
            kotlinx.coroutines.delay(500)
            onboardingStore.markDhikrCoachMarkShown()
            coachMarkState.show()
        }
    }

    LaunchedEffect(selectedIndex) {
        chipListState.animateScrollToItem(selectedIndex)
    }

    LaunchedEffect(pulseKey) {
        if (pulseKey > 0) {
            kotlinx.coroutines.delay(120)
            pulseKey = 0
        }
    }

    LaunchedEffect(currentPresetCount, preset.target) {
        if (currentPresetCount > 0 && currentPresetCount % preset.target == 0) {
            confirmHaptic()
            soundPlayer.playStop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SaatColors.HomeBg)
                .navigationBarsPadding()
        ) {
            // Unified Top Bar
            app.kamy.saatApp.features.tools.components.SpiritualToolTopBar(
                title = stringResource(R.string.dhikr_title),
                subtitle = stringResource(R.string.dhikr_premium_subtitle),
                onBack = onBack
            )

            Spacer(Modifier.height(8.dp))

            // Top Category Chips for Presets
            LazyRow(
                state = chipListState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(DhikrStore.presets) { index, item ->
                    FilterChip(
                        selected = index == selectedIndex,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        label = { Text(dhikrLabel(context, item), maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaatColors.DeepEmerald,
                            selectedLabelColor = SaatColors.PureWhite
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Main Content Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val activePreset = DhikrStore.presets[pageIndex]
                val pageCount = remember(activePreset.id, sessionVersion) {
                    DhikrStore.sessionCount(context, activePreset.id)
                }

                val leftCount = (activePreset.target - pageCount).coerceAtLeast(0)
                val progress = (pageCount.toFloat() / activePreset.target.coerceAtLeast(1)).coerceIn(0f, 1f)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 1. Arabic
                    Text(
                        text = activePreset.arabic,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp, lineHeight = 46.sp),
                        color = SaatColors.DeepEmerald,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    // 2. Transliteration & Meaning
                    Text(
                        text = dhikrString(context, activePreset.transliterationResKey),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SaatColors.Teal,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = dhikrString(context, activePreset.meaningResKey),
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate500,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(14.dp))

                    // 3. Target & Page Nav
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (pageIndex > 0) {
                            IconButton(
                                onClick = { scope.launch { pagerState.animateScrollToPage(pageIndex - 1) } },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous",
                                    tint = SaatColors.Slate500,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                        }

                        Text(
                            text = stringResource(R.string.tasbih_target_left, activePreset.target, leftCount),
                            style = MaterialTheme.typography.labelMedium,
                            color = SaatColors.Slate500,
                            textAlign = TextAlign.Center
                        )

                        if (pageIndex < DhikrStore.presets.size - 1) {
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                onClick = { scope.launch { pagerState.animateScrollToPage(pageIndex + 1) } },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next",
                                    tint = SaatColors.Slate500,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // 4. Progress
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SaatColors.Teal,
                        trackColor = SaatColors.SageTint
                    )

                    Spacer(Modifier.height(16.dp))

                    // 5. Counter (Tasbeeh Counter Device Widget)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .coachMarkTarget(
                                coachMarkState,
                                0,
                                R.string.coach_mark_dhikr_title,
                                R.string.coach_mark_dhikr_desc
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        TasbeehCounterWidget(
                            count = pageCount,
                            pulseKey = if (pageIndex == selectedIndex) pulseKey else 0,
                            counterWidth = 340.dp,
                            counterHeight = 360.dp,
                            onTap = {
                                if (pageCount >= activePreset.target) {
                                    confirmHaptic()
                                    soundPlayer.playStop()
                                    if (pageIndex < DhikrStore.presets.size - 1) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pageIndex + 1)
                                        }
                                    } else {
                                        showEndBottomSheet = true
                                    }
                                } else {
                                    val newCount = DhikrStore.increment(context, activePreset.id)
                                    sessionVersion++
                                    pulseKey++
                                    tapHaptic()
                                    soundPlayer.playClick()

                                    if (newCount >= activePreset.target) {
                                        confirmHaptic()
                                        soundPlayer.playStop()
                                        scope.launch {
                                            kotlinx.coroutines.delay(300)
                                            if (pageIndex < DhikrStore.presets.size - 1) {
                                                pagerState.animateScrollToPage(pageIndex + 1)
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons (Reset & Finish)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .clickable { showResetBottomSheet = true },
                        shape = RoundedCornerShape(26.dp),
                        color = SaatColors.PureWhite,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.tasbih_reset_button),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .clickable { showEndBottomSheet = true },
                        shape = RoundedCornerShape(26.dp),
                        color = SaatColors.PureWhite,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.tasbih_end_button),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }
                    }
                }
            }
        }

        CoachMarkOverlay(state = coachMarkState, onDismiss = { coachMarkState.skip() })

        // Bottom Sheets for Reset and End
        if (showResetBottomSheet) {
            TasbihResetBottomSheet(
                count = currentPresetCount,
                onDismiss = { showResetBottomSheet = false },
                onConfirmReset = {
                    DhikrStore.resetSession(context, preset.id)
                    sessionVersion++
                    showResetBottomSheet = false
                }
            )
        }

        if (showEndBottomSheet) {
            TasbihEndBottomSheet(
                count = currentPresetCount,
                onDismiss = { showEndBottomSheet = false },
                onConfirmEnd = {
                    DhikrStore.resetSession(context, preset.id)
                    sessionVersion++
                    showEndBottomSheet = false
                    onBack()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbihResetBottomSheet(
    count: Int,
    onDismiss: () -> Unit,
    onConfirmReset: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SaatColors.PureWhite,
        scrimColor = Color.Black.copy(alpha = 0.4f),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.tasbih_reset_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFAF7F2),
                border = BorderStroke(1.dp, Color(0xFFF3EFE6))
            ) {
                Text(
                    text = stringResource(R.string.tasbih_reset_dialog_desc, count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaatColors.Slate700,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onConfirmReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaatColors.DeepEmerald,
                        contentColor = SaatColors.PureWhite
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tasbih_reset_dialog_confirm),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SaatColors.Slate700
                    )
                ) {
                    Text(
                        text = stringResource(R.string.tasbih_reset_dialog_cancel),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbihEndBottomSheet(
    count: Int,
    onDismiss: () -> Unit,
    onConfirmEnd: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SaatColors.PureWhite,
        scrimColor = Color.Black.copy(alpha = 0.4f),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.tasbih_end_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFAF7F2),
                border = BorderStroke(1.dp, Color(0xFFF3EFE6))
            ) {
                Text(
                    text = stringResource(R.string.tasbih_end_dialog_desc, count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaatColors.Slate700,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onConfirmEnd,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaatColors.DeepEmerald,
                        contentColor = SaatColors.PureWhite
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tasbih_end_dialog_confirm),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SaatColors.Slate700
                    )
                ) {
                    Text(
                        text = stringResource(R.string.tasbih_end_dialog_cancel),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun dhikrLabel(context: android.content.Context, preset: DhikrPreset): String =
    dhikrString(context, preset.labelResKey)

private fun dhikrString(context: android.content.Context, key: String): String =
    when (key) {
        "dhikr_subhanallah" -> context.getString(R.string.dhikr_subhanallah)
        "dhikr_alhamdulillah" -> context.getString(R.string.dhikr_alhamdulillah)
        "dhikr_allahuakbar" -> context.getString(R.string.dhikr_allahuakbar)
        "dhikr_istighfar" -> context.getString(R.string.dhikr_istighfar)
        "dhikr_salawat" -> context.getString(R.string.dhikr_salawat)
        "dhikr_translit_subhanallah" -> context.getString(R.string.dhikr_translit_subhanallah)
        "dhikr_translit_alhamdulillah" -> context.getString(R.string.dhikr_translit_alhamdulillah)
        "dhikr_translit_allahuakbar" -> context.getString(R.string.dhikr_translit_allahuakbar)
        "dhikr_translit_istighfar" -> context.getString(R.string.dhikr_translit_istighfar)
        "dhikr_translit_salawat" -> context.getString(R.string.dhikr_translit_salawat)
        "dhikr_meaning_subhanallah" -> context.getString(R.string.dhikr_meaning_subhanallah)
        "dhikr_meaning_alhamdulillah" -> context.getString(R.string.dhikr_meaning_alhamdulillah)
        "dhikr_meaning_allahuakbar" -> context.getString(R.string.dhikr_meaning_allahuakbar)
        "dhikr_meaning_istighfar" -> context.getString(R.string.dhikr_meaning_istighfar)
        "dhikr_meaning_salawat" -> context.getString(R.string.dhikr_meaning_salawat)
        else -> key
    }
