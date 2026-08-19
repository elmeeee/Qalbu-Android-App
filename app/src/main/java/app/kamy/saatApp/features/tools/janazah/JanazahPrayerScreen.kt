package app.kamy.saatApp.features.tools.janazah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalJanazahGuideCatalog
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.TajweedFontFamily
import app.kamy.saatApp.domain.model.JanazahDuaItem
import app.kamy.saatApp.domain.model.JanazahGuide
import app.kamy.saatApp.domain.model.JanazahNiatItem
import app.kamy.saatApp.domain.model.JanazahPositionGuide
import app.kamy.saatApp.domain.model.JanazahTakbirStep
import app.kamy.saatApp.features.quran.tajweed.TajweedEngine
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun JanazahPrayerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val store = remember(context) { AppLanguageStore.from(context) }
    val appLanguage by store.currentFlow.collectAsStateWithLifecycle(initialValue = AppLanguage.INDONESIAN)
    val guide = remember(context) { LocalJanazahGuideCatalog.getGuide(context) }
    var activeTab by remember { mutableIntStateOf(0) }

    val tabTitles = remember(appLanguage) {
        when (appLanguage) {
            AppLanguage.ENGLISH -> listOf("4 Takbirs Step-by-Step", "Niyyah (Intentions)", "Imam & Body Position", "Rulings & Rewards", "Duas After Prayer")
            AppLanguage.MALAY -> listOf("Tatacara 4 Takbir", "Pilihan Niat", "Kedudukan Imam & Jenazah", "Syarat & Keutamaan", "Doa Selepas Solat")
            else -> listOf("Tata Cara 4 Takbir", "Pilihan Niat", "Posisi Imam & Jenazah", "Syarat & Keutamaan", "Doa Ba'da Shalat")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
    ) {
        // Sticky Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SaatColors.PureWhite,
            shadowElevation = 2.dp
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
                            text = stringResource(R.string.jenazah_prayer_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        Text(
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "Fard Kifayah · 4 Takbirs without Ruku & Sujud"
                                AppLanguage.MALAY -> "Fardu Kifayah · 4 Takbir Tanpa Rukuk & Sujud"
                                else -> "Fardhu Kifayah · 4 Takbir Tanpa Ruku' & Sujud"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.GoldDeep,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Scrollable Sub-tabs
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = SaatColors.PureWhite,
                    contentColor = SaatColors.DeepEmerald,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (activeTab < tabPositions.size) {
                            Surface(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[activeTab])
                                    .height(3.dp),
                                color = SaatColors.DeepEmerald,
                                shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                            ) {}
                        }
                    },
                    divider = {
                        HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.5.sp,
                                    color = if (activeTab == index) SaatColors.DeepEmerald else SaatColors.Slate500
                                )
                            }
                        )
                    }
                }
            }
        }

        // Tab Content
        when (activeTab) {
            0 -> JanazahTakbirTab(guide = guide, appLanguage = appLanguage)
            1 -> JanazahNiatTab(niats = guide.niatList, appLanguage = appLanguage)
            2 -> JanazahPositionTab(positions = guide.positionGuides, appLanguage = appLanguage)
            3 -> JanazahRulingsTab(guide = guide, appLanguage = appLanguage)
            4 -> JanazahAfterDuasTab(duas = guide.afterDuas, appLanguage = appLanguage)
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 0: TATA CARA 4 TAKBIR (STEP-BY-STEP)
// -----------------------------------------------------------------------------------------

@Composable
private fun JanazahTakbirTab(guide: JanazahGuide, appLanguage: AppLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = floatingNavBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Essential Header Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SaatColors.PureWhite,
                shadowElevation = 1.5.dp,
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "How Janazah Prayer Works"
                                AppLanguage.MALAY -> "Prinsip Utama Solat Jenazah"
                                else -> "Prinsip Utama Shalat Jenazah"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }
                    Text(
                        text = guide.principleDesc(appLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate700,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Steps
        items(guide.takbirSteps, key = { it.takbirNumber }) { step ->
            JanazahStepCard(step = step, appLanguage = appLanguage)
        }
    }
}

@Composable
private fun JanazahStepCard(step: JanazahTakbirStep, appLanguage: AppLanguage) {
    val stepTitle = step.title(appLanguage)
    val stepDesc = step.desc(appLanguage)
    val stepTrans = step.translation(appLanguage)
    val stepNote = step.importantNotes(appLanguage)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(app.kamy.saatApp.R.drawable.frame_number_icon),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "${step.takbirNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SaatColors.Slate900
                        )
                    }
                    Text(
                        text = "Takbir ${step.takbirNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = SaatColors.GoldDeep.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = when (step.takbirNumber) {
                            1 -> "Surah Al-Fatihah"
                            2 -> when (appLanguage) {
                                AppLanguage.ENGLISH -> "Salawat upon Prophet"
                                AppLanguage.MALAY -> "Selawat Nabi"
                                else -> "Shalawat Nabi"
                            }
                            3 -> when (appLanguage) {
                                AppLanguage.ENGLISH -> "Main Supplication"
                                AppLanguage.MALAY -> "Doa Utama Jenazah"
                                else -> "Doa Utama Jenazah"
                            }
                            else -> when (appLanguage) {
                                AppLanguage.ENGLISH -> "Closing Dua & Salam"
                                AppLanguage.MALAY -> "Doa Penutup & Salam"
                                else -> "Doa Penutup & Salam"
                            }
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.GoldDeep
                    )
                }
            }

            Text(
                text = stepTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stepDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate700,
                lineHeight = 20.sp
            )

            // Arabic text with Tajweed styling
            if (step.arabic.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SaatColors.ScreenBackground.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val annotatedArabic = remember(step.arabic) { TajweedEngine.applyTajweed(step.arabic) }
                        Text(
                            text = annotatedArabic,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 22.sp,
                                lineHeight = 38.sp,
                                fontFamily = TajweedFontFamily,
                                fontWeight = FontWeight.Normal
                            ),
                            color = SaatColors.Slate900,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = step.latin,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                lineHeight = 20.sp
                            ),
                            color = SaatColors.DeepEmerald
                        )

                        HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

                        Text(
                            text = stepTrans,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SaatColors.Slate800,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Note if any
            stepNote?.let { note ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SaatColors.GoldDeep.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "💡 ",
                            fontSize = 14.sp
                        )
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate800,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 1: PILIHAN NIAT LENGKAP
// -----------------------------------------------------------------------------------------

@Composable
private fun JanazahNiatTab(niats: List<JanazahNiatItem>, appLanguage: AppLanguage) {
    val categories = remember(appLanguage) {
        listOf(
            "ALL" to when (appLanguage) {
                AppLanguage.ENGLISH -> "All Intentions"
                AppLanguage.MALAY -> "Semua Niat"
                else -> "Semua Niat"
            },
            "LAKI" to when (appLanguage) {
                AppLanguage.ENGLISH -> "Male Deceased"
                AppLanguage.MALAY -> "Jenazah Lelaki"
                else -> "Jenazah Laki-laki"
            },
            "PEREMPUAN" to when (appLanguage) {
                AppLanguage.ENGLISH -> "Female Deceased"
                AppLanguage.MALAY -> "Jenazah Perempuan"
                else -> "Jenazah Perempuan"
            },
            "ANAK" to when (appLanguage) {
                AppLanguage.ENGLISH -> "Child Deceased"
                AppLanguage.MALAY -> "Jenazah Kanak-kanak"
                else -> "Jenazah Anak-anak"
            },
            "GHAIB" to when (appLanguage) {
                AppLanguage.ENGLISH -> "Absent (Ghaib)"
                AppLanguage.MALAY -> "Solat Ghaib"
                else -> "Shalat Ghaib"
            }
        )
    }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filtered = remember(selectedCategory, niats) {
        if (selectedCategory == "ALL") niats
        else niats.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            items(categories) { (catKey, catLabel) ->
                FilterChip(
                    selected = selectedCategory == catKey,
                    onClick = { selectedCategory = catKey },
                    label = { Text(catLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaatColors.DeepEmerald,
                        selectedLabelColor = SaatColors.PureWhite
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = floatingNavBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                JanazahNiatCard(item = item, appLanguage = appLanguage)
            }
        }
    }
}

@Composable
private fun JanazahNiatCard(item: JanazahNiatItem, appLanguage: AppLanguage) {
    val title = item.title(appLanguage)
    val subtitle = item.subtitle(appLanguage)
    val trans = item.translation(appLanguage)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = SaatColors.DeepEmerald.copy(alpha = 0.1f)
                ) {
                    val categoryLabel = when (item.category) {
                        "LAKI" -> when (appLanguage) {
                            AppLanguage.ENGLISH -> "MALE"
                            AppLanguage.MALAY -> "LELAKI"
                            else -> "LAKI-LAKI"
                        }
                        "PEREMPUAN" -> when (appLanguage) {
                            AppLanguage.ENGLISH -> "FEMALE"
                            AppLanguage.MALAY -> "PEREMPUAN"
                            else -> "PEREMPUAN"
                        }
                        "ANAK" -> when (appLanguage) {
                            AppLanguage.ENGLISH -> "CHILD"
                            AppLanguage.MALAY -> "KANAK-KANAK"
                            else -> "ANAK-ANAK"
                        }
                        "GHAIB" -> when (appLanguage) {
                            AppLanguage.ENGLISH -> "ABSENT"
                            AppLanguage.MALAY -> "GHAIB"
                            else -> "GHAIB"
                        }
                        else -> item.category
                    }
                    Text(
                        text = categoryLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500
            )

            val annotatedArabic = remember(item.arabic) { TajweedEngine.applyTajweed(item.arabic) }
            Text(
                text = annotatedArabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 22.sp,
                    lineHeight = 36.sp,
                    fontFamily = TajweedFontFamily,
                    fontWeight = FontWeight.Normal
                ),
                color = SaatColors.Slate900,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = item.latin,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp
                ),
                color = SaatColors.DeepEmerald
            )

            HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

            Text(
                text = trans,
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate800,
                lineHeight = 20.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 2: POSISI IMAM & JENAZAH
// -----------------------------------------------------------------------------------------

@Composable
private fun JanazahPositionTab(positions: List<JanazahPositionGuide>, appLanguage: AppLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = floatingNavBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Essential Header Card for Position Guide
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SaatColors.PureWhite,
                shadowElevation = 1.5.dp,
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "Sunnah Standing Positions"
                                AppLanguage.MALAY -> "Panduan Kedudukan Imam & Saf"
                                else -> "Panduan Posisi Imam & Saf Jenazah"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "The Imam stands in line with the HEAD for a male deceased, and in line with the WAIST/MIDDLE for a female deceased, as established in the authentic Sunnah."
                            AppLanguage.MALAY -> "Imam berdiri bersetentangan KEPALA bagi jenazah lelaki, dan bersetentangan PINGGANG/TENGAH bagi jenazah perempuan mengikut Sunnah Shahih."
                            else -> "Imam berdiri sejajar KEPALA untuk jenazah laki-laki, dan sejajar PINGGANG/TENGAH badan untuk jenazah perempuan sesuai Sunnah Shahih."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate700,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        items(positions) { pos ->
            JanazahPositionCard(guide = pos, appLanguage = appLanguage)
        }
    }
}

@Composable
private fun JanazahPositionCard(guide: JanazahPositionGuide, appLanguage: AppLanguage) {
    val title = guide.title(appLanguage)
    val imamPos = guide.imamPosition(appLanguage)
    val desc = guide.description(appLanguage)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = SaatColors.GoldDeep.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = guide.hadithRef,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.GoldDeep,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SaatColors.DeepEmerald.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = imamPos,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate700,
                lineHeight = 22.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 3: SYARAT, RUKUN & KEUTAMAAN (2 QIRATH)
// -----------------------------------------------------------------------------------------

@Composable
private fun JanazahRulingsTab(guide: JanazahGuide, appLanguage: AppLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = floatingNavBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hadith 2 Qirath Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SaatColors.GoldDeep.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SaatColors.GoldDeep,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "Reward of 2 Qirats (Mount Uhud)"
                                AppLanguage.MALAY -> "Pahala 2 Qirat (Gunung Uhud)"
                                else -> "Pahala 2 Qirath Sebesar Gunung Uhud"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.GoldDeep
                        )
                    }

                    Text(
                        text = guide.rewardHadith(appLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate800,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Rukun Shalat Jenazah
        item {
            RulingDetailCard(
                title = when (appLanguage) {
                    AppLanguage.ENGLISH -> "7 Pillars (Rukn) of Janazah Prayer"
                    AppLanguage.MALAY -> "7 Rukun Solat Jenazah"
                    else -> "7 Rukun Shalat Jenazah"
                },
                items = guide.pillars(appLanguage)
            )
        }

        // Syarat Sah
        item {
            RulingDetailCard(
                title = when (appLanguage) {
                    AppLanguage.ENGLISH -> "Valid Conditions (Shurut)"
                    AppLanguage.MALAY -> "Syarat Sah Solat Jenazah"
                    else -> "Syarat Sah Shalat Jenazah"
                },
                items = guide.conditions(appLanguage)
            )
        }
    }
}

@Composable
private fun RulingDetailCard(title: String, items: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )

            items.forEachIndexed { idx, str ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SaatColors.DeepEmerald.copy(alpha = 0.1f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }
                    }

                    Text(
                        text = str,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate700,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 4: DOA & DZIKIR BA'DA SHALAT JENAZAH
// -----------------------------------------------------------------------------------------

@Composable
private fun JanazahAfterDuasTab(duas: List<JanazahDuaItem>, appLanguage: AppLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = floatingNavBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(duas) { dua ->
            JanazahDuaCard(dua = dua, appLanguage = appLanguage)
        }
    }
}

@Composable
private fun JanazahDuaCard(dua: JanazahDuaItem, appLanguage: AppLanguage) {
    val title = dua.title(appLanguage)
    val trans = dua.translation(appLanguage)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )

            val annotatedArabic = remember(dua.arabic) { TajweedEngine.applyTajweed(dua.arabic) }
            Text(
                text = annotatedArabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 22.sp,
                    lineHeight = 38.sp,
                    fontFamily = TajweedFontFamily,
                    fontWeight = FontWeight.Normal
                ),
                color = SaatColors.Slate900,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = dua.latin,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp
                ),
                color = SaatColors.DeepEmerald
            )

            HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

            Text(
                text = trans,
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate800,
                lineHeight = 20.sp
            )
        }
    }
}
