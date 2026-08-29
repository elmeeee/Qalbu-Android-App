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

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.lerp

@Composable
fun TodayHeader(
    cityName: String?,
    locationStatus: String? = null,
    hijriLabel: String?,
    gregorianLabel: String?,
    scrollProgress: Float = 0f,
    isDarkBackground: Boolean = false,
    onLocationClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
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

    val progress = scrollProgress.coerceIn(0f, 1f)

    val headerBgColor = lerp(Color.Transparent, SaatColors.HomeBg, progress)

    val initialTextColor = if (isDarkBackground) Color.White else SaatColors.HomeDarkGreen
    val targetTextColor = SaatColors.HomeDarkGreen
    val greetingColor = lerp(initialTextColor, targetTextColor, progress)
    val dateTextColor = lerp(initialTextColor, targetTextColor, progress)

    val initialBadgeBg = if (isDarkBackground) Color.White.copy(alpha = 0.22f) else SaatColors.HomeDarkGreen.copy(alpha = 0.10f)
    val targetBadgeBg = SaatColors.HomeDarkGreen.copy(alpha = 0.15f)
    val badgeBgColor = lerp(initialBadgeBg, targetBadgeBg, progress)

    val initialBadgeBorder = if (isDarkBackground) Color.White.copy(alpha = 0.45f) else SaatColors.HomeDarkGreen.copy(alpha = 0.25f)
    val targetBadgeBorder = SaatColors.HomeDarkGreen.copy(alpha = 0.35f)
    val badgeBorderColor = lerp(initialBadgeBorder, targetBadgeBorder, progress)

    val initialBadgeContent = if (isDarkBackground) Color.White else SaatColors.HomeDarkGreen
    val targetBadgeContent = SaatColors.HomeDarkGreen
    val badgeContentColor = lerp(initialBadgeContent, targetBadgeContent, progress)

    val elevationDp = lerp(0.dp, 2.dp, progress)
    val dividerColor = lerp(Color.Transparent, SaatColors.HomeDarkGreen.copy(alpha = 0.15f), progress)
    val isScrolled = progress > 0.05f

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Parallax Glass Backdrop Layer
        if (isScrolled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        translationY = (-progress * 20f).coerceIn(-30f, 0f)
                    }
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(radius = 16.dp * progress)
                        } else Modifier
                    )
                    .background(
                        SaatColors.HomeBg.copy(alpha = 0.88f * progress)
                    )
                    .drawWithContent {
                        drawContent()
                        if (progress > 0.1f) {
                            drawLine(
                                color = SaatColors.HomeDarkGreen.copy(alpha = 0.12f * progress),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isScrolled) Color.Transparent else headerBgColor,
            shadowElevation = if (isScrolled) 0.dp else elevationDp
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .tabContentStatusBarInset()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Assalamu'alaikum",
                        style = MaterialTheme.typography.labelSmall,
                        color = greetingColor,
                        fontWeight = FontWeight.SemiBold,
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

                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = dateTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable(
                                onClick = onCalendarClick
                            )
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Location Badge on the right
                Surface(
                    onClick = onLocationClick,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = badgeBgColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorderColor),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_location_custom),
                            contentDescription = stringResource(R.string.location_enable),
                            tint = badgeContentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = displayLocation,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 140.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
        }
    }
}
}

