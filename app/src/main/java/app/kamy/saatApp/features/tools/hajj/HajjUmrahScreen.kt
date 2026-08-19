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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.features.tools.hajj.components.*
import app.kamy.saatApp.features.tools.hajj.data.HajjUmrahCatalog
import app.kamy.saatApp.features.tools.hajj.model.HajjDoaItem
import app.kamy.saatApp.features.tools.hajj.model.ManasikType
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
    var searchQuery by rememberSaveable { mutableStateOf("") }
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

    val tabTitles = remember(appLanguage) {
        when (appLanguage) {
            AppLanguage.ENGLISH -> listOf(
                "🕋 Umrah",
                "⛺ Hajj",
                "🤲 Du'a & Dzikir",
                "📖 Quran & Hadith",
                "⚖️ 4-Madhhab & Dam",
                "📍 Miqat & Sights",
                "✅ Checklist"
            )
            AppLanguage.MALAY -> listOf(
                "🕋 Umrah",
                "⛺ Haji",
                "🤲 Doa & Zikir",
                "📖 Dalil & Hadis",
                "⚖️ 4 Mazhab & Dam",
                "📍 Miqat & Ziarah",
                "✅ Senarai Semak"
            )
            else -> listOf(
                "🕋 Umrah",
                "⛺ Haji",
                "🤲 Doa & Dzikir",
                "📖 Dalil & Hadits",
                "⚖️ 4 Mazhab & Dam",
                "📍 Miqat & Ziarah",
                "✅ Checklist"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
    ) {
        // Sticky Header with Title & Back Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SaatColors.PureWhite,
            shadowElevation = 3.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
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
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "Hajj & Umrah Companion"
                                AppLanguage.MALAY -> "Panduan Lengkap Haji & Umrah"
                                else -> "Panduan Haji & Umrah"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        Text(
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "Authentic Manasik, Du'as, Dalil & Fiqh"
                                AppLanguage.MALAY -> "Tatacara Sahih, Doa, Dalil & Fikih 4 Mazhab"
                                else -> "Manasik Shahih, Doa, Dalil & Fikih 4 Mazhab"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate500
                        )
                    }
                }

                // Scrollable Tabs
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
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
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
                        title = when (appLanguage) {
                            AppLanguage.ENGLISH -> "5 Core Stages of Umrah"
                            AppLanguage.MALAY -> "5 Rukun & Peringkat Ibadah Umrah"
                            else -> "5 Tahapan Inti Ibadah Umrah"
                        },
                        subtitle = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Follow these consecutive steps with pure intention from Miqat to Tahallul."
                            AppLanguage.MALAY -> "Ikuti urutan manasik secara tertib bermula dari Miqat hingga Tahallul."
                            else -> "Ikuti urutan manasik secara tertib mulai dari Miqat hingga Tahallul."
                        }
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
                        title = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Daily Timeline of Hajj (8–13 Dhul-Hijjah)"
                            AppLanguage.MALAY -> "Kronologi Hari-Hari Haji (8–13 Zulhijjah)"
                            else -> "Kronologi Harian Haji (8–13 Dzulhijjah)"
                        },
                        subtitle = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Detailed guide for Tarwiyah, Arafah, Muzdalifah, Jamarat, and Tashreeq."
                            AppLanguage.MALAY -> "Panduan lengkap hari Tarwiyah, Arafah, Muzdalifah, Jamarat, dan Tasyrik."
                            else -> "Panduan lengkap hari Tarwiyah, Arafah, Muzdalifah, Jamarat, dan Tasyriq."
                        }
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
                item(key = "doa_search") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = when (appLanguage) {
                                    AppLanguage.ENGLISH -> "Search du'a, talbiyah, or occasion..."
                                    AppLanguage.MALAY -> "Cari doa, talbiyah, atau tempat..."
                                    else -> "Cari doa manasik, talbiyah, tempat..."
                                },
                                color = SaatColors.Slate500,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = SaatColors.Slate500)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = SaatColors.Slate500)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = SaatColors.DeepEmerald,
                            unfocusedBorderColor = SaatColors.SoftGrey
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val filteredDoas: List<HajjDoaItem> = if (searchQuery.isBlank()) {
                    hajjData.manasikDuas
                } else {
                    val q = searchQuery.lowercase()
                    hajjData.manasikDuas.filter { doa ->
                        doa.title.get(appLanguage).lowercase().contains(q) ||
                                doa.latin.lowercase().contains(q) ||
                                doa.translation.get(appLanguage).lowercase().contains(q) ||
                                doa.category.get(appLanguage).lowercase().contains(q)
                    }
                }

                if (filteredDoas.isEmpty()) {
                    item(key = "doa_empty") {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔍", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = when (appLanguage) {
                                        AppLanguage.ENGLISH -> "No du'a found for '$searchQuery'"
                                        AppLanguage.MALAY -> "Tiada doa dijumpai untuk '$searchQuery'"
                                        else -> "Doa tidak ditemukan untuk kata kunci '$searchQuery'"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SaatColors.Slate700
                                )
                            }
                        }
                    }
                } else {
                    items(filteredDoas, key = { it.id }) { doa ->
                        HajjDoaCard(doa = doa, appLanguage = appLanguage)
                    }
                }
            }

            // Tab 3: Dalil Al-Qur'an & Hadits
            if (activeTab == 3) {
                item(key = "dalil_intro") {
                    SectionIntroCard(
                        title = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Foundational Qur'an & Sahih Hadith Dalil"
                            AppLanguage.MALAY -> "Dalil Al-Qur'an & Hadis Sahih"
                            else -> "Landasan Dalil Al-Qur'an & Hadits Shahih"
                        },
                        subtitle = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Primary scriptural evidence from Surah Al-Baqarah, Ali 'Imran, Al-Hajj & Sahih Muslim."
                            AppLanguage.MALAY -> "Dalil utama dari Surah Al-Baqarah, Ali 'Imran, Al-Hajj & Sahih Muslim."
                            else -> "Rujukan nash Al-Qur'an dan Hadits Shahih perawi utama (Bukhari & Muslim)."
                        }
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
                        title = when (appLanguage) {
                            AppLanguage.ENGLISH -> "4-Madhhab Fiqh & Penalty (Dam) Guide"
                            AppLanguage.MALAY -> "Fikih 4 Mazhab & Panduan Dam (Denda)"
                            else -> "Komparasi Fikih 4 Mazhab & Panduan Dam"
                        },
                        subtitle = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Comparative jurisprudence between Shafi'i, Hanafi, Maliki, Hanbali schools."
                            AppLanguage.MALAY -> "Perbandingan hukum antara Mazhab Syafi'i, Hanafi, Maliki, dan Hanbali."
                            else -> "Perbandingan pandangan hukum Mazhab Syafi'i, Hanafi, Maliki, dan Hanbali."
                        }
                    )
                }

                item(key = "fiqh_rulings_header") {
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "⚖️ Jurisprudence Matrix"
                            AppLanguage.MALAY -> "⚖️ Perbandingan Hukum 4 Mazhab"
                            else -> "⚖️ Matriks Perbandingan 4 Mazhab"
                        },
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
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "🐑 Penalty (Dam / Fidyah) Rules"
                            AppLanguage.MALAY -> "🐑 Panduan Dam & Tebusan Fidyah"
                            else -> "🐑 Rincian Dam (Denda) & Fidyah Pelanggaran"
                        },
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
                        title = when (appLanguage) {
                            AppLanguage.ENGLISH -> "5 Miqat Boundaries & Historic Ziyarah Sites"
                            AppLanguage.MALAY -> "5 Lokasi Miqat & Tempat Bersejarah"
                            else -> "Peta 5 Miqat Makani & Tempat Ziarah Bersejarah"
                        },
                        subtitle = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Geographical stations of Ihram established by the Prophet (ﷺ) and holy sights in Makkah & Madinah."
                            AppLanguage.MALAY -> "Sempadan Miqat yang ditetapkan Rasulullah SAW dan tempat ziarah utama."
                            else -> "Titik batas Miqat Makani ketetapan Rasulullah SAW serta tempat bersejarah Makkah & Madinah."
                        }
                    )
                }

                item(key = "miqat_sub_header") {
                    Text(
                        text = "📍 Miqat Makani",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                }

                items(hajjData.miqatLocations, key = { it.id }) { miqat ->
                    MiqatCard(miqat = miqat, appLanguage = appLanguage)
                }

                item(key = "ziarah_sub_header") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🕌 Tempat Ziarah Bersejarah Makkah & Madinah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
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
                                    text = when (appLanguage) {
                                        AppLanguage.ENGLISH -> "Pilgrim Preparation Progress"
                                        AppLanguage.MALAY -> "Kemajuan Persediaan Jemaah"
                                        else -> "Progres Kesiapan Jamaah"
                                    },
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
                        text = "MANASIK UMRAH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Gold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when (appLanguage) {
                        AppLanguage.ENGLISH -> "Umrah Step-by-Step"
                        AppLanguage.MALAY -> "Panduan Manasik Umrah"
                        else -> "Panduan Manasik Umrah"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (appLanguage) {
                        AppLanguage.ENGLISH -> "From Ihram at Miqat to Final Tahallul"
                        AppLanguage.MALAY -> "Daripada Ihram di Miqat hingga Tahallul"
                        else -> "Dari Niat di Miqat hingga Tahallul Sempurna"
                    },
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
                Text("🕋", fontSize = 32.sp)
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
                        text = "RUKUN ISLAM KE-5",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when (appLanguage) {
                        AppLanguage.ENGLISH -> "Hajj Pilgrimage Guide"
                        AppLanguage.MALAY -> "Panduan Manasik Haji"
                        else -> "Panduan Manasik Haji"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (appLanguage) {
                        AppLanguage.ENGLISH -> "Day-by-Day Journey (8–13 Dhul-Hijjah)"
                        AppLanguage.MALAY -> "Perjalanan Lengkap (8–13 Zulhijjah)"
                        else -> "Perjalanan Lengkap (8–13 Dzulhijjah)"
                    },
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
                Text("⛺", fontSize = 32.sp)
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
                text = when (appLanguage) {
                    AppLanguage.ENGLISH -> "Select Type of Hajj:"
                    AppLanguage.MALAY -> "Pilih Jenis Ibadah Haji:"
                    else -> "Pilihan Jenis Ibadah Haji:"
                },
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
                        ManasikType.HAJJ_TAMATTU -> when (appLanguage) {
                            AppLanguage.ENGLISH -> "Tamattu': Umrah first in Shawwal/Dhul-Qa'dah/Dhul-Hijjah, Tahallul, then enter Hajj on 8th Dhul-Hijjah (Dam Hady mandatory). Standard choice for Southeast Asian pilgrims."
                            AppLanguage.MALAY -> "Tamattu': Mengerjakan Umrah terlebih dahulu, bertahallul, kemudian memakai ihram Haji pada 8 Zulhijjah (Wajib Dam Hadyu). Pilihan utama jemaah Malaysia/Nusantara."
                            else -> "Tamattu': Mengerjakan Umrah terlebih dahulu di bulan haji, bertahallul, lalu berihram Haji pada 8 Dzulhijjah (Wajib membayar Dam Hadyu). Pilihan umum jamaah Indonesia."
                        }
                        ManasikType.HAJJ_IFRAD -> when (appLanguage) {
                            AppLanguage.ENGLISH -> "Ifrad: Performing Hajj first until complete, then performing Umrah afterwards (No Dam mandatory)."
                            AppLanguage.MALAY -> "Ifrad: Mengerjakan Haji dahulu sehingga selesai, kemudian mengerjakan Umrah selepasnya (Tidak wajib Dam)."
                            else -> "Ifrad: Mengerjakan Haji terlebih dahulu hingga selesai, baru kemudian mengerjakan Umrah (Tidak wajib bayar Dam)."
                        }
                        ManasikType.HAJJ_QIRAN -> when (appLanguage) {
                            AppLanguage.ENGLISH -> "Qiran: Combining intention of Hajj & Umrah in one single Ihram without breaking ihram between them (Dam mandatory)."
                            AppLanguage.MALAY -> "Qiran: Menggabungkan niat Haji & Umrah sekali gus dalam satu ihram berterusan (Wajib Dam)."
                            else -> "Qiran: Menggabungkan niat Haji dan Umrah sekaligus dalam satu ihram tanpa melepas ihram di antaranya (Wajib bayar Dam)."
                        }
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
