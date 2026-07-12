package app.kamy.saatApp.features.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.saatApp.ui.permissions.hasAggressiveOemBatteryManagement
import app.kamy.saatApp.ui.permissions.isIgnoringBatteryOptimizations
import app.kamy.saatApp.ui.permissions.openBackgroundReliabilitySettings
import app.kamy.saatApp.design.components.SaatSettingsGroup
import app.kamy.saatApp.design.components.SaatSettingsNavigationRow
import app.kamy.saatApp.design.components.SaatSettingsToggleRow
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

@Composable
fun NotificationSettingsScreen(
    vm: AccountViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val showBatterySettings = hasAggressiveOemBatteryManagement()
    val batteryUnrestricted = context.isIgnoringBatteryOptimizations()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .tabContentStatusBarInset()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SaatSpacing.screenHorizontal, vertical = SaatSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SaatSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.notifications_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = stringResource(R.string.notifications_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = SaatSpacing.xs)
        )

        NotificationSectionLabel(stringResource(R.string.section_quran))
        SaatSettingsGroup {
            SaatSettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.daily_verse),
                subtitle = stringResource(R.string.daily_verse_subtitle),
                checked = state.dailyVerseEnabled,
                onCheckedChange = vm::setDailyVerseEnabled
            )
            if (state.dailyVerseEnabled) {
                SaatSettingsNavigationRow(
                    icon = Icons.Filled.Schedule,
                    title = stringResource(R.string.reminder_time),
                    subtitle = state.reminderTimeLabel.ifBlank { "7:00 AM" },
                    onClick = { vm.toggleNotifTimeSheet(true) }
                )
            }

        }

        NotificationSectionLabel(stringResource(R.string.section_prayer))
        SaatSettingsGroup {
            if (showBatterySettings) {
                SaatSettingsNavigationRow(
                    icon = Icons.Filled.BatteryChargingFull,
                    title = stringResource(R.string.battery_opt_settings_title),
                    subtitle = stringResource(
                        if (batteryUnrestricted) {
                            R.string.battery_opt_settings_subtitle_ok
                        } else {
                            R.string.battery_opt_settings_subtitle
                        }
                    ),
                    onClick = { context.openBackgroundReliabilitySettings() }
                )
            }
            PrayerType.ADZAN_NOTIFICATION_PRAYERS.forEach { prayer ->
                val isAdzanSoundEnabled = state.isPrayerNotificationEnabled(prayer)
                val isIndoMalay = state.appLanguage == app.kamy.saatApp.core.locale.AppLanguage.INDONESIAN || state.appLanguage == app.kamy.saatApp.core.locale.AppLanguage.MALAY
                val sub = if (isAdzanSoundEnabled) {
                    if (isIndoMalay) "Suara Adzan" else "Adhan Voice"
                } else {
                    if (isIndoMalay) "Suara Bawaan HP" else "Default System Sound"
                }
                SaatSettingsToggleRow(
                    icon = Icons.Filled.Notifications,
                    title = AppNotificationCopy.prayerDisplayName(context, prayer.aladhanKey),
                    subtitle = sub,
                    checked = isAdzanSoundEnabled,
                    onCheckedChange = { enabled ->
                        vm.setPrayerNotificationEnabled(prayer, enabled)
                    }
                )
            }
            SaatSettingsToggleRow(
                icon = Icons.Filled.WbTwilight,
                title = stringResource(R.string.imsak),
                subtitle = stringResource(R.string.imsak_subtitle),
                checked = state.imsakEnabled,
                onCheckedChange = vm::setImsakEnabled
            )
        }

        NotificationSectionLabel(stringResource(R.string.section_night))
        SaatSettingsGroup {
            SaatSettingsToggleRow(
                icon = Icons.Filled.Bedtime,
                title = stringResource(R.string.midnight),
                subtitle = stringResource(R.string.midnight_subtitle),
                checked = state.midnightEnabled,
                onCheckedChange = vm::setMidnightEnabled
            )
            SaatSettingsToggleRow(
                icon = Icons.Filled.Bedtime,
                title = stringResource(R.string.first_third),
                subtitle = stringResource(R.string.first_third_subtitle),
                checked = state.firstThirdEnabled,
                onCheckedChange = vm::setFirstThirdEnabled
            )
            SaatSettingsToggleRow(
                icon = Icons.Filled.Bedtime,
                title = stringResource(R.string.last_third),
                subtitle = stringResource(R.string.last_third_subtitle),
                checked = state.tahajudEnabled,
                onCheckedChange = vm::setTahajudEnabled
            )
        }

        NotificationSectionLabel(stringResource(R.string.section_sunnah))
        SaatSettingsGroup {
            SaatSettingsNavigationRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = stringResource(R.string.surah_reminders_settings_title),
                subtitle = stringResource(R.string.surah_reminders_settings_subtitle),
                onClick = vm::openSurahRemindersSheet
            )
            SaatSettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.important_days_reminder),
                subtitle = stringResource(R.string.important_days_reminder_subtitle),
                checked = state.importantDaysReminderEnabled,
                onCheckedChange = vm::setImportantDaysReminderEnabled
            )
            SaatSettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.fast_mon_thu),
                subtitle = stringResource(R.string.fast_mon_thu_subtitle),
                checked = state.monThuFastReminderEnabled,
                onCheckedChange = vm::setMonThuFastReminderEnabled
            )
            SaatSettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.dhuha_reminder),
                subtitle = stringResource(R.string.dhuha_reminder_subtitle),
                checked = state.dhuhaReminderEnabled,
                onCheckedChange = vm::setDhuhaReminderEnabled
            )
            if (state.dhuhaReminderEnabled) {
                val context = LocalContext.current
                val isIndoMalay = state.appLanguage == app.kamy.saatApp.core.locale.AppLanguage.INDONESIAN || state.appLanguage == app.kamy.saatApp.core.locale.AppLanguage.MALAY
                SaatSettingsNavigationRow(
                    icon = Icons.Filled.Schedule,
                    title = if (isIndoMalay) "Waktu Pengingat Duha" else "Dhuha Reminder Time",
                    subtitle = state.dhuhaTimeLabel,
                    onClick = {
                        android.app.TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                vm.setDhuhaTime(hour, minute)
                            },
                            state.dhuhaHour,
                            state.dhuhaMinute,
                            true
                        ).show()
                    }
                )
            }
        }

        Spacer(Modifier.height(SaatSpacing.xl))
    }
}

private fun prayerNotificationSubtitle(context: android.content.Context, prayer: PrayerType): String =
    when (prayer) {
        PrayerType.FAJR -> context.getString(R.string.prayer_notif_fajr_sub)
        PrayerType.DHUHR -> context.getString(R.string.prayer_notif_dhuhr_sub)
        PrayerType.ASR -> context.getString(R.string.prayer_notif_asr_sub)
        PrayerType.MAGHRIB -> context.getString(R.string.prayer_notif_maghrib_sub)
        PrayerType.ISHA -> context.getString(R.string.prayer_notif_isha_sub)
        else -> ""
    }

@Composable
private fun NotificationSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = SaatSpacing.xs, vertical = SaatSpacing.xs)
    )
}
