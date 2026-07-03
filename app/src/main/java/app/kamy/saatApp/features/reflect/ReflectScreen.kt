package app.kamy.saatApp.features.reflect

import android.content.Intent
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.design.components.AlKhatibErrorStateDark
import app.kamy.saatApp.design.components.AlKhatibPullToRefresh
import app.kamy.saatApp.design.components.ReflectPostSkeleton
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import app.kamy.saatApp.domain.model.ReflectFeedPost
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import app.kamy.saatApp.ui.common.stripHtmlTags
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import coil.compose.AsyncImage
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
private fun ReflectSurfaceColor() = MaterialTheme.colorScheme.surface

@Composable
private fun ReflectSurfaceVariantColor() = MaterialTheme.colorScheme.surfaceVariant

@Composable
private fun ReflectOnSurfaceColor() = MaterialTheme.colorScheme.onSurface

@Composable
private fun ReflectOnSurfaceVariantColor() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun ReflectOnPrimaryColor() = MaterialTheme.colorScheme.onPrimary

@Composable
fun ReflectScreen(
    onSignIn: () -> Unit,
    onOpenVerse: (verseKey: String) -> Unit = {}
) {
    val vm: ReflectViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val reflectTitle = stringResource(R.string.nav_reflect)
    val reflectCommunity = stringResource(R.string.reflect_community)
    val shareLabel = stringResource(R.string.share)
    val shareReflectionLabel = stringResource(R.string.share_reflection)
    val context = LocalContext.current
    val contributorLabel = stringResource(R.string.reflect_contributor)
    val verifiedLabel = stringResource(R.string.verified)
    val signInTitle = stringResource(R.string.reflect_sign_in_title)
    val signInSubtitle = stringResource(R.string.reflect_sign_in_subtitle)
    val errorDisplay = state.error.rememberErrorDisplay(R.string.reflect_feed_load_failed)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        when {
            !state.isAuthenticated -> SignInGate(onSignIn, signInTitle, signInSubtitle)
            state.isLoading && state.posts.isEmpty() -> ReflectLoadingState(
                segment = state.segment,
                onSelectSegment = vm::switchSegment,
                reflectTitle = reflectTitle,
                reflectCommunity = reflectCommunity
            )
            state.error != null && state.posts.isEmpty() && errorDisplay != null ->
                Column(Modifier.fillMaxSize()) {
                    ReflectStickyHeader(
                        segment = state.segment,
                        onSelectSegment = vm::switchSegment,
                        reflectTitle = reflectTitle,
                        reflectCommunity = reflectCommunity
                    )
                    AlKhatibErrorStateDark(
                        display = errorDisplay,
                        onRetry = { vm.loadPosts(reset = true) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = AlKhatibSpacing.screenHorizontal)
                    )
                }
            else -> ReflectFeed(
                state = state,
                vm = vm,
                onOpenVerse = onOpenVerse,
                reflectTitle = reflectTitle,
                reflectCommunity = reflectCommunity,
                shareLabel = shareLabel,
                onSharePost = { post ->
                    val body = (post.body ?: "").stripHtmlTags()
                    val verseKey = post.references?.firstOrNull()?.verseKey
                    val text = buildString {
                        append(body)
                        if (!verseKey.isNullOrBlank()) {
                            if (isNotEmpty()) append("\n\n")
                            append("— $verseKey")
                        }
                    }.ifBlank { return@ReflectFeed }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(intent, shareReflectionLabel))
                },
                contributorLabel = contributorLabel,
                verifiedLabel = verifiedLabel
            )
        }
    }
}

