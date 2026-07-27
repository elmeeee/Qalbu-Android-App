package app.kamy.saatApp.design.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import app.kamy.saatApp.infrastructure.preferences.AppThemeColor
import app.kamy.saatApp.infrastructure.preferences.ThemePreferencesStore

@Composable
fun SaatTheme(
    theme: AppThemeColor = AppThemeColor.EMERALD,
    content: @Composable () -> Unit
) {
    // Synchronize SaatColors state with theme
    SaatColors.applyTheme(theme)

    val dynamicColors = lightColorScheme(
        primary = SaatColors.DeepEmerald,
        onPrimary = SaatColors.OffWhite,
        primaryContainer = SaatColors.SageTint,
        onPrimaryContainer = SaatColors.ForestDeeper,
        secondary = SaatColors.Teal,
        onSecondary = SaatColors.OffWhite,
        secondaryContainer = SaatColors.MintWash,
        onSecondaryContainer = SaatColors.EmeraldRich,
        tertiary = SaatColors.Gold,
        onTertiary = SaatColors.OffWhite,
        tertiaryContainer = SaatColors.AmberWash,
        onTertiaryContainer = SaatColors.GoldDeep,
        background = SaatColors.OffWhite,
        onBackground = SaatColors.Slate900,
        surface = SaatColors.PureWhite,
        onSurface = SaatColors.Slate900,
        surfaceVariant = SaatColors.LightGrey,
        onSurfaceVariant = SaatColors.Slate500,
        surfaceContainerLowest = SaatColors.PureWhite,
        surfaceContainerLow = SaatColors.SageMist,
        surfaceContainer = SaatColors.PanelGreyAlt,
        surfaceContainerHigh = SaatColors.PanelGrey,
        surfaceContainerHighest = SaatColors.SoftGrey,
        outline = SaatColors.SoftGrey,
        outlineVariant = SaatColors.PanelGrey,
        error = SaatColors.Danger,
        onError = SaatColors.OffWhite
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
        typography = SaatTypography,
        shapes = SaatShapes,
        content = content
    )
}
