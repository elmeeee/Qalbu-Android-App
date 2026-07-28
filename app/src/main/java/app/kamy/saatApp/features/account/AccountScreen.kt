package app.kamy.saatApp.features.account

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.design.components.SaatInlineError
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.domain.adhan.AdhanVoice
import app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.saatApp.domain.adhan.FajrAdhanVoice
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.domain.model.QFTranslation
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.domain.prayer.PrayerMadhab
import app.kamy.saatApp.domain.prayer.PrayerMethodOption
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: (() -> Unit)? = null,
    onAccountDetailScreenChanged: (Boolean) -> Unit = {}
) {
    val vm: AccountViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val currentDetailScreen = rememberSaveable { mutableStateOf<String?>(null) }
    var showUpToDateSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(currentDetailScreen.value) {
        onAccountDetailScreenChanged(currentDetailScreen.value != null)
    }

    if (currentDetailScreen.value != null) {
        BackHandler {
            currentDetailScreen.value = null
        }
    }

    when (currentDetailScreen.value) {
        "READING_NOTIFICATION" -> {
            ReadingNotificationScreen(
                state = state,
                vm = vm,
                onBack = { currentDetailScreen.value = null }
            )
        }
        "NOTIFICATION_ADHAN" -> {
            NotificationAdhanScreen(
                state = state,
                vm = vm,
                onBack = { currentDetailScreen.value = null }
            )
        }
        "PRIVACY_POLICY" -> {
            PrivacyPolicyScreen(
                appLanguage = state.appLanguage,
                appTheme = state.appTheme,
                onBack = { currentDetailScreen.value = null }
            )
        }
        "TERMS_CONDITIONS" -> {
            TermsAndConditionsScreen(
                appLanguage = state.appLanguage,
                appTheme = state.appTheme,
                onBack = { currentDetailScreen.value = null }
            )
        }
        "ABOUT_SAAT" -> {
            AboutSaatScreen(
                appLanguage = state.appLanguage,
                appTheme = state.appTheme,
                onBack = { currentDetailScreen.value = null }
            )
        }
        else -> {
            AccountSettingsContent(
                state = state,
                vm = vm,
                onBack = onBack,
                onOpenReadingNotification = { currentDetailScreen.value = "READING_NOTIFICATION" },
                onOpenNotificationAdhan = { currentDetailScreen.value = "NOTIFICATION_ADHAN" },
                onOpenAbout = { currentDetailScreen.value = "ABOUT_SAAT" },
                onOpenPrivacyPolicy = { currentDetailScreen.value = "PRIVACY_POLICY" },
                onOpenTerms = { currentDetailScreen.value = "TERMS_CONDITIONS" },
                onCheckUpdate = { showUpToDateSheet = true }
            )
        }
    }

    val filteredTranslations = remember(state.translations, state.translatorQuery) {
        vm.filteredTranslations()
    }

    if (state.showTranslatorSheet) {
        TranslatorSheet(
            query = state.translatorQuery,
            selectedId = state.selectedTranslationId,
            translations = filteredTranslations,
            isLoading = state.translationsLoading,
            error = state.translationsError,
            onQueryChange = vm::setTranslatorQuery,
            onPick = { translation ->
                if (vm.selectTranslation(translation)) {
                    (context as? ComponentActivity)?.recreate()
                }
            },
            onDismiss = vm::closeTranslator,
            onRetry = vm::loadTranslations
        )
    }

    if (state.showNotifTimeSheet) {
        ReminderTimeSheet(
            hour = state.reminderHour,
            minute = state.reminderMinute,
            onSave = vm::saveReminderTime,
            onDismiss = { vm.toggleNotifTimeSheet(false) }
        )
    }

    if (state.showMadhabSheet) {
        MadhabSelectionSheet(
            selected = state.prayerMadhab,
            onSelect = vm::setPrayerMadhab,
            onDismiss = vm::closeMadhabSheet
        )
    }

    if (state.showPrayerSheet) {
        PrayerMethodSheet(
            selected = state.prayerMethod,
            methods = state.prayerMethods,
            isLoading = state.prayerMethodsLoading,
            error = state.prayerMethodsError,
            onSelect = vm::setPrayerMethod,
            onRetry = vm::loadPrayerMethods,
            onDismiss = { vm.togglePrayerSheet(false) }
        )
    }

    if (state.showAdhanSheet) {
        AdhanVoiceSheet(
            selected = state.selectedAdhanVoice,
            selectedFajr = state.selectedFajrVoice,
            previewingVoiceId = state.previewingAdhanVoiceId,
            onSelect = vm::selectAdhanVoice,
            onSelectFajr = vm::selectFajrVoice,
            onPreview = vm::toggleAdhanPreview,
            onPreviewFajr = vm::toggleFajrPreview,
            onDismiss = vm::closeAdhanSheet
        )
    }

    if (state.showLanguageSheet) {
        LanguageSheet(
            selected = state.appLanguage,
            onSelect = { language ->
                vm.setAppLanguage(language)
                (context as? ComponentActivity)?.recreate()
            },
            onDismiss = vm::closeLanguageSheet
        )
    }

    if (state.showSurahRemindersSheet) {
        SurahRemindersSheet(
            reminders = state.surahReminders,
            onToggle = vm::toggleSurahReminder,
            onAdd = vm::addSurahReminder,
            onUpdate = vm::updateSurahReminder,
            onDelete = vm::deleteSurahReminder,
            onDismiss = vm::closeSurahRemindersSheet
        )
    }

    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val appVersion = remember(packageInfo) {
        packageInfo?.versionName ?: "1.0.1"
    }

    if (showUpToDateSheet) {
        UpToDateSheet(
            appVersion = appVersion,
            onDismiss = { showUpToDateSheet = false }
        )
    }
}

