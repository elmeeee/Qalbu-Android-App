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
    val bgColorStart: String,
    val bgColorEnd: String,
    val hasGradient: Boolean,
    val borderColor: String,
    val innerBorderColor: String,
    val logoBackdropColor: String,
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
        bgColorStart = "#FCFBF7",
        bgColorEnd = "#FAF9F6",
        hasGradient = false,
        borderColor = "#C5A880",
        innerBorderColor = "#D5BE9C",
        logoBackdropColor = "#F3EFE0",
        appNameColor = "#8C6239",
        arabicTextColor = "#1F2937",
        translationColor = "#4B5563",
        dividerColor = "#D5BE9C",
        referenceColor = "#8C6239",
        footerColor = "#9CA3AF"
    ),
    DEEP_EMERALD(
        id = "deep_emerald",
        displayName = "Deep Emerald",
        bgColorStart = "#0C231B",
        bgColorEnd = "#1B4332",
        hasGradient = true,
        borderColor = "#D4AF37",
        innerBorderColor = "#A38A33",
        logoBackdropColor = "#2D5A46",
        appNameColor = "#D4AF37",
        arabicTextColor = "#FFFFFF",
        translationColor = "#E8F5E9",
        dividerColor = "#A38A33",
        referenceColor = "#D4AF37",
        footerColor = "#95D5B2"
    ),
    MIDNIGHT_DUSK(
        id = "midnight",
        displayName = "Midnight Dusk",
        bgColorStart = "#0B0C10",
        bgColorEnd = "#1F2833",
        hasGradient = true,
        borderColor = "#45A29E",
        innerBorderColor = "#233342",
        logoBackdropColor = "#15222E",
        appNameColor = "#66FCF1",
        arabicTextColor = "#FFFFFF",
        translationColor = "#E0F7FA",
        dividerColor = "#45A29E",
        referenceColor = "#66FCF1",
        footerColor = "#A0AEC0"
    ),
    ROYAL_GOLD(
        id = "royal_gold",
        displayName = "Royal Gold",
        bgColorStart = "#121212",
        bgColorEnd = "#2C2318",
        hasGradient = true,
        borderColor = "#E5A93B",
        innerBorderColor = "#A0782D",
        logoBackdropColor = "#3E3628",
        appNameColor = "#E5A93B",
        arabicTextColor = "#F9D976",
        translationColor = "#E6DFD3",
        dividerColor = "#A0782D",
        referenceColor = "#E5A93B",
        footerColor = "#8C8C8C"
    ),
    WARM_ROSE(
        id = "warm_rose",
        displayName = "Warm Rose",
        bgColorStart = "#FDF3F2",
        bgColorEnd = "#F5DFDC",
        hasGradient = true,
        borderColor = "#C38380",
        innerBorderColor = "#E5C1C0",
        logoBackdropColor = "#FCE8E6",
        appNameColor = "#7A3B3E",
        arabicTextColor = "#3D0C11",
        translationColor = "#4A5568",
        dividerColor = "#E5C1C0",
        referenceColor = "#7A3B3E",
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

        // Draw background (solid or linear gradient)
        if (template.hasGradient) {
            val gradPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor(template.bgColorStart),
                    Color.parseColor(template.bgColorEnd),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradPaint)
        } else {
            val bgPaint = Paint().apply {
                color = Color.parseColor(template.bgColorStart)
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        }

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

        // Draw elegant corner ornaments (small filled dots inside corners)
        val ornamentPaint = Paint().apply {
            color = Color.parseColor(template.borderColor)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(48f, 48f, 10f, ornamentPaint)
        canvas.drawCircle(width - 48f, 48f, 10f, ornamentPaint)
        canvas.drawCircle(48f, height - 48f, 10f, ornamentPaint)
        canvas.drawCircle(width - 48f, height - 48f, 10f, ornamentPaint)

        // Draw circular shield under App Icon to make it pop
        val logoBackdropPaint = Paint().apply {
            color = Color.parseColor(template.logoBackdropColor)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val iconSize = 96
        val iconX = (width - iconSize) / 2
        val iconY = 100
        val circleCenterX = (width / 2).toFloat()
        val circleCenterY = (iconY + iconSize / 2).toFloat()
        val circleRadius = (iconSize / 2 + 16).toFloat()
        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, logoBackdropPaint)

        // Draw elegant thin ring around the shield
        val ringPaint = Paint().apply {
            color = Color.parseColor(template.borderColor)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, ringPaint)

        // Draw App Icon
        val iconDrawable = context.resources.getDrawable(R.drawable.splash_icon_display, context.theme).mutate()
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
        canvas.drawText("S Ā A T", (width / 2).toFloat(), (iconY + iconSize + 48).toFloat(), textPaint)

        // Arabic text & translation setup
        val arabicText = verse.fullArabicForShare().trim()
        val translationText = verse.translations?.firstOrNull()?.text?.trim().orEmpty()
            .replace(Regex("<[^>]*>"), "") // strip tags

        // Auto-fit font size calculation loop
        var currentArabicSize = 52f
        var currentTranslationSize = 32f
        val contentWidth = width - 200
        val dividerHeight = 60
        val topReserved = 320
        val bottomReserved = 220
        val availableHeight = height - topReserved - bottomReserved

        var arabicPaint = TextPaint().apply {
            color = Color.parseColor(template.arabicTextColor)
            textSize = currentArabicSize
            typeface = Typeface.create("serif", Typeface.BOLD)
            isAntiAlias = true
        }
        var transPaint = TextPaint().apply {
            color = Color.parseColor(template.translationColor)
            textSize = currentTranslationSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        var arabicLayout = StaticLayout.Builder.obtain(arabicText, 0, arabicText.length, arabicPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.6f)
            .build()
        var transLayout = StaticLayout.Builder.obtain(translationText, 0, translationText.length, transPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.35f)
            .build()

        // Loop to fit content inside the available vertical height
        var loopCount = 0
        while (arabicLayout.height + dividerHeight + transLayout.height > availableHeight && currentArabicSize > 26f && loopCount < 10) {
            currentArabicSize -= 3f
            currentTranslationSize -= 1.5f

            arabicPaint = TextPaint().apply {
                color = Color.parseColor(template.arabicTextColor)
                textSize = currentArabicSize
                typeface = Typeface.create("serif", Typeface.BOLD)
                isAntiAlias = true
            }
            transPaint = TextPaint().apply {
                color = Color.parseColor(template.translationColor)
                textSize = currentTranslationSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }

            arabicLayout = StaticLayout.Builder.obtain(arabicText, 0, arabicText.length, arabicPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.6f)
                .build()
            transLayout = StaticLayout.Builder.obtain(translationText, 0, translationText.length, transPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.35f)
                .build()

            loopCount++
        }

        // Center content vertically based on final auto-fitted heights
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
            strokeWidth = 2.5f
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

        // Footer note (uses share_brand_tagline resource without markdown formatting)
        val taglineRaw = context.getString(R.string.share_brand_tagline)
        val tagline = taglineRaw.replace("_", "").trim()
        val footerPaint = TextPaint().apply {
            color = Color.parseColor(template.footerColor)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(tagline, (width / 2).toFloat(), (height - 80).toFloat(), footerPaint)

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
