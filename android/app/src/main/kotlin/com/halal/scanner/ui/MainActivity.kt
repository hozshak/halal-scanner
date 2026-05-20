package com.halal.scanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.halal.scanner.R
import com.halal.scanner.databinding.ActivityMainBinding

/**
 * Hauptscreen mit 5-Tab-Bottom-Navigation:
 *   Settings · Watchlist · Compare · [Scan (zentral, blau)] · Home
 *
 * Der zentrale Scan-Button öffnet direkt den Barcode-Scanner (Schnellzugriff).
 * Beim Zurück landet man im zuvor aktiven Tab.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentTab: BottomNavBinder.Tab = BottomNavBinder.Tab.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            selectTab(BottomNavBinder.Tab.HOME, initial = true)
        } else {
            // Tab nach Konfigurationsänderung wiederherstellen
            BottomNavBinder.attach(binding.bottomNavContainer.root, currentTab, ::onTabSelected)
        }
    }

    private fun onTabSelected(tab: BottomNavBinder.Tab) {
        if (tab == BottomNavBinder.Tab.SCAN) {
            // Scan-Tab ist Shortcut: direkt Kamera-Scanner öffnen, Tab-Highlight
            // bleibt aber bei "SCAN" damit die UI mit den Screenshots übereinstimmt.
            startActivity(Intent(this, ScannerActivity::class.java))
            return
        }
        selectTab(tab)
    }

    private fun selectTab(tab: BottomNavBinder.Tab, initial: Boolean = false) {
        currentTab = tab
        val frag: Fragment = when (tab) {
            BottomNavBinder.Tab.HOME      -> ScanFragment()        // Übersicht / Hauptschirm
            BottomNavBinder.Tab.WATCHLIST -> WatchlistFragment()
            BottomNavBinder.Tab.COMPARE   -> CompareFragment()
            BottomNavBinder.Tab.SETTINGS  -> SettingsFragment()
            BottomNavBinder.Tab.SCAN      -> ScanFragment() // wird nie über selectTab erreicht
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, frag)
            .commit()

        // Visueller Tab-Highlight neu binden
        BottomNavBinder.attach(binding.bottomNavContainer.root, tab, ::onTabSelected)
    }
}
