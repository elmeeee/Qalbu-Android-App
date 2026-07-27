package app.kamy.saatApp.features.tools

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.displayTransliteration
import app.kamy.saatApp.domain.model.transliterationUsesHtml
import app.kamy.saatApp.ui.common.ReaderHtmlView
import app.kamy.saatApp.ui.common.TajweedHtmlView
import app.kamy.saatApp.ui.common.TransliterationView
import app.kamy.saatApp.ui.common.looksLikeHtml
import app.kamy.saatApp.ui.common.toVerseTranslationPlainText
import app.kamy.saatApp.ui.feedback.rememberConfirmHaptic
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

private val ManzilExpandSpec = spring<IntSize>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
)

@Composable
fun ManzilScreen(
    onBack: () -> Unit,
    viewModel: ManzilViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var expandedIndex by remember { mutableIntStateOf(-1) }
    val confirmHaptic = rememberConfirmHaptic()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SaatColors.ScreenBackground,
                        SaatColors.SageMist,
                        SaatColors.PrayerMint
                    )
                )
            )
            .tabContentStatusBarInset()
    ) {
        // ─── Top bar ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = SaatColors.DeepEmerald
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.manzil_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
                Text(
                    text = stringResource(R.string.manzil_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500
                )
            }
        }

        // ─── Premium Protection Header ──────────────────────────────────
        ManzilProtectionHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = floatingNavBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "manzil_info_card") {
                ManzilInfoCard()
            }
            itemsIndexed(
                uiState.sections,
                key = { _, section -> section.def.key }
            ) { index, section ->
                val def = section.def
                ManzilSectionCard(
                    sectionNumber = index + 1,
                    title = stringResource(def.titleRes),
                    description = stringResource(def.descriptionRes),
                    verseRangeLabel = stringResource(
                        R.string.manzil_verse_range,
                        def.surah,
                        def.startAyah,
                        if (def.startAyah == def.endAyah) "" else "–${def.endAyah}"
                    ).trim(),
                    verses = section.verses,
                    loading = section.loading || uiState.loading,
                    translationId = uiState.translationId,
                    expanded = expandedIndex == index,
                    onToggle = {
                        confirmHaptic()
                        expandedIndex = if (expandedIndex == index) -1 else index
                    },
                    index = index
                )
            }
        }
    }
}

/**
 * Expandable "About Manzil Al-Quran" info card — shows the 7-day division table,
 * the Fami bi Syauqin mnemonic, purpose, and how to practice Manzil.
 */
