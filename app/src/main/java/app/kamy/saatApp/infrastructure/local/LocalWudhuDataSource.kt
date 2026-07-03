package app.kamy.saatApp.infrastructure.local

import android.content.Context
import app.kamy.saatApp.domain.model.WudhuItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class LocalWudhuDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) {
    suspend fun getWudhuSteps(): List<WudhuItem> = withContext(Dispatchers.IO) {
        context.assets.open("qara/wudhu.json").bufferedReader().use { it.readText() }.let { text ->
            json.decodeFromString<List<WudhuItem>>(text)
        }
    }
}
