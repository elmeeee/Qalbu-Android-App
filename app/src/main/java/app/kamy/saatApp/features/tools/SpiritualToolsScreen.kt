package app.kamy.saatApp.features.tools

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

private data class SpiritualToolItem(
    @DrawableRes val iconRes: Int,
    val titleRes: Int,
    val route: String,
    val accentStart: Color,
    val accentEnd: Color
)

@Composable
fun SpiritualToolsScreen(
    onOpenTool: (String) -> Unit
) {
    val tools = remember {
        listOf(
            SpiritualToolItem(
                R.drawable.ic_radio_custom,
                R.string.radio_quran_title,
                "radio",
                SaatColors.DeepEmerald,
                SaatColors.Gold
            ),
            SpiritualToolItem(
                R.drawable.ic_asmaulhusna_custom,
                R.string.asmaul_husna_title,
                "asmaul-husna",
                SaatColors.GoldDeep,
                SaatColors.DeepEmerald
            ),
            SpiritualToolItem(
                R.drawable.ic_qibla,
                R.string.qibla_title,
                "qibla",
                SaatColors.DeepEmerald,
                SaatColors.Teal
            ),
            SpiritualToolItem(
                R.drawable.ic_dua,
                R.string.doa_zikir_title,
                "doa-zikir",
                SaatColors.Teal,
                SaatColors.DeepEmerald
            ),
            SpiritualToolItem(
                R.drawable.ic_dhikr,
                R.string.dhikr_title,
                "dhikr",
                SaatColors.GoldDeep,
                SaatColors.Gold
            ),
            SpiritualToolItem(
                R.drawable.ic_zakat,
                R.string.zakat_title,
                "zakat",
                SaatColors.IndigoAccent,
                SaatColors.Teal
            ),
            SpiritualToolItem(
                R.drawable.ic_zakat,
                R.string.faraidh_title,
                "faraidh",
                SaatColors.GoldDeep,
                SaatColors.DeepEmerald
            ),
            SpiritualToolItem(
                R.drawable.ic_manzil,
                R.string.manzil_title,
                "manzil",
                SaatColors.DeepEmerald,
                SaatColors.GoldDeep
            ),
            SpiritualToolItem(
                R.drawable.ic_prayer_rug,
                R.string.sunnah_prayer_title,
                "sunnah-prayer",
                SaatColors.GoldDeep,
                SaatColors.DeepEmerald
            ),
            SpiritualToolItem(
                R.drawable.ic_prayer_rug,
                R.string.jenazah_prayer_title,
                "jenazah",
                SaatColors.DeepEmerald,
                SaatColors.GoldDeep
            ),
            SpiritualToolItem(
                R.drawable.ic_rice,
                R.string.fidyah_title,
                "fidyah",
                SaatColors.Teal,
                SaatColors.GoldDeep
            ),
            SpiritualToolItem(
                R.drawable.ic_faraidh_doc,
                R.string.encyclopedia_title,
                "encyclopedia",
                SaatColors.DeepEmerald,
                SaatColors.Teal
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SaatSpacing.screenHorizontal,
                    vertical = SaatSpacing.md
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✦",
                        style = MaterialTheme.typography.labelMedium,
                        color = SaatColors.GoldDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(SaatSpacing.sm))
                    Text(
                        text = stringResource(R.string.spiritual_tools_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = SaatColors.DeepEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = SaatColors.DeepEmerald.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = stringResource(R.string.spiritual_tools_badge_count, tools.size),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.spiritual_tools_tab_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 18.dp, top = 4.dp)
            )
            Spacer(Modifier.height(SaatSpacing.sm))
            Box(
                modifier = Modifier
                    .height(2.5.dp)
                    .width(52.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                SaatColors.GoldDeep,
                                SaatColors.GoldDeep.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = SaatSpacing.screenHorizontal,
                end = SaatSpacing.screenHorizontal,
                bottom = floatingNavBottomPadding() + 20.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tools, key = { it.route }) { tool ->
                SpiritualToolGridCard(
                    tool = tool,
                    onClick = { onOpenTool(tool.route) }
                )
            }
        }
    }
}

@Composable
private fun SpiritualToolGridCard(
    tool: SpiritualToolItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val titleStr = stringResource(tool.titleRes)
    val borderGradient = remember(tool.accentStart, tool.accentEnd) {
        Brush.linearGradient(
            colors = listOf(
                tool.accentStart.copy(alpha = 0.30f),
                tool.accentEnd.copy(alpha = 0.10f)
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = tool.accentStart.copy(alpha = 0.15f)),
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, borderGradient),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large Colored Vector Icon
            Icon(
                painter = painterResource(tool.iconRes),
                contentDescription = null,
                tint = tool.accentStart,
                modifier = Modifier.size(42.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Centered Feature Title Name
            Text(
                text = titleStr,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp, lineHeight = 16.sp),
                fontWeight = FontWeight.SemiBold,
                color = SaatColors.Slate900,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}