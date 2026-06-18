package app.kamy.qalbuApp.features.today.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import app.kamy.qalbuApp.design.components.AlKhatibSkeletonCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import app.kamy.qalbuApp.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.util.Calendar

private const val DATE_ALTERNATE_MS = 4_000L
private const val DAY_ALTERNATE_MS = 4_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayHeader(
    cityName: String?,
    locationStatus: String? = null,
    hijriLabel: String?,
    gregorianLabel: String?,
    avatarUrl: String? = null,
    isProfileLoading: Boolean = false,
    onAccountClick: () -> Unit = {},
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
                .padding(
                    horizontal = AlKhatibSpacing.screenHorizontal,
                    vertical = AlKhatibSpacing.md
                ),
            horizontalArrangement = Arrangement.spacedBy(AlKhatibSpacing.md)
        ) {
            HeaderAvatar(
                avatarUrl = avatarUrl,
                isProfileLoading = isProfileLoading,
                onAccountClick = onAccountClick
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

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
                        fadeIn(animationSpec = tween(280)) togetherWith
                            fadeOut(animationSpec = tween(220))
                    },
                    label = "headerDate"
                ) { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onLocationClick)
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = cityName ?: locationStatus ?: stringResource(R.string.locating),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✦",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(3.dp))
                AnimatedContent(
                    targetState = dayName,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(280)) togetherWith
                            fadeOut(animationSpec = tween(220))
                    },
                    label = "headerDay"
                ) { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun HeaderAvatar(
    avatarUrl: String?,
    isProfileLoading: Boolean,
    onAccountClick: () -> Unit
) {
    Surface(
        onClick = onAccountClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.size(42.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            AlKhatibColors.DeepEmerald.copy(alpha = 0.4f),
                            AlKhatibColors.Gold.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isProfileLoading -> AlKhatibSkeletonCircle(size = 36.dp)
                !avatarUrl.isNullOrBlank() -> AsyncImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.account),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                else -> Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(R.string.account),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberRotatingDayName(): String {
    val weekdays = stringArrayResource(R.array.weekday_names)
    val weekdaysAr = stringArrayResource(R.array.weekday_names_ar)
    val dayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
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