@Composable
private fun ManzilInfoCard() {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "info_chevron"
    )

    val manzilRows = listOf(
        Triple("١ — ف (fa')", "Juz 1–6", "Al-Fatihah – An-Nisa'"),
        Triple("٢ — م (mim)", "Juz 6–11", "Al-Ma'idah – At-Taubah"),
        Triple("٣ — ي (ya')", "Juz 11–14", "Yunus – An-Nahl"),
        Triple("٤ — ب (ba')", "Juz 15–19", "Al-Isra' – Al-Furqan"),
        Triple("٥ — ش (syin)", "Juz 19–23", "Asy-Syu'ara – Yasin"),
        Triple("٦ — و (wau)", "Juz 23–26", "Ash-Shaffat – Al-Hujurat"),
        Triple("٧ — ق (qaf)", "Juz 26–30", "Qaf – An-Nas"),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (expanded) 6.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = SaatColors.IndigoAccent.copy(alpha = 0.10f),
                spotColor = SaatColors.IndigoAccent.copy(alpha = 0.14f),
                clip = false
            )
            .clip(RoundedCornerShape(18.dp))
            .background(SaatColors.PureWhite)
            .border(
                width = if (expanded) 1.5.dp else 1.dp,
                color = if (expanded) SaatColors.IndigoAccent.copy(alpha = 0.30f)
                else SaatColors.SoftGrey.copy(alpha = 0.7f),
                shape = RoundedCornerShape(18.dp)
            )
            .animateContentSize(animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ))
            .clickable { expanded = !expanded }
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (expanded)
                            Brush.linearGradient(listOf(SaatColors.IndigoAccent, SaatColors.Teal))
                        else
                            Brush.linearGradient(listOf(SaatColors.IndigoAccent.copy(0.14f), SaatColors.Teal.copy(0.08f)))
                    )
                    .border(
                        width = 1.dp,
                        color = SaatColors.IndigoAccent.copy(alpha = if (expanded) 0.6f else 0.22f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = if (expanded) SaatColors.PureWhite else SaatColors.IndigoAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.manzil_info_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SaatColors.Slate800
                )
                Text(
                    text = stringResource(R.string.manzil_info_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (expanded) SaatColors.IndigoAccent.copy(0.85f) else SaatColors.Slate500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = if (expanded) SaatColors.IndigoAccent else SaatColors.Teal,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }

        if (expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
            ) {
                HorizontalDivider(
                    color = SaatColors.IndigoAccent.copy(alpha = 0.10f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // ── Definition ──────────────────────────────────────────
                Text(
                    text = stringResource(R.string.manzil_info_definition),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = SaatColors.Slate700
                )

                Spacer(Modifier.height(16.dp))

                // ── Fami bi Syauqin badge ────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    SaatColors.IndigoAccent.copy(alpha = 0.09f),
                                    SaatColors.Teal.copy(alpha = 0.07f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            SaatColors.IndigoAccent.copy(alpha = 0.18f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "فَمِي بِشَوْقٍ",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 22.sp,
                                lineHeight = 36.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            textAlign = TextAlign.End,
                            color = SaatColors.Slate900,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.manzil_fami_label),
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = SaatColors.IndigoAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.manzil_fami_desc),
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = SaatColors.Slate500
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── 7-Manzil Table ───────────────────────────────────────
                Text(
                    text = stringResource(R.string.manzil_table_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(SaatColors.DeepEmerald)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.manzil_col_manzil),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.PureWhite,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = stringResource(R.string.manzil_col_juz),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.PureWhite,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = stringResource(R.string.manzil_col_surah),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.PureWhite,
                        modifier = Modifier.weight(2.3f)
                    )
                }

                // Table rows
                manzilRows.forEachIndexed { idx, (manzil, juz, surah) ->
                    val isEven = idx % 2 == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (idx == manzilRows.lastIndex)
                                    Modifier.clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                                else Modifier
                            )
                            .background(
                                if (isEven) SaatColors.MintWash.copy(alpha = 0.55f)
                                else SaatColors.PureWhite
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = manzil,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.DeepEmerald,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            text = juz,
                            style = MaterialTheme.typography.bodySmall,
                            color = SaatColors.Slate700,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = surah,
                            style = MaterialTheme.typography.bodySmall,
                            color = SaatColors.Slate700,
                            modifier = Modifier.weight(2.3f)
                        )
                    }
                    if (idx < manzilRows.lastIndex) {
                        HorizontalDivider(
                            color = SaatColors.SoftGrey.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── How to Practice ─────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = SaatColors.GoldDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.manzil_how_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.manzil_how_body),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = SaatColors.Slate700
                )

                Spacer(Modifier.height(12.dp))

                // ── Benefits chip row ────────────────────────────────────
                Text(
                    text = stringResource(R.string.manzil_benefits_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val benefits = stringResource(R.string.manzil_benefits_list).split("|")
                benefits.forEach { benefit ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SaatColors.GoldDeep)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = benefit.trim(),
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = SaatColors.Slate700
                        )
                    }
                }
            }
        }
    }
}

/**
 * Premium "Protection from the Quran" header card with Islamic-inspired design.
 */
