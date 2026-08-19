package app.kamy.saatApp.features.tools.hajj

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.features.tools.hajj.components.*
import app.kamy.saatApp.features.tools.hajj.data.HajjUmrahCatalog
import app.kamy.saatApp.features.tools.hajj.model.*
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HajjUmrahScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val store = remember(context) { AppLanguageStore.from(context) }
    val appLanguage by store.currentFlow.collectAsStateWithLifecycle(initialValue = AppLanguage.INDONESIAN)
    val hajjData = remember(context) { HajjUmrahCatalog.getData(context) }

    var activeTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedHajjType by rememberSaveable { mutableStateOf(ManasikType.HAJJ_TAMATTU) }

    // Persistent checklist state using SharedPreferences
    val prefs = remember(context) { context.getSharedPreferences("hajj_checklist_prefs", Context.MODE_PRIVATE) }
    var checkedItems by remember {
        val initialSet = prefs.getStringSet("checked_items", emptySet()) ?: emptySet()
        mutableStateOf(HashSet(initialSet))
    }

    fun toggleChecklist(id: String) {
        val updated = HashSet(checkedItems)
        if (updated.contains(id)) {
            updated.remove(id)
        } else {
            updated.add(id)
        }
        checkedItems = updated
        prefs.edit().putStringSet("checked_items", updated).apply()
    }

    data class HajjTabItem(val title: String, val iconRes: Int)

    val tabs = listOf(
        HajjTabItem(stringResource(R.string.hajj_tab_umrah), R.drawable.ic_kaaba_hajj),
        HajjTabItem(stringResource(R.string.hajj_tab_hajj), R.drawable.ic_kaaba_hajj),
        HajjTabItem(stringResource(R.string.hajj_tab_doa), R.drawable.ic_dua),
        HajjTabItem(stringResource(R.string.hajj_tab_dalil), R.drawable.ic_tafsir),
        HajjTabItem(stringResource(R.string.hajj_tab_fiqh), R.drawable.ic_madhab_custom),
        HajjTabItem(stringResource(R.string.hajj_tab_miqat), R.drawable.ic_location_custom),
        HajjTabItem(stringResource(R.string.hajj_tab_checklist), R.drawable.ic_check_custom)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
    ) {
        // Sticky Header / Top Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SaatColors.Slate700
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.hajj_top_bar_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        Text(
                            text = stringResource(R.string.hajj_top_bar_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate500
                        )
                    }
                }

                // Scrollable Tabs with Vector Icons
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = SaatColors.DeepEmerald,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = SaatColors.DeepEmerald,
                            height = 3.dp
                        )
                    },
                    divider = { HorizontalDivider(color = SaatColors.SoftGrey, thickness = 1.dp) }
                ) {
                    tabs.forEachIndexed { index, item ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = null,
                                    tint = if (activeTab == index) SaatColors.DeepEmerald else SaatColors.Slate500,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (activeTab == index) SaatColors.DeepEmerald else SaatColors.Slate700
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = floatingNavBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tab 0: Umrah Step-by-Step
            if (activeTab == 0) {
                item(key = "umrah_hero") {
                    HeroUmrahBanner(appLanguage = appLanguage)
                }

                item(key = "umrah_intro") {
                    SectionIntroCard(
                        title = stringResource(R.string.hajj_umrah_intro_title),
                        subtitle = stringResource(R.string.hajj_umrah_intro_subtitle)
                    )
                }

                items(hajjData.umrahSteps, key = { it.id }) { step ->
                    ManasikStepCard(
                        step = step,
                        appLanguage = appLanguage,
                        onOpenDoa = {
                            activeTab = 2 // Jump to Doa Tab
                        }
                    )
                }
            }

            // Tab 1: Hajj Step-by-Step
            if (activeTab == 1) {
                item(key = "hajj_hero") {
                    HeroHajjBanner(appLanguage = appLanguage)
                }

                item(key = "hajj_type_selector") {
                    HajjTypeSelector(
                        selected = selectedHajjType,
                        onSelect = { selectedHajjType = it },
                        appLanguage = appLanguage
                    )
                }

                item(key = "hajj_intro") {
                    SectionIntroCard(
                        title = stringResource(R.string.hajj_intro_title),
                        subtitle = stringResource(R.string.hajj_intro_subtitle)
                    )
                }

                items(hajjData.hajjSteps, key = { it.id }) { step ->
                    ManasikStepCard(
                        step = step,
                        appLanguage = appLanguage,
                        onOpenDoa = {
                            activeTab = 2 // Jump to Doa Tab
                        }
                    )
                }
            }

            // Tab 2: Doa & Dzikir
            if (activeTab == 2) {
                items(hajjData.manasikDuas, key = { it.id }) { doa ->
                    HajjDoaCard(doa = doa, appLanguage = appLanguage)
                }
            }

            // Tab 3: Dalil Al-Qur'an & Hadits
            if (activeTab == 3) {
                item(key = "dalil_intro") {
                    SectionIntroCard(
                        title = stringResource(R.string.hajj_dalil_intro_title),
                        subtitle = stringResource(R.string.hajj_dalil_intro_subtitle)
                    )
                }

                items(hajjData.dalilList, key = { it.id }) { dalil ->
                    HajjDalilCard(dalil = dalil, appLanguage = appLanguage)
                }
            }

            // Tab 4: Fikih 4 Mazhab & Dam
            if (activeTab == 4) {
                item(key = "fiqh_intro") {
                    SectionIntroCard(
                        title = stringResource(R.string.hajj_madhhab_intro_title),
                        subtitle = stringResource(R.string.hajj_madhhab_intro_subtitle)
                    )
                }

                item(key = "fiqh_rulings_header") {
                    Text(
                        text = stringResource(R.string.hajj_madhhab_matrix_header),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                }

                items(hajjData.madhhabRulings, key = { it.id }) { ruling ->
                    MadhhabRulingCard(ruling = ruling, appLanguage = appLanguage)
                }

                item(key = "dam_header") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.hajj_dam_rules_header),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                }

                items(hajjData.damRules, key = { it.id }) { dam ->
                    DamRuleCard(dam = dam, appLanguage = appLanguage)
                }
            }

            // Tab 5: Miqat & Sights
            if (activeTab == 5) {
                item(key = "miqat_intro") {
                    SectionIntroCard(
                        title = stringResource(R.string.hajj_miqat_intro_title),
                        subtitle = stringResource(R.string.hajj_miqat_intro_subtitle)
                    )
                }

                item(key = "miqat_sub_header") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location_custom),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.hajj_miqat_boundaries_header),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.Slate900
                        )
                    }
                }

                items(hajjData.miqatLocations, key = { it.id }) { miqat ->
                    MiqatCard(miqat = miqat, appLanguage = appLanguage)
                }

                item(key = "ziarah_sub_header") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_institution_custom),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.hajj_ziarah_sites_header),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.Slate900
                        )
                    }
                }

                items(hajjData.historicSites, key = { it.id }) { site ->
                    HistoricSiteCard(site = site, appLanguage = appLanguage)
                }
            }

            // Tab 6: Checklist Jamaah
            if (activeTab == 6) {
                val totalItems = hajjData.checklistCategories.sumOf { it.items.size }
                val checkedCount = checkedItems.size
                val progress = if (totalItems > 0) checkedCount.toFloat() / totalItems.toFloat() else 0f

                item(key = "checklist_progress") {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SaatColors.SoftGrey),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.hajj_checklist_progress_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.Slate900
                                )
                                Text(
                                    text = "$checkedCount / $totalItems (${(progress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.DeepEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape),
                                color = SaatColors.DeepEmerald,
                                trackColor = SaatColors.LightGrey
                            )
                        }
                    }
                }

                hajjData.checklistCategories.forEach { category ->
                    item(key = "cat_${category.id}") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = category.title.get(appLanguage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }

                    items(category.items, key = { it.id }) { item ->
                        val isChecked = checkedItems.contains(item.id)
                        HajjChecklistTile(
                            item = item,
                            isChecked = isChecked,
                            onToggle = { toggleChecklist(item.id) },
                            appLanguage = appLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroUmrahBanner(appLanguage: AppLanguage) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SaatColors.DeepEmerald,
                            SaatColors.Teal,
                            Color(0xFF064E3B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = stringResource(R.string.hajj_hero_umrah_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Gold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.hajj_hero_umrah_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.hajj_hero_umrah_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaaba_hajj),
                    contentDescription = null,
                    tint = SaatColors.Gold,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroHajjBanner(appLanguage: AppLanguage) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SaatColors.GoldDeep,
                            Color(0xFFB45309),
                            SaatColors.DeepEmerald
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = stringResource(R.string.hajj_hero_hajj_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.hajj_hero_hajj_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.hajj_hero_hajj_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaaba_hajj),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun HajjTypeSelector(
    selected: ManasikType,
    onSelect: (ManasikType) -> Unit,
    appLanguage: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.hajj_select_type_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate700
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HajjTypeChip(
                    title = "Tamattu'",
                    isSelected = selected == ManasikType.HAJJ_TAMATTU,
                    onClick = { onSelect(ManasikType.HAJJ_TAMATTU) },
                    modifier = Modifier.weight(1f)
                )
                HajjTypeChip(
                    title = "Ifrad",
                    isSelected = selected == ManasikType.HAJJ_IFRAD,
                    onClick = { onSelect(ManasikType.HAJJ_IFRAD) },
                    modifier = Modifier.weight(1f)
                )
                HajjTypeChip(
                    title = "Qiran",
                    isSelected = selected == ManasikType.HAJJ_QIRAN,
                    onClick = { onSelect(ManasikType.HAJJ_QIRAN) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SaatColors.LightGrey,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (selected) {
                        ManasikType.HAJJ_TAMATTU -> stringResource(R.string.hajj_tamattu_desc)
                        ManasikType.HAJJ_IFRAD -> stringResource(R.string.hajj_ifrad_desc)
                        ManasikType.HAJJ_QIRAN -> stringResource(R.string.hajj_qiran_desc)
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate700,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun HajjTypeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SaatColors.DeepEmerald else SaatColors.LightGrey,
        border = BorderStroke(1.dp, if (isSelected) SaatColors.DeepEmerald else SaatColors.SoftGrey),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else SaatColors.Slate700
            )
        }
    }
}

@Composable
private fun SectionIntroCard(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SaatColors.Slate900
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = SaatColors.Slate700,
            lineHeight = 18.sp
        )
    }
}
