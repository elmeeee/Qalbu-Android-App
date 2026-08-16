package app.kamy.saatApp.features.tools.fidyah

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.FidyahMadhhab
import app.kamy.saatApp.domain.model.FidyahReason
import app.kamy.saatApp.domain.model.FidyahRecord
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FidyahCalculatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: FidyahViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentLang = AppLanguageStore.from(context).current()

    val screenTitle = when (currentLang) {
        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Kalkulator & Jejak Fidyah (4 Mazhab)"
        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Fidyah Calculator & Tracker (4 Madhhabs)"
        else -> "Kalkulator & Tracker Fidyah (4 Mazhab)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SaatColors.DeepEmerald
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setShowDuaDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Duas",
                            tint = SaatColors.DeepEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
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

            // 2. Fidyah Reason Selector
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
                Text(
                    text = when (currentLang) {
                        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Rekod & Log Bayaran Fidyah"
                        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Fidyah Payment Log & Records"
                        else -> "Catatan & Riwayat Pembayaran Fidyah"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
            }

            if (state.records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = when (currentLang) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Belum ada rekod fidyah / qada tersimpan. Tekan 'Simpan Rekod' di atas."
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "No saved fidyah / qadha records yet. Tap 'Save Record' above."
                                else -> "Belum ada catatan fidyah / qadha tersimpan. Tekan 'Simpan Catatan' di atas."
                            },
                            modifier = Modifier.padding(16.dp),
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
        FidyahDuaModalDialog(
            language = currentLang,
            onDismiss = { viewModel.setShowDuaDialog(false) }
        )
    }
}

