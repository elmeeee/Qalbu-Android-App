package app.kamy.saatApp.features.tools

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.domain.faraidh.MoneyInputFormatter
import app.kamy.saatApp.domain.tools.ZakatBody
import app.kamy.saatApp.domain.tools.ZakatBodyRepository
import app.kamy.saatApp.domain.tools.ZakatCountry
import app.kamy.saatApp.domain.tools.ZakatFitrahCalculationResult
import app.kamy.saatApp.domain.tools.ZakatMaalCalculationResult
import app.kamy.saatApp.domain.tools.ZakatType
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ZakatCalculatorScreen(
    onBack: () -> Unit,
    vm: ZakatViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val currency = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")) }
    val result = state.result

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = stringResource(R.string.zakat_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.zakat_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ZakatType.values().forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.selectedType == type,
                        onClick = { vm.updateType(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ZakatType.values().size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = AlKhatibColors.DeepEmerald,
                            activeContentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(
                                if (type == ZakatType.MAAL) R.string.zakat_type_maal else R.string.zakat_type_fitrah
                            )
                        )
                    }
                }
            }

            if (state.selectedType == ZakatType.MAAL) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.priceLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp,
                            color = AlKhatibColors.DeepEmerald
                        )
                    } else {
                        state.priceQuote?.let { quote ->
                            Column {
                                Text(
                                    text = stringResource(
                                        R.string.zakat_live_gold_price,
                                        currency.format(quote.goldPerGramIdr)
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AlKhatibColors.DeepEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(R.string.zakat_live_price_source, quote.sourceLabel),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AlKhatibColors.Slate500
                                )
                            }
                        } ?: Text(
                            text = stringResource(R.string.zakat_price_error),
                            style = MaterialTheme.typography.labelMedium,
                            color = AlKhatibColors.Gold
                        )
                    }
                    TextButton(onClick = vm::refreshPrices) {
                        Text(stringResource(R.string.retry))
                    }
                }

                if (!state.priceLoading && state.priceQuote == null) {
ZakatField(
                        stringResource(R.string.zakat_manual_gold_price),
                        state.manualGoldPrice,
                        { vm.updateManualGoldPrice(MoneyInputFormatter.format(it)) }
                    )
                    Text(
                        text = stringResource(R.string.zakat_manual_price_help),
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                }

                ZakatField(
                    stringResource(R.string.zakat_cash),
                    state.cash,
                    { vm.updateCash(MoneyInputFormatter.format(it)) }
                )
                ZakatField(stringResource(R.string.zakat_gold_grams), state.goldGrams, vm::updateGoldGrams)
                ZakatField(stringResource(R.string.zakat_silver_grams), state.silverGrams, vm::updateSilverGrams)
                ZakatField(
                    stringResource(R.string.zakat_investments),
                    state.investments,
                    { vm.updateInvestments(MoneyInputFormatter.format(it)) }
                )
                ZakatField(
                    stringResource(R.string.zakat_debts),
                    state.debts,
                    { vm.updateDebts(MoneyInputFormatter.format(it)) }
                )

                Text(
                    text = stringResource(R.string.zakat_haul_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
            } else {
                ZakatField(
                    label = stringResource(R.string.zakat_family_members),
                    value = state.familyMembers,
                    onValueChange = vm::updateFamilyMembers,
                    keyboardType = KeyboardType.Number
                )
                ZakatField(
                    label = stringResource(R.string.zakat_rice_price_per_kg),
                    value = state.ricePricePerKg,
                    onValueChange = { vm.updateRicePricePerKg(MoneyInputFormatter.format(it)) }
                )
                Text(
                    text = stringResource(R.string.zakat_fitrah_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
            }

            if (result != null) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = AlKhatibColors.SoftGrey)
                Spacer(Modifier.height(8.dp))
                when (result) {
                    is ZakatMaalCalculationResult -> {
                        ResultRow(stringResource(R.string.zakat_net_wealth), currency.format(result.zakatableWealth))
                        ResultRow(
                            stringResource(R.string.zakat_nisab_gold, result.nisabGoldGrams.toInt()),
                            currency.format(result.nisabGoldValue)
                        )
                        ResultRow(
                            stringResource(R.string.zakat_nisab_silver, result.nisabSilverGrams.toInt()),
                            currency.format(result.nisabSilverValue)
                        )
                        ResultRow(
                            stringResource(R.string.zakat_due),
                            currency.format(result.zakatDue),
                            highlight = result.meetsNisab
                        )
                        if (!result.meetsNisab) {
                            Text(
                                text = stringResource(R.string.zakat_below_nisab),
                                style = MaterialTheme.typography.bodySmall,
                                color = AlKhatibColors.Slate500
                            )
                        }
                    }
                    is ZakatFitrahCalculationResult -> {
                        ResultRow(stringResource(R.string.zakat_family_members), result.familyMembers.toString())
                        ResultRow(
                            stringResource(R.string.zakat_fitrah_weight_per_person),
                            "${result.stapleWeightPerPersonKg} kg"
                        )
                        ResultRow(
                            stringResource(R.string.zakat_fitrah_total_weight),
                            "${result.totalStapleKilograms} kg"
                        )
                        ResultRow(
                            stringResource(R.string.zakat_rice_price_per_kg),
                            currency.format(result.staplePricePerKg)
                        )
                        ResultRow(
                            stringResource(R.string.zakat_due),
                            currency.format(result.zakatDue),
                            highlight = true
                        )
                    }
                }
            }

            // ── Zakat Bodies Directory ─────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AlKhatibColors.SoftGrey)
            Spacer(Modifier.height(4.dp))

            ZakatBodiesSection(
                selectedCountry = state.selectedZakatCountry,
                onCountrySelected = vm::updateZakatCountry
            )
        }
    }
}

