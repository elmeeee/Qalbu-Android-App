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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

import app.kamy.saatApp.infrastructure.repository.DailyQuoteItem
import app.kamy.saatApp.infrastructure.repository.DailyQuoteRepository

@Composable
fun PrayerTrackerCard(
    state: PrayerTrackerUiState,
    verse: RandomAyahPayload? = null,
    referenceLabel: String? = null,
    dailyQuote: DailyQuoteItem? = null,
    isAfterIsha: Boolean = false,
    onShowToastMessage: (String) -> Unit = {},
    onToggleDailyPrayer: () -> Unit = {},
    onToggleOptional: (OptionalWorshipHabit) -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val titleText = stringResource(R.string.todays_journey_title)
    val subtitleText = stringResource(R.string.todays_journey_subtitle)
    val prayerBadgeLabel = stringResource(R.string.journey_badge_prayer)
    val quranBadgeLabel = stringResource(R.string.journey_badge_quran)
    val dhikrBadgeLabel = stringResource(R.string.journey_badge_dhikr)
    val sunnahBadgeLabel = stringResource(R.string.journey_badge_sunnah)
    val notAfterIshaMsg = stringResource(R.string.prayer_badge_not_after_isha)
    val prayerSuccessMsg = stringResource(R.string.journey_toast_prayer_success)
    val quranSuccessMsg = stringResource(R.string.journey_toast_quran_success)
    val dhikrSuccessMsg = stringResource(R.string.journey_toast_dhikr_success)
    val sunnahSuccessMsg = stringResource(R.string.journey_toast_sunnah_success)

    val isPrayerClickable = isAfterIsha
    val isPrayerDone = state.todayProgress.isPrayerDone
    val isQuranDone = state.todayProgress.isQuranDone
    val isDhikrDone = state.todayProgress.isDhikrDone
    val isSunnahDone = state.todayProgress.isSunnahDone

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
                        text = titleText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = subtitleText,
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
                    label = prayerBadgeLabel,
                    isCompleted = isPrayerDone,
                    onClick = {
                        if (isPrayerClickable) {
                            val willComplete = !isPrayerDone
                            onToggleDailyPrayer()
                            if (willComplete) onShowToastMessage(prayerSuccessMsg)
                        } else {
                            onShowToastMessage(notAfterIshaMsg)
                        }
                    }
                )

                // 2. Quran
                JourneyBadge(
                    label = quranBadgeLabel,
                    isCompleted = isQuranDone,
                    onClick = {
                        val willComplete = !isQuranDone
                        onToggleOptional(OptionalWorshipHabit.READ_QURAN)
                        if (willComplete) onShowToastMessage(quranSuccessMsg)
                    }
                )

                // 3. Dhikr
                JourneyBadge(
                    label = dhikrBadgeLabel,
                    isCompleted = isDhikrDone,
                    onClick = {
                        val willComplete = !isDhikrDone
                        onToggleOptional(OptionalWorshipHabit.DHIKR_MORNING)
                        if (willComplete) onShowToastMessage(dhikrSuccessMsg)
                    }
                )

                // 4. Sunnah
                JourneyBadge(
                    label = sunnahBadgeLabel,
                    isCompleted = isSunnahDone,
                    onClick = {
                        val willComplete = !isSunnahDone
                        onToggleOptional(OptionalWorshipHabit.DHUHA)
                        if (willComplete) onShowToastMessage(sunnahSuccessMsg)
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
                    val currentLang by AppLanguageStore.from(context).currentFlow.collectAsStateWithLifecycle()
                    val activeQuote = remember(currentLang, dailyQuote) {
                        DailyQuoteRepository(context).getTodayQuote(currentLang)
                    }

                    val rawTranslation = verse?.translations?.firstOrNull()?.text?.toVerseTranslationPlainText()
                    val translationText = activeQuote.quoteText.ifEmpty {
                        if (!rawTranslation.isNullOrBlank()) "\"$rawTranslation\""
                        else "\"And keep your prayer, and worship your Lord until there comes to you the certainty (i.e. death).\""
                    }

                    val mascotRes = remember(translationText, verse) {
                        resolveDailyQuoteMascot(translationText, verse)
                    }

                    Image(
                        painter = painterResource(mascotRes),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val refText = activeQuote.referenceLabel.ifEmpty {
                            referenceLabel ?: "Qur'an 15:99"
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

                        Text(
                            text = refText,
                            color = Color(0xFF64748B),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
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

/**
 * Dynamically resolves the mascot illustration for the daily quote based on:
 * 1. Semantic keywords in the quote text (Prayer, Reading/Knowledge, Light/Night, Happiness/Gratitude, Patience/Trial, Leadership/Creation)
 * 2. Deterministic daily hash from the verse/quote so it rotates across the 6 mascot illustrations every day.
 */
@androidx.annotation.DrawableRes
private fun resolveDailyQuoteMascot(
    quoteText: String,
    verse: RandomAyahPayload?
): Int {
    val text = quoteText.lowercase()

    return when {
        text.contains("shalat") || text.contains("prayer") || text.contains("solat") ||
            text.contains("sujud") || text.contains("doa") || text.contains("sembah") ||
            text.contains("ruku") || text.contains("ibadah") -> R.drawable.mascot_prayer

        text.contains("baca") || text.contains("read") || text.contains("kitab") ||
            text.contains("qur'an") || text.contains("quran") || text.contains("ilmu") ||
            text.contains("hikmah") || text.contains("pelajaran") -> R.drawable.mascot_reading

        text.contains("cahaya") || text.contains("light") || text.contains("malam") ||
            text.contains("night") || text.contains("petunjuk") || text.contains("hidayah") ||
            text.contains("bintang") || text.contains("bulan") -> R.drawable.mascot_lentera

        text.contains("gembira") || text.contains("senang") || text.contains("syukur") ||
            text.contains("nikmat") || text.contains("surga") || text.contains("pahala") ||
            text.contains("rahmat") || text.contains("bahagia") -> R.drawable.mascot_smile

        text.contains("sabar") || text.contains("patience") || text.contains("sulit") ||
            text.contains("sedih") || text.contains("duka") || text.contains("kesulitan") ||
            text.contains("ujian") || text.contains("cobaan") -> R.drawable.mascot_happysad

        text.contains("bumi") || text.contains("langit") || text.contains("kerajaan") ||
            text.contains("pemimpin") || text.contains("amanah") || text.contains("adil") ||
            text.contains("khalifah") || text.contains("alam") -> R.drawable.mascot_khilafah

        else -> {
            val seed = Math.abs(
                (verse?.globalAyah ?: verse?.id ?: quoteText.hashCode())
            )
            val mascots = intArrayOf(
                R.drawable.mascot_prayer,
                R.drawable.mascot_reading,
                R.drawable.mascot_lentera,
                R.drawable.mascot_smile,
                R.drawable.mascot_happysad,
                R.drawable.mascot_khilafah
            )
            mascots[seed % mascots.size]
        }
    }
}
