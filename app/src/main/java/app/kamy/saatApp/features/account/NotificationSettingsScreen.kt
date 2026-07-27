package app.kamy.saatApp.features.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kamy.saatApp.BuildConfig
import app.kamy.saatApp.R
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.infrastructure.audio.AdhanPlaybackService
import app.kamy.saatApp.infrastructure.notifications.AppNotificationCopy
import app.kamy.saatApp.ui.permissions.hasAggressiveOemBatteryManagement
import app.kamy.saatApp.ui.permissions.isIgnoringBatteryOptimizations
import app.kamy.saatApp.ui.permissions.openBackgroundReliabilitySettings
import app.kamy.saatApp.design.components.SaatSettingsGroup
import app.kamy.saatApp.design.components.SaatSettingsNavigationRow
import app.kamy.saatApp.design.components.SaatSettingsToggleRow
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.saatApp.infrastructure.preferences.AdhanPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
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
    var showTestAdhanDialog by remember { mutableStateOf(false) }

    if (showTestAdhanDialog) {
        val testPrayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        AlertDialog(
            onDismissRequest = { showTestAdhanDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.select_test_prayer),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    testPrayers.forEach { prayerKey ->
                        val displayName = AppNotificationCopy.prayerDisplayName(context, prayerKey)
                        TextButton(
                            onClick = {
                                showTestAdhanDialog = false
                                val store = AdhanPreferencesStore.from(context)
                                val voice = store.currentVoice()
                                val fajrVoice = store.currentFajrVoice()
                                val rawRes = AdhanVoiceCatalog.rawResForPrayer(prayerKey, voice, fajrVoice)
                                val locationLabel = LocationPreferencesStore.from(context).displayLabel() ?: "Jakarta"
                                val bodyText = AppNotificationCopy.prayerBody(context, prayerKey)
                                AdhanPlaybackService.start(
                                    context = context,
                                    rawRes = rawRes,
                                    title = "$displayName • $locationLabel",
                                    body = bodyText,
                                    prayerName = prayerKey
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTestAdhanDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Header bar staying fixed at top
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .tabContentStatusBarInset()
                    .padding(horizontal = SaatSpacing.screenHorizontal, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = SaatColors.DeepEmerald
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.notifications_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SaatColors.DeepEmerald
                    )
                }
            }
        }

        // Scrollable Settings Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SaatSpacing.screenHorizontal, vertical = SaatSpacing.md),
            verticalArrangement = Arrangement.spacedBy(SaatSpacing.md)
        ) {
            // Section 1: Al-Qur'an
            NotificationSectionLabel(
                text = stringResource(R.string.section_quran),
                accentColor = Color(0xFF0D9488)
            )
            SaatSettingsGroup {
                SaatSettingsToggleRow(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.daily_verse),
                    subtitle = stringResource(R.string.daily_verse_subtitle),
                    checked = state.dailyVerseEnabled,
                    onCheckedChange = vm::setDailyVerseEnabled,
                    iconTint = Color(0xFF0D9488),
                    iconBgColor = Color(0xFFF0FDFA),
                    showDivider = state.dailyVerseEnabled
                )
                if (state.dailyVerseEnabled) {
                    SaatSettingsNavigationRow(
                        icon = Icons.Filled.Schedule,
                        title = stringResource(R.string.reminder_time),
                        subtitle = state.reminderTimeLabel.ifBlank { "7:00 AM" },
                        valueBadge = state.reminderTimeLabel.ifBlank { "7:00 AM" },
                        onClick = { vm.toggleNotifTimeSheet(true) },
                        iconTint = Color(0xFF0D9488),
                        iconBgColor = Color(0xFFF0FDFA),
                        showDivider = false
                    )
                }
            }

            // Section 2: Waktu Shalat & Adzan
            NotificationSectionLabel(
                text = stringResource(R.string.section_prayer),
                accentColor = Color(0xFFD97706)
            )
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
                        valueBadge = if (batteryUnrestricted) {
                            stringResource(R.string.battery_unrestricted)
                        } else {
                            stringResource(R.string.battery_optimized)
                        },
                        onClick = { context.openBackgroundReliabilitySettings() },
                        iconTint = if (batteryUnrestricted) Color(0xFF085E43) else Color(0xFFD97706),
                        iconBgColor = if (batteryUnrestricted) Color(0xFFE6F3EE) else Color(0xFFFFF7ED),
                        showDivider = true
                    )
                }
                PrayerType.ADZAN_NOTIFICATION_PRAYERS.forEachIndexed { idx, prayer ->
                    val isAdzanSoundEnabled = state.isPrayerNotificationEnabled(prayer)
                    val sub = if (isAdzanSoundEnabled) {
                        stringResource(R.string.sound_mode_adhan)
                    } else {
                        stringResource(R.string.sound_mode_system)
                    }
                    SaatSettingsToggleRow(
                        icon = Icons.Filled.Notifications,
                        title = AppNotificationCopy.prayerDisplayName(context, prayer.aladhanKey),
                        subtitle = sub,
                        checked = isAdzanSoundEnabled,
                        onCheckedChange = { enabled ->
                            vm.setPrayerNotificationEnabled(prayer, enabled)
                        },
                        iconTint = Color(0xFFD97706),
                        iconBgColor = Color(0xFFFFF7ED),
                        showDivider = true
                    )
                }
                SaatSettingsToggleRow(
                    icon = Icons.Filled.WbTwilight,
                    title = stringResource(R.string.imsak),
                    subtitle = stringResource(R.string.imsak_subtitle),
                    checked = state.imsakEnabled,
                    onCheckedChange = vm::setImsakEnabled,
                    iconTint = Color(0xFFD97706),
                    iconBgColor = Color(0xFFFFF7ED),
                    showDivider = false
                )
            }

            // Debug-only: test adhan full-screen intent without waiting for prayer time.
            if (BuildConfig.DEBUG) {
                NotificationSectionLabel(
                    text = "🐞 Debug & Testing",
                    accentColor = Color(0xFFE11D48)
                )
                SaatSettingsGroup {
                    SaatSettingsNavigationRow(
                        icon = Icons.Filled.BugReport,
                        title = "Test Adzan Alarm (Full Screen)",
                        subtitle = "Pilih waktu shalat untuk simulasi adzan",
                        onClick = { showTestAdhanDialog = true },
                        iconTint = Color(0xFFE11D48),
                        iconBgColor = Color(0xFFFFE4E6),
                        showDivider = false
                    )
                }
            }

            // Section 3: Shalat Malam & Qiyam
            NotificationSectionLabel(
                text = stringResource(R.string.section_night),
                accentColor = Color(0xFF7C3AED)
            )
            SaatSettingsGroup {
                SaatSettingsToggleRow(
                    icon = Icons.Filled.Bedtime,
                    title = stringResource(R.string.midnight),
                    subtitle = stringResource(R.string.midnight_subtitle),
                    checked = state.midnightEnabled,
                    onCheckedChange = vm::setMidnightEnabled,
                    iconTint = Color(0xFF7C3AED),
                    iconBgColor = Color(0xFFF3E8FF),
                    showDivider = true
                )
                SaatSettingsToggleRow(
                    icon = Icons.Filled.Bedtime,
                    title = stringResource(R.string.first_third),
                    subtitle = stringResource(R.string.first_third_subtitle),
                    checked = state.firstThirdEnabled,
                    onCheckedChange = vm::setFirstThirdEnabled,
                    iconTint = Color(0xFF7C3AED),
                    iconBgColor = Color(0xFFF3E8FF),
                    showDivider = true
                )
                SaatSettingsToggleRow(
                    icon = Icons.Filled.Bedtime,
                    title = stringResource(R.string.last_third),
                    subtitle = stringResource(R.string.last_third_subtitle),
                    checked = state.tahajudEnabled,
                    onCheckedChange = vm::setTahajudEnabled,
                    iconTint = Color(0xFF7C3AED),
                    iconBgColor = Color(0xFFF3E8FF),
                    showDivider = false
                )
            }

            // Section 4: Amalan Sunnah
            NotificationSectionLabel(
                text = stringResource(R.string.section_sunnah),
                accentColor = Color(0xFF085E43)
            )
            SaatSettingsGroup {
                val activeSurahCount = state.surahReminders.count { it.enabled }
                SaatSettingsNavigationRow(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = stringResource(R.string.surah_reminders_settings_title),
                    subtitle = stringResource(R.string.surah_reminders_settings_subtitle),
                    valueBadge = if (activeSurahCount > 0) "$activeSurahCount Aktif" else null,
                    onClick = vm::openSurahRemindersSheet,
                    iconTint = Color(0xFF085E43),
                    iconBgColor = Color(0xFFE6F3EE),
                    showDivider = true
                )
                SaatSettingsToggleRow(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.important_days_reminder),
                    subtitle = stringResource(R.string.important_days_reminder_subtitle),
                    checked = state.importantDaysReminderEnabled,
                    onCheckedChange = vm::setImportantDaysReminderEnabled,
                    iconTint = Color(0xFF085E43),
                    iconBgColor = Color(0xFFE6F3EE),
                    showDivider = true
                )
                SaatSettingsToggleRow(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.fast_mon_thu),
                    subtitle = stringResource(R.string.fast_mon_thu_subtitle),
                    checked = state.monThuFastReminderEnabled,
                    onCheckedChange = vm::setMonThuFastReminderEnabled,
                    iconTint = Color(0xFF085E43),
                    iconBgColor = Color(0xFFE6F3EE),
                    showDivider = true
                )
            }

            // Dhuha Reminder
            NotificationSectionLabel(
                text = stringResource(R.string.dhuha_reminder),
                accentColor = Color(0xFF085E43)
            )
            SaatSettingsGroup {
                SaatSettingsToggleRow(
                    icon = Icons.Filled.WbTwilight,
                    title = stringResource(R.string.dhuha_reminder),
                    subtitle = stringResource(R.string.dhuha_reminder_subtitle),
                    checked = state.dhuhaReminderEnabled,
                    onCheckedChange = vm::setDhuhaReminderEnabled,
                    iconTint = Color(0xFF085E43),
                    iconBgColor = Color(0xFFE6F3EE),
                    showDivider = state.dhuhaReminderEnabled
                )
                if (state.dhuhaReminderEnabled) {
                    SaatSettingsNavigationRow(
                        icon = Icons.Filled.Schedule,
                        title = stringResource(R.string.dhuha_reminder_time_title),
                        subtitle = state.dhuhaTimeLabel,
                        valueBadge = state.dhuhaTimeLabel,
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
                        },
                        iconTint = Color(0xFF085E43),
                        iconBgColor = Color(0xFFE6F3EE),
                        showDivider = false
                    )
                }
            }

            Spacer(Modifier.height(SaatSpacing.xl))
        }
    }
}

@Composable
private fun NotificationHeroCard(
    activeVoice: String,
    summary: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SaatColors.PrimaryVerticalGradient)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Pengaturan Notifikasi & Adzan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.22f),
                    thickness = 1.dp
                )

                // Active Voice badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "🔊 Muazin Active: $activeVoice",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSectionLabel(
    text: String,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.padding(start = 2.dp, bottom = 6.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}
