package app.kamy.saatApp.features.tools

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

private data class SpiritualToolItem(
    @DrawableRes val iconRes: Int,
    val titleRes: Int,
    val subtitleRes: Int,
    val route: String,
    val accentStart: androidx.compose.ui.graphics.Color,
    val accentEnd: androidx.compose.ui.graphics.Color
)

@Composable
fun SpiritualToolsScreen(
    onOpenTool: (String) -> Unit
) {
    val tools = listOf(
        SpiritualToolItem(
            R.drawable.ic_qibla,
            R.string.qibla_title,
            R.string.qibla_account_subtitle,
            "qibla",
            SaatColors.DeepEmerald,
            SaatColors.Teal
        ),
        SpiritualToolItem(
            R.drawable.ic_dua,
            R.string.doa_zikir_title,
            R.string.doa_zikir_account_subtitle,
            "doa-zikir",
            SaatColors.Teal,
            SaatColors.DeepEmerald
        ),
        SpiritualToolItem(
            R.drawable.ic_dhikr,
            R.string.dhikr_title,
            R.string.dhikr_account_subtitle,
            "dhikr",
            SaatColors.GoldDeep,
            SaatColors.Gold
        ),
        SpiritualToolItem(
            R.drawable.ic_zakat,
            R.string.zakat_title,
            R.string.zakat_account_subtitle,
            "zakat",
            SaatColors.IndigoAccent,
            SaatColors.Teal
        ),
        SpiritualToolItem(
            R.drawable.ic_zakat,
            R.string.faraidh_title,
            R.string.faraidh_account_subtitle,
            "faraidh",
            SaatColors.GoldDeep,
            SaatColors.DeepEmerald
        ),
        SpiritualToolItem(
            R.drawable.ic_qiyam,
            R.string.qiyam_title,
            R.string.qiyam_account_subtitle,
            "qiyam",
            SaatColors.DeepEmerald,
            SaatColors.IndigoAccent
        ),
        SpiritualToolItem(
            R.drawable.ic_manzil,
            R.string.manzil_title,
            R.string.manzil_account_subtitle,
            "manzil",
            SaatColors.DeepEmerald,
            SaatColors.GoldDeep
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SaatSpacing.screenHorizontal)
                .padding(top = SaatSpacing.md, bottom = SaatSpacing.lg)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            SaatColors.DeepEmerald.copy(alpha = 0.14f),
                            SaatColors.Teal.copy(alpha = 0.08f),
                            SaatColors.Gold.copy(alpha = 0.06f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            SaatColors.DeepEmerald.copy(alpha = 0.25f),
                            SaatColors.Teal.copy(alpha = 0.12f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.spiritual_tools_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
                Text(
                    text = stringResource(R.string.spiritual_tools_tab_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaatColors.Slate500,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = SaatSpacing.screenHorizontal,
                end = SaatSpacing.screenHorizontal,
                bottom = floatingNavBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tools, key = { it.route }) { tool ->
                SpiritualToolCard(
                    iconRes = tool.iconRes,
                    title = stringResource(tool.titleRes),
                    subtitle = stringResource(tool.subtitleRes),
                    accentStart = tool.accentStart,
                    accentEnd = tool.accentEnd,
                    onClick = { onOpenTool(tool.route) }
                )
            }
        }
    }
}

@Composable
private fun SpiritualToolCard(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    accentStart: androidx.compose.ui.graphics.Color,
    accentEnd: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SaatColors.PureWhite)
            .border(
                width = 1.dp,
                color = SaatColors.SoftGrey.copy(alpha = 0.7f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(listOf(accentStart.copy(alpha = 0.18f), accentEnd.copy(alpha = 0.10f)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = accentStart,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SaatColors.Slate800
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = SaatColors.Teal,
            modifier = Modifier.size(22.dp)
        )
    }
}