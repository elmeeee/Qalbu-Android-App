package app.kamy.saatApp.features.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.OptionalWorshipHabit
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.features.today.OptionalHabitUiItem
import app.kamy.saatApp.features.today.PrayerTrackerUiState
import app.kamy.saatApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.ui.feedback.rememberTapHaptic

import androidx.compose.foundation.Image
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.ui.common.toVerseTranslationPlainText

@Composable
fun PrayerTrackerCard(
    state: PrayerTrackerUiState,
    verse: RandomAyahPayload? = null,
    referenceLabel: String? = null,
    isAfterIsha: Boolean = false,
    isQuranReadToday: Boolean = false,
    onShowToastMessage: (String) -> Unit = {},
    onTogglePrayer: (PrayerType) -> Unit = {},
    onToggleOptional: (OptionalWorshipHabit) -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onShareVerse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPrayerOverridden by remember {
        mutableStateOf<Boolean?>(if (PrayerTrackerStore.isOptionalCompleted(context, OptionalWorshipHabit.QIYAMUL_LAIL)) true else null)
    }
    var isQuranDone by remember(isQuranReadToday) {
        mutableStateOf(isQuranReadToday || PrayerTrackerStore.isOptionalCompleted(context, OptionalWorshipHabit.READ_QURAN))
    }
    var isDhikrDone by remember {
        mutableStateOf(PrayerTrackerStore.isOptionalCompleted(context, OptionalWorshipHabit.DHIKR_MORNING))
    }
    var isSunnahDone by remember {
        mutableStateOf(PrayerTrackerStore.isOptionalCompleted(context, OptionalWorshipHabit.DHUHA))
    }

    val completedFardhuCount = state.completedPrayers.size
    val isPrayerClickable = isAfterIsha || completedFardhuCount >= 5
    val isPrayerDone = isPrayerOverridden ?: (completedFardhuCount >= 5)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SaatColors.JourneyCardBg,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFF0EBE1).copy(alpha = 0.6f))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Today's Journey & Calendar Streak Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Today's Journey",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Complete your daily worship",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF64748B)
                    )
                }

                // Calendar Streak Icon Button (top right above Sunnah)
                IconButton(
                    onClick = onOpenCalendar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_streak_custom),
                        contentDescription = "Prayer Calendar Streak",
                        tint = SaatColors.HomeDarkGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 4 Circular Journey Badges Row (Interactive)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Prayer
                JourneyBadge(
                    label = "Prayer",
                    isCompleted = isPrayerDone,
                    onClick = {
                        if (isPrayerClickable) {
                            val next = !isPrayerDone
                            isPrayerOverridden = next
                            PrayerTrackerStore.setOptionalCompleted(context, OptionalWorshipHabit.QIYAMUL_LAIL, next)
                            onToggleOptional(OptionalWorshipHabit.QIYAMUL_LAIL)
                        } else {
                            val msg = "Shalat dapat dicentang setelah waktu Isya atau 5 shalat selesai"
                            onShowToastMessage(msg)
                        }
                    }
                )

                // 2. Quran
                JourneyBadge(
                    label = "Quran",
                    isCompleted = isQuranDone,
                    onClick = {
                        val next = !isQuranDone
                        isQuranDone = next
                        PrayerTrackerStore.setOptionalCompleted(context, OptionalWorshipHabit.READ_QURAN, next)
                        onToggleOptional(OptionalWorshipHabit.READ_QURAN)
                    }
                )

                // 3. Dhikr
                JourneyBadge(
                    label = "Dhikr",
                    isCompleted = isDhikrDone,
                    onClick = {
                        val next = !isDhikrDone
                        isDhikrDone = next
                        PrayerTrackerStore.setOptionalCompleted(context, OptionalWorshipHabit.DHIKR_MORNING, next)
                        onToggleOptional(OptionalWorshipHabit.DHIKR_MORNING)
                    }
                )

                // 4. Sunnah
                JourneyBadge(
                    label = "Sunnah",
                    isCompleted = isSunnahDone,
                    onClick = {
                        val next = !isSunnahDone
                        isSunnahDone = next
                        PrayerTrackerStore.setOptionalCompleted(context, OptionalWorshipHabit.DHUHA, next)
                        onToggleOptional(OptionalWorshipHabit.DHUHA)
                    }
                )
            }

            // Inner Quran Quote Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFFDF7),
                border = BorderStroke(1.dp, Color(0xFFF3EDE2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.mascot_quran_qoute),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val rawTranslation = verse?.translations?.firstOrNull()?.text?.toVerseTranslationPlainText()
                        val translationText = if (!rawTranslation.isNullOrBlank()) {
                            "\"$rawTranslation\""
                        } else {
                            "\"And keep your prayer, and worship your Lord until there comes to you the certainty (i.e. death).\""
                        }

                        Text(
                            text = translationText,
                            color = Color(0xFF1E293B),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = referenceLabel ?: "Qur'an 15:99",
                                color = Color(0xFF64748B),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )

                            IconButton(
                                onClick = onShareVerse,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_share_custom),
                                    contentDescription = "Share",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyBadge(
    label: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val performTapHaptic = rememberTapHaptic()

    val animatedBg by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFFE6F4EA) else Color(0xFFF7F4E9),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "badgeBg"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFFB8E0C4) else Color(0xFFECE4D5),
        label = "badgeBorder"
    )

    val animatedLabelColor by animateColorAsState(
        targetValue = if (isCompleted) SaatColors.HomeDarkGreen else Color(0xFF475569),
        label = "badgeLabel"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                performTapHaptic()
                onClick()
            }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(animatedBg)
                .border(1.dp, animatedBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SaatColors.HomeDarkGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "$label completed",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(2.dp, SaatColors.HomeDarkGreen.copy(alpha = 0.65f), CircleShape)
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.SemiBold,
            color = animatedLabelColor
        )
    }
}
