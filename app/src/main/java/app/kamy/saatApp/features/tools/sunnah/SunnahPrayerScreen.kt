package app.kamy.saatApp.features.tools.sunnah

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalSunnahNeedsCatalog
import app.kamy.saatApp.core.config.LocalSunnahPrayerCatalog
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.design.theme.TajweedFontFamily
import app.kamy.saatApp.domain.model.NiatItem
import app.kamy.saatApp.domain.model.PrayerStepItem
import app.kamy.saatApp.domain.model.SunnahActionStep
import app.kamy.saatApp.domain.model.SunnahNeedItem
import app.kamy.saatApp.domain.model.SunnahPrayerItem
import app.kamy.saatApp.features.quran.tajweed.TajweedEngine
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding

private enum class SunnahCategoryFilter(val labelId: String, val labelMs: String, val labelEn: String) {
    ALL("Semua Shalat Sunnah", "Semua Solat Sunat", "All Sunnah"),
    HARIAN("Shalat Harian & Rawatib", "Solat Harian & Rawatib", "Daily & Rawatib"),
    MALAM("Shalat Malam (Qiyam)", "Solat Malam (Qiyam)", "Night (Qiyam)"),
    KEBUTUHAN("Hajat, Taubat & Khusus", "Hajat, Taubat & Khusus", "Needs & Occasions");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> labelEn
        AppLanguage.MALAY -> labelMs
        AppLanguage.INDONESIAN -> labelId
    }
}

private enum class NeedCategoryFilter(val labelId: String, val labelMs: String, val labelEn: String) {
    ALL("Semua Amalan", "Semua Amalan", "All Deeds"),
    REZEKI_KARIR("Rezeki & Karir", "Rezeki & Kerjaya", "Sustenance & Career"),
    KELANCARAN_URUSAN("Kelancaran Urusan", "Kelancaran Urusan", "Smooth Affairs"),
    HUTANG_BEBAN("Pelunas Hutang", "Pelunas Hutang", "Debt Relief"),
    KETENANGAN_HATI("Ketenangan Hati", "Ketenangan Hati", "Peace of Mind"),
    JODOH_KELUARGA("Jodoh & Keluarga", "Jodoh & Keluarga", "Spouse & Family"),
    KESEHATAN_SEMBUH("Kesehatan", "Kesihatan", "Health & Healing"),
    TAUBAT_AMPUNAN("Taubat & Ampunan", "Taubat & Keampunan", "Repentance");

    fun label(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> labelEn
        AppLanguage.MALAY -> labelMs
        AppLanguage.INDONESIAN -> labelId
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunnahPrayerScreen(
    onBack: () -> Unit,
    onOpenDoaZikir: () -> Unit = {},
    onOpenQiyam: () -> Unit = {}
) {
    val context = LocalContext.current
    val appLanguage = remember(context) { AppLanguageStore.from(context).current() }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Shalat Sunnah, 1: Amalan Kehidupan

    var selectedPrayerCategory by remember { mutableStateOf(SunnahCategoryFilter.ALL) }
    var selectedNeedCategory by remember { mutableStateOf(NeedCategoryFilter.ALL) }

    var selectedPrayerForDetail by remember { mutableStateOf<SunnahPrayerItem?>(null) }
    var selectedNeedForDetail by remember { mutableStateOf<SunnahNeedItem?>(null) }

    val allPrayers = remember(context) { LocalSunnahPrayerCatalog.getItems(context) }
    val allNeeds = remember(context) { LocalSunnahNeedsCatalog.getItems(context) }

    val filteredPrayers = remember(selectedPrayerCategory, appLanguage, allPrayers) {
        allPrayers.filter { item ->
            when (selectedPrayerCategory) {
                SunnahCategoryFilter.ALL -> true
                SunnahCategoryFilter.HARIAN -> item.category == "HARIAN"
                SunnahCategoryFilter.MALAM -> item.category == "MALAM"
                SunnahCategoryFilter.KEBUTUHAN -> item.category == "KEBUTUHAN"
            }
        }
    }

    val filteredNeeds = remember(selectedNeedCategory, appLanguage, allNeeds) {
        allNeeds.filter { item ->
            when (selectedNeedCategory) {
                NeedCategoryFilter.ALL -> true
                NeedCategoryFilter.REZEKI_KARIR -> item.category == "REZEKI_KARIR"
                NeedCategoryFilter.KELANCARAN_URUSAN -> item.category == "KELANCARAN_URUSAN"
                NeedCategoryFilter.HUTANG_BEBAN -> item.category == "HUTANG_BEBAN"
                NeedCategoryFilter.KETENANGAN_HATI -> item.category == "KETENANGAN_HATI"
                NeedCategoryFilter.JODOH_KELUARGA -> item.category == "JODOH_KELUARGA"
                NeedCategoryFilter.KESEHATAN_SEMBUH -> item.category == "KESEHATAN_SEMBUH"
                NeedCategoryFilter.TAUBAT_AMPUNAN -> item.category == "TAUBAT_AMPUNAN"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SaatColors.ScreenBackground)
        ) {
            // Header Bar - Full White behind status bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaatColors.PureWhite,
                shadowElevation = 1.dp
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
                                text = stringResource(R.string.sunnah_prayer_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                            Text(
                                text = when (appLanguage) {
                                    AppLanguage.ENGLISH -> "Complete sunnah prayers & life-intention deeds with Hadiths"
                                    AppLanguage.MALAY -> "Solat sunat & amalan kehidupan dengan dalil sahih"
                                    else -> "Shalat sunnah & amalan kehidupan dengan dalil shahih"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = SaatColors.GoldDeep,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // 2-Segment Top Tabs
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = SaatColors.PureWhite,
                        contentColor = SaatColors.DeepEmerald,
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
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = {
                                Text(
                                    text = stringResource(R.string.sunnah_prayers_tab),
                                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (activeTab == 0) SaatColors.DeepEmerald else SaatColors.Slate500
                                )
                            }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = {
                                Text(
                                    text = stringResource(R.string.sunnah_deeds_tab),
                                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (activeTab == 1) SaatColors.DeepEmerald else SaatColors.Slate500
                                )
                            }
                        )
                    }
                }
            }

            if (activeTab == 0) {
                // Category Filter Pills for Sunnah Prayers
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = SaatSpacing.screenHorizontal, vertical = 10.dp)
                ) {
                    items(SunnahCategoryFilter.entries.toTypedArray()) { cat ->
                        FilterChip(
                            selected = selectedPrayerCategory == cat,
                            onClick = { selectedPrayerCategory = cat },
                            label = { Text(cat.label(appLanguage)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaatColors.DeepEmerald,
                                selectedLabelColor = SaatColors.PureWhite
                            )
                        )
                    }
                }

                // Prayer List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = SaatSpacing.screenHorizontal,
                        end = SaatSpacing.screenHorizontal,
                        top = 4.dp,
                        bottom = floatingNavBottomPadding() + 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredPrayers, key = { it.id }) { prayer ->
                        SunnahPrayerCard(
                            item = prayer,
                            appLanguage = appLanguage,
                            onClick = { selectedPrayerForDetail = prayer }
                        )
                    }
                }
            } else {
                // Category Filter Pills for Life Needs Deeds
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = SaatSpacing.screenHorizontal, vertical = 10.dp)
                ) {
                    items(NeedCategoryFilter.entries.toTypedArray()) { cat ->
                        FilterChip(
                            selected = selectedNeedCategory == cat,
                            onClick = { selectedNeedCategory = cat },
                            label = { Text(cat.label(appLanguage)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaatColors.DeepEmerald,
                                selectedLabelColor = SaatColors.PureWhite
                            )
                        )
                    }
                }

                // Sunnah Life Needs List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = SaatSpacing.screenHorizontal,
                        end = SaatSpacing.screenHorizontal,
                        top = 4.dp,
                        bottom = floatingNavBottomPadding() + 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredNeeds, key = { it.id }) { need ->
                        SunnahNeedCard(
                            item = need,
                            appLanguage = appLanguage,
                            onClick = { selectedNeedForDetail = need }
                        )
                    }
                }
            }
        }

        // Full Screen Prayer Detail
        selectedPrayerForDetail?.let { prayer ->
            SunnahPrayerDetailFullScreen(
                item = prayer,
                appLanguage = appLanguage,
                onDismiss = { selectedPrayerForDetail = null },
                onOpenDoaZikir = {
                    selectedPrayerForDetail = null
                    onOpenDoaZikir()
                },
                onOpenQiyam = {
                    selectedPrayerForDetail = null
                    onOpenQiyam()
                }
            )
        }

        // Full Screen Need Deed Detail
        selectedNeedForDetail?.let { need ->
            SunnahNeedDetailFullScreen(
                item = need,
                appLanguage = appLanguage,
                onDismiss = { selectedNeedForDetail = null }
            )
        }
    }
}

