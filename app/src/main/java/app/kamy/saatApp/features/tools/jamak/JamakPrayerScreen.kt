package app.kamy.saatApp.features.tools.jamak

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalJamakCatalog
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.design.theme.TajweedFontFamily
import app.kamy.saatApp.domain.model.JamakDalilItem
import app.kamy.saatApp.domain.model.JamakGuideData
import app.kamy.saatApp.domain.model.JamakNiatItem
import app.kamy.saatApp.domain.model.JamakRuleItem
import app.kamy.saatApp.domain.model.JamakStepItem
import app.kamy.saatApp.domain.model.JamakType
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding

private val Slate100 = Color(0xFFF1F5F9)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate400 = Color(0xFF94A3B8)
private val Slate600 = Color(0xFF475569)

private enum class JamakTab(val titleId: String, val titleMs: String, val titleEn: String) {
    DALIL("Dalil & Hadits", "Dalil & Hadis", "Hadith & Quran"),
    RULES("Syarat Safar", "Syarat Safar", "Travel Rules"),
    STEPS("Tata Cara", "Tatacara", "Step-by-Step"),
    NIAT("Lafaz Niat", "Lafaz Niat", "Intentions");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> titleEn
        AppLanguage.MALAY -> titleMs
        AppLanguage.INDONESIAN -> titleId
    }
}

@Composable
fun JamakPrayerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = remember(context) { AppLanguageStore.from(context).current() }
    val guideData = remember(context) { LocalJamakCatalog.getGuideData(context) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedJamakType by remember { mutableStateOf(JamakType.JAMAK_TAQDIM_DZUHUR_ASHAR) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
            .statusBarsPadding()
    ) {
        // Header Bar
        HeaderBar(
            title = stringResource(R.string.jamak_prayer_title),
            subtitle = stringResource(R.string.jamak_prayer_subtitle),
            onBack = onBack
        )

        // Scrollable Tab Switcher (4 Tabs)
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = SaatColors.DeepEmerald,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (selectedTab in tabPositions.indices) {
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp)
                            .padding(horizontal = 16.dp)
                            .background(SaatColors.GoldDeep, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    )
                }
            }
        ) {
            JamakTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = tab.label(currentLang),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) SaatColors.DeepEmerald else SaatColors.Slate500
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (JamakTab.entries.getOrNull(selectedTab) ?: JamakTab.DALIL) {
                JamakTab.DALIL -> DalilTabContent(dalilList = guideData.dalilList, currentLang = currentLang)
                JamakTab.RULES -> RulesTabContent(rules = guideData.rules, currentLang = currentLang)
                JamakTab.STEPS -> StepsTabContent(
                    guideData = guideData,
                    currentLang = currentLang,
                    selectedType = selectedJamakType,
                    onSelectType = { selectedJamakType = it },
                    context = context
                )
                JamakTab.NIAT -> NiatTabContent(niatList = guideData.niatList, currentLang = currentLang, context = context)
            }
        }
    }
}

@Composable
private fun HeaderBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SaatColors.DeepEmerald.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SaatColors.DeepEmerald
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = SaatColors.GoldDeep.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Musafir",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.GoldDeep,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SaatColors.Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DalilTabContent(
    dalilList: List<JamakDalilItem>,
    currentLang: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = SaatSpacing.screenHorizontal,
            end = SaatSpacing.screenHorizontal,
            top = 16.dp,
            bottom = floatingNavBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dalilList, key = { it.id }) { dalil ->
            DalilCardItem(dalil = dalil, currentLang = currentLang)
        }
    }
}

@Composable
private fun DalilCardItem(
    dalil: JamakDalilItem,
    currentLang: AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SaatColors.DeepEmerald.copy(alpha = 0.10f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dalil.title(currentLang),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SaatColors.GoldDeep.copy(alpha = 0.12f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = dalil.reference(currentLang),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.GoldDeep,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SaatColors.DeepEmerald.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = dalil.arabic,
                        fontFamily = TajweedFontFamily,
                        fontSize = 19.sp,
                        lineHeight = 34.sp,
                        color = SaatColors.DeepEmerald,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = dalil.transliteration,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = Slate600
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "“${dalil.translation(currentLang)}”",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = SaatColors.Slate800
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "💡 ${dalil.explanation(currentLang)}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                color = SaatColors.Slate700
            )
        }
    }
}

@Composable
private fun RulesTabContent(
    rules: List<JamakRuleItem>,
    currentLang: AppLanguage
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = SaatSpacing.screenHorizontal,
            end = SaatSpacing.screenHorizontal,
            top = 16.dp,
            bottom = floatingNavBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SaatColors.DeepEmerald.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = when (currentLang) {
                                AppLanguage.ENGLISH -> "Rukshah (Ease) for Travelers"
                                AppLanguage.MALAY -> "Ruhsah (Keringanan) Solat Musafir"
                                AppLanguage.INDONESIAN -> "Rukhsah (Keringanan) Shalat Musafir"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = when (currentLang) {
                                AppLanguage.ENGLISH -> "Allah loves that His concessions are taken just as He hates that His prohibitions are committed."
                                AppLanguage.MALAY -> "Allah menyukai bahawa keringanan-Nya diambil sebagaimana Dia membenci kemaksiatan."
                                AppLanguage.INDONESIAN -> "Allah menyukai jika keringanan-Nya (rukhsah) dilaksanakan sebagaimana Dia benci kemaksiatan."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontStyle = FontStyle.Italic),
                            color = SaatColors.Slate700
                        )
                    }
                }
            }
        }

        items(rules, key = { it.id }) { rule ->
            RuleCardItem(rule = rule, currentLang = currentLang)
        }
    }
}

