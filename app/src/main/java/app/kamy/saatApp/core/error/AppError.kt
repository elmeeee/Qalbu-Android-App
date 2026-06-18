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

private fun QFError.HttpStatus.toAppError(): AppError {
    val parsed = parseApiErrorBody(bodyText)
    val apiMessage = parsed?.message?.takeIf { it.isNotBlank() }
    val apiType = parsed?.type?.takeIf { it.isNotBlank() }
    val kind = when (code) {
        401 -> AppErrorKind.Unauthorized
        403 -> AppErrorKind.Forbidden
        404 -> AppErrorKind.NotFound
        429 -> AppErrorKind.RateLimited
        in 400..499 -> AppErrorKind.ClientError
        in 500..599 -> AppErrorKind.ServerError
        else -> AppErrorKind.Generic
    }
    return AppError(kind = kind, apiMessage = apiMessage, apiType = apiType, debugMessage = bodyText ?: message)
}

fun Throwable.toAppError(): AppError = when (this) {
    is QFError.Network -> AppError(AppErrorKind.NoInternet, debugMessage = message)
    is QFError.AuthExpired, is QFError.MissingUserSession ->
        AppError(AppErrorKind.Unauthorized, debugMessage = message)
    is QFError.MissingContentToken ->
        AppError(AppErrorKind.MissingConfig, debugMessage = message)
    is QFError.HttpStatus -> toAppError()
    is QFError.Parsing -> AppError(AppErrorKind.Parsing, debugMessage = detail)
    else -> when {
        isAuthenticationFailure() -> AppError(AppErrorKind.Unauthorized, debugMessage = message)
        else -> AppError(AppErrorKind.Generic, debugMessage = message)
    }
}
