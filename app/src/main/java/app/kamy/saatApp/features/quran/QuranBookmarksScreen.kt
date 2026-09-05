package app.kamy.saatApp.features.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.model.VerseBookmark
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranBookmarksScreen(
    onBack: () -> Unit,
    onOpenVerse: (chapter: Int, ayah: Int) -> Unit
) {
    val context = LocalContext.current
    val counts = rememberQuranLibraryCounts()
    val bookmarks = remember(counts) { QuranPersonalStore.bookmarks(context) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.bookmarks_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.45f))
                            .border(1.dp, Color.White.copy(alpha = 0.75f), CircleShape)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.06f),
                                spotColor = Color.Black.copy(alpha = 0.04f)
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SaatColors.Slate900,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = SaatColors.ScreenBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = SaatSpacing.screenHorizontal,
                vertical = SaatSpacing.sm
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Liquid Glass Hero Card
            item(key = "hero") {
                BookmarksHeroCard(count = bookmarks.size)
            }

            if (bookmarks.isEmpty()) {
                item(key = "empty_bookmarks") {
                    BookmarksEmptyState()
                }
            } else {
                items(bookmarks, key = { "bm_${it.verseKey}" }) { bookmark ->
                    BookmarkItemCard(
                        bookmark = bookmark,
                        onClick = { onOpenVerse(bookmark.chapterNumber, bookmark.verseNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarksHeroCard(count: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.45f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            SaatColors.DeepEmerald.copy(alpha = 0.06f),
                            SaatColors.GoldDeep.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    SaatColors.DeepEmerald.copy(alpha = 0.15f),
                                    SaatColors.GoldDeep.copy(alpha = 0.18f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bookmark_custom),
                        contentDescription = null,
                        tint = SaatColors.DeepEmerald,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.bookmarked_verses_hero_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.Slate900
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = if (count > 0) {
                            stringResource(R.string.bookmarked_verses_count, count)
                        } else {
                            stringResource(R.string.bookmarked_verses_empty_desc)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = SaatColors.Slate500,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkItemCard(
    bookmark: VerseBookmark,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.48f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SaatColors.DeepEmerald.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark_custom),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                val surahName = bookmark.surahLabel ?: stringResource(R.string.surah_number, bookmark.chapterNumber)
                val title = stringResource(R.string.surah_ayah_format, surahName, bookmark.verseNumber)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.Slate900
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.verse_has_bookmark),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SaatColors.Slate500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BookmarksEmptyState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.45f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SaatColors.DeepEmerald.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.75f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bookmark_custom),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.bookmarks_empty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SaatColors.Slate900
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.quran_library_empty_bookmarks_body),
                style = MaterialTheme.typography.bodySmall,
                color = SaatColors.Slate500,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
