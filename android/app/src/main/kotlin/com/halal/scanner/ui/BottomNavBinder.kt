package com.halal.scanner.ui

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.ImageViewCompat
import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.halal.scanner.R

/**
 * Bindet die fünf Tabs aus include_bottom_nav.xml an Click-Callbacks +
 * markiert den aktiven Tab.
 *
 * Mode = welcher Tab gerade aktiv ist - jeder Screen ruft beim Hochfahren
 * `BottomNavBinder.attach(rootView, active = Tab.HOME, onSelect = { ... })`.
 */
object BottomNavBinder {

    enum class Tab { SETTINGS, WATCHLIST, COMPARE, SCAN, HOME }

    fun attach(root: View, active: Tab, onSelect: (Tab) -> Unit) {
        val ctx = root.context
        val active_ = ContextCompat.getColor(ctx, R.color.nav_active_blue)
        val inactive_ = ContextCompat.getColor(ctx, R.color.nav_inactive)

        data class Slot(
            val container: LinearLayout,
            val icon: ImageView,
            val label: TextView,
            val tab: Tab,
        )

        val slots = listOf(
            Slot(
                root.findViewById(R.id.navSettings),
                root.findViewById(R.id.navSettingsIcon),
                root.findViewById(R.id.navSettingsLabel),
                Tab.SETTINGS,
            ),
            Slot(
                root.findViewById(R.id.navWatchlist),
                root.findViewById(R.id.navWatchlistIcon),
                root.findViewById(R.id.navWatchlistLabel),
                Tab.WATCHLIST,
            ),
            Slot(
                root.findViewById(R.id.navCompare),
                root.findViewById(R.id.navCompareIcon),
                root.findViewById(R.id.navCompareLabel),
                Tab.COMPARE,
            ),
            Slot(
                root.findViewById(R.id.navScan),
                root.findViewById(R.id.navScanIcon),
                root.findViewById(R.id.navScanLabel),
                Tab.SCAN,
            ),
            Slot(
                root.findViewById(R.id.navHome),
                root.findViewById(R.id.navHomeIcon),
                root.findViewById(R.id.navHomeLabel),
                Tab.HOME,
            ),
        )

        for (s in slots) {
            val isActive = s.tab == active
            // Scan-Tab hat IMMER blaue Box + weißes Icon. Nur Label-Farbe wechseln.
            if (s.tab == Tab.SCAN) {
                s.label.setTextColor(if (isActive) active_ else inactive_)
            } else {
                val tint = if (isActive) active_ else inactive_
                ImageViewCompat.setImageTintList(s.icon, ColorStateList.valueOf(tint))
                s.label.setTextColor(tint)
            }
            s.container.setOnClickListener { onSelect(s.tab) }
        }
    }
}
