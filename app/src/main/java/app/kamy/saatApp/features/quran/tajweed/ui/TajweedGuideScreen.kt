package app.kamy.saatApp.features.quran.tajweed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.features.quran.tajweed.TajweedType

data class TajweedGuideItem(
    val type: TajweedType,
    val titleRes: Int,
    val descRes: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TajweedGuideScreen(
    onNavigateBack: () -> Unit
) {
    val tajweedItems = listOf(
        TajweedGuideItem(TajweedType.IKHFA_SYAFAWI, R.string.tajweed_ikhfa_syafawi_title, R.string.tajweed_ikhfa_syafawi_desc, Color(0xFFD32F2F)),
        TajweedGuideItem(TajweedType.QALQALAH, R.string.tajweed_qalqalah_title, R.string.tajweed_qalqalah_desc, Color(0xFF1976D2)),
        // We can add more here as needed
        TajweedGuideItem(TajweedType.GHUNNA, R.string.tajweed_ikhfa_syafawi_title, R.string.tajweed_ikhfa_syafawi_desc, Color(0xFFC2185B)),
        TajweedGuideItem(TajweedType.IDGHAM_WITH_GHUNNA, R.string.tajweed_ikhfa_syafawi_title, R.string.tajweed_ikhfa_syafawi_desc, Color(0xFF388E3C))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tajweed Guide") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tajweedItems) { item ->
                TajweedGuideCard(item)
            }
        }
    }
}

@Composable
fun TajweedGuideCard(item: TajweedGuideItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color),
                contentAlignment = Alignment.Center
            ) {
                // Initial or icon
                Text(
                    text = stringResource(item.titleRes).take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(item.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(item.descRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
