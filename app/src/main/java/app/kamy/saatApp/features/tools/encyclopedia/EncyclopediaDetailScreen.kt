package app.kamy.saatApp.features.tools.encyclopedia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors

import androidx.compose.ui.platform.LocalContext
import app.kamy.saatApp.features.today.TanyaSaatViewModel
import app.kamy.saatApp.ui.components.TanyaSaatFullScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaDetailScreen(
    onBack: () -> Unit,
    onOpenVerse: (surahNumber: Int, ayahNumber: Int) -> Unit = { _, _ -> },
    viewModel: EncyclopediaDetailViewModel = hiltViewModel(),
    tanyaSaatVm: TanyaSaatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tanyaSaatState by tanyaSaatVm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val topic = state.topic
    val title = topic?.localizedTitle(state.currentLanguage).orEmpty()
    val subtitle = topic?.localizedSubtitle(state.currentLanguage).orEmpty()
    val summary = topic?.localizedSummary(state.currentLanguage).orEmpty()
    val content = topic?.localizedContent(state.currentLanguage).orEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                app.kamy.saatApp.features.tools.components.SpiritualToolTopBar(
                    title = title,
                    subtitle = subtitle.takeIf { it.isNotBlank() },
                    onBack = onBack
                )
            },
            containerColor = SaatColors.HomeBg
        ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SaatColors.DeepEmerald)
            }
        } else if (topic == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.encyclopedia_empty_title),
                    color = SaatColors.Slate500
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SaatColors.GoldDeep
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = summary,
                            fontSize = 14.sp,
                            color = SaatColors.Slate500,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Qur'an References Section Card
                if (topic.quranReferences.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.encyclopedia_quran_references),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaatColors.DeepEmerald,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    topic.quranReferences.forEach { qRef ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(
                                        R.string.encyclopedia_surah_label,
                                        qRef.localizedSurahName(state.currentLanguage),
                                        qRef.surahNumber,
                                        qRef.ayahRange
                                    ),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.GoldDeep
                                )

                                if (qRef.verseTextAr.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = qRef.verseTextAr,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaatColors.Slate900,
                                        lineHeight = 32.sp
                                    )
                                }

                                val verseTranslation = qRef.localizedVerseTranslation(state.currentLanguage)
                                if (verseTranslation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "\"$verseTranslation\"",
                                        fontSize = 13.sp,
                                        color = SaatColors.Slate500,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content Reader Body
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = content,
                            fontSize = 15.sp,
                            color = SaatColors.Slate900,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }

    if (tanyaSaatState.isSheetVisible) {
        TanyaSaatFullScreen(
            state = tanyaSaatState,
            onDismiss = { tanyaSaatVm.closeSheet() },
            onMoodSelected = tanyaSaatVm::onMoodSelected,
            onInputChanged = tanyaSaatVm::onInputTextChanged,
            onSendMessage = { tanyaSaatVm.sendMessage() },
            onOpenVerseInReader = { chapter, verse ->
                tanyaSaatVm.closeSheet()
                onOpenVerse(chapter, verse)
            },
            onBookmarkVerse = { verseData ->
                tanyaSaatVm.bookmarkVerse(context, verseData)
            },
            onClearToast = tanyaSaatVm::clearToast
        )
    }
}
}
