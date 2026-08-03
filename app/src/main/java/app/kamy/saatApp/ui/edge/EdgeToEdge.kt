package app.kamy.saatApp.ui.edge

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

object EdgeToEdge {
    @JvmStatic
    fun enable(window: Window, view: View, enableLightBars: Boolean = true) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.isAppearanceLightStatusBars = enableLightBars
            controller.isAppearanceLightNavigationBars = enableLightBars
        }
    }
}

fun enableEdgeToEdge(window: Window, view: View, enableLightBars: Boolean = true) {
    EdgeToEdge.enable(window, view, enableLightBars)
}

@Composable
fun EdgeToEdgeContent(enableLightBars: Boolean = true) {
    val view = LocalView.current

    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        EdgeToEdge.enable(window, view, enableLightBars)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
