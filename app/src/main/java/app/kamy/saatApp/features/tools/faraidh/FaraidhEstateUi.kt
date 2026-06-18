package app.kamy.saatApp.features.tools.faraidh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.domain.faraidh.EstateAssetInput
import app.kamy.saatApp.domain.faraidh.EstateComputation
import app.kamy.saatApp.domain.faraidh.FaraidhMadhhab
import java.text.NumberFormat

@Composable
fun FaraidhMadhhabPicker(selected: FaraidhMadhhab, onSelect: (FaraidhMadhhab) -> Unit) {
    val options = listOf(
        FaraidhMadhhab.HANAFI to R.string.faraidh_madhhab_hanafi,
        FaraidhMadhhab.MALIKI to R.string.faraidh_madhhab_maliki,
        FaraidhMadhhab.SHAFII to R.string.faraidh_madhhab_shafii,
        FaraidhMadhhab.HANBALI to R.string.faraidh_madhhab_hanbali
    )
    Column {
        Text(
            stringResource(R.string.faraidh_madhhab_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.DeepEmerald
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.take(2).forEach { (madhhab, labelRes) ->
                MadhhabChip(
                    label = stringResource(labelRes),
                    selected = selected == madhhab,
                    onClick = { onSelect(madhhab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.drop(2).forEach { (madhhab, labelRes) ->
                MadhhabChip(
                    label = stringResource(labelRes),
                    selected = selected == madhhab,
                    onClick = { onSelect(madhhab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Text(
            stringResource(R.string.faraidh_madhhab_hint),
            style = MaterialTheme.typography.bodySmall,
            color = AlKhatibColors.Slate500,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun MadhhabChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) AlKhatibColors.DeepEmerald else AlKhatibColors.PureWhite
    val fg = if (selected) Color.White else AlKhatibColors.Slate800
    val border = if (selected) AlKhatibColors.DeepEmerald else AlKhatibColors.SoftGrey
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(1.dp, border, RoundedCornerShape(12.dp)),
        color = bg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
fun FaraidhEstateInputSection(
    estate: EstateAssetInput,
    computation: EstateComputation?,
    currency: NumberFormat,
    onFieldChange: (EstateAssetInput.() -> EstateAssetInput) -> Unit
) {
    EstateSection(title = stringResource(R.string.faraidh_section_assets)) {
        MoneyField(stringResource(R.string.faraidh_asset_cash), estate.cashSavings) {
            onFieldChange { copy(cashSavings = it) }
        }
        MoneyField(stringResource(R.string.faraidh_asset_gold), estate.goldJewelry) {
            onFieldChange { copy(goldJewelry = it) }
        }
        MoneyField(stringResource(R.string.faraidh_asset_property), estate.propertyValue) {
            onFieldChange { copy(propertyValue = it) }
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.faraidh_has_house), fontWeight = FontWeight.Medium, color = AlKhatibColors.Slate800)
                Text(stringResource(R.string.faraidh_has_house_hint), style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
            }
            Switch(
                checked = estate.hasResidentialProperty,
                onCheckedChange = { checked -> onFieldChange { copy(hasResidentialProperty = checked) } },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AlKhatibColors.Teal)
            )
        }
        if (estate.hasResidentialProperty) {
            OutlinedTextField(
                value = estate.propertyNotes,
                onValueChange = { onFieldChange { copy(propertyNotes = it) } },
                label = { Text(stringResource(R.string.faraidh_property_notes)) },
                placeholder = { Text(stringResource(R.string.faraidh_property_notes_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlKhatibColors.Teal)
            )
        }
        MoneyField(stringResource(R.string.faraidh_asset_business), estate.businessAssets) {
            onFieldChange { copy(businessAssets = it) }
        }
        MoneyField(stringResource(R.string.faraidh_asset_other), estate.otherAssets) {
            onFieldChange { copy(otherAssets = it) }
        }
    }

    EstateSection(title = stringResource(R.string.faraidh_section_deductions)) {
        MoneyField(stringResource(R.string.faraidh_deduction_funeral), estate.funeralCosts) {
            onFieldChange { copy(funeralCosts = it) }
        }
        MoneyField(stringResource(R.string.faraidh_deduction_debts), estate.debts) {
            onFieldChange { copy(debts = it) }
        }
        MoneyField(stringResource(R.string.faraidh_deduction_zakat), estate.unpaidZakat) {
            onFieldChange { copy(unpaidZakat = it) }
        }
        MoneyField(stringResource(R.string.faraidh_deduction_wasiat), estate.bequestWasiat) {
            onFieldChange { copy(bequestWasiat = it) }
        }
        Text(
            stringResource(R.string.faraidh_wasiat_hint),
            style = MaterialTheme.typography.bodySmall,
            color = AlKhatibColors.Slate500,
            modifier = Modifier.padding(top = 2.dp)
        )
    }

    computation?.let { EstateComputationSummary(it, currency) }
}

@Composable
private fun EstateSection(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = AlKhatibColors.ScreenBackground
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = AlKhatibColors.Slate800)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun MoneyField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text("0") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlKhatibColors.Teal, focusedLabelColor = AlKhatibColors.DeepEmerald)
    )
}

@Composable
fun EstateComputationSummary(computation: EstateComputation, currency: NumberFormat) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AlKhatibColors.PrayerMint,
        border = androidx.compose.foundation.BorderStroke(1.dp, AlKhatibColors.Teal.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(R.string.faraidh_estate_summary), fontWeight = FontWeight.Bold, color = AlKhatibColors.DeepEmerald)
            Spacer(Modifier.height(8.dp))
            SummaryRow(stringResource(R.string.faraidh_gross_assets), currency.format(computation.grossAssets))
            if (computation.funeralCosts > java.math.BigDecimal.ZERO) {
                SummaryRow(stringResource(R.string.faraidh_deduction_funeral), "− ${currency.format(computation.funeralCosts)}")
            }
            if (computation.debts > java.math.BigDecimal.ZERO) {
                SummaryRow(stringResource(R.string.faraidh_deduction_debts), "− ${currency.format(computation.debts)}")
            }
            if (computation.unpaidZakat > java.math.BigDecimal.ZERO) {
                SummaryRow(stringResource(R.string.faraidh_deduction_zakat), "− ${currency.format(computation.unpaidZakat)}")
            }
            if (computation.wasiatApplied > java.math.BigDecimal.ZERO) {
                SummaryRow(stringResource(R.string.faraidh_deduction_wasiat), "− ${currency.format(computation.wasiatApplied)}")
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = AlKhatibColors.Teal.copy(alpha = 0.25f))
            SummaryRow(
                stringResource(R.string.faraidh_tarikah_net),
                currency.format(computation.netEstate),
                bold = true
            )
            if (computation.hasResidentialProperty) {
                Text(
                    stringResource(R.string.faraidh_house_included),
                    style = MaterialTheme.typography.labelSmall,
                    color = AlKhatibColors.Slate500,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = AlKhatibColors.Slate500)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = if (bold) AlKhatibColors.DeepEmerald else AlKhatibColors.Slate800
        )
    }
}
