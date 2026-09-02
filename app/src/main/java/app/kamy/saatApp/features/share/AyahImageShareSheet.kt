package app.kamy.saatApp.features.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.share.VerseShareTextComposer.Companion.fullArabicForShare
import app.kamy.saatApp.design.theme.SaatColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahImageShareSheet(
    verse: RandomAyahPayload,
    surahName: String,
    onDismiss: () -> Unit,
    onShare: (ShareTemplate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTemplate by remember { mutableStateOf(ShareTemplate.TEMPLATE_1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SaatColors.ScreenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald
                    )
                    Text(
                        text = stringResource(R.string.share_as_image),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            val arabicColor = Color(android.graphics.Color.parseColor(selectedTemplate.arabicTextColor))
            val translationColor = Color(android.graphics.Color.parseColor(selectedTemplate.translationColor))
            val ornamentColor = Color(android.graphics.Color.parseColor(selectedTemplate.ornamentColor))
            val refColor = Color(android.graphics.Color.parseColor(selectedTemplate.referenceColor))
            val hashtagColor = Color(android.graphics.Color.parseColor(selectedTemplate.hashtagColor))

            // Real-time Preview Card (941 x 1672 Aspect Ratio)
            Box(
                modifier = Modifier
                    .width(230.dp)
                    .aspectRatio(941f / 1672f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFC5A880).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                // Background Ornate Image
                Image(
                    painter = painterResource(selectedTemplate.bgDrawableRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Content inside the arch frame
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 62.dp, bottom = 112.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 1. Top Diamond Ornament
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(1.dp)
                                .background(ornamentColor)
                        )
                        Text(
                            text = "❖",
                            fontSize = 8.sp,
                            color = ornamentColor
                        )
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(1.dp)
                                .background(ornamentColor)
                        )
                    }

                    // 2. Arabic Text
                    Text(
                        text = verse.fullArabicForShare().trim(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        color = arabicColor,
                        lineHeight = 16.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 3. Translation Quote
                    if (translationQuote.isNotBlank()) {
                        Text(
                            text = translationQuote,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            color = translationColor,
                            lineHeight = 11.sp,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 4. Middle Ornament
                    Text(
                        text = "― ❖ ―",
                        fontSize = 7.sp,
                        color = ornamentColor
                    )

                    // 5. Reference Text & Hashtag
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = referenceText,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = refColor
                        )
                        if (hashtagText.isNotBlank()) {
                            Text(
                                text = hashtagText,
                                fontSize = 6.sp,
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Center,
                                color = hashtagColor
                            )
                        }
                    }
                }
            }

            // Template Selector Title
            Text(
                text = "Pilih Template Desain",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Horizontal Template List (Card Previews)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
            ) {
                ShareTemplate.values().forEach { template ->
                    val isSelected = selectedTemplate == template

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clickable { selectedTemplate = template }
                            .padding(4.dp)
                    ) {
                        // Thumbnail Preview
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .aspectRatio(941f / 1672f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) SaatColors.DeepEmerald else Color(0xFFD5BE9C),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(template.bgDrawableRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(SaatColors.DeepEmerald),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Display Name
                        Text(
                            text = template.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SaatColors.DeepEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Share Button
            Button(
                onClick = { onShare(selectedTemplate) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaatColors.DeepEmerald,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text(
                    text = stringResource(R.string.share_as_image),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(stringResource(R.string.close), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
