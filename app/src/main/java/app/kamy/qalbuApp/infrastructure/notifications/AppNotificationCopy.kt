package app.kamy.qalbuApp.infrastructure.notifications

import android.content.Context
import app.kamy.qalbuApp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppNotificationCopy {

    fun prayerTitle(context: Context, prayerName: String, fireAtMillis: Long): String {
        val time = SimpleDateFormat("HH.mm", Locale.getDefault()).format(Date(fireAtMillis))
        return context.getString(R.string.prayer_notif_title, prayerDisplayName(context, prayerName), time)
    }

    fun prayerBody(context: Context, prayerName: String): String = when (prayerName) {
        "Fajr" -> context.getString(R.string.prayer_body_fajr)
        "Dhuhr" -> context.getString(R.string.prayer_body_dhuhr)
        "Asr" -> context.getString(R.string.prayer_body_asr)
        "Maghrib" -> context.getString(R.string.prayer_body_maghrib)
        "Isha" -> context.getString(R.string.prayer_body_isha)
        "Imsak" -> context.getString(R.string.prayer_body_imsak)
        else -> context.getString(R.string.prayer_body_default, prayerDisplayName(context, prayerName))
    }

    fun nightTitle(context: Context, kind: NightDivisionKind): String = when (kind) {
        NightDivisionKind.MIDNIGHT -> context.getString(R.string.night_midnight_title)
        NightDivisionKind.FIRST_THIRD -> context.getString(R.string.night_first_third_title)
        NightDivisionKind.LAST_THIRD -> context.getString(R.string.night_last_third_title)
    }

    fun nightBody(context: Context, kind: NightDivisionKind): String = when (kind) {
        NightDivisionKind.MIDNIGHT -> context.getString(R.string.night_midnight_body)
        NightDivisionKind.FIRST_THIRD -> context.getString(R.string.night_first_third_body)
        NightDivisionKind.LAST_THIRD -> context.getString(R.string.night_last_third_body)
    }

    fun sunnahTitle(context: Context, kind: String): String? = when (kind) {
        "sunnah_yasin" -> context.getString(R.string.sunnah_yasin_title)
        "sunnah_kahf" -> context.getString(R.string.sunnah_kahf_title)
        else -> null
    }

    fun sunnahBody(context: Context, kind: String): String? = when (kind) {
        "sunnah_yasin" -> context.getString(R.string.sunnah_yasin_body)
        "sunnah_kahf" -> context.getString(R.string.sunnah_kahf_body)
        else -> null
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

    fun prayerDisplayName(context: Context, prayerName: String): String = when (prayerName) {
        "Fajr" -> context.getString(R.string.prayer_fajr)
        "Dhuhr" -> context.getString(R.string.prayer_dhuhr)
        "Asr" -> context.getString(R.string.prayer_asr)
        "Maghrib" -> context.getString(R.string.prayer_maghrib)
        "Isha" -> context.getString(R.string.prayer_isha)
        "Imsak" -> context.getString(R.string.prayer_imsak)
        else -> prayerName
    }
}