// ─── Main Settings View (Mockup 1) ───────────────────────────────────────────

@Composable
private fun AccountSettingsContent(
    state: AccountUiState,
    vm: AccountViewModel,
    onBack: (() -> Unit)?,
    onOpenReadingNotification: () -> Unit,
    onOpenNotificationAdhan: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onCheckUpdate: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val appVersion = remember(packageInfo) {
        packageInfo?.versionName ?: "1.0.1"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        // Sticky Header bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF7F7F7),
            shadowElevation = 0.5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tabContentStatusBarInset()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color(0xFF1C1C1E)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = stringResource(R.string.settings_main_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1C1C1E)
                )
            }
        }

        // Scrollable Settings Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = floatingNavBottomPadding() + 24.dp
                )
        ) {
            // 1. General settings
            SettingsSectionHeader(stringResource(R.string.settings_section_general))
            SettingsCard {
                SettingsCustomRow(
                    iconRes = R.drawable.ic_language_custom,
                    title = stringResource(R.string.settings_item_language),
                    subtitle = stringResource(state.appLanguage.labelRes),
                    onClick = { vm.openLanguageSheet() },
                    showChevron = true,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_setting_quran_custom,
                    title = stringResource(R.string.settings_item_quran),
                    subtitle = stringResource(R.string.settings_item_advance_options),
                    onClick = onOpenReadingNotification,
                    showChevron = true,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_notification_custom,
                    title = stringResource(R.string.settings_item_notification),
                    subtitle = stringResource(R.string.settings_item_advance_options),
                    onClick = onOpenNotificationAdhan,
                    showChevron = true,
                    showDivider = false
                )
            }

            Spacer(Modifier.height(16.dp))

            // 2. Prayer Calculation
            SettingsSectionHeader(stringResource(R.string.settings_section_prayer_calc))
            SettingsCard {
                SettingsCustomRow(
                    iconRes = R.drawable.ic_madhab_custom,
                    title = stringResource(R.string.settings_item_madhab),
                    subtitle = stringResource(state.prayerMadhab.displayNameRes),
                    onClick = { vm.openMadhabSheet() },
                    showChevron = true,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_institution_custom,
                    title = stringResource(R.string.settings_item_institution),
                    subtitle = state.prayerMethod.organization,
                    onClick = { vm.togglePrayerSheet(true) },
                    showChevron = true,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_adhan_voice_custom,
                    title = stringResource(R.string.settings_item_adhan_voice),
                    subtitle = state.selectedAdhanVoice.displayName,
                    onClick = { vm.openAdhanSheet() },
                    showChevron = true,
                    showDivider = false
                )
            }

            Spacer(Modifier.height(16.dp))

            // 3. About Sāat
            SettingsSectionHeader(stringResource(R.string.settings_section_about_saat))
            SettingsCard {
                SettingsCustomRow(
                    iconRes = R.drawable.ic_about_custom,
                    title = stringResource(R.string.settings_item_about),
                    onClick = onOpenAbout,
                    showChevron = true,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_privacy_custom,
                    title = stringResource(R.string.settings_item_privacy),
                    onClick = onOpenPrivacyPolicy,
                    showChevron = true,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_terms_custom,
                    title = stringResource(R.string.settings_item_terms),
                    onClick = onOpenTerms,
                    showChevron = true,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_update_custom,
                    title = stringResource(R.string.settings_item_check_update),
                    subtitle = stringResource(R.string.settings_item_up_to_date),
                    onClick = onCheckUpdate,
                    showChevron = true,
                    showDivider = false
                )
            }

            Spacer(Modifier.height(24.dp))
            AppFooterCard(appVersion = appVersion)
            Spacer(Modifier.height(floatingNavBottomPadding()))
        }
    }
}

