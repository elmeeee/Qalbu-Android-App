package app.kamy.saatApp.features.tools.faraidh

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.faraidh.BlockingReasonKey
import app.kamy.saatApp.domain.faraidh.DeceasedGender
import app.kamy.saatApp.domain.faraidh.FaraidhAdjustment
import app.kamy.saatApp.domain.faraidh.FaraidhGlossaryItem
import app.kamy.saatApp.domain.faraidh.FaraidhMadhhab
import app.kamy.saatApp.domain.faraidh.FaraidhNameLabels
import app.kamy.saatApp.domain.faraidh.FaraidhParticipantNames
import app.kamy.saatApp.domain.faraidh.FaraidhProofItem
import app.kamy.saatApp.domain.faraidh.FaraidhProofKind
import app.kamy.saatApp.domain.faraidh.FaraidhResult
import app.kamy.saatApp.domain.faraidh.HeirType
import app.kamy.saatApp.domain.faraidh.SilsilahNode
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FaraidhPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 44f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2

    fun export(
        context: Context,
        result: FaraidhResult,
        names: FaraidhParticipantNames,
        proofs: List<FaraidhProofItem>,
        glossary: List<FaraidhGlossaryItem>,
        language: AppLanguage
    ): Uri {
        val document = PdfDocument()
        var pageNumber = 1
        val paints = PdfPaints()
        val currency = currencyFormat(language)
        val locale = localeFor(language)

        var page = startPage(document, pageNumber++)
        var canvas = page.canvas
        var y = MARGIN

        fun ensure(needed: Float) {
            if (y + needed > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                page = startPage(document, pageNumber++)
                canvas = page.canvas
                y = MARGIN
            }
        }

        loadLogo(context)?.let { logo ->
            canvas.drawBitmap(Bitmap.createScaledBitmap(logo, 64, 64, true), MARGIN, y, null)
            y += 72f
        }

        y = drawLine(canvas, y, paints.title, "SĀAT — ${t(language, "Laporan Faraidh", "Laporan Faraidh", "Faraidh Report")}")
        y += 4f
        y = drawLine(canvas, y, paints.small, SimpleDateFormat("yyyy-MM-dd HH:mm", locale).format(Date()))
        y += 16f

        // --- Deceased ---
        y = drawLine(canvas, y, paints.section, t(language, "Data pewaris", "Data si mati", "Deceased profile"))
        y += 10f
        val deceasedName = names.deceasedName.ifBlank { t(language, "(Belum diisi)", "(Belum diisi)", "(Not provided)") }
        y = drawLine(canvas, y, paints.bodyBold, "${t(language, "Nama", "Nama", "Name")}: $deceasedName")
        y += 4f
        val genderLabel = when (result.deceased.gender) {
            DeceasedGender.MALE -> t(language, "Laki-laki", "Lelaki", "Male")
            DeceasedGender.FEMALE -> t(language, "Perempuan", "Perempuan", "Female")
        }
        y = drawLine(canvas, y, paints.body, "${t(language, "Jenis kelamin", "Jantina", "Gender")}: $genderLabel")
        y += 4f
        y = drawLine(canvas, y, paints.body, "${t(language, "Mazhab", "Mazhab", "Madhhab")}: ${madhhabLabel(result.madhhab, language)}")
        y += 4f
        y = drawLine(canvas, y, paints.body, "${t(language, "Harta bersih (tarikah)", "Harta bersih (tarikah)", "Net estate (tarikah)")}: ${currency.format(result.deceased.netEstate)}")
        y += 4f
        y = drawLine(canvas, y, paints.body, "${t(language, "Total terdistribusi", "Jumlah diagihkan", "Total distributed")}: ${currency.format(result.totalDistributed)}")
        y += 14f

        result.deceased.estate?.let { estate ->
            y = drawLine(canvas, y, paints.section, t(language, "Perhitungan harta (tarikah)", "Pengiraan harta (tarikah)", "Estate calculation (tarikah)"))
            y += 10f
            y = drawLine(canvas, y, paints.body, "${t(language, "Tunai & tabungan", "Tunai & simpanan", "Cash & savings")}: ${currency.format(estate.cashComponent)}")
            y += 3f
            y = drawLine(canvas, y, paints.body, "${t(language, "Emas & perhiasan", "Emas & barang kemas", "Gold & jewelry")}: ${currency.format(estate.goldComponent)}")
            y += 3f
            y = drawLine(canvas, y, paints.body, "${t(language, "Properti & tanah", "Hartanah & tanah", "Property & land")}: ${currency.format(estate.propertyComponent)}")
            y += 3f
            y = drawLine(canvas, y, paints.body, "${t(language, "Aset usaha", "Aset perniagaan", "Business assets")}: ${currency.format(estate.businessComponent)}")
            y += 3f
            y = drawLine(canvas, y, paints.body, "${t(language, "Aset lainnya", "Aset lain", "Other assets")}: ${currency.format(estate.otherComponent)}")
            y += 3f
            y = drawLine(canvas, y, paints.bodyBold, "${t(language, "Total aset kotor", "Jumlah aset kasar", "Gross assets")}: ${currency.format(estate.grossAssets)}")
            y += 6f
            if (estate.funeralCosts > BigDecimal.ZERO) {
                y = drawLine(canvas, y, paints.body, "− ${t(language, "Biaya jenazah", "Kos jenazah", "Funeral costs")}: ${currency.format(estate.funeralCosts)}")
                y += 3f
            }
            if (estate.debts > BigDecimal.ZERO) {
                y = drawLine(canvas, y, paints.body, "− ${t(language, "Hutang", "Hutang", "Debts")}: ${currency.format(estate.debts)}")
                y += 3f
            }
            if (estate.unpaidZakat > BigDecimal.ZERO) {
                y = drawLine(canvas, y, paints.body, "− ${t(language, "Zakat tertunda", "Zakat tertunggak", "Unpaid zakat")}: ${currency.format(estate.unpaidZakat)}")
                y += 3f
            }
            if (estate.wasiatApplied > BigDecimal.ZERO) {
                y = drawLine(canvas, y, paints.body, "− ${t(language, "Wasiat (maks ⅓)", "Wasiat (maks ⅓)", "Wasiat (max ⅓)")}: ${currency.format(estate.wasiatApplied)}")
                y += 3f
            }
            if (estate.hasResidentialProperty) {
                val note = estate.propertyNotes.ifBlank { t(language, "Rumah tinggal", "Rumah kediaman", "Residential house") }
                y = drawLine(canvas, y, paints.small, "${t(language, "Properti", "Hartanah", "Property")}: $note")
                y += 3f
            }
            y = drawLine(canvas, y, paints.bodyBold, "${t(language, "Tarikah untuk faraidh", "Tarikah untuk faraidh", "Tarikah for faraidh")}: ${currency.format(estate.netEstate)}")
            y += 12f
        }

        // --- Family register ---
        y = drawLine(canvas, y, paints.section, t(language, "Daftar keluarga", "Senarai keluarga", "Family register"))
        y += 10f
        y = drawFamilyRegister(canvas, y, paints, names, result, language, ::ensure)
        y += 12f

        // --- Adjustment ---
        if (result.adjustment != FaraidhAdjustment.NONE) {
            val (title, body) = when (result.adjustment) {
                FaraidhAdjustment.AWL -> t(language, "Penyesuaian Aul", "Pelarasan Aul", "ʿAwl adjustment") to
                    t(language, "Bagian furud melebihi harta — semua porsi diskalakan proporsional.", "Bahagian furud melebihi harta — semua bahagian diskalakan.", "Fixed shares exceeded estate — all portions scaled proportionally.")
                FaraidhAdjustment.RADD -> t(language, "Penyesuaian Radd", "Pelarasan Radd", "Radd adjustment") to
                    t(language, "Kelebihan harta dikembalikan ke waris nasab (pasangan dikecualikan).", "Lebihan harta dikembalikan kepada waris nasab.", "Surplus redistributed to blood heirs (spouses excluded).")
                FaraidhAdjustment.NONE -> "" to ""
            }
            y = drawLine(canvas, y, paints.bodyBold, title)
            y += 4f
            y = drawWrapped(canvas, y, paints.body, body, ::ensure)
            y += 12f
        }

        // --- Breakdown table ---
        y = drawLine(canvas, y, paints.section, t(language, "Rincian pembagian waris", "Perincian pembahagian", "Inheritance breakdown"))
        y += 10f
        y = drawTableHeader(canvas, y, paints, language)
        result.activeShares.forEach { share ->
            ensure(52f)
            val role = heirLabel(share.type, language)
            val persons = FaraidhNameLabels.displayList(share.type, role, names, share.headCount)
            val perHead = if (share.headCount > 1) {
                share.cashAmount.divide(BigDecimal(share.headCount), 2, RoundingMode.HALF_UP)
            } else null
            y = drawLine(canvas, y, paints.bodyBold, role)
            y += 2f
            persons.forEachIndexed { index, person ->
                ensure(14f)
                val shareLine = buildString {
                    append("  • $person")
                    append(" | ${share.fraction.toDisplayString()}")
                    append(" | ${formatPercent(share.percentage, language)}")
                    if (perHead != null && share.headCount > 1) {
                        append(" | ${currency.format(perHead)} ${t(language, "per orang", "setiap orang", "each")}")
                    }
                }
                y = drawLine(canvas, y, paints.small, shareLine)
            }
            ensure(16f)
            y = drawLine(canvas, y, paints.body, "  ${t(language, "Jumlah kelompok", "Jumlah kumpulan", "Group total")}: ${currency.format(share.cashAmount)} (${if (share.isAsabah) "Asabah" else "Furud"})")
            y += 8f
        }

        if (result.blockedHeirs.isNotEmpty()) {
            ensure(24f)
            y = drawLine(canvas, y, paints.section, t(language, "Ahli waris terhalang (hajb)", "Waris terhalang (hajb)", "Blocked heirs (hajb)"))
            y += 8f
            result.blockedHeirs.forEach { blocked ->
                ensure(14f)
                val reason = blockReason(blocked.reason, language)
                y = drawLine(canvas, y, paints.body, "• ${heirLabel(blocked.type, language)} ×${blocked.headCount} — $reason")
                y += 2f
            }
            y += 8f
        }

        // --- Silsilah summary ---
        ensure(24f)
        y = drawLine(canvas, y, paints.section, t(language, "Ringkasan silsilah", "Ringkasan silsilah", "Lineage summary"))
        y += 8f
        result.silsilah.forEach { node ->
            ensure(14f)
            val name = node.displayName.ifBlank { heirLabel(node.type, language) }
            val status = when {
                node.blocked -> t(language, "Terhalang", "Terhalang", "Blocked")
                node.inherits -> t(language, "Mewaris", "Mewarisi", "Inherits")
                else -> t(language, "Tidak mewaris", "Tidak mewarisi", "Not inheriting")
            }
            y = drawLine(canvas, y, paints.small, "• $name — $status")
        }
        y += 12f

        // --- Glossary ---
        ensure(28f)
        y = drawLine(canvas, y, paints.section, t(language, "Glosarium istilah faraidh", "Glosari istilah faraidh", "Faraidh glossary"))
        y += 10f
        glossary.forEach { term ->
            ensure(36f)
            y = drawLine(canvas, y, paints.bodyBold, term.title)
            y += 3f
            term.arabic?.let { arabic ->
                y = drawWrapped(canvas, y, paints.arabic, arabic, ::ensure)
                y += 2f
            }
            y = drawWrapped(canvas, y, paints.body, term.body, ::ensure)
            y += 8f
        }

        // --- Dalil ---
        ensure(28f)
        y = drawLine(canvas, y, paints.section, t(language, "Dalil & rujukan syariah", "Dalil & rujukan syariah", "Scriptural proofs"))
        y += 10f
        proofs.forEach { proof ->
            ensure(40f)
            y = drawLine(canvas, y, paints.bodyBold, "[${proof.kind.name}] ${proof.title}")
            y += 4f
            proof.arabic?.let { arabic ->
                y = drawWrapped(canvas, y, paints.arabic, arabic, ::ensure)
                y += 4f
            }
            y = drawWrapped(canvas, y, paints.body, proof.body, ::ensure)
            proof.externalUrl?.let { url ->
                ensure(12f)
                y = drawLine(canvas, y, paints.small, url)
            }
            y += 10f
        }

        ensure(20f)
        y = drawLine(canvas, y, paints.small, t(
            language,
            "Dokumen ini dihasilkan oleh SĀAT untuk tujuan edukasi. Konsultasikan ulama untuk keputusan hukum yang mengikat.",
            "Dokumen ini dijana oleh SĀAT untuk pendidikan. Rujuk ulama untuk keputusan mengikat.",
            "Generated by SĀAT for educational purposes. Consult a qualified scholar for binding rulings."
        ))

        document.finishPage(page)

        val dir = File(context.cacheDir, "faraidh_reports").apply { mkdirs() }
        val file = File(dir, "faraidh_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun drawFamilyRegister(
        canvas: android.graphics.Canvas,
        startY: Float,
        paints: PdfPaints,
        names: FaraidhParticipantNames,
        result: FaraidhResult,
        language: AppLanguage,
        ensure: (Float) -> Unit
    ): Float {
        var y = startY
        val entries = buildList {
            if (result.deceased.gender == DeceasedGender.FEMALE && result.input.husbandCount > 0) {
                add(heirLabel(HeirType.HUSBAND, language) to names.husbandName)
            }
            if (result.deceased.gender == DeceasedGender.MALE) {
                names.wifeNames.forEachIndexed { i, n ->
                    add("${heirLabel(HeirType.WIFE, language)} ${i + 1}" to n)
                }
            }
            if (result.input.fatherCount > 0) add(heirLabel(HeirType.FATHER, language) to names.fatherName)
            if (result.input.motherCount > 0) add(heirLabel(HeirType.MOTHER, language) to names.motherName)
            names.sonNames.forEachIndexed { i, n -> add("${heirLabel(HeirType.SON, language)} ${i + 1}" to n) }
            names.daughterNames.forEachIndexed { i, n -> add("${heirLabel(HeirType.DAUGHTER, language)} ${i + 1}" to n) }
            names.grandsonNames.forEachIndexed { i, n -> add("${heirLabel(HeirType.GRANDSON, language)} ${i + 1}" to n) }
            names.granddaughterNames.forEachIndexed { i, n -> add("${heirLabel(HeirType.GRANDDAUGHTER, language)} ${i + 1}" to n) }
            names.fullBrotherNames.forEachIndexed { i, n -> add("${heirLabel(HeirType.FULL_BROTHER, language)} ${i + 1}" to n) }
            names.fullSisterNames.forEachIndexed { i, n -> add("${heirLabel(HeirType.FULL_SISTER, language)} ${i + 1}" to n) }
            names.paternalBrotherNames.forEachIndexed { i, n -> add("${heirLabel(HeirType.PATERNAL_BROTHER, language)} ${i + 1}" to n) }
            names.paternalSisterNames.forEachIndexed { i, n -> add("${heirLabel(HeirType.PATERNAL_SISTER, language)} ${i + 1}" to n) }
            names.maternalBrotherNames.forEachIndexed { i, n -> add("${t(language, "Saudara seibu", "Saudara seibu", "Maternal brother")} ${i + 1}" to n) }
            names.maternalSisterNames.forEachIndexed { i, n -> add("${t(language, "Saudari seibu", "Saudari seibu", "Maternal sister")} ${i + 1}" to n) }
        }
        entries.forEach { (role, name) ->
            ensure(14f)
            val display = name.ifBlank { t(language, "(Belum diisi)", "(Belum diisi)", "(Not provided)") }
            y = drawLine(canvas, y, paints.body, "• $role: $display")
        }
        return y
    }

    private fun drawTableHeader(canvas: android.graphics.Canvas, y: Float, paints: PdfPaints, language: AppLanguage): Float {
        val header = "${t(language, "Waris", "Waris", "Heir")} | ${t(language, "Bagian", "Bahagian", "Share")} | % | ${t(language, "Nominal", "Amaun", "Amount")}"
        return drawLine(canvas, y, paints.small, header)
    }

    private fun startPage(document: PdfDocument, number: Int): PdfDocument.Page {
        val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()
        return document.startPage(info)
    }

    private fun drawLine(canvas: android.graphics.Canvas, y: Float, paint: Paint, text: String): Float {
        canvas.drawText(text, MARGIN, y, paint)
        return y + paint.textSize + 4f
    }

    private fun drawWrapped(
        canvas: android.graphics.Canvas,
        startY: Float,
        paint: Paint,
        text: String,
        ensure: (Float) -> Unit
    ): Float {
        var y = startY
        wrapText(text, paint, CONTENT_WIDTH.toInt()).forEach { line ->
            ensure(14f)
            y = drawLine(canvas, y, paint, line)
        }
        return y
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) <= maxWidth) {
                current = StringBuilder(test)
            } else {
                if (current.isNotEmpty()) lines += current.toString()
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines += current.toString()
        return lines
    }

    private fun formatPercent(value: BigDecimal, language: AppLanguage): String {
        val scaled = value.setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
        val sep = when (language) {
            AppLanguage.INDONESIAN, AppLanguage.MALAY -> ','
            AppLanguage.ENGLISH -> '.'
        }
        return "${scaled.toPlainString().replace('.', sep)}%"
    }

    private fun loadLogo(context: Context): Bitmap? =
        runCatching { BitmapFactory.decodeResource(context.resources, R.drawable.splash_screen_saat) }.getOrNull()

    private fun currencyFormat(language: AppLanguage) = NumberFormat.getNumberInstance(localeFor(language)).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    private fun localeFor(language: AppLanguage) = when (language) {
        AppLanguage.INDONESIAN -> Locale("id", "ID")
        AppLanguage.MALAY -> Locale("ms", "MY")
        AppLanguage.ENGLISH -> Locale.US
    }

    private fun t(language: AppLanguage, id: String, ms: String, en: String) = when (language) {
        AppLanguage.INDONESIAN -> id
        AppLanguage.MALAY -> ms
        AppLanguage.ENGLISH -> en
    }

    private fun blockReason(reason: BlockingReasonKey, language: AppLanguage): String = when (reason) {
        BlockingReasonKey.BY_SON -> t(language, "Dihijab anak laki-laki", "Dihalang anak lelaki", "Blocked by son")
        BlockingReasonKey.BY_CHILDREN -> t(language, "Dihijab anak", "Dihalang anak", "Blocked by children")
        BlockingReasonKey.BY_FATHER -> t(language, "Dihijab ayah", "Dihalang bapa", "Blocked by father")
        BlockingReasonKey.BY_GRANDCHILDREN_SUBSTITUTE -> t(language, "Digantikan waris lebih dekat", "Diganti waris lebih dekat", "Substituted by closer heirs")
        BlockingReasonKey.GENDER_MISMATCH -> t(language, "Tidak berlaku", "Tidak terpakai", "Not applicable")
        BlockingReasonKey.NO_SHARE_REMAINDER -> t(language, "Tidak ada sisa bagian", "Tiada baki bahagian", "No remaining share")
    }

    private fun madhhabLabel(madhhab: FaraidhMadhhab, language: AppLanguage): String = when (madhhab) {
        FaraidhMadhhab.HANAFI -> "Hanafi"
        FaraidhMadhhab.MALIKI -> "Maliki"
        FaraidhMadhhab.SHAFII -> when (language) {
            AppLanguage.INDONESIAN, AppLanguage.MALAY -> "Syafi'i"
            AppLanguage.ENGLISH -> "Shafi'i"
        }
        FaraidhMadhhab.HANBALI -> "Hanbali"
    }

    private fun heirLabel(type: HeirType, language: AppLanguage): String = when (type) {
        HeirType.HUSBAND -> t(language, "Suami", "Suami", "Husband")
        HeirType.WIFE -> t(language, "Istri", "Isteri", "Wife")
        HeirType.FATHER -> t(language, "Ayah", "Bapa", "Father")
        HeirType.MOTHER -> t(language, "Ibu", "Ibu", "Mother")
        HeirType.SON -> t(language, "Anak laki-laki", "Anak lelaki", "Son")
        HeirType.DAUGHTER -> t(language, "Anak perempuan", "Anak perempuan", "Daughter")
        HeirType.GRANDSON -> t(language, "Cucu laki-laki", "Cucu lelaki", "Grandson")
        HeirType.GRANDDAUGHTER -> t(language, "Cucu perempuan", "Cucu perempuan", "Granddaughter")
        HeirType.FULL_BROTHER -> t(language, "Saudara kandung", "Saudara kandung", "Full brother")
        HeirType.FULL_SISTER -> t(language, "Saudari kandung", "Saudari kandung", "Full sister")
        HeirType.PATERNAL_BROTHER -> t(language, "Saudara sebapak", "Saudara sebapa", "Paternal half-brother")
        HeirType.PATERNAL_SISTER -> t(language, "Saudari sebapak", "Saudari sebapa", "Paternal half-sister")
        HeirType.MATERNAL_SIBLING -> t(language, "Saudara seibu", "Saudara seibu", "Maternal sibling")
    }

    private class PdfPaints {
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = 0xFF064E3B.toInt()
        }
        val section = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = 0xFF1E293B.toInt()
        }
        val bodyBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); color = 0xFF334155.toInt()
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10.5f; color = 0xFF334155.toInt()
        }
        val small = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f; color = 0xFF64748B.toInt()
        }
        val arabic = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f; color = 0xFF0F172A.toInt(); textAlign = Paint.Align.RIGHT
        }
    }
}
