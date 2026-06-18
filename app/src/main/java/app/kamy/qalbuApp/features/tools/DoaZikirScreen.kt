package app.kamy.qalbuApp.features.tools

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.domain.model.DoaCatalogKind
import app.kamy.qalbuApp.ui.layout.tabContentStatusBarInset

@Composable
fun DoaZikirScreen(
    onBack: () -> Unit,
    viewModel: DoaZikirViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val inDetail = state.selectedSlug != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        DoaZikirTopBar(
            inDetail = inDetail,
            title = if (inDetail) {
                state.selectedTitle.orEmpty()
            } else {
                stringResource(R.string.doa_zikir_title)
            },
            onBack = {
                if (inDetail) viewModel.clearSelection() else onBack()
            }
        )

        if (state.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AlKhatibColors.DeepEmerald)
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!inDetail) {
                items(state.catalog, key = { it.slug }) { entry ->
                    CatalogRow(
                        title = entry.title,
                        kindLabel = if (entry.kind == DoaCatalogKind.DHIKR) {
                            stringResource(R.string.doa_zikir_kind_dhikr)
                        } else {
                            stringResource(R.string.doa_zikir_kind_doa)
                        },
                        onClick = { viewModel.selectCategory(entry.slug) }
                    )
                }
            } else {
                if (state.doaItems.isNotEmpty()) {
                    items(state.doaItems, key = { it.id ?: it.title.orEmpty() }) { doa ->
                        PremiumDoaCard(
                            title = doa.title.orEmpty(),
                            arabic = doa.arabic.orEmpty(),
                            latin = doa.latin.orEmpty(),
                            translation = doa.translation.orEmpty(),
                            reference = doa.fawaid?.takeIf { it.isNotBlank() && it != "-" }
                        )
                    }
                }
                state.dhikrBundles.forEach { bundle ->
                    val showBundleTitle = state.dhikrBundles.size > 1 ||
                        bundle.title?.equals(state.selectedTitle, ignoreCase = true) == false
                    if (showBundleTitle) {
                        item(key = "bundle-${bundle.title}") {
                            bundle.title?.let { title ->
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AlKhatibColors.TealDark,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }
                    items(bundle.content.orEmpty(), key = { it.arabic.orEmpty() }) { item ->
                        PremiumDoaCard(
                            title = "",
                            arabic = item.arabic.orEmpty(),
                            latin = item.latin.orEmpty(),
                            translation = item.translation.orEmpty(),
                            reference = item.fawaid?.takeIf { it.isNotBlank() && it != "-" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoaZikirTopBar(
    inDetail: Boolean,
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (inDetail) {
                    Modifier
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    AlKhatibColors.DeepEmerald.copy(alpha = 0.12f),
                                    AlKhatibColors.Teal.copy(alpha = 0.06f)
                                )
                            )
                        )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp, vertical = if (inDetail) 10.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = AlKhatibColors.DeepEmerald
            )
        }
        Text(
            text = title,
            style = if (inDetail) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.DeepEmerald,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CatalogRow(
    title: String,
    kindLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AlKhatibColors.PureWhite)
            .border(
                width = 1.dp,
                color = AlKhatibColors.SoftGrey.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = AlKhatibColors.Slate800,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = kindLabel,
            style = MaterialTheme.typography.labelSmall,
            color = AlKhatibColors.TealDark,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(AlKhatibColors.MintWash)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PremiumDoaCard(
    title: String,
    arabic: String,
    latin: String,
    translation: String,
    reference: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AlKhatibColors.PureWhite)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        AlKhatibColors.Teal.copy(alpha = 0.35f),
                        AlKhatibColors.SoftGrey.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(AlKhatibColors.DeepEmerald, AlKhatibColors.Teal)
                    )
                )
        )
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AlKhatibColors.DeepEmerald
                )
                Spacer(Modifier.height(10.dp))
            }
            if (arabic.isNotBlank()) {
                Text(
                    text = arabic,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        lineHeight = 36.sp,
                        fontSize = 26.sp
                    ),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    color = AlKhatibColors.Slate900
                )
                Spacer(Modifier.height(12.dp))
            }
            if (latin.isNotBlank()) {
                Text(
                    text = latin.replace("\r\n", "\n"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlKhatibColors.Slate500,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(8.dp))
            }
            if (translation.isNotBlank()) {
                Text(
                    text = translation.replace("\r\n", "\n"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AlKhatibColors.Slate800,
                    lineHeight = 24.sp
                )
            }
            reference?.let {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = AlKhatibColors.SoftGrey.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(AlKhatibColors.Teal)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = AlKhatibColors.Slate500
                    )
                }
            }
        }
    }
}
