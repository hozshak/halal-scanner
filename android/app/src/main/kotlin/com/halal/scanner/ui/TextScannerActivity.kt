package com.halal.scanner.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.halal.scanner.R
import com.halal.scanner.databinding.ActivityTextScannerBinding
import com.halal.scanner.scanner.PhotoAnnotator
import com.halal.scanner.scanner.PhotoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * 2-Schritt-Workflow:
 *  1. Foto aufnehmen -> Vorschau
 *  2. Tap auf "Analysieren" -> async OCR + Annotation -> ResultActivity
 */
class TextScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextScannerBinding
    private lateinit var imageCapture: ImageCapture
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var capturedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnAnalyze.setOnClickListener { analyzeCaptured() }
        binding.btnRetake.setOnClickListener { backToCamera() }
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

        val photoFile = File(cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        ic.takePicture(options, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                capturedFile = photoFile
                showPreview(photoFile)
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("TextScanner", "capture failed", exception)
                binding.btnCapture.isEnabled = true
                binding.btnCapture.alpha = 1f
            }
        })
    }

    private fun showPreview(file: File) {
        val bmp = PhotoUtils.decodeRotated(file.absolutePath)
        binding.imgPreview.setImageBitmap(bmp)
        binding.resultPanel.visibility = View.VISIBLE
        binding.btnCapture.visibility = View.GONE
    }

    private fun backToCamera() {
        capturedFile = null
        binding.resultPanel.visibility = View.GONE
        binding.btnCapture.visibility = View.VISIBLE
        binding.btnCapture.isEnabled = true
        binding.btnCapture.alpha = 1f
    }

    private fun analyzeCaptured() {
        val file = capturedFile ?: return
        binding.btnAnalyze.isEnabled = false
        binding.btnRetake.isEnabled = false
        binding.progressOverlay.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // OCR auf Background-Thread
                val text = runOcrAsync(file)
                if (text.isBlank()) {
                    binding.progressOverlay.visibility = View.GONE
                    binding.txtStatus.visibility = View.VISIBLE
                    binding.txtStatus.text = getString(R.string.ocr_no_text)
                    binding.btnAnalyze.isEnabled = true
                    binding.btnRetake.isEnabled = true
                    return@launch
                }
                // Annotation auch async
                val annotatedPath = withContext(Dispatchers.IO) {
                    val img = InputImage.fromFilePath(this@TextScannerActivity, Uri.fromFile(file))
                    val result = awaitTask(recognizer.process(img))
                    val annotatedFile = File(file.parentFile, "annotated_${System.currentTimeMillis()}.jpg")
                    val out = PhotoAnnotator.annotate(file, result, annotatedFile)
                    out?.absolutePath ?: file.absolutePath
                }
                startActivity(
                    Intent(this@TextScannerActivity, ResultActivity::class.java)
                        .putExtra(ResultActivity.EXTRA_OCR_TEXT, text)
                        .putExtra(ResultActivity.EXTRA_OCR_PHOTO_PATH, annotatedPath)
                )
                finish()
            } catch (e: Exception) {
                Log.e("TextScanner", "analysis failed", e)
                binding.progressOverlay.visibility = View.GONE
                binding.txtStatus.visibility = View.VISIBLE
                binding.txtStatus.text = e.message
                binding.btnAnalyze.isEnabled = true
                binding.btnRetake.isEnabled = true
            }
        }
    }

    private suspend fun runOcrAsync(file: File): String = withContext(Dispatchers.IO) {
        val img = InputImage.fromFilePath(this@TextScannerActivity, Uri.fromFile(file))
        val result = awaitTask(recognizer.process(img))
        result.text
    }

    /** Brücke von Google Tasks API zu Kotlin Coroutines. */
    private suspend fun <T> awaitTask(task: com.google.android.gms.tasks.Task<T>): T =
        suspendCancellableCoroutine { cont ->
            task.addOnSuccessListener { if (cont.isActive) cont.resume(it) }
            task.addOnFailureListener { if (cont.isActive) cont.cancel(it) }
        }
}
