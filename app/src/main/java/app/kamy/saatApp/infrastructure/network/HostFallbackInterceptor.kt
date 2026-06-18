package app.kamy.saatApp.infrastructure.network

import okhttp3.Interceptor
import okhttp3.Response
import java.net.UnknownHostException

class HostFallbackInterceptor : Interceptor {
    private val fallbackHosts = mapOf(
        "apis.quran.foundation" to "apis-prelive.quran.foundation",
        "oauth2.quran.foundation" to "prelive-oauth2.quran.foundation"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (e: UnknownHostException) {
            val fallbackHost = fallbackHosts[request.url.host] ?: throw e
            val fallbackUrl = request.url.newBuilder().host(fallbackHost).build()
            chain.proceed(
                request.newBuilder()
                    .url(fallbackUrl)
                    .build()
            )
        }
    }
}

