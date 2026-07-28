package app.kamy.saatApp.features.today.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import java.util.Calendar

@Composable
fun TodayHeader(
    cityName: String?,
    locationStatus: String? = null,
    hijriLabel: String?,
    gregorianLabel: String?,
    onLocationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val weekdays = stringArrayResource(R.array.weekday_names)
    val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    val localDayName = remember(dayIndex, weekdays) {
        if (dayIndex in weekdays.indices) weekdays[dayIndex] else ""
    }

    val context = LocalContext.current
    val languageCode = context.resources.configuration.locales[0].language
    val isIndonesianOrMalay = languageCode == "in" || languageCode == "id" || languageCode == "ms"
    val hijriSuffix = if (isIndonesianOrMalay) " H" else " AH"

    val formattedHijri = remember(hijriLabel, hijriSuffix) {
        if (!hijriLabel.isNullOrBlank()) {
            if (hijriLabel.endsWith(" H") || hijriLabel.endsWith(" AH")) hijriLabel
            else "$hijriLabel$hijriSuffix"
        } else null
    }

    val locationText = cityName ?: locationStatus ?: stringResource(R.string.locating)

    val displayLocation = remember(locationText) {
        val raw = if (locationText.contains(",")) {
            val parts = locationText.split(",")
            if (parts.size == 2 && parts[0].trim().toDoubleOrNull() != null) {
                locationText
            } else {
                parts[0].trim()
            }
        } else {
            locationText
        }
        raw.replace("Kecamatan", "Kec", ignoreCase = true)
           .replace("Kelurahan", "Kel", ignoreCase = true)
           .replace("Subdistrict", "Subdist", ignoreCase = true)
           .replace("Township", "Twp", ignoreCase = true)
           .replace("District", "Dist", ignoreCase = true)
           .replace("Municipality", "Mun", ignoreCase = true)
           .replace("County", "Co.", ignoreCase = true)
           .replace("Kampung", "Kg", ignoreCase = true)
           .replace("Kampong", "Kg", ignoreCase = true)
           .replace("Taman", "Tmn", ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Assalamualaikum Wr Wb",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                var showHijri by remember { mutableStateOf(false) }
                LaunchedEffect(formattedHijri) {
                    if (!formattedHijri.isNullOrBlank()) {
                        while (true) {
                            kotlinx.coroutines.delay(5000)
                            showHijri = !showHijri
                        }
                    }
                }

                AnimatedContent(
                    targetState = showHijri,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "dateTransition"
                ) { targetShowHijri ->
                    val dateText = remember(targetShowHijri, localDayName, gregorianLabel, formattedHijri) {
                        val prefix = if (localDayName.isNotEmpty()) "$localDayName, " else ""
                        if (targetShowHijri && !formattedHijri.isNullOrBlank()) {
                            "$prefix$formattedHijri"
                        } else {
                            "$prefix${gregorianLabel.orEmpty()}"
                        }
                    }
                    val textColor = if (targetShowHijri) SaatColors.DeepEmerald else Color.Black

                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(
                            onClickLabel = stringResource(R.string.date_switch_a11y),
                            onClick = { showHijri = !showHijri }
                        )
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Location Badge on the right
            Surface(
                onClick = onLocationClick,
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_location_custom),
                        contentDescription = stringResource(R.string.location_enable),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = displayLocation,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                }
            }
        }
    }
}