// ─── Notification Adhan Screen (Mockup 2) ───────────────────────────────────

@Composable
fun NotificationAdhanScreen(
    state: AccountUiState,
    vm: AccountViewModel,
    onBack: () -> Unit
) {
    var showTahajudTimePicker by remember { mutableStateOf(false) }
    var showDhuhaTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        // Sticky Header bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF7F7F7),
            shadowElevation = 0.5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tabContentStatusBarInset()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF1C1C1E)
                    )
                }
                Text(
                    text = stringResource(R.string.notif_adhan_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1C1C1E)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Section 1: Notification Adhan
            SettingsSectionHeader(stringResource(R.string.notif_section_adhan))
            SettingsCard {
                val fajrOn = state.isPrayerNotificationEnabled(PrayerType.FAJR)
                SettingsCustomRow(
                    iconRes = if (fajrOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_fajr),
                    subtitle = if (fajrOn) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = fajrOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.FAJR, it) },
                    showDivider = true
                )
                val dhuhrOn = state.isPrayerNotificationEnabled(PrayerType.DHUHR)
                SettingsCustomRow(
                    iconRes = if (dhuhrOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_dhuhr),
                    subtitle = if (dhuhrOn) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = dhuhrOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.DHUHR, it) },
                    showDivider = true
                )
                val asrOn = state.isPrayerNotificationEnabled(PrayerType.ASR)
                SettingsCustomRow(
                    iconRes = if (asrOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_asr),
                    subtitle = if (asrOn) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = asrOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.ASR, it) },
                    showDivider = true
                )
                val maghribOn = state.isPrayerNotificationEnabled(PrayerType.MAGHRIB)
                SettingsCustomRow(
                    iconRes = if (maghribOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_maghrib),
                    subtitle = if (maghribOn) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = maghribOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.MAGHRIB, it) },
                    showDivider = true
                )
                val ishaOn = state.isPrayerNotificationEnabled(PrayerType.ISHA)
                SettingsCustomRow(
                    iconRes = if (ishaOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_isha),
                    subtitle = if (ishaOn) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = ishaOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.ISHA, it) },
                    showDivider = false
                )
            }

            Spacer(Modifier.height(16.dp))

            // Section 2: Notification Reading
            SettingsSectionHeader(stringResource(R.string.notif_section_reading))
            SettingsCard {
                val activeCount = state.surahReminders.count { it.enabled }
                val surahSubtitle = if (state.yasinReminderEnabled) {
                    stringResource(R.string.surah_count_on_format, if (activeCount > 0) activeCount else 3)
                } else {
                    stringResource(R.string.state_off)
                }
                SettingsCustomRow(
                    iconRes = R.drawable.ic_remainders_custom,
                    title = stringResource(R.string.notif_surah_reminders),
                    subtitle = surahSubtitle,
                    onClick = { vm.openSurahRemindersSheet() },
                    checked = state.yasinReminderEnabled,
                    onCheckedChange = { vm.setYasinReminderEnabled(it) },
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_remainders_custom,
                    title = stringResource(R.string.notif_fasting_important_days),
                    subtitle = if (state.importantDaysReminderEnabled) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = state.importantDaysReminderEnabled,
                    onCheckedChange = vm::setImportantDaysReminderEnabled,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_remainders_custom,
                    title = stringResource(R.string.notif_mon_thu_fasting),
                    subtitle = if (state.monThuFastReminderEnabled) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = state.monThuFastReminderEnabled,
                    onCheckedChange = vm::setMonThuFastReminderEnabled,
                    showDivider = false
                )
            }

            Spacer(Modifier.height(16.dp))

            // Section 3: Other Prayer
            SettingsSectionHeader(stringResource(R.string.notif_section_other_prayer))
            SettingsCard {
                val tahajudSub = if (state.tahajudEnabled) "${state.tahajudTimeLabel} - ${stringResource(R.string.state_on)}" else stringResource(R.string.state_off)
                SettingsCustomRow(
                    iconRes = R.drawable.ic_remainders_custom,
                    title = stringResource(R.string.notif_tahajud_remainder),
                    subtitle = tahajudSub,
                    onClick = { showTahajudTimePicker = true },
                    checked = state.tahajudEnabled,
                    onCheckedChange = { isChecked ->
                        vm.setTahajudEnabled(isChecked)
                        if (isChecked) showTahajudTimePicker = true
                    },
                    showDivider = true
                )
                val dhuhaSub = if (state.dhuhaReminderEnabled) "${state.dhuhaTimeLabel} - ${stringResource(R.string.state_on)}" else stringResource(R.string.state_off)
                SettingsCustomRow(
                    iconRes = R.drawable.ic_remainders_custom,
                    title = stringResource(R.string.notif_dhuha_remainder),
                    subtitle = dhuhaSub,
                    onClick = { showDhuhaTimePicker = true },
                    checked = state.dhuhaReminderEnabled,
                    onCheckedChange = { isChecked ->
                        vm.setDhuhaReminderEnabled(isChecked)
                        if (isChecked) showDhuhaTimePicker = true
                    },
                    showDivider = false
                )
            }
            Spacer(Modifier.height(floatingNavBottomPadding()))
        }
    }

    if (showTahajudTimePicker) {
        ReminderTimeSheet(
            hour = state.tahajudHour,
            minute = state.tahajudMinute,
            onSave = { h, m ->
                vm.setTahajudTime(h, m)
                showTahajudTimePicker = false
            },
            onDismiss = { showTahajudTimePicker = false }
        )
    }

    if (showDhuhaTimePicker) {
        ReminderTimeSheet(
            hour = state.dhuhaHour,
            minute = state.dhuhaMinute,
            onSave = { h, m ->
                vm.setDhuhaTime(h, m)
                showDhuhaTimePicker = false
            },
            onDismiss = { showDhuhaTimePicker = false }
        )
    }
}

