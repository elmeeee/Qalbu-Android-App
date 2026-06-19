package app.kamy.saatApp.features.today.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import kotlinx.coroutines.delay
import java.util.Calendar

private const val DATE_ALTERNATE_MS = 4_000L
private const val DAY_ALTERNATE_MS = 4_000L

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
    val dayName = rememberRotatingDayName()
    val canAlternate = !hijriLabel.isNullOrBlank() && !gregorianLabel.isNullOrBlank()
    var showHijri by remember(hijriLabel, gregorianLabel) { mutableStateOf(false) }

    LaunchedEffect(hijriLabel, gregorianLabel) {
        when {
            canAlternate -> {
                showHijri = false
                while (true) {
                    delay(DATE_ALTERNATE_MS)
                    showHijri = !showHijri
                }
            }
            !hijriLabel.isNullOrBlank() -> showHijri = true
            else -> showHijri = false
        }
    }

    val displayDate = when {
        canAlternate && showHijri -> hijriLabel!!
        canAlternate -> gregorianLabel!!
        !gregorianLabel.isNullOrBlank() -> gregorianLabel
        else -> hijriLabel.orEmpty()
    }

    val dateSwitchA11y = stringResource(R.string.date_switch_a11y)
    val locationText = cityName ?: locationStatus ?: stringResource(R.string.locating)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .tabContentStatusBarInset()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                AnimatedContent(
                    targetState = displayDate,
                    modifier = Modifier
                        .then(
                            if (canAlternate) Modifier.clickable { showHijri = !showHijri }
                            else Modifier
                        )
                        .semantics {
                            if (canAlternate) {
                                contentDescription = dateSwitchA11y
                            }
                        },
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(180)))
                            .using(SizeTransform(clip = false) { _, _ -> snap() })
                    },
                    label = "headerDate"
                ) { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .clickable(onClick = onLocationClick)
                        .padding(top = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier.width(80.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                AnimatedContent(
                    targetState = dayName,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(180)))
                            .using(SizeTransform(clip = false) { _, _ -> snap() })
                    },
                    label = "headerDay"
                ) { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
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
private fun rememberRotatingDayName(): String {
    val weekdays = stringArrayResource(R.array.weekday_names)
    val weekdaysAr = stringArrayResource(R.array.weekday_names_ar)
    val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
    val names = listOf(weekdays[dayIndex], weekdaysAr[dayIndex])
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(DAY_ALTERNATE_MS)
            index = (index + 1) % names.size
        }
    }

    return names[index]
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
