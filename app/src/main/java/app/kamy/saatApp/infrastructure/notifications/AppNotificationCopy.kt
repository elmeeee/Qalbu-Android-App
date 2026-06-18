package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppNotificationCopy {

    private fun localized(context: Context): Context =
        AppLocale.wrap(context, AppLanguageStore.from(context).current())

    fun prayerTitle(context: Context, prayerName: String, fireAtMillis: Long): String {
        val ctx = localized(context)
        val time = SimpleDateFormat("HH.mm", Locale.getDefault()).format(Date(fireAtMillis))
        return ctx.getString(R.string.prayer_notif_title, prayerDisplayName(ctx, prayerName), time)
    }

    fun prayerBody(context: Context, prayerName: String): String {
        val ctx = localized(context)
        return when (prayerName) {
            "Fajr" -> ctx.getString(R.string.prayer_body_fajr)
            "Dhuhr" -> ctx.getString(R.string.prayer_body_dhuhr)
            "Asr" -> ctx.getString(R.string.prayer_body_asr)
            "Maghrib" -> ctx.getString(R.string.prayer_body_maghrib)
            "Isha" -> ctx.getString(R.string.prayer_body_isha)
            "Imsak" -> ctx.getString(R.string.prayer_body_imsak)
            else -> ctx.getString(R.string.prayer_body_default, prayerDisplayName(ctx, prayerName))
        }
    }

    fun nightTitle(context: Context, kind: NightDivisionKind): String {
        val ctx = localized(context)
        return when (kind) {
            NightDivisionKind.MIDNIGHT -> ctx.getString(R.string.night_midnight_title)
            NightDivisionKind.FIRST_THIRD -> ctx.getString(R.string.night_first_third_title)
            NightDivisionKind.LAST_THIRD -> ctx.getString(R.string.night_last_third_title)
        }
    }

    fun nightBody(context: Context, kind: NightDivisionKind): String {
        val ctx = localized(context)
        return when (kind) {
            NightDivisionKind.MIDNIGHT -> ctx.getString(R.string.night_midnight_body)
            NightDivisionKind.FIRST_THIRD -> ctx.getString(R.string.night_first_third_body)
            NightDivisionKind.LAST_THIRD -> ctx.getString(R.string.night_last_third_body)
        }
    }

    fun sunnahTitle(context: Context, kind: String): String? {
        val ctx = localized(context)
        return when (kind) {
            "sunnah_yasin" -> ctx.getString(R.string.sunnah_yasin_title)
            "sunnah_kahf" -> ctx.getString(R.string.sunnah_kahf_title)
            else -> null
        }
    }

    fun sunnahBody(context: Context, kind: String): String? {
        val ctx = localized(context)
        return when (kind) {
            "sunnah_yasin" -> ctx.getString(R.string.sunnah_yasin_body)
            "sunnah_kahf" -> ctx.getString(R.string.sunnah_kahf_body)
            else -> null
        }
    }

    fun resolveTitle(context: Context, kind: String?, prayerName: String?, fireAtMillis: Long): String {
        if (!prayerName.isNullOrBlank() && kind?.startsWith("prayer_") == true) {
            return prayerTitle(context, prayerName, fireAtMillis)
        }
        if (kind == "imsak") {
            return prayerTitle(context, "Imsak", fireAtMillis)
        }
        kind?.removePrefix("night_")?.let { raw ->
            runCatching { NightDivisionKind.valueOf(raw) }.getOrNull()?.let {
                return nightTitle(context, it)
            }
        }
        sunnahTitle(context, kind.orEmpty())?.let { return it }
        return ""
    }

    fun resolveBody(context: Context, kind: String?, prayerName: String?): String {
        if (!prayerName.isNullOrBlank() && kind?.startsWith("prayer_") == true) {
            return prayerBody(context, prayerName)
        }
        if (kind == "imsak") {
            return prayerBody(context, "Imsak")
        }
        kind?.removePrefix("night_")?.let { raw ->
            runCatching { NightDivisionKind.valueOf(raw) }.getOrNull()?.let {
                return nightBody(context, it)
            }
        }
        return sunnahBody(context, kind.orEmpty()).orEmpty()
    }

    fun prayerDisplayName(context: Context, prayerName: String): String {
        val ctx = localized(context)
        return when (prayerName) {
            "Fajr" -> ctx.getString(R.string.prayer_fajr)
            "Dhuhr" -> ctx.getString(R.string.prayer_dhuhr)
            "Asr" -> ctx.getString(R.string.prayer_asr)
            "Maghrib" -> ctx.getString(R.string.prayer_maghrib)
            "Isha" -> ctx.getString(R.string.prayer_isha)
            "Imsak" -> ctx.getString(R.string.prayer_imsak)
            else -> prayerName
        }
    }
}