@Composable
private fun ReflectLoadingState(
    segment: ReflectSegment,
    onSelectSegment: (ReflectSegment) -> Unit,
    reflectTitle: String,
    reflectCommunity: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        ReflectStickyHeader(
            segment = segment,
            onSelectSegment = onSelectSegment,
            reflectTitle = reflectTitle,
            reflectCommunity = reflectCommunity
        )
        Column(
            modifier = Modifier.padding(horizontal = AlKhatibSpacing.screenHorizontal, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(3) { ReflectPostSkeleton() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReflectFeed(
    state: ReflectUiState,
    vm: ReflectViewModel,
    onOpenVerse: (String) -> Unit,
    reflectTitle: String,
    reflectCommunity: String,
    shareLabel: String,
    onSharePost: (ReflectFeedPost) -> Unit,
    contributorLabel: String,
    verifiedLabel: String
) {
    val listState = rememberLazyListState()
    val firstVisible by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisible) { vm.loadMoreIfNeeded(firstVisible) }

    val listBottomPadding = floatingNavBottomPadding()
    val scope = rememberCoroutineScope()
    val emptyMessage = stringResource(
        if (state.segment == ReflectSegment.MINE) R.string.reflect_empty_mine
        else R.string.reflect_empty_all
    )

    AlKhatibPullToRefresh(
        isRefreshing = state.isLoading && state.posts.isNotEmpty(),
        onRefresh = { scope.launch { vm.refreshFeed() } },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            ReflectStickyHeader(
                segment = state.segment,
                onSelectSegment = vm::switchSegment,
                reflectTitle = reflectTitle,
                reflectCommunity = reflectCommunity
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = AlKhatibSpacing.screenHorizontal,
                    end = AlKhatibSpacing.screenHorizontal,
                    bottom = listBottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.posts.isEmpty() && !state.isLoading) {
                    item(key = "empty") {
                        ReflectEmptyState(message = emptyMessage)
                    }
                }
                itemsIndexed(state.posts, key = { _, p -> p.id }) { _, post ->
                    ReflectPostCard(
                        post = post,
                        currentUserId = state.currentUserId,
                        togglingLike = post.id in state.togglingLikePostIds,
                        togglingFollow = post.author?.id in state.togglingFollowAuthorIds,
                        onLike = { vm.toggleLike(post.id) },
                        onFollow = { post.author?.id?.let(vm::toggleFollowAuthor) },
                        onOpenVerse = { post.references?.firstOrNull()?.verseKey?.let(onOpenVerse) },
                        contributorLabel = contributorLabel,
                        verifiedLabel = verifiedLabel,
                        shareLabel = shareLabel,
                        onShare = { onSharePost(post) }
                    )
                }
                if (state.isLoadingMore) {
                    item(key = "loading_more") {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AlKhatibColors.GoldBright,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReflectStickyHeader(
    segment: ReflectSegment,
    onSelectSegment: (ReflectSegment) -> Unit,
    reflectTitle: String,
    reflectCommunity: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .tabContentStatusBarInset()
            .padding(
                horizontal = AlKhatibSpacing.screenHorizontal,
                vertical = AlKhatibSpacing.md
            )
    ) {
        Text(
            text = reflectTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = ReflectOnPrimaryColor(),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = reflectCommunity,
            style = MaterialTheme.typography.bodyMedium,
            color = ReflectOnPrimaryColor().copy(alpha = 0.72f)
        )
        Spacer(Modifier.height(AlKhatibSpacing.md))
        SegmentSwitcher(
            segment = segment,
            onSelect = onSelectSegment,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentSwitcher(
    segment: ReflectSegment,
    onSelect: (ReflectSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = segment == ReflectSegment.ALL,
            onClick = { onSelect(ReflectSegment.ALL) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = ReflectOnPrimaryColor().copy(alpha = 0.18f),
                activeContentColor = ReflectOnPrimaryColor(),
                inactiveContainerColor = Color.Transparent,
                inactiveContentColor = ReflectOnPrimaryColor().copy(alpha = 0.55f)
            )
        ) { Text(stringResource(R.string.reflect_all)) }
        SegmentedButton(
            selected = segment == ReflectSegment.MINE,
            onClick = { onSelect(ReflectSegment.MINE) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = ReflectOnPrimaryColor().copy(alpha = 0.18f),
                activeContentColor = ReflectOnPrimaryColor(),
                inactiveContainerColor = Color.Transparent,
                inactiveContentColor = ReflectOnPrimaryColor().copy(alpha = 0.55f)
            )
        ) { Text(stringResource(R.string.reflect_mine)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReflectPostCard(
    post: ReflectFeedPost,
    currentUserId: String?,
    togglingLike: Boolean,
    togglingFollow: Boolean,
    onLike: () -> Unit,
    onFollow: () -> Unit,
    onOpenVerse: () -> Unit,
    contributorLabel: String,
    verifiedLabel: String,
    shareLabel: String,
    onShare: () -> Unit
) {
    val bodyText = (post.body ?: "").stripHtmlTags()
    val verseKey = post.references?.firstOrNull()?.verseKey
    val recentCommentLabel = stringResource(R.string.reflect_recent_comment)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ReflectSurfaceColor())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.author?.avatarUrl,
                contentDescription = post.author?.displayName,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.author?.displayName ?: contributorLabel,
                        color = ReflectPaperInk,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (post.author?.verified == true) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "✓",
                            color = AlKhatibColors.Gold,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = formatReflectTime(post.createdAt),
                    color = ReflectOnSurfaceVariantColor(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            val authorId = post.author?.id
            val showFollowButton = authorId != null && authorId != currentUserId
            if (showFollowButton) {
                Spacer(Modifier.width(8.dp))
                FollowButton(
                    followed = post.author?.followed == true,
                    loading = togglingFollow,
                    onClick = onFollow
                )
            }
        }

        if (!verseKey.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenVerse)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = verseKey,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (bodyText.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = bodyText,
                color = ReflectOnSurfaceColor(),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }

        post.tags?.mapNotNull { it.name?.takeIf(String::isNotBlank) }?.takeIf { it.isNotEmpty() }?.let { tags ->
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.take(4).forEach { tag ->
                    Text(
                        text = "#$tag",
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        color = ReflectOnSurfaceVariantColor(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        post.recentComment?.body?.stripHtmlTags()?.takeIf { it.isNotBlank() }?.let { commentBody ->
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Text(
                    text = recentCommentLabel,
                    color = ReflectOnSurfaceVariantColor(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = commentBody,
                    color = ReflectOnSurfaceColor().copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReflectActionChip(
                icon = if (post.isLiked == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                tint = if (post.isLiked == true) MaterialTheme.colorScheme.error else ReflectOnSurfaceVariantColor(),
                label = (post.likesCount ?: 0).toString(),
                loading = togglingLike,
                onClick = onLike
            )
            ReflectActionChip(
                icon = Icons.Filled.Share,
                tint = ReflectPaperMuted,
                label = shareLabel,
                onClick = onShare
            )
        }
    }
}

@Composable
private fun ReflectActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = !loading, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = tint,
                strokeWidth = 2.dp
            )
        } else {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = ReflectOnSurfaceColor(),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ReflectEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = ReflectOnPrimaryColor().copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun FollowButton(
    followed: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (followed) AlKhatibColors.DeepEmerald.copy(alpha = 0.08f)
                else AlKhatibColors.DeepEmerald.copy(alpha = 0.15f)
            )
            .border(
                width = 1.dp,
                color = if (followed) AlKhatibColors.DeepEmerald.copy(alpha = 0.3f) else AlKhatibColors.DeepEmerald.copy(alpha = 0.6f),
                shape = RoundedCornerShape(50)
            )
            .clickable(enabled = !loading, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = AlKhatibColors.DeepEmerald,
                strokeWidth = 1.5.dp,
                modifier = Modifier.size(14.dp)
            )
        } else {
            Text(
                text = if (followed) stringResource(R.string.following) else stringResource(R.string.follow),
                color = AlKhatibColors.DeepEmerald,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SignInGate(onSignIn: () -> Unit, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Login,
                contentDescription = null,
                tint = AlKhatibColors.GoldBright,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            color = ReflectOnPrimaryColor(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = subtitle,
            color = ReflectOnPrimaryColor().copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSignIn,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(stringResource(R.string.sign_in_qf), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun formatReflectTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val justNow = stringResource(R.string.time_just_now)
    return runCatching {
        val instant = Instant.parse(iso)
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        when {
            duration.toMinutes() < 1 -> justNow
            duration.toHours() < 1 -> stringResource(R.string.time_minutes_ago, duration.toMinutes())
            duration.toDays() < 1 -> stringResource(R.string.time_hours_ago, duration.toHours())
            duration.toDays() == 1L -> stringResource(R.string.time_yesterday)
            duration.toDays() < 7 -> stringResource(R.string.time_days_ago, duration.toDays())
            else -> DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }
    }.getOrDefault(iso.take(10))
}
