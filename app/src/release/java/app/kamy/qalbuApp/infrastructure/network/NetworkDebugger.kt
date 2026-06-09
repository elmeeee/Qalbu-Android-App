package app.kamy.qalbuApp.infrastructure.network

import android.app.Application
import okhttp3.OkHttpClient

/** Release no-op — ProwlKit is not on the classpath. */
object NetworkDebugger {
    fun install(application: Application) = Unit

    fun applyTo(builder: OkHttpClient.Builder): OkHttpClient.Builder = builder
}
