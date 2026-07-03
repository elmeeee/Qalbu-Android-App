package app.kamy.saatApp.features.tools.wudhu.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.domain.model.WudhuItem
import app.kamy.saatApp.domain.model.getLocalizedExplanation
import app.kamy.saatApp.domain.model.getLocalizedInstruction
import app.kamy.saatApp.domain.model.getLocalizedProposition
import app.kamy.saatApp.domain.model.getLocalizedTransliteration
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WudhuScreen(
    onBack: () -> Unit,
    viewModel: WudhuViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlKhatibColors.ScreenBackground)
            .statusBarsPadding()
    ) {
        // Custom Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AlKhatibColors.PureWhite,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = AlKhatibColors.Teal
                    )
                }
                Text(
                    text = stringResource(R.string.title_wudhu_guide),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AlKhatibColors.DeepEmerald
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (uiState.loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AlKhatibColors.Teal)
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Error occurred",
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.steps, key = { it.id }) { step ->
                    WudhuStepCard(step = step, language = uiState.language)
                }
            }
        }
    }
}

@Composable
private fun WudhuStepCard(step: WudhuItem, language: app.kamy.saatApp.core.locale.AppLanguage) {
    val explanation = step.getLocalizedExplanation(language)
    val instruction = step.getLocalizedInstruction(language)
    val translit = step.getLocalizedTransliteration(language)
    val proposition = step.getLocalizedProposition(language)
    val isWajib = step.type.equals("wajib", ignoreCase = true)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AlKhatibColors.PureWhite,
        shadowElevation = 3.dp,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    AlKhatibColors.Teal.copy(alpha = 0.25f),
                    AlKhatibColors.Gold.copy(alpha = 0.20f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Step number, Wajib/Sunnah Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AlKhatibColors.Teal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.id.toString(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AlKhatibColors.Teal
                            )
                        )
                    }
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AlKhatibColors.DeepEmerald
                        ),
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                // Wajib / Sunnah Badge
                val badgeBg = if (isWajib) Color(0xFFFFECEC) else Color(0xFFE8F8F0)
                val badgeText = if (isWajib) Color(0xFFE53935) else Color(0xFF2E7D32)
                val label = if (isWajib) "Wajib" else "Sunnah"

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeText.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Step Illustration (graceful fallback check)
            WudhuStepImage(imagePath = step.image)

            // Instruction Detail Text
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = AlKhatibColors.Slate800,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )

            // Arabic text (if present)
            if (!step.arab.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = step.arab,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = AlKhatibColors.DeepEmerald,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Latin / Transliteration text (if present)
            if (!translit.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AlKhatibColors.LightGrey.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = translit,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 22.sp
                        ),
                        color = AlKhatibColors.Teal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Proposition / Reference (if present)
            if (!proposition.isNullOrBlank() && proposition != "--") {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = proposition,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = AlKhatibColors.Slate500,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WudhuStepImage(imagePath: String?, modifier: Modifier = Modifier) {
    if (imagePath.isNullOrBlank()) return
    val model = "file:///android_asset/${imagePath.removePrefix("assets/")}"
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AlKhatibColors.SoftGrey.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = {
                // High-quality fallback design if the asset doesn't exist
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    AlKhatibColors.Teal.copy(alpha = 0.1f),
                                    AlKhatibColors.Gold.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = null,
                            tint = AlKhatibColors.Teal.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        )
    }
    Spacer(Modifier.height(12.dp))
}
