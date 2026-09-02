package app.kamy.saatApp.features.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.RandomAyahPayload
import app.kamy.saatApp.domain.share.VerseShareTextComposer.Companion.fullArabicForShare
import java.io.File
import java.io.FileOutputStream

enum class ShareTemplate(
    val id: String,
    val displayName: String,
    val bgDrawableRes: Int,
    val arabicTextColor: String,
    val translationColor: String,
    val ornamentColor: String,
    val referenceColor: String,
    val hashtagColor: String
) {
    TEMPLATE_1(
        id = "bg_share_1",
        displayName = "Template 1",
        bgDrawableRes = R.drawable.bg_share_1,
        arabicTextColor = "#1B4332",
        translationColor = "#2C3E50",
        ornamentColor = "#B89758",
        referenceColor = "#1B4332",
        hashtagColor = "#7A6239"
    ),
    TEMPLATE_2(
        id = "bg_share_2",
        displayName = "Template 2",
        bgDrawableRes = R.drawable.bg_share_2,
        arabicTextColor = "#FFFFFF",
        translationColor = "#FFFFFF",
        ornamentColor = "#F5D77F",
        referenceColor = "#FFFFFF",
        hashtagColor = "#E2E8F0"
    ),
    TEMPLATE_3(
        id = "bg_share_3",
        displayName = "Template 3",
        bgDrawableRes = R.drawable.bg_share_3,
        arabicTextColor = "#FFFFFF",
        translationColor = "#FFFFFF",
        ornamentColor = "#F5D77F",
        referenceColor = "#FFFFFF",
        hashtagColor = "#E2E8F0"
    )
}

object AyahImageShare {

    fun shareAyahAsImage(
        context: Context,
        verse: RandomAyahPayload,
        surahName: String,
        template: ShareTemplate = ShareTemplate.TEMPLATE_1
    ) {
        val bitmap = renderToBitmap(context, verse, surahName, template)
        val file = saveToCache(context, bitmap)
        if (file != null) {
            shareFile(context, file)
        }
    }

