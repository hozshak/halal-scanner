package com.halal.scanner.ui

import android.content.Intent
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
import androidx.core.content.FileProvider
import com.halal.scanner.R
import com.halal.scanner.databinding.ActivityFrontScannerBinding
import java.io.File

/**
 * Vorderseite-Scanner mit BILDER-Suche (z.B. Google Lens):
 * 1. Foto vom Produkt
 * 2. Teile das Foto via ACTION_SEND - User wählt Google Lens aus
 * 3. Lens identifiziert das Produkt visuell, zeigt Web-Treffer
 */
class FrontScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFrontScannerBinding
    private lateinit var imageCapture: ImageCapture
    private var capturedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrontScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnSearchImage.setOnClickListener { shareForImageSearch() }
        binding.btnRetake.setOnClickListener {
            binding.resultPanel.visibility = View.GONE
            binding.btnCapture.visibility = View.VISIBLE
        }
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
                capturedFile = photoFile
                showPhoto(photoFile)
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("FrontScanner", "capture failed", exception)
                binding.btnCapture.isEnabled = true
                binding.btnCapture.alpha = 1f
            }
        })
    }

    private fun showPhoto(file: File) {
        try {
            val bmp = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            binding.imgPreview.setImageBitmap(bmp)
        } catch (_: Exception) {}
        binding.resultPanel.visibility = View.VISIBLE
        binding.btnCapture.visibility = View.GONE
    }

    private fun shareForImageSearch() {
        val file = capturedFile ?: return
        try {
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.front_share_chooser_title)))
        } catch (e: Exception) {
            Log.e("FrontScanner", "share failed", e)
        }
    }
}
