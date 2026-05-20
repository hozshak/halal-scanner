package com.halal.scanner.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.halal.scanner.BuildConfig
import com.halal.scanner.R
import com.halal.scanner.databinding.FragmentSettingsBinding
import com.halal.scanner.db.ScanStatsStore
import com.halal.scanner.util.LocalePrefs

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        // Stats
        val stats = ScanStatsStore(ctx)
        binding.txtStatAppVersion.text = BuildConfig.VERSION_NAME
        binding.txtStatHalal.text = stats.halal().toString()
        binding.txtStatTotalScans.text = stats.total().toString()

        // About
        binding.txtAboutVersion.text =
            getString(R.string.settings_about_version_line, BuildConfig.VERSION_NAME)

        // Aktuelle Sprache anzeigen
        binding.txtCurrentLanguage.text = when (LocalePrefs(ctx).languageTag()) {
            "en" -> getString(R.string.lang_en)
            "ar" -> getString(R.string.lang_ar)
            else -> getString(R.string.lang_de)
        }

        binding.rowLanguage.setOnClickListener { showLanguageDialog() }
        binding.rowAllergens.setOnClickListener {
            android.widget.Toast.makeText(ctx,
                R.string.settings_allergens_coming_soon,
                android.widget.Toast.LENGTH_SHORT).show()
        }
        binding.rowReportBug.setOnClickListener {
            openUrl("https://github.com/hozshak/halal-scanner/issues/new")
        }
        binding.rowFeedback.setOnClickListener {
            openUrl("https://www.instagram.com/en0o0n/")
        }
        binding.btnAboutInstagram.setOnClickListener {
            openUrl("https://www.instagram.com/en0o0n/")
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {}
    }

    private fun showLanguageDialog() {
        val ctx = requireContext()
        val tags = arrayOf("de", "en", "ar")
        val labels = arrayOf(
            getString(R.string.lang_de),
            getString(R.string.lang_en),
            getString(R.string.lang_ar),
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
