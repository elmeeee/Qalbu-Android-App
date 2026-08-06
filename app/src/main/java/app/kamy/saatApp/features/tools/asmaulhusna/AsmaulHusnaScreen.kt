package app.kamy.saatApp.features.tools.asmaulhusna

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalAsmaulHusnaCatalog
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.AsmaulHusnaItem
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore

import androidx.compose.runtime.LaunchedEffect
import app.kamy.saatApp.infrastructure.preferences.OnboardingStore
import app.kamy.saatApp.ui.components.CoachMarkOverlay
import app.kamy.saatApp.ui.components.coachMarkTarget
import app.kamy.saatApp.ui.components.rememberCoachMarkState
import kotlinx.coroutines.delay

private enum class AsmaulHusnaFilter {
    ALL, FAVORITES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsmaulHusnaScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val onboardingStore = remember(context) { OnboardingStore.from(context) }
    val coachMarkState = rememberCoachMarkState()
    val appLanguage = remember(context) { AppLanguageStore.from(context).current() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AsmaulHusnaFilter.ALL) }
    var selectedItemForDetail by remember { mutableStateOf<AsmaulHusnaItem?>(null) }
    var favoriteIds by remember { mutableStateOf(setOf<Int>()) }
    val allItems = remember(context) { LocalAsmaulHusnaCatalog.getItems(context) }

    LaunchedEffect(Unit) {
        if (!onboardingStore.hasShownAsmaulCoachMark()) {
            delay(500)
            onboardingStore.markAsmaulCoachMarkShown()
            coachMarkState.show()
        }
    }

    val filteredItems = remember(searchQuery, selectedFilter, favoriteIds, appLanguage, allItems) {
        allItems.filter { item ->
            val matchesFilter = when (selectedFilter) {
                AsmaulHusnaFilter.ALL -> true
                AsmaulHusnaFilter.FAVORITES -> favoriteIds.contains(item.number)
            }

            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                item.number.toString() == query ||
                item.latin.lowercase().contains(query) ||
                item.arabic.contains(query) ||
                item.meaning(appLanguage).lowercase().contains(query)

            matchesFilter && matchesQuery
        }
    }

    val onToggleFavorite: (Int) -> Unit = remember {
        { itemNumber ->
            favoriteIds = if (favoriteIds.contains(itemNumber)) favoriteIds - itemNumber else favoriteIds + itemNumber
        }
    }
    val onSelectItem: (AsmaulHusnaItem) -> Unit = remember {
        { item ->
            selectedItemForDetail = item
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.asmaul_husna_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.asmaul_husna_subtitle_count),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar & Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .coachMarkTarget(
                        coachMarkState,
                        0,
                        R.string.coach_mark_asmaul_title,
                        R.string.coach_mark_asmaul_desc
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.asmaul_husna_search_hint),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = SaatColors.DeepEmerald,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilter == AsmaulHusnaFilter.ALL,
                        onClick = { selectedFilter = AsmaulHusnaFilter.ALL },
                        label = { Text(stringResource(R.string.asmaul_husna_filter_all, 99)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaatColors.DeepEmerald,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    FilterChip(
                        selected = selectedFilter == AsmaulHusnaFilter.FAVORITES,
                        onClick = { selectedFilter = AsmaulHusnaFilter.FAVORITES },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selectedFilter == AsmaulHusnaFilter.FAVORITES) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedFilter == AsmaulHusnaFilter.FAVORITES) Color.White else SaatColors.DeepEmerald
                            )
                        },
                        label = { Text(stringResource(R.string.asmaul_husna_filter_favorites, favoriteIds.size)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaatColors.DeepEmerald,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Grid of Asmaul Husna Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    AsmaulHusnaHeroCard()
                }

                items(filteredItems, key = { it.number }) { item ->
                    val isFav = favoriteIds.contains(item.number)
                    AsmaulHusnaCardItem(
                        item = item,
                        appLanguage = appLanguage,
                        isFavorite = isFav,
                        onToggleFavorite = onToggleFavorite,
                        onClick = onSelectItem
                    )
                }
            }
        }
    }

    // Detail & Dhikr Counter Bottom Sheet
    selectedItemForDetail?.let { item ->
        AsmaulHusnaDetailSheet(
            item = item,
            appLanguage = appLanguage,
            onDismiss = { selectedItemForDetail = null }
        )
    }

    CoachMarkOverlay(state = coachMarkState, onDismiss = { coachMarkState.skip() })
}

@Composable
private fun AsmaulHusnaHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(SaatColors.Gold, SaatColors.Gold.copy(alpha = 0.3f))
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SaatColors.DeepEmerald,
                            Color(0xFF0F4435),
                            Color(0xFF0A2E24)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = SaatColors.GoldBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.asmaul_husna_hero_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.GoldBright,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "وَلِلَّهِ الْأَسْمَاءُ الْحُسْنَىٰ فَادْعُوهُ بِهَا",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.asmaul_husna_hero_verse),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    text = stringResource(R.string.asmaul_husna_hero_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.GoldBright.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun AsmaulHusnaCardItem(
    item: AsmaulHusnaItem,
    appLanguage: AppLanguage,
    isFavorite: Boolean,
    onToggleFavorite: (Int) -> Unit,
    onClick: (AsmaulHusnaItem) -> Unit
) {
    val meaningText = remember(item, appLanguage) { item.meaning(appLanguage) }
    val dhikrText = remember(item.recommendedCount) { item.recommendedCount }
    val cardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val cardBorder = remember { BorderStroke(1.dp, Color(0xFFE2E8F0)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(item) }),
        shape = RoundedCornerShape(18.dp),
        colors = cardColors,
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = SaatColors.DeepEmerald.copy(alpha = 0.1f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${item.number}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite(item.number) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isFavorite) SaatColors.GoldDeep else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = item.arabic,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontSize = 26.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = item.latin,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = meaningText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(2.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SaatColors.DeepEmerald.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.asmaul_husna_dhikr_fmt, dhikrText),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsmaulHusnaDetailSheet(
    item: AsmaulHusnaItem,
    appLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    var dhikrCount by remember { mutableIntStateOf(0) }
    val targetCount = item.recommendedCount

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SaatColors.DeepEmerald,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "No. ${item.number} / 99",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = { dhikrCount = 0 }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Calligraphy & Latin
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.arabic,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald,
                    fontSize = 42.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = item.latin,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.meaning(appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Dalil Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${stringResource(R.string.asmaul_husna_dalil_title)} (${item.dalilReference})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }
                    Text(
                        text = item.dalil(appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }

            // Keutamaan & Amalan Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SaatColors.Gold.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, SaatColors.Gold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.asmaul_husna_fadhilah_title),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }
                    Text(
                        text = item.fadhilah(appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }

            // Interactive Digital Tasbih Counter
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SaatColors.DeepEmerald
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.asmaul_husna_tasbih_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.GoldBright,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$dhikrCount",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "/ $targetCount",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (dhikrCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SaatColors.GoldBright,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )

                    if (dhikrCount >= targetCount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SaatColors.GoldBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.asmaul_husna_counter_finish),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.GoldBright
                            )
                        }
                    }

                    // Large Tap Button
                    Surface(
                        onClick = {
                            if (dhikrCount < targetCount) {
                                dhikrCount++
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                dhikrCount = 0
                            }
                        },
                        shape = CircleShape,
                        color = SaatColors.Gold,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (dhikrCount >= targetCount) stringResource(R.string.asmaul_husna_tap_to_reset) else stringResource(R.string.asmaul_husna_tap_to_dhikr),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
