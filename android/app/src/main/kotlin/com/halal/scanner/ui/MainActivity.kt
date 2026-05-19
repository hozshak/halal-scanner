package com.halal.scanner.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.halal.scanner.R
import com.halal.scanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val cameraPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startScan()
        else showPermissionDenied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener { requestCameraAndScan() }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnInfo.setOnClickListener { showInfo() }
    }

    private fun requestCameraAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startScan()
        } else {
            cameraPerm.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScan() {
        startActivity(Intent(this, ScannerActivity::class.java))
    }

    private fun showPermissionDenied() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_denied_title)
            .setMessage(R.string.permission_denied_msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showInfo() {
        AlertDialog.Builder(this)
            .setTitle(R.string.info_title)
            .setMessage(R.string.info_msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
