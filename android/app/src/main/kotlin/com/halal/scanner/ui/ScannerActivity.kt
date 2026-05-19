package com.halal.scanner.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.lifecycleScope
import com.halal.scanner.databinding.ActivityScannerBinding
import com.halal.scanner.scanner.BarcodeAnalyzer
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding
    private val executor by lazy { Executors.newSingleThreadExecutor() }
    @Volatile private var consumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnCancel.setOnClickListener { finish() }
        startCamera()
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = binding.previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor, BarcodeAnalyzer { code, _ ->
                    if (!consumed) {
                        consumed = true
                        runOnUiThread { openResult(code) }
                    }
                })
                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.e("Scanner", "camera init failed: ${e.message}")
            }
        }, ContextCompatExecutor)
    }

    private fun openResult(barcode: String) {
        val i = Intent(this, ResultActivity::class.java)
            .putExtra(ResultActivity.EXTRA_BARCODE, barcode)
        startActivity(i)
        finish()
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }

    private val ContextCompatExecutor get() = androidx.core.content.ContextCompat.getMainExecutor(this)
}
