package com.halal.scanner.ui

import android.content.ActivityNotFoundException
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
import com.halal.scanner.R
import com.halal.scanner.databinding.FragmentMoreBinding
import com.halal.scanner.util.LocalePrefs

class MoreFragment : Fragment() {
    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateLangLabel()
        binding.cardLanguage.setOnClickListener { showLanguageDialog() }
        binding.cardAbout.setOnClickListener { showInfo() }
        binding.cardInstagram.setOnClickListener { openInstagram() }
    }

    private fun openInstagram() {
        val username = "en0o0n"
        // Einfachster + zuverlässigster Weg: HTTPS-Profil-URL öffnen.
        // Android zeigt einen Chooser bzw. öffnet die Instagram-App falls installiert.
        // App-internes Deep-Linking (instagram://user?...) ist auf neueren IG-Versionen
        // unzuverlässig - manche landen auf Home statt Profil.
        val profileUrl = "https://www.instagram.com/$username/"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl)))
        } catch (_: Exception) {
            // sollte nicht passieren - jedes Android hat einen Browser
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun updateLangLabel() {
        val tag = LocalePrefs(requireContext()).languageTag() ?: "de"
        binding.txtCurrentLang.text = when (tag) {
            "en" -> getString(R.string.lang_en)
            "ar" -> getString(R.string.lang_ar)
            else -> getString(R.string.lang_de)
        }
    }

    private fun showInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.info_title)
            .setMessage(R.string.info_msg)
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
