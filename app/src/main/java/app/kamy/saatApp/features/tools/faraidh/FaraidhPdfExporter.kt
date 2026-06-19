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
import app.kamy.saatApp.domain.faraidh.EstateAssetInput
import app.kamy.saatApp.domain.faraidh.EstateComputation
import app.kamy.saatApp.domain.faraidh.FaraidhEstateCalculator
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

    private class PdfExportContext(
        val document: PdfDocument,
        var pageNumber: Int,
        var page: PdfDocument.Page,
        var canvas: android.graphics.Canvas,
        var y: Float
    )

    private class EstateRow(
        val category: String,
        val detail: String,
        val amount: BigDecimal,
        val isDeduction: Boolean = false,
        val isHeader: Boolean = false,
        val isTotal: Boolean = false
    )

    fun export(
        context: Context,
        result: FaraidhResult,
        estateInput: EstateAssetInput,
        names: FaraidhParticipantNames,
        proofs: List<FaraidhProofItem>,
        glossary: List<FaraidhGlossaryItem>,
        language: AppLanguage
    ): Uri {
        val document = PdfDocument()
        val firstPage = startPage(document, 1)
        val ctx = PdfExportContext(
            document = document,
            pageNumber = 1,
            page = firstPage,
            canvas = firstPage.canvas,
            y = MARGIN
        )
        val paints = PdfPaints()
        val currency = currencyFormat(language)
        val locale = localeFor(language)

        loadLogo(context)?.let { logo ->
            ctx.canvas.drawBitmap(Bitmap.createScaledBitmap(logo, 64, 64, true), MARGIN, ctx.y, null)
            ctx.y += 72f
        }

        drawLine(ctx, paints.title, "SĀAT — ${t(language, "Laporan Faraidh", "Laporan Faraidh", "Faraidh Report")}")
        ctx.y += 4f
        drawLine(ctx, paints.small, SimpleDateFormat("yyyy-MM-dd HH:mm", locale).format(Date()))
        ctx.y += 16f

        // --- Deceased ---
        drawLine(ctx, paints.section, t(language, "Data pewaris", "Data si mati", "Deceased profile"))
        ctx.y += 10f
        val deceasedName = names.deceasedName.ifBlank { t(language, "(Belum diisi)", "(Belum diisi)", "(Not provided)") }
        drawLine(ctx, paints.bodyBold, "${t(language, "Nama", "Nama", "Name")}: $deceasedName")
        ctx.y += 4f
        val genderLabel = when (result.deceased.gender) {
            DeceasedGender.MALE -> t(language, "Laki-laki", "Lelaki", "Male")
            DeceasedGender.FEMALE -> t(language, "Perempuan", "Perempuan", "Female")
        }
        drawLine(ctx, paints.body, "${t(language, "Jenis kelamin", "Jantina", "Gender")}: $genderLabel")
        ctx.y += 4f
        drawLine(ctx, paints.body, "${t(language, "Mazhab", "Mazhab", "Madhhab")}: ${madhhabLabel(result.madhhab, language)}")
        ctx.y += 4f
        drawLine(ctx, paints.body, "${t(language, "Harta bersih (tarikah)", "Harta bersih (tarikah)", "Net estate (tarikah)")}: ${currency.format(result.deceased.netEstate.toDouble())}")
        ctx.y += 4f
        drawLine(ctx, paints.body, "${t(language, "Total terdistribusi", "Jumlah diagihkan", "Total distributed")}: ${currency.format(result.totalDistributed.toDouble())}")
        ctx.y += 14f

        // --- Estate table ---
        result.deceased.estate?.let { estate ->
            drawLine(ctx, paints.section, t(language, "Perhitungan harta (tarikah)", "Pengiraan harta (tarikah)", "Estate calculation (tarikah)"))
            ctx.y += 8f
            drawEstateTable(ctx, paints, estate, estateInput, currency, language)
            ctx.y += 8f
            if (estate.hasResidentialProperty) {
                ensureSpace(ctx, 14f)
                val note = estate.propertyNotes.ifBlank { t(language, "Rumah tinggal", "Rumah kediaman", "Residential house") }
                drawLine(ctx, paints.small, "${t(language, "Catatan kediaman", "Nota kediaman", "Residential note")}: $note")
                ctx.y += 6f
            }
            ctx.y += 12f
        }

        // --- Family register ---
        drawLine(ctx, paints.section, t(language, "Daftar keluarga", "Senarai keluarga", "Family register"))
        ctx.y += 10f
        drawFamilyRegister(ctx, paints, names, result, language)
        ctx.y += 12f

        // --- Adjustment ---
        if (result.adjustment != FaraidhAdjustment.NONE) {
            val (title, body) = when (result.adjustment) {
                FaraidhAdjustment.AWL -> t(language, "Penyesuaian Aul", "Pelarasan Aul", "ʿAwl adjustment") to
                    t(language, "Bagian furud melebihi harta — semua porsi diskalakan proporsional.", "Bahagian furud melebihi harta — semua bahagian diskalakan.", "Fixed shares exceeded estate — all portions scaled proportionally.")
                FaraidhAdjustment.RADD -> t(language, "Penyesuaian Radd", "Pelarasan Radd", "Radd adjustment") to
                    t(language, "Kelebihan harta dikembalikan ke waris nasab (pasangan dikecualikan).", "Lebihan harta dikembalikan kepada waris nasab.", "Surplus redistributed to blood heirs (spouses excluded).")
                FaraidhAdjustment.NONE -> "" to ""
            }
            drawLine(ctx, paints.bodyBold, title)
            ctx.y += 4f
            drawWrapped(ctx, paints.body, body)
            ctx.y += 12f
        }

        // --- Breakdown table ---
        drawLine(ctx, paints.section, t(language, "Rincian pembagian waris", "Perincian pembahagian", "Inheritance breakdown"))
        ctx.y += 8f
        drawInheritanceTable(ctx, paints, result, names, currency, language)
        ctx.y += 14f

        // --- Blocked Heirs ---
        if (result.blockedHeirs.isNotEmpty()) {
            ensureSpace(ctx, 24f)
            drawLine(ctx, paints.section, t(language, "Ahli waris terhalang (hajb)", "Waris terhalang (hajb)", "Blocked heirs (hajb)"))
            ctx.y += 8f
            result.blockedHeirs.forEach { blocked ->
                ensureSpace(ctx, 14f)
                val reason = blockReason(blocked.reason, language)
                drawLine(ctx, paints.body, "• ${heirLabel(blocked.type, language)} ×${blocked.headCount} — $reason")
                ctx.y += 2f
            }
            ctx.y += 8f
        }

        // --- Silsilah summary ---
        ensureSpace(ctx, 24f)
        drawLine(ctx, paints.section, t(language, "Ringkasan silsilah", "Ringkasan silsilah", "Lineage summary"))
        ctx.y += 8f
        result.silsilah.forEach { node ->
            ensureSpace(ctx, 14f)
            val name = node.displayName.ifBlank { heirLabel(node.type, language) }
            val status = when {
                node.blocked -> t(language, "Terhalang", "Terhalang", "Blocked")
                node.inherits -> t(language, "Mewaris", "Mewarisi", "Inherits")
                else -> t(language, "Tidak mewaris", "Tidak mewarisi", "Not inheriting")
            }
            drawLine(ctx, paints.small, "• $name — $status")
        }
        ctx.y += 12f

        // --- Glossary ---
        ensureSpace(ctx, 28f)
        drawLine(ctx, paints.section, t(language, "Glosarium istilah faraidh", "Glosari istilah faraidh", "Faraidh glossary"))
        ctx.y += 10f
        glossary.forEach { term ->
            ensureSpace(ctx, 36f)
            drawLine(ctx, paints.bodyBold, term.title)
            ctx.y += 3f
            term.arabic?.let { arabic ->
                drawWrapped(ctx, paints.arabic, arabic)
                ctx.y += 2f
            }
            drawWrapped(ctx, paints.body, term.body)
            ctx.y += 8f
        }

        // --- Dalil ---
        ensureSpace(ctx, 28f)
        drawLine(ctx, paints.section, t(language, "Dalil & rujukan syariah", "Dalil & rujukan syariah", "Scriptural proofs"))
        ctx.y += 10f
        proofs.forEach { proof ->
            ensureSpace(ctx, 40f)
            drawLine(ctx, paints.bodyBold, "[${proof.kind.name}] ${proof.title}")
            ctx.y += 4f
            proof.arabic?.let { arabic ->
                drawWrapped(ctx, paints.arabic, arabic)
                ctx.y += 4f
            }
            drawWrapped(ctx, paints.body, proof.body)
            proof.externalUrl?.let { url ->
                ensureSpace(ctx, 12f)
                drawLine(ctx, paints.small, url)
            }
            ctx.y += 10f
        }

        ensureSpace(ctx, 20f)
        drawLine(ctx, paints.small, t(
            language,
            "Dokumen ini dihasilkan oleh SĀAT untuk tujuan edukasi. Konsultasikan ulama untuk keputusan hukum yang mengikat.",
            "Dokumen ini dijana oleh SĀAT untuk pendidikan. Rujuk ulama untuk keputusan mengikat.",
            "Generated by SĀAT for educational purposes. Consult a qualified scholar for binding rulings."
        ))

        document.finishPage(ctx.page)

        val dir = File(context.cacheDir, "faraidh_reports").apply { mkdirs() }
        val file = File(dir, "faraidh_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun startPage(document: PdfDocument, number: Int): PdfDocument.Page {
        val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()
        return document.startPage(info)
    }

    private fun ensureSpace(ctx: PdfExportContext, needed: Float) {
        if (ctx.y + needed > PAGE_HEIGHT - MARGIN) {
            ctx.document.finishPage(ctx.page)
            ctx.pageNumber++
            ctx.page = startPage(ctx.document, ctx.pageNumber)
            ctx.canvas = ctx.page.canvas
            ctx.y = MARGIN
        }
    }

    private fun drawLine(ctx: PdfExportContext, paint: Paint, text: String) {
        ctx.canvas.drawText(text, MARGIN, ctx.y, paint)
        ctx.y += paint.textSize + 4f
    }

    private fun drawWrapped(
        ctx: PdfExportContext,
        paint: Paint,
        text: String
    ) {
        wrapText(text, paint, CONTENT_WIDTH.toInt()).forEach { line ->
            ensureSpace(ctx, 14f)
            drawLine(ctx, paint, line)
        }
    }

    private fun drawFamilyRegister(
        ctx: PdfExportContext,
        paints: PdfPaints,
        names: FaraidhParticipantNames,
        result: FaraidhResult,
        language: AppLanguage
    ) {
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
            ensureSpace(ctx, 14f)
            val display = name.ifBlank { t(language, "(Belum diisi)", "(Belum diisi)", "(Not provided)") }
            drawLine(ctx, paints.body, "• $role: $display")
        }
    }

    private fun drawEstateTable(
        ctx: PdfExportContext,
        paints: PdfPaints,
        estate: EstateComputation,
        estateInput: EstateAssetInput,
        currency: NumberFormat,
        language: AppLanguage
    ) {
        ensureSpace(ctx, 30f)

        val colWidths = floatArrayOf(200f, 187f, 120f)
        val colXs = FloatArray(3).apply {
            this[0] = MARGIN
            this[1] = MARGIN + colWidths[0]
            this[2] = MARGIN + colWidths[0] + colWidths[1]
        }

        val headerHeight = 24f
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF064E3B.toInt()
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCBD5E1.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        ctx.canvas.drawRect(MARGIN, ctx.y, PAGE_WIDTH - MARGIN, ctx.y + headerHeight, headerPaint)
        
        val headers = arrayOf(
            t(language, "Item / Kategori", "Item / Kategori", "Item / Category"),
            t(language, "Keterangan", "Butiran", "Details"),
            t(language, "Nilai", "Nilai", "Value")
        )
        for (i in 0..2) {
            ctx.canvas.drawText(headers[i], colXs[i] + 8f, ctx.y + 16f, headerTextPaint)
        }
        
        ctx.canvas.drawRect(MARGIN, ctx.y, PAGE_WIDTH - MARGIN, ctx.y + headerHeight, borderPaint)
        for (i in 1..2) {
            ctx.canvas.drawLine(colXs[i], ctx.y, colXs[i], ctx.y + headerHeight, borderPaint)
        }

        ctx.y += headerHeight

        val rows = mutableListOf<EstateRow>()
        
        // Cash component
        if (estate.cashComponent > BigDecimal.ZERO) {
            rows.add(
                EstateRow(
                    category = t(language, "Tunai & tabungan", "Tunai & simpanan", "Cash & savings"),
                    detail = "",
                    amount = estate.cashComponent
                )
            )
        }
        
        // Gold component
        if (estate.goldComponent > BigDecimal.ZERO) {
            val detailText = if (estateInput.inputGoldByGrams) {
                val weight = estateInput.goldWeightGrams.replace(',', '.')
                val price = FaraidhEstateCalculator.parseAmount(estateInput.goldPricePerGram)
                "$weight g × ${currency.format(price.toDouble())}"
            } else {
                ""
            }
            rows.add(
                EstateRow(
                    category = t(language, "Emas & perhiasan", "Emas & barang kemas", "Gold & jewelry"),
                    detail = detailText,
                    amount = estate.goldComponent
                )
            )
        }
        
        // Property component
        if (estate.propertyComponent > BigDecimal.ZERO) {
            if (estateInput.inputPropertyDetailed && estateInput.properties.isNotEmpty()) {
                estateInput.properties.forEach { item ->
                    val sizeDetail = if (item.sizeSqm.isNotBlank()) "${item.sizeSqm} m²" else ""
                    rows.add(
                        EstateRow(
                            category = "${t(language, "Properti", "Hartanah", "Property")}: ${item.name}",
                            detail = sizeDetail,
                            amount = FaraidhEstateCalculator.parseAmount(item.value)
                        )
                    )
                }
            } else {
                rows.add(
                    EstateRow(
                        category = t(language, "Properti & tanah", "Hartanah & tanah", "Property & land"),
                        detail = estate.propertyNotes,
                        amount = estate.propertyComponent
                    )
                )
            }
        }
        
        // Business component
        if (estate.businessComponent > BigDecimal.ZERO) {
            rows.add(
                EstateRow(
                    category = t(language, "Aset usaha", "Aset perniagaan", "Business assets"),
                    detail = "",
                    amount = estate.businessComponent
                )
            )
        }
        
        // Other component
        if (estate.otherComponent > BigDecimal.ZERO) {
            rows.add(
                EstateRow(
                    category = t(language, "Aset lainnya", "Aset lain", "Other assets"),
                    detail = "",
                    amount = estate.otherComponent
                )
            )
        }
        
        // Subtotal: Gross Assets
        rows.add(
            EstateRow(
                category = t(language, "Total Aset Kotor", "Jumlah Aset Kasar", "Gross Assets"),
                detail = "",
                amount = estate.grossAssets,
                isTotal = true
            )
        )
        
        // Deductions
        if (estate.funeralCosts > BigDecimal.ZERO) {
            rows.add(
                EstateRow(
                    category = t(language, "Biaya jenazah", "Kos jenazah", "Funeral costs"),
                    detail = "",
                    amount = estate.funeralCosts,
                    isDeduction = true
                )
            )
        }
        if (estate.debts > BigDecimal.ZERO) {
            rows.add(
                EstateRow(
                    category = t(language, "Hutang", "Hutang", "Debts"),
                    detail = "",
                    amount = estate.debts,
                    isDeduction = true
                )
            )
        }
        if (estate.unpaidZakat > BigDecimal.ZERO) {
            rows.add(
                EstateRow(
                    category = t(language, "Zakat tertunda", "Zakat tertunggak", "Unpaid zakat"),
                    detail = "",
                    amount = estate.unpaidZakat,
                    isDeduction = true
                )
            )
        }
        if (estate.wasiatApplied > BigDecimal.ZERO) {
            val wasiatDetail = if (FaraidhEstateCalculator.parseAmount(estateInput.bequestWasiat) > estate.maxWasiat) {
                t(language, "Dibatasi maks ⅓", "Dihadkan maks ⅓", "Capped at max ⅓")
            } else {
                ""
            }
            rows.add(
                EstateRow(
                    category = t(language, "Wasiat", "Wasiat", "Wasiat/Bequest"),
                    detail = wasiatDetail,
                    amount = estate.wasiatApplied,
                    isDeduction = true
                )
            )
        }
        
        // Final Total: Net Estate
        rows.add(
            EstateRow(
                category = t(language, "Tarikah Bersih (untuk Faraidh)", "Tarikah Bersih (untuk Faraidh)", "Net Estate (for Faraidh)"),
                detail = "",
                amount = estate.netEstate,
                isTotal = true
            )
        )

        val rowHeight = 22f
        val bgPaintEven = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        val bgPaintOdd = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF8FAFC.toInt()
            style = Paint.Style.FILL
        }
        val bgPaintGross = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF1F5F9.toInt() // light grey
            style = Paint.Style.FILL
        }
        val bgPaintNet = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFECFDF5.toInt() // light green
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF334155.toInt()
            textSize = 9f
        }
        val textPaintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E293B.toInt()
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaintRed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF991B1B.toInt() // dark red
            textSize = 9f
        }

        rows.forEachIndexed { index, row ->
            ensureSpace(ctx, rowHeight)
            val bgPaint = when {
                row.isTotal && row.category.contains("Tarikah") -> bgPaintNet
                row.isTotal -> bgPaintGross
                index % 2 == 0 -> bgPaintEven
                else -> bgPaintOdd
            }
            ctx.canvas.drawRect(MARGIN, ctx.y, PAGE_WIDTH - MARGIN, ctx.y + rowHeight, bgPaint)
            
            // Draw category
            val catPaint = if (row.isTotal) textPaintBold else textPaint
            ctx.canvas.drawText(row.category, colXs[0] + 8f, ctx.y + 15f, catPaint)
            
            // Draw details
            ctx.canvas.drawText(row.detail, colXs[1] + 8f, ctx.y + 15f, textPaint)
            
            // Draw value
            val prefix = if (row.isDeduction) "− " else ""
            val valStr = "$prefix${currency.format(row.amount.toDouble())}"
            val valPaint = when {
                row.isDeduction -> textPaintRed
                row.isTotal -> textPaintBold
                else -> textPaint
            }
            ctx.canvas.drawText(valStr, colXs[2] + 8f, ctx.y + 15f, valPaint)
            
            // Draw borders
            ctx.canvas.drawRect(MARGIN, ctx.y, PAGE_WIDTH - MARGIN, ctx.y + rowHeight, borderPaint)
            for (i in 1..2) {
                ctx.canvas.drawLine(colXs[i], ctx.y, colXs[i], ctx.y + rowHeight, borderPaint)
            }
            ctx.y += rowHeight
        }
    }

    private fun drawInheritanceTable(
        ctx: PdfExportContext,
        paints: PdfPaints,
        result: FaraidhResult,
        names: FaraidhParticipantNames,
        currency: NumberFormat,
        language: AppLanguage
    ) {
        ensureSpace(ctx, 30f)

        val colWidths = floatArrayOf(197f, 80f, 80f, 150f)
        val colXs = FloatArray(4).apply {
            this[0] = MARGIN
            this[1] = MARGIN + colWidths[0]
            this[2] = MARGIN + colWidths[0] + colWidths[1]
            this[3] = MARGIN + colWidths[0] + colWidths[1] + colWidths[2]
        }

        val headerHeight = 24f
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF064E3B.toInt()
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCBD5E1.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        ctx.canvas.drawRect(MARGIN, ctx.y, PAGE_WIDTH - MARGIN, ctx.y + headerHeight, headerPaint)
        
        val headers = arrayOf(
            t(language, "Ahli Waris", "Waris", "Heir"),
            t(language, "Bagian", "Bahagian", "Share"),
            t(language, "Persen", "Peratus", "Percentage"),
            t(language, "Nilai Waris", "Amaun", "Amount")
        )
        for (i in 0..3) {
            ctx.canvas.drawText(headers[i], colXs[i] + 8f, ctx.y + 16f, headerTextPaint)
        }
        
        ctx.canvas.drawRect(MARGIN, ctx.y, PAGE_WIDTH - MARGIN, ctx.y + headerHeight, borderPaint)
        for (i in 1..3) {
            ctx.canvas.drawLine(colXs[i], ctx.y, colXs[i], ctx.y + headerHeight, borderPaint)
        }

        ctx.y += headerHeight

        val rowHeight = 22f
        val bgPaintEven = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        val bgPaintOdd = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF8FAFC.toInt()
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF334155.toInt()
            textSize = 9f
        }
        val textPaintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E293B.toInt()
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var rowIndex = 0
        result.activeShares.forEach { share ->
            val role = heirLabel(share.type, language)
            val persons = FaraidhNameLabels.displayList(share.type, role, names, share.headCount)
            val indivFrac = share.fraction.divideAmongHeads(share.headCount)
            val indivPercent = share.percentage.divide(BigDecimal(share.headCount), 1, RoundingMode.HALF_UP)
            val indivCash = share.cashAmount.divide(BigDecimal(share.headCount), 2, RoundingMode.HALF_UP)

            persons.forEach { person ->
                ensureSpace(ctx, rowHeight)
                
                val bgPaint = if (rowIndex % 2 == 0) bgPaintEven else bgPaintOdd
                ctx.canvas.drawRect(MARGIN, ctx.y, PAGE_WIDTH - MARGIN, ctx.y + rowHeight, bgPaint)
                
                ctx.canvas.drawText(person, colXs[0] + 8f, ctx.y + 15f, textPaintBold)
                ctx.canvas.drawText(indivFrac.toDisplayString(), colXs[1] + 8f, ctx.y + 15f, textPaint)
                ctx.canvas.drawText(formatPercent(indivPercent, language), colXs[2] + 8f, ctx.y + 15f, textPaint)
                ctx.canvas.drawText(currency.format(indivCash.toDouble()), colXs[3] + 8f, ctx.y + 15f, textPaint)
                
                ctx.canvas.drawRect(MARGIN, ctx.y, PAGE_WIDTH - MARGIN, ctx.y + rowHeight, borderPaint)
                for (i in 1..3) {
                    ctx.canvas.drawLine(colXs[i], ctx.y, colXs[i], ctx.y + rowHeight, borderPaint)
                }

                ctx.y += rowHeight
                rowIndex++
            }
        }
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
        BlockingReasonKey.OUT_OF_WEDLOCK -> t(language, "Tidak ada nasab bapak", "Tiada nasab bapa", "No paternal lineage (born out of wedlock)")
        BlockingReasonKey.HOMICIDE -> t(language, "Terhalang: Pembunuhan (pembunuh)", "Terhalang: Pembunuhan (pembunuh)", "Excluded: Homicide (killer)")
        BlockingReasonKey.DIFFERENCE_OF_RELIGION -> t(language, "Terhalang: Perbedaan agama", "Terhalang: Perbezaan agama", "Excluded: Difference of religion")
        BlockingReasonKey.SIMULTANEOUS_DEATH -> t(language, "Terhalang: Kematian serentak", "Terhalang: Kematian serentak", "Excluded: Simultaneous death")
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
        HeirType.STEP_CHILD -> t(language, "Anak tiri", "Anak tiri", "Step-child")
        HeirType.UNBORN_FETUS -> t(language, "Janin dalam kandungan", "Janin dalam kandungan", "Unborn Fetus (Al-Janin)")
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
