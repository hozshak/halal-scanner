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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.halal.scanner.databinding.ActivityTextScannerBinding
import java.io.File

class TextScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextScannerBinding
    private lateinit var imageCapture: ImageCapture
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextScannerBinding.inflate(layoutInflater)
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
            provider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
            )
            binding.btnCapture.isEnabled = true
            binding.btnCapture.alpha = 1f
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val ic = if (::imageCapture.isInitialized) imageCapture else return
        binding.btnCapture.isEnabled = false
        binding.btnCapture.alpha = 0.5f
        binding.txtStatus.visibility = View.VISIBLE
        binding.txtStatus.text = getString(com.halal.scanner.R.string.ocr_capturing)

        val photoFile = File(cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        ic.takePicture(options, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                analyzePhoto(photoFile)
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("TextScanner", "capture failed", exception)
                binding.txtStatus.text = exception.message
                binding.btnCapture.isEnabled = true
                binding.btnCapture.alpha = 1f
            }
        })
    }

    private fun analyzePhoto(file: File) {
        try {
            val img = InputImage.fromFilePath(this, Uri.fromFile(file))
            recognizer.process(img)
                .addOnSuccessListener { result ->
                    val text = result.text
                    if (text.isBlank()) {
                        binding.txtStatus.text = getString(com.halal.scanner.R.string.ocr_no_text)
                        binding.btnCapture.isEnabled = true
                        binding.btnCapture.alpha = 1f
                    } else {
                        startActivity(
                            Intent(this, ResultActivity::class.java)
                                .putExtra(ResultActivity.EXTRA_OCR_TEXT, text)
                        )
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    binding.txtStatus.text = e.message
                    binding.btnCapture.isEnabled = true
                    binding.btnCapture.alpha = 1f
                }
        } catch (e: Exception) {
            binding.txtStatus.text = e.message
            binding.btnCapture.isEnabled = true
            binding.btnCapture.alpha = 1f
        }
    }
}
