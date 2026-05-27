package app.kamy.qalbuApp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom-nav destinations. Mirrors iOS App/RootTabView.swift tabs
 * (Today / Reflect / Quran). Account is opened from the Today header avatar (iOS parity).
 */
enum class RootTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Today(
        route = "today",
        labelRes = app.kamy.qalbuApp.R.string.nav_today,
        selectedIcon = Icons.Filled.WbSunny,
        unselectedIcon = Icons.Outlined.WbSunny
    ),
    Reflect(
        route = "reflect",
        labelRes = app.kamy.qalbuApp.R.string.nav_reflect,
        selectedIcon = Icons.Filled.Edit,
        unselectedIcon = Icons.Outlined.Edit
    ),
    Quran(
        route = "quran",
        labelRes = app.kamy.qalbuApp.R.string.nav_quran,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    ),
    Account(
        route = "account",
        labelRes = app.kamy.qalbuApp.R.string.nav_account,
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    );

    companion object {
        val Default = Today

        /** Bottom bar tabs — mirrors iOS RootTabView (Account opened from Today header). */
        val mainTabs: List<RootTab> = listOf(Today, Reflect, Quran)
    }
}
