package app.kamy.saatApp.features.share

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.share.VerseShareTextComposer.Companion.fullArabicForShare
import app.kamy.saatApp.design.theme.SaatColors
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahImageShareSheet(
    verse: RandomAyahPayload,
    surahName: String,
    onDismiss: () -> Unit,
    onShare: (ShareTemplate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val templates = ShareTemplate.values()
    val pagerState = rememberPagerState(initialPage = 0) { templates.size }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SaatColors.ScreenBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header (Compact)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.share_as_image),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }

            // Swipeable Carousel of Templates
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 85.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                val template = templates[pageIndex]
                val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                val scale = lerp(0.88f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                val alpha = lerp(0.65f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

                val arabicColor = Color(android.graphics.Color.parseColor(template.arabicTextColor))
                val translationColor = Color(android.graphics.Color.parseColor(template.translationColor))
                val ornamentColor = Color(android.graphics.Color.parseColor(template.ornamentColor))
                val refColor = Color(android.graphics.Color.parseColor(template.referenceColor))
                val hashtagColor = Color(android.graphics.Color.parseColor(template.hashtagColor))

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .width(190.dp)
                        .aspectRatio(941f / 1672f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFC5A880).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    // Background Image
                    Image(
                        painter = painterResource(template.bgDrawableRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    val chapterNum = verse.chapterId ?: verse.verseKey?.substringBefore(':')?.toIntOrNull()
                    val verseNum = verse.resolvedVerseNumber ?: ""
                    val referenceText = if (chapterNum != null) {
                        "QS. $surahName ($chapterNum) : $verseNum"
                    } else {
                        "QS. $surahName : $verseNum"
                    }

                    val rawTranslation = verse.translations?.firstOrNull()?.text?.trim().orEmpty()
                        .replace(Regex("<[^>]*>"), "")
                    val translationQuote = if (rawTranslation.isNotBlank()) "“$rawTranslation”" else ""
                    val hashtagText = stringResource(R.string.share_image_hashtag).trim()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 70.dp, bottom = 90.dp, start = 14.dp, end = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            // 1. Top Diamond Ornament
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(1.dp)
                                        .background(ornamentColor)
                                )
                                Text(
                                    text = "❖",
                                    fontSize = 7.sp,
                                    color = ornamentColor
                                )
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(1.dp)
                                        .background(ornamentColor)
                                )
                            }

                            // 2. Arabic Text
                            Text(
                                text = verse.fullArabicForShare().trim(),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Center,
                                color = arabicColor,
                                lineHeight = 14.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )

                            // 3. Translation Quote
                            if (translationQuote.isNotBlank()) {
                                Text(
                                    text = translationQuote,
                                    fontSize = 7.sp,
                                    fontFamily = FontFamily.Serif,
                                    textAlign = TextAlign.Center,
                                    color = translationColor,
                                    lineHeight = 9.5.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // 4. Middle Ornament
                            Text(
                                text = "― ❖ ―",
                                fontSize = 6.sp,
                                color = ornamentColor
                            )

                            // 5. Reference Text & Hashtag
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    text = referenceText,
                                    fontSize = 7.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = refColor
                                )
                                if (hashtagText.isNotBlank()) {
                                    Text(
                                        text = hashtagText,
                                        fontSize = 5.5.sp,
                                        fontFamily = FontFamily.Serif,
                                        textAlign = TextAlign.Center,
                                        color = hashtagColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Page Indicator Dots & Template Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = templates[pagerState.currentPage].displayName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SaatColors.DeepEmerald
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    templates.indices.forEach { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotWidth by animateFloatAsState(targetValue = if (isSelected) 18f else 6f, label = "dotWidth")
                        val dotAlpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.35f, label = "dotAlpha")

                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(dotWidth.dp)
                                .clip(CircleShape)
                                .background(SaatColors.DeepEmerald.copy(alpha = dotAlpha))
                        )
                    }
                }
            }

            // Share Button (Compact & Clean)
            Button(
                onClick = { onShare(templates[pagerState.currentPage]) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaatColors.DeepEmerald,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.share_as_image),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
