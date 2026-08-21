package app.kamy.saatApp.core.error

enum class AppErrorKind {
    NoInternet,
    Unauthorized,
    Forbidden,
    NotFound,
    ClientError,
    ServerError,
    RateLimited,
    Parsing,
    MissingConfig,
    Location,
    Generic
}

data class AppError(
    val kind: AppErrorKind,
    val apiMessage: String? = null,
    val apiType: String? = null,
    val debugMessage: String? = null
)

fun Throwable.toAppError(): AppError =
    AppError(AppErrorKind.Generic, debugMessage = message)
