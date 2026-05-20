package com.halal.scanner.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object PhotoUtils {

    /**
     * Lädt ein Foto verkleinert auf max. `maxDim` Pixel + dreht laut EXIF.
     * Vermeidet OutOfMemory bei 12 MP Smartphone-Fotos und beschleunigt ML Kit.
     */
    fun decodeRotated(path: String, maxDim: Int = 2000): Bitmap? {
        // 1. Originale Größe ermitteln ohne Bitmap zu laden
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // 2. Sample-Size so wählen dass das Resultat <= maxDim ist
        var sample = 1
        val orig = maxOf(bounds.outWidth, bounds.outHeight)
        while (orig / sample > maxDim) sample *= 2

        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeFile(path, opts) ?: return null

        // 3. EXIF-Rotation anwenden
        val angle = readRotation(path)
        return if (angle == 0f) bitmap else rotate(bitmap, angle)
    }

    /**
     * Speichert ein vor-rotiertes Bitmap zurück auf Disk und überschreibt das Original.
     * So sieht ML Kit das Bild in korrekter Ausrichtung.
     */
    fun saveAsJpeg(bitmap: Bitmap, file: File, quality: Int = 85): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (_: Exception) { false }
    }

    private fun readRotation(path: String): Float {
        return try {
            val exif = ExifInterface(path)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (_: Exception) { 0f }
    }

    private fun rotate(src: Bitmap, angle: Float): Bitmap {
        val m = Matrix().apply { postRotate(angle) }
        return try {
            val rotated = Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
            if (rotated != src) src.recycle()
            rotated
        } catch (_: OutOfMemoryError) {
            src
        }
    }
}
