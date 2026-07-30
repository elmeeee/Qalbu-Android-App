package app.kamy.saatApp.infrastructure.local

import app.kamy.saatApp.domain.model.DhikrListResponse
import app.kamy.saatApp.domain.model.DoaListResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalDoaDataSourceTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val assetsDir = File("src/main/assets/doa")

    @Test
    fun testDecodeDhikrAfterSalah() {
        val file = File(assetsDir, "dhikir_dhikir-after-salah.json")
        assertTrue("File should exist", file.exists())
        val text = file.readText()
        val response = json.decodeFromString(DhikrListResponse.serializer(), text)
        assertNotNull(response.data)
        assertFalse("Data should not be empty", response.data.isNullOrEmpty())
        val firstContent = response.data?.first()?.content?.first()
        assertNotNull("Translation should be parsed", firstContent?.translation)
        assertTrue("Translation should not be blank", firstContent?.translation?.isNotBlank() == true)
    }

    @Test
    fun testDecodeSleepDhikr() {
        val file = File(assetsDir, "dhikir_sleep-dhikir.json")
        assertTrue("File should exist", file.exists())
        val text = file.readText()
        val response = json.decodeFromString(DhikrListResponse.serializer(), text)
        assertNotNull(response.data)
        assertFalse("Data should not be empty", response.data.isNullOrEmpty())
        val firstContent = response.data?.first()?.content?.first()
        assertNotNull("Translation should be parsed", firstContent?.translation)
        assertTrue("Translation should not be blank", firstContent?.translation?.isNotBlank() == true)
    }

    @Test
    fun testDecodeDoaKetikaSakit() {
        val file = File(assetsDir, "duas_doa-ketika-sakit.json")
        assertTrue("File should exist", file.exists())
        val text = file.readText()
        val response = json.decodeFromString(DoaListResponse.serializer(), text)
        assertNotNull(response.data)
        assertFalse("Data should not be empty", response.data.isNullOrEmpty())
        val firstItem = response.data?.first()
        assertNotNull("Translation should be parsed", firstItem?.translation)
        assertTrue("Translation should not be blank", firstItem?.translation?.isNotBlank() == true)
    }

    @Test
    fun testDecodeFadhilahDoa() {
        val file = File(assetsDir, "duas_fadhilahdoa.json")
        assertTrue("File should exist", file.exists())
        val text = file.readText()
        val response = json.decodeFromString(DoaListResponse.serializer(), text)
        assertNotNull(response.data)
        assertFalse("Data should not be empty", response.data.isNullOrEmpty())
        val firstItem = response.data?.first()
        assertNotNull("Translation should be parsed", firstItem?.translation)
        assertTrue("Translation should not be blank", firstItem?.translation?.isNotBlank() == true)
    }

    @Test
    fun testDecodeSelectedDoa() {
        val file = File(assetsDir, "duas_selected.json")
        assertTrue("File should exist", file.exists())
        val text = file.readText()
        val response = json.decodeFromString(DoaListResponse.serializer(), text)
        assertNotNull(response.data)
        assertFalse("Data should not be empty", response.data.isNullOrEmpty())
        val firstItem = response.data?.first()
        assertNotNull("Translation should be parsed", firstItem?.translation)
        assertTrue("Translation should not be blank", firstItem?.translation?.isNotBlank() == true)
    }

    @Test
    fun testDoaLocaleOverlayEnglish() {
        val overlay = DoaLocaleOverlay()
        val file = File(assetsDir, "duas_fadhilahdoa.json")
        val response = json.decodeFromString(DoaListResponse.serializer(), file.readText())
        val items = response.data.orEmpty()

        val localized = overlay.localizeDoas(items, app.kamy.saatApp.core.locale.AppLanguage.ENGLISH)
        val firstItem = localized.first()
        assertTrue("English translation should be used", firstItem.translation?.startsWith("There is nothing") == true)
    }

    @Test
    fun testDoaLocaleOverlayMalay() {
        val overlay = DoaLocaleOverlay()
        val file = File(assetsDir, "duas_fadhilahdoa.json")
        val response = json.decodeFromString(DoaListResponse.serializer(), file.readText())
        val items = response.data.orEmpty()

        val localized = overlay.localizeDoas(items, app.kamy.saatApp.core.locale.AppLanguage.MALAY)
        val firstItem = localized.first()
        assertTrue("Malay translation should be used", firstItem.translation?.startsWith("Tiada sesuatu") == true)
    }
}
