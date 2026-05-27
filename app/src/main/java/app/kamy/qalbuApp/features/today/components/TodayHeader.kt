package app.kamy.qalbuApp.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.qalbuApp.design.theme.AlKhatibColors

/**
 * Mirrors iOS Features/Discovery/Components/TodayDiscoveryHeaderView.swift.
 *
 * Top header with greeting/date + location + a small "Today" badge. Sits above
 * the prayer dashboard and verse card.
 */
@Composable
fun TodayHeader(
    cityName: String?,
    locationStatus: String? = null,
    hijriLabel: String?,
    gregorianLabel: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.WbSunny,
                contentDescription = null,
                tint = AlKhatibColors.Gold
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = hijriLabel ?: gregorianLabel ?: "Today",
                    style = MaterialTheme.typography.titleMedium,
                    color = AlKhatibColors.DeepEmerald,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = AlKhatibColors.Slate500,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = cityName ?: locationStatus ?: "Locating…",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlKhatibColors.Slate500
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AlKhatibColors.DeepEmerald.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "✦ Today",
                    color = AlKhatibColors.DeepEmerald,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        // Decorative gold-emerald-gold gradient divider (iOS parity).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            AlKhatibColors.DeepEmerald.copy(alpha = 0.0f),
                            AlKhatibColors.Gold.copy(alpha = 0.5f),
                            AlKhatibColors.DeepEmerald.copy(alpha = 0.0f)
                        )
                    )
                )
        )
    }
}
