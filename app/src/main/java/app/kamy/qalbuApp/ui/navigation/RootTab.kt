package app.kamy.qalbuApp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom-nav destinations. Mirrors iOS App/RootTabView.swift tabs
 * (Today / Reflect / Quran). Account is accessed from a header button on each tab
 * in iOS — we elevate it to the bottom nav for Material parity.
 */
enum class RootTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    Today("today", app.kamy.qalbuApp.R.string.nav_today, Icons.Filled.WbSunny),
    Reflect("reflect", app.kamy.qalbuApp.R.string.nav_reflect, Icons.Filled.Forum),
    Quran("quran", app.kamy.qalbuApp.R.string.nav_quran, Icons.Filled.AutoStories),
    Account("account", app.kamy.qalbuApp.R.string.nav_account, Icons.Filled.AccountCircle);

    companion object {
        val Default = Today
    }
}
