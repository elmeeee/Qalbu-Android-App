package app.kamy.qalbuApp.core.error

/**
 * Mirrors iOS Core/Errors/QFError.swift. Thrown by repositories and the API client.
 */
sealed class QFError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    /** No content-API access token (and refresh failed). */
    object MissingContentToken : QFError("Content token unavailable")

    /** User is signed out or refresh token is invalid. */
    object MissingUserSession : QFError("User session expired")

    /** HTTP 401 — caller should attempt token refresh and retry. */
    object AuthExpired : QFError("Auth token expired")

    /** Generic HTTP failure. */
    data class HttpStatus(val code: Int, val bodyText: String? = null) :
        QFError("HTTP $code${bodyText?.let { ": $it" } ?: ""}")

    /** Network-level failure (no connection, timeout, DNS). */
    data class Network(val underlying: Throwable) :
        QFError(underlying.message, underlying)

    /** JSON decode / parsing failure. */
    data class Parsing(val detail: String) : QFError("Parse error: $detail")
}