// ── Zakat Bodies Section ───────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZakatBodiesSection(
    selectedCountry: ZakatCountry,
    onCountrySelected: (ZakatCountry) -> Unit
) {
    val context = LocalContext.current
    val bodies = remember(selectedCountry) { ZakatBodyRepository.byCountry(selectedCountry) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Section header
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.zakat_pay_where_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.Slate900
            )
            Text(
                text = stringResource(R.string.zakat_pay_where_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )
        }

        // Country chip selector
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ZakatCountry.values().forEach { country ->
                FilterChip(
                    selected = selectedCountry == country,
                    onClick = { onCountrySelected(country) },
                    label = {
                        Text(
                            text = "${country.emoji} ${stringResource(country.labelRes)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AlKhatibColors.DeepEmerald,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Malaysia special note
        if (selectedCountry == ZakatCountry.MALAYSIA) {
            Text(
                text = stringResource(R.string.zakat_body_malaysia_note),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )
        }

        // Body cards
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            bodies.forEach { body ->
                ZakatBodyCard(
                    body = body,
                    onOpenUrl = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun ZakatBodyCard(
    body: ZakatBody,
    onOpenUrl: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = AlKhatibColors.SoftGrey,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onOpenUrl("https://${body.websiteUrl}") },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // State tag badge (Malaysia only)
                body.stateTag?.let { tag ->
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = AlKhatibColors.DeepEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = body.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AlKhatibColors.Slate900
                )
                Text(
                    text = body.fullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
                Text(
                    text = body.websiteUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = AlKhatibColors.DeepEmerald
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.zakat_body_open_website),
                tint = AlKhatibColors.Slate500
            )
        }
    }
}

// ── Reusable field composables ─────────────────────────────────────────────────

@Composable
private fun ZakatField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
private fun ResultRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = AlKhatibColors.Slate800)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) AlKhatibColors.DeepEmerald else AlKhatibColors.Slate900
        )
    }
}
