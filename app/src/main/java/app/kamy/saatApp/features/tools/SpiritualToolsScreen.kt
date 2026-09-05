package app.kamy.saatApp.features.tools

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.ui.feedback.rememberTapHaptic
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

enum class ToolCategory(
    @StringRes val labelRes: Int
) {
    ALL(R.string.tool_category_all),
    PRAYER(R.string.tool_category_prayer),
    DHIKR(R.string.tool_category_dhikr),
    FIQH(R.string.tool_category_fiqh)
}

private data class SpiritualToolItem(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val route: String,
    val category: ToolCategory
)

@Composable
fun SpiritualToolsScreen(
    onOpenTool: (String) -> Unit
) {
    val haptic = rememberTapHaptic()
    val focusManager = LocalFocusManager.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(ToolCategory.ALL) }

    val allTools = remember {
        listOf(
            // Prayer & Time
            SpiritualToolItem(
                R.drawable.ic_qibla_3d,
                R.string.tool_qibla_title,
                R.string.tool_qibla_desc,
                "qibla",
                ToolCategory.PRAYER
            ),
            SpiritualToolItem(
                R.drawable.ic_sunnah_3d,
                R.string.tool_sunnah_practices_title,
                R.string.tool_sunnah_practices_desc,
                "sunnah-prayer",
                ToolCategory.PRAYER
            ),
            SpiritualToolItem(
                R.drawable.ic_jamak_3d,
                R.string.tool_jamak_guide_title,
                R.string.tool_jamak_guide_desc,
                "jamak-qashar",
                ToolCategory.PRAYER
            ),
            SpiritualToolItem(
                R.drawable.ic_radio_3d,
                R.string.tool_radio_title,
                R.string.tool_radio_desc,
                "radio",
                ToolCategory.PRAYER
            ),

            // Dhikr & Du'a
            SpiritualToolItem(
                R.drawable.ic_doazikir_3d,
                R.string.tool_dua_dhikr_title,
                R.string.tool_dua_dhikr_desc,
                "doa-zikir",
                ToolCategory.DHIKR
            ),
            SpiritualToolItem(
                R.drawable.ic_tasbih_3d,
                R.string.tool_tasbih_title,
                R.string.tool_tasbih_desc,
                "dhikr",
                ToolCategory.DHIKR
            ),
            SpiritualToolItem(
                R.drawable.ic_asmaulhusna_3d,
                R.string.tool_asmaul_husna_title,
                R.string.tool_asmaul_husna_desc,
                "asmaul-husna",
                ToolCategory.DHIKR
            ),
            SpiritualToolItem(
                R.drawable.ic_manzil_3d,
                R.string.tool_manzil_title,
                R.string.tool_manzil_desc,
                "manzil",
                ToolCategory.DHIKR
            ),

            // Fiqh & Guides
            SpiritualToolItem(
                R.drawable.ic_zakat_3d,
                R.string.tool_zakah_title,
                R.string.tool_zakah_desc,
                "zakat",
                ToolCategory.FIQH
            ),
            SpiritualToolItem(
                R.drawable.ic_fidyah_3d,
                R.string.tool_fidyah_tracker_title,
                R.string.tool_fidyah_tracker_desc,
                "fidyah",
                ToolCategory.FIQH
            ),
            SpiritualToolItem(
                R.drawable.ic_faraidh_3d,
                R.string.tool_faraidh_title,
                R.string.tool_faraidh_desc,
                "faraidh",
                ToolCategory.FIQH
            ),
            SpiritualToolItem(
                R.drawable.ic_hajj_umrah_3d,
                R.string.tool_hajj_umrah_title,
                R.string.tool_hajj_umrah_desc,
                "hajj-umrah",
                ToolCategory.FIQH
            ),
            SpiritualToolItem(
                R.drawable.ic_jenazah_3d,
                R.string.tool_janazah_guide_title,
                R.string.tool_janazah_guide_desc,
                "jenazah",
                ToolCategory.FIQH
            ),
            SpiritualToolItem(
                R.drawable.ic_encyclopedia_3d,
                R.string.tool_encyclopedia_title,
                R.string.tool_encyclopedia_desc,
                "encyclopedia",
                ToolCategory.FIQH
            )
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val filteredTools by remember(searchQuery, selectedCategory) {
        derivedStateOf {
            allTools.filter { tool ->
                val matchesCategory = selectedCategory == ToolCategory.ALL || tool.category == selectedCategory
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    val query = searchQuery.trim().lowercase()
                    val title = context.getString(tool.titleRes).lowercase()
                    val desc = context.getString(tool.descRes).lowercase()
                    title.contains(query) || desc.contains(query)
                }
                matchesCategory && matchesSearch
            }
        }
    }

    val gridState = rememberLazyGridState()
    val isScrolled by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 90
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.HomeBg)
    ) {
        // 1. Parallax Header Background Image (Edge to Edge, 0.45x Parallax Speed)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .graphicsLayer {
                    val offset = if (gridState.firstVisibleItemIndex == 0) {
                        gridState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        1000f
                    }
                    translationY = -offset * 0.45f
                    alpha = (1f - (offset / 180f)).coerceIn(0f, 1f)
                }
        ) {
            AsyncImage(
                model = R.drawable.bg_worship_header,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                SaatColors.HomeBg.copy(alpha = 0.20f),
                                SaatColors.HomeBg.copy(alpha = 0.65f),
                                SaatColors.HomeBg
                            )
                        )
                    )
            )
        }

        // 2. Main Scrollable Grid
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 0.dp,
                bottom = floatingNavBottomPadding() + 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Content spanning 2 columns
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tabContentStatusBarInset()
                        .padding(top = 16.dp, bottom = 6.dp)
                ) {
                    // Large Title & Subtitle
                    Text(
                        text = stringResource(R.string.worship_header_title),
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.HomeDarkGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.worship_header_subtitle),
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = SaatColors.Slate700
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Modern Search Bar
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SaatColors.PureWhite,
                        border = BorderStroke(1.dp, Color(0xFFE8E2D2)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SaatColors.HomeDarkGreen.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.tool_search_hint),
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = SaatColors.Slate500
                                        )
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SaatColors.Slate900
                                    ),
                                    cursorBrush = SolidColor(SaatColors.HomeDarkGreen),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = SaatColors.Slate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Filter Pills (Tasbih style FilterChip)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ToolCategory.entries.toTypedArray()) { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptic()
                                    selectedCategory = category
                                },
                                label = {
                                    Text(
                                        text = stringResource(category.labelRes),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaatColors.DeepEmerald,
                                    selectedLabelColor = SaatColors.PureWhite
                                )
                            )
                        }
                    }
                }
            }

            // Grid Items
            if (filteredTools.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.tool_search_empty),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SaatColors.Slate700
                            )
                        )
                    }
                }
            } else {
                items(filteredTools, key = { it.route }) { tool ->
                    SpiritualToolGridCard(
                        tool = tool,
                        onClick = {
                            haptic()
                            onOpenTool(tool.route)
                        }
                    )
                }
            }
        }

        // 3. Sticky Top Bar on Scroll (Frosted Glass & Compact Title)
        AnimatedVisibility(
            visible = isScrolled,
            enter = fadeIn(tween(140)),
            exit = fadeOut(tween(100)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SaatColors.HomeBg.copy(alpha = 0.96f))
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = Color(0xFF176345).copy(alpha = 0.08f),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tabContentStatusBarInset()
                        .padding(top = 12.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.worship_header_title),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.HomeDarkGreen
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = stringResource(R.string.worship_header_subtitle),
                        style = TextStyle(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = SaatColors.Slate700
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SpiritualToolGridCard(
    tool: SpiritualToolItem,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(156f / 176f),
        shape = cardShape,
        color = SaatColors.PureWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFEAE4D6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3D Visual Icon
            AsyncImage(
                model = tool.iconRes,
                contentDescription = stringResource(tool.titleRes),
                modifier = Modifier
                    .size(76.dp)
                    .padding(bottom = 6.dp),
                contentScale = ContentScale.Fit
            )

            // Title
            Text(
                text = stringResource(tool.titleRes),
                style = TextStyle(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SaatColors.HomeDarkGreen,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle Description
            Text(
                text = stringResource(tool.descRes),
                style = TextStyle(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = SaatColors.Slate700,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.5.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}