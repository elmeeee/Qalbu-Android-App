package app.kamy.qalbuApp.features.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.doa_zikir_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
        }

        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp),
                color = AlKhatibColors.DeepEmerald
            )
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.selectedSlug == null) {
                items(state.catalog, key = { it.slug }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AlKhatibColors.PureWhite)
                            .clickable { viewModel.selectCategory(entry.slug) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AlKhatibColors.Slate800
                        )
                        Text(
                            text = if (entry.kind == DoaCatalogKind.DHIKR) {
                                stringResource(R.string.doa_zikir_kind_dhikr)
                            } else {
                                stringResource(R.string.doa_zikir_kind_doa)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = AlKhatibColors.Slate500
                        )
                    }
                }
            } else {
                item {
                    TextButtonBack(onBack = { viewModel.clearSelection() })
                }
                if (state.doaItems.isNotEmpty()) {
                    items(state.doaItems, key = { it.id ?: it.title.orEmpty() }) { doa ->
                        DoaCard(
                            title = doa.title.orEmpty(),
                            arabic = doa.arabic.orEmpty(),
                            latin = doa.latin.orEmpty(),
                            translation = doa.translation.orEmpty(),
                            reference = doa.fawaid?.takeIf { it.isNotBlank() && it != "-" }
                        )
                    }
                }
                state.dhikrBundles.forEach { bundle ->
                    item(key = "bundle-${bundle.title}") {
                        bundle.title?.let { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AlKhatibColors.DeepEmerald,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    items(bundle.content.orEmpty(), key = { it.arabic.orEmpty() }) { item ->
                        DoaCard(
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
private fun TextButtonBack(onBack: () -> Unit) {
    Text(
        text = stringResource(R.string.back),
        color = AlKhatibColors.DeepEmerald,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clickable(onClick = onBack)
            .padding(bottom = 4.dp)
    )
}

@Composable
private fun DoaCard(
    title: String,
    arabic: String,
    latin: String,
    translation: String,
    reference: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AlKhatibColors.PureWhite)
            .padding(16.dp)
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.Slate800
            )
            Spacer(Modifier.height(8.dp))
        }
        if (arabic.isNotBlank()) {
            Text(
                text = arabic,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                color = AlKhatibColors.Slate800
            )
            Spacer(Modifier.height(8.dp))
        }
        if (latin.isNotBlank()) {
            Text(
                text = latin.replace("\r\n", "\n"),
                style = MaterialTheme.typography.bodyMedium,
                color = AlKhatibColors.Slate500
            )
            Spacer(Modifier.height(6.dp))
        }
        if (translation.isNotBlank()) {
            Text(
                text = translation.replace("\r\n", "\n"),
                style = MaterialTheme.typography.bodyLarge,
                color = AlKhatibColors.Slate800
            )
        }
        reference?.let {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AlKhatibColors.SoftGrey)
            Spacer(Modifier.height(6.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = AlKhatibColors.Slate500
            )
        }
    }
}
