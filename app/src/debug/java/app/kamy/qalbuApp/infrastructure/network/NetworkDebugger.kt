package app.kamy.qalbuApp.infrastructure.network

import android.app.Application
import com.prowllabs.prowl.Prowl
import com.prowllabs.prowl.applyProwl
import okhttp3.OkHttpClient

/** Debug-only ProwlKit wiring with mocking support. */
object NetworkDebugger {
    fun install(application: Application) {
        Prowl.start(application)
    }

    fun applyTo(builder: OkHttpClient.Builder): OkHttpClient.Builder = builder.applyProwl()
}
