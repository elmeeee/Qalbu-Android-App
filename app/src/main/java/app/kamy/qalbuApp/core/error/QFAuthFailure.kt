package app.kamy.qalbuApp.core.error

import app.kamy.qalbuApp.infrastructure.auth.UserSession

const val SESSION_EXPIRED_MESSAGE = "Session expired. Sign in again from Account."

fun isAuthHttpFailure(code: Int, body: String?): Boolean {
    if (code == 401) return true
    if (code != 403) return false
    val lower = body?.lowercase().orEmpty()
    return lower.contains("token") && lower.contains("expired")
        || lower.contains("token expired")
        || lower.contains("invalid_token")
        || lower.contains("invalid_grant")
        || lower.contains("session expired")
        || lower.contains("not authorized")
        || lower.contains("unauthorized")
}

fun Throwable.isAuthenticationFailure(): Boolean = when (this) {
    is QFError.MissingUserSession, is QFError.AuthExpired -> true
    is QFError.HttpStatus -> isAuthHttpFailure(code, bodyText)
    else -> {
        val lower = message?.lowercase().orEmpty()
        lower.contains("http 401")
            || lower.contains("http 403")
            || lower.contains("token expired")
            || lower.contains("invalid_grant")
            || lower.contains("invalid_token")
            || lower.contains("session expired")
    }
}

fun Throwable.userFacingAuthOrApiMessage(): String =
    if (isAuthenticationFailure()) SESSION_EXPIRED_MESSAGE else message ?: "Request failed"

suspend fun UserSession.invalidateIfAuthenticationFailure(error: Throwable): Boolean {
    if (!error.isAuthenticationFailure()) return false
    clear()
    return true
}
