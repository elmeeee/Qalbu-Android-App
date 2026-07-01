package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.background
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
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
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
    val greeting = rememberGreeting()

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .tabContentStatusBarInset()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Greeting: e.g. "Selamat Pagi" / "Good Morning"
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Day and Gregorian Date: e.g. "Rabu, 1 Juli 2026"
                Text(
                    text = buildString {
                        if (localDayName.isNotEmpty()) {
                            append(localDayName)
                            append(", ")
                        }
                        append(gregorianLabel.orEmpty())
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Hijri Date: e.g. "16 Muharram 1448 H"
                if (!formattedHijri.isNullOrBlank()) {
                    Text(
                        text = formattedHijri,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = AlKhatibColors.GoldDeep,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Clickable Location Badge
                Surface(
                    onClick = onLocationClick,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = stringResource(R.string.location_enable),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun rememberGreeting(): String {
    val morning = stringResource(R.string.greeting_morning)
    val afternoon = stringResource(R.string.greeting_afternoon)
    val evening = stringResource(R.string.greeting_evening)
    val night = stringResource(R.string.greeting_night)
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 3..11 -> morning
        in 12..17 -> afternoon
        in 18..20 -> evening
        else -> night
    }
}
