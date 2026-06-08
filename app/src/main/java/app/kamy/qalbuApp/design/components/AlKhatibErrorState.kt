package app.kamy.qalbuApp.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.kamy.qalbuApp.R
import app.kamy.qalbuApp.core.error.ErrorDisplay
import app.kamy.qalbuApp.core.error.ErrorIcon
import app.kamy.qalbuApp.design.theme.AlKhatibColors
import app.kamy.qalbuApp.design.theme.AlKhatibSpacing
import androidx.compose.material3.Icon

@Composable
fun AlKhatibErrorState(
    display: ErrorDisplay,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(R.string.try_again)
) {
    AlKhatibErrorStateContent(
        display = display,
        onRetry = onRetry,
        modifier = modifier,
        retryLabel = retryLabel,
        titleColor = AlKhatibColors.Slate900,
        bodyColor = AlKhatibColors.Slate500,
        iconTint = AlKhatibColors.Slate500,
        buttonColors = ButtonDefaults.buttonColors(
            containerColor = AlKhatibColors.DeepEmerald,
            contentColor = Color.White
        )
    )
}

@Composable
fun AlKhatibErrorStateDark(
    display: ErrorDisplay,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(R.string.try_again)
) {
    AlKhatibErrorStateContent(
        display = display,
        onRetry = onRetry,
        modifier = modifier,
        retryLabel = retryLabel,
        titleColor = Color.White,
        bodyColor = Color.White.copy(alpha = 0.78f),
        iconTint = AlKhatibColors.GoldBright,
        buttonColors = ButtonDefaults.buttonColors(
            containerColor = AlKhatibColors.GoldBright,
            contentColor = AlKhatibColors.ForestDeeper
        )
    )
}

@Composable
fun AlKhatibErrorStateCompact(
    display: ErrorDisplay,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AlKhatibSpacing.md, horizontal = AlKhatibSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AlKhatibSpacing.sm)
    ) {
        Icon(
            imageVector = display.icon.toImageVector(),
            contentDescription = null,
            tint = AlKhatibColors.Slate500,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = display.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate900,
            textAlign = TextAlign.Center
        )
        Text(
            text = display.description,
            style = MaterialTheme.typography.bodySmall,
            color = AlKhatibColors.Slate500,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.try_again),
                color = AlKhatibColors.DeepEmerald,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AlKhatibInlineError(
    display: ErrorDisplay,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AlKhatibSpacing.sm)
    ) {
        Icon(
            imageVector = display.icon.toImageVector(),
            contentDescription = null,
            tint = AlKhatibColors.Danger.copy(alpha = 0.85f),
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = display.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AlKhatibColors.Slate900,
            textAlign = TextAlign.Center
        )
        Text(
            text = display.description,
            style = MaterialTheme.typography.bodySmall,
            color = AlKhatibColors.Slate500,
            textAlign = TextAlign.Center
        )
        onRetry?.let { retry ->
            OutlinedButton(onClick = retry) {
                Text(stringResource(R.string.try_again))
            }
        }
    }
}

@Composable
private fun AlKhatibErrorStateContent(
    display: ErrorDisplay,
    onRetry: () -> Unit,
    modifier: Modifier,
    retryLabel: String,
    titleColor: Color,
    bodyColor: Color,
    iconTint: Color,
    buttonColors: androidx.compose.material3.ButtonColors
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AlKhatibSpacing.xl, vertical = AlKhatibSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AlKhatibSpacing.md)
    ) {
        Icon(
            imageVector = display.icon.toImageVector(),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = display.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = display.description,
            style = MaterialTheme.typography.bodyMedium,
            color = bodyColor,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry, colors = buttonColors) {
            Text(retryLabel)
        }
    }
}

private fun ErrorIcon.toImageVector(): ImageVector = when (this) {
    ErrorIcon.NoInternet -> Icons.Filled.WifiOff
    ErrorIcon.Server -> Icons.Filled.CloudOff
    ErrorIcon.Forbidden -> Icons.Filled.Lock
    ErrorIcon.Unauthorized -> Icons.AutoMirrored.Filled.Login
    ErrorIcon.NotFound -> Icons.Filled.SearchOff
    ErrorIcon.Generic -> Icons.Filled.ErrorOutline
}
