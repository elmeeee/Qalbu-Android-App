package app.kamy.saatApp.features.quran.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kamy.saatApp.R
import app.kamy.saatApp.design.theme.SaatColors

@Composable
fun QuranReaderHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backdropLayer: GraphicsLayer? = null,
    isPageTurning: Boolean = false,
    onHeightChanged: (androidx.compose.ui.unit.Dp) -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    titleActionDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val density = LocalDensity.current
    val blurEffect = remember(density, backdropLayer) {
        if (backdropLayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            with(density) {
                BlurEffect(
                    radiusX = 16.dp.toPx(),
                    radiusY = 16.dp.toPx(),
                    edgeTreatment = TileMode.Decal
                )
            }
        } else {
            null
        }
    }
    val canBlurBackdrop = backdropLayer != null && blurEffect != null && !isPageTurning
    val surface = MaterialTheme.colorScheme.surface
    val darkSurface = backdropLayer != null && surface.luminance() < 0.5f
    val titleColor = if (darkSurface) Color.White else SaatColors.Slate900
    val subtitleColor = if (darkSurface) Color.White.copy(alpha = 0.76f) else SaatColors.Slate500

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { onHeightChanged(with(density) { it.height.toDp() }) }
            .drawWithContent {
                if (backdropLayer != null && canBlurBackdrop) {
                    val previousEffect = backdropLayer.renderEffect
                    backdropLayer.renderEffect = blurEffect
                    clipRect {
                        drawLayer(backdropLayer)
                    }
                    backdropLayer.renderEffect = previousEffect
                }

                val background = if (backdropLayer == null) {
                    Brush.verticalGradient(
                        colors = listOf(
                            SaatColors.ScreenBackground,
                            SaatColors.ScreenBackground.copy(alpha = 0.96f),
                            SaatColors.ScreenBackground.copy(alpha = 0.85f),
                            SaatColors.ScreenBackground.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                } else {
                    val tintAlpha = if (canBlurBackdrop) 0.78f else 0.97f
                    Brush.verticalGradient(
                        colors = listOf(
                            surface.copy(alpha = tintAlpha),
                            surface.copy(alpha = tintAlpha * 0.94f),
                            surface.copy(alpha = tintAlpha * 0.82f),
                            surface.copy(alpha = tintAlpha * 0.52f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                }
                drawRect(background)
                drawContent()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = titleColor
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onTitleClick != null) {
                            Modifier.clickable(onClick = onTitleClick)
                        } else {
                            Modifier
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onTitleClick != null) {
                IconButton(onClick = onTitleClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_quran_on),
                        contentDescription = titleActionDescription,
                        tint = titleColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            actions()
        }
    }
}