@Composable
private fun ManzilProtectionHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = SaatColors.DeepEmerald.copy(alpha = 0.18f),
                spotColor = SaatColors.DeepEmerald.copy(alpha = 0.25f),
                clip = false
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to SaatColors.DeepEmerald,
                        0.55f to SaatColors.TealDark,
                        1.0f to SaatColors.EmeraldRich
                    )
                )
            )
    ) {
        // Decorative arc overlay at top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(120.dp)
                .clip(CircleShape)
                .background(SaatColors.PureWhite.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(SaatColors.PureWhite.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Shield icon in glowing circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SaatColors.PureWhite.copy(alpha = 0.15f))
                        .border(
                            width = 1.5.dp,
                            color = SaatColors.PureWhite.copy(alpha = 0.30f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = SaatColors.PureWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.manzil_header_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.PureWhite
                    )
                    Spacer(Modifier.height(2.dp))
                    // Decorative gold accent line
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(SaatColors.GoldBright.copy(alpha = 0.85f))
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            // Subtle divider
            HorizontalDivider(
                color = SaatColors.PureWhite.copy(alpha = 0.15f),
                thickness = 1.dp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.manzil_header_body),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = SaatColors.PureWhite.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ManzilSectionCard(
    sectionNumber: Int,
    title: String,
    description: String,
    verseRangeLabel: String,
    verses: List<RandomAyahPayload>,
    loading: Boolean,
    translationId: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    index: Int
) {
    val gradientPairs = listOf(
        SaatColors.DeepEmerald to SaatColors.Teal,
        SaatColors.Teal to SaatColors.DeepEmerald,
        SaatColors.GoldDeep to SaatColors.Gold,
        SaatColors.Teal to SaatColors.GoldDeep,
        SaatColors.IndigoAccent to SaatColors.Teal,
        SaatColors.DeepEmerald to SaatColors.IndigoAccent
    )
    val (accentStart, accentEnd) = gradientPairs[index % gradientPairs.size]

    // Chevron rotation animation
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "manzil_chevron"
    )

    // Chevron circle background scale/alpha animation
    val chevronBgScale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "manzil_chevron_scale"
    )
    val chevronBgAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "manzil_chevron_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (expanded) 8.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (expanded) accentStart.copy(alpha = 0.14f) else SaatColors.Slate800.copy(alpha = 0.06f),
                spotColor = if (expanded) accentStart.copy(alpha = 0.18f) else SaatColors.Slate800.copy(alpha = 0.08f),
                clip = false
            )
            .clip(RoundedCornerShape(18.dp))
            .background(SaatColors.PureWhite)
            .border(
                width = if (expanded) 1.5.dp else 1.dp,
                color = if (expanded) accentStart.copy(alpha = 0.35f)
                else SaatColors.SoftGrey.copy(alpha = 0.7f),
                shape = RoundedCornerShape(18.dp)
            )
            .animateContentSize(animationSpec = ManzilExpandSpec)
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numbered circle badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (expanded) {
                            Brush.linearGradient(listOf(accentStart, accentEnd))
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    accentStart.copy(alpha = 0.14f),
                                    accentEnd.copy(alpha = 0.08f)
                                )
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = accentStart.copy(alpha = if (expanded) 0.6f else 0.22f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sectionNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (expanded) SaatColors.PureWhite else accentStart
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SaatColors.Slate800,
                    maxLines = 2
                )
                Text(
                    text = verseRangeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (expanded) accentStart.copy(alpha = 0.85f) else SaatColors.Slate500,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Chevron with animated circular background
            Box(
                modifier = Modifier
                    .size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animated circular highlight behind the chevron
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .graphicsLayer {
                            scaleX = chevronBgScale
                            scaleY = chevronBgScale
                            alpha = chevronBgAlpha
                        }
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    accentStart.copy(alpha = 0.18f),
                                    accentEnd.copy(alpha = 0.06f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = accentStart.copy(alpha = 0.28f),
                            shape = CircleShape
                        )
                )
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = if (expanded) accentStart else SaatColors.Teal,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }

        if (expanded) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 18.dp
                )
            ) {
                HorizontalDivider(
                    color = accentStart.copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaatColors.Slate700,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(14.dp))

                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = SaatColors.DeepEmerald,
                            strokeWidth = 2.5.dp
                        )
                    }
                } else if (verses.isEmpty()) {
                    Text(
                        text = stringResource(R.string.manzil_verses_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate500
                    )
                } else {
                    verses.forEachIndexed { verseIndex, verse ->
                        if (verseIndex > 0) {
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))
                            Spacer(Modifier.height(16.dp))
                        }
                        ManzilVerseBlock(verse = verse, translationId = translationId)
                    }
                }
            }
        }
    }
}

@Composable
private fun ManzilVerseBlock(
    verse: RandomAyahPayload,
    translationId: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            SaatColors.DeepEmerald.copy(alpha = 0.07f),
                            SaatColors.Teal.copy(alpha = 0.03f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = SaatColors.DeepEmerald.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            TajweedHtmlView(
                textUthmani = verse.textUthmani,
                ayahNumber = verse.resolvedVerseNumber,
                fontSizeSp = 26,
                compact = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        verse.displayTransliteration(translationId)?.let { transliteration ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SaatColors.LightGrey.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                TransliterationView(
                    text = transliteration,
                    useHtml = verse.transliterationUsesHtml(translationId),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        verse.translations?.firstOrNull()?.text?.let { translation ->
            val normalized = translation.trim()
            if (normalized.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.manzil_meaning_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SaatColors.Teal
                )
                ManzilTranslationView(text = normalized)
            }
        }
    }
}

@Composable
private fun ManzilTranslationView(text: String) {
    if (text.looksLikeHtml()) {
        ReaderHtmlView(
            htmlBody = text,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        val plain = text.toVerseTranslationPlainText()
        if (plain.isNotEmpty()) {
            Text(
                text = plain,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = SaatColors.Slate800,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
