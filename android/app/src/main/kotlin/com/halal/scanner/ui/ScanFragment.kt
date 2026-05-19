package com.halal.scanner.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.halal.scanner.R
import com.halal.scanner.databinding.FragmentScanBinding
import com.halal.scanner.util.LocalePrefs

class ScanFragment : Fragment() {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private var pendingMode: Mode = Mode.BARCODE

    enum class Mode { BARCODE, OCR, FRONT }

    private val cameraPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchScan()
        else showPermissionDenied()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardBarcode.setOnClickListener {
            pendingMode = Mode.BARCODE
            requestCameraAndScan()
        }
        binding.cardOcr.setOnClickListener {
            pendingMode = Mode.OCR
            requestCameraAndScan()
        }
        binding.cardFront.setOnClickListener {
            pendingMode = Mode.FRONT
            requestCameraAndScan()
        }
        binding.btnManualLookup.setOnClickListener { lookupManual() }
        binding.txtManualBarcode.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) { lookupManual(); true } else false
        }
        binding.btnLanguage.setOnClickListener { showLanguageDialog() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun requestCameraAndScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            launchScan()
        } else {
            cameraPerm.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchScan() {
        val cls = when (pendingMode) {
            Mode.BARCODE -> ScannerActivity::class.java
            Mode.OCR -> TextScannerActivity::class.java
            Mode.FRONT -> FrontScannerActivity::class.java
        }
        startActivity(Intent(requireContext(), cls))
    }

    private fun lookupManual() {
        val code = binding.txtManualBarcode.text?.toString()?.trim().orEmpty()
        if (code.length < 4) return
        startActivity(
            Intent(requireContext(), ResultActivity::class.java)
                .putExtra(ResultActivity.EXTRA_BARCODE, code)
        )
    }

    private fun showPermissionDenied() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.permission_denied_title)
            .setMessage(R.string.permission_denied_msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showLanguageDialog() {
        val ctx = requireContext()
        val tags = arrayOf("de", "en", "ar")
        val labels = arrayOf(
            getString(R.string.lang_de),
            getString(R.string.lang_en),
            getString(R.string.lang_ar)
        )
        val current = LocalePrefs(ctx).languageTag()
        val checked = tags.indexOf(current).takeIf { it >= 0 } ?: 0
        AlertDialog.Builder(ctx)
            .setTitle(R.string.lang_choose)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                val tag = tags[which]
                LocalePrefs(ctx).setLanguageTag(tag)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
