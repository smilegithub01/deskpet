package com.deskpet.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import com.deskpet.app.data.model.OutfitCategory
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.PetState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoHelper {

    private const val IMAGE_SIZE = 1080

    fun captureAndSave(
        context: Context,
        petColor: PetColor,
        species: PetSpecies,
        petState: PetState,
        outfits: Map<OutfitCategory, String>,
        petName: String
    ): Uri? {
        val bitmap = renderBitmap(petColor, species, petState, outfits, petName)
        return saveBitmap(context, bitmap, petName)
    }

    private fun renderBitmap(
        petColor: PetColor,
        species: PetSpecies,
        petState: PetState,
        outfits: Map<OutfitCategory, String>,
        petName: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = IMAGE_SIZE.toFloat()
        val h = IMAGE_SIZE.toFloat()

        // Background gradient circle
        val bgPaint = Paint().apply {
            isAntiAlias = true
            shader = android.graphics.LinearGradient(
                w * 0.3f, h * 0.3f,
                w * 0.7f, h * 0.7f,
                intArrayOf(0xFFFFE0EC.toInt(), 0xFFE0F0FF.toInt()),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w / 2, h / 2, w * 0.42f, bgPaint)

        // Pet body
        drawPetOnCanvas(canvas, w, h, petColor, species, petState)

        // Watermark
        val watermarkPaint = Paint().apply {
            isAntiAlias = true
            color = 0x88000000.toInt()
            textSize = 36f
            textAlign = Paint.Align.LEFT
        }
        val dateStr = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        canvas.drawText("$petName  $dateStr", 30f, h - 30f, watermarkPaint)

        return bitmap
    }

    private fun drawPetOnCanvas(
        canvas: Canvas,
        w: Float,
        h: Float,
        petColor: PetColor,
        species: PetSpecies,
        petState: PetState
    ) {
        val bodyColor = android.graphics.Color.parseColor(petColor.hex)
        val bodyPaint = Paint().apply { isAntiAlias = true; color = bodyColor }

        // Body
        val bodyRect = RectF(w * 0.25f, h * 0.25f, w * 0.75f, h * 0.80f)
        canvas.drawRoundRect(bodyRect, w * 0.25f, w * 0.25f, bodyPaint)

        // Ears
        when (species) {
            PetSpecies.CAT -> {
                val leftEar = android.graphics.Path()
                leftEar.moveTo(w * 0.28f, h * 0.30f)
                leftEar.lineTo(w * 0.20f, h * 0.15f)
                leftEar.lineTo(w * 0.38f, h * 0.22f)
                leftEar.close()
                canvas.drawPath(leftEar, bodyPaint)
                val rightEar = android.graphics.Path()
                rightEar.moveTo(w * 0.72f, h * 0.30f)
                rightEar.lineTo(w * 0.80f, h * 0.15f)
                rightEar.lineTo(w * 0.62f, h * 0.22f)
                rightEar.close()
                canvas.drawPath(rightEar, bodyPaint)
            }
            PetSpecies.DOG -> {
                val darker = (bodyColor and 0xFF000000.toInt()) or
                    ((android.graphics.Color.red(bodyColor) * 0.82f).toInt() shl 16) or
                    ((android.graphics.Color.green(bodyColor) * 0.82f).toInt() shl 8) or
                    (android.graphics.Color.blue(bodyColor) * 0.82f).toInt()
                val earPaint = Paint().apply { isAntiAlias = true; color = darker }
                canvas.drawOval(RectF(w * 0.15f, h * 0.25f, w * 0.30f, h * 0.50f), earPaint)
                canvas.drawOval(RectF(w * 0.70f, h * 0.25f, w * 0.85f, h * 0.50f), earPaint)
            }
            PetSpecies.RABBIT -> {
                canvas.drawOval(RectF(w * 0.38f, h * 0.05f, w * 0.46f, h * 0.30f), bodyPaint)
                canvas.drawOval(RectF(w * 0.54f, h * 0.05f, w * 0.62f, h * 0.30f), bodyPaint)
            }
            PetSpecies.HAMSTER -> {
                canvas.drawCircle(w * 0.35f, h * 0.25f, w * 0.06f, bodyPaint)
                canvas.drawCircle(w * 0.65f, h * 0.25f, w * 0.06f, bodyPaint)
            }
        }

        // Eyes
        val eyeWhitePaint = Paint().apply { isAntiAlias = true; color = 0xFFFFFFFF.toInt() }
        val eyePupilPaint = Paint().apply { isAntiAlias = true; color = 0xFF2D2420.toInt() }
        val eyeY = h * 0.45f
        if (petState == PetState.SLEEPY) {
            val linePaint = Paint().apply { isAntiAlias = true; color = 0xFFFFFFFF.toInt(); strokeWidth = 6f }
            canvas.drawLine(w * 0.32f, eyeY, w * 0.40f, eyeY, linePaint)
            canvas.drawLine(w * 0.60f, eyeY, w * 0.68f, eyeY, linePaint)
        } else {
            canvas.drawCircle(w * 0.38f, eyeY, w * 0.04f, eyeWhitePaint)
            canvas.drawCircle(w * 0.62f, eyeY, w * 0.04f, eyeWhitePaint)
            canvas.drawCircle(w * 0.38f, eyeY, w * 0.02f, eyePupilPaint)
            canvas.drawCircle(w * 0.62f, eyeY, w * 0.02f, eyePupilPaint)
        }

        // Blush
        val blushPaint = Paint().apply { isAntiAlias = true; color = 0x66F4A7B9.toInt() }
        canvas.drawCircle(w * 0.30f, h * 0.55f, w * 0.05f, blushPaint)
        canvas.drawCircle(w * 0.70f, h * 0.55f, w * 0.05f, blushPaint)

        // Mouth
        val mouthPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val mouthPath = android.graphics.Path()
        when (petState) {
            PetState.HAPPY, PetState.EXCITED -> {
                mouthPath.moveTo(w * 0.44f, h * 0.62f)
                mouthPath.quadTo(w * 0.50f, h * 0.68f, w * 0.56f, h * 0.62f)
            }
            else -> {
                mouthPath.moveTo(w * 0.46f, h * 0.63f)
                mouthPath.lineTo(w * 0.54f, h * 0.63f)
            }
        }
        canvas.drawPath(mouthPath, mouthPaint)
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, petName: String): Uri? {
        val dir = File(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
            "deskpet_photos"
        )
        if (!dir.exists()) dir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "deskpet_${timestamp}.png")

        return runCatching {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }

    fun launchShare(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享合影"))
    }
}
