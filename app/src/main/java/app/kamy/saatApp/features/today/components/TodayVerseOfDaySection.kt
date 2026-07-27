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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    isPlaying: Boolean,
    reciterName: String? = null,
    aiShareLoading: Boolean = false,
    onPlayAudio: () -> Unit,
    onReciterClick: () -> Unit = {},
    onAiShare: () -> Unit,
    onTafsir: () -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val errorDisplay = error.rememberErrorDisplay(R.string.verse_of_day_load_failed)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SaatSpacing.screenHorizontal, vertical = SaatSpacing.md)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SaatColors.PureWhite)
                .border(
                    width = 1.dp,
                    color = SaatColors.SoftGrey.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(22.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                SaatColors.DeepEmerald.copy(alpha = 0.16f),
                                SaatColors.Teal.copy(alpha = 0.08f),
                                SaatColors.Gold.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.verse_of_day),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        occasion?.takeIf { it != DailyVerseOccasion.Daily }?.let {
                            OccasionChip(label = stringResource(it.labelRes))
                        }
                    }
                    referenceLabel?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = SaatColors.Slate500,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (verse != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SaatColors.DeepEmerald.copy(alpha = 0.04f))
                            .border(
                                width = 1.dp,
                                color = SaatColors.DeepEmerald.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        TajweedHtmlView(
                            textUthmani = verse.textUthmani,
                            ayahNumber = verse.resolvedVerseNumber,
                            fontSizeSp = 28,
                            compact = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    verse.displayTransliteration(translationId)?.takeIf { showTransliteration }?.let { latin ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SaatColors.LightGrey.copy(alpha = 0.35f))
                                .border(
                                    width = 1.dp,
                                    color = SaatColors.SoftGrey.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            TransliterationView(
                                text = latin,
                                useHtml = verse.transliterationUsesHtml(translationId),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (showTranslation) {
                        verse.translations?.firstOrNull()?.text?.let { translation ->
                            val clean = translation.toVerseTranslationPlainText()
                            if (clean.isNotEmpty()) {
                                Text(
                                    text = clean,
                                    color = SaatColors.Slate800,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 26.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                reciterName?.takeIf { it.isNotBlank() }?.let { name ->
                    TextButton(
                        onClick = onReciterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "${stringResource(R.string.reciter)}: $name",
                            color = SaatColors.Slate500,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VerseActionButton(
                        iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                        tint = SaatColors.DeepEmerald,
                        onClick = onPlayAudio,
                        modifier = Modifier.weight(1f)
                    )
                    VerseActionButton(
                        iconRes = R.drawable.ic_ai,
                        tint = SaatColors.GoldDeep,
                        onClick = onAiShare,
                        modifier = Modifier.weight(1f)
                    )
                    if (LocalQuranConfig.supportsTafsir(translationId)) {
                        VerseActionButton(
                            iconRes = R.drawable.ic_tafsir,
                            tint = SaatColors.IndigoAccent,
                            onClick = onTafsir,
                            modifier = Modifier.weight(1f)
                        )
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
