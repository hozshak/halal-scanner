package com.halal.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.halal.scanner.databinding.ActivityTextScannerBinding
import com.halal.scanner.scanner.TextAnalyzer
import java.util.concurrent.Executors

/**
 * Liest fortlaufend Text aus dem Kamerabild. Wenn der User auf "Erfassen" tippt,
 * wird der aktuell zuletzt erkannte Text zur Analyse weitergegeben.
 */
class TextScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextScannerBinding
    private val executor = Executors.newSingleThreadExecutor()
    @Volatile private var lastText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnCapture.setOnClickListener {
            val text = lastText
            if (text.isNotBlank()) {
                val i = Intent(this, ResultActivity::class.java)
                    .putExtra(ResultActivity.EXTRA_OCR_TEXT, text)
                startActivity(i)
                finish()
            }
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
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(executor, TextAnalyzer { text ->
                lastText = text
                runOnUiThread {
                    val preview = text.take(220).replace("\n", " ")
                    binding.txtPreview.text = preview
                    binding.btnCapture.isEnabled = true
                    binding.btnCapture.alpha = 1.0f
                }
            })
            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}
