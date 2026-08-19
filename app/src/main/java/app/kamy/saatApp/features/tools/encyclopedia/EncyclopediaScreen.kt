package app.kamy.saatApp.features.tools.encyclopedia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.EncyclopediaCategory
import app.kamy.saatApp.domain.model.EncyclopediaTopic
import app.kamy.saatApp.domain.model.GlossaryTerm
import app.kamy.saatApp.design.theme.SaatColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaScreen(
    onBack: () -> Unit,
    onOpenTopic: (String) -> Unit,
    viewModel: EncyclopediaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.encyclopedia_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = SaatColors.DeepEmerald
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SaatColors.DeepEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAF9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onClear = { viewModel.onSearchQueryChanged("") }
            )

            // Category Chips Row
            CategoryChipsRow(
                selectedCategory = state.selectedCategory,
                onSelectCategory = viewModel::selectCategory
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Body
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SaatColors.DeepEmerald)
                }
            } else if (state.topics.isEmpty() && state.glossaryTerms.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Articles Section
                    if (state.topics.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.encyclopedia_section_topics),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(state.topics, key = { it.id }) { topic ->
                            TopicCardItem(
                                topic = topic,
                                state = state,
                                onClick = { onOpenTopic(topic.id) }
                            )
                        }
                    }

                    // Glossary Section
                    if (state.glossaryTerms.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.encyclopedia_section_glossary),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.DeepEmerald,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(state.glossaryTerms, key = { it.id }) { term ->
                            GlossaryCardItem(term = term, state = state)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = SaatColors.Slate500
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.encyclopedia_search_hint),
                        color = SaatColors.Slate500,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                )
            )
            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.stop),
                        tint = SaatColors.Slate500
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    selectedCategory: EncyclopediaCategory,
    onSelectCategory: (EncyclopediaCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(EncyclopediaCategory.entries) { category ->
            val isSelected = selectedCategory == category
            val bgContainer = if (isSelected) {
                Brush.horizontalGradient(listOf(SaatColors.DeepEmerald, SaatColors.Teal))
            } else {
                Brush.linearGradient(listOf(Color.White, Color.White))
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgContainer)
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(category.labelRes),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else SaatColors.Slate900
                )
            }
        }
    }
}

@Composable
private fun TopicCardItem(
    topic: EncyclopediaTopic,
    state: EncyclopediaUiState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SaatColors.DeepEmerald.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        when (topic.categoryId) {
                            "prophets" -> R.drawable.ic_faraidh_people
                            "companions" -> R.drawable.ic_faraidh_people
                            "fiqh" -> R.drawable.ic_faraidh_terms
                            else -> R.drawable.ic_faraidh_doc
                        }
                    ),
                    contentDescription = null,
                    tint = SaatColors.DeepEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.localizedTitle(state.currentLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
                Text(
                    text = topic.localizedSubtitle(state.currentLanguage),
                    fontSize = 12.sp,
                    color = SaatColors.GoldDeep,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.localizedSummary(state.currentLanguage),
                    fontSize = 13.sp,
                    color = SaatColors.Slate500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GlossaryCardItem(
    term: GlossaryTerm,
    state: EncyclopediaUiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = term.term,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald
                )
                Text(
                    text = term.termAr,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.GoldDeep
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = term.localizedDefinition(state.currentLanguage),
                fontSize = 13.sp,
                color = SaatColors.Slate900,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_faraidh_doc),
            contentDescription = null,
            tint = SaatColors.Slate500,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.encyclopedia_empty_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SaatColors.Slate900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.encyclopedia_empty_desc),
            fontSize = 13.sp,
            color = SaatColors.Slate500
        )
    }
}
