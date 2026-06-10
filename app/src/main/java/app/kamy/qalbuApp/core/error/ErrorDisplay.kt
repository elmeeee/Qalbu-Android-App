package app.kamy.qalbuApp.core.error

import android.content.Context
import app.kamy.qalbuApp.R

enum class ErrorIcon {
    NoInternet,
    Server,
    Forbidden,
    Unauthorized,
    NotFound,
    RateLimited,
    Generic
}

data class ErrorDisplay(
    val title: String,
    val description: String,
    val icon: ErrorIcon
)

fun AppError.toDisplay(
    context: Context,
    featureTitleRes: Int? = null
): ErrorDisplay {
    val title = featureTitleRes?.let(context::getString)
        ?: context.getString(
            when (kind) {
                AppErrorKind.NoInternet -> R.string.error_no_internet_title
                AppErrorKind.Unauthorized -> R.string.error_unauthorized_title
                AppErrorKind.Forbidden -> R.string.error_forbidden_title
                AppErrorKind.NotFound -> R.string.error_not_found_title
                AppErrorKind.ClientError -> R.string.error_client_title
                AppErrorKind.ServerError -> R.string.error_server_title
                AppErrorKind.RateLimited -> R.string.error_rate_limit_title
                AppErrorKind.Parsing -> R.string.error_parsing_title
                AppErrorKind.MissingConfig -> R.string.error_config_title
                AppErrorKind.Location -> R.string.error_location_title
                AppErrorKind.Generic -> R.string.error_generic_title
            }
        )
    val fallbackDescription = context.getString(
        when (kind) {
            AppErrorKind.NoInternet -> R.string.error_no_internet_body
            AppErrorKind.Unauthorized -> R.string.error_unauthorized_body
            AppErrorKind.Forbidden -> R.string.error_forbidden_body
            AppErrorKind.NotFound -> R.string.error_not_found_body
            AppErrorKind.ClientError -> R.string.error_client_body
            AppErrorKind.ServerError -> R.string.error_server_body
            AppErrorKind.RateLimited -> R.string.error_rate_limit_body
            AppErrorKind.Parsing -> R.string.error_parsing_body
            AppErrorKind.MissingConfig -> R.string.error_config_body
            AppErrorKind.Location -> R.string.error_location_body
            AppErrorKind.Generic -> R.string.error_generic_body
        }
    )
    val description = apiMessage?.takeIf { it.isNotBlank() } ?: fallbackDescription
    val icon = when (kind) {
        AppErrorKind.NoInternet -> ErrorIcon.NoInternet
        AppErrorKind.Unauthorized -> ErrorIcon.Unauthorized
        AppErrorKind.Forbidden -> ErrorIcon.Forbidden
        AppErrorKind.NotFound -> ErrorIcon.NotFound
        AppErrorKind.RateLimited -> ErrorIcon.RateLimited
        AppErrorKind.Location -> ErrorIcon.Generic
        AppErrorKind.ServerError, AppErrorKind.ClientError, AppErrorKind.Parsing -> ErrorIcon.Server
        AppErrorKind.MissingConfig, AppErrorKind.Generic -> ErrorIcon.Generic
    }
    return ErrorDisplay(title = title, description = description, icon = icon)
}
