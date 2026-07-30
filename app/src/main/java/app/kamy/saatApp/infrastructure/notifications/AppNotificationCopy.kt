package app.kamy.saatApp.infrastructure.notifications

import android.content.Context
import app.kamy.saatApp.R
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.core.locale.AppLocale
import app.kamy.saatApp.infrastructure.local.ImportantDayRegistry
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore

object AppNotificationCopy {

    private fun localized(context: Context): Context =
        AppLocale.wrap(context, AppLanguageStore.from(context).current())

    fun prayerTitle(context: Context, prayerName: String, fireAtMillis: Long): String {
        val ctx = localized(context)
        val locationLabel = LocationPreferencesStore.from(ctx).displayLabel()
        val prayerDisp = prayerDisplayName(ctx, prayerName)
        val locationOrTime = locationLabel ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(fireAtMillis))
        return ctx.getString(R.string.prayer_notif_title, prayerDisp, locationOrTime)
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
            NightDivisionKind.LAST_THIRD -> ctx.getString(R.string.tahajud_alarm_title)
        }
    }

    fun nightBody(context: Context, kind: NightDivisionKind): String {
        val ctx = localized(context)
        return when (kind) {
            NightDivisionKind.MIDNIGHT -> ctx.getString(R.string.night_midnight_body)
            NightDivisionKind.FIRST_THIRD -> ctx.getString(R.string.night_first_third_body)
            NightDivisionKind.LAST_THIRD -> ctx.getString(R.string.tahajud_alarm_body)
        }
    }

    fun sunnahTitle(context: Context, kind: String): String? {
        val ctx = localized(context)
        return when (kind) {
            "sunnah_yasin" -> ctx.getString(R.string.sunnah_yasin_title)
            "sunnah_kahf" -> ctx.getString(R.string.sunnah_kahf_title)
            "sunnah_mon_fast" -> ctx.getString(R.string.sunnah_mon_fast_title)
            "sunnah_thu_fast" -> ctx.getString(R.string.sunnah_thu_fast_title)
            "sunnah_dhuha" -> ctx.getString(R.string.sunnah_dhuha_title)
            else -> null
        }
    }

    fun sunnahBody(context: Context, kind: String): String? {
        val ctx = localized(context)
        return when (kind) {
            "sunnah_yasin" -> ctx.getString(R.string.sunnah_yasin_body)
            "sunnah_kahf" -> ctx.getString(R.string.sunnah_kahf_body)
            "sunnah_mon_fast" -> ctx.getString(R.string.sunnah_mon_fast_body)
            "sunnah_thu_fast" -> ctx.getString(R.string.sunnah_thu_fast_body)
            "sunnah_dhuha" -> ctx.getString(R.string.sunnah_dhuha_body)
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
        if (kind?.startsWith("important_day_") == true) {
            val eventTitle = kind.removePrefix("important_day_")
            return importantDayTitle(context, eventTitle)
        }
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
        sunnahBody(context, kind.orEmpty())?.let { return it }
        if (kind?.startsWith("important_day_") == true) {
            val eventTitle = kind.removePrefix("important_day_")
            return importantDayBody(context, eventTitle)
        }
        return ""
    }

    fun importantDayTitle(context: Context, eventTitle: String): String {
        val ctx = localized(context)
        val languageCode = ctx.resources.configuration.locales[0].language
        val language = when (languageCode) {
            "in", "id" -> AppLanguage.INDONESIAN
            "ms" -> AppLanguage.MALAY
            else -> AppLanguage.ENGLISH
        }
        val localizedTitle = ImportantDayRegistry.getLocalizedEventName(eventTitle, language)
        return when (language) {
            AppLanguage.ENGLISH -> "Tomorrow: $localizedTitle"
            AppLanguage.INDONESIAN -> "Besok: $localizedTitle"
            AppLanguage.MALAY -> "Esok: $localizedTitle"
        }
    }

    fun importantDayBody(context: Context, eventTitle: String): String {
        val ctx = localized(context)
        val languageCode = ctx.resources.configuration.locales[0].language
        val language = when (languageCode) {
            "in", "id" -> AppLanguage.INDONESIAN
            "ms" -> AppLanguage.MALAY
            else -> AppLanguage.ENGLISH
        }
        val detail = ImportantDayRegistry.getImportantDayDetail(eventTitle, language) ?: return eventTitle
        return detail.sunnah
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
