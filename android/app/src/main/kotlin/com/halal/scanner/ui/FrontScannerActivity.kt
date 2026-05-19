package com.halal.scanner.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.halal.scanner.R
import com.halal.scanner.data.OpenFoodFactsClient
import com.halal.scanner.data.Product
import com.halal.scanner.databinding.ActivityFrontScannerBinding
import kotlinx.coroutines.launch
import java.io.File

/**
 * Scannt die Vorderseite eines Produkts:
 * 1. Foto vom Produkt machen
 * 2. OCR extrahiert Produktname/Marke
 * 3. OpenFoodFacts-Volltextsuche -> Liste von Treffern
 * 4. User wählt richtigen Treffer -> ResultActivity mit Barcode
 */
class FrontScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFrontScannerBinding
    private lateinit var imageCapture: ImageCapture
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val offClient = OpenFoodFactsClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrontScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnCapture.setOnClickListener { takePhoto() }
        startCamera()
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().apply {
                surfaceProvider = binding.previewView.surfaceProvider
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            binding.btnCapture.isEnabled = true
            binding.btnCapture.alpha = 1f
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val ic = if (::imageCapture.isInitialized) imageCapture else return
        binding.btnCapture.isEnabled = false
        binding.btnCapture.alpha = 0.5f
        binding.txtStatus.visibility = View.VISIBLE
        binding.txtStatus.text = getString(R.string.ocr_capturing)
        binding.resultsContainer.visibility = View.GONE

        val photoFile = File(cacheDir, "front_${System.currentTimeMillis()}.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        ic.takePicture(options, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                analyzePhoto(photoFile)
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("FrontScanner", "capture failed", exception)
                binding.txtStatus.text = exception.message
                binding.btnCapture.isEnabled = true
                binding.btnCapture.alpha = 1f
            }
        })
    }

    private fun analyzePhoto(file: File) {
        val img = InputImage.fromFilePath(this, Uri.fromFile(file))
        recognizer.process(img)
            .addOnSuccessListener { result ->
                val text = result.text
                if (text.isBlank()) {
                    binding.txtStatus.text = getString(R.string.ocr_no_text)
                    binding.btnCapture.isEnabled = true
                    binding.btnCapture.alpha = 1f
                } else {
                    searchProducts(text)
                }
            }
            .addOnFailureListener { e ->
                binding.txtStatus.text = e.message
                binding.btnCapture.isEnabled = true
                binding.btnCapture.alpha = 1f
            }
    }

    private fun searchProducts(rawText: String) {
        // Reduziere OCR-Text zu sinnvollen Suchwörtern: nimm die längsten Wörter/Lines
        val candidates = rawText.lines()
            .map { it.trim() }
            .filter { it.length in 3..40 }
            .sortedByDescending { it.length }
            .take(3)
        val query = candidates.joinToString(" ").take(80).ifBlank { rawText.take(80) }

        binding.txtStatus.text = getString(R.string.front_searching, query)
        lifecycleScope.launch {
            val results = offClient.search(query, limit = 10)
            if (results.isEmpty()) {
                binding.txtStatus.text = getString(R.string.front_no_results, query)
                binding.btnCapture.isEnabled = true
                binding.btnCapture.alpha = 1f
            } else {
                showResults(results)
            }
        }
    }

    private fun showResults(products: List<Product>) {
        binding.txtStatus.visibility = View.GONE
        binding.resultsContainer.removeAllViews()
        binding.resultsContainer.visibility = View.VISIBLE
        binding.resultsScroll.visibility = View.VISIBLE
        for (p in products) {
            val card = LayoutInflater.from(this)
                .inflate(R.layout.item_front_result, binding.resultsContainer, false)
            val img = card.findViewById<android.widget.ImageView>(R.id.thumb)
            val name = card.findViewById<android.widget.TextView>(R.id.txtName)
            val brand = card.findViewById<android.widget.TextView>(R.id.txtBrand)
            name.text = p.name ?: "?"
            brand.text = listOfNotNull(p.brand, p.barcode.takeIf { it.isNotBlank() }).joinToString(" · ")
            if (!p.imageUrl.isNullOrBlank()) {
                img.visibility = View.VISIBLE
                img.load(p.imageUrl)
            } else {
                img.visibility = View.INVISIBLE
            }
            card.setOnClickListener {
                startActivity(
                    Intent(this, ResultActivity::class.java)
                        .putExtra(ResultActivity.EXTRA_BARCODE, p.barcode)
                )
                finish()
            }
            binding.resultsContainer.addView(card)
        }
        binding.btnCapture.text = getString(R.string.front_take_new)
        binding.btnCapture.isEnabled = true
        binding.btnCapture.alpha = 1f
    }
}
