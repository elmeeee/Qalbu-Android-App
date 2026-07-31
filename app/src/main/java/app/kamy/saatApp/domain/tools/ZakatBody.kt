package app.kamy.saatApp.domain.tools

import androidx.annotation.StringRes
import app.kamy.saatApp.R

enum class ZakatCountry(val emoji: String, @StringRes val labelRes: Int) {
    INDONESIA("🇮🇩", R.string.zakat_country_indonesia),
    MALAYSIA("🇲🇾", R.string.zakat_country_malaysia),
    SINGAPORE("🇸🇬", R.string.zakat_country_singapore),
    BRUNEI("🇧🇳", R.string.zakat_country_brunei)
}

data class ZakatBody(
    val name: String,
    val fullName: String,
    val websiteUrl: String,
    val country: ZakatCountry,
    /** For Malaysia: the state/territory name. Null for other countries. */
    val stateTag: String? = null
)

/**
 * Static repository of official zakat collection bodies.
 *
 * Sources:
 *  - Indonesia: BAZNAS, Dompet Dhuafa, LAZISMU, LAZISNU, Rumah Zakat, YDSF
 *  - Malaysia: JAWHAR (jawhar.gov.my) – official list of all state MAIN bodies
 *  - Singapore: MUIS (muis.gov.sg / zakat.sg)
 *  - Brunei: MUIB / JUZWAB (mora.gov.bn)
 */
object ZakatBodyRepository {

    val all: List<ZakatBody> = listOf(

        // ── Indonesia ────────────────────────────────────────────────────────────
        ZakatBody(
            name = "BAZNAS",
            fullName = "Badan Amil Zakat Nasional",
            websiteUrl = "https://baznas.go.id",
            country = ZakatCountry.INDONESIA
        ),
        ZakatBody(
            name = "Dompet Dhuafa",
            fullName = "Dompet Dhuafa Republika",
            websiteUrl = "https://www.dompetdhuafa.org",
            country = ZakatCountry.INDONESIA
        ),
        ZakatBody(
            name = "LAZISMU",
            fullName = "Lembaga Amil Zakat Infak Sedekah Muhammadiyah",
            websiteUrl = "https://lazismu.org",
            country = ZakatCountry.INDONESIA
        ),
        ZakatBody(
            name = "LAZISNU",
            fullName = "Lembaga Amil Zakat Infak Shodaqoh NU",
            websiteUrl = "https://nucare.id/zakat",
            country = ZakatCountry.INDONESIA
        ),
        ZakatBody(
            name = "Rumah Zakat",
            fullName = "Rumah Zakat Indonesia",
            websiteUrl = "https://www.rumahzakat.org",
            country = ZakatCountry.INDONESIA
        ),
        ZakatBody(
            name = "YDSF",
            fullName = "Yayasan Dana Sosial Al-Falah",
            websiteUrl = "https://ydsf.org",
            country = ZakatCountry.INDONESIA
        ),

        // ── Malaysia ─────────────────────────────────────────────────────────────
        // Administered by each state's Islamic Religious Council (Majlis Agama Islam Negeri)
        ZakatBody(
            name = "PPZ-MAIWP",
            fullName = "Pusat Pungutan Zakat – Majlis Agama Islam Wilayah Persekutuan",
            websiteUrl = "https://www.zakat.com.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "WP Kuala Lumpur / Putrajaya / Labuan"
        ),
        ZakatBody(
            name = "LZS",
            fullName = "Lembaga Zakat Selangor",
            websiteUrl = "https://www.zakatselangor.com.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Selangor"
        ),
        ZakatBody(
            name = "Zakat Pulau Pinang",
            fullName = "Majlis Agama Islam Negeri Pulau Pinang",
            websiteUrl = "https://zakat.mainpp.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Pulau Pinang"
        ),
        ZakatBody(
            name = "MAIPk",
            fullName = "Majlis Agama Islam dan Adat Melayu Perak",
            websiteUrl = "https://www.maipk.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Perak"
        ),
        ZakatBody(
            name = "MAINS",
            fullName = "Majlis Agama Islam Negeri Sembilan",
            websiteUrl = "https://www.mains.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Negeri Sembilan"
        ),
        ZakatBody(
            name = "MAIJ",
            fullName = "Majlis Agama Islam Negeri Johor",
            websiteUrl = "https://www.maij.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Johor"
        ),
        ZakatBody(
            name = "MAIM",
            fullName = "Majlis Agama Islam Melaka",
            websiteUrl = "https://www.maim.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Melaka"
        ),
        ZakatBody(
            name = "MUIP",
            fullName = "Majlis Ugama Islam dan Adat Resam Melayu Pahang",
            websiteUrl = "https://www.muip.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Pahang"
        ),
        ZakatBody(
            name = "MAIDAM",
            fullName = "Majlis Agama Islam dan Adat Melayu Terengganu",
            websiteUrl = "https://www.maidam.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Terengganu"
        ),
        ZakatBody(
            name = "MAIK",
            fullName = "Majlis Agama Islam dan Adat Istiadat Melayu Kelantan",
            websiteUrl = "https://www.e-maik.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Kelantan"
        ),
        ZakatBody(
            name = "LZNK",
            fullName = "Lembaga Zakat Negeri Kedah",
            websiteUrl = "https://www.zakatkedah.com.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Kedah"
        ),
        ZakatBody(
            name = "MAIPs",
            fullName = "Majlis Agama Islam dan Adat Istiadat Melayu Perlis",
            websiteUrl = "https://www.maips.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Perlis"
        ),
        ZakatBody(
            name = "MUIS Sabah",
            fullName = "Majlis Ugama Islam Sabah",
            websiteUrl = "https://muis.sabah.gov.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Sabah"
        ),
        ZakatBody(
            name = "TBS",
            fullName = "Tabung Baitulmal Sarawak",
            websiteUrl = "https://www.tbs.org.my",
            country = ZakatCountry.MALAYSIA,
            stateTag = "Sarawak"
        ),

        // ── Singapore ────────────────────────────────────────────────────────────
        ZakatBody(
            name = "MUIS",
            fullName = "Majlis Ugama Islam Singapura",
            websiteUrl = "https://www.muis.gov.sg",
            country = ZakatCountry.SINGAPORE
        ),
        ZakatBody(
            name = "Zakat.sg",
            fullName = "Portal Zakat Rasmi Singapura",
            websiteUrl = "https://www.zakat.sg",
            country = ZakatCountry.SINGAPORE
        ),

        // ── Brunei ───────────────────────────────────────────────────────────────
        ZakatBody(
            name = "MUIB",
            fullName = "Majlis Ugama Islam Brunei",
            websiteUrl = "https://www.muib.gov.bn",
            country = ZakatCountry.BRUNEI
        ),
        ZakatBody(
            name = "JUZWAB",
            fullName = "Jabatan Urusan Zakat, Waqaf dan Baitulmal",
            websiteUrl = "https://www.mora.gov.bn",
            country = ZakatCountry.BRUNEI
        )
    )

    fun byCountry(country: ZakatCountry): List<ZakatBody> =
        all.filter { it.country == country }
}
