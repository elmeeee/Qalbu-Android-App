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
import app.kamy.saatApp.features.tools.dhikr.DhikrSessionView
import app.kamy.saatApp.features.tools.dhikr.PremiumTasbihCounter
import app.kamy.saatApp.ui.feedback.rememberConfirmHaptic
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            DhikrSessionView(
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