    fun renderToBitmap(
        context: Context,
        verse: RandomAyahPayload,
        surahName: String,
        template: ShareTemplate
    ): Bitmap {
        // Load background image
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val rawBg = BitmapFactory.decodeResource(context.resources, template.bgDrawableRes, options)
        val width = rawBg?.width ?: 941
        val height = rawBg?.height ?: 1672

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (rawBg != null) {
            val bgPaint = Paint().apply {
                isFilterBitmap = true
                isAntiAlias = true
            }
            canvas.drawBitmap(rawBg, 0f, 0f, bgPaint)
        } else {
            canvas.drawColor(Color.parseColor("#FAF6EC"))
        }

        val centerX = width / 2f

        // Fonts
        val lpmqTypeface = runCatching {
            ResourcesCompat.getFont(context, R.font.lpmq)
        }.getOrNull() ?: Typeface.create("serif", Typeface.BOLD)

        val serifRegular = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        val serifMedium = Typeface.create(Typeface.SERIF, Typeface.NORMAL)

        // Texts
        val arabicText = verse.fullArabicForShare().trim()
        val rawTranslation = verse.translations?.firstOrNull()?.text?.trim().orEmpty()
            .replace(Regex("<[^>]*>"), "")
        val translationText = if (rawTranslation.isNotBlank()) "“$rawTranslation”" else ""

        val chapterNum = verse.chapterId ?: verse.verseKey?.substringBefore(':')?.toIntOrNull()
        val verseNum = verse.resolvedVerseNumber ?: ""
        val referenceText = if (chapterNum != null) {
            "QS. $surahName ($chapterNum) : $verseNum"
        } else {
            "QS. $surahName : $verseNum"
        }
        val hashtagText = context.getString(R.string.share_image_hashtag).trim()

        val isVeryLongAyah = arabicText.length > 200 || translationText.length > 280
        val isMediumAyah = arabicText.length > 90 || translationText.length > 140

        // Content horizontal width (expands slightly for longer verses to make optimal use of arch)
        val contentWidthFactor = when {
            isVeryLongAyah -> 0.78f
            isMediumAyah -> 0.75f
            else -> 0.72f
        }
        val contentWidth = (width * contentWidthFactor).toInt()
        val contentLeft = (width - contentWidth) / 2f

        // Vertical space inside arch
        val archTopY = if (isVeryLongAyah) height * 0.155f else height * 0.175f
        val archBottomY = if (isVeryLongAyah) height * 0.680f else height * 0.655f
        val availableHeight = archBottomY - archTopY

        // Auto-sizing calculation
        var currentArabicSize = when {
            isVeryLongAyah -> 34f
            isMediumAyah -> 40f
            else -> 46f
        }
        var currentTranslationSize = when {
            isVeryLongAyah -> 22f
            isMediumAyah -> 25f
            else -> 28f
        }
        var arabicLineSpacing = if (isVeryLongAyah) 1.35f else 1.55f
        var transLineSpacing = if (isVeryLongAyah) 1.25f else 1.35f
        var sectionSpacing = if (isVeryLongAyah) 14f else if (isMediumAyah) 20f else 26f

        val topOrnamentHeight = if (isVeryLongAyah) 24f else 34f
        val midOrnamentHeight = if (isVeryLongAyah) 20f else 28f
        val refHeight = 32f
        val hashtagHeight = 24f

        var arabicPaint = TextPaint().apply {
            color = Color.parseColor(template.arabicTextColor)
            textSize = currentArabicSize
            typeface = lpmqTypeface
            isAntiAlias = true
        }

        var transPaint = TextPaint().apply {
            color = Color.parseColor(template.translationColor)
            textSize = currentTranslationSize
            typeface = serifRegular
            isAntiAlias = true
        }

        var arabicLayout = StaticLayout.Builder.obtain(arabicText, 0, arabicText.length, arabicPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, arabicLineSpacing)
            .build()

        var transLayout = StaticLayout.Builder.obtain(translationText, 0, translationText.length, transPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, transLineSpacing)
            .build()

        var loopCount = 0
        val totalFixedOverhead = topOrnamentHeight + midOrnamentHeight + refHeight + hashtagHeight + (sectionSpacing * 4)
        while (arabicLayout.height + transLayout.height + totalFixedOverhead > availableHeight && currentArabicSize > 18f && loopCount < 16) {
            currentArabicSize -= 1.5f
            currentTranslationSize -= 1.0f
            sectionSpacing = (sectionSpacing - 1.0f).coerceAtLeast(8f)

            arabicPaint = TextPaint().apply {
                color = Color.parseColor(template.arabicTextColor)
                textSize = currentArabicSize
                typeface = lpmqTypeface
                isAntiAlias = true
            }
            transPaint = TextPaint().apply {
                color = Color.parseColor(template.translationColor)
                textSize = currentTranslationSize
                typeface = serifRegular
                isAntiAlias = true
            }

            arabicLayout = StaticLayout.Builder.obtain(arabicText, 0, arabicText.length, arabicPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, arabicLineSpacing)
                .build()

            transLayout = StaticLayout.Builder.obtain(translationText, 0, translationText.length, transPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, transLineSpacing)
                .build()

            loopCount++
        }

        val totalContentHeight = totalFixedOverhead + arabicLayout.height + transLayout.height
        var currentY = archTopY + (availableHeight - totalContentHeight) / 2f
        if (currentY < archTopY) currentY = archTopY

        // 1. Top Decorative Flourish Ornament (Golden Diamond & Flanking Lines)
        val ornamentPaint = Paint().apply {
            color = Color.parseColor(template.ornamentColor)
            isAntiAlias = true
            strokeWidth = 2f
        }
        val topOrnamentCenterY = currentY + (topOrnamentHeight / 2f)
        val lineWidth = if (isVeryLongAyah) 60f else 85f
        val diamondRadius = if (isVeryLongAyah) 7f else 9f
        // Left line
        canvas.drawLine(centerX - lineWidth - diamondRadius, topOrnamentCenterY, centerX - diamondRadius - 5f, topOrnamentCenterY, ornamentPaint)
        // Right line
        canvas.drawLine(centerX + diamondRadius + 5f, topOrnamentCenterY, centerX + lineWidth + diamondRadius, topOrnamentCenterY, ornamentPaint)
        // Center diamond
        val diamondPath = Path().apply {
            moveTo(centerX, topOrnamentCenterY - diamondRadius)
            lineTo(centerX + diamondRadius, topOrnamentCenterY)
            lineTo(centerX, topOrnamentCenterY + diamondRadius)
            lineTo(centerX - diamondRadius, topOrnamentCenterY)
            close()
        }
        canvas.drawPath(diamondPath, ornamentPaint)

        currentY += topOrnamentHeight + sectionSpacing

        // 2. Arabic Text
        canvas.save()
        canvas.translate(contentLeft, currentY)
        arabicLayout.draw(canvas)
        canvas.restore()

        currentY += arabicLayout.height + sectionSpacing

        // 3. Translation Text (in double quotes)
        if (translationText.isNotBlank()) {
            canvas.save()
            canvas.translate(contentLeft, currentY)
            transLayout.draw(canvas)
            canvas.restore()

            currentY += transLayout.height + sectionSpacing
        }

        // 4. Middle Ornament (Rosette / Diamond symbol)
        val midOrnamentCenterY = currentY + (midOrnamentHeight / 2f)
        val midLineWidth = if (isVeryLongAyah) 35f else 50f
        val midDiamondRadius = if (isVeryLongAyah) 5f else 6f
        canvas.drawLine(centerX - midLineWidth - midDiamondRadius, midOrnamentCenterY, centerX - midDiamondRadius - 4f, midOrnamentCenterY, ornamentPaint)
        canvas.drawLine(centerX + midDiamondRadius + 4f, midOrnamentCenterY, centerX + midLineWidth + midDiamondRadius, midOrnamentCenterY, ornamentPaint)
        val midDiamondPath = Path().apply {
            moveTo(centerX, midOrnamentCenterY - midDiamondRadius)
            lineTo(centerX + midDiamondRadius, midOrnamentCenterY)
            lineTo(centerX, midOrnamentCenterY + midDiamondRadius)
            lineTo(centerX - midDiamondRadius, midOrnamentCenterY)
            close()
        }
        canvas.drawPath(midDiamondPath, ornamentPaint)

        currentY += midOrnamentHeight + (sectionSpacing * 0.7f)

        // 5. Surah & Verse Reference Text (e.g. QS. Al-Insyirah (94) : 6)
        val refSize = if (isVeryLongAyah) 24f else 28f
        val refPaint = TextPaint().apply {
            color = Color.parseColor(template.referenceColor)
            textSize = refSize
            typeface = serifMedium
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(referenceText, centerX, currentY + 22f, refPaint)

        currentY += refHeight + 6f

        // 6. Footer Hashtag (e.g. #SaatNyaDekatDenganQuran / #SaatDekatDenganQuran / #TimeForQuran)
        if (hashtagText.isNotBlank()) {
            val hashtagPaint = TextPaint().apply {
                color = Color.parseColor(template.hashtagColor)
                textSize = if (isVeryLongAyah) 18f else 20f
                typeface = serifRegular
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.05f
                isAntiAlias = true
            }
            canvas.drawText(hashtagText, centerX, currentY + 16f, hashtagPaint)
        }

        return bitmap
    }

    private fun saveToCache(context: Context, bitmap: Bitmap): File? {
        return runCatching {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "ayah_share_${System.currentTimeMillis()}.webp")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 95, out)
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
            type = "image/webp"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, context.getString(R.string.share_verse_image_chooser))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
