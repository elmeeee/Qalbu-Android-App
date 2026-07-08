package app.kamy.saatApp.features.share

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.share.VerseShareTextComposer.Companion.fullArabicForShare
import app.kamy.saatApp.design.theme.AlKhatibColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahImageShareSheet(
    verse: RandomAyahPayload,
    surahName: String,
    onDismiss: () -> Unit,
    onShare: (ShareTemplate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTemplate by remember { mutableStateOf(ShareTemplate.IVORY_CREAM) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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
                        tint = AlKhatibColors.DeepEmerald
                    )
                    Text(
                        text = stringResource(R.string.share_as_image),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.DeepEmerald,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Real-time Preview Card
            val tBgColor = Color(android.graphics.Color.parseColor(selectedTemplate.bgColor))
            val tBorderColor = Color(android.graphics.Color.parseColor(selectedTemplate.borderColor))
            val tInnerBorderColor = Color(android.graphics.Color.parseColor(selectedTemplate.innerBorderColor))
            val tAppNameColor = Color(android.graphics.Color.parseColor(selectedTemplate.appNameColor))
            val tArabicTextColor = Color(android.graphics.Color.parseColor(selectedTemplate.arabicTextColor))
            val tTranslationColor = Color(android.graphics.Color.parseColor(selectedTemplate.translationColor))
            val tDividerColor = Color(android.graphics.Color.parseColor(selectedTemplate.dividerColor))
            val tReferenceColor = Color(android.graphics.Color.parseColor(selectedTemplate.referenceColor))
            val tFooterColor = Color(android.graphics.Color.parseColor(selectedTemplate.footerColor))

            Box(
                modifier = Modifier
                    .width(224.dp)
                    .aspectRatio(4f / 5f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tBgColor)
                    .border(4.dp, tBorderColor, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // Inner border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(0.5.dp, tInnerBorderColor, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Header (Logo + App Name)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.splash_icon_adaptive),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "Sāat",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tAppNameColor
                                )
                            )
                        }

                        // Middle Content (Arabic, Divider, Translation)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = verse.fullArabicForShare().trim(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    color = tArabicTextColor
                                ),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Subtle Divider
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(1.dp)
                                    .background(tDividerColor)
                            )

                            val translationText = verse.translations?.firstOrNull()?.text?.trim().orEmpty()
                                .replace(Regex("<[^>]*>"), "")
                            Text(
                                text = translationText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 7.sp,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center,
                                    color = tTranslationColor
                                ),
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Bottom Footer (Reference + App Promo)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                text = "$surahName ${verse.resolvedVerseNumber ?: ""}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tReferenceColor
                                )
                            )
                            Text(
                                text = "Read & Reflect on SĀAT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 5.sp,
                                    color = tFooterColor
                                )
                            )
                        }
                    }
                }
            }

            // Template Selector Title
            Text(
                text = "Choose Template",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Horizontal Template List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                ShareTemplate.values().forEach { template ->
                    val isSelected = selectedTemplate == template
                    val itemBgColor = Color(android.graphics.Color.parseColor(template.bgColor))
                    val itemBorderColor = Color(android.graphics.Color.parseColor(template.borderColor))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable { selectedTemplate = template }
                            .padding(4.dp)
                    ) {
                        // Colored Circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(itemBgColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.5.dp,
                                    color = if (isSelected) AlKhatibColors.DeepEmerald else itemBorderColor,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(AlKhatibColors.DeepEmerald),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Display Name
                        Text(
                            text = template.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AlKhatibColors.DeepEmerald else MaterialTheme.colorScheme.onSurfaceVariant
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
                    containerColor = AlKhatibColors.DeepEmerald,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text(
                    text = "Share Design",
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
