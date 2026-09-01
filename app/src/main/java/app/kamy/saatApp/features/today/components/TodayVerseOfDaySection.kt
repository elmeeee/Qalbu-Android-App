package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.design.components.SaatErrorStateCompact
import app.kamy.saatApp.design.components.TodayVerseCardSkeleton
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.model.displayTransliteration
import app.kamy.saatApp.domain.model.transliterationUsesHtml
import app.kamy.saatApp.domain.quran.DailyVerseOccasion
import app.kamy.saatApp.ui.common.TajweedHtmlView
import app.kamy.saatApp.ui.common.TransliterationView
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import app.kamy.saatApp.ui.common.toVerseTranslationPlainText

@Composable
fun TodayVerseOfDaySection(
    verse: RandomAyahPayload?,
    referenceLabel: String?,
    translationId: Int = LocalQuranConfig.DEFAULT_TRANSLATION_ID,
    showTranslation: Boolean = true,
    showTransliteration: Boolean = false,
    occasion: DailyVerseOccasion? = null,
    isLoading: Boolean,
    error: AppError? = null,
    isPlaying: Boolean = false,
    reciterName: String? = null,
    aiShareLoading: Boolean = false,
    onPlayAudio: () -> Unit = {},
    onReciterClick: () -> Unit = {},
    onAiShare: () -> Unit = {},
    onTafsir: () -> Unit = {},
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val errorDisplay = error.rememberErrorDisplay(R.string.verse_of_day_load_failed)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = SaatColors.LastReadBg,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.5f))
    ) {
        if (verse != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mascot illustration
                AsyncImage(
                    model = R.drawable.mascot_quran_qoute,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp)
                )

                // Translation & Reference Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val rawTranslation = verse.translations?.firstOrNull()?.text?.toVerseTranslationPlainText()
                    val translationText = if (!rawTranslation.isNullOrBlank()) {
                        "\"$rawTranslation\""
                    } else {
                        "\"And keep your prayer, and worship your Lord until there comes to you the certainty (i.e. death).\""
                    }

                    Text(
                        text = translationText,
                        color = Color(0xFF1E293B),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp,
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        IconButton(
                            onClick = onAiShare,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share_custom),
                                contentDescription = "Share",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        } else if (isLoading) {
            TodayVerseCardSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            )
        } else if (errorDisplay != null) {
            SaatErrorStateCompact(
                display = errorDisplay,
                onRetry = onRetry,
                modifier = Modifier.padding(18.dp)
            )
        } else {
            Text(
                text = stringResource(R.string.no_verse_retry),
                color = SaatColors.Slate500,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun OccasionChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SaatColors.Gold.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = SaatColors.GoldDeep
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseActionButton(
    @androidx.annotation.DrawableRes iconRes: Int,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = tint.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
