package app.kamy.qalbuApp.features.reflect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.ui.layout.floatingNavListPadding
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset
import app.kamy.qalbuApp.domain.model.ReflectFeedPost
import coil.compose.AsyncImage

/**
 * Mirrors iOS Features/Reflection/Views/ReflectionView.swift +
 * ReflectReelFeedView.swift. Full-screen vertical feed of community reflections.
 */
@Composable
fun ReflectScreen(
    onSignIn: () -> Unit,
    onOpenVerse: (verseKey: String) -> Unit = {}
) {
    val vm: ReflectViewModel = hiltViewModel()
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        AlKhatibColors.ForestDark,
                        AlKhatibColors.DeepEmerald,
                        AlKhatibColors.ForestDeeper
                    )
                )
            )
    ) {
        when {
            !state.isAuthenticated -> SignInGate(onSignIn)
            state.isLoading && state.posts.isEmpty() ->
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            state.error != null && state.posts.isEmpty() ->
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error.orEmpty(), color = Color.White)
                    TextButton(onClick = { vm.loadPosts(reset = true) }) { Text("Retry", color = AlKhatibColors.GoldBright) }
                }
            else -> ReelFeed(state = state, vm = vm, onOpenVerse = onOpenVerse)
        }

        // Segment switcher overlay (top).
        if (state.isAuthenticated) {
            SegmentSwitcher(
                segment = state.segment,
                onSelect = vm::switchSegment,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .tabContentStatusBarInset()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ReelFeed(
    state: ReflectUiState,
    vm: ReflectViewModel,
    onOpenVerse: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val firstVisible by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisible) { vm.loadMoreIfNeeded(firstVisible) }

    val listPadding = floatingNavListPadding(extraTop = 56.dp)
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(state.posts, key = { _, p -> p.id }) { _, post ->
            ReelPostCard(
                post = post,
                togglingLike = post.id in state.togglingLikePostIds,
                onLike = { vm.toggleLike(post.id) },
                onOpenVerse = { post.references?.firstOrNull()?.verseKey?.let(onOpenVerse) }
            )
        }
        if (state.isLoadingMore) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
                activeContainerColor = Color.White.copy(alpha = 0.22f),
                activeContentColor = Color.White,
                inactiveContainerColor = Color.Transparent,
                inactiveContentColor = Color.White.copy(alpha = 0.55f)
            )
        ) { Text("All Reflect") }
        SegmentedButton(
            selected = segment == ReflectSegment.MINE,
            onClick = { onSelect(ReflectSegment.MINE) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Color.White.copy(alpha = 0.22f),
                activeContentColor = Color.White,
                inactiveContainerColor = Color.Transparent,
                inactiveContentColor = Color.White.copy(alpha = 0.55f)
            )
        ) { Text("My Reflect") }
    }
}

@Composable
private fun ReelPostCard(
    post: ReflectFeedPost,
    togglingLike: Boolean,
    onLike: () -> Unit,
    onOpenVerse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.author?.avatarUrl,
                contentDescription = post.author?.displayName,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.author?.displayName ?: "Contributor",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (post.author?.verified == true) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = "Verified",
                            tint = AlKhatibColors.Gold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                post.createdAt?.let {
                    Text(
                        text = it.take(10),
                        color = Color.White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            post.references?.firstOrNull()?.verseKey?.let { vk ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AlKhatibColors.Gold.copy(alpha = 0.18f))
                        .border(1.dp, AlKhatibColors.Gold.copy(alpha = 0.45f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = AlKhatibColors.GoldBright, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = vk, color = AlKhatibColors.GoldBright, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
        Spacer(Modifier.height(12.dp))

        // Body
        Text(
            text = (post.body ?: "").replace(Regex("<[^>]+>"), "").trim(),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(16.dp))

        // Action rail
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionRailButton(
                icon = if (post.isLiked == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                tint = if (post.isLiked == true) AlKhatibColors.Danger else Color.White,
                label = (post.likesCount ?: 0).toString(),
                loading = togglingLike,
                onClick = onLike
            )
            ActionRailButton(
                icon = Icons.Filled.Forum,
                tint = Color.White.copy(alpha = 0.6f),
                label = (post.commentsCount ?: 0).toString(),
                onClick = { /* comments hidden until API supports */ }
            )
            ActionRailButton(
                icon = Icons.Filled.Share,
                tint = Color.White.copy(alpha = 0.8f),
                label = "Share",
                onClick = { /* TODO: share */ }
            )
        }
    }
    }
}

@Composable
private fun ActionRailButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (loading) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            }
        } else {
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = tint
                )
            ) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SignInGate(onSignIn: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = AlKhatibColors.GoldBright, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Sign in to Reflect",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sync your reflections across devices and join the community feed.",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onSignIn) { Text("Sign in with Quran Foundation") }
    }
}
