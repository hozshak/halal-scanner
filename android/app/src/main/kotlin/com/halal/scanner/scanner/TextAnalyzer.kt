package com.halal.scanner.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * OCR-Analyzer für Zutatenlisten via ML Kit Text Recognition (Latin-Script).
 *
 * Liefert jeden erkannten Frame als String an onText. Lateinisches Script deckt
 * Deutsch, Englisch, Französisch, Spanisch, Italienisch, Türkisch ab - genau das
 * was auf europäischen Verpackungen steht.
 */
class TextAnalyzer(
    private val onText: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val img = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(img)
            .addOnSuccessListener { result ->
                if (result.text.isNotBlank()) onText(result.text)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
