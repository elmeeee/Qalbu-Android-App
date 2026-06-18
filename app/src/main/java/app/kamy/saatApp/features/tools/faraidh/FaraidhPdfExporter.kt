package app.kamy.saatApp.features.tools.faraidh

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.faraidh.FaraidhProofItem
import app.kamy.saatApp.domain.faraidh.FaraidhProofKind
import app.kamy.saatApp.domain.faraidh.FaraidhResult
import app.kamy.saatApp.domain.faraidh.HeirType
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FaraidhPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f

    fun export(
        context: Context,
        result: FaraidhResult,
        proofs: List<FaraidhProofItem>,
        language: AppLanguage
    ): Uri {
        val document = PdfDocument()
        var pageNumber = 1
        var y = MARGIN

        fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber++).create()
            return document.startPage(pageInfo)
        }

        var page = newPage()
        var canvas = page.canvas
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = 0xFF064E3B.toInt()
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = 0xFF1E293B.toInt()
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color = 0xFF334155.toInt()
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            color = 0xFF64748B.toInt()
        }

        val logo = loadLogo(context)
        if (logo != null) {
            val logoSize = 72
            canvas.drawBitmap(Bitmap.createScaledBitmap(logo, logoSize, logoSize, true), MARGIN, y, null)
            y += logoSize + 12f
        }

        canvas.drawText("SĀAT — ${localizedTitle(language)}", MARGIN, y, titlePaint)
        y += 22f
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(timestamp, MARGIN, y, smallPaint)
        y += 28f

        val currency = NumberFormat.getNumberInstance(
            when (language) {
                AppLanguage.INDONESIAN -> Locale("id", "ID")
                AppLanguage.MALAY -> Locale("ms", "MY")
                AppLanguage.ENGLISH -> Locale.US
            }
        ).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }

        canvas.drawText(
            "${localizedEstateLabel(language)}: ${currency.format(result.deceased.netEstate)}",
            MARGIN, y, headerPaint
        )
        y += 20f
        canvas.drawText(
            "${localizedDistributedLabel(language)}: ${currency.format(result.totalDistributed)}",
            MARGIN, y, bodyPaint
        )
        y += 28f

        canvas.drawText(localizedBreakdownHeader(language), MARGIN, y, headerPaint)
        y += 18f

        fun ensureSpace(needed: Float) {
            if (y + needed > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                page = newPage()
                canvas = page.canvas
                y = MARGIN
            }
        }

        result.activeShares.forEach { share ->
            ensureSpace(36f)
            val line = "${heirLabel(share.type, language)} ×${share.headCount}  |  " +
                "${share.fraction.toDisplayString()}  |  ${share.percentage}%  |  ${currency.format(share.cashAmount)}"
            canvas.drawText(line, MARGIN, y, bodyPaint)
            y += 16f
        }

        if (result.blockedHeirs.isNotEmpty()) {
            ensureSpace(28f)
            y += 8f
            canvas.drawText(localizedBlockedHeader(language), MARGIN, y, headerPaint)
            y += 18f
            result.blockedHeirs.forEach { blocked ->
                ensureSpace(20f)
                canvas.drawText(
                    "• ${heirLabel(blocked.type, language)} ×${blocked.headCount}",
                    MARGIN, y, smallPaint
                )
                y += 14f
            }
        }

        ensureSpace(40f)
        y += 16f
        canvas.drawText(localizedProofsHeader(language), MARGIN, y, headerPaint)
        y += 18f

        proofs.forEach { proof ->
            ensureSpace(48f)
            canvas.drawText(proof.title, MARGIN, y, bodyPaint.apply { typeface = Typeface.DEFAULT_BOLD })
            y += 14f
            wrapText(proof.body, bodyPaint, PAGE_WIDTH - (MARGIN * 2).toInt()).forEach { line ->
                ensureSpace(14f)
                canvas.drawText(line, MARGIN, y, bodyPaint)
                y += 12f
            }
            proof.externalUrl?.let { url ->
                ensureSpace(14f)
                canvas.drawText(url, MARGIN, y, smallPaint)
                y += 12f
            }
            y += 6f
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "faraidh_reports").apply { mkdirs() }
        val file = File(dir, "faraidh_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun loadLogo(context: Context): Bitmap? =
        runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.splash_screen_saat)
        }.getOrNull()

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

    private fun localizedTitle(language: AppLanguage) = when (language) {
        AppLanguage.INDONESIAN -> "Laporan Faraidh"
        AppLanguage.MALAY -> "Laporan Faraidh"
        AppLanguage.ENGLISH -> "Faraidh Report"
    }

    private fun localizedEstateLabel(language: AppLanguage) = when (language) {
        AppLanguage.INDONESIAN -> "Harta bersih"
        AppLanguage.MALAY -> "Harta bersih"
        AppLanguage.ENGLISH -> "Net estate"
    }

    private fun localizedDistributedLabel(language: AppLanguage) = when (language) {
        AppLanguage.INDONESIAN -> "Total terdistribusi"
        AppLanguage.MALAY -> "Jumlah diagihkan"
        AppLanguage.ENGLISH -> "Total distributed"
    }

    private fun localizedBreakdownHeader(language: AppLanguage) = when (language) {
        AppLanguage.INDONESIAN -> "Rincian ahli waris"
        AppLanguage.MALAY -> "Perincian waris"
        AppLanguage.ENGLISH -> "Heir breakdown"
    }

    private fun localizedBlockedHeader(language: AppLanguage) = when (language) {
        AppLanguage.INDONESIAN -> "Ahli waris terhalang (hajb)"
        AppLanguage.MALAY -> "Waris terhalang (hajb)"
        AppLanguage.ENGLISH -> "Blocked heirs (hajb)"
    }

    private fun localizedProofsHeader(language: AppLanguage) = when (language) {
        AppLanguage.INDONESIAN -> "Dalil & rujukan"
        AppLanguage.MALAY -> "Dalil & rujukan"
        AppLanguage.ENGLISH -> "Scriptural proofs"
    }

    private fun heirLabel(type: HeirType, language: AppLanguage): String = when (type) {
        HeirType.HUSBAND -> tr(language, "Suami", "Suami", "Husband")
        HeirType.WIFE -> tr(language, "Istri", "Isteri", "Wife")
        HeirType.FATHER -> tr(language, "Ayah", "Bapa", "Father")
        HeirType.MOTHER -> tr(language, "Ibu", "Ibu", "Mother")
        HeirType.SON -> tr(language, "Anak laki-laki", "Anak lelaki", "Son")
        HeirType.DAUGHTER -> tr(language, "Anak perempuan", "Anak perempuan", "Daughter")
        HeirType.GRANDSON -> tr(language, "Cucu laki-laki", "Cucu lelaki", "Grandson")
        HeirType.GRANDDAUGHTER -> tr(language, "Cucu perempuan", "Cucu perempuan", "Granddaughter")
        HeirType.FULL_BROTHER -> tr(language, "Saudara kandung", "Saudara kandung", "Full brother")
        HeirType.FULL_SISTER -> tr(language, "Saudari kandung", "Saudari kandung", "Full sister")
        HeirType.PATERNAL_BROTHER -> tr(language, "Saudara sebapak", "Saudara sebapa", "Paternal half-brother")
        HeirType.PATERNAL_SISTER -> tr(language, "Saudari sebapak", "Saudari sebapa", "Paternal half-sister")
        HeirType.MATERNAL_SIBLING -> tr(language, "Saudara seibu", "Saudara seibu", "Maternal sibling")
    }

    private fun tr(language: AppLanguage, id: String, ms: String, en: String) = when (language) {
        AppLanguage.INDONESIAN -> id
        AppLanguage.MALAY -> ms
        AppLanguage.ENGLISH -> en
    }
}
