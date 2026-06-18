package app.kamy.saatApp.core.error

import app.kamy.saatApp.core.error.QFError
import retrofit2.HttpException

suspend inline fun <T> qfCall(crossinline block: suspend () -> T): T {
    return try {
        block()
    } catch (e: QFError) {
        throw e
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string()
        when {
            isAuthHttpFailure(e.code(), body) -> throw QFError.AuthExpired
            else -> throw QFError.HttpStatus(e.code(), body)
        }
    } catch (e: java.io.IOException) {
        throw QFError.Network(e)
    } catch (e: kotlinx.serialization.SerializationException) {
        throw QFError.Parsing(e.message ?: "serialization failed")
    }
}
