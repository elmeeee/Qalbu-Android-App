package app.kamy.saatApp.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.R
import app.kamy.saatApp.core.error.ErrorDisplay
import app.kamy.saatApp.core.error.ErrorIcon
import app.kamy.saatApp.design.theme.AlKhatibColors
import app.kamy.saatApp.design.theme.AlKhatibSpacing
import kotlinx.coroutines.delay

// ─── Public entry-points ─────────────────────────────────────────────────────

/**
 * Full-page / centered error state for light backgrounds (e.g. Chapters, Reader).
 * Shows a large icon, title, API message (if present), fallback description, and a retry button.
 */
@Composable
fun AlKhatibErrorState(
    display: ErrorDisplay,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(R.string.try_again)
) {
    val accentColor = display.icon.accentColor()
    val iconBg = accentColor.copy(alpha = 0.10f)

    ErrorStateContainer(
        modifier = modifier,
        background = Color.White,
        border = accentColor.copy(alpha = 0.14f)
    ) {
        // Animated icon with pulsing ring
        PulsingIconBox(
            imageVector = display.icon.toImageVector(),
            tint = accentColor,
            background = iconBg,
            size = 56.dp
        )

        Spacer(Modifier.height(AlKhatibSpacing.md))

        // Title
        Text(
            text = display.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AlKhatibColors.Slate900,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AlKhatibSpacing.sm))

        // Description — shows the API message when available (most actionable info)
        Text(
            text = display.description,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = AlKhatibColors.Slate500,
            textAlign = TextAlign.Center
        )

        // API error type badge
        display.apiType?.let { type ->
            Spacer(Modifier.height(AlKhatibSpacing.sm))
            ApiErrorTypeBadge(apiType = type, accentColor = accentColor)
        }

        Spacer(Modifier.height(AlKhatibSpacing.lg))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 0.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(retryLabel, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Full-page error state for dark/gradient backgrounds (e.g. Reflect feed).
 */
@Composable
fun AlKhatibErrorStateDark(
    display: ErrorDisplay,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(R.string.try_again)
) {
    val accentColor = AlKhatibColors.GoldBright

    ErrorStateContainer(
        modifier = modifier,
        background = Color.White.copy(alpha = 0.08f),
        border = Color.White.copy(alpha = 0.16f)
    ) {
        PulsingIconBox(
            imageVector = display.icon.toImageVector(),
            tint = accentColor,
            background = Color.White.copy(alpha = 0.12f),
            size = 56.dp
        )

        Spacer(Modifier.height(AlKhatibSpacing.md))

        Text(
            text = display.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AlKhatibSpacing.sm))

        Text(
            text = display.description,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = Color.White.copy(alpha = 0.78f),
            textAlign = TextAlign.Center
        )

        display.apiType?.let { type ->
            Spacer(Modifier.height(AlKhatibSpacing.sm))
            ApiErrorTypeBadge(
                apiType = type,
                accentColor = accentColor,
                forceLightContent = true
            )
        }

        Spacer(Modifier.height(AlKhatibSpacing.lg))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = AlKhatibColors.ForestDeeper
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(retryLabel, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Compact error card — for embedding inside feature cards (e.g. Verse of Day).
 */
@Composable
fun AlKhatibErrorStateCompact(
    display: ErrorDisplay,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = display.icon.accentColor()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            .border(1.dp, accentColor.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(vertical = AlKhatibSpacing.md, horizontal = AlKhatibSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AlKhatibSpacing.xs)
    ) {
        Icon(
            imageVector = display.icon.toImageVector(),
            contentDescription = null,
            tint = accentColor,
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
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = AlKhatibColors.Slate500,
            textAlign = TextAlign.Center
        )
        display.apiType?.let { type ->
            ApiErrorTypeBadge(apiType = type, accentColor = accentColor)
        }
        Spacer(Modifier.height(4.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.try_again),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/**
 * Inline error — for use inside sheets / bottom panels.
 * Shows error details prominently with a retry option.
 */
@Composable
fun AlKhatibInlineError(
    display: ErrorDisplay,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accentColor = display.icon.accentColor()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .border(1.dp, accentColor.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = AlKhatibSpacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon pill
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(accentColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = display.icon.toImageVector(),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = display.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AlKhatibColors.Slate900
            )
            Spacer(Modifier.height(2.dp))
            // Description carries the API message when present
            Text(
                text = display.description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = AlKhatibColors.Slate500
            )
            display.apiType?.let { type ->
                Spacer(Modifier.height(AlKhatibSpacing.xs))
                ApiErrorTypeBadge(apiType = type, accentColor = accentColor)
            }
        }

        if (onRetry != null) {
            TextButton(
                onClick = onRetry,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.try_again),
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── Internal layout container ────────────────────────────────────────────────

@Composable
private fun ErrorStateContainer(
    modifier: Modifier,
    background: Color,
    border: Color,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(background, RoundedCornerShape(20.dp))
                .border(1.dp, border, RoundedCornerShape(20.dp))
                .padding(horizontal = AlKhatibSpacing.xl, vertical = AlKhatibSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

// ─── Pulsing icon ─────────────────────────────────────────────────────────────

@Composable
private fun PulsingIconBox(
    imageVector: ImageVector,
    tint: Color,
    background: Color,
    size: androidx.compose.ui.unit.Dp
) {
    var triggered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0.85f,
        animationSpec = tween(400),
        label = "errorIconScale"
    )
    LaunchedEffect(Unit) {
        delay(80)
        triggered = true
    }

    Box(
        modifier = Modifier
            .size(size + 16.dp)
            .scale(scale)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(size + 16.dp)
                .border(1.5.dp, tint.copy(alpha = 0.18f), CircleShape)
        )
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size - 8.dp)
        )
    }
}

// ─── API type badge ────────────────────────────────────────────────────────────

@Composable
private fun ApiErrorTypeBadge(
    apiType: String,
    accentColor: Color,
    forceLightContent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val surfaceAlpha = if (forceLightContent) 0.22f else 0.10f
    val contentAlpha = if (forceLightContent) 1f else 0.9f

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = accentColor.copy(alpha = surfaceAlpha),
        modifier = modifier
    ) {
        Text(
            text = apiType.formatAsErrorType(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = accentColor.copy(alpha = contentAlpha),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun ErrorIcon.toImageVector(): ImageVector = when (this) {
    ErrorIcon.NoInternet  -> Icons.Filled.WifiOff
    ErrorIcon.Server      -> Icons.Filled.CloudOff
    ErrorIcon.Forbidden   -> Icons.Filled.Lock
    ErrorIcon.Unauthorized -> Icons.AutoMirrored.Filled.Login
    ErrorIcon.NotFound    -> Icons.Filled.SearchOff
    ErrorIcon.RateLimited -> Icons.Filled.Schedule
    ErrorIcon.Generic     -> Icons.Filled.ErrorOutline
}

/**
 * Per-category accent colour. Semantically meaningful and visually distinct.
 */
private fun ErrorIcon.accentColor(): Color = when (this) {
    ErrorIcon.NoInternet   -> Color(0xFF3B82F6)  // blue-500   — network
    ErrorIcon.Server       -> Color(0xFFEF4444)  // red-500    — server down
    ErrorIcon.Forbidden    -> Color(0xFFDC2626)  // red-600    — forbidden
    ErrorIcon.Unauthorized -> Color(0xFFF59E0B)  // amber-500  — auth
    ErrorIcon.NotFound     -> Color(0xFF6366F1)  // indigo-500 — 404
    ErrorIcon.RateLimited  -> Color(0xFFF97316)  // orange-500 — throttle
    ErrorIcon.Generic      -> Color(0xFF64748B)  // slate-500  — unknown
}

/**
 * Converts a snake_case or dash-case API error type string to Title Case.
 * "rate_limit_exceeded" → "Rate Limit Exceeded"
 */
private fun String.formatAsErrorType(): String = replace('-', ' ')
    .replace('_', ' ')
    .split(' ')
    .filter { it.isNotBlank() }
    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }
