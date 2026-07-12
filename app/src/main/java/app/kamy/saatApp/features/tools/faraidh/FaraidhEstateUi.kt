package app.kamy.saatApp.features.tools.faraidh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import app.kamy.saatApp.domain.faraidh.FaraidhPropertyItem
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.faraidh.EstateAssetInput
import app.kamy.saatApp.domain.faraidh.MoneyInputFormatter
import app.kamy.saatApp.domain.faraidh.EstateComputation
import app.kamy.saatApp.domain.faraidh.FaraidhMadhhab
import java.text.NumberFormat
import java.math.BigDecimal

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
            color = SaatColors.DeepEmerald
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
            color = SaatColors.Slate500,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun MadhhabChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) SaatColors.DeepEmerald else SaatColors.PureWhite
    val fg = if (selected) Color.White else SaatColors.Slate800
    val border = if (selected) SaatColors.DeepEmerald else SaatColors.SoftGrey
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaraidhEstateInputSection(
    estate: EstateAssetInput,
    computation: EstateComputation?,
    currency: NumberFormat,
    liveGoldPrice: String?,
    onFieldChange: (EstateAssetInput.() -> EstateAssetInput) -> Unit
) {
    EstateSection(title = stringResource(R.string.faraidh_section_assets)) {
        MoneyField(stringResource(R.string.faraidh_asset_cash), estate.cashSavings) {
            onFieldChange { copy(cashSavings = it) }
        }

        // Gold & jewelry detailed input section
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = stringResource(R.string.faraidh_asset_gold),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SaatColors.Slate800,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                SegmentedButton(
                    selected = !estate.inputGoldByGrams,
                    onClick = { onFieldChange { copy(inputGoldByGrams = false) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = SaatColors.DeepEmerald, activeContentColor = Color.White)
                ) { Text(stringResource(R.string.faraidh_gold_mode_value)) }
                SegmentedButton(
                    selected = estate.inputGoldByGrams,
                    onClick = { onFieldChange { copy(inputGoldByGrams = true) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = SaatColors.DeepEmerald, activeContentColor = Color.White)
                ) { Text(stringResource(R.string.faraidh_gold_mode_grams)) }
            }
            if (!estate.inputGoldByGrams) {
                MoneyField("", estate.goldJewelry) {
                    onFieldChange { copy(goldJewelry = it) }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = estate.goldWeightGrams,
                        onValueChange = { onFieldChange { copy(goldWeightGrams = it) } },
                        label = { Text(stringResource(R.string.faraidh_gold_weight)) },
                        placeholder = { Text("0.0") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaatColors.Teal)
                    )
                    Column(modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(
                            value = MoneyInputFormatter.format(estate.goldPricePerGram),
                            onValueChange = { onFieldChange { copy(goldPricePerGram = MoneyInputFormatter.format(it)) } },
                            label = { Text(stringResource(R.string.faraidh_gold_price)) },
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaatColors.Teal)
                        )
                        if (liveGoldPrice != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.faraidh_gold_price_live_btn) + ": " + currency.format(liveGoldPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO),
                                style = MaterialTheme.typography.labelSmall,
                                color = SaatColors.Teal,
                                modifier = Modifier
                                    .clickable { onFieldChange { copy(goldPricePerGram = MoneyInputFormatter.format(liveGoldPrice)) } }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Property & Land detailed input section
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = stringResource(R.string.faraidh_asset_property),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SaatColors.Slate800,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                SegmentedButton(
                    selected = !estate.inputPropertyDetailed,
                    onClick = { onFieldChange { copy(inputPropertyDetailed = false) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = SaatColors.DeepEmerald, activeContentColor = Color.White)
                ) { Text(stringResource(R.string.faraidh_property_mode_value)) }
                SegmentedButton(
                    selected = estate.inputPropertyDetailed,
                    onClick = { onFieldChange { copy(inputPropertyDetailed = true) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = SaatColors.DeepEmerald, activeContentColor = Color.White)
                ) { Text(stringResource(R.string.faraidh_property_mode_detailed)) }
            }
            if (!estate.inputPropertyDetailed) {
                MoneyField("", estate.propertyValue) {
                    onFieldChange { copy(propertyValue = it) }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.faraidh_property_list_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.Slate500
                    )
                    estate.properties.forEach { prop ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SaatColors.SoftGrey, RoundedCornerShape(12.dp))
                                .background(SaatColors.PureWhite)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prop.name.ifBlank { "Properti" },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SaatColors.Slate800
                                )
                                if (prop.sizeSqm.isNotBlank()) {
                                    Text(
                                        text = "${prop.sizeSqm} m²",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SaatColors.Slate500
                                    )
                                }
                                Text(
                                    text = currency.format(MoneyInputFormatter.parseAmount(prop.value)),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SaatColors.DeepEmerald
                                )
                            }
                            IconButton(
                                onClick = {
                                    onFieldChange {
                                        copy(properties = properties.filterNot { it.id == prop.id })
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = SaatColors.Danger
                                )
                            }
                        }
                    }

                    // Section to add a new property item
                    var newPropName by remember { mutableStateOf("") }
                    var newPropSize by remember { mutableStateOf("") }
                    var newPropVal by remember { mutableStateOf("") }

                    Surface(
                         shape = RoundedCornerShape(14.dp),
                         color = SaatColors.LightGrey.copy(alpha = 0.5f),
                         modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                         Column(modifier = Modifier.padding(10.dp)) {
                             OutlinedTextField(
                                 value = newPropName,
                                 onValueChange = { newPropName = it },
                                 label = { Text(stringResource(R.string.faraidh_property_name_label)) },
                                 placeholder = { Text(stringResource(R.string.faraidh_property_name_hint)) },
                                 modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                 singleLine = true,
                                 shape = RoundedCornerShape(10.dp),
                                 colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaatColors.Teal)
                             )
                             Row(
                                 modifier = Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.spacedBy(8.dp)
                             ) {
                                 OutlinedTextField(
                                     value = newPropSize,
                                     onValueChange = { newPropSize = it },
                                     label = { Text(stringResource(R.string.faraidh_property_size_label)) },
                                     placeholder = { Text(stringResource(R.string.faraidh_property_size_hint)) },
                                     modifier = Modifier.weight(1f),
                                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                     singleLine = true,
                                     shape = RoundedCornerShape(10.dp),
                                     colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaatColors.Teal)
                                 )
                                 OutlinedTextField(
                                     value = MoneyInputFormatter.format(newPropVal),
                                     onValueChange = { newPropVal = MoneyInputFormatter.format(it) },
                                     label = { Text(stringResource(R.string.faraidh_property_value_label)) },
                                     placeholder = { Text("0") },
                                     modifier = Modifier.weight(1.2f),
                                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                     singleLine = true,
                                     shape = RoundedCornerShape(10.dp),
                                     colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaatColors.Teal)
                                 )
                             }
                             Spacer(Modifier.height(8.dp))
                             OutlinedButton(
                                 onClick = {
                                     if (newPropName.isNotBlank() && newPropVal.isNotBlank()) {
                                         val newItem = FaraidhPropertyItem(
                                             id = System.currentTimeMillis().toString(),
                                             name = newPropName.trim(),
                                             sizeSqm = newPropSize.trim(),
                                             value = newPropVal
                                         )
                                         onFieldChange {
                                             copy(properties = properties + newItem)
                                         }
                                         newPropName = ""
                                         newPropSize = ""
                                         newPropVal = ""
                                     }
                                 },
                                 modifier = Modifier.fillMaxWidth(),
                                 shape = RoundedCornerShape(10.dp),
                                 border = androidx.compose.foundation.BorderStroke(1.dp, SaatColors.Teal)
                             ) {
                                 Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SaatColors.Teal)
                                 Spacer(Modifier.width(4.dp))
                                 Text(stringResource(R.string.faraidh_property_add_btn), color = SaatColors.Teal, style = MaterialTheme.typography.labelMedium)
                             }
                         }
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.faraidh_has_house), fontWeight = FontWeight.Medium, color = SaatColors.Slate800)
                Text(stringResource(R.string.faraidh_has_house_hint), style = MaterialTheme.typography.bodySmall, color = SaatColors.Slate500)
            }
            Switch(
                checked = estate.hasResidentialProperty,
                onCheckedChange = { checked -> onFieldChange { copy(hasResidentialProperty = checked) } },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SaatColors.Teal)
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
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaatColors.Teal)
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
            color = SaatColors.Slate500,
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
        color = SaatColors.ScreenBackground
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = SaatColors.Slate800)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun MoneyField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = MoneyInputFormatter.format(value),
        onValueChange = { onChange(MoneyInputFormatter.format(it)) },
        label = { Text(label) },
        placeholder = { Text("0") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SaatColors.Teal, focusedLabelColor = SaatColors.DeepEmerald)
    )
}

@Composable
fun EstateComputationSummary(computation: EstateComputation, currency: NumberFormat) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PrayerMint,
        border = androidx.compose.foundation.BorderStroke(1.dp, SaatColors.Teal.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(R.string.faraidh_estate_summary), fontWeight = FontWeight.Bold, color = SaatColors.DeepEmerald)
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
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = SaatColors.Teal.copy(alpha = 0.25f))
            SummaryRow(
                stringResource(R.string.faraidh_tarikah_net),
                currency.format(computation.netEstate),
                bold = true
            )
            if (computation.hasResidentialProperty) {
                Text(
                    stringResource(R.string.faraidh_house_included),
                    style = MaterialTheme.typography.labelSmall,
                    color = SaatColors.Slate500,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = SaatColors.Slate500)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = if (bold) SaatColors.DeepEmerald else SaatColors.Slate800
        )
    }
}
