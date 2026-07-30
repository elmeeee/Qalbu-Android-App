package app.kamy.saatApp.features.tools

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
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
import kotlinx.coroutines.launch

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
            .background(SaatColors.ScreenBackground)
            .tabContentStatusBarInset()
            .imePadding()
            .navigationBarsPadding()
    ) {
        // Sticky Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SaatColors.ScreenBackground)
                .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = SaatColors.Slate800
                )
            }
            Text(
                text = stringResource(R.string.zakat_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )
        }

        // Scrollable Body Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SaatColors.PureWhite,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, SaatColors.SoftGrey)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SaatColors.DeepEmerald.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_zakat),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = stringResource(R.string.zakat_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate700,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ZakatType.values().forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.selectedType == type,
                        onClick = { vm.updateType(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ZakatType.values().size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = SaatColors.DeepEmerald,
                            activeContentColor = Color.White,
                            inactiveContainerColor = SaatColors.PureWhite,
                            inactiveContentColor = SaatColors.Slate700
                        )
                    ) {
                        Text(
                            text = stringResource(
                                if (type == ZakatType.MAAL) R.string.zakat_type_maal else R.string.zakat_type_fitrah
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (state.selectedType == ZakatType.MAAL) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SaatColors.PureWhite,
                    border = BorderStroke(1.dp, SaatColors.Teal.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.priceLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = SaatColors.DeepEmerald
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.loading),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SaatColors.Slate500
                                )
                            }
                        } else {
                            state.priceQuote?.let { quote ->
                                Column {
                                    Text(
                                        text = stringResource(
                                            R.string.zakat_live_gold_price,
                                            currency.format(quote.goldPerGramIdr)
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = SaatColors.DeepEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.zakat_live_price_source, quote.sourceLabel),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SaatColors.Slate500
                                    )
                                }
                            } ?: Text(
                                text = stringResource(R.string.zakat_price_error),
                                style = MaterialTheme.typography.labelMedium,
                                color = SaatColors.GoldDeep,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(onClick = vm::refreshPrices) {
                            Text(
                                text = stringResource(R.string.retry),
                                color = SaatColors.Teal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!state.priceLoading && state.priceQuote == null) {
                    ZakatField(
                        label = stringResource(R.string.zakat_manual_gold_price),
                        value = state.manualGoldPrice,
                        onValueChange = { vm.updateManualGoldPrice(MoneyInputFormatter.format(it)) },
                        unitBadge = "Rp"
                    )
                    Text(
                        text = stringResource(R.string.zakat_manual_price_help),
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate500
                    )
                }

                ZakatField(
                    label = stringResource(R.string.zakat_cash),
                    value = state.cash,
                    onValueChange = { vm.updateCash(MoneyInputFormatter.format(it)) },
                    unitBadge = "Rp"
                )
                ZakatField(
                    label = stringResource(R.string.zakat_gold_grams),
                    value = state.goldGrams,
                    onValueChange = { vm.updateGoldGrams(MoneyInputFormatter.format(it)) },
                    unitBadge = "gram"
                )
                ZakatField(
                    label = stringResource(R.string.zakat_silver_grams),
                    value = state.silverGrams,
                    onValueChange = { vm.updateSilverGrams(MoneyInputFormatter.format(it)) },
                    unitBadge = "gram"
                )
                ZakatField(
                    label = stringResource(R.string.zakat_investments),
                    value = state.investments,
                    onValueChange = { vm.updateInvestments(MoneyInputFormatter.format(it)) },
                    unitBadge = "Rp"
                )
                ZakatField(
                    label = stringResource(R.string.zakat_debts),
                    value = state.debts,
                    onValueChange = { vm.updateDebts(MoneyInputFormatter.format(it)) },
                    unitBadge = "Rp"
                )

                Text(
                    text = stringResource(R.string.zakat_haul_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500
                )
            } else {
                ZakatField(
                    label = stringResource(R.string.zakat_family_members),
                    value = state.familyMembers,
                    onValueChange = vm::updateFamilyMembers,
                    keyboardType = KeyboardType.Number,
                    unitBadge = "orang"
                )
                ZakatField(
                    label = stringResource(R.string.zakat_rice_price_per_kg),
                    value = state.ricePricePerKg,
                    onValueChange = { vm.updateRicePricePerKg(MoneyInputFormatter.format(it)) },
                    unitBadge = "Rp/kg"
                )
                Text(
                    text = stringResource(R.string.zakat_fitrah_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500
                )
            }

            if (result != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SaatColors.PureWhite,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.5.dp, SaatColors.DeepEmerald.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ringkasan Perhitungan Zakat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )

                        HorizontalDivider(color = SaatColors.SoftGrey)

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

                                HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (result.meetsNisab) SaatColors.DeepEmerald.copy(alpha = 0.08f) else SaatColors.GoldDeep.copy(alpha = 0.08f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (result.meetsNisab) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                                contentDescription = null,
                                                tint = if (result.meetsNisab) SaatColors.DeepEmerald else SaatColors.GoldDeep,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = if (result.meetsNisab) stringResource(R.string.zakat_meets_nisab) else "Belum Wajib Zakat",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (result.meetsNisab) SaatColors.DeepEmerald else SaatColors.GoldDeep
                                            )
                                        }
                                    }
                                }

                                ResultRow(
                                    stringResource(R.string.zakat_due),
                                    currency.format(result.zakatDue),
                                    highlight = result.meetsNisab
                                )

                                if (!result.meetsNisab) {
                                    Text(
                                        text = stringResource(R.string.zakat_below_nisab),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SaatColors.Slate500,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                            is ZakatFitrahCalculationResult -> {
                                ResultRow(stringResource(R.string.zakat_family_members), "${result.familyMembers} orang")
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

                                HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

                                ResultRow(
                                    stringResource(R.string.zakat_due),
                                    currency.format(result.zakatDue),
                                    highlight = true
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = SaatColors.SoftGrey)
            Spacer(Modifier.height(4.dp))

            ZakatBodiesSection(
                selectedCountry = state.selectedZakatCountry,
                onCountrySelected = vm::updateZakatCountry
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZakatBodiesSection(
    selectedCountry: ZakatCountry,
    onCountrySelected: (ZakatCountry) -> Unit
) {
    val context = LocalContext.current
    val bodies = remember(selectedCountry) { ZakatBodyRepository.byCountry(selectedCountry) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.zakat_pay_where_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )
            Text(
                text = stringResource(R.string.zakat_pay_where_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500
            )
        }

        if (selectedCountry == ZakatCountry.MALAYSIA) {
            Text(
                text = stringResource(R.string.zakat_body_malaysia_note),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            bodies.forEach { body ->
                ZakatBodyCard(
                    body = body,
                    onOpenUrl = { targetUrl ->
                        val cleanUrl = if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
                            targetUrl
                        } else {
                            "https://$targetUrl"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Tidak dapat membuka link: $cleanUrl", Toast.LENGTH_SHORT).show()
                        }
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
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = SaatColors.SoftGrey,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onOpenUrl(body.websiteUrl) },
        color = SaatColors.PureWhite,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                body.stateTag?.let { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SaatColors.DeepEmerald.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = SaatColors.DeepEmerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = body.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate900
                )
                Text(
                    text = body.fullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500,
                    lineHeight = 18.sp
                )
                Text(
                    text = body.websiteUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = SaatColors.Teal,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SaatColors.DeepEmerald.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.zakat_body_open_website),
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZakatField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    unitBadge: String? = null
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = unitBadge?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = SaatColors.Slate500,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    scope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SaatColors.DeepEmerald,
            unfocusedBorderColor = SaatColors.SoftGrey,
            focusedContainerColor = SaatColors.PureWhite,
            unfocusedContainerColor = SaatColors.PureWhite
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
private fun ResultRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SaatColors.Slate700
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) SaatColors.DeepEmerald else SaatColors.Slate900
        )
    }
}