@Composable
private fun MadhhabSelectorSection(
    selectedMadhhab: FidyahMadhhab,
    onSelectMadhhab: (FidyahMadhhab) -> Unit,
    language: app.kamy.saatApp.core.locale.AppLanguage
) {
    Column {
        Text(
            text = when (language) {
                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Pilih Mazhab Fikrah:"
                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Select Madhhab:"
                else -> "Pilih Mazhab Fiqih:"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SaatColors.DeepEmerald
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FidyahMadhhab.entries.toTypedArray()) { m ->
                val isSelected = m == selectedMadhhab
                val title = when (language) {
                    app.kamy.saatApp.core.locale.AppLanguage.MALAY -> m.titleMs
                    app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> m.titleEn
                    else -> m.titleId
                }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectMadhhab(m) },
                    color = if (isSelected) SaatColors.DeepEmerald else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = if (isSelected) BorderStroke(1.dp, SaatColors.GoldDeep) else null
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
    language: app.kamy.saatApp.core.locale.AppLanguage
) {
    Column {
        Text(
            text = when (language) {
                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Sebab Meninggalkan Puasa:"
                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Reason for Missed Fast:"
                else -> "Sebab / Alasan Meninggalkan Puasa:"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SaatColors.DeepEmerald
        )
        Spacer(modifier = Modifier.height(8.dp))
        FidyahReason.entries.forEach { r ->
            val isSelected = r == selectedReason
            val title = when (language) {
                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> r.titleMs
                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> r.titleEn
                else -> r.titleId
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectReason(r) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SaatColors.DeepEmerald.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) SaatColors.DeepEmerald else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectReason(r) },
                        colors = RadioButtonDefaults.colors(selectedColor = SaatColors.DeepEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
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
    language: app.kamy.saatApp.core.locale.AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Jumlah Hari Puasa Ditinggalkan"
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Missed Fast Days"
                            else -> "Jumlah Hari Puasa Ditinggalkan"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$missedDays hari",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onDaysChange(missedDays - 1) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Kelewatan Tahun Ramadan"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Delayed Ramadan Years"
                                else -> "Keterlambatan Tahun Ramadan"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$delayedYears tahun",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onYearsChange(delayedYears - 1) },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
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

            // Hijri Year Label
            OutlinedTextField(
                value = hijriYear,
                onValueChange = onHijriYearChange,
                label = {
                    Text(
                        when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Tahun Hijriah (cth: 1447 H)"
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Hijri Year (e.g. 1447 H)"
                            else -> "Tahun Hijriah (contoh: 1447 H)"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaatColors.DeepEmerald,
                    focusedLabelColor = SaatColors.DeepEmerald
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
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Kadar Fidyah ($userCurrencySymbol / hari)"
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Fidyah Rate ($userCurrencySymbol / day)"
                            else -> "Nominal / Kadar Fidyah ($userCurrencySymbol / hari)"
                        }
                    )
                },
                prefix = { Text("$userCurrencySymbol ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaatColors.DeepEmerald,
                    focusedLabelColor = SaatColors.DeepEmerald
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
    language: app.kamy.saatApp.core.locale.AppLanguage,
    onSaveRecord: () -> Unit,
    onOpenDua: () -> Unit
) {
    val totalCurrency = result.totalFidyahDaysMultiplier * pricePerDay
    val formattedTotal = NumberFormat.getNumberInstance(Locale.US).format(totalCurrency.toLong())

    val explanation = when (language) {
        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> result.fiqhExplanationMs
        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> result.fiqhExplanationEn
        else -> result.fiqhExplanationId
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SaatColors.DeepEmerald),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (language) {
                        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Ringkasan Hasil Fidyah"
                        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Fidyah Calculation Summary"
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Total Kadar Fidyah"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Total Fidyah Payable"
                                else -> "Total Pembayaran Fidyah"
                            },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$userCurrencySymbol $formattedTotal",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.GoldDeep
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Setara Beras"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Staple Rice Equiv."
                                else -> "Setara Beras (Makanan Pokok)"
                            },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "%.2f kg".format(result.riceWeightKg),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (result.requiredQadhaDays > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SaatColors.GoldDeep)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Wajib Qada Puasa: ${result.requiredQadhaDays} Hari"
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Required Qadha Fast: ${result.requiredQadhaDays} Days"
                            else -> "Wajib Qadha Puasa: ${result.requiredQadhaDays} Hari"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = explanation,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveRecord,
                    colors = ButtonDefaults.buttonColors(containerColor = SaatColors.GoldDeep, contentColor = Color.Black),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Simpan Rekod"
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Save Record"
                            else -> "Simpan Catatan"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(
                    onClick = onOpenDua,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Doa & Niat Fidyah"
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Fidyah Duas & Niyyah"
                            else -> "Doa & Niat Fidyah"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
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
    language: app.kamy.saatApp.core.locale.AppLanguage,
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
        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> record.reason.titleMs
        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> record.reason.titleEn
        else -> record.reason.titleId
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isFidyahRequired) {
                        val paidTag = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> if (record.isFullyPaid) "FIDYAH JELAS" else "FIDYAH BELUM"
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> if (record.isFullyPaid) "FIDYAH PAID" else "FIDYAH PENDING"
                            else -> if (record.isFullyPaid) "FIDYAH LUNAS" else "FIDYAH BELUM"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (record.isFullyPaid) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = paidTag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "days"
                else -> "hari"
            }
            val missedFastLabel = when (language) {
                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Hutang Puasa: ${record.missedDays} hari"
                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Missed Fast: ${record.missedDays} days"
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
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Kemajuan Qada Puasa:"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Qadha Fast Progress:"
                                else -> "Progres Qadha Puasa:"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        Text(
                            text = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "${record.completedQadhaDays} / ${record.missedDays} days completed"
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
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "+1 Qada Hari Ini"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "+1 Qadha Today"
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

@Composable
fun FidyahDuaModalDialog(
    language: app.kamy.saatApp.core.locale.AppLanguage,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (language) {
                        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Doa & Niat Pembayaran Fidyah"
                        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Fidyah Payment Duas & Niyyah"
                        else -> "Doa & Niat Pembayaran Fidyah"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.height(380.dp)
                ) {
                    // Item 1: Niat Fidyah Diri Sendiri
                    item {
                        DuaCardItem(
                            title = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "1. Niat Fidyah Diri Sendiri"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "1. Intention (Niyyah) for Oneself"
                                else -> "1. Niat Membayar Fidyah Diri Sendiri"
                            },
                            arabic = "نَوَيْتُ أَنْ أُخْرِجَ هَذِهِ الْفِدْيَةَ عَنْ إِفْطَارِ صَوْمِ رَمَضَانَ فَرْضًا لِلَّهِ تَعَالَى",
                            latin = "Nawaitu an ukhrija hadhihi al-fidyata 'an iftari saumi ramadhana fardhan lillahi ta'ala.",
                            translation = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Niat saya mengeluarkan fidyah ini kerana meninggalkan puasa Ramadan, fardu kerana Allah Ta'ala."
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "I intend to pay this fidyah for missing the Ramadan fast, as an obligation for Allah Almighty."
                                else -> "Niat saya mengeluarkan fidyah ini karena meninggalkan puasa Ramadan, fardhu karena Allah Ta'ala."
                            }
                        )
                    }

                    // Item 2: Niat Fidyah Orang Lain / Almarhum
                    item {
                        DuaCardItem(
                            title = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "2. Niat Fidyah Mewakili Arwah / Orang Tua"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "2. Intention (Niyyah) for Deceased / Parents"
                                else -> "2. Niat Fidyah Mewakili Almarhum / Orang Tua"
                            },
                            arabic = "نَوَيْتُ أَنْ أُخْرِجَ هَذِهِ الْفِدْيَةَ عَنْ صَوْمِ رَمَضَانَ فُلَانِ بْنِ فُلَانٍ فَرْضًا لِلَّهِ تَعَالَى",
                            latin = "Nawaitu an ukhrija hadhihi al-fidyata 'an saumi ramadhana (Nama Almarhum/ah) fardhan lillahi ta'ala.",
                            translation = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Niat saya mengeluarkan fidyah ini daripada puasa Ramadan (Nama Arwah), fardu kerana Allah Ta'ala."
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "I intend to pay this fidyah on behalf of the Ramadan fast of (Name), obligatory for Allah Almighty."
                                else -> "Niat saya mengeluarkan fidyah ini dari puasa Ramadan (Nama Almarhum/ah), fardhu karena Allah Ta'ala."
                            }
                        )
                    }

                    // Item 3: Doa Penerimaan Fidyah
                    item {
                        DuaCardItem(
                            title = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "3. Doa Keberkahan Pembayaran Fidyah"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "3. Supplication for Blessing in Fidyah"
                                else -> "3. Doa Keberkahan Pembayaran Fidyah"
                            },
                            arabic = "بَارَكَ اللَّهُ لَكَ فِي مَالِكَ وَأَهْلِكَ وَتَقَبَّلَ اللَّهُ مِنْكَ صَالِحَ الْأَعْمَالِ",
                            latin = "Barakallahu laka fi malika wa ahlika wa taqabbalallahu minka shalihal a'mal.",
                            translation = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Semoga Allah memberkati harta dan keluargamu, serta menerima amal kebaikanmu."
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "May Allah bless your wealth and family, and accept your righteous deeds."
                                else -> "Semoga Allah memberkahi harta dan keluargamu, serta menerima amal kebaikanmu."
                            }
                        )
                    }

                    // Item 4: Niat Puasa Qadha Ramadan
                    item {
                        DuaCardItem(
                            title = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "4. Niat Puasa Qada Ramadan"
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "4. Niyyah for Qadha Ramadan Fast"
                                else -> "4. Niat Puasa Qadha Ramadan"
                            },
                            arabic = "نَوَيْتُ صَوْمَ غَدٍ عَنْ قَضَاءِ فَرْضِ شَهْرِ رَمَضَانَ لِلَّهِ تَعَالَى",
                            latin = "Nawaitu shauma ghadin 'an qadha'i fardhi shahri ramadhana lillahi ta'ala.",
                            translation = when (language) {
                                app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Niat saya puasa esok hari kerana meng-Qada fardu bulan Ramadan kerana Allah Ta'ala."
                                app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "I intend to fast tomorrow to make up for the obligatory fast of Ramadan for Allah Almighty."
                                else -> "Aku berniat puasa esok hari untuk meng-qadha fardhu bulan Ramadan karena Allah Ta'ala."
                            }
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Tutup", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SaatColors.DeepEmerald.copy(alpha = 0.06f)),
        border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )
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
                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
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
    language: app.kamy.saatApp.core.locale.AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_remainders_custom),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Panduan Fikrah: Waktu Sah Bayar Fidyah"
                        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Fiqh Guide: Valid Fidyah Payment Timing"
                        else -> "Panduan Fiqih: Waktu Sah & Kebolehan Bayar Fidyah"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text(text = "✅ ", fontSize = 12.sp)
                    Text(
                        text = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "SAH: Setiap hari Ramadan selepas terbenam matahari (Maghrib) bagi hari yang ditinggalkan."
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "VALID: Every Ramadan day after sunset (Maghrib) for that missed day."
                            else -> "SAH: Setiap hari Ramadan setelah terbenam matahari (Maghrib) untuk hari puasa yang telah ditinggalkan."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
                Row {
                    Text(text = "✅ ", fontSize = 12.sp)
                    Text(
                        text = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "SAH: Dikumpulkan & dibayar sekali gus di akhir bulan Ramadan."
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "VALID: Collected & paid altogether at the end of Ramadan."
                            else -> "SAH: Dikumpulkan & dibayar sekaligus di akhir bulan Ramadan (tgl 29/30)."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
                Row {
                    Text(text = "✅ ", fontSize = 12.sp)
                    Text(
                        text = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "SAH: Dibayarkan selepas Ramadan (sepanjang tahun) sebelum Ramadan berikutnya."
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "VALID: Paid after Ramadan (throughout the year) before next Ramadan."
                            else -> "SAH: Dibayarkan setelah Ramadan (sepanjang tahun) sebelum Ramadan berikutnya."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
                Row {
                    Text(text = "❌ ", fontSize = 12.sp)
                    Text(
                        text = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "TIDAK SAH: Mendahului sebelum Ramadan tiba (Ta'jil sebelum Ramadan - Ijma' 4 Mazhab)."
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "INVALID: Paying before Ramadan arrives (Consensus of 4 Madhhabs)."
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
                shape = RoundedCornerShape(8.dp),
                color = SaatColors.DeepEmerald.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (language) {
                        app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "🔔 Notifikasi pengingat fidyah hanya dihantar selepas Maghrib atau sebelum bulan Ramadan baru tiba untuk memastikan keabsahan fiqh."
                        app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "🔔 Fidyah reminder notifications are sent after Maghrib or before the new Ramadan to align with Fiqh rules."
                        else -> "🔔 Notifikasi pengingat fidyah hanya dikirim setelah Maghrib atau menjelang Ramadan baru (bulan Sya'ban) agar selalu sesuai hukum Fiqih."
                    },
                    modifier = Modifier.padding(10.dp),
                    fontSize = 11.sp,
                    color = SaatColors.DeepEmerald,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun FidyahDalilSection(
    language: app.kamy.saatApp.core.locale.AppLanguage
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.5f))
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
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_faraidh_dalil),
                        contentDescription = null,
                        tint = SaatColors.GoldDeep,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (language) {
                            app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "Dalil Al-Quran & Hadis Fidyah"
                            app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "Quran & Hadith Evidences for Fidyah"
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SaatColors.DeepEmerald.copy(alpha = 0.05f))
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
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (language) {
                                    app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "\"Maka sesiapa di antara kamu yang sakit atau dalam musafir, maka (wajiblah) sebanyak hari yang ditinggalkan itu pada hari-hari yang lain. Dan wajib atas orang-orang yang berat menjalankannya membayar fidyah, iaitu memberi makan seorang miskin.\""
                                    app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "\"So whoever among you is ill or on a journey - then an equal number of other days. And upon those who can afford it with hardship - a ransom [fidyah] of feeding a poor person.\""
                                    else -> "\"Maka barangsiapa di antara kamu ada yang sakit atau dalam perjalanan, maka (wajiblah baginya berpuasa) sebanyak hari yang ditinggalkan itu pada hari-hari yang lain. Dan wajib bagi orang-orang yang berat menjalankannya membayar fidyah, yaitu memberi makan seorang miskin.\""
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2. Hadits Shahih Al-Bukhari No. 4505
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SaatColors.DeepEmerald.copy(alpha = 0.05f))
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
                                    app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "\"Ayat ini tidak dimansuhkan. Ia ditujukan kepada lelaki dan wanita tua yang tidak mampu berpuasa, maka mereka memberi makan seorang miskin bagi setiap hari.\""
                                    app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "\"This verse is not abrogated. It applies to elderly men and women who cannot fast, so they feed a poor person for every missed day.\""
                                    else -> "\"Ayat ini tidak di-mansukh. Ayat ini berlaku untuk laki-laki dan wanita tua yang tidak mampu lagi berpuasa, maka keduanya memberi makan seorang miskin untuk setiap hari puasa.\""
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Hadits Abu Dawud No. 2318
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SaatColors.DeepEmerald.copy(alpha = 0.05f))
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
                                    app.kamy.saatApp.core.locale.AppLanguage.MALAY -> "\"Sesungguhnya Allah meringankan kewajipan puasa bagi musafir, wanita hamil, dan wanita yang menyusukan anak.\""
                                    app.kamy.saatApp.core.locale.AppLanguage.ENGLISH -> "\"Indeed, Allah has relieved the traveler, pregnant women, and nursing mothers from the obligation of fasting.\""
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
