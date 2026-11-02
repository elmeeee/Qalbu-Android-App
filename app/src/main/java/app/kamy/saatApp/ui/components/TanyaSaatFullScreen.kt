package app.kamy.saatApp.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.model.ChatSender
import app.kamy.saatApp.domain.model.DoaItem
import app.kamy.saatApp.domain.model.KimiAiModel
import app.kamy.saatApp.domain.model.SaatChatMessage
import app.kamy.saatApp.domain.model.SaatMood
import app.kamy.saatApp.domain.model.SaatReference
import app.kamy.saatApp.domain.model.SaatReferenceType
import app.kamy.saatApp.domain.model.SaatVerseCardData
import app.kamy.saatApp.features.today.TanyaSaatUiState
import app.kamy.saatApp.features.tools.components.SpiritualToolTopBar
import app.kamy.saatApp.ui.common.TajweedHtmlView

// ─── Palette ────────────────────────────────────────────────────────────────

// Matches the app's warm cream theme (HomeBg / HomeDarkGreen / ArcGold) used across Tools screens.
private val ScreenBg = SaatColors.HomeBg
private val Ink = Color(0xFF1F2A24)
private val InkSoft = Color(0xFF4F5B55)
private val InkMuted = Color(0xFF8A9690)
private val Line = Color(0xFFE8E2D2)
private val CardWhite = Color(0xFFFFFDF7)
private val Gold = SaatColors.ArcGold
private val AmberWash = Color(0xFFFFFBEB)
private val AmberLine = Color(0xFFFDE68A)
private val MintWash = Color(0xFFEDF5F0)
private val MintLine = Color(0xFFCADFD3)
private val DangerWash = Color(0xFFFEF2F2)
private val DangerLine = Color(0xFFFECACA)
private val Danger = Color(0xFFB91C1C)

