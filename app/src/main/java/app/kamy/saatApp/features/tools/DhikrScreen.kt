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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.features.tools.dhikr.PremiumTasbihCounter
import app.kamy.saatApp.infrastructure.preferences.DhikrPreset
import app.kamy.saatApp.infrastructure.preferences.DhikrStore
import app.kamy.saatApp.ui.feedback.rememberConfirmHaptic
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

@Composable
fun DhikrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val tapHaptic = rememberTapHaptic()
    val confirmHaptic = rememberConfirmHaptic()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { DhikrStore.presets.size }
    val selectedIndex = pagerState.currentPage
    val preset = DhikrStore.presets[selectedIndex]
    var count by remember(preset.id) { mutableIntStateOf(DhikrStore.sessionCount(context, preset.id)) }
    var pulseKey by remember { mutableIntStateOf(0) }
    val chipListState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        chipListState.animateScrollToItem(selectedIndex)
    }

    LaunchedEffect(pulseKey) {
        if (pulseKey > 0) {
            kotlinx.coroutines.delay(120)
            pulseKey = 0
        }
    }

    LaunchedEffect(count, preset.target) {
        if (count > 0 && count % preset.target == 0) confirmHaptic()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SaatColors.ScreenBackground, SaatColors.SageMist, SaatColors.PrayerMint)
                )
            )
            .tabContentStatusBarInset()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dhikr_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dhikr_premium_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500
                )
            }
        }

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
                    label = { Text(dhikrLabel(context, item)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaatColors.DeepEmerald,
                        selectedLabelColor = SaatColors.PureWhite
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val activePreset = DhikrStore.presets[pageIndex]
            var pageCount by remember(activePreset.id) {
                mutableIntStateOf(DhikrStore.sessionCount(context, activePreset.id))
            }
            if (pageIndex == selectedIndex && pageCount != count) {
                pageCount = count
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DhikrReadingCard(preset = activePreset)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val newCount = DhikrStore.increment(context, activePreset.id)
                            pageCount = newCount
                            count = newCount
                            pulseKey++
                            tapHaptic()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    PremiumTasbihCounter(
                        count = pageCount,
                        target = activePreset.target,
                        pulseKey = if (pageIndex == selectedIndex) pulseKey else 0,
                        subtitle = stringResource(R.string.dhikr_of_target, activePreset.target),
                        counterSize = 220.dp
                    )
                    Text(
                        text = stringResource(R.string.dhikr_tap_hint),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = SaatColors.Slate500.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = SaatColors.PureWhite,
            shadowElevation = 4.dp,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            DhikrStatsRow(
                count = count,
                target = preset.target,
                lifetime = DhikrStore.totalCount(context, preset.id),
                onReset = {
                    DhikrStore.resetSession(context, preset.id)
                    count = 0
                }
            )
        }
    }
}

@Composable
private fun DhikrReadingCard(preset: DhikrPreset) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 4.dp,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(SaatColors.Teal.copy(0.25f), SaatColors.Gold.copy(0.2f)))
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = preset.arabic,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 44.sp),
                color = SaatColors.DeepEmerald,
                textAlign = TextAlign.Center
            )
            Text(
                text = dhikrString(context, preset.transliterationResKey),
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Teal,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = dhikrString(context, preset.meaningResKey),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DhikrStatsRow(
    count: Int,
    target: Int,
    lifetime: Int,
    onReset: () -> Unit
) {
    val progress = if (target > 0) (count * 100 / target).coerceIn(0, 100) else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.dhikr_session_progress, progress),
                style = MaterialTheme.typography.labelMedium,
                color = SaatColors.DeepEmerald,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.dhikr_total, lifetime),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500
            )
        }
        TextButton(onClick = onReset) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.width(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.dhikr_reset))
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
