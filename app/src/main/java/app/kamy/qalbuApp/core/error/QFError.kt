package app.kamy.qalbuApp.core.error

sealed class QFError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    object MissingContentToken : QFError("Content token unavailable")

    object MissingUserSession : QFError("User session expired")

    object AuthExpired : QFError("Auth token expired")

    data class HttpStatus(val code: Int, val bodyText: String? = null) :
        QFError("HTTP $code${bodyText?.let { ": $it" } ?: ""}")

    data class Network(val underlying: Throwable) :
        QFError(underlying.message, underlying)

    data class Parsing(val detail: String) : QFError("Parse error: $detail")
}