private val KimiGradient: Brush
    get() = Brush.linearGradient(colors = listOf(SaatColors.DeepEmerald, SaatColors.Teal))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TanyaSaatFullScreen(
    state: TanyaSaatUiState,
    onDismiss: () -> Unit,
    onModelSelected: (KimiAiModel) -> Unit,
    onMoodSelected: (SaatMood) -> Unit,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onOpenVerseInReader: (chapter: Int, verse: Int) -> Unit,
    onBookmarkVerse: (SaatVerseCardData) -> Unit,
    onClearToast: () -> Unit,
    onFollowUp: (String) -> Unit = {},
    onNewChat: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    BackHandler { onDismiss() }

    LaunchedEffect(state.messages.size, state.isLoading) {
        val count = state.messages.size + if (state.isLoading) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onClearToast()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = ScreenBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            KimiHeader(
                hasConversation = state.messages.isNotEmpty(),
                isLoading = state.isLoading,
                onBack = onDismiss,
                onNewChat = onNewChat
            )

            // Chat content scrolls underneath the glass composer.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.messages.isEmpty() && !state.isLoading) {
                    KimiEmptyState(
                        onMoodSelected = onMoodSelected,
                        bottomInset = ComposerReservedHeight
                    )
                } else {
                    val lastAiId = state.messages.lastOrNull { it.sender == ChatSender.AI }?.id
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 14.dp,
                            bottom = ComposerReservedHeight
                        )
                    ) {
                        items(state.messages, key = { it.id }) { msg ->
                            if (msg.sender == ChatSender.USER) {
                                UserBubble(message = msg)
                            } else {
                                KimiAnswer(
                                    message = msg,
                                    showFollowUps = msg.id == lastAiId && !state.isLoading,
                                    onOpenVerseInReader = onOpenVerseInReader,
                                    onBookmarkVerse = onBookmarkVerse,
                                    onFollowUp = onFollowUp,
                                    onRetry = onRetry,
                                    onCopied = {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.tanya_saat_copied),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                        if (state.isLoading) {
                            item(key = "ai_thinking_state") { KimiThinkingBubble() }
                        }
                    }
                }

                KimiComposer(
                    inputText = state.inputText,
                    selectedModel = state.selectedModel,
                    isLoading = state.isLoading,
                    onInputChanged = onInputChanged,
                    onSend = onSendMessage,
                    onModelSelected = onModelSelected,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

/** Space reserved at the bottom of scrolling content so the floating composer never covers it. */
private val ComposerReservedHeight = 128.dp

// ─── Header ─────────────────────────────────────────────────────────────────

@Composable
private fun KimiHeader(
    hasConversation: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNewChat: () -> Unit
) {
    SpiritualToolTopBar(
        title = stringResource(R.string.tanya_saat_ai_title),
        subtitle = stringResource(R.string.tanya_saat_ai_subtitle),
        onBack = onBack,
        actions = {
            AnimatedVisibility(visible = hasConversation, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    shape = CircleShape,
                    color = SaatColors.PureWhite,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.clickable(enabled = !isLoading, onClick = onNewChat)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddComment,
                            contentDescription = null,
                            tint = SaatColors.HomeDarkGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.tanya_saat_new_chat),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SaatColors.HomeDarkGreen
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun KimiAvatar(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(KimiGradient),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.saat_ai_icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

// ─── Empty state ────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KimiEmptyState(
    onMoodSelected: (SaatMood) -> Unit,
    bottomInset: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = bottomInset)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, Line),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KimiAvatar(size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.tanya_saat_welcome_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Ink
                        )
                        Text(
                            text = "Kīmi · Sāat",
                            style = MaterialTheme.typography.labelSmall,
                            color = InkMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.tanya_saat_welcome_body),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 21.sp),
                    color = InkSoft
                )
                Spacer(modifier = Modifier.height(14.dp))
                CapabilityRow(Icons.AutoMirrored.Filled.MenuBook, stringResource(R.string.tanya_saat_capability_quran))
                CapabilityRow(Icons.Outlined.Verified, stringResource(R.string.tanya_saat_capability_hadith))
                CapabilityRow(Icons.Default.Gavel, stringResource(R.string.tanya_saat_capability_fiqh))
                CapabilityRow(Icons.Default.HistoryEdu, stringResource(R.string.tanya_saat_capability_history))
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.tanya_saat_suggestions_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Ink
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            SaatMood.defaultList.forEach { mood ->
                SuggestionCard(
                    mood = mood,
                    modifier = Modifier.weight(1f),
                    onClick = { onMoodSelected(mood) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CapabilityRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MintWash),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SaatColors.DeepEmerald,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = InkSoft
        )
    }
}

@Composable
private fun SuggestionCard(mood: SaatMood, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .heightIn(min = 104.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, Line)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = mood.iconEmoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(mood.labelRes),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = SaatColors.DeepEmerald,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(mood.promptRes),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
                color = InkSoft,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Bubbles ────────────────────────────────────────────────────────────────

@Composable
private fun UserBubble(message: SaatChatMessage) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp),
            color = Color.Transparent,
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.background(KimiGradient)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp, lineHeight = 21.sp),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KimiAnswer(
    message: SaatChatMessage,
    showFollowUps: Boolean,
    onOpenVerseInReader: (chapter: Int, verse: Int) -> Unit,
    onBookmarkVerse: (SaatVerseCardData) -> Unit,
    onFollowUp: (String) -> Unit,
    onRetry: () -> Unit,
    onCopied: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KimiAvatar(size = 28.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Kīmi",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Ink
            )
            message.modelName?.let { model ->
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = MintWash) {
                    Text(
                        text = model,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (message.isError) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = DangerWash,
                border = BorderStroke(1.dp, DangerLine)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                        color = Danger
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Danger),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.tanya_saat_retry), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            return@Column
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, Line),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                KimiMarkdown(markdown = message.text, textColor = Ink)

                // Verse, dua, and references all live inside the same bubble.
                message.verseData?.let { verse ->
                    Spacer(modifier = Modifier.height(14.dp))
                    EmbeddedVerseCard(
                        verse = verse,
                        onOpenInReader = { onOpenVerseInReader(verse.chapterNumber, verse.verseNumber) },
                        onBookmark = { onBookmarkVerse(verse) }
                    )
                }

                message.doaData?.let { doa ->
                    Spacer(modifier = Modifier.height(10.dp))
                    EmbeddedDoaCard(doa = doa)
                }

                if (message.references.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ReferencesSection(references = message.references)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = InkMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.tanya_saat_disclaimer),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, lineHeight = 14.sp),
                        color = InkMuted,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MintWash,
                        modifier = Modifier.clickable {
                            clipboard.setText(AnnotatedString(message.text))
                            onCopied()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.tanya_saat_copy),
                                tint = SaatColors.DeepEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.tanya_saat_copy),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SaatColors.DeepEmerald
                            )
                        }
                    }
                }
            }
        }

        if (showFollowUps && message.followUps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.tanya_saat_follow_up_title),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = InkMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                message.followUps.forEach { question ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, MintLine),
                        modifier = Modifier.clickable { onFollowUp(question) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = SaatColors.DeepEmerald,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 280.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── References ─────────────────────────────────────────────────────────────

@Composable
private fun ReferencesSection(references: List<SaatReference>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAF9))
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Verified,
                contentDescription = null,
                tint = SaatColors.DeepEmerald,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.tanya_saat_sources_title),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Ink
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        references.forEachIndexed { index, ref ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            ReferenceRow(ref)
        }
    }
}

