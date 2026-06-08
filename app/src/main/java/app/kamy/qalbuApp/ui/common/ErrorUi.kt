package app.kamy.qalbuApp.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.kamy.qalbuApp.core.error.AppError
import app.kamy.qalbuApp.core.error.ErrorDisplay
import app.kamy.qalbuApp.core.error.toDisplay

@Composable
fun AppError?.rememberErrorDisplay(@StringRes featureTitleRes: Int? = null): ErrorDisplay? {
    val context = LocalContext.current
    return remember(this, featureTitleRes) {
        this?.toDisplay(context, featureTitleRes)
    }
}