// ─── Reading Notification Screen (Mockup 3) ──────────────────────────────────

@Composable
fun ReadingNotificationScreen(
    state: AccountUiState,
    vm: AccountViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        // Sticky Header bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF7F7F7),
            shadowElevation = 0.5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tabContentStatusBarInset()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF1C1C1E)
                    )
                }
                Text(
                    text = stringResource(R.string.reading_notif_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1C1C1E)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Section 1: Reading Notification
            SettingsSectionHeader(stringResource(R.string.reading_section_reading_notif))
            SettingsCard {
                SettingsCustomRow(
                    iconRes = R.drawable.ic_daily_verse_custom,
                    title = stringResource(R.string.reading_daily_verse),
                    subtitle = if (state.dailyVerseEnabled) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = state.dailyVerseEnabled,
                    onCheckedChange = vm::setDailyVerseEnabled,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_daily_time_custom,
                    title = stringResource(R.string.reading_daily_verse_time),
                    subtitle = state.reminderTimeLabel.ifBlank { "07:00 AM" },
                    onClick = { vm.toggleNotifTimeSheet(true) },
                    showChevron = true,
                    showDivider = false
                )
            }

            Spacer(Modifier.height(16.dp))

            // Section 2: Writing
            SettingsSectionHeader(stringResource(R.string.reading_section_writing))
            SettingsCard {
                SettingsCustomRow(
                    iconRes = R.drawable.ic_latin_custom,
                    title = stringResource(R.string.reading_show_latin),
                    subtitle = if (state.showTransliteration) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = state.showTransliteration,
                    onCheckedChange = vm::setShowTransliteration,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_translator_custom,
                    title = stringResource(R.string.reading_show_translation),
                    subtitle = if (state.showTranslation) stringResource(R.string.state_shown) else stringResource(R.string.state_hidden),
                    checked = state.showTranslation,
                    onCheckedChange = vm::setShowTranslation,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_translator_custom,
                    title = stringResource(R.string.reading_translator),
                    subtitle = state.selectedTranslationName.ifBlank { LocalQuranConfig.translationForAppLanguage(state.appLanguage).authorName },
                    onClick = { vm.openTranslator() },
                    showChevron = true,
                    showDivider = false
                )
            }
            Spacer(Modifier.height(floatingNavBottomPadding()))
        }
    }
}

