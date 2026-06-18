package app.kamy.saatApp.features.tools.faraidh

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.School
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.AlKhatibPartialBottomSheet
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.layout.ContentScale
import app.kamy.saatApp.domain.faraidh.EstateAssetInput
import app.kamy.saatApp.domain.faraidh.EstateComputation
import app.kamy.saatApp.domain.faraidh.FaraidhGlossaryItem
import app.kamy.saatApp.domain.faraidh.FaraidhMadhhab
import app.kamy.saatApp.domain.faraidh.BlockingReasonKey
import app.kamy.saatApp.domain.faraidh.DeceasedGender
import app.kamy.saatApp.domain.faraidh.FaraidhAdjustment
import app.kamy.saatApp.domain.faraidh.FaraidhProofItem
import app.kamy.saatApp.domain.faraidh.FaraidhProofKind
import app.kamy.saatApp.domain.faraidh.FaraidhNameLabels
import app.kamy.saatApp.domain.faraidh.FaraidhParticipantNames
import app.kamy.saatApp.domain.faraidh.FaraidhResult
import app.kamy.saatApp.domain.faraidh.HeirShare
import app.kamy.saatApp.domain.faraidh.HeirType
import app.kamy.saatApp.domain.faraidh.SilsilahNode
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaraidhCalculatorScreen(
    onBack: () -> Unit,
    onOpenVerse: (surah: Int, ayah: Int) -> Unit,
    vm: FaraidhViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showTooltip by remember { mutableStateOf(false) }
    val currency = remember {
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
    }
    val hasResult = state.result?.activeShares?.isNotEmpty() == true
    val heirCount = remember(state) { countActiveHeirs(state) }

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

    if (state.showInputSheet) {
        AlKhatibPartialBottomSheet(
            onDismiss = { vm.toggleInputSheet(false) },
            maxHeightFraction = 0.92f
        ) {
            FaraidhInputSheetContent(
                state = state,
                currency = currency,
                onDeceasedNameChange = vm::setDeceasedName,
                onGenderChange = vm::setGender,
                onMadhhabChange = vm::setMadhhab,
                onEstateFieldChange = vm::setEstateField,
                onHeirChange = vm::setHeirCount,
                onHeirNameChange = vm::setHeirName,
                onDone = { vm.toggleInputSheet(false) }
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset(),
        containerColor = AlKhatibColors.ScreenBackground,
        bottomBar = {
            if (hasResult) {
                Surface(
                    shadowElevation = 12.dp,
                    color = AlKhatibColors.PureWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            vm.exportPdf { uri ->
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(share, context.getString(R.string.faraidh_export_pdf))
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlKhatibColors.DeepEmerald,
                            contentColor = Color.White
                        ),
                        enabled = !state.pdfExporting
                    ) {
                        if (state.pdfExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                        } else {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.faraidh_export_pdf), fontWeight = FontWeight.SemiBold)
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

            FaraidhStatusStrip(
                netEstate = state.netEstate,
                deceasedName = state.names.deceasedName,
                heirCount = heirCount,
                hasResult = hasResult,
                currency = currency,
                onEdit = { vm.toggleInputSheet(true) }
            )

            FaraidhPillTabs(
                selectedTab = state.selectedTab,
                onTabSelected = vm::selectTab
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.selectedTab) {
                    0 -> FaraidhBreakdownTab(
                        result = state.result,
                        estateComputation = state.estateComputation,
                        names = state.names,
                        currency = currency,
                        hasHeirs = heirCount > 0,
                        onStart = { vm.toggleInputSheet(true) }
                    )
                    1 -> FaraidhSilsilahTab(
                        nodes = state.result?.silsilah.orEmpty(),
                        hasHeirs = heirCount > 0,
                        onStart = { vm.toggleInputSheet(true) }
                    )
                    2 -> FaraidhDalilTab(
                        proofs = state.proofs,
                        hasResult = hasResult,
                        onOpenVerse = onOpenVerse,
                        onOpenUrl = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) },
                        onStart = { vm.toggleInputSheet(true) }
                    )
                    else -> FaraidhGlossaryTab(glossary = state.glossary)
                }
            }
        }
    }
}

