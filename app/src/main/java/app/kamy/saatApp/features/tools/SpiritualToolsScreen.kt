package app.kamy.saatApp.features.tools

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

private data class SpiritualToolItem(
    val icon: ImageVector,
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
            Icons.Filled.Explore,
            R.string.qibla_title,
            R.string.qibla_account_subtitle,
            "qibla",
            AlKhatibColors.DeepEmerald,
            AlKhatibColors.Teal
        ),
        SpiritualToolItem(
            Icons.Filled.AutoStories,
            R.string.doa_zikir_title,
            R.string.doa_zikir_account_subtitle,
            "doa-zikir",
            AlKhatibColors.Teal,
            AlKhatibColors.DeepEmerald
        ),
        SpiritualToolItem(
            Icons.Filled.Favorite,
            R.string.dhikr_title,
            R.string.dhikr_account_subtitle,
            "dhikr",
            AlKhatibColors.GoldDeep,
            AlKhatibColors.Gold
        ),
        SpiritualToolItem(
            Icons.Filled.Calculate,
            R.string.zakat_title,
            R.string.zakat_account_subtitle,
            "zakat",
            AlKhatibColors.IndigoAccent,
            AlKhatibColors.Teal
        ),
        SpiritualToolItem(
            Icons.Filled.FamilyRestroom,
            R.string.faraidh_title,
            R.string.faraidh_account_subtitle,
            "faraidh",
            AlKhatibColors.GoldDeep,
            AlKhatibColors.DeepEmerald
        ),
        SpiritualToolItem(
            Icons.Filled.NightsStay,
            R.string.qiyam_title,
            R.string.qiyam_account_subtitle,
            "qiyam",
            AlKhatibColors.DeepEmerald,
            AlKhatibColors.IndigoAccent
        ),
        SpiritualToolItem(
            Icons.Filled.Shield,
            R.string.manzil_title,
            R.string.manzil_account_subtitle,
            "manzil",
            AlKhatibColors.DeepEmerald,
            AlKhatibColors.GoldDeep
        ),
        SpiritualToolItem(
            Icons.Filled.WaterDrop,
            R.string.title_wudhu_guide,
            R.string.wudhu_subtitle,
            "wudhu",
            AlKhatibColors.DeepEmerald,
            AlKhatibColors.Teal
        ),
        SpiritualToolItem(
            Icons.Filled.MenuBook,
            R.string.tajweed_title,
            R.string.tajweed_subtitle,
            "tajweed",
            AlKhatibColors.DeepEmerald,
            AlKhatibColors.GoldDeep
        ),
        SpiritualToolItem(
            Icons.Filled.AutoStories,
            R.string.title_dzikir_kubro,
            R.string.dhikr_account_subtitle,
            "dhikr/dzikiralmathuratkubro",
            AlKhatibColors.DeepEmerald,
            AlKhatibColors.Teal
        ),
        SpiritualToolItem(
            Icons.Filled.AutoStories,
            R.string.title_dzikir_sughro,
            R.string.dhikr_account_subtitle,
            "dhikr/dzikiralmathuratsughro",
            AlKhatibColors.DeepEmerald,
            AlKhatibColors.Teal
        ),
        SpiritualToolItem(
            Icons.Filled.AutoStories,
            R.string.title_fadhilah_doa,
            R.string.doa_zikir_account_subtitle,
            "dhikr/fadhilahdoa",
            AlKhatibColors.DeepEmerald,
            AlKhatibColors.Teal
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal)
                .padding(top = AlKhatibSpacing.md, bottom = AlKhatibSpacing.lg)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            AlKhatibColors.DeepEmerald.copy(alpha = 0.14f),
                            AlKhatibColors.Teal.copy(alpha = 0.08f),
                            AlKhatibColors.Gold.copy(alpha = 0.06f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            AlKhatibColors.DeepEmerald.copy(alpha = 0.25f),
                            AlKhatibColors.Teal.copy(alpha = 0.12f)
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
                    color = AlKhatibColors.DeepEmerald
                )
                Text(
                    text = stringResource(R.string.spiritual_tools_tab_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlKhatibColors.Slate500,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AlKhatibSpacing.screenHorizontal,
                end = AlKhatibSpacing.screenHorizontal,
                bottom = floatingNavBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tools, key = { it.route }) { tool ->
                SpiritualToolCard(
                    icon = tool.icon,
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
    icon: ImageVector,
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
            .background(AlKhatibColors.PureWhite)
            .border(
                width = 1.dp,
                color = AlKhatibColors.SoftGrey.copy(alpha = 0.7f),
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
                icon,
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
                color = AlKhatibColors.Slate800
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AlKhatibColors.Slate500,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AlKhatibColors.Teal,
            modifier = Modifier.size(22.dp)
        )
    }
}
