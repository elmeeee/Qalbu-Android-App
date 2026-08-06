package app.kamy.saatApp.features.tools.sunnah

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalSunnahPrayerCatalog
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.model.NiatItem
import app.kamy.saatApp.domain.model.PrayerStepItem
import app.kamy.saatApp.domain.model.SunnahPrayerItem
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

private enum class SunnahCategoryFilter(val labelId: String, val labelMs: String, val labelEn: String) {
    ALL("Semua", "Semua", "All"),
    HARIAN("Harian", "Harian", "Daily"),
    MALAM("Malam", "Malam", "Night"),
    KEBUTUHAN("Hajat & Taubat", "Hajat & Taubat", "Needs & Repentance"),
    JENAZAH("Shalat Jenazah", "Solat Jenazah", "Janazah (Funeral)");

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
    var selectedCategory by remember { mutableStateOf(SunnahCategoryFilter.ALL) }
    var selectedItemForDetail by remember { mutableStateOf<SunnahPrayerItem?>(null) }

    val allPrayers = remember(context) { LocalSunnahPrayerCatalog.getItems(context) }

    val filteredPrayers = remember(selectedCategory, appLanguage, allPrayers) {
        allPrayers.filter { item ->
            when (selectedCategory) {
                SunnahCategoryFilter.ALL -> true
                SunnahCategoryFilter.HARIAN -> item.category == "HARIAN"
                SunnahCategoryFilter.MALAM -> item.category == "MALAM"
                SunnahCategoryFilter.KEBUTUHAN -> item.category == "KEBUTUHAN"
                SunnahCategoryFilter.JENAZAH -> item.category == "JENAZAH"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SaatColors.ScreenBackground)
                .tabContentStatusBarInset()
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SaatColors.ScreenBackground,
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (appLanguage == AppLanguage.ENGLISH) "Sunnah & Janazah Guide" else if (appLanguage == AppLanguage.MALAY) "Panduan Solat Sunnah & Jenazah" else "Panduan Salat Sunnah & Jenazah",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (appLanguage == AppLanguage.ENGLISH) "Complete guide with Hadiths, Surahs & Supplications" else if (appLanguage == AppLanguage.MALAY) "Panduan lengkap niat, hadis, surah & doa solat sunnah" else "Panduan lengkap niat, hadits, surah & doa shalat sunnah",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.Slate500
                        )
                    }
                }
            }

            // Category Filter Pills (No search bar)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = SaatSpacing.screenHorizontal, vertical = 10.dp)
            ) {
                items(SunnahCategoryFilter.entries.toTypedArray()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
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
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredPrayers, key = { it.id }) { prayer ->
                    SunnahPrayerCard(
                        item = prayer,
                        appLanguage = appLanguage,
                        onClick = { selectedItemForDetail = prayer }
                    )
                }
            }
        }

        // Full Screen Detail Screen
        selectedItemForDetail?.let { prayer ->
            SunnahPrayerDetailFullScreen(
                item = prayer,
                appLanguage = appLanguage,
                onDismiss = { selectedItemForDetail = null },
                onOpenDoaZikir = {
                    selectedItemForDetail = null
                    onOpenDoaZikir()
                },
                onOpenQiyam = {
                    selectedItemForDetail = null
                    onOpenQiyam()
                }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (item.category == "JENAZAH") SaatColors.GoldDeep.copy(0.15f) else SaatColors.DeepEmerald.copy(0.12f)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.category == "JENAZAH") SaatColors.GoldDeep else SaatColors.DeepEmerald,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = item.rakaatInfo(appLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = SaatColors.Slate500
                )
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
                maxLines = 2
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SaatColors.Slate500
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.waktu(appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = SaatColors.Slate500,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (appLanguage == AppLanguage.ENGLISH) "View Guide" else if (appLanguage == AppLanguage.MALAY) "Lihat Panduan" else "Lihat Panduan",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SaatColors.ScreenBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .tabContentStatusBarInset()
            ) {
                // Top Header Bar with Back Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SaatColors.ScreenBackground,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title(appLanguage),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SaatColors.GoldDeep
                            )
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Info Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SaatColors.DeepEmerald.copy(0.08f),
                        border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.title(appLanguage),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                            Text(
                                text = item.summary(appLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SaatColors.Slate700
                            )
                            HorizontalDivider(color = SaatColors.DeepEmerald.copy(0.15f))
                            Text(
                                text = "📌 ${if (appLanguage == AppLanguage.ENGLISH) "Timing" else if (appLanguage == AppLanguage.MALAY) "Waktu Masa" else "Waktu Pelaksanaan"}: ${item.waktu(appLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SaatColors.Slate700,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "⭐ ${if (appLanguage == AppLanguage.ENGLISH) "Virtue" else if (appLanguage == AppLanguage.MALAY) "Kelebihan" else "Keutamaan"}: ${item.fadhilah(appLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SaatColors.Slate700
                            )
                        }
                    }

                    // Hadits / Dalil Sahih Card
                    item.dalilHadith(appLanguage)?.let { hadith ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SaatColors.GoldDeep.copy(0.08f),
                            border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.FormatQuote,
                                        contentDescription = null,
                                        tint = SaatColors.GoldDeep,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (appLanguage == AppLanguage.ENGLISH) "Hadith & Proof" else if (appLanguage == AppLanguage.MALAY) "Dalil & Hadis Sahih" else "Dalil & Hadits Sahih",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.GoldDeep
                                    )
                                }

                                Text(
                                    text = "\"$hadith\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SaatColors.Slate800
                                )

                                item.hadithReference?.let { ref ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SaatColors.GoldDeep.copy(0.2f),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(
                                            text = ref,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SaatColors.GoldDeep,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recommended Surahs Card
                    item.recommendedSurahs(appLanguage)?.let { surahs ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SaatColors.DeepEmerald.copy(0.06f),
                            border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = SaatColors.DeepEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (appLanguage == AppLanguage.ENGLISH) "Recommended Surahs" else if (appLanguage == AppLanguage.MALAY) "Surah Yang Disyorkan" else "Surah Yang Dianjurkan",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.DeepEmerald
                                    )
                                }

                                Text(
                                    text = surahs,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SaatColors.Slate700
                                )
                            }
                        }
                    }

                    // Section 1: Niat & Lafaz
                    if (item.niatList.isNotEmpty()) {
                        Text(
                            text = if (appLanguage == AppLanguage.ENGLISH) "1. Prayer Intention (Niyyah)" else if (appLanguage == AppLanguage.MALAY) "1. Niat Solat" else "1. Niat Shalat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        item.niatList.forEach { niat ->
                            NiatCard(niat = niat, appLanguage = appLanguage)
                        }
                    }

                    // Section 2: Tata Cara Step-by-Step
                    if (item.steps.isNotEmpty()) {
                        Text(
                            text = if (appLanguage == AppLanguage.ENGLISH) "2. Step-by-Step Procedure" else if (appLanguage == AppLanguage.MALAY) "2. Tatacara Pelaksanaan" else "2. Tata Cara Pelaksanaan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        item.steps.forEach { step ->
                            StepCard(step = step, appLanguage = appLanguage)
                        }
                    }

                    // Section 3: Doa Khusus & Feature Connections
                    Text(
                        text = if (appLanguage == AppLanguage.ENGLISH) "3. Supplication & Feature Actions" else if (appLanguage == AppLanguage.MALAY) "3. Doa & Sambungan Fitur" else "3. Doa & Sambungan Fitur",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!item.doaArabic.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SaatColors.PureWhite,
                            border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = item.doaArabic.orEmpty(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.DeepEmerald,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                item.doaLatin?.let { latin ->
                                    Text(
                                        text = latin,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = SaatColors.GoldDeep
                                    )
                                }
                                item.doaTranslation(appLanguage)?.let { trans ->
                                    Text(
                                        text = trans,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SaatColors.Slate700
                                    )
                                }
                            }
                        }
                    }

                    // Feature Action Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenDoaZikir,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald)
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (appLanguage == AppLanguage.ENGLISH) "Open Doa & Zikir Collection" else if (appLanguage == AppLanguage.MALAY) "Buka Koleksi Doa & Zikir Lengkap" else "Buka Koleksi Doa & Zikir Lengkap",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (item.id == "tahajjud" || item.category == "MALAM") {
                            Button(
                                onClick = onOpenQiyam,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald)
                            ) {
                                Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (appLanguage == AppLanguage.ENGLISH) "Open Qiyam Night Tracker" else "Buka Qiyam Night Tracker",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(36.dp))
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = niat.title(appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = niat.arabic,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = niat.latin,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SaatColors.GoldDeep
            )
            Text(
                text = "\"${niat.translation(appLanguage)}\"",
                style = MaterialTheme.typography.bodySmall,
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
                    Text(
                        text = ar,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                step.latin?.let { lt ->
                    Text(
                        text = lt,
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.GoldDeep
                    )
                }
            }
        }
    }
}
