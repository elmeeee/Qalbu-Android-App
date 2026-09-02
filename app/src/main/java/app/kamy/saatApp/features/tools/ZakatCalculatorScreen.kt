package app.kamy.saatApp.features.tools

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.LocalTextStyle
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.tools.ZakatBody
import app.kamy.saatApp.domain.tools.ZakatBodyRepository
import app.kamy.saatApp.domain.tools.ZakatCountry
import app.kamy.saatApp.domain.tools.ZakatFitrahCalculationResult
import app.kamy.saatApp.domain.tools.ZakatMaalCalculationResult
import app.kamy.saatApp.domain.tools.ZakatNumberFormatter
import app.kamy.saatApp.domain.tools.ZakatType
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ZakatCalculatorScreen(
    onBack: () -> Unit,
    vm: ZakatViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val result = state.result
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val appLanguageStore = remember { AppLanguageStore.from(context) }
    val currentLang by appLanguageStore.currentFlow.collectAsStateWithLifecycle()
    val isMalay = currentLang == AppLanguage.MALAY
    val isIndo = currentLang == AppLanguage.INDONESIAN

    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.HomeBg)
            .imePadding()
    ) {
        // Unified Sticky Premium Header Row
        app.kamy.saatApp.features.tools.components.SpiritualToolTopBar(
            title = stringResource(R.string.zakat_title),
            subtitle = stringResource(R.string.tool_zakah_desc),
            onBack = onBack
        )

        // Scrollable Body Form with Bottom Padding guaranteed above Android Navbar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = navBarBottomPadding + 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Intro Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = SaatColors.PureWhite,
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, SaatColors.DeepEmerald.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        SaatColors.DeepEmerald.copy(alpha = 0.15f),
                                        SaatColors.GoldDeep.copy(alpha = 0.12f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_zakat),
                            contentDescription = null,
                            tint = SaatColors.DeepEmerald,
                            modifier = Modifier.size(28.dp)
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

            // Custom Segmented Pill Tab Selector (Maal vs Fitrah)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SaatColors.PureWhite)
                    .border(1.dp, SaatColors.SoftGrey, RoundedCornerShape(18.dp))
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ZakatType.values().forEach { type ->
                    val isSelected = state.selectedType == type
                    val label = stringResource(if (type == ZakatType.MAAL) R.string.zakat_type_maal else R.string.zakat_type_fitrah)
                    val iconRes = if (type == ZakatType.MAAL) R.drawable.ic_zakat_mal else R.drawable.ic_zakat_fitrah

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { vm.updateType(type) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SaatColors.DeepEmerald else Color.Transparent,
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else SaatColors.Slate500,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else SaatColors.Slate700
                            )
                        }
                    }
                }
            }

            if (state.selectedType == ZakatType.MAAL) {
                // Gold Live Price Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SaatColors.PureWhite,
                    border = BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.3f)),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (state.priceQuote != null) SaatColors.DeepEmerald else SaatColors.GoldDeep)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isIndo) "Harga Emas Live" else "Live Gold Market Price",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.Slate800
                                )
                            }

                            IconButton(
                                onClick = vm::refreshPrices,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = stringResource(R.string.retry),
                                    tint = SaatColors.DeepEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (state.priceLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
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
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "${ZakatNumberFormatter.formatCurrency(quote.goldPerGramIdr, currencySymbol = state.userCurrencySymbol)} / gram",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = SaatColors.DeepEmerald,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } ?: Text(
                                text = stringResource(R.string.zakat_price_error),
                                style = MaterialTheme.typography.labelMedium,
                                color = SaatColors.GoldDeep,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (!state.priceLoading && state.priceQuote == null) {
                    ZakatField(
                        label = stringResource(R.string.zakat_manual_gold_price),
                        value = state.manualGoldPrice,
                        onValueChange = { vm.updateManualGoldPrice(ZakatNumberFormatter.formatMoneyInput(it, isIndonesian = isIndo)) },
                        unitBadge = state.userCurrencySymbol,
                        iconRes = R.drawable.ic_gold_silver,
                        iconColor = SaatColors.GoldDeep
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
                    onValueChange = { vm.updateCash(ZakatNumberFormatter.formatMoneyInput(it, isIndonesian = isIndo)) },
                    unitBadge = state.userCurrencySymbol,
                    iconRes = R.drawable.ic_cash_saving,
                    iconColor = SaatColors.DeepEmerald
                )
                ZakatField(
                    label = stringResource(R.string.zakat_gold_grams),
                    value = state.goldGrams,
                    onValueChange = { vm.updateGoldGrams(ZakatNumberFormatter.formatDecimalInput(it, isIndonesian = isIndo)) },
                    unitBadge = "gram",
                    iconRes = R.drawable.ic_gold_silver,
                    iconColor = SaatColors.GoldDeep
                )
                ZakatField(
                    label = stringResource(R.string.zakat_silver_grams),
                    value = state.silverGrams,
                    onValueChange = { vm.updateSilverGrams(ZakatNumberFormatter.formatDecimalInput(it, isIndonesian = isIndo)) },
                    unitBadge = "gram",
                    iconRes = R.drawable.ic_gold_silver,
                    iconColor = Color(0xFF708090)
                )
                ZakatField(
                    label = stringResource(R.string.zakat_investments),
                    value = state.investments,
                    onValueChange = { vm.updateInvestments(ZakatNumberFormatter.formatMoneyInput(it, isIndonesian = isIndo)) },
                    unitBadge = state.userCurrencySymbol,
                    iconRes = R.drawable.ic_invest,
                    iconColor = SaatColors.DeepEmerald
                )
                ZakatField(
                    label = stringResource(R.string.zakat_debts),
                    value = state.debts,
                    onValueChange = { vm.updateDebts(ZakatNumberFormatter.formatMoneyInput(it, isIndonesian = isIndo)) },
                    unitBadge = state.userCurrencySymbol,
                    iconRes = R.drawable.ic_debt,
                    iconColor = Color(0xFFD9534F)
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
                    unitBadge = if (isIndo) "orang" else if (isMalay) "orang" else "people",
                    iconRes = R.drawable.ic_family,
                    iconColor = SaatColors.DeepEmerald
                )
                ZakatField(
                    label = stringResource(R.string.zakat_rice_price_per_kg),
                    value = state.ricePricePerKg,
                    onValueChange = { vm.updateRicePricePerKg(ZakatNumberFormatter.formatMoneyInput(it, isIndonesian = isIndo)) },
                    unitBadge = "${state.userCurrencySymbol}/kg",
                    iconRes = R.drawable.ic_rice,
                    iconColor = SaatColors.GoldDeep
                )
                Text(
                    text = stringResource(R.string.zakat_fitrah_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500
                )
            }

            // Calculation Summary Card
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (result != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = SaatColors.PureWhite,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.5.dp, SaatColors.DeepEmerald.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.zakat_summary_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SaatColors.DeepEmerald
                            )

                            HorizontalDivider(color = SaatColors.SoftGrey)

                            when (result) {
                                is ZakatMaalCalculationResult -> {
                                    ResultRow(
                                        stringResource(R.string.zakat_net_wealth),
                                        ZakatNumberFormatter.formatCurrency(result.zakatableWealth, currencySymbol = state.userCurrencySymbol)
                                    )

                                    HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (result.meetsNisab) SaatColors.DeepEmerald.copy(alpha = 0.1f) else SaatColors.GoldDeep.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (result.meetsNisab) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                                    contentDescription = null,
                                                    tint = if (result.meetsNisab) SaatColors.DeepEmerald else SaatColors.GoldDeep,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    text = if (result.meetsNisab) stringResource(R.string.zakat_meets_nisab) else stringResource(R.string.zakat_not_yet_mandatory),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (result.meetsNisab) SaatColors.DeepEmerald else SaatColors.GoldDeep
                                                )
                                            }
                                        }
                                    }

                                    ResultRow(
                                        stringResource(R.string.zakat_due),
                                        ZakatNumberFormatter.formatCurrency(result.zakatDue, currencySymbol = state.userCurrencySymbol),
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
                                        ZakatNumberFormatter.formatCurrency(result.staplePricePerKg, currencySymbol = state.userCurrencySymbol)
                                    )

                                    HorizontalDivider(color = SaatColors.SoftGrey.copy(alpha = 0.5f))

                                    ResultRow(
                                        stringResource(R.string.zakat_due),
                                        ZakatNumberFormatter.formatCurrency(result.zakatDue, currencySymbol = state.userCurrencySymbol),
                                        highlight = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = SaatColors.SoftGrey)
            Spacer(Modifier.height(4.dp))

            // Directory of Official Zakat Bodies
            ZakatBodiesSection(
                selectedCountry = state.selectedZakatCountry,
                onSelectCountry = vm::updateZakatCountry
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZakatBodiesSection(
    selectedCountry: ZakatCountry,
    onSelectCountry: (ZakatCountry) -> Unit
) {
    val context = LocalContext.current
    val bodies = remember(selectedCountry) { ZakatBodyRepository.byCountry(selectedCountry) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.zakat_pay_where_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SaatColors.Slate900
            )
            Text(
                text = stringResource(R.string.zakat_pay_where_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500
            )
        }

        // Country Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZakatCountry.entries.forEach { country ->
                val isSelected = country == selectedCountry
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCountry(country) },
                    label = {
                        Text(
                            text = "${country.emoji} ${stringResource(country.labelRes)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaatColors.DeepEmerald,
                        selectedLabelColor = Color.White,
                        containerColor = SaatColors.SoftGrey,
                        labelColor = SaatColors.Slate700
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
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
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = SaatColors.SoftGrey,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onOpenUrl(body.websiteUrl) },
        color = SaatColors.PureWhite,
        shadowElevation = 2.dp
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
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    fontWeight = FontWeight.ExtraBold,
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
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(42.dp)
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
    unitBadge: String? = null,
    iconRes: Int? = null,
    iconColor: Color = SaatColors.DeepEmerald
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onValueChange(newValue.text)
        },
        label = { Text(label) },
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
        leadingIcon = iconRes?.let { resId ->
            {
                Icon(
                    painter = painterResource(resId),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        suffix = unitBadge?.let {
            {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SaatColors.DeepEmerald.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = SaatColors.DeepEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
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
        shape = RoundedCornerShape(16.dp),
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlight) SaatColors.DeepEmerald else SaatColors.Slate900
        )
    }
}