@Composable
private fun SunnahPrayerCard(
    item: SunnahPrayerItem,
    appLanguage: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SaatColors.PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SaatColors.DeepEmerald.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = item.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }

                item.hadithReference?.let { ref ->
                    Surface(
                        shape = CircleShape,
                        color = SaatColors.GoldDeep.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = ref,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.GoldDeep
                        )
                    }
                }
            }

            Text(
                text = item.title(appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = item.summary(appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate700,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SaatColors.Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.waktu(appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = SaatColors.Slate500
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "View Guide"
                            AppLanguage.MALAY -> "Lihat Panduan"
                            else -> "Lihat Panduan"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SunnahNeedCard(
    item: SunnahNeedItem,
    appLanguage: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SaatColors.PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SaatColors.DeepEmerald.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = item.category.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = SaatColors.GoldDeep.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.hadithReference,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.GoldDeep
                    )
                }
            }

            Text(
                text = item.title(appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = item.subtitle(appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate700,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SaatColors.GoldDeep,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${item.actionSteps.size} " + when (appLanguage) {
                            AppLanguage.ENGLISH -> "Deeds"
                            AppLanguage.MALAY -> "Amalan"
                            else -> "Amalan"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = SaatColors.Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Open Amalan"
                            AppLanguage.MALAY -> "Buka Amalan"
                            else -> "Buka Amalan"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SunnahNeedDetailFullScreen(
    item: SunnahNeedItem,
    appLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SaatColors.ScreenBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaatColors.PureWhite,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title(appLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.hadithReference,
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.GoldDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Description & Dalil Section
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SaatColors.PureWhite,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = item.description(appLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SaatColors.Slate700,
                                lineHeight = 22.sp
                            )

                            item.dalilArabic?.let { ar ->
                                HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.6f))
                                val annotatedArabic = remember(ar) { TajweedEngine.applyTajweed(ar) }
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
                            }

                            item.dalilLatin?.let { lt ->
                                Text(
                                    text = lt,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 20.sp
                                    ),
                                    color = SaatColors.DeepEmerald
                                )
                            }

                            item.dalilTranslation(appLanguage)?.let { tr ->
                                Text(
                                    text = tr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SaatColors.Slate700,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Action Steps Title
                item {
                    Text(
                        text = when (appLanguage) {
                            AppLanguage.ENGLISH -> "Steps of Sunnah Practice"
                            AppLanguage.MALAY -> "Langkah Amalan Sunnah"
                            else -> "Langkah-Langkah Amalan Sunnah"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Action Steps List
                items(item.actionSteps, key = { it.stepNumber }) { step ->
                    SunnahActionStepCard(step = step, appLanguage = appLanguage)
                }
            }
        }
    }
}

@Composable
private fun SunnahActionStepCard(step: SunnahActionStep, appLanguage: AppLanguage) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SaatColors.DeepEmerald,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = step.stepNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = step.title(appLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    step.targetCount?.let { count ->
                        Surface(
                            shape = CircleShape,
                            color = SaatColors.GoldDeep.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = count,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.GoldDeep
                            )
                        }
                    }
                }

                Text(
                    text = step.desc(appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaatColors.Slate700
                )

                step.arabic?.let { ar ->
                    val annotatedStepArabic = remember(ar) { TajweedEngine.applyTajweed(ar) }
                    Text(
                        text = annotatedStepArabic,
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
                }

                step.latin?.let { lt ->
                    Text(
                        text = lt,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp
                        ),
                        color = SaatColors.DeepEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun SunnahPrayerDetailFullScreen(
    item: SunnahPrayerItem,
    appLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onOpenDoaZikir: () -> Unit,
    onOpenQiyam: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SaatColors.ScreenBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaatColors.PureWhite,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title(appLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.waktu(appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate500
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Summary Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SaatColors.PureWhite,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = item.summary(appLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SaatColors.Slate700
                            )

                            HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.6f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = when (appLanguage) {
                                            AppLanguage.ENGLISH -> "Rakaat Info"
                                            AppLanguage.MALAY -> "Info Rakaat"
                                            else -> "Info Rakaat"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SaatColors.Slate500
                                    )
                                    Text(
                                        text = item.rakaatInfo(appLanguage),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.DeepEmerald
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = when (appLanguage) {
                                            AppLanguage.ENGLISH -> "Category"
                                            AppLanguage.MALAY -> "Kategori"
                                            else -> "Kategori"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SaatColors.Slate500
                                    )
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.GoldDeep
                                    )
                                }
                            }
                        }
                    }
                }

                // Hadith / Dalil Card
                item {
                    item.dalilHadith(appLanguage)?.let { dalil ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = SaatColors.GoldDeep.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_quran_on),
                                        contentDescription = null,
                                        tint = SaatColors.GoldDeep,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = when (appLanguage) {
                                            AppLanguage.ENGLISH -> "Hadith / Dalil Reference"
                                            AppLanguage.MALAY -> "Dalil / Hadis Sahih"
                                            else -> "Dalil / Hadits Shahih"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.GoldDeep
                                    )
                                }

                                Text(
                                    text = dalil,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SaatColors.Slate800
                                )

                                item.hadithReference?.let { ref ->
                                    Text(
                                        text = ref,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.GoldDeep
                                    )
                                }
                            }
                        }
                    }
                }

                // Niat List
                if (item.niatList.isNotEmpty()) {
                    item {
                        Text(
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "Niyyah (Intentions)"
                                AppLanguage.MALAY -> "Niat Solat"
                                else -> "Niat Shalat"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(item.niatList) { niat ->
                        NiatCard(niat = niat, appLanguage = appLanguage)
                    }
                }

                // Doa Khusus
                item.doaArabic?.let { doaAr ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = SaatColors.PureWhite,
                            shadowElevation = 1.5.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = when (appLanguage) {
                                        AppLanguage.ENGLISH -> "Special Supplication (Dua)"
                                        AppLanguage.MALAY -> "Doa Khusus Solat"
                                        else -> "Doa Khusus Shalat"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.DeepEmerald
                                )

                                val annotatedDoaArabic = remember(doaAr) { TajweedEngine.applyTajweed(doaAr) }
                                Text(
                                    text = annotatedDoaArabic,
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

                                item.doaLatin?.let { latin ->
                                    Text(
                                        text = latin,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontStyle = FontStyle.Italic,
                                            lineHeight = 20.sp
                                        ),
                                        color = SaatColors.DeepEmerald
                                    )
                                }

                                item.doaTranslation(appLanguage)?.let { tr ->
                                    Text(
                                        text = tr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SaatColors.Slate700
                                    )
                                }
                            }
                        }
                    }
                }

                // Steps List
                if (item.steps.isNotEmpty()) {
                    item {
                        Text(
                            text = when (appLanguage) {
                                AppLanguage.ENGLISH -> "Step-by-Step Guide"
                                AppLanguage.MALAY -> "Tatacara Solat"
                                else -> "Tata Cara Shalat"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(item.steps, key = { it.stepNumber }) { step ->
                        StepCard(step = step, appLanguage = appLanguage)
                    }
                }
            }
        }
    }
}

@Composable
private fun NiatCard(niat: NiatItem, appLanguage: AppLanguage) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = niat.title(appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )

            val annotatedArabic = remember(niat.arabic) { TajweedEngine.applyTajweed(niat.arabic) }
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
                text = niat.latin,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp
                ),
                color = SaatColors.DeepEmerald
            )

            Text(
                text = niat.translation(appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = SaatColors.Slate700
            )
        }
    }
}

@Composable
private fun StepCard(step: PrayerStepItem, appLanguage: AppLanguage) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SaatColors.DeepEmerald,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = step.stepNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = step.title(appLanguage),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = step.desc(appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaatColors.Slate700
                )
                step.arabic?.let { ar ->
                    val annotatedStepArabic = remember(ar) { TajweedEngine.applyTajweed(ar) }
                    Text(
                        text = annotatedStepArabic,
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
                }
                step.latin?.let { lt ->
                    Text(
                        text = lt,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp
                        ),
                        color = SaatColors.DeepEmerald
                    )
                }
            }
        }
    }
}