// ─── Settings Custom Components ─────────────────────────────────────────────

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        ),
        color = Color(0xFF8E8E93),
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsCustomRow(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    showChevron: Boolean = false,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null || onCheckedChange != null) {
                if (onClick != null) {
                    onClick()
                } else if (onCheckedChange != null && checked != null) {
                    onCheckedChange(!checked)
                }
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = title,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF1C1C1E)
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp
                    ),
                    color = Color(0xFF8E8E93)
                )
            }
        }

        if (checked != null && onCheckedChange != null) {
            Image(
                painter = painterResource(if (checked) R.drawable.ic_toggle_on_custom else R.drawable.ic_toggle_off_custom),
                contentDescription = if (checked) "On" else "Off",
                modifier = Modifier
                    .size(width = 52.dp, height = 28.dp)
                    .clickable(
                        enabled = true,
                        onClick = { onCheckedChange(!checked) }
                    )
            )
        } else if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFC7C7CC),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 54.dp),
            color = Color(0xFFF2F2F7),
            thickness = 1.dp
        )
    }
}

// ─── Revamped Bottom Sheets ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpToDateSheet(
    appVersion: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6F3EE)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_update_custom),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = stringResource(R.string.up_to_date_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.up_to_date_sheet_desc, appVersion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF085E43))
            ) {
                Text(
                    text = stringResource(R.string.got_it),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSheet(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.language_settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.values().forEach { lang ->
                    val isSelected = lang == selected
                    Surface(
                        onClick = { onSelect(lang) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        } else {
                            Color.Transparent
                        },
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        } else {
                            null
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(lang.labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MadhabSelectionSheet(
    selected: PrayerMadhab,
    onSelect: (PrayerMadhab) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_madhab_custom),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.madhab_settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrayerMadhab.values().forEach { madhab ->
                    val isSelected = madhab == selected
                    Surface(
                        onClick = { onSelect(madhab) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        } else {
                            Color.Transparent
                        },
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        } else {
                            null
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(madhab.displayNameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerMethodSheet(
    selected: PrayerCalculationMethod,
    methods: List<PrayerMethodOption>,
    isLoading: Boolean,
    error: AppError?,
    onSelect: (PrayerCalculationMethod) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_institution_custom),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.prayer_calculation_method),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                error != null -> {
                    val display = error.rememberErrorDisplay()
                    if (display != null) {
                        SaatInlineError(
                            display = display,
                            onRetry = onRetry
                        )
                    }
                }
                methods.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(methods) { methodOpt ->
                            val isSelected = methodOpt.method == selected
                            Surface(
                                onClick = { onSelect(methodOpt.method) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                } else {
                                    Color.Transparent
                                },
                                border = if (isSelected) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                } else {
                                    null
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = methodOpt.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                        if (methodOpt.organization.isNotBlank()) {
                                            Text(
                                                text = methodOpt.organization,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Text(
                                            text = "✓",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdhanVoiceSheet(
    selected: AdhanVoice,
    selectedFajr: FajrAdhanVoice,
    previewingVoiceId: String?,
    onSelect: (AdhanVoice) -> Unit,
    onSelectFajr: (FajrAdhanVoice) -> Unit,
    onPreview: (AdhanVoice) -> Unit,
    onPreviewFajr: (FajrAdhanVoice) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedTab by remember { mutableStateOf(0) }

    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_adhan_voice_custom),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.adhan_voice),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Segmented Tab for Regular Adhan vs Subuh Adhan
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val isRegularTab = selectedTab == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                color = if (isRegularTab) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Adzan Umum",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isRegularTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val isFajrTab = selectedTab == 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                color = if (isFajrTab) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Adzan Subuh",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFajrTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AdhanVoice.selectable) { voice ->
                        val isSelected = voice == selected
                        val isPreviewing = voice.id == previewingVoiceId
                        Surface(
                            onClick = { onSelect(voice) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                Color.Transparent
                            },
                            border = if (isSelected) {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            } else {
                                null
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = voice.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                        .clickable { onPreview(voice) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(if (isPreviewing) R.drawable.ic_pause else R.drawable.ic_play),
                                        contentDescription = "Preview",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FajrAdhanVoice.selectable) { voice ->
                        val isSelected = voice == selectedFajr
                        val isPreviewing = voice.id == previewingVoiceId
                        Surface(
                            onClick = { onSelectFajr(voice) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                Color.Transparent
                            },
                            border = if (isSelected) {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            } else {
                                null
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = voice.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                        .clickable { onPreviewFajr(voice) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(if (isPreviewing) R.drawable.ic_pause else R.drawable.ic_play),
                                        contentDescription = "Preview",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslatorSheet(
    query: String,
    selectedId: Int,
    translations: List<QFTranslation>,
    isLoading: Boolean,
    error: AppError?,
    onQueryChange: (String) -> Unit,
    onPick: (QFTranslation) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.choose_translator),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_translator_placeholder)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                error != null -> {
                    val display = error.rememberErrorDisplay()
                    if (display != null) {
                        SaatInlineError(
                            display = display,
                            onRetry = onRetry
                        )
                    }
                }
                translations.isEmpty() -> {
                    Text(
                        stringResource(R.string.no_translators),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(translations, key = { it.id }) { translation ->
                            val isSelected = translation.id == selectedId
                            Surface(
                                onClick = { onPick(translation) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                } else {
                                    Color.Transparent
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = translation.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${translation.authorName} · ${translation.languageName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Text(
                                            text = "✓",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeSheet(
    hour: Int,
    minute: Int,
    onSave: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val timeState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = false
    )
    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.reminder_time_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TimePicker(state = timeState)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onSave(timeState.hour, timeState.minute) }) {
                    Text(stringResource(R.string.save), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaatModalBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        content = content
    )
}

// ─── Legal Web Views & Info Screens ──────────────────────────────────────────

@Composable
fun AboutSaatScreen(
    appLanguage: AppLanguage,
    appTheme: app.kamy.saatApp.infrastructure.preferences.AppThemeColor,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                text = stringResource(R.string.settings_item_about),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LegalWebView(
            url = "https://elmee.my/saat/about?lang=${appLanguage.tag}&theme=${appTheme.key}",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PrivacyPolicyScreen(
    appLanguage: AppLanguage,
    appTheme: app.kamy.saatApp.infrastructure.preferences.AppThemeColor,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                text = stringResource(R.string.privacy_policy),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LegalWebView(
            url = "https://elmee.my/saat/privacy?lang=${appLanguage.tag}&theme=${appTheme.key}",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun TermsAndConditionsScreen(
    appLanguage: AppLanguage,
    appTheme: app.kamy.saatApp.infrastructure.preferences.AppThemeColor,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .tabContentStatusBarInset()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                text = stringResource(R.string.terms_and_conditions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LegalWebView(
            url = "https://elmee.my/saat/terms?lang=${appLanguage.tag}&theme=${appTheme.key}",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun LegalWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    settings.setSupportZoom(false)
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AppFooterCard(appVersion: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SaatColors.DeepEmerald
            )
            Text(
                text = stringResource(R.string.app_version_format, appVersion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
