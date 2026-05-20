package com.halal.scanner.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.halal.scanner.R
import com.halal.scanner.data.OpenFoodFactsClient
import com.halal.scanner.data.Product
import com.halal.scanner.databinding.ActivityFrontScannerBinding
import com.halal.scanner.scanner.PhotoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * 2-Schritt-Workflow für Vorderseite:
 *  1. Foto -> Vorschau zum prüfen
 *  2. "Produkt suchen" -> OCR + OFF-Volltextsuche -> Liste
 *  Falls keine OFF-Treffer: Fallback-Button "Mit Google Bildersuche öffnen"
 */
class FrontScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFrontScannerBinding
    private lateinit var imageCapture: ImageCapture
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val offClient = OpenFoodFactsClient()
    private var capturedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrontScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnSearchProduct.setOnClickListener { startSearch() }
        binding.btnRetake.setOnClickListener { backToCamera() }
        binding.btnSearchImage.setOnClickListener { shareForImageSearch() }
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

        val photoFile = File(cacheDir, "front_${System.currentTimeMillis()}.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        ic.takePicture(options, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                lifecycleScope.launch {
                    val processed = withContext(Dispatchers.IO) {
                        val bmp = PhotoUtils.decodeRotated(photoFile.absolutePath)
                            ?: return@withContext photoFile
                        PhotoUtils.saveAsJpeg(bmp, photoFile)
                        bmp.recycle()
                        photoFile
                    }
                    capturedFile = processed
                    showPreview(processed)
                }
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("FrontScanner", "capture failed", exception)
                binding.btnCapture.isEnabled = true
                binding.btnCapture.alpha = 1f
            }
        })
    }

    private fun showPreview(file: File) {
        binding.imgPreview.load(file)
        binding.resultPanel.visibility = View.VISIBLE
        binding.btnCapture.visibility = View.GONE
        binding.resultsContainer.removeAllViews()
        binding.resultsScroll.visibility = View.GONE
        binding.txtStatus.visibility = View.GONE
        binding.btnSearchProduct.visibility = View.VISIBLE
        binding.btnSearchImage.visibility = View.GONE
    }

    private fun backToCamera() {
        capturedFile = null
        binding.resultPanel.visibility = View.GONE
        binding.btnCapture.visibility = View.VISIBLE
        binding.btnCapture.isEnabled = true
        binding.btnCapture.alpha = 1f
    }

    private fun startSearch() {
        val file = capturedFile ?: return
        binding.btnSearchProduct.isEnabled = false
        binding.btnRetake.isEnabled = false
        binding.progressOverlay.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val text = ocrAsync(file)
                val query = buildQuery(text)
                if (query.isBlank()) {
                    showNoResults("(kein Text erkannt)")
                    return@launch
                }
                val results = offClient.search(query, limit = 12)
                binding.progressOverlay.visibility = View.GONE
                binding.btnSearchProduct.isEnabled = true
                binding.btnRetake.isEnabled = true
                if (results.isEmpty()) {
                    showNoResults(query)
                } else {
                    showResults(results)
                }
            } catch (e: Exception) {
                Log.e("FrontScanner", "search failed", e)
                binding.progressOverlay.visibility = View.GONE
                binding.btnSearchProduct.isEnabled = true
                binding.btnRetake.isEnabled = true
                showNoResults(e.message ?: "")
            }
        }
    }

    private fun showNoResults(query: String) {
        binding.txtStatus.visibility = View.VISIBLE
        binding.txtStatus.text = getString(R.string.front_no_results, query)
        binding.btnSearchImage.visibility = View.VISIBLE
        binding.resultsScroll.visibility = View.GONE
    }

    private fun showResults(products: List<Product>) {
        binding.txtStatus.visibility = View.GONE
        binding.btnSearchImage.visibility = View.GONE
        binding.resultsContainer.removeAllViews()
        binding.resultsScroll.visibility = View.VISIBLE
        for (p in products) {
            val card = LayoutInflater.from(this)
                .inflate(R.layout.item_front_result, binding.resultsContainer, false)
            val img = card.findViewById<ImageView>(R.id.thumb)
            val name = card.findViewById<TextView>(R.id.txtName)
            val brand = card.findViewById<TextView>(R.id.txtBrand)
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
    }

    private fun buildQuery(rawText: String): String {
        val candidates = rawText.lines()
            .map { it.trim() }
            .filter { it.length in 3..40 }
            .sortedByDescending { it.length }
            .take(3)
        return candidates.joinToString(" ").take(80).ifBlank { rawText.take(80) }
    }

    private suspend fun ocrAsync(file: File): String = withContext(Dispatchers.IO) {
        val img = InputImage.fromFilePath(this@FrontScannerActivity, Uri.fromFile(file))
        val task = recognizer.process(img)
        suspendCancellableCoroutine<String> { cont ->
            task.addOnSuccessListener { if (cont.isActive) cont.resume(it.text) }
            task.addOnFailureListener { if (cont.isActive) cont.cancel(it) }
        }
    }

    private fun shareForImageSearch() {
        val file = capturedFile ?: return
        try {
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            // Versuche Google-App direkt (Google Lens) ohne Chooser
            val googleApps = listOf(
                "com.google.android.googlequicksearchbox",  // Google-App / Lens
                "com.google.ar.lens",                       // Lens-App
                "com.google.android.apps.photos",           // Fotos-App (kann Lens)
            )
            for (pkg in googleApps) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage(pkg)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                    return
                }
            }
            // Fallback: Chooser, falls keine bekannte Google-App installiert
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(fallback, getString(R.string.front_share_chooser_title)))
        } catch (e: Exception) {
            Log.e("FrontScanner", "share failed", e)
        }
    }
}
