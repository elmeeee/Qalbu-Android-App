package app.kamy.qalbuApp.features.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset

private data class SpiritualToolItem(
    val icon: ImageVector,
    val titleRes: Int,
    val subtitleRes: Int,
    val route: String
)

@Composable
fun SpiritualToolsScreen(
    onOpenTool: (String) -> Unit
) {
    val tools = listOf(
        SpiritualToolItem(Icons.Filled.Explore, R.string.qibla_title, R.string.qibla_account_subtitle, "qibla"),
        SpiritualToolItem(Icons.Filled.AutoStories, R.string.doa_zikir_title, R.string.doa_zikir_account_subtitle, "doa-zikir"),
        SpiritualToolItem(Icons.Filled.Favorite, R.string.dhikr_title, R.string.dhikr_account_subtitle, "dhikr"),
        SpiritualToolItem(Icons.Filled.Calculate, R.string.zakat_title, R.string.zakat_account_subtitle, "zakat"),
        SpiritualToolItem(Icons.Filled.NightsStay, R.string.qiyam_title, R.string.qiyam_account_subtitle, "qiyam")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset()
            .padding(horizontal = AlKhatibSpacing.screenHorizontal)
    ) {
        Spacer(Modifier.height(AlKhatibSpacing.md))
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
            modifier = Modifier.padding(top = 4.dp, bottom = AlKhatibSpacing.lg)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(tools, key = { it.route }) { tool ->
                SpiritualToolCard(
                    icon = tool.icon,
                    title = stringResource(tool.titleRes),
                    subtitle = stringResource(tool.subtitleRes),
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
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AlKhatibColors.PureWhite)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = AlKhatibColors.DeepEmerald,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate800
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = AlKhatibColors.Slate500,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
