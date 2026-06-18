package app.kamy.saatApp.features.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ZakatCalculatorScreen(
    onBack: () -> Unit,
    vm: ZakatViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val currency = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
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
                text = stringResource(R.string.zakat_intro_sharia),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )

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
                        Text(
                            text = stringResource(
                                R.string.zakat_live_gold_price,
                                currency.format(quote.goldPerGramIdr)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = AlKhatibColors.DeepEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
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

            ZakatField(stringResource(R.string.zakat_cash), state.cash, vm::updateCash)
            ZakatField(stringResource(R.string.zakat_gold_grams), state.goldGrams, vm::updateGoldGrams)
            ZakatField(stringResource(R.string.zakat_silver_grams), state.silverGrams, vm::updateSilverGrams)
            ZakatField(stringResource(R.string.zakat_investments), state.investments, vm::updateInvestments)
            ZakatField(stringResource(R.string.zakat_debts), state.debts, vm::updateDebts)

            Text(
                text = stringResource(R.string.zakat_haul_note),
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500
            )

            if (result != null) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = AlKhatibColors.SoftGrey)
                Spacer(Modifier.height(8.dp))
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
        }
    }
}

@Composable
private fun ZakatField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

