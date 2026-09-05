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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EncyclopediaScreen(
    onBack: () -> Unit,
    onOpenTopic: (String) -> Unit,
    viewModel: EncyclopediaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val isImeVisible = WindowInsets.isImeVisible
    var wasImeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isImeVisible) {
        if (wasImeVisible && !isImeVisible) {
            focusManager.clearFocus()
        }
        wasImeVisible = isImeVisible
    }

    Scaffold(
        topBar = {
            app.kamy.saatApp.features.tools.components.SpiritualToolTopBar(
                title = stringResource(R.string.encyclopedia_title),
                subtitle = stringResource(R.string.tool_encyclopedia_desc),
                onBack = {
                    focusManager.clearFocus()
                    onBack()
                }
            )
        },
        containerColor = SaatColors.HomeBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            // Search Bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onClear = {
                    viewModel.onSearchQueryChanged("")
                    focusManager.clearFocus()
                },
                focusManager = focusManager
            )

            // Category Chips Row
            CategoryChipsRow(
                selectedCategory = state.selectedCategory,
                onSelectCategory = { category ->
                    focusManager.clearFocus()
                    viewModel.selectCategory(category)
                }
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
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
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
                                onClick = {
                                    focusManager.clearFocus()
                                    onOpenTopic(topic.id)
                                }
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
    onClear: () -> Unit,
    focusManager: FocusManager
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = stringResource(category.labelRes),
                        maxLines = 1,
                        softWrap = false
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SaatColors.DeepEmerald,
                    selectedLabelColor = SaatColors.PureWhite
                )
            )
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
                            else -> R.drawable.ic_encyclopedia_custom
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
            painter = painterResource(R.drawable.ic_encyclopedia_custom),
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
