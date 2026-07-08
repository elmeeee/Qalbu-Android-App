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

enum class ShareTemplate(
    val id: String,
    val displayName: String,
    val bgColor: String,
    val borderColor: String,
    val innerBorderColor: String,
    val appNameColor: String,
    val arabicTextColor: String,
    val translationColor: String,
    val dividerColor: String,
    val referenceColor: String,
    val footerColor: String
) {
    IVORY_CREAM(
        id = "ivory_cream",
        displayName = "Ivory Cream",
        bgColor = "#FAF9F6",
        borderColor = "#DFD3C3",
        innerBorderColor = "#DFD3C3",
        appNameColor = "#1B4332",
        arabicTextColor = "#1F2937",
        translationColor = "#4B5563",
        dividerColor = "#E5E7EB",
        referenceColor = "#1B4332",
        footerColor = "#9CA3AF"
    ),
    DEEP_EMERALD(
        id = "deep_emerald",
        displayName = "Deep Emerald",
        bgColor = "#1B4332",
        borderColor = "#D8F3DC",
        innerBorderColor = "#52B788",
        appNameColor = "#D8F3DC",
        arabicTextColor = "#FFFFFF",
        translationColor = "#D8F3DC",
        dividerColor = "#52B788",
        referenceColor = "#D8F3DC",
        footerColor = "#95D5B2"
    ),
    MIDNIGHT(
        id = "midnight",
        displayName = "Midnight",
        bgColor = "#121212",
        borderColor = "#9E8050",
        innerBorderColor = "#2D3748",
        appNameColor = "#9E8050",
        arabicTextColor = "#FFFFFF",
        translationColor = "#E2E8F0",
        dividerColor = "#2D3748",
        referenceColor = "#9E8050",
        footerColor = "#A0AEC0"
    ),
    SOFT_TEAL(
        id = "soft_teal",
        displayName = "Soft Teal",
        bgColor = "#E8F1F2",
        borderColor = "#132E32",
        innerBorderColor = "#132E32",
        appNameColor = "#132E32",
        arabicTextColor = "#132E32",
        translationColor = "#374151",
        dividerColor = "#B2C7C9",
        referenceColor = "#132E32",
        footerColor = "#7A8B99"
    ),
    WARM_PEACH(
        id = "warm_peach",
        displayName = "Warm Peach",
        bgColor = "#FDF0ED",
        borderColor = "#D8A48F",
        innerBorderColor = "#EAD5C3",
        appNameColor = "#4A1521",
        arabicTextColor = "#4A1521",
        translationColor = "#374151",
        dividerColor = "#EAD5C3",
        referenceColor = "#4A1521",
        footerColor = "#B08A82"
    );
}

object AyahImageShare {

    fun shareAyahAsImage(
        context: Context,
        verse: RandomAyahPayload,
        surahName: String,
        template: ShareTemplate = ShareTemplate.IVORY_CREAM
    ) {
        val bitmap = renderToBitmap(context, verse, surahName, template)
        val file = saveToCache(context, bitmap)
        if (file != null) {
            shareFile(context, file)
        }
    }

    private fun renderToBitmap(
        context: Context,
        verse: RandomAyahPayload,
        surahName: String,
        template: ShareTemplate
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Draw background
        val bgPaint = Paint().apply {
            color = Color.parseColor(template.bgColor)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw elegant thin borders
        val borderPaint = Paint().apply {
            color = Color.parseColor(template.borderColor)
            style = Paint.Style.STROKE
            strokeWidth = 16f
        }
        canvas.drawRect(24f, 24f, width - 24f, height - 24f, borderPaint)

        val innerBorderPaint = Paint().apply {
            color = Color.parseColor(template.innerBorderColor)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(36f, 36f, width - 36f, height - 36f, innerBorderPaint)

        // Draw App Icon
        val iconDrawable = context.resources.getDrawable(R.drawable.splash_icon_adaptive, context.theme).mutate()
        iconDrawable.setTint(Color.parseColor(template.appNameColor))
        val iconSize = 96
        val iconX = (width - iconSize) / 2
        val iconY = 100
        iconDrawable.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
        iconDrawable.draw(canvas)

        // Draw App Name "SĀAT"
        val textPaint = TextPaint().apply {
            color = Color.parseColor(template.appNameColor)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("S Ā A T", (width / 2).toFloat(), (iconY + iconSize + 40).toFloat(), textPaint)

        // Arabic text setup
        val arabicText = verse.fullArabicForShare().trim()
        val arabicPaint = TextPaint().apply {
            color = Color.parseColor(template.arabicTextColor)
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
            color = Color.parseColor(template.translationColor)
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
            color = Color.parseColor(template.dividerColor)
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
            color = Color.parseColor(template.referenceColor)
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(refText, (width / 2).toFloat(), (height - 130).toFloat(), refPaint)

        // Footer note
        val footerPaint = TextPaint().apply {
            color = Color.parseColor(template.footerColor)
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
