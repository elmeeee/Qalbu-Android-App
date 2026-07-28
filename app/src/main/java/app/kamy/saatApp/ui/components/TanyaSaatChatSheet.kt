package app.kamy.saatApp.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.ChatSender
import app.kamy.saatApp.domain.model.DoaItem
import app.kamy.saatApp.domain.model.SaatChatMessage
import app.kamy.saatApp.domain.model.SaatMood
import app.kamy.saatApp.domain.model.SaatVerseCardData
import app.kamy.saatApp.features.today.TanyaSaatUiState
import app.kamy.saatApp.ui.common.TajweedHtmlView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TanyaSaatChatSheet(
    state: TanyaSaatUiState,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onMoodSelected: (SaatMood) -> Unit,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onOpenVerseInReader: (chapter: Int, verse: Int) -> Unit,
    onBookmarkVerse: (SaatVerseCardData) -> Unit,
    onClearToast: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onClearToast()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // ── Header Bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SaatColors.DeepEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tanya_saat_ai_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = stringResource(R.string.tanya_saat_ai_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF8E8E93)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF8E8E93)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // ── Quick Mood Chips Bar ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.tanya_saat_welcome_msg),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SaatMood.defaultList.forEach { mood ->
                        val isSelected = state.activeMood?.id == mood.id
                        Surface(
                            onClick = { onMoodSelected(mood) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) SaatColors.DeepEmerald else Color(0xFFF0F4F2),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E8E4))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = mood.iconEmoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(mood.labelRes),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF2C3E35)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // ── Messages Area ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (state.messages.isEmpty() && !state.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "✨", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.tanya_saat_welcome_msg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp)
                    ) {
                        items(state.messages, key = { it.id }) { msg ->
                            ChatMessageRow(
                                message = msg,
                                onOpenVerseInReader = onOpenVerseInReader,
                                onBookmarkVerse = onBookmarkVerse
                            )
                        }

                        if (state.isLoading) {
                            item(key = "loading_indicator") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = SaatColors.DeepEmerald,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Tanya Sāat AI sedang menyiapkan nasihat & rekomendasi...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF8E8E93)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // ── Bottom Input Bar ──────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = onInputChanged,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.tanya_saat_placeholder),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = Color(0xFFAAAAAA)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaatColors.DeepEmerald,
                            unfocusedBorderColor = Color(0xFFE5E5EA),
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSendMessage,
                        enabled = state.inputText.isNotBlank() && !state.isLoading,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.inputText.isNotBlank() && !state.isLoading) SaatColors.DeepEmerald else Color(0xFFE5E5EA)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageRow(
    message: SaatChatMessage,
    onOpenVerseInReader: (chapter: Int, verse: Int) -> Unit,
    onBookmarkVerse: (SaatVerseCardData) -> Unit
) {
    val isUser = message.sender == ChatSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SaatColors.DeepEmerald),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✨", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) SaatColors.DeepEmerald else Color.White,
                border = if (isUser) null else BorderStroke(1.dp, Color(0xFFEEEEEE)),
                shadowElevation = if (isUser) 0.dp else 1.dp
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    color = if (isUser) Color.White else Color(0xFF1C1C1E),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            // ── Embedded Verse Recommendation Card ───────────────────────────
            message.verseData?.let { verse ->
                Spacer(modifier = Modifier.height(8.dp))
                EmbeddedVerseCard(
                    verse = verse,
                    onOpenInReader = { onOpenVerseInReader(verse.chapterNumber, verse.verseNumber) },
                    onBookmark = { onBookmarkVerse(verse) }
                )
            }

            // ── Embedded Doa Recommendation Card ──────────────────────────────
            message.doaData?.let { doa ->
                Spacer(modifier = Modifier.height(8.dp))
                EmbeddedDoaCard(doa = doa)
            }
        }
    }
}

@Composable
private fun EmbeddedVerseCard(
    verse: SaatVerseCardData,
    onOpenInReader: () -> Unit,
    onBookmark: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF4F9F6),
        border = BorderStroke(1.dp, Color(0xFFC8E6D9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SaatColors.DeepEmerald
                ) {
                    Text(
                        text = "📖 ${verse.surahName} ${verse.chapterNumber}:${verse.verseNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (verse.arabicText.isNotBlank()) {
                TajweedHtmlView(
                    textUthmani = verse.arabicText,
                    fontSizeSp = 22,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (verse.translationText.isNotBlank()) {
                Text(
                    text = verse.translationText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenInReader,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaatColors.DeepEmerald)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.btn_open_in_quran_reader),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onBookmark,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SaatColors.DeepEmerald)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SaatColors.DeepEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun EmbeddedDoaCard(doa: DoaItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFBEB),
        border = BorderStroke(1.dp, Color(0xFFFDE68A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "🤲 ${doa.title ?: "Doa Shahih"}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF92400E)
            )
            Spacer(modifier = Modifier.height(8.dp))

            doa.arabic?.let { ar ->
                Text(
                    text = ar,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, textAlign = TextAlign.End),
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            doa.translation?.let { tr ->
                Text(
                    text = tr,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                    color = Color(0xFF78350F)
                )
            }
        }
    }
}
