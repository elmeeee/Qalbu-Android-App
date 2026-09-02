package app.kamy.saatApp.features.tools.fidyah

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.components.SaatPartialBottomSheet
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.model.FidyahMadhhab
import app.kamy.saatApp.domain.model.FidyahReason
import app.kamy.saatApp.domain.model.FidyahRecord
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import java.text.NumberFormat
import java.util.Locale

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FidyahCalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: FidyahViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val currentLang = AppLanguageStore.from(context).current()

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val screenTitle = stringResource(R.string.fidyah_title)
    val subtitle = stringResource(R.string.fidyah_subtitle)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SaatColors.HomeBg)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .imePadding()
        ) {
            // Unified Sticky Premium Header Bar
            app.kamy.saatApp.features.tools.components.SpiritualToolTopBar(
                title = screenTitle,
                subtitle = subtitle,
                onBack = onNavigateBack
            )

        HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SaatSpacing.screenHorizontal, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Madhhab Selector Tabs
            item {
                MadhhabSelectorSection(
                    selectedMadhhab = state.selectedMadhhab,
                    onSelectMadhhab = { viewModel.setMadhhab(it) },
                    language = currentLang
                )
            }

            // 2. Fidyah Reason Selector (Compact Dropdown Selector Card)
            item {
                FidyahReasonSection(
                    selectedReason = state.selectedReason,
                    onSelectReason = { viewModel.setReason(it) },
                    language = currentLang
                )
            }

            // 3. Inputs (Missed Days & Delayed Years)
            item {
                FidyahInputsSection(
                    missedDays = state.missedDays,
                    delayedYears = state.delayedYears,
                    showYearsInput = state.selectedReason == FidyahReason.LATE_QADHA || state.selectedMadhhab == FidyahMadhhab.SYAFII,
                    onDaysChange = { viewModel.setMissedDays(it) },
                    onYearsChange = { viewModel.setDelayedYears(it) },
                    pricePerDay = state.pricePerDay,
                    userCurrencySymbol = state.userCurrencySymbol,
                    onPriceChange = { viewModel.setPricePerDay(it) },
                    hijriYear = state.selectedHijriYear,
                    onHijriYearChange = { viewModel.setHijriYear(it) },
                    language = currentLang
                )
            }

            // 4. Calculation Summary Result Card
            item {
                state.calculationResult?.let { res ->
                    FidyahResultCard(
                        result = res,
                        pricePerDay = state.pricePerDay,
                        userCurrencySymbol = state.userCurrencySymbol,
                        language = currentLang,
                        onSaveRecord = { viewModel.saveCurrentCalculation() },
                        onOpenDua = { viewModel.setShowDuaDialog(true) }
                    )
                }
            }

            // 4b. Dalil Al-Qur'an & Hadits Evidences Card
            item {
                FidyahDalilSection(language = currentLang)
            }

            // 4c. Fiqh Timing & Notification Rules Guide Card
            item {
                FidyahTimingRulesSection(language = currentLang)
            }

            // 5. Payment Log History
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_faraidh_doc),
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (currentLang) {
                            AppLanguage.MALAY -> "Rekod & Log Bayaran Fidyah"
                            AppLanguage.ENGLISH -> "Fidyah Payment Log & Records"
                            else -> "Catatan & Riwayat Pembayaran Fidyah"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
            }

            if (state.records.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SaatColors.PureWhite,
                        border = BorderStroke(1.dp, SaatColors.SoftGrey)
                    ) {
                        Text(
                            text = when (currentLang) {
                                AppLanguage.MALAY -> "Belum ada rekod fidyah / qada tersimpan. Tekan 'Simpan Rekod' di atas."
                                AppLanguage.ENGLISH -> "No saved fidyah / qadha records yet. Tap 'Save Record' above."
                                else -> "Belum ada catatan fidyah / qadha tersimpan. Tekan 'Simpan Catatan' di atas."
                            },
                            modifier = Modifier.padding(20.dp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(state.records, key = { it.id }) { record ->
                    FidyahRecordItem(
                        record = record,
                        pricePerDay = state.pricePerDay,
                        language = currentLang,
                        onTogglePaid = { viewModel.toggleRecordPaid(record) },
                        onIncrementQadha = { viewModel.incrementQadhaDay(record) },
                        onToggleQadhaCompleted = { viewModel.toggleQadhaCompleted(record) },
                        onDelete = { viewModel.deleteRecord(record.id) }
                    )
                }
            }
        }
        }

        if (state.showDuaDialog) {
            FidyahDuaModalSheet(
                language = currentLang,
                onDismiss = { viewModel.setShowDuaDialog(false) }
            )
        }

        // Custom Floating Pill Toast / Snackbar matching Prayer Tracker & TodayScreen
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) { snackbarData ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SaatColors.PureWhite,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    SaatColors.MintWash,
                                    SaatColors.PureWhite
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SaatColors.DeepEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.Slate900,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MadhhabSelectorSection(
    selectedMadhhab: FidyahMadhhab,
    onSelectMadhhab: (FidyahMadhhab) -> Unit,
    language: AppLanguage
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_madhab_custom),
                contentDescription = null,
                tint = SaatColors.DeepEmerald,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (language) {
                    AppLanguage.MALAY -> "Pilih Mazhab Fikrah:"
                    AppLanguage.ENGLISH -> "Select Madhhab:"
                    else -> "Pilih Mazhab Fiqih:"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FidyahMadhhab.entries.toTypedArray()) { m ->
                val isSelected = m == selectedMadhhab
                val title = when (language) {
                    AppLanguage.MALAY -> m.titleMs
                    AppLanguage.ENGLISH -> m.titleEn
                    else -> m.titleId
                }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectMadhhab(m) },
                    color = if (isSelected) SaatColors.DeepEmerald else SaatColors.PureWhite,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) SaatColors.GoldDeep else SaatColors.SoftGrey
                    ),
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun FidyahReasonSection(
    selectedReason: FidyahReason,
    onSelectReason: (FidyahReason) -> Unit,
    language: AppLanguage
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val currentTitle = when (language) {
        AppLanguage.MALAY -> selectedReason.titleMs
        AppLanguage.ENGLISH -> selectedReason.titleEn
        else -> selectedReason.titleId
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_faraidh_breakdown),
                contentDescription = null,
                tint = SaatColors.DeepEmerald,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (language) {
                    AppLanguage.MALAY -> "Sebab Meninggalkan Puasa:"
                    AppLanguage.ENGLISH -> "Reason for Missed Fast:"
                    else -> "Sebab / Alasan Meninggalkan Puasa:"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { isDropdownExpanded = !isDropdownExpanded },
                shape = RoundedCornerShape(14.dp),
                color = SaatColors.PureWhite,
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.4f)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Select Reason",
                        tint = SaatColors.DeepEmerald
                    )
                }
            }

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(SaatColors.PureWhite)
            ) {
                FidyahReason.entries.forEach { r ->
                    val isSelected = r == selectedReason
                    val itemTitle = when (language) {
                        AppLanguage.MALAY -> r.titleMs
                        AppLanguage.ENGLISH -> r.titleEn
                        else -> r.titleId
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemTitle,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SaatColors.DeepEmerald else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSelectReason(r)
                            isDropdownExpanded = false
                        },
                        leadingIcon = {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = SaatColors.DeepEmerald)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FidyahInputsSection(
    missedDays: Int,
    delayedYears: Int,
    showYearsInput: Boolean,
    onDaysChange: (Int) -> Unit,
    onYearsChange: (Int) -> Unit,
    pricePerDay: Double,
    userCurrencySymbol: String,
    onPriceChange: (Double) -> Unit,
    hijriYear: String,
    onHijriYearChange: (String) -> Unit,
    language: AppLanguage
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Days Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "Jumlah Hari Puasa Ditinggalkan"
                            AppLanguage.ENGLISH -> "Missed Fast Days"
                            else -> "Jumlah Hari Puasa Ditinggalkan"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$missedDays hari",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onDaysChange(missedDays - 1) },
                        modifier = Modifier.background(SaatColors.SageMist, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = SaatColors.DeepEmerald)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onDaysChange(missedDays + 1) },
                        modifier = Modifier.background(SaatColors.DeepEmerald, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                    }
                }
            }

            // Years Stepper (if applicable)
            if (showYearsInput) {
                HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                AppLanguage.MALAY -> "Kelewatan Tahun Ramadan"
                                AppLanguage.ENGLISH -> "Delayed Ramadan Years"
                                else -> "Keterlambatan Tahun Ramadan"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$delayedYears tahun",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onYearsChange(delayedYears - 1) },
                            modifier = Modifier.background(SaatColors.SageMist, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = SaatColors.DeepEmerald)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onYearsChange(delayedYears + 1) },
                            modifier = Modifier.background(SaatColors.DeepEmerald, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                        }
                    }
                }
            }

            HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

            val focusManager = LocalFocusManager.current

            // Hijri Year Label
            OutlinedTextField(
                value = hijriYear,
                onValueChange = onHijriYearChange,
                label = {
                    Text(
                        when (language) {
                            AppLanguage.MALAY -> "Tahun Hijriah (cth: 1447 H)"
                            AppLanguage.ENGLISH -> "Hijri Year (e.g. 1447 H)"
                            else -> "Tahun Hijriah (contoh: 1447 H)"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaatColors.DeepEmerald,
                    focusedLabelColor = SaatColors.DeepEmerald,
                    unfocusedBorderColor = SaatColors.SoftGrey
                )
            )

            // Price per day input
            OutlinedTextField(
                value = if (pricePerDay > 0) pricePerDay.toLong().toString() else "",
                onValueChange = { str ->
                    val d = str.toDoubleOrNull() ?: 0.0
                    onPriceChange(d)
                },
                label = {
                    Text(
                        when (language) {
                            AppLanguage.MALAY -> "Kadar Fidyah ($userCurrencySymbol / hari)"
                            AppLanguage.ENGLISH -> "Fidyah Rate ($userCurrencySymbol / day)"
                            else -> "Nominal / Kadar Fidyah ($userCurrencySymbol / hari)"
                        }
                    )
                },
                prefix = { Text("$userCurrencySymbol ", fontWeight = FontWeight.Bold, color = SaatColors.DeepEmerald) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaatColors.DeepEmerald,
                    focusedLabelColor = SaatColors.DeepEmerald,
                    unfocusedBorderColor = SaatColors.SoftGrey
                )
            )
        }
    }
}