@Composable
private fun RuleCardItem(
    rule: JamakRuleItem,
    currentLang: AppLanguage
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SaatColors.GoldDeep.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SaatColors.GoldDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.title(currentLang),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                    Text(
                        text = rule.desc(currentLang),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = SaatColors.Slate500
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Slate100)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = rule.detail(currentLang),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    color = SaatColors.Slate700
                )
                val dalilRef = rule.dalilRef(currentLang)
                if (!dalilRef.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "📖 $dalilRef",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontStyle = FontStyle.Italic),
                        color = SaatColors.GoldDeep
                    )
                }
            }
        }
    }
}

@Composable
private fun StepsTabContent(
    guideData: JamakGuideData,
    currentLang: AppLanguage,
    selectedType: JamakType,
    onSelectType: (JamakType) -> Unit,
    context: Context
) {
    val steps = remember(selectedType, guideData) { guideData.stepsMap[selectedType] ?: emptyList() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Filter Chips for Jamak Types
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = SaatSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(JamakType.entries.toTypedArray(), key = { it.name }) { type ->
                val isSelected = type == selectedType
                val typeInfo = guideData.typeInfo(type)
                val typeTitle = typeInfo?.title(currentLang) ?: type.name

                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectType(type) },
                    label = {
                        Text(
                            text = typeTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaatColors.DeepEmerald,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = SaatColors.Slate700
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) SaatColors.DeepEmerald else Slate300
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Subtitle banner for selected type
        val selectedSubtitle = guideData.typeInfo(selectedType)?.subtitle(currentLang) ?: ""
        if (selectedSubtitle.isNotBlank()) {
            Surface(
                color = SaatColors.GoldDeep.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📌 $selectedSubtitle",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = SaatColors.GoldDeep,
                    modifier = Modifier.padding(horizontal = SaatSpacing.screenHorizontal, vertical = 8.dp)
                )
            }
        }

        // Steps List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = SaatSpacing.screenHorizontal,
                end = SaatSpacing.screenHorizontal,
                top = 12.dp,
                bottom = floatingNavBottomPadding() + 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(steps, key = { it.stepNumber }) { step ->
                StepCardItem(step = step, currentLang = currentLang, context = context)
            }
        }
    }
}

@Composable
private fun StepCardItem(
    step: JamakStepItem,
    currentLang: AppLanguage,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SaatColors.DeepEmerald,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${step.stepNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = step.title(currentLang),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate900,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = step.desc(currentLang),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = SaatColors.Slate700
            )

            // Optional Arabic Niat
            val arabic = step.arabic
            if (!arabic.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SaatColors.DeepEmerald.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = arabic,
                            fontFamily = TajweedFontFamily,
                            fontSize = 20.sp,
                            lineHeight = 36.sp,
                            color = SaatColors.DeepEmerald,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val transliteration = step.transliteration
                        if (!transliteration.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = transliteration,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic
                                ),
                                color = Slate600
                            )
                        }

                        val translation = step.translation(currentLang)
                        if (!translation.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "“$translation”",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = SaatColors.Slate800
                            )
                        }
                    }
                }
            }

            // Optional Tip
            val tip = step.tip(currentLang)
            if (!tip.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaatColors.GoldDeep.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 $tip",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SaatColors.GoldDeep,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NiatTabContent(
    niatList: List<JamakNiatItem>,
    currentLang: AppLanguage,
    context: Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = SaatSpacing.screenHorizontal,
            end = SaatSpacing.screenHorizontal,
            top = 14.dp,
            bottom = floatingNavBottomPadding() + 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(niatList, key = { it.id }) { niat ->
            NiatCardItem(niat = niat, currentLang = currentLang, context = context)
        }
    }
}

@Composable
private fun NiatCardItem(
    niat: JamakNiatItem,
    currentLang: AppLanguage,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = niat.title(currentLang),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val textToCopy = "${niat.title(currentLang)}\n\n${niat.arabic}\n\n${niat.transliteration}\n\n${niat.translation(currentLang)}"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Niat Jamak", textToCopy))
                        Toast.makeText(context, "Lafaz niat berhasil disalin!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SaatColors.DeepEmerald.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = niat.arabic,
                        fontFamily = TajweedFontFamily,
                        fontSize = 20.sp,
                        lineHeight = 36.sp,
                        color = SaatColors.DeepEmerald,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = niat.transliteration,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = Slate600
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "“${niat.translation(currentLang)}”",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = SaatColors.Slate800
                    )
                }
            }

            val note = niat.note(currentLang)
            if (!note.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "📌 $note",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SaatColors.GoldDeep
                )
            }

            val hadithRef = niat.hadithRef
            if (!hadithRef.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "📖 $hadithRef",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontStyle = FontStyle.Italic),
                    color = Slate600
                )
            }
        }
    }
}
