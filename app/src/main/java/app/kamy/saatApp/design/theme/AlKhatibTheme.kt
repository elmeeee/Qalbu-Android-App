package app.kamy.saatApp.design.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import app.kamy.saatApp.infrastructure.preferences.AppThemeColor

@Composable
fun AlKhatibTheme(
    theme: AppThemeColor = AppThemeColor.EMERALD,
    content: @Composable () -> Unit
) {
    // Synchronize AlKhatibColors state with theme
    AlKhatibColors.applyTheme(theme)

    val dynamicColors = lightColorScheme(
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
        background = AlKhatibColors.OffWhite,
        onBackground = AlKhatibColors.Slate900,
        surface = AlKhatibColors.PureWhite,
        onSurface = AlKhatibColors.Slate900,
        surfaceVariant = AlKhatibColors.LightGrey,
        onSurfaceVariant = AlKhatibColors.Slate500,
        surfaceContainerLowest = AlKhatibColors.PureWhite,
        surfaceContainerLow = AlKhatibColors.SageMist,
        surfaceContainer = AlKhatibColors.PanelGreyAlt,
        surfaceContainerHigh = AlKhatibColors.PanelGrey,
        surfaceContainerHighest = AlKhatibColors.SoftGrey,
        outline = AlKhatibColors.SoftGrey,
        outlineVariant = AlKhatibColors.PanelGrey,
        error = AlKhatibColors.Danger,
        onError = AlKhatibColors.OffWhite
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = dynamicColors,
        typography = AlKhatibTypography,
        shapes = AlKhatibShapes,
        content = content
    )
}