@Composable
private fun FidyahResultCard(
    result: app.kamy.saatApp.domain.model.FidyahCalculationResult,
    pricePerDay: Double,
    userCurrencySymbol: String,
    language: AppLanguage,
    onSaveRecord: () -> Unit,
    onOpenDua: () -> Unit
) {
    val totalCurrency = result.totalFidyahDaysMultiplier * pricePerDay
    val formattedTotal = NumberFormat.getNumberInstance(Locale.US).format(totalCurrency.toLong())

    val explanation = when (language) {
        AppLanguage.MALAY -> result.fiqhExplanationMs
        AppLanguage.ENGLISH -> result.fiqhExplanationEn
        else -> result.fiqhExplanationId
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SaatColors.DeepEmerald,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.MALAY -> "Ringkasan Hasil Fidyah"
                        AppLanguage.ENGLISH -> "Fidyah Calculation Summary"
                        else -> "Hasil Perhitungan Fidyah"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SaatColors.GoldDeep
                ) {
                    Text(
                        text = result.madhhab.titleId,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            if (result.isFidyahRequired) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (language) {
                                AppLanguage.MALAY -> "Total Kadar Fidyah"
                                AppLanguage.ENGLISH -> "Total Fidyah Payable"
                                else -> "Total Pembayaran Fidyah"
                            },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$userCurrencySymbol $formattedTotal",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SaatColors.GoldBright
                        )
                    }

                    // Staple Rice Icon & Weight Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_rice),
                                contentDescription = "Rice",
                                tint = SaatColors.GoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.MALAY -> "Setara Beras"
                                        AppLanguage.ENGLISH -> "Rice Equiv."
                                        else -> "Setara Beras"
                                    },
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "%.2f kg".format(result.riceWeightKg),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            if (result.requiredQadhaDays > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SaatColors.GoldBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (language) {
                                AppLanguage.MALAY -> "Wajib Qada Puasa: ${result.requiredQadhaDays} Hari"
                                AppLanguage.ENGLISH -> "Required Qadha Fast: ${result.requiredQadhaDays} Days"
                                else -> "Wajib Qadha Puasa: ${result.requiredQadhaDays} Hari"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                text = explanation,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = 17.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveRecord,
                    colors = ButtonDefaults.buttonColors(containerColor = SaatColors.GoldDeep, contentColor = Color.Black),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_faraidh_save),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "Simpan Rekod"
                            AppLanguage.ENGLISH -> "Save Record"
                            else -> "Simpan Catatan"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onOpenDua,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dua),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "Doa & Niat"
                            AppLanguage.ENGLISH -> "Fidyah Duas"
                            else -> "Doa & Niat"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FidyahRecordItem(
    record: FidyahRecord,
    pricePerDay: Double,
    language: AppLanguage,
    onTogglePaid: () -> Unit,
    onIncrementQadha: () -> Unit,
    onToggleQadhaCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    val isFidyahRequired = record.reason != FidyahReason.HAID_NIFAS &&
            record.reason != FidyahReason.SICK_TEMPORARY &&
            record.reason != FidyahReason.TRAVELER_MUSAFIR &&
            record.reason != FidyahReason.PREGNANT_NURSING_SELF

    val isQadhaRequired = record.reason != FidyahReason.ELDERLY_CHRONIC && record.reason != FidyahReason.DECEASED_BY_HEIR

    val totalAmount = record.missedDays * record.delayedYears * pricePerDay
    val formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(totalAmount.toLong())

    val reasonTitle = when (language) {
        AppLanguage.MALAY -> record.reason.titleMs
        AppLanguage.ENGLISH -> record.reason.titleEn
        else -> record.reason.titleId
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.hijriYear,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isFidyahRequired) {
                        val paidTag = when (language) {
                            AppLanguage.MALAY -> if (record.isFullyPaid) "FIDYAH JELAS" else "FIDYAH BELUM"
                            AppLanguage.ENGLISH -> if (record.isFullyPaid) "FIDYAH PAID" else "FIDYAH PENDING"
                            else -> if (record.isFullyPaid) "FIDYAH LUNAS" else "FIDYAH BELUM"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (record.isFullyPaid) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = paidTag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (record.isFullyPaid) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }
                }
                Row {
                    if (isFidyahRequired) {
                        IconButton(onClick = onTogglePaid) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Toggle Paid",
                                tint = if (record.isFullyPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Record",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = reasonTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            val daysUnit = when (language) {
                AppLanguage.ENGLISH -> "days"
                else -> "hari"
            }
            val missedFastLabel = when (language) {
                AppLanguage.MALAY -> "Hutang Puasa: ${record.missedDays} hari"
                AppLanguage.ENGLISH -> "Missed Fast: ${record.missedDays} days"
                else -> "Utang Puasa: ${record.missedDays} hari"
            }
            Text(
                text = if (isFidyahRequired)
                    "${record.missedDays} $daysUnit • ${record.madhhab.titleId} • ${record.currencySymbol} $formattedAmount"
                else
                    missedFastLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Qadha Fasting Tracker Section
            if (isQadhaRequired) {
                HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (language) {
                                AppLanguage.MALAY -> "Kemajuan Qada Puasa:"
                                AppLanguage.ENGLISH -> "Qadha Fast Progress:"
                                else -> "Progres Qadha Puasa:"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        Text(
                            text = when (language) {
                                AppLanguage.ENGLISH -> "${record.completedQadhaDays} / ${record.missedDays} days completed"
                                else -> "${record.completedQadhaDays} / ${record.missedDays} hari selesai"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (record.isQadhaCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = onIncrementQadha,
                        enabled = !record.isQadhaCompleted,
                        colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.MALAY -> "+1 Qada Hari Ini"
                                AppLanguage.ENGLISH -> "+1 Qadha Today"
                                else -> "+1 Qadha Hari Ini"
                            },
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FidyahDuaModalSheet(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    SaatPartialBottomSheet(
        onDismiss = onDismiss,
        maxHeightFraction = 0.85f,
        scrollContent = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_dua),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = when (language) {
                        AppLanguage.MALAY -> "Doa & Niat Pembayaran Fidyah"
                        AppLanguage.ENGLISH -> "Fidyah Payment Duas & Niyyah"
                        else -> "Doa & Niat Pembayaran Fidyah"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
            }

            HorizontalDivider(color = SaatColors.SoftGrey)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Item 1: Niat Fidyah Diri Sendiri
                DuaCardItem(
                    title = when (language) {
                        AppLanguage.MALAY -> "1. Niat Fidyah Diri Sendiri"
                        AppLanguage.ENGLISH -> "1. Intention (Niyyah) for Oneself"
                        else -> "1. Niat Membayar Fidyah Diri Sendiri"
                    },
                    arabic = "نَوَيْتُ أَنْ أُخْرِجَ هَذِهِ الْفِدْيَةَ عَنْ إِفْطَارِ صَوْمِ رَمَضَانَ فَرْضًا لِلَّهِ تَعَالَى",
                    latin = "Nawaitu an ukhrija hadhihi al-fidyata 'an iftari saumi ramadhana fardhan lillahi ta'ala.",
                    translation = when (language) {
                        AppLanguage.MALAY -> "Niat saya mengeluarkan fidyah ini kerana meninggalkan puasa Ramadan, fardu kerana Allah Ta'ala."
                        AppLanguage.ENGLISH -> "I intend to pay this fidyah for missing the Ramadan fast, as an obligation for Allah Almighty."
                        else -> "Niat saya mengeluarkan fidyah ini karena meninggalkan puasa Ramadan, fardhu karena Allah Ta'ala."
                    }
                )

                // Item 2: Niat Fidyah Orang Lain / Almarhum
                DuaCardItem(
                    title = when (language) {
                        AppLanguage.MALAY -> "2. Niat Fidyah Mewakili Arwah / Orang Tua"
                        AppLanguage.ENGLISH -> "2. Intention (Niyyah) for Deceased / Parents"
                        else -> "2. Niat Fidyah Mewakili Almarhum / Orang Tua"
                    },
                    arabic = "نَوَيْتُ أَنْ أُخْرِجَ هَذِهِ الْفِدْيَةَ عَنْ صَوْمِ رَمَضَانَ فُلَانِ بْنِ فُلَانٍ فَرْضًا لِلَّهِ تَعَالَى",
                    latin = "Nawaitu an ukhrija hadhihi al-fidyata 'an saumi ramadhana (Nama Almarhum/ah) fardhan lillahi ta'ala.",
                    translation = when (language) {
                        AppLanguage.MALAY -> "Niat saya mengeluarkan fidyah ini daripada puasa Ramadan (Nama Arwah), fardu kerana Allah Ta'ala."
                        AppLanguage.ENGLISH -> "I intend to pay this fidyah on behalf of the Ramadan fast of (Name), obligatory for Allah Almighty."
                        else -> "Niat saya mengeluarkan fidyah ini dari puasa Ramadan (Nama Almarhum/ah), fardhu karena Allah Ta'ala."
                    }
                )

                // Item 3: Doa Penerimaan Fidyah
                DuaCardItem(
                    title = when (language) {
                        AppLanguage.MALAY -> "3. Doa Keberkahan Pembayaran Fidyah"
                        AppLanguage.ENGLISH -> "3. Supplication for Blessing in Fidyah"
                        else -> "3. Doa Keberkahan Pembayaran Fidyah"
                    },
                    arabic = "بَارَكَ اللَّهُ لَكَ فِي مَالِكَ وَأَهْلِكَ وَتَقَبَّلَ اللَّهُ مِنْكَ صَالِحَ الْأَعْمَالِ",
                    latin = "Barakallahu laka fi malika wa ahlika wa taqabbalallahu minka shalihal a'mal.",
                    translation = when (language) {
                        AppLanguage.MALAY -> "Semoga Allah memberkati harta dan keluargamu, serta menerima amal kebaikanmu."
                        AppLanguage.ENGLISH -> "May Allah bless your wealth and family, and accept your righteous deeds."
                        else -> "Semoga Allah memberkahi harta dan keluargamu, serta menerima amal kebaikanmu."
                    }
                )

                // Item 4: Niat Puasa Qadha Ramadan
                DuaCardItem(
                    title = when (language) {
                        AppLanguage.MALAY -> "4. Niat Puasa Qada Ramadan"
                        AppLanguage.ENGLISH -> "4. Niyyah for Qadha Ramadan Fast"
                        else -> "4. Niat Puasa Qadha Ramadan"
                    },
                    arabic = "نَوَيْتُ صَوْمَ غَدٍ عَنْ قَضَاءِ فَرْضِ شَهْرِ رَمَضَانَ لِلَّهِ تَعَالَى",
                    latin = "Nawaitu shauma ghadin 'an qadha'i fardhi shahri ramadhana lillahi ta'ala.",
                    translation = when (language) {
                        AppLanguage.MALAY -> "Niat saya puasa esok hari kerana meng-Qada fardu bulan Ramadan kerana Allah Ta'ala."
                        AppLanguage.ENGLISH -> "I intend to fast tomorrow to make up for the obligatory fast of Ramadan for Allah Almighty."
                        else -> "Aku berniat puasa esok hari untuk meng-qadha fardhu bulan Ramadan karena Allah Ta'ala."
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "Tutup", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DuaCardItem(
    title: String,
    arabic: String,
    latin: String,
    translation: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.DeepEmerald.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_dua),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
            }
            Text(
                text = arabic,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 28.sp
            )
            Text(
                text = latin,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontStyle = FontStyle.Italic
            )
            Text(
                text = translation,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FidyahTimingRulesSection(
    language: AppLanguage
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        border = BorderStroke(1.dp, SaatColors.SoftGrey),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_remainders_custom),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        AppLanguage.MALAY -> "Panduan Fikrah: Waktu Sah Bayar Fidyah"
                        AppLanguage.ENGLISH -> "Fiqh Guide: Valid Fidyah Payment Timing"
                        else -> "Panduan Fiqih: Waktu Sah & Kebolehan Bayar Fidyah"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_custom),
                        contentDescription = "Valid",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "SAH: Setiap hari Ramadan selepas terbenam matahari (Maghrib) bagi hari yang ditinggalkan."
                            AppLanguage.ENGLISH -> "VALID: Every Ramadan day after sunset (Maghrib) for that missed day."
                            else -> "SAH: Setiap hari Ramadan setelah terbenam matahari (Maghrib) untuk hari puasa yang telah ditinggalkan."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_custom),
                        contentDescription = "Valid",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "SAH: Dikumpulkan & dibayar sekali gus di akhir bulan Ramadan."
                            AppLanguage.ENGLISH -> "VALID: Collected & paid altogether at the end of Ramadan."
                            else -> "SAH: Dikumpulkan & dibayar sekaligus di akhir bulan Ramadan (tgl 29/30)."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_custom),
                        contentDescription = "Valid",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "SAH: Dibayarkan selepas Ramadan (sepanjang tahun) sebelum Ramadan berikutnya."
                            AppLanguage.ENGLISH -> "VALID: Paid after Ramadan (throughout the year) before next Ramadan."
                            else -> "SAH: Dibayarkan setelah Ramadan (sepanjang tahun) sebelum Ramadan berikutnya."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.Block,
                        contentDescription = "Invalid",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "TIDAK SAH: Mendahului sebelum Ramadan tiba (Ta'jil sebelum Ramadan - Ijma' 4 Mazhab)."
                            AppLanguage.ENGLISH -> "INVALID: Paying before Ramadan arrives (Consensus of 4 Madhhabs)."
                            else -> "TIDAK SAH: Mendahului sebelum bulan Ramadan tiba (Ta'jil sebelum Ramadan - Kesepakatan 4 Mazhab)."
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        lineHeight = 16.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SaatColors.DeepEmerald.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_notification_custom),
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "Notifikasi pengingat fidyah hanya dihantar selepas Maghrib atau sebelum bulan Ramadan baru tiba untuk memastikan keabsahan fiqh."
                            AppLanguage.ENGLISH -> "Fidyah reminder notifications are sent after Maghrib or before the new Ramadan to align with Fiqh rules."
                            else -> "Notifikasi pengingat fidyah hanya dikirim setelah Maghrib atau menjelang Ramadan baru (bulan Sya'ban) agar selalu sesuai hukum Fiqih."
                        },
                        fontSize = 11.sp,
                        color = SaatColors.DeepEmerald,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FidyahDalilSection(
    language: AppLanguage
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SaatColors.PureWhite,
        border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.4f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_faraidh_dalil),
                        contentDescription = null,
                        tint = SaatColors.GoldDeep,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.MALAY -> "Dalil Al-Quran & Hadis Fidyah"
                            AppLanguage.ENGLISH -> "Quran & Hadith Evidences for Fidyah"
                            else -> "Dalil Al-Qur'an & Hadits Hukum Fidyah"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // 1. Surah Al-Baqarah: 184
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SaatColors.DeepEmerald.copy(alpha = 0.05f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "📖 Q.S. Al-Baqarah (2) : 184",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                            Text(
                                text = "فَمَن كَانَ مِنكُم مَّرِيضًا أَوْ عَلَىٰ سَفَرٍ فَعِدَّةٌ مِّنْ أَيَّامٍ أُخَرَ ۚ وَعَلَى الَّذِينَ يُطِيقُونَهُ فِدْيَةٌ طَعَامُ مِسْكِينٍ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 26.sp
                            )
                            Text(
                                text = "Faman kana minkum maridhan aw 'ala safari fa'iddatum min ayyamin ukhar. Wa 'alalladhina yutiqunahu fidyatun tha'amu miskin.",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.MALAY -> "\"Maka sesiapa di antara kamu yang sakit atau dalam musafir, maka (wajiblah) sebanyak hari yang ditinggalkan itu pada hari-hari yang lain. Dan wajib atas orang-orang yang berat menjalankannya membayar fidyah, iaitu memberi makan seorang miskin.\""
                                    AppLanguage.ENGLISH -> "\"So whoever among you is ill or on a journey - then an equal number of other days. And upon those who can afford it with hardship - a ransom [fidyah] of feeding a poor person.\""
                                    else -> "\"Maka barangsiapa di antara kamu ada yang sakit atau dalam perjalanan, maka (wajiblah baginya berpuasa) sebanyak hari yang ditinggalkan itu pada hari-hari yang lain. Dan wajib bagi orang-orang yang berat menjalankannya membayar fidyah, yaitu memberi makan seorang miskin.\""
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2. Hadits Shahih Al-Bukhari No. 4505
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SaatColors.DeepEmerald.copy(alpha = 0.05f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "📜 H.R. Al-Bukhari No. 4505 (Athar Ibnu Abbas R.A.)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                            Text(
                                text = "لَيْسَتْ بِمَنْسُوخَةٍ، هُوَ الشَّيْخُ الْكَبِيرُ وَالْمَرْأَةُ الْكَبِيرَةُ لاَ يَسْتَطِيعَانِ أَنْ يَصُومَا، فَيُطْعِمَانِ مَكَانَ كُلِّ يَوْمٍ مِسْكِينًا",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 24.sp
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.MALAY -> "\"Ayat ini tidak dimansuhkan. Ia ditujukan kepada lelaki dan wanita tua yang tidak mampu berpuasa, maka mereka memberi makan seorang miskin bagi setiap hari.\""
                                    AppLanguage.ENGLISH -> "\"This verse is not abrogated. It applies to elderly men and women who cannot fast, so they feed a poor person for every missed day.\""
                                    else -> "\"Ayat ini tidak di-mansukh. Ayat ini berlaku untuk laki-laki dan wanita tua yang tidak mampu lagi berpuasa, maka keduanya memberi makan seorang miskin untuk setiap hari puasa.\""
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Hadits Abu Dawud No. 2318
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SaatColors.DeepEmerald.copy(alpha = 0.05f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "📜 H.R. Abu Dawud No. 2318 & At-Tirmidzi No. 715",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald
                            )
                            Text(
                                text = "إِنَّ اللَّهَ عَزَّ وَجَلَّ وَضَعَ عَنِ الْمُسَافِرِ وَالْحَامِلِ وَالْمُرْضِعِ الصَّوْمَ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 24.sp
                            )
                            Text(
                                text = when (language) {
                                    AppLanguage.MALAY -> "\"Sesungguhnya Allah meringankan kewajipan puasa bagi musafir, wanita hamil, dan wanita yang menyusukan anak.\""
                                    AppLanguage.ENGLISH -> "\"Indeed, Allah has relieved the traveler, pregnant women, and nursing mothers from the obligation of fasting.\""
                                    else -> "\"Sesungguhnya Allah menggugurkan kewajiban puasa bagi musafir, wanita hamil, dan wanita yang menyusui.\""
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
