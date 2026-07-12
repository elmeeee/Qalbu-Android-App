package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.KhgtTodayInfo
import app.kamy.saatApp.infrastructure.local.ImportantDayRegistry
import app.kamy.saatApp.infrastructure.local.ImportantDayDetail

@Suppress("SpellCheckingInspection")
@Composable
fun TodayImportantDayBanner(
    info: KhgtTodayInfo?,
    modifier: Modifier = Modifier
) {
    val event = info?.eventTitle ?: return
    val context = LocalContext.current
    
    // Resolve language from configuration
    val languageCode = context.resources.configuration.locales[0].language
    val language = when (languageCode) {
        "in", "id" -> AppLanguage.INDONESIAN
        "ms" -> AppLanguage.MALAY
        else -> AppLanguage.ENGLISH
    }
    
    val localizedTitle = ImportantDayRegistry.getLocalizedEventName(event, language)
    var showDetailSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        SaatColors.PrayerCreamWarm.copy(alpha = 0.45f),
                        SaatColors.PrayerCream.copy(alpha = 0.25f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    SaatColors.GoldDeep.copy(alpha = 0.2f)
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { showDetailSheet = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(SaatColors.GoldDeep.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Event,
                contentDescription = null,
                tint = SaatColors.GoldDeep,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.khgt_important_day).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.sp
                ),
                fontWeight = FontWeight.Bold,
                color = SaatColors.GoldDeep
            )
            Text(
                text = localizedTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SaatColors.Slate900,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    if (showDetailSheet) {
        ImportantDayDetailSheet(
            rawEvent = event,
            language = language,
            onDismiss = { showDetailSheet = false }
        )
    }
}

@Suppress("SpellCheckingInspection")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportantDayDetailSheet(
    rawEvent: String,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val localizedTitle = ImportantDayRegistry.getLocalizedEventName(rawEvent, language)
    val detail = ImportantDayRegistry.getImportantDayDetail(rawEvent, language)
    val headers = ImportantDayRegistry.getLocalizedSectionHeaders(language)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = headers.first,
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.GoldDeep
                )
                Text(
                    text = localizedTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SaatColors.Slate900,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (detail != null) {
                // Section 1: About
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaatColors.SageMist.copy(alpha = 0.5f))
                        .border(
                            BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.5f)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = SaatColors.Teal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = headers.second,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.Slate800
                        )
                    }
                    Text(
                        text = detail.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate700,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Section 2: Sunnah
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaatColors.PrayerCream.copy(alpha = 0.5f))
                        .border(
                            BorderStroke(1.dp, SaatColors.GoldDeep.copy(alpha = 0.15f)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = SaatColors.GoldDeep,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = headers.third,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.GoldDeep
                        )
                    }
                    Text(
                        text = detail.sunnah,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaatColors.Slate700,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Text(
                    text = if (language == AppLanguage.ENGLISH) "No additional information available."
                           else "Informasi tambahan tidak tersedia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SaatColors.Slate500
                )
            }
        }
    }
}
