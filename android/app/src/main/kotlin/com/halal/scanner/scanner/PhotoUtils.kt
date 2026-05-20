package com.halal.scanner.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface

/**
 * Hilfsfunktionen für Foto-Verarbeitung.
 */
object PhotoUtils {

    /**
     * Lädt ein Foto und rotiert es laut EXIF-Metadaten in die richtige Ausrichtung.
     * Nötig weil Smartphone-Kameras Fotos meist seitlich abspeichern und nur EXIF-Flag setzen.
     */
    fun decodeRotated(path: String): Bitmap? {
        val orig = BitmapFactory.decodeFile(path) ?: return null
        val rotation = try {
            val exif = ExifInterface(path)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (_: Exception) { 0f }
        if (rotation == 0f) return orig
        val m = Matrix().apply { postRotate(rotation) }
        return try {
            val rotated = Bitmap.createBitmap(orig, 0, 0, orig.width, orig.height, m, true)
            if (rotated != orig) orig.recycle()
            rotated
        } catch (_: OutOfMemoryError) {
            orig
        }
    }
}
