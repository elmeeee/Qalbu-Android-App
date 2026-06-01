package app.kamy.qalbuApp.features.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.kamy.qalbuApp.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.design.components.AlKhatibSkeletonLine
import app.kamy.qalbuApp.design.theme.AlKhatibColors

@Composable
fun ReaderKnowledgeSheetBackground(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AlKhatibColors.OffWhite, AlKhatibColors.SageMist)
                )
            )
            .navigationBarsPadding()
    ) {
        content()
    }
}

@Composable
fun ColumnScope.ReaderSheetScrollBody(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .weight(1f, fill = true)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content
    )
}

@Composable
fun ColumnScope.ReaderSheetLazyBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier
            .weight(1f, fill = true)
            .fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Composable
fun ReaderSheetTopBar(
    title: String,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.DeepEmerald
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onDone) {
            Text(
                stringResource(R.string.done),
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.DeepEmerald
            )
        }
    }
}

@Composable
fun VerseContextHeader(
    verseReference: String,
    icon: ImageVector,
    subtitle: String?
) {
    val thisVerseLabel = stringResource(R.string.this_verse)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 6.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.88f))
            .border(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AlKhatibColors.DeepEmerald.copy(alpha = 0.88f),
            modifier = Modifier.size(28.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = verseReference.ifBlank { thisVerseLabel },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.DeepEmerald
            )
            subtitle?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AlKhatibColors.Slate500
                )
            }
        }
    }
}

@Composable
fun ReaderSheetDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(top = 8.dp),
        color = AlKhatibColors.SoftGrey.copy(alpha = 0.55f)
    )
}

@Composable
fun ReaderLoadingSkeleton(lines: Int = 8) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(lines) { i ->
            AlKhatibSkeletonLine(
                widthFraction = when (i % 3) {
                    0 -> 1f
                    1 -> 0.92f
                    else -> 0.78f
                },
                height = 14.dp
            )
        }
    }
}

@Composable
fun HadithCardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .readerKnowledgeCard()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AlKhatibSkeletonLine(widthFraction = 0.45f, height = 12.dp)
        AlKhatibSkeletonLine(height = 12.dp)
        AlKhatibSkeletonLine(height = 12.dp)
        AlKhatibSkeletonLine(widthFraction = 0.7f, height = 12.dp)
    }
}

@Composable
fun ReaderEmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.AutoMirrored.Filled.MenuBook
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AlKhatibColors.DeepEmerald.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate900,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = AlKhatibColors.Slate500,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ReaderErrorState(
    title: String,
    description: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.WifiOff,
            contentDescription = null,
            tint = AlKhatibColors.Slate500,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate900,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = AlKhatibColors.Slate500,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.try_again))
        }
    }
}

fun Modifier.readerKnowledgeCard(): Modifier = this
    .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.04f))
    .clip(RoundedCornerShape(14.dp))
    .background(Color.White.copy(alpha = 0.88f))
    .border(1.dp, AlKhatibColors.SoftGrey.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
