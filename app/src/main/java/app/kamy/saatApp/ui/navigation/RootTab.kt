package app.kamy.saatApp.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.kamy.saatApp.R

enum class RootTab(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val selectedIconRes: Int,
    @DrawableRes val unselectedIconRes: Int
) {
    Today(
        route = "today",
        labelRes = R.string.nav_home,
        selectedIconRes = R.drawable.ic_home_on,
        unselectedIconRes = R.drawable.ic_home_off
    ),
    Quran(
        route = "quran",
        labelRes = R.string.nav_quran,
        selectedIconRes = R.drawable.ic_quran_on,
        unselectedIconRes = R.drawable.ic_quran_off
    ),
    Tools(
        route = "tools",
        labelRes = R.string.nav_spiritual,
        selectedIconRes = R.drawable.ic_spritual_on,
        unselectedIconRes = R.drawable.ic_spritual_off
    ),
    Account(
        route = "account",
        labelRes = R.string.nav_setting,
        selectedIconRes = R.drawable.ic_setting_on,
        unselectedIconRes = R.drawable.ic_setting_off
    );

    companion object {
        val Default = Today

        val mainTabs: List<RootTab> = listOf(Today, Quran, Tools, Account)
    }
}