@Composable
private fun FaraidhHeader(onBack: () -> Unit, onInfo: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.Teal, AlKhatibColors.Gold.copy(alpha = 0.6f))
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 10.dp),
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
                    text = stringResource(R.string.faraidh_subtitle_short),
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
            }
            IconButton(onClick = onInfo) {
                Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.faraidh_tooltip_title), tint = AlKhatibColors.Teal)
            }
        }
    }
}

@Composable
private fun FaraidhStatusStrip(
    netEstate: String,
    deceasedName: String,
    heirCount: Int,
    hasResult: Boolean,
    currency: NumberFormat,
    onEdit: () -> Unit
) {
    val estateValue = netEstate.replace(",", "").toBigDecimalOrNull() ?: BigDecimal.ZERO
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal)
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(20.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            AlKhatibColors.DeepEmerald.copy(alpha = 0.2f),
                            AlKhatibColors.Teal.copy(alpha = 0.1f)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AlKhatibColors.MintWash),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (hasResult) Icons.Filled.CheckCircle else Icons.Outlined.Calculate,
                    contentDescription = null,
                    tint = AlKhatibColors.DeepEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deceasedName.ifBlank { stringResource(R.string.faraidh_node_deceased) },
                    style = MaterialTheme.typography.labelMedium,
                    color = AlKhatibColors.DeepEmerald,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (estateValue > BigDecimal.ZERO) currency.format(estateValue) else "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlKhatibColors.Slate900
                )
                Text(
                    text = when {
                        heirCount == 0 -> stringResource(R.string.faraidh_status_no_heirs)
                        hasResult -> stringResource(R.string.faraidh_status_ready, heirCount)
                        else -> stringResource(R.string.faraidh_status_heirs_only, heirCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
            }
            OutlinedButton(
                onClick = onEdit,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AlKhatibColors.Teal.copy(alpha = 0.4f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = AlKhatibColors.DeepEmerald)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.faraidh_edit), style = MaterialTheme.typography.labelMedium, color = AlKhatibColors.DeepEmerald)
            }
        }
    }
}

@Composable
private fun FaraidhPillTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        Triple(0, R.string.faraidh_tab_breakdown, Icons.Filled.TableChart),
        Triple(1, R.string.faraidh_tab_silsilah, Icons.Filled.AccountTree),
        Triple(2, R.string.faraidh_tab_dalil, Icons.AutoMirrored.Filled.MenuBook),
        Triple(3, R.string.faraidh_tab_glossary, Icons.Filled.School)
    )
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = AlKhatibSpacing.screenHorizontal)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (index, labelRes, icon) ->
            val selected = selectedTab == index
            val bg by animateColorAsState(
                if (selected) AlKhatibColors.DeepEmerald else AlKhatibColors.LightGrey,
                label = "tabBg"
            )
            val fg by animateColorAsState(
                if (selected) Color.White else AlKhatibColors.Slate500,
                label = "tabFg"
            )
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onTabSelected(index) },
                color = bg,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = fg,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaraidhInputSheetContent(
    state: FaraidhUiState,
    currency: NumberFormat,
    onDeceasedNameChange: (String) -> Unit,
    onGenderChange: (DeceasedGender) -> Unit,
    onMadhhabChange: (FaraidhMadhhab) -> Unit,
    onEstateFieldChange: (EstateAssetInput.() -> EstateAssetInput) -> Unit,
    onHeirChange: (HeirCountField, Int) -> Unit,
    onHeirNameChange: (HeirNameField, Int, String) -> Unit,
    onDone: () -> Unit
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = AlKhatibSpacing.screenHorizontal)
            .padding(bottom = 28.dp)
    ) {
        Text(
            stringResource(R.string.faraidh_input_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.Slate900
        )
        Text(
            stringResource(R.string.faraidh_input_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = AlKhatibColors.Slate500,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Text(stringResource(R.string.faraidh_step_madhhab), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.DeepEmerald)
        Spacer(Modifier.height(8.dp))
        FaraidhMadhhabPicker(selected = state.madhhab, onSelect = onMadhhabChange)

        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.faraidh_step_estate_detail), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.DeepEmerald)
        Spacer(Modifier.height(8.dp))
        FaraidhEstateInputSection(
            estate = state.estate,
            computation = state.estateComputation,
            currency = currency,
            onFieldChange = onEstateFieldChange
        )

        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.faraidh_step_profile), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.DeepEmerald)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.names.deceasedName,
            onValueChange = onDeceasedNameChange,
            label = { Text(stringResource(R.string.faraidh_deceased_name)) },
            placeholder = { Text(stringResource(R.string.faraidh_name_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlKhatibColors.Teal, focusedLabelColor = AlKhatibColors.DeepEmerald)
        )

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.faraidh_deceased_gender), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.DeepEmerald)
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = state.gender == DeceasedGender.MALE,
                onClick = { onGenderChange(DeceasedGender.MALE) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(activeContainerColor = AlKhatibColors.DeepEmerald, activeContentColor = Color.White)
            ) { Text(stringResource(R.string.faraidh_gender_male)) }
            SegmentedButton(
                selected = state.gender == DeceasedGender.FEMALE,
                onClick = { onGenderChange(DeceasedGender.FEMALE) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(activeContainerColor = AlKhatibColors.DeepEmerald, activeContentColor = Color.White)
            ) { Text(stringResource(R.string.faraidh_gender_female)) }
        }

        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.faraidh_step_heirs), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.DeepEmerald)
        Spacer(Modifier.height(10.dp))

        HeirSection(
            title = stringResource(R.string.faraidh_section_spouse),
            icon = Icons.Filled.FamilyRestroom
        ) {
            if (state.gender == DeceasedGender.FEMALE) {
                NamedHeirStepper(
                    label = stringResource(R.string.faraidh_heir_husband),
                    count = state.husbandCount,
                    names = listOf(state.names.husbandName),
                    min = 0,
                    max = 1,
                    onCountChange = { onHeirChange(HeirCountField.HUSBAND, it) },
                    onNameChange = { _, name -> onHeirNameChange(HeirNameField.HUSBAND, 0, name) }
                )
            } else {
                NamedHeirStepper(
                    label = stringResource(R.string.faraidh_heir_wife),
                    count = state.wifeCount,
                    names = state.names.wifeNames,
                    min = 0,
                    max = 4,
                    onCountChange = { onHeirChange(HeirCountField.WIFE, it) },
                    onNameChange = { index, name -> onHeirNameChange(HeirNameField.WIFE, index, name) }
                )
            }
        }

        HeirSection(title = stringResource(R.string.faraidh_section_parents), icon = Icons.Filled.People) {
            NamedHeirStepper(stringResource(R.string.faraidh_heir_father), state.fatherCount, listOf(state.names.fatherName), 0, 1, { onHeirChange(HeirCountField.FATHER, it) }) { _, n -> onHeirNameChange(HeirNameField.FATHER, 0, n) }
            NamedHeirStepper(stringResource(R.string.faraidh_heir_mother), state.motherCount, listOf(state.names.motherName), 0, 1, { onHeirChange(HeirCountField.MOTHER, it) }) { _, n -> onHeirNameChange(HeirNameField.MOTHER, 0, n) }
        }

        HeirSection(title = stringResource(R.string.faraidh_section_children), icon = Icons.Filled.People) {
            NamedHeirStepper(stringResource(R.string.faraidh_heir_son), state.sonCount, state.names.sonNames, onCountChange = { onHeirChange(HeirCountField.SON, it) }) { i, n -> onHeirNameChange(HeirNameField.SON, i, n) }
            NamedHeirStepper(stringResource(R.string.faraidh_heir_daughter), state.daughterCount, state.names.daughterNames, onCountChange = { onHeirChange(HeirCountField.DAUGHTER, it) }) { i, n -> onHeirNameChange(HeirNameField.DAUGHTER, i, n) }
        }

        HeirSection(title = stringResource(R.string.faraidh_section_grandchildren), icon = Icons.Filled.People) {
            NamedHeirStepper(stringResource(R.string.faraidh_heir_grandson), state.grandsonCount, state.names.grandsonNames, onCountChange = { onHeirChange(HeirCountField.GRANDSON, it) }) { i, n -> onHeirNameChange(HeirNameField.GRANDSON, i, n) }
            NamedHeirStepper(stringResource(R.string.faraidh_heir_granddaughter), state.granddaughterCount, state.names.granddaughterNames, onCountChange = { onHeirChange(HeirCountField.GRANDDAUGHTER, it) }) { i, n -> onHeirNameChange(HeirNameField.GRANDDAUGHTER, i, n) }
        }

        HeirSection(title = stringResource(R.string.faraidh_section_siblings), icon = Icons.Filled.People) {
            NamedHeirStepper(stringResource(R.string.faraidh_heir_full_brother), state.fullBrotherCount, state.names.fullBrotherNames, onCountChange = { onHeirChange(HeirCountField.FULL_BROTHER, it) }) { i, n -> onHeirNameChange(HeirNameField.FULL_BROTHER, i, n) }
            NamedHeirStepper(stringResource(R.string.faraidh_heir_full_sister), state.fullSisterCount, state.names.fullSisterNames, onCountChange = { onHeirChange(HeirCountField.FULL_SISTER, it) }) { i, n -> onHeirNameChange(HeirNameField.FULL_SISTER, i, n) }
            NamedHeirStepper(stringResource(R.string.faraidh_heir_paternal_brother), state.paternalBrotherCount, state.names.paternalBrotherNames, onCountChange = { onHeirChange(HeirCountField.PATERNAL_BROTHER, it) }) { i, n -> onHeirNameChange(HeirNameField.PATERNAL_BROTHER, i, n) }
            NamedHeirStepper(stringResource(R.string.faraidh_heir_paternal_sister), state.paternalSisterCount, state.names.paternalSisterNames, onCountChange = { onHeirChange(HeirCountField.PATERNAL_SISTER, it) }) { i, n -> onHeirNameChange(HeirNameField.PATERNAL_SISTER, i, n) }
            NamedHeirStepper(stringResource(R.string.faraidh_heir_maternal_brother), state.maternalBrotherCount, state.names.maternalBrotherNames, onCountChange = { onHeirChange(HeirCountField.MATERNAL_BROTHER, it) }) { i, n -> onHeirNameChange(HeirNameField.MATERNAL_BROTHER, i, n) }
            NamedHeirStepper(stringResource(R.string.faraidh_heir_maternal_sister), state.maternalSisterCount, state.names.maternalSisterNames, onCountChange = { onHeirChange(HeirCountField.MATERNAL_SISTER, it) }) { i, n -> onHeirNameChange(HeirNameField.MATERNAL_SISTER, i, n) }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AlKhatibColors.DeepEmerald)
        ) {
            Text(stringResource(R.string.faraidh_done), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HeirSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = AlKhatibColors.ScreenBackground
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AlKhatibColors.Teal, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.Slate800)
            }
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun NamedHeirStepper(
    label: String,
    count: Int,
    names: List<String>,
    min: Int = 0,
    max: Int = 99,
    onCountChange: (Int) -> Unit,
    onNameChange: (Int, String) -> Unit
) {
    HeirStepperRow(label, count, min, max, onCountChange)
    if (count > 0) {
        repeat(count) { index ->
            val value = names.getOrNull(index).orEmpty()
            OutlinedTextField(
                value = value,
                onValueChange = { onNameChange(index, it) },
                label = { Text(stringResource(R.string.faraidh_person_name, index + 1)) },
                placeholder = { Text(stringResource(R.string.faraidh_name_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, bottom = 6.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlKhatibColors.Teal)
            )
        }
    }
}

@Composable
private fun HeirStepperRow(label: String, count: Int, min: Int = 0, max: Int = 99, onChange: (Int) -> Unit) {
    val active = count > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) AlKhatibColors.PrayerMint else AlKhatibColors.PureWhite)
            .border(
                1.dp,
                if (active) AlKhatibColors.Teal.copy(alpha = 0.35f) else AlKhatibColors.SoftGrey.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = AlKhatibColors.Slate800)
        IconButton(onClick = { onChange((count - 1).coerceAtLeast(min)) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = null, tint = AlKhatibColors.Slate500, modifier = Modifier.size(18.dp))
        }
        Text(
            "$count",
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = if (active) AlKhatibColors.DeepEmerald else AlKhatibColors.Slate500
        )
        IconButton(onClick = { onChange((count + 1).coerceAtMost(max)) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = AlKhatibColors.DeepEmerald, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun FaraidhBreakdownTab(
    result: FaraidhResult?,
    estateComputation: EstateComputation?,
    names: FaraidhParticipantNames,
    currency: NumberFormat,
    hasHeirs: Boolean,
    onStart: () -> Unit
) {
    when {
        !hasHeirs -> EmptyState(
            icon = Icons.Outlined.Calculate,
            title = stringResource(R.string.faraidh_empty_title),
            body = stringResource(R.string.faraidh_empty_body),
            cta = stringResource(R.string.faraidh_empty_cta),
            onCta = onStart
        )
        result == null || result.activeShares.isEmpty() -> EmptyState(
            icon = Icons.Filled.People,
            title = stringResource(R.string.faraidh_empty_no_share_title),
            body = stringResource(R.string.faraidh_empty_no_share_body),
            cta = stringResource(R.string.faraidh_edit),
            onCta = onStart
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                EstateHeroCard(
                    deceasedName = result.deceased.name,
                    estate = currency.format(result.deceased.netEstate),
                    distributed = currency.format(result.totalDistributed),
                    heirCount = result.activeShares.size,
                    madhhab = result.madhhab
                )
            }
            estateComputation?.let { comp ->
                item { EstateComputationSummary(comp, currency) }
            }
            if (result.adjustment != FaraidhAdjustment.NONE) {
                item { AdjustmentBanner(result) }
            }
            item {
                BreakdownTableHeader()
            }
            items(result.activeShares, key = { "${it.type}_${it.headCount}_${it.fraction}" }) { share ->
                HeirShareCard(share, names, currency)
            }
            if (result.blockedHeirs.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Block, contentDescription = null, tint = AlKhatibColors.Danger, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.faraidh_blocked_heirs), fontWeight = FontWeight.Bold, color = AlKhatibColors.Slate800)
                    }
                }
                items(result.blockedHeirs, key = { "${it.type}_${it.reason}" }) { blocked ->
                    BlockedHeirCard(blocked)
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun EstateHeroCard(deceasedName: String, estate: String, distributed: String, heirCount: Int, madhhab: FaraidhMadhhab) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.TealDark, AlKhatibColors.ForestDark)))
            .padding(20.dp)
    ) {
        Column {
            if (deceasedName.isNotBlank()) {
                Text(deceasedName, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.faraidh_node_deceased), color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 8.dp))
            }
            Text(stringResource(R.string.faraidh_tarikah_net), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
            Text(estate, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.faraidh_madhhab_applied, madhhabLabel(madhhab)),
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.faraidh_distributed_label), color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                    Text(distributed, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.faraidh_recipients_label), color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                    Text("$heirCount", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BreakdownTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.faraidh_col_heir), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500, modifier = Modifier.weight(1.2f))
        Text(stringResource(R.string.faraidh_fraction), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
        Text(stringResource(R.string.faraidh_amount), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
private fun HeirShareCard(share: HeirShare, names: FaraidhParticipantNames, currency: NumberFormat) {
    val percentFloat = share.percentage.toFloat().coerceIn(0f, 100f) / 100f
    val perHead = if (share.headCount > 1) share.cashAmount.divide(BigDecimal(share.headCount), 2, RoundingMode.HALF_UP) else null
    val roleLabel = heirTypeLabel(share.type)
    val personNames = FaraidhNameLabels.displayList(share.type, roleLabel, names, share.headCount)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 1.dp,
        modifier = Modifier.border(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(roleLabel, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.Slate900, style = MaterialTheme.typography.labelMedium)
                    Text(
                        personNames.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.DeepEmerald,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.faraidh_heads, share.headCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                }
                ShareTypeBadge(isAsabah = share.isAsabah)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { percentFloat },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = AlKhatibColors.Teal,
                trackColor = AlKhatibColors.MintWash,
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(share.fraction.toDisplayString(), fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald, fontSize = 18.sp)
                    Text(formatFaraidhPercent(share.percentage), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(currency.format(share.cashAmount), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AlKhatibColors.Slate900)
                    perHead?.let {
                        Text(
                            stringResource(R.string.faraidh_per_head, currency.format(it)),
                            style = MaterialTheme.typography.labelSmall,
                            color = AlKhatibColors.Slate500
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareTypeBadge(isAsabah: Boolean) {
    val label = if (isAsabah) stringResource(R.string.faraidh_badge_asabah) else stringResource(R.string.faraidh_badge_furud)
    val bg = if (isAsabah) AlKhatibColors.AmberWash else AlKhatibColors.MintWash
    val fg = if (isAsabah) AlKhatibColors.GoldDeep else AlKhatibColors.DeepEmerald
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AdjustmentBanner(result: FaraidhResult) {
    val (title, body) = when (result.adjustment) {
        FaraidhAdjustment.AWL -> stringResource(R.string.faraidh_awl_title) to stringResource(R.string.faraidh_awl_applied)
        FaraidhAdjustment.RADD -> {
            val msg = if (result.madhhab == FaraidhMadhhab.HANAFI) {
                stringResource(R.string.faraidh_radd_applied_hanafi)
            } else {
                stringResource(R.string.faraidh_radd_applied)
            }
            stringResource(R.string.faraidh_radd_title) to msg
        }
        FaraidhAdjustment.NONE -> "" to ""
    }
    Surface(shape = RoundedCornerShape(16.dp), color = AlKhatibColors.AmberWash, modifier = Modifier.border(1.dp, AlKhatibColors.Gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = AlKhatibColors.GoldDeep)
            Text(body, style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.GoldDeep.copy(alpha = 0.85f), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun BlockedHeirCard(blocked: app.kamy.saatApp.domain.faraidh.BlockedHeir) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AlKhatibColors.LightGrey)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).background(AlKhatibColors.Danger.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Block, contentDescription = null, tint = AlKhatibColors.Danger, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("${heirTypeLabel(blocked.type)} ×${blocked.headCount}", fontWeight = FontWeight.Medium, color = AlKhatibColors.Slate800)
            Text(blockingReasonLabel(blocked.reason), style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
        }
    }
}

@Composable
private fun FaraidhSilsilahTab(nodes: List<SilsilahNode>, hasHeirs: Boolean, onStart: () -> Unit) {
    if (!hasHeirs || nodes.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.AccountTree,
            title = stringResource(R.string.faraidh_empty_silsilah_title),
            body = stringResource(R.string.faraidh_empty_silsilah),
            cta = stringResource(R.string.faraidh_empty_cta),
            onCta = onStart
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp)
    ) {
        item { SilsilahLegend() }
        item {
            Spacer(Modifier.height(8.dp))
            FaraidhFamilyTree(
                nodes = nodes,
                nodeTitle = { node -> silsilahNodeTitle(node) },
                nodeSubtitle = { node -> silsilahNodeSubtitle(node) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun silsilahNodeTitle(node: SilsilahNode): String {
    if (node.displayName.isNotBlank()) return node.displayName
    return stringResource(heirLabelRes(node.labelKey))
}

@Composable
private fun silsilahNodeSubtitle(node: SilsilahNode): String {
    val role = stringResource(heirLabelRes(node.labelKey))
    return when {
        node.id == "deceased" -> stringResource(R.string.faraidh_node_deceased)
        node.blocked -> stringResource(R.string.faraidh_status_blocked)
        node.inherits -> "$role · ${stringResource(R.string.faraidh_status_inherits)}"
        else -> role
    }
}

@Composable
private fun SilsilahLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AlKhatibColors.LightGrey)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendDot(color = AlKhatibColors.Teal, label = stringResource(R.string.faraidh_status_inherits))
        LegendDot(color = AlKhatibColors.Danger, label = stringResource(R.string.faraidh_status_blocked))
        LegendDot(color = AlKhatibColors.Slate500, label = stringResource(R.string.faraidh_status_absent))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.Slate500)
    }
}

@Composable
private fun FaraidhGlossaryTab(glossary: List<FaraidhGlossaryItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(stringResource(R.string.faraidh_glossary_intro), style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
        }
        if (glossary.isEmpty()) {
            item {
                Text(stringResource(R.string.faraidh_empty_glossary), style = MaterialTheme.typography.bodyMedium, color = AlKhatibColors.Slate500)
            }
        } else {
            items(glossary, key = { it.id }) { term ->
                GlossaryCard(term)
            }
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun GlossaryCard(term: FaraidhGlossaryItem) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AlKhatibColors.PureWhite,
        modifier = Modifier.border(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(term.title, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald, style = MaterialTheme.typography.titleSmall)
            term.arabic?.let { arabic ->
                Text(
                    arabic,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.Rtl, fontSize = 18.sp),
                    color = AlKhatibColors.Slate900
                )
            }
            Text(term.body, style = MaterialTheme.typography.bodyMedium, color = AlKhatibColors.Slate800, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun madhhabLabel(madhhab: FaraidhMadhhab): String = when (madhhab) {
    FaraidhMadhhab.HANAFI -> stringResource(R.string.faraidh_madhhab_hanafi)
    FaraidhMadhhab.MALIKI -> stringResource(R.string.faraidh_madhhab_maliki)
    FaraidhMadhhab.SHAFII -> stringResource(R.string.faraidh_madhhab_shafii)
    FaraidhMadhhab.HANBALI -> stringResource(R.string.faraidh_madhhab_hanbali)
}

@Composable
private fun FaraidhDalilTab(
    proofs: List<FaraidhProofItem>,
    hasResult: Boolean,
    onOpenVerse: (Int, Int) -> Unit,
    onOpenUrl: (String) -> Unit,
    onStart: () -> Unit
) {
    if (!hasResult || proofs.isEmpty()) {
        EmptyState(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            title = stringResource(R.string.faraidh_empty_dalil_title),
            body = stringResource(R.string.faraidh_empty_dalil),
            cta = stringResource(R.string.faraidh_empty_cta),
            onCta = onStart
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(stringResource(R.string.faraidh_dalil_intro), style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
        }
        items(proofs, key = { it.id }) { proof ->
            DalilCard(proof, onOpenVerse, onOpenUrl)
        }
        item { Spacer(Modifier.height(48.dp)) }
    }
}

@Composable
private fun DalilCard(proof: FaraidhProofItem, onOpenVerse: (Int, Int) -> Unit, onOpenUrl: (String) -> Unit) {
    val kindLabel = when (proof.kind) {
        FaraidhProofKind.QURAN -> stringResource(R.string.faraidh_badge_quran)
        FaraidhProofKind.HADITH -> stringResource(R.string.faraidh_badge_hadith)
        FaraidhProofKind.NOTE -> stringResource(R.string.faraidh_badge_note)
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AlKhatibColors.PureWhite,
        modifier = Modifier.border(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(8.dp), color = AlKhatibColors.MintWash) {
                    Text(kindLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = AlKhatibColors.DeepEmerald, fontWeight = FontWeight.SemiBold)
                }
                when (proof.kind) {
                    FaraidhProofKind.QURAN -> {
                        if (proof.surah != null && proof.ayah != null) {
                            TextButton(onClick = { onOpenVerse(proof.surah, proof.ayah) }) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = AlKhatibColors.Teal)
                                Spacer(Modifier.width(4.dp))
                                Text(proof.title, color = AlKhatibColors.Teal)
                            }
                        } else Text(proof.title, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
                    }
                    FaraidhProofKind.HADITH -> {
                        proof.externalUrl?.let { url ->
                            TextButton(onClick = { onOpenUrl(url) }) {
                                Text(proof.title, color = AlKhatibColors.BlueLink)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = AlKhatibColors.BlueLink)
                            }
                        } ?: Text(proof.title, fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
                    }
                    FaraidhProofKind.NOTE -> Text(proof.title, fontWeight = FontWeight.Bold, color = AlKhatibColors.GoldDeep)
                }
            }
            proof.arabic?.let { arabic ->
                Text(
                    arabic,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, textDirection = TextDirection.Rtl, lineHeight = 32.sp),
                    color = AlKhatibColors.Slate900
                )
                HorizontalDivider(color = AlKhatibColors.SoftGrey.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
            }
            Text(proof.body, style = MaterialTheme.typography.bodyMedium, color = AlKhatibColors.Slate800, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    cta: String,
    onCta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(AlKhatibColors.MintWash, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AlKhatibColors.DeepEmerald, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AlKhatibColors.Slate900, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = AlKhatibColors.Slate500, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onCta,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AlKhatibColors.DeepEmerald)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(cta, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun countActiveHeirs(state: FaraidhUiState): Int = listOf(
    state.husbandCount, state.wifeCount, state.fatherCount, state.motherCount,
    state.sonCount, state.daughterCount, state.grandsonCount, state.granddaughterCount,
    state.fullBrotherCount, state.fullSisterCount,
    state.paternalBrotherCount, state.paternalSisterCount,
    state.maternalBrotherCount, state.maternalSisterCount
).count { it > 0 }

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