@Composable
private fun ReferenceRow(ref: SaatReference) {
    val (label, icon, tint) = when (ref.type) {
        SaatReferenceType.QURAN -> Triple(stringResource(R.string.tanya_saat_ref_quran), Icons.AutoMirrored.Filled.MenuBook, SaatColors.DeepEmerald)
        SaatReferenceType.HADITH -> Triple(stringResource(R.string.tanya_saat_ref_hadith), Icons.Outlined.Verified, Color(0xFF0D9488))
        SaatReferenceType.SCHOLAR -> Triple(stringResource(R.string.tanya_saat_ref_scholar), Icons.Default.School, Color(0xFF7C3AED))
        SaatReferenceType.HISTORY -> Triple(stringResource(R.string.tanya_saat_ref_history), Icons.Default.HistoryEdu, Color(0xFFB45309))
        SaatReferenceType.OTHER -> Triple(stringResource(R.string.tanya_saat_ref_other), Icons.Outlined.Info, InkMuted)
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        }
        Spacer(modifier = Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = tint
                )
                ref.grade?.let { grade ->
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = AmberWash, border = BorderStroke(1.dp, AmberLine)) {
                        Text(
                            text = grade,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(
                text = ref.citation,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold),
                color = Ink
            )
            ref.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                    color = InkSoft
                )
            }
        }
    }
}

// ─── Thinking bubble ────────────────────────────────────────────────────────

@Composable
private fun KimiThinkingBubble() {
    val transition = rememberInfiniteTransition(label = "kimi_thinking")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "kimi_dots"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "kimi_pulse"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.alpha(pulse)) { KimiAvatar(size = 28.dp) }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Kīmi",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Ink
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, Line),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val active = phase.toInt() % 3 == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.5.dp)
                            .size(if (active) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) SaatColors.DeepEmerald else MintLine)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.tanya_saat_ai_thinking),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = InkSoft
                )
            }
        }
    }
}

// ─── Composer (input + model picker) ────────────────────────────────────────

@Composable
private fun KimiComposer(
    inputText: String,
    selectedModel: KimiAiModel,
    isLoading: Boolean,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onModelSelected: (KimiAiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var isModelMenuExpanded by remember { mutableStateOf(false) }
    val canSend = inputText.isNotBlank() && !isLoading
    val fieldShape = RoundedCornerShape(24.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        // Only the strip ABOVE the field is translucent: chat fades out underneath it.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, ScreenBg)))
        )

        // The field area itself sits on a solid background, with or without the keyboard.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScreenBg)
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = fieldShape,
                        ambientColor = SaatColors.HomeDarkGreen.copy(alpha = 0.12f),
                        spotColor = SaatColors.HomeDarkGreen.copy(alpha = 0.16f)
                    )
                    .clip(fieldShape)
                    .background(SaatColors.PureWhite)
                    .border(
                        width = 1.dp,
                        color = if (canSend) SaatColors.HomeDarkGreen.copy(alpha = 0.45f) else Line,
                        shape = fieldShape
                    )
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 8.dp)) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 24.dp, max = 120.dp),
                        textStyle = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, color = Ink),
                        cursorBrush = SolidColor(SaatColors.DeepEmerald),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        ),
                        maxLines = 5,
                        decorationBox = { inner ->
                            Box {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.tanya_saat_placeholder),
                                        style = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
                                        color = InkMuted
                                    )
                                }
                                inner()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ── Model picker lives right beside the typing area ──
                        Box {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MintWash,
                                border = BorderStroke(1.dp, MintLine),
                                modifier = Modifier.clickable(enabled = !isLoading) { isModelMenuExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 9.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = SaatColors.DeepEmerald,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = selectedModel.displayName,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = SaatColors.DeepEmerald,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = stringResource(R.string.tanya_saat_model_picker_title),
                                        tint = SaatColors.DeepEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = isModelMenuExpanded,
                                onDismissRequest = { isModelMenuExpanded = false },
                                containerColor = CardWhite,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.tanya_saat_model_picker_title),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = InkMuted,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                                KimiAiModel.entries.forEach { model ->
                                    val selected = model == selectedModel
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = model.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (selected) SaatColors.DeepEmerald else Ink
                                                )
                                                Text(
                                                    text = model.tagline,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = InkMuted
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (selected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = SaatColors.DeepEmerald,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            onModelSelected(model)
                                            isModelMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSend) KimiGradient
                                    else Brush.linearGradient(listOf(Line, Line))
                                )
                                .clickable(enabled = canSend, onClick = onSend),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.sent_icon),
                                contentDescription = "Send",
                                colorFilter = ColorFilter.tint(if (canSend) Color.White else InkMuted),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Embedded Verse Card ────────────────────────────────────────────────────

@Composable
private fun EmbeddedVerseCard(
    verse: SaatVerseCardData,
    onOpenInReader: () -> Unit,
    onBookmark: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MintWash,
        border = BorderStroke(1.dp, MintLine)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(10.dp), color = SaatColors.DeepEmerald) {
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
                    color = InkSoft
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

// ─── Embedded Doa Card ──────────────────────────────────────────────────────

@Composable
private fun EmbeddedDoaCard(doa: DoaItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AmberWash,
        border = BorderStroke(1.dp, AmberLine)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                text = "🤲 ${doa.title ?: "Doa"}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF92400E)
            )
            Spacer(modifier = Modifier.height(10.dp))

            doa.arabic?.let { ar ->
                Text(
                    text = ar,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, textAlign = TextAlign.End),
                    color = Ink,
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

            doa.source?.takeIf { it.isNotBlank() }?.let { src ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = src,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = Color(0xFFA16207)
                )
            }
        }
    }
}
