package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors

@Composable
fun SpiritualToolsRow(
    onOpenQibla: () -> Unit,
    onOpenDhikr: () -> Unit,
    onOpenZakat: () -> Unit,
    onOpenQiyam: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.spiritual_tools_title),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate500,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ToolItem(Icons.Filled.Explore, stringResource(R.string.qibla_title), onOpenQibla)
            ToolItem(Icons.Filled.Favorite, stringResource(R.string.dhikr_title), onOpenDhikr)
            ToolItem(Icons.Filled.Calculate, stringResource(R.string.zakat_title), onOpenZakat)
            ToolItem(Icons.Filled.NightsStay, stringResource(R.string.qiyam_title), onOpenQiyam)
        }
    }
}

@Composable
private fun ToolItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = AlKhatibColors.DeepEmerald)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AlKhatibColors.Slate800,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
