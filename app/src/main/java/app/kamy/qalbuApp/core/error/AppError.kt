package app.kamy.qalbuApp.core.error

enum class AppErrorKind {
    NoInternet,
    Unauthorized,
    Forbidden,
    NotFound,
    ClientError,
    ServerError,
    Parsing,
    MissingConfig,
    Location,
    Generic
}

data class AppError(
    val kind: AppErrorKind,
    val debugMessage: String? = null
)

fun Throwable.toAppError(): AppError = when (this) {
    is QFError.Network -> AppError(AppErrorKind.NoInternet, message)
    is QFError.AuthExpired, is QFError.MissingUserSession -> AppError(AppErrorKind.Unauthorized, message)
    is QFError.MissingContentToken -> AppError(AppErrorKind.MissingConfig, message)
    is QFError.HttpStatus -> when (code) {
        401 -> AppError(AppErrorKind.Unauthorized, bodyText ?: message)
        403 -> AppError(AppErrorKind.Forbidden, bodyText ?: message)
        404 -> AppError(AppErrorKind.NotFound, bodyText ?: message)
        in 400..499 -> AppError(AppErrorKind.ClientError, bodyText ?: message)
        in 500..599 -> AppError(AppErrorKind.ServerError, bodyText ?: message)
        else -> AppError(AppErrorKind.Generic, bodyText ?: message)
    }
    is QFError.Parsing -> AppError(AppErrorKind.Parsing, detail)
    else -> when {
        isAuthenticationFailure() -> AppError(AppErrorKind.Unauthorized, message)
        else -> AppError(AppErrorKind.Generic, message)
    }
}
