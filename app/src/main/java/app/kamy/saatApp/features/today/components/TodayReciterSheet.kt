package app.kamy.saatApp.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.domain.model.RecitationPayload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayReciterSheet(
    visible: Boolean,
    recitations: List<RecitationPayload>,
    selectedRecitationId: Int,
    onDismiss: () -> Unit,
    onSelectRecitation: (Int) -> Unit
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.reciter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AlKhatibColors.DeepEmerald
            )
            if (recitations.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = AlKhatibColors.DeepEmerald
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(recitations, key = { it.identifiableId }) { recitation ->
                        val selected = recitation.identifiableId == selectedRecitationId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectRecitation(recitation.identifiableId) }
                                .background(
                                    if (selected) AlKhatibColors.DeepEmerald.copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = recitation.displayName,
                                modifier = Modifier.weight(1f),
                                color = AlKhatibColors.Slate900,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (selected) {
                                Text(
                                    text = "✓",
                                    color = AlKhatibColors.DeepEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
