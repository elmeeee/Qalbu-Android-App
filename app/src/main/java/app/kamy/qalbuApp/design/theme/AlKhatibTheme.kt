package app.kamy.qalbuApp.design.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = AlKhatibColors.DeepEmerald,
    onPrimary = AlKhatibColors.OffWhite,
    primaryContainer = AlKhatibColors.SageTint,
    onPrimaryContainer = AlKhatibColors.ForestDeeper,
    secondary = AlKhatibColors.Teal,
    onSecondary = AlKhatibColors.OffWhite,
    secondaryContainer = AlKhatibColors.MintWash,
    onSecondaryContainer = AlKhatibColors.EmeraldRich,
    tertiary = AlKhatibColors.Gold,
    onTertiary = AlKhatibColors.OffWhite,
    tertiaryContainer = AlKhatibColors.AmberWash,
    onTertiaryContainer = AlKhatibColors.GoldDeep,
    background = AlKhatibColors.ScreenBackground,
    onBackground = AlKhatibColors.Slate900,
    surface = AlKhatibColors.PureWhite,
    onSurface = AlKhatibColors.Slate900,
    surfaceVariant = AlKhatibColors.LightGrey,
    onSurfaceVariant = AlKhatibColors.Slate500,
    outline = AlKhatibColors.SoftGrey,
    outlineVariant = AlKhatibColors.PanelGrey,
    error = AlKhatibColors.Danger,
    onError = AlKhatibColors.OffWhite
)

@Composable
fun AlKhatibTheme(
    // App is always light mode to match current brand.
    content: @Composable () -> Unit
) {
    val colors = LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // MainActivity uses enableEdgeToEdge(); keep system bars light for a bright feel.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AlKhatibTypography,
        content = content
    )
}
