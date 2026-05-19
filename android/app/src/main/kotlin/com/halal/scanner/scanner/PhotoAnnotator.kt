package com.halal.scanner.scanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.mlkit.vision.text.Text
import com.halal.scanner.halal.HalalStatus
import com.halal.scanner.halal.IngredientDatabase
import java.io.File
import java.io.FileOutputStream

/**
 * Zeichnet Bounding-Boxes um die Wörter im Foto die haram (rot) oder mushbooh (orange) sind.
 * Speichert ein neues Bild und gibt dessen Pfad zurück.
 */
object PhotoAnnotator {

    fun annotate(originalFile: File, textResult: Text, outputFile: File): File? {
        return try {
            val opts = BitmapFactory.Options().apply { inMutable = true }
            val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, opts) ?: return null
            val canvas = Canvas(bitmap)

            val strokeWidth = (bitmap.width * 0.008f).coerceAtLeast(6f)

            val haramFillPaint = Paint().apply {
                color = 0x55E54B4B
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val haramStrokePaint = Paint().apply {
                color = 0xFFE54B4B.toInt()
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                isAntiAlias = true
            }
            val mushFillPaint = Paint().apply {
                color = 0x55E6A23C
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val mushStrokePaint = Paint().apply {
                color = 0xFFE6A23C.toInt()
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                isAntiAlias = true
            }

            val haramKws = IngredientDatabase.HARAM.flatMap { it.keywords.map { kw -> kw.lowercase().trim() } }
                .filter { it.length >= 3 }
                .distinct()
            val mushKws = IngredientDatabase.MUSHBOOH.flatMap { it.keywords.map { kw -> kw.lowercase().trim() } }
                .filter { it.length >= 3 }
                .distinct()

            for (block in textResult.textBlocks) {
                for (line in block.lines) {
                    val lineLower = line.text.lowercase()
                    val lineSeverity = severityFor(lineLower, haramKws, mushKws)
                    if (lineSeverity == HalalStatus.UNKNOWN) continue

                    // Falls Line-Match: alle Elemente der Line markieren
                    for (element in line.elements) {
                        val rect = element.boundingBox ?: continue
                        val rectF = RectF(rect)
                        when (lineSeverity) {
                            HalalStatus.HARAM -> {
                                canvas.drawRect(rectF, haramFillPaint)
                                canvas.drawRect(rectF, haramStrokePaint)
                            }
                            HalalStatus.MUSHBOOH -> {
                                canvas.drawRect(rectF, mushFillPaint)
                                canvas.drawRect(rectF, mushStrokePaint)
                            }
                            else -> {}
                        }
                    }
                }
            }

            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Wort-Grenzen-Matching damit "Hamburg" nicht als "ham" erkannt wird. */
    private fun severityFor(text: String, haramKws: List<String>, mushKws: List<String>): HalalStatus {
        for (kw in haramKws) {
            if (matchesWord(text, kw)) return HalalStatus.HARAM
        }
        for (kw in mushKws) {
            if (matchesWord(text, kw)) return HalalStatus.MUSHBOOH
        }
        return HalalStatus.UNKNOWN
    }

    private fun matchesWord(haystack: String, keyword: String): Boolean {
        val k = keyword.trim()
        if (k.isEmpty()) return false
        val pattern = "(?<![\\p{L}\\p{N}])" + Regex.escape(k) + "(?![\\p{L}\\p{N}])"
        return Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(haystack)
    }
}
