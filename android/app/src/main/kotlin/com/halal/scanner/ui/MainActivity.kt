package com.halal.scanner.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.halal.scanner.R
import com.halal.scanner.databinding.ActivityMainBinding
import com.halal.scanner.util.LocalePrefs

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var pendingScanMode: ScanMode = ScanMode.BARCODE

    private val cameraPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchScan()
        else showPermissionDenied()
    }

    enum class ScanMode { BARCODE, OCR }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardBarcode.setOnClickListener {
            pendingScanMode = ScanMode.BARCODE
            requestCameraAndScan()
        }
        binding.cardOcr.setOnClickListener {
            pendingScanMode = ScanMode.OCR
            requestCameraAndScan()
        }
        binding.btnManualLookup.setOnClickListener { lookupManual() }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnInfo.setOnClickListener { showInfo() }
        binding.btnLanguage.setOnClickListener { showLanguageDialog() }
    }

    private fun requestCameraAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            launchScan()
        } else {
            cameraPerm.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchScan() {
        val cls = when (pendingScanMode) {
            ScanMode.BARCODE -> ScannerActivity::class.java
            ScanMode.OCR -> TextScannerActivity::class.java
        }
        startActivity(Intent(this, cls))
    }

    private fun lookupManual() {
        val code = binding.txtManualBarcode.text?.toString()?.trim().orEmpty()
        if (code.length < 4) return
        startActivity(
            Intent(this, ResultActivity::class.java)
                .putExtra(ResultActivity.EXTRA_BARCODE, code)
        )
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

    private fun showLanguageDialog() {
        val tags = arrayOf("de", "en", "ar")
        val labels = arrayOf(
            getString(R.string.lang_de),
            getString(R.string.lang_en),
            getString(R.string.lang_ar)
        )
        val current = LocalePrefs(this).languageTag()
        val checked = tags.indexOf(current).takeIf { it >= 0 } ?: 0
        AlertDialog.Builder(this)
            .setTitle(R.string.lang_choose)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                val tag = tags[which]
                LocalePrefs(this).setLanguageTag(tag)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
