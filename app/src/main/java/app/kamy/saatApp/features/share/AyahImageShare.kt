package app.kamy.saatApp.features.share

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.share.VerseShareTextComposer
import app.kamy.saatApp.domain.share.VerseShareTextComposer.Companion.fullArabicForShare
import java.io.File
import java.io.FileOutputStream

object AyahImageShare {

    fun shareAyahAsImage(context: Context, verse: RandomAyahPayload, surahName: String) {
        val bitmap = renderToBitmap(context, verse, surahName)
        val file = saveToCache(context, bitmap)
        if (file != null) {
            shareFile(context, file)
        }
    }

    private fun renderToBitmap(context: Context, verse: RandomAyahPayload, surahName: String): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background (solid minimal soft cream-white)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FAF9F6")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw elegant thin dark gold borders
        val borderPaint = Paint().apply {
            color = Color.parseColor("#DFD3C3")
            style = Paint.Style.STROKE
            strokeWidth = 16f
        }
        canvas.drawRect(24f, 24f, width - 24f, height - 24f, borderPaint)

        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#DFD3C3")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(36f, 36f, width - 36f, height - 36f, innerBorderPaint)

        // Draw App Icon
        val iconDrawable = context.resources.getDrawable(R.mipmap.ic_launcher, context.theme)
        val iconSize = 96
        val iconX = (width - iconSize) / 2
        val iconY = 100
        iconDrawable.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
        iconDrawable.draw(canvas)

        // Draw App Name "SĀAT"
        val textPaint = TextPaint().apply {
            color = Color.parseColor("#1B4332") // Deep emerald
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("S Ā A T", (width / 2).toFloat(), (iconY + iconSize + 40).toFloat(), textPaint)

        // Arabic text setup
        val arabicText = verse.fullArabicForShare().trim()
        val arabicPaint = TextPaint().apply {
            color = Color.parseColor("#1F2937") // Slate 800
            textSize = 48f
            typeface = Typeface.create("serif", Typeface.NORMAL)
            isAntiAlias = true
        }
        
        val contentWidth = width - 200
        val arabicLayout = StaticLayout.Builder.obtain(
            arabicText, 0, arabicText.length, arabicPaint, contentWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.5f)
            .build()

        // Translation setup
        val translationText = verse.translations?.firstOrNull()?.text?.trim().orEmpty()
            .replace(Regex("<[^>]*>"), "") // strip tags
        val transPaint = TextPaint().apply {
            color = Color.parseColor("#4B5563") // Slate 600
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        val transLayout = StaticLayout.Builder.obtain(
            translationText, 0, translationText.length, transPaint, contentWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.3f)
            .build()

        // Center content vertically
        val topReserved = 300
        val bottomReserved = 220
        val availableHeight = height - topReserved - bottomReserved
        
        val dividerHeight = 60
        val totalTextHeight = arabicLayout.height + dividerHeight + transLayout.height
        
        var startY = topReserved + (availableHeight - totalTextHeight) / 2
        if (startY < topReserved) startY = topReserved

        // Draw Arabic
        canvas.save()
        canvas.translate(100f, startY.toFloat())
        arabicLayout.draw(canvas)
        canvas.restore()

        // Draw subtle divider line
        val dividerY = startY + arabicLayout.height + (dividerHeight / 2)
        val divPaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 2f
        }
        canvas.drawLine(380f, dividerY.toFloat(), (width - 380).toFloat(), dividerY.toFloat(), divPaint)

        // Draw Translation
        canvas.save()
        canvas.translate(100f, (dividerY + (dividerHeight / 2)).toFloat())
        transLayout.draw(canvas)
        canvas.restore()

        // Draw Reference (Surah Name + Verse)
        val refText = "$surahName ${verse.resolvedVerseNumber ?: ""}"
        val refPaint = TextPaint().apply {
            color = Color.parseColor("#1B4332") // Deep emerald
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(refText, (width / 2).toFloat(), (height - 130).toFloat(), refPaint)

        // Footer note
        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#9CA3AF") // Slate 400
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Read & Reflect on SĀAT", (width / 2).toFloat(), (height - 80).toFloat(), footerPaint)

        return bitmap
    }

    private fun saveToCache(context: Context, bitmap: Bitmap): File? {
        return runCatching {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "ayah_share_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file
        }.getOrNull()
    }

    private fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, context.getString(R.string.share_verse_image_chooser))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
