package app.kamy.saatApp.core.error

import android.content.Context
import app.kamy.saatApp.R

fun isAuthHttpFailure(code: Int, body: String?): Boolean {
    if (code == 401) return true
    if (code != 403) return false
    if (parseApiErrorBody(body)?.type == "forbidden") return false
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

fun Throwable.userFacingAuthOrApiMessage(context: Context): String {
    if (isAuthenticationFailure()) return context.getString(R.string.session_expired)
    val fromApi = (this as? QFError.HttpStatus)?.bodyText?.let(::parseApiErrorBody)?.message
    return fromApi?.takeIf { it.isNotBlank() } ?: message ?: context.getString(R.string.request_failed)
}
