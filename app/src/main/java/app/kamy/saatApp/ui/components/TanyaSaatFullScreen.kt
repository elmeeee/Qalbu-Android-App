package app.kamy.saatApp.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TanyaSaatFullScreen(
    state: TanyaSaatUiState,
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

    BackHandler {
        onDismiss()
    }

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7FAF8)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .tabContentStatusBarInset()
        ) {
            // ── Top Header Bar ────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color(0xFF1C1C1E)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SaatColors.DeepEmerald, Color(0xFF0D9488))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.saat_ai_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White),
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
                }
            }

            // ── Quick Mood Chips Container ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.tanya_saat_welcome_msg),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF64748B),
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
                            shape = RoundedCornerShape(22.dp),
                            color = if (isSelected) SaatColors.DeepEmerald else Color(0xFFF1F5F3),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = mood.iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(mood.labelRes),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // ── Chat Messages List Area ─────────────────────────────────────────
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
                        Image(
                            painter = painterResource(R.drawable.saat_ai_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(SaatColors.DeepEmerald),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.tanya_saat_welcome_msg),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(state.messages, key = { it.id }) { msg ->
                            ChatMessageBubbleRow(
                                message = msg,
                                onOpenVerseInReader = onOpenVerseInReader,
                                onBookmarkVerse = onBookmarkVerse
                            )
                        }

                        if (state.isLoading) {
                            item(key = "ai_thinking_state") {
                                AiThinkingBubble()
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // ── Bottom Input Container ──────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = onInputChanged,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.tanya_saat_placeholder),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaatColors.DeepEmerald,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = onSendMessage,
                        enabled = state.inputText.isNotBlank() && !state.isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.inputText.isNotBlank() && !state.isLoading) {
                                    Brush.linearGradient(
                                        colors = listOf(SaatColors.DeepEmerald, Color(0xFF0D9488))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))
                                    )
                                }
                            )
                    ) {
                        Image(
                            painter = painterResource(R.drawable.sent_icon),
                            contentDescription = "Send",
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Thinking State Bubble (State AI Lagi Mikir) ────────────────────────────

@Composable
private fun AiThinkingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_dots")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking_pulse"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SaatColors.DeepEmerald),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.saat_ai_icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier.scale(pulseScale),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = SaatColors.DeepEmerald,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tanya Sāat AI sedang merenungkan & mencari ayat terbaik...",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

// ─── Chat Message Bubble Row ─────────────────────────────────────────────────

@Composable
private fun ChatMessageBubbleRow(
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SaatColors.DeepEmerald, Color(0xFF0D9488))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.saat_ai_icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) SaatColors.DeepEmerald else Color.White,
                border = if (isUser) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = if (isUser) 2.dp else 1.dp
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    ),
                    color = if (isUser) Color.White else Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // ── Embedded Verse Recommendation Card ───────────────────────────
            message.verseData?.let { verse ->
                Spacer(modifier = Modifier.height(10.dp))
                EmbeddedVerseCard(
                    verse = verse,
                    onOpenInReader = { onOpenVerseInReader(verse.chapterNumber, verse.verseNumber) },
                    onBookmark = { onBookmarkVerse(verse) }
                )
            }

            // ── Embedded Doa Recommendation Card ──────────────────────────────
            message.doaData?.let { doa ->
                Spacer(modifier = Modifier.height(10.dp))
                EmbeddedDoaCard(doa = doa)
            }
        }
    }
}

// ─── Embedded Verse Card Component ──────────────────────────────────────────

@Composable
private fun EmbeddedVerseCard(
    verse: SaatVerseCardData,
    onOpenInReader: () -> Unit,
    onBookmark: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF0FDF4),
        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SaatColors.DeepEmerald
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.saat_ai_quran_icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${verse.surahName} ${verse.chapterNumber}:${verse.verseNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (verse.arabicText.isNotBlank()) {
                TajweedHtmlView(
                    textUthmani = verse.arabicText,
                    fontSizeSp = 22,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (verse.translationText.isNotBlank()) {
                Text(
                    text = verse.translationText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenInReader,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
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

// ─── Embedded Doa Card Component ────────────────────────────────────────────

@Composable
private fun EmbeddedDoaCard(doa: DoaItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFFBEB),
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🤲 ${doa.title ?: "Doa Shahih"}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF92400E)
            )
            Spacer(modifier = Modifier.height(10.dp))

            doa.arabic?.let { ar ->
                Text(
                    text = ar,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, textAlign = TextAlign.End),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            doa.translation?.let { tr ->
                Text(
                    text = tr,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = Color(0xFF78350F)
                )
            }
        }
    }
}
