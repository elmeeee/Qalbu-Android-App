package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.RamadanDayInfo

@Composable
fun TodayRamadanCard(
    info: RamadanDayInfo,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundRes: Int = R.drawable.ramadan_home_night
) {
    val progressFraction = remember(info.dayNumber, info.totalDays) {
        if (info.totalDays > 0) {
            (info.dayNumber.toFloat() / info.totalDays.toFloat()).coerceIn(0.04f, 1f)
        } else {
            0.4f
        }
    }

    Surface(
        onClick = onTap,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0C382E),
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFF265A4D).copy(alpha = 0.65f))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Background Artwork (ramadan_home_night / ramadan_home_day)
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Deep Emerald Green gradient blend covering left atmospheric area
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.00f to Color(0xFF0C382E),
                                0.38f to Color(0xFF0C382E).copy(alpha = 0.96f),
                                0.54f to Color(0xFF0C382E).copy(alpha = 0.72f),
                                0.68f to Color(0xFF0C382E).copy(alpha = 0.25f),
                                0.82f to Color.Transparent,
                                1.00f to Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                // Top section: Moon Icon, Title, and Right Arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌙",
                        fontSize = 20.sp
                    )
                    Text(
                        text = stringResource(R.string.ramadan_mubarak),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Subtitle: e.g. "12 Ramadan 1448 H"
                Text(
                    text = info.hijriLabel.ifBlank {
                        stringResource(R.string.ramadan_date_format, info.dayNumber, info.hijriYear)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White.copy(alpha = 0.92f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Middle section: Imsak and Iftar Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imsak Chip
                    RamadanTimingChip(
                        iconRes = R.drawable.ramadan_asset_mangkok,
                        label = stringResource(R.string.ramadan_imsak),
                        time = info.imsakTime ?: "--:--"
                    )

                    // Iftar Chip
                    RamadanTimingChip(
                        iconRes = R.drawable.ramadan_asset_kurma,
                        label = stringResource(R.string.ramadan_iftar),
                        time = info.iftarTime ?: "--:--"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom section: Progress Info & Circular Arrow Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left Column: Progress Header + Progress Bar
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.ramadan_progress_title),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${info.dayNumber}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = " / ${info.totalDays} " + stringResource(R.string.ramadan_days_unit),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        // Progress Bar Track & Soft Sage/Mint Indicator Fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F362D).copy(alpha = 0.85f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFD4EFE1),
                                                Color(0xFFB8E2CD)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // Right Side Circular Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0B332A))
                            .border(BorderStroke(1.dp, Color(0xFF225749)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View details",
                            tint = Color(0xFF86EFAC),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RamadanTimingChip(
    iconRes: Int,
    label: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF103E33).copy(alpha = 0.88f),
        border = BorderStroke(1.dp, Color(0xFF2E6B5C).copy(alpha = 0.75f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(36.dp)
            )

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodayRamadanCardPreview() {
    TodayRamadanCard(
        info = RamadanDayInfo(
            isRamadan = true,
            dayNumber = 12,
            totalDays = 30,
            hijriYear = 1448,
            hijriLabel = "12 Ramadan 1448 H",
            imsakTime = "04:35",
            iftarTime = "18:11"
        ),
        backgroundRes = R.drawable.ramadan_home_night,
        onTap = {}
    )
}
