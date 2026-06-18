package app.kamy.saatApp.features.tools.faraidh

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.domain.faraidh.BlockingReasonKey
import app.kamy.saatApp.domain.faraidh.DeceasedGender
import app.kamy.saatApp.domain.faraidh.FaraidhAdjustment
import app.kamy.saatApp.domain.faraidh.FaraidhProofItem
import app.kamy.saatApp.domain.faraidh.FaraidhProofKind
import app.kamy.saatApp.domain.faraidh.FaraidhResult
import app.kamy.saatApp.domain.faraidh.HeirType
import app.kamy.saatApp.domain.faraidh.SilsilahNode
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FaraidhCalculatorScreen(
    onBack: () -> Unit,
    onOpenVerse: (surah: Int, ayah: Int) -> Unit,
    vm: FaraidhViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showTooltip by remember { mutableStateOf(false) }
    val currency = remember { NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    } }

    if (showTooltip) {
        AlertDialog(
            onDismissRequest = { showTooltip = false },
            title = { Text(stringResource(R.string.faraidh_tooltip_title)) },
            text = { Text(stringResource(R.string.faraidh_tooltip_body)) },
            confirmButton = {
                TextButton(onClick = { showTooltip = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset(),
        containerColor = AlKhatibColors.ScreenBackground,
        floatingActionButton = {
            if (state.result?.activeShares?.isNotEmpty() == true && state.selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        vm.exportPdf { uri ->
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(share, context.getString(R.string.faraidh_export_pdf)))
                        }
                    },
                    containerColor = AlKhatibColors.DeepEmerald,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    if (state.pdfExporting) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = stringResource(R.string.faraidh_export_pdf))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FaraidhHeader(onBack = onBack, onInfo = { showTooltip = true })

            FaraidhHeroCard(
                netEstate = state.netEstate,
                onNetEstateChange = vm::setNetEstate,
                gender = state.gender,
                onGenderChange = vm::setGender,
                expanded = state.showInputSheet,
                onToggleExpanded = { vm.toggleInputSheet(!state.showInputSheet) },
                state = state,
                onHeirChange = vm::setHeirCount
            )

            val tabs = listOf(
                Triple(0, stringResource(R.string.faraidh_tab_breakdown), Icons.Filled.TableChart),
                Triple(1, stringResource(R.string.faraidh_tab_silsilah), Icons.Filled.AccountTree),
                Triple(2, stringResource(R.string.faraidh_tab_dalil), Icons.Filled.MenuBook)
            )
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Color.Transparent,
                contentColor = AlKhatibColors.DeepEmerald,
                indicator = { tabPositions ->
                    if (state.selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                            color = AlKhatibColors.DeepEmerald,
                            height = 3.dp
                        )
                    }
                },
                divider = { HorizontalDivider(color = AlKhatibColors.SoftGrey.copy(alpha = 0.5f)) }
            ) {
                tabs.forEach { (index, label, icon) ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { vm.selectTab(index) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(label, fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Medium)
                            }
                        },
                        selectedContentColor = AlKhatibColors.DeepEmerald,
                        unselectedContentColor = AlKhatibColors.Slate500
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.selectedTab) {
                    0 -> FaraidhBreakdownTab(state.result, currency)
                    1 -> FaraidhSilsilahTab(state.result?.silsilah.orEmpty())
                    2 -> FaraidhDalilTab(state.proofs, onOpenVerse) { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                }
            }
        }
    }
}

@Composable
private fun FaraidhHeader(onBack: () -> Unit, onInfo: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.faraidh_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.Slate900
            )
            Text(
                text = stringResource(R.string.faraidh_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )
        }
        IconButton(onClick = onInfo) {
            Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.faraidh_tooltip_title), tint = AlKhatibColors.Teal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FaraidhHeroCard(
    netEstate: String,
    onNetEstateChange: (String) -> Unit,
    gender: DeceasedGender,
    onGenderChange: (DeceasedGender) -> Unit,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    state: FaraidhUiState,
    onHeirChange: (HeirCountField, Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal)
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            AlKhatibColors.DeepEmerald.copy(alpha = 0.12f),
                            AlKhatibColors.Teal.copy(alpha = 0.08f),
                            AlKhatibColors.Gold.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(AlKhatibColors.DeepEmerald.copy(alpha = 0.3f), AlKhatibColors.Teal.copy(alpha = 0.15f))
                    ),
                    RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Calculate, contentDescription = null, tint = AlKhatibColors.DeepEmerald)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.faraidh_input_title),
                        fontWeight = FontWeight.SemiBold,
                        color = AlKhatibColors.DeepEmerald
                    )
                }
                TextButton(onClick = onToggleExpanded) {
                    Text(if (expanded) stringResource(R.string.faraidh_collapse) else stringResource(R.string.faraidh_expand))
                }
            }

            OutlinedTextField(
                value = netEstate,
                onValueChange = onNetEstateChange,
                label = { Text(stringResource(R.string.faraidh_net_estate)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.faraidh_deceased_gender), style = MaterialTheme.typography.labelMedium, color = AlKhatibColors.Slate500)
            Spacer(Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = gender == DeceasedGender.MALE,
                    onClick = { onGenderChange(DeceasedGender.MALE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = AlKhatibColors.DeepEmerald, activeContentColor = Color.White)
                ) { Text(stringResource(R.string.faraidh_gender_male)) }
                SegmentedButton(
                    selected = gender == DeceasedGender.FEMALE,
                    onClick = { onGenderChange(DeceasedGender.FEMALE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = AlKhatibColors.DeepEmerald, activeContentColor = Color.White)
                ) { Text(stringResource(R.string.faraidh_gender_female)) }
            }

            AnimatedVisibility(expanded) {
                Column(modifier = Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.faraidh_heirs_section), fontWeight = FontWeight.SemiBold, color = AlKhatibColors.Slate800)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        heirCounter(stringResource(R.string.faraidh_heir_husband), state.husbandCount, 0, 1) { onHeirChange(HeirCountField.HUSBAND, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_wife), state.wifeCount, 0, 4) { onHeirChange(HeirCountField.WIFE, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_father), state.fatherCount, 0, 1) { onHeirChange(HeirCountField.FATHER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_mother), state.motherCount, 0, 1) { onHeirChange(HeirCountField.MOTHER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_son), state.sonCount) { onHeirChange(HeirCountField.SON, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_daughter), state.daughterCount) { onHeirChange(HeirCountField.DAUGHTER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_grandson), state.grandsonCount) { onHeirChange(HeirCountField.GRANDSON, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_granddaughter), state.granddaughterCount) { onHeirChange(HeirCountField.GRANDDAUGHTER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_full_brother), state.fullBrotherCount) { onHeirChange(HeirCountField.FULL_BROTHER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_full_sister), state.fullSisterCount) { onHeirChange(HeirCountField.FULL_SISTER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_paternal_brother), state.paternalBrotherCount) { onHeirChange(HeirCountField.PATERNAL_BROTHER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_paternal_sister), state.paternalSisterCount) { onHeirChange(HeirCountField.PATERNAL_SISTER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_maternal_brother), state.maternalBrotherCount) { onHeirChange(HeirCountField.MATERNAL_BROTHER, it) }
                        heirCounter(stringResource(R.string.faraidh_heir_maternal_sister), state.maternalSisterCount) { onHeirChange(HeirCountField.MATERNAL_SISTER, it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun heirCounter(label: String, count: Int, min: Int = 0, max: Int = 99, onChange: (Int) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AlKhatibColors.PureWhite.copy(alpha = 0.85f),
        modifier = Modifier.border(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = AlKhatibColors.Slate800, maxLines = 1)
            IconButton(onClick = { onChange((count - 1).coerceAtLeast(min)) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text("$count", fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
            IconButton(onClick = { onChange((count + 1).coerceAtMost(max)) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun FaraidhBreakdownTab(result: FaraidhResult?, currency: NumberFormat) {
    if (result == null || result.activeShares.isEmpty()) {
        EmptyTabMessage(stringResource(R.string.faraidh_empty_result))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PremiumSummaryCard(
                title = stringResource(R.string.faraidh_net_estate),
                value = currency.format(result.deceased.netEstate),
                subtitle = stringResource(R.string.faraidh_distributed, currency.format(result.totalDistributed))
            )
        }
        if (result.adjustment != FaraidhAdjustment.NONE) {
            item {
                AdjustmentBanner(result)
            }
        }
        item {
            Text(stringResource(R.string.faraidh_active_heirs), fontWeight = FontWeight.Bold, color = AlKhatibColors.Slate800)
        }
        items(result.activeShares, key = { "${it.type}_${it.headCount}_${it.fraction}" }) { share ->
            HeirShareCard(share, currency)
        }
        if (result.blockedHeirs.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.faraidh_blocked_heirs), fontWeight = FontWeight.Bold, color = AlKhatibColors.Slate800)
            }
            items(result.blockedHeirs, key = { "${it.type}_${it.reason}" }) { blocked ->
                BlockedHeirCard(blocked)
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun PremiumSummaryCard(title: String, value: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark)))
            .padding(20.dp)
    ) {
        Column {
            Text(title, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun AdjustmentBanner(result: FaraidhResult) {
    val text = when (result.adjustment) {
        FaraidhAdjustment.AWL -> stringResource(R.string.faraidh_awl_applied)
        FaraidhAdjustment.RADD -> stringResource(R.string.faraidh_radd_applied)
        FaraidhAdjustment.NONE -> ""
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AlKhatibColors.AmberWash,
        modifier = Modifier.border(1.dp, AlKhatibColors.Gold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
    ) {
        Text(text, modifier = Modifier.padding(14.dp), color = AlKhatibColors.GoldDeep, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HeirShareCard(share: app.kamy.saatApp.domain.faraidh.HeirShare, currency: NumberFormat) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AlKhatibColors.PureWhite,
        modifier = Modifier.border(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(heirTypeLabel(share.type), fontWeight = FontWeight.SemiBold, color = AlKhatibColors.Slate900)
                if (share.isAsabah) {
                    Surface(shape = RoundedCornerShape(8.dp), color = AlKhatibColors.MintWash) {
                        Text("Asabah", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.DeepEmerald)
                    }
                }
            }
            Text(stringResource(R.string.faraidh_heads, share.headCount), style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.faraidh_fraction), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500)
                    Text(share.fraction.toDisplayString(), fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.faraidh_percent), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500)
                    Text("${share.percentage}%", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.faraidh_amount), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500)
                    Text(currency.format(share.cashAmount), fontWeight = FontWeight.Bold, color = AlKhatibColors.Slate900)
                }
            }
        }
    }
}

@Composable
private fun BlockedHeirCard(blocked: app.kamy.saatApp.domain.faraidh.BlockedHeir) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AlKhatibColors.LightGrey,
        modifier = Modifier.border(1.dp, AlKhatibColors.SoftGrey, RoundedCornerShape(14.dp))
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(AlKhatibColors.Danger, CircleShape))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("${heirTypeLabel(blocked.type)} ×${blocked.headCount}", fontWeight = FontWeight.Medium, color = AlKhatibColors.Slate800)
                Text(blockingReasonLabel(blocked.reason), style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
            }
        }
    }
}

@Composable
private fun FaraidhSilsilahTab(nodes: List<SilsilahNode>) {
    if (nodes.isEmpty()) {
        EmptyTabMessage(stringResource(R.string.faraidh_empty_silsilah))
        return
    }
    val grouped = nodes.groupBy { it.generationLevel }.toSortedMap()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        grouped.forEach { (level, levelNodes) ->
            item(key = "gen_$level") {
                Text(
                    when (level) {
                        -1 -> stringResource(R.string.faraidh_gen_parents)
                        0 -> stringResource(R.string.faraidh_gen_same)
                        else -> stringResource(R.string.faraidh_gen_descendants)
                    },
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.DeepEmerald
                )
            }
            items(levelNodes, key = { it.id }) { node ->
                SilsilahNodeCard(node)
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun SilsilahNodeCard(node: SilsilahNode) {
    val borderColor = when {
        node.blocked -> AlKhatibColors.Danger.copy(alpha = 0.4f)
        node.inherits -> AlKhatibColors.Teal.copy(alpha = 0.5f)
        else -> AlKhatibColors.SoftGrey
    }
    val bg = when {
        node.blocked -> AlKhatibColors.LightGrey
        node.inherits -> AlKhatibColors.PrayerMint
        else -> AlKhatibColors.PureWhite
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(start = (node.generationLevel.coerceAtLeast(0) * 12).dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(heirLabelRes(node.labelKey)), fontWeight = FontWeight.SemiBold)
                Text("×${node.headCount}", style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
            }
            when {
                node.blocked -> Text(stringResource(R.string.faraidh_status_blocked), color = AlKhatibColors.Danger, style = MaterialTheme.typography.labelMedium)
                node.inherits -> Text(stringResource(R.string.faraidh_status_inherits), color = AlKhatibColors.DeepEmerald, style = MaterialTheme.typography.labelMedium)
                else -> Text(stringResource(R.string.faraidh_status_absent), color = AlKhatibColors.Slate500, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun FaraidhDalilTab(
    proofs: List<FaraidhProofItem>,
    onOpenVerse: (Int, Int) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    if (proofs.isEmpty()) {
        EmptyTabMessage(stringResource(R.string.faraidh_empty_dalil))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(proofs, key = { it.id }) { proof ->
            DalilCard(proof, onOpenVerse, onOpenUrl)
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun DalilCard(
    proof: FaraidhProofItem,
    onOpenVerse: (Int, Int) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AlKhatibColors.PureWhite,
        modifier = Modifier.border(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(proof.title, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
                when (proof.kind) {
                    FaraidhProofKind.QURAN -> {
                        if (proof.surah != null && proof.ayah != null) {
                            IconButton(onClick = { onOpenVerse(proof.surah, proof.ayah) }) {
                                Icon(Icons.Filled.MenuBook, contentDescription = stringResource(R.string.faraidh_open_quran), tint = AlKhatibColors.Teal)
                            }
                        }
                    }
                    FaraidhProofKind.HADITH -> {
                        proof.externalUrl?.let { url ->
                            IconButton(onClick = { onOpenUrl(url) }) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = stringResource(R.string.faraidh_open_external), tint = AlKhatibColors.BlueLink)
                            }
                        }
                    }
                    FaraidhProofKind.NOTE -> Unit
                }
            }
            proof.arabic?.let { arabic ->
                Text(
                    arabic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, textDirection = TextDirection.Rtl),
                    color = AlKhatibColors.Slate900
                )
            }
            Text(proof.body, style = MaterialTheme.typography.bodyMedium, color = AlKhatibColors.Slate800)
        }
    }
}

@Composable
private fun EmptyTabMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = AlKhatibColors.Slate500, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
    }
}

@Composable
private fun heirTypeLabel(type: HeirType): String = stringResource(heirTypeRes(type))

@Composable
private fun blockingReasonLabel(reason: BlockingReasonKey): String = when (reason) {
    BlockingReasonKey.BY_SON -> stringResource(R.string.faraidh_block_by_son)
    BlockingReasonKey.BY_CHILDREN -> stringResource(R.string.faraidh_block_by_children)
    BlockingReasonKey.BY_FATHER -> stringResource(R.string.faraidh_block_by_father)
    BlockingReasonKey.BY_GRANDCHILDREN_SUBSTITUTE -> stringResource(R.string.faraidh_block_by_grandchild)
    BlockingReasonKey.GENDER_MISMATCH -> stringResource(R.string.faraidh_block_gender)
    BlockingReasonKey.NO_SHARE_REMAINDER -> stringResource(R.string.faraidh_block_no_share)
}

private fun heirTypeRes(type: HeirType): Int = when (type) {
    HeirType.HUSBAND -> R.string.faraidh_heir_husband
    HeirType.WIFE -> R.string.faraidh_heir_wife
    HeirType.FATHER -> R.string.faraidh_heir_father
    HeirType.MOTHER -> R.string.faraidh_heir_mother
    HeirType.SON -> R.string.faraidh_heir_son
    HeirType.DAUGHTER -> R.string.faraidh_heir_daughter
    HeirType.GRANDSON -> R.string.faraidh_heir_grandson
    HeirType.GRANDDAUGHTER -> R.string.faraidh_heir_granddaughter
    HeirType.FULL_BROTHER -> R.string.faraidh_heir_full_brother
    HeirType.FULL_SISTER -> R.string.faraidh_heir_full_sister
    HeirType.PATERNAL_BROTHER -> R.string.faraidh_heir_paternal_brother
    HeirType.PATERNAL_SISTER -> R.string.faraidh_heir_paternal_sister
    HeirType.MATERNAL_SIBLING -> R.string.faraidh_heir_maternal_sibling
}

private fun heirLabelRes(key: String): Int = when (key) {
    "faraidh_node_deceased" -> R.string.faraidh_node_deceased
    else -> heirTypeRes(
        when (key) {
            "faraidh_heir_father" -> HeirType.FATHER
            "faraidh_heir_mother" -> HeirType.MOTHER
            "faraidh_heir_husband" -> HeirType.HUSBAND
            "faraidh_heir_wife" -> HeirType.WIFE
            "faraidh_heir_son" -> HeirType.SON
            "faraidh_heir_daughter" -> HeirType.DAUGHTER
            "faraidh_heir_grandson" -> HeirType.GRANDSON
            "faraidh_heir_granddaughter" -> HeirType.GRANDDAUGHTER
            "faraidh_heir_full_brother" -> HeirType.FULL_BROTHER
            "faraidh_heir_full_sister" -> HeirType.FULL_SISTER
            "faraidh_heir_paternal_brother" -> HeirType.PATERNAL_BROTHER
            "faraidh_heir_paternal_sister" -> HeirType.PATERNAL_SISTER
            "faraidh_heir_maternal_sibling" -> HeirType.MATERNAL_SIBLING
            else -> HeirType.SON
        }
    )
}
