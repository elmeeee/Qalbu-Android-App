package app.kamy.saatApp.features.account

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.ViewCompat
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import coil.compose.AsyncImage
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.kamy.saatApp.domain.model.AuthUser
import app.kamy.saatApp.domain.model.QuranReadingStyle
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import app.kamy.saatApp.R
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.locale.AppLanguage
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import app.kamy.saatApp.design.components.SaatInlineError
import app.kamy.saatApp.design.theme.SaatColors
import app.kamy.saatApp.design.theme.SaatSpacing
import app.kamy.saatApp.domain.adhan.AdhanVoice
import app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.saatApp.domain.adhan.FajrAdhanVoice
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.domain.model.QuranTranslation
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.domain.prayer.PrayerMadhab
import app.kamy.saatApp.domain.prayer.PrayerMethodOption
import app.kamy.saatApp.infrastructure.defaults.CountryDefaultsProvider
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.ui.common.rememberErrorDisplay
import app.kamy.saatApp.ui.layout.floatingNavBottomPadding
import app.kamy.saatApp.ui.layout.tabContentStatusBarInset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: (() -> Unit)? = null,
    onAccountDetailScreenChanged: (Boolean) -> Unit = {}
) {
    val vm: AccountViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentDetailScreen = rememberSaveable { mutableStateOf<String?>(null) }
    var showUpToDateSheet by rememberSaveable { mutableStateOf(false) }
    val mainScrollState = rememberScrollState()

    LaunchedEffect(currentDetailScreen.value) {
        onAccountDetailScreenChanged(currentDetailScreen.value != null)
    }

    if (currentDetailScreen.value != null) {
        BackHandler {
            currentDetailScreen.value = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AccountSettingsContent(
            state = state,
            vm = vm,
            scrollState = mainScrollState,
            onBack = onBack,
            onOpenAccountDetail = { currentDetailScreen.value = "ACCOUNT_DETAIL" },
            onOpenNotificationAdhan = { currentDetailScreen.value = "NOTIFICATION_ADHAN" },
            onOpenAbout = { currentDetailScreen.value = "ABOUT_SAAT" },
            onOpenPrivacyPolicy = { currentDetailScreen.value = "PRIVACY_POLICY" },
            onOpenTerms = { currentDetailScreen.value = "TERMS_CONDITIONS" },
            onCheckUpdate = { showUpToDateSheet = true }
        )

        when (currentDetailScreen.value) {
            "ACCOUNT_DETAIL" -> {
                AccountDetailScreen(
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
                vm.selectTranslation(translation)
            },
            onDismiss = vm::closeTranslator,
            onRetry = vm::loadTranslations
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
            onSelect = vm::setAppLanguage,
            onDismiss = vm::closeLanguageSheet
        )
    }

    if (state.showReadingStyleSheet) {
        QuranReadingStyleSheet(
            selected = state.readingStyle,
            onSelect = vm::setReadingStyle,
            onDismiss = vm::closeReadingStyleSheet
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

    LaunchedEffect(state.authSuccessMessage) {
        state.authSuccessMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            vm.dismissAuthSuccessMessage()
        }
    }

    if (state.showEmailAuthSheet) {
        EmailAuthBottomSheet(
            mode = state.emailAuthMode,
            isLoading = state.isAuthLoading,
            errorMessage = state.authErrorMessage,
            onModeChange = vm::setEmailAuthMode,
            onSignIn = vm::signInWithEmail,
            onSignUp = vm::signUpWithEmail,
            onForgotPassword = vm::sendPasswordReset,
            onDismiss = vm::closeEmailAuth,
            onDismissError = vm::dismissAuthError
        )
    }
}

// ─── Main Settings View (Mockup 1) ───────────────────────────────────────────

@Composable
private fun AccountSettingsContent(
    state: AccountUiState,
    vm: AccountViewModel,
    scrollState: ScrollState,
    onBack: (() -> Unit)?,
    onOpenAccountDetail: () -> Unit,
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
            .background(SaatColors.HomeBg)
    ) {
        // Clean Header bar
        Column(
            modifier = Modifier
                .tabContentStatusBarInset()
                .padding(
                    horizontal = SaatSpacing.screenHorizontal,
                    vertical = SaatSpacing.md
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = stringResource(R.string.settings_main_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(R.string.settings_main_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = if (onBack != null) 58.dp else 18.dp, top = 4.dp)
            )
            Spacer(Modifier.height(SaatSpacing.sm))
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(48.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
        }

        // Scrollable Settings Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = floatingNavBottomPadding() + 24.dp
                )
        ) {
            // 0. Account & Sync Card (Optional)
            if (state.authUser == null) {
                AccountSyncAuthCard(
                    isLoading = state.isAuthLoading,
                    onSignIn = { vm.signInWithGoogle(context) },
                    onOpenEmailAuth = { vm.openEmailAuth() }
                )
            } else {
                AccountSyncProfileCard(
                    user = state.authUser!!,
                    isLoading = state.isAuthLoading,
                    isSyncing = state.isSyncing,
                    lastSyncTimeMillis = state.lastSyncTimeMillis,
                    onSyncNow = { vm.syncNow() },
                    onClick = onOpenAccountDetail
                )
            }

            state.syncErrorMessage?.let { errMsg ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB91C1C),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { vm.dismissSyncError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFB91C1C),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            state.authErrorMessage?.let { errMsg ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB91C1C),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { vm.dismissAuthError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFB91C1C),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

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
                    iconRes = R.drawable.ic_translator_custom,
                    title = stringResource(R.string.reading_translator),
                    subtitle = state.selectedTranslationName.ifBlank { LocalQuranConfig.translationForAppLanguage(state.appLanguage).authorName },
                    onClick = { vm.openTranslator() },
                    showChevron = true,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_quran_on,
                    iconTint = Color(0xFF1C1C1E),
                    title = stringResource(R.string.reading_style_title),
                    subtitle = stringResource(
                        if (state.readingStyle == QuranReadingStyle.SCROLL) R.string.reading_style_scroll
                        else R.string.reading_style_pager
                    ),
                    onClick = { vm.openReadingStyleSheet() },
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

            // 3. Ramadan & 10 Last Nights Simulation (Debug / Testing Only)
            if (app.kamy.saatApp.BuildConfig.DEBUG) {
                Spacer(Modifier.height(16.dp))
                SettingsSectionHeader(stringResource(R.string.settings_section_ramadan_testing))
                SettingsCard {
                    SettingsCustomRow(
                        iconRes = R.drawable.taraweh_icon,
                        title = stringResource(R.string.settings_force_ramadan_title),
                        subtitle = stringResource(R.string.settings_force_ramadan_desc),
                        checked = state.forceRamadanMode,
                        onCheckedChange = vm::setForceRamadanMode,
                        showDivider = true
                    )
                    SettingsCustomRow(
                        iconRes = R.drawable.ramadan_quran_goal,
                        title = stringResource(R.string.settings_force_ten_nights_title),
                        subtitle = stringResource(R.string.settings_force_ten_nights_desc),
                        checked = state.forceTenLastNights,
                        onCheckedChange = vm::setForceTenLastNights,
                        showDivider = false
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. About Sāat
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

            Spacer(Modifier.height(16.dp))
            AppFooterCard(appVersion = appVersion)
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
            .background(SaatColors.HomeBg)
    ) {
        // Sticky Header bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SaatColors.HomeBg,
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
                    subtitle = if (fajrOn) stringResource(R.string.state_on) else stringResource(R.string.notif_off_default_sound_hint),
                    checked = fajrOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.FAJR, it) },
                    showDivider = true
                )
                val dhuhrOn = state.isPrayerNotificationEnabled(PrayerType.DHUHR)
                SettingsCustomRow(
                    iconRes = if (dhuhrOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_dhuhr),
                    subtitle = if (dhuhrOn) stringResource(R.string.state_on) else stringResource(R.string.notif_off_default_sound_hint),
                    checked = dhuhrOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.DHUHR, it) },
                    showDivider = true
                )
                val asrOn = state.isPrayerNotificationEnabled(PrayerType.ASR)
                SettingsCustomRow(
                    iconRes = if (asrOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_asr),
                    subtitle = if (asrOn) stringResource(R.string.state_on) else stringResource(R.string.notif_off_default_sound_hint),
                    checked = asrOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.ASR, it) },
                    showDivider = true
                )
                val maghribOn = state.isPrayerNotificationEnabled(PrayerType.MAGHRIB)
                SettingsCustomRow(
                    iconRes = if (maghribOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_maghrib),
                    subtitle = if (maghribOn) stringResource(R.string.state_on) else stringResource(R.string.notif_off_default_sound_hint),
                    checked = maghribOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.MAGHRIB, it) },
                    showDivider = true
                )
                val ishaOn = state.isPrayerNotificationEnabled(PrayerType.ISHA)
                SettingsCustomRow(
                    iconRes = if (ishaOn) R.drawable.ic_adhan_on_custom else R.drawable.ic_adhan_off_custom,
                    title = stringResource(R.string.prayer_isha),
                    subtitle = if (ishaOn) stringResource(R.string.state_on) else stringResource(R.string.notif_off_default_sound_hint),
                    checked = ishaOn,
                    onCheckedChange = { vm.setPrayerNotificationEnabled(PrayerType.ISHA, it) },
                    showDivider = false
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.notif_adhan_section_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Section: Live Prayer Countdown
            SettingsSectionHeader(stringResource(R.string.channel_live_prayer_countdown))
            SettingsCard {
                SettingsCustomRow(
                    iconRes = R.drawable.ic_notification_custom,
                    title = stringResource(R.string.setting_live_prayer_countdown),
                    subtitle = if (state.liveCountdownEnabled) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = state.liveCountdownEnabled,
                    onCheckedChange = vm::setLiveCountdownEnabled,
                    showDivider = false
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.setting_live_prayer_countdown_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Section 2: Notification Reading
            SettingsSectionHeader(stringResource(R.string.notif_section_reading))
            SettingsCard {
                SettingsCustomRow(
                    iconRes = R.drawable.ic_daily_verse_custom,
                    title = stringResource(R.string.reading_daily_verse),
                    subtitle = if (state.dailyVerseEnabled) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = state.dailyVerseEnabled,
                    onCheckedChange = vm::setDailyVerseEnabled,
                    showDivider = true
                )
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
                    title = stringResource(R.string.quran_reminder_setting_title),
                    subtitle = if (state.quranReminderEnabled) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = state.quranReminderEnabled,
                    onCheckedChange = vm::setQuranReminderEnabled,
                    showDivider = true
                )
                SettingsCustomRow(
                    iconRes = R.drawable.ic_remainders_custom,
                    title = stringResource(R.string.notif_fasting_important_days),
                    subtitle = if (state.importantDaysReminderEnabled) stringResource(R.string.state_on) else stringResource(R.string.state_off),
                    checked = state.importantDaysReminderEnabled,
                    onCheckedChange = vm::setImportantDaysReminderEnabled,
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
                val dhuhaSub = if (state.dhuhaReminderEnabled) stringResource(R.string.state_on) else stringResource(R.string.state_off)
                SettingsCustomRow(
                    iconRes = R.drawable.ic_remainders_custom,
                    title = stringResource(R.string.notif_dhuha_remainder),
                    subtitle = dhuhaSub,
                    checked = state.dhuhaReminderEnabled,
                    onCheckedChange = { isChecked ->
                        vm.setDhuhaReminderEnabled(isChecked)
                    },
                    showDivider = false
                )
            }

            if (app.kamy.saatApp.BuildConfig.DEBUG) {
                Spacer(Modifier.height(16.dp))
                SettingsSectionHeader("DEBUG ALARM TESTS")
                SettingsCard {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    SettingsCustomRow(
                        iconRes = R.drawable.ic_remainders_custom,
                        title = "Test Tahajjud Alarm Intent",
                        subtitle = "Play alarm audio & full-screen overlay",
                        onClick = {
                            app.kamy.saatApp.infrastructure.audio.AdhanPlaybackService.start(
                                context = context,
                                rawRes = R.raw.tahajud_alarm,
                                title = "Sholat Tahajjud",
                                body = "Waktunya Bangun Sholat Tahajjud",
                                notificationId = 9991,
                                prayerName = "tahajud"
                            )
                        },
                        showDivider = true
                    )
                    SettingsCustomRow(
                        iconRes = R.drawable.ic_adhan_on_custom,
                        title = "Test Subuh Adhan Intent",
                        subtitle = "Play Subuh adhan audio & full-screen overlay",
                        onClick = {
                            val store = app.kamy.saatApp.infrastructure.preferences.AdhanPreferencesStore.from(context)
                            val voice = store.currentVoice()
                            val fajrVoice = store.currentFajrVoice()
                            val rawRes = app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog.rawResForPrayer("Subuh", voice, fajrVoice)
                            app.kamy.saatApp.infrastructure.audio.AdhanPlaybackService.start(
                                context = context,
                                rawRes = rawRes,
                                title = "Adhan Subuh",
                                body = "Waktunya Sholat Subuh",
                                notificationId = 9992,
                                prayerName = "Subuh"
                            )
                        },
                        showDivider = true
                    )
                    SettingsCustomRow(
                        iconRes = R.drawable.ic_adhan_on_custom,
                        title = "Test Dzuhur Adhan Intent",
                        subtitle = "Play Dzuhur adhan audio & full-screen overlay",
                        onClick = {
                            val store = app.kamy.saatApp.infrastructure.preferences.AdhanPreferencesStore.from(context)
                            val voice = store.currentVoice()
                            val fajrVoice = store.currentFajrVoice()
                            val rawRes = app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog.rawResForPrayer("Dzuhur", voice, fajrVoice)
                            app.kamy.saatApp.infrastructure.audio.AdhanPlaybackService.start(
                                context = context,
                                rawRes = rawRes,
                                title = "Adhan Dzuhur",
                                body = "Waktunya Sholat Dzuhur",
                                notificationId = 9993,
                                prayerName = "Dzuhur"
                            )
                        },
                        showDivider = false
                    )
                }
            }
            Spacer(Modifier.height(floatingNavBottomPadding()))
        }
    }

    if (showTahajudTimePicker) {
        ReminderTimeSheet(
            reminderType = ReminderType.TAHAJUD,
            hour = state.tahajudHour,
            minute = state.tahajudMinute,
            initialDays = state.tahajudDays,
            onSave = { h, m, days ->
                vm.setTahajudTime(h, m, days)
                showTahajudTimePicker = false
            },
            onDismiss = { showTahajudTimePicker = false }
        )
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
    showDivider: Boolean = true,
    iconTint: Color? = null
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
            modifier = Modifier.size(24.dp),
            colorFilter = iconTint?.let { ColorFilter.tint(it) }
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
    val scope = rememberCoroutineScope()
    var applyingLang by remember { mutableStateOf<AppLanguage?>(null) }

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
                AppLanguage.entries.forEach { lang ->
                    val isSelected = lang == (applyingLang ?: selected)
                    val isApplying = lang == applyingLang
                    val flagRes = when (lang) {
                        AppLanguage.INDONESIAN -> R.drawable.ic_flag_id
                        AppLanguage.ENGLISH -> R.drawable.ic_flag_en
                        AppLanguage.MALAY -> R.drawable.ic_flag_ms
                    }
                    Surface(
                        onClick = {
                            if (lang != selected && applyingLang == null) {
                                applyingLang = lang
                                scope.launch {
                                    kotlinx.coroutines.delay(200)
                                    onSelect(lang)
                                    kotlinx.coroutines.delay(100)
                                    sheetState.hide()
                                    onDismiss()
                                }
                            } else if (lang == selected && applyingLang == null) {
                                scope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            SaatColors.DeepEmerald.copy(alpha = 0.08f)
                        } else {
                            SaatColors.LightGrey.copy(alpha = 0.4f)
                        },
                        border = if (isSelected) {
                            BorderStroke(1.5.dp, SaatColors.DeepEmerald)
                        } else {
                            BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF8F4F7)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(flagRes),
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = stringResource(lang.labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) SaatColors.DeepEmerald else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            if (isApplying) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SaatColors.DeepEmerald,
                                    strokeWidth = 2.dp
                                )
                            } else if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(SaatColors.DeepEmerald),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check_custom),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
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
private fun QuranReadingStyleSheet(
    selected: QuranReadingStyle,
    onSelect: (QuranReadingStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.reading_style_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.reading_style_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = SaatColors.Slate500
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuranReadingStyle.entries.forEach { style ->
                    val isSelected = style == selected
                    val (titleRes, descRes) = when (style) {
                        QuranReadingStyle.PAGER -> Pair(R.string.reading_style_pager, R.string.reading_style_pager_desc)
                        QuranReadingStyle.SCROLL -> Pair(R.string.reading_style_scroll, R.string.reading_style_scroll_desc)
                    }

                    Surface(
                        onClick = {
                            scope.launch {
                                onSelect(style)
                                sheetState.hide()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            SaatColors.DeepEmerald.copy(alpha = 0.08f)
                        } else {
                            SaatColors.LightGrey.copy(alpha = 0.4f)
                        },
                        border = if (isSelected) {
                            BorderStroke(1.5.dp, SaatColors.DeepEmerald)
                        } else {
                            BorderStroke(1.dp, SaatColors.SoftGrey.copy(alpha = 0.6f))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) SaatColors.DeepEmerald.copy(alpha = 0.15f)
                                        else Color(0xFFF8F4F7)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (style == QuranReadingStyle.PAGER) R.drawable.ic_quran_on
                                        else R.drawable.ic_tafsir
                                    ),
                                    contentDescription = null,
                                    tint = if (isSelected) SaatColors.DeepEmerald else SaatColors.Slate500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(titleRes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) SaatColors.DeepEmerald else SaatColors.Slate900
                                )
                                Text(
                                    text = stringResource(descRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SaatColors.Slate500
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(SaatColors.DeepEmerald),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check_custom),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
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
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(madhab.iconRes),
                                    contentDescription = stringResource(madhab.displayNameRes),
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = stringResource(madhab.displayNameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
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
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val filteredMethods = remember(methods, searchQuery) {
        if (searchQuery.isBlank()) {
            methods
        } else {
            val q = searchQuery.trim().lowercase()
            methods.filter {
                it.name.lowercase().contains(q) ||
                it.organization.lowercase().contains(q) ||
                it.countryName.lowercase().contains(q) ||
                it.apiKey.lowercase().contains(q)
            }
        }
    }

    LaunchedEffect(selected, methods) {
        val index = methods.indexOfFirst { it.method == selected }
        if (index > 0) {
            listState.animateScrollToItem(index)
        }
    }

    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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

            if (methods.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_institution_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    )
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
                filteredMethods.isNotEmpty() -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMethods) { methodOpt ->
                            val isSelected = methodOpt.method == selected
                            Surface(
                                onClick = { onSelect(methodOpt.method) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                },
                                border = if (isSelected) {
                                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(methodOpt.iconRes),
                                            contentDescription = methodOpt.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Spacer(Modifier.width(14.dp))

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
                                        if (methodOpt.countryName.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text(
                                                    text = methodOpt.countryName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "✓",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.search_no_results),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            text = stringResource(R.string.adhan_tab_general),
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
                            text = stringResource(R.string.adhan_tab_fajr),
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
                        .height(340.dp),
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
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(voice.avatarRes),
                                        contentDescription = voice.displayName,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = voice.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
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
                        .height(340.dp),
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
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(voice.avatarRes),
                                        contentDescription = voice.displayName,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = voice.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
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
    translations: List<QuranTranslation>,
    isLoading: Boolean,
    error: AppError?,
    onQueryChange: (String) -> Unit,
    onPick: (QuranTranslation) -> Unit,
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
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(translations, key = { it.id }) { translation ->
                            val isSelected = translation.id == selectedId
                            val avatarRes = LocalQuranConfig.translationAvatar(translation.id)
                            Surface(
                                onClick = { onPick(translation) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                } else {
                                    Color.Transparent
                                },
                                border = if (isSelected) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(avatarRes),
                                            contentDescription = translation.name,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = translation.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${translation.authorName} · ${translation.languageName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Text(
                                            text = "✓",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp)
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

private enum class ReminderType {
    TAHAJUD,
    DHUHA,
    DAILY_VERSE
}

private data class QuickPresetTime(
    val hour: Int,
    val minute: Int,
    val label: String
)

private data class DayOption(
    val calendarDay: Int,
    val labelEn: String,
    val labelId: String,
    val labelMs: String
)

private val WEEK_DAYS = listOf(
    DayOption(java.util.Calendar.MONDAY, "Mon", "Sen", "Isn"),
    DayOption(java.util.Calendar.TUESDAY, "Tue", "Sel", "Sel"),
    DayOption(java.util.Calendar.WEDNESDAY, "Wed", "Rab", "Rab"),
    DayOption(java.util.Calendar.THURSDAY, "Thu", "Kam", "Kha"),
    DayOption(java.util.Calendar.FRIDAY, "Fri", "Jum", "Jum"),
    DayOption(java.util.Calendar.SATURDAY, "Sat", "Sab", "Sab"),
    DayOption(java.util.Calendar.SUNDAY, "Sun", "Min", "Aha")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeSheet(
    reminderType: ReminderType = ReminderType.TAHAJUD,
    hour: Int,
    minute: Int,
    initialDays: Set<Int> = (1..7).toSet(),
    onSave: (Int, Int, Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var currentHour by remember { mutableIntStateOf(hour) }
    var currentMinute by remember { mutableIntStateOf(minute) }
    var selectedDays by remember { mutableStateOf(if (initialDays.isEmpty()) (1..7).toSet() else initialDays) }

    val context = LocalContext.current
    val currentLang = remember(context) {
        app.kamy.saatApp.infrastructure.preferences.AppLanguageStore.from(context).current()
    }
    val isIndo = currentLang == AppLanguage.INDONESIAN
    val isMalay = currentLang == AppLanguage.MALAY

    val title = when (reminderType) {
        ReminderType.TAHAJUD -> if (isIndo) "Pengingat Tahajud" else if (isMalay) "Peringatan Tahajud" else "Tahajud Reminder"
        ReminderType.DHUHA -> if (isIndo) "Pengingat Duha" else if (isMalay) "Peringatan Dhuha" else "Dhuha Reminder"
        ReminderType.DAILY_VERSE -> if (isIndo) "Pengingat Ayat Harian" else if (isMalay) "Peringatan Ayat Harian" else "Daily Verse Reminder"
    }

    val subtitle = when (reminderType) {
        ReminderType.TAHAJUD -> if (isIndo) "Atur jam & hari pengingat sepertiga malam" else if (isMalay) "Tetapkan masa & hari sepertiga malam" else "Set time & days for last third of night"
        ReminderType.DHUHA -> if (isIndo) "Atur jam & hari pengingat shalat Duha" else if (isMalay) "Tetapkan masa & hari solat Dhuha" else "Set time & days for Dhuha prayer"
        ReminderType.DAILY_VERSE -> if (isIndo) "Atur jam & hari baca Al-Qur'an harian" else if (isMalay) "Tetapkan masa & hari membaca Al-Quran" else "Set time & days for daily Quran verse"
    }

    val presets = when (reminderType) {
        ReminderType.TAHAJUD -> listOf(
            QuickPresetTime(2, 30, if (isIndo || isMalay) "02:30 • Awal Malam" else "02:30 • Early Night"),
            QuickPresetTime(3, 0, if (isIndo || isMalay) "03:00 • 1/3 Malam" else "03:00 • Last Third"),
            QuickPresetTime(3, 30, if (isIndo || isMalay) "03:30 • Waktu Utama" else "03:30 • Optimal"),
            QuickPresetTime(4, 0, if (isIndo || isMalay) "04:00 • Sblm Subuh" else "04:00 • Before Fajr"),
            QuickPresetTime(4, 30, if (isIndo || isMalay) "04:30 • Akhir Malam" else "04:30 • Late Night")
        )
        ReminderType.DHUHA -> listOf(
            QuickPresetTime(7, 30, if (isIndo || isMalay) "07:30 • Awal Duha" else "07:30 • Early Dhuha"),
            QuickPresetTime(8, 30, if (isIndo || isMalay) "08:30 • Waktu Utama" else "08:30 • Optimal"),
            QuickPresetTime(9, 30, if (isIndo || isMalay) "09:30 • Pertengahan" else "09:30 • Mid Dhuha")
        )
        ReminderType.DAILY_VERSE -> listOf(
            QuickPresetTime(6, 30, if (isIndo || isMalay) "06:30 • Pagi Hari" else "06:30 • Morning"),
            QuickPresetTime(12, 30, if (isIndo || isMalay) "12:30 • Siang Hari" else "12:30 • Midday"),
            QuickPresetTime(18, 30, if (isIndo || isMalay) "18:30 • Petang" else "18:30 • Evening")
        )
    }

    val repeatDaysHeader = if (isIndo || isMalay) "Ulangi Pada Hari" else "Repeat On Days"
    val typeTimeHeader = if (isIndo || isMalay) "Ketik Jam & Menit" else "Type Hour & Minute"

    val daysSummary = remember(selectedDays, currentLang) {
        if (selectedDays.size == 7) {
            if (isIndo || isMalay) "Setiap Hari" else "Every Day"
        } else if (selectedDays.isEmpty()) {
            if (isIndo) "Tidak Ada Hari" else if (isMalay) "Tiada Hari" else "No Days"
        } else {
            WEEK_DAYS.filter { it.calendarDay in selectedDays }.joinToString(", ") {
                if (isIndo) it.labelId else if (isMalay) it.labelMs else it.labelEn
            }
        }
    }

    SaatModalBottomSheet(onDismiss, sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Subtitle
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Formatted Time Display Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = String.format(java.util.Locale.getDefault(), "%02d:%02d", currentHour, currentMinute),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.width(10.dp))
                val tzAbbrev = remember {
                    val tz = java.util.TimeZone.getDefault()
                    tz.getDisplayName(tz.inDaylightTime(java.util.Date()), java.util.TimeZone.SHORT, java.util.Locale.getDefault())
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = tzAbbrev,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            // Quick Preset Selection Chips (Horizontal Scrollable, Single Line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = currentHour == preset.hour && currentMinute == preset.minute
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                currentHour = preset.hour
                                currentMinute = preset.minute
                            }
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
            }

            // Days of the Week Selection Row (Mon - Sun)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = repeatDaysHeader,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = daysSummary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WEEK_DAYS.forEach { day ->
                        val isDaySelected = day.calendarDay in selectedDays
                        val dayLabel = if (isIndo) day.labelId else if (isMalay) day.labelMs else day.labelEn

                        Surface(
                            shape = CircleShape,
                            color = if (isDaySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isDaySelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    selectedDays = if (isDaySelected) {
                                        if (selectedDays.size > 1) selectedDays - day.calendarDay else selectedDays
                                    } else {
                                        selectedDays + day.calendarDay
                                    }
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDaySelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSave(currentHour, currentMinute, selectedDays) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        stringResource(R.string.save),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    val context = LocalContext.current
    val packageInfo = remember(context) {
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

        val langTag = appLanguage.tag.ifBlank { "id" }
        LegalWebView(
            url = "https://elmee.my/saat/about?lang=${langTag}&version=${appVersion}",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
fun PrivacyPolicyScreen(
    appLanguage: AppLanguage,
    appTheme: app.kamy.saatApp.infrastructure.preferences.AppThemeColor,
    onBack: () -> Unit
) {
    val langTag = appLanguage.tag.ifBlank { "id" }
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
            url = "https://elmee.my/saat/privacy?lang=${langTag}",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
fun TermsAndConditionsScreen(
    appLanguage: AppLanguage,
    appTheme: app.kamy.saatApp.infrastructure.preferences.AppThemeColor,
    onBack: () -> Unit
) {
    val langTag = appLanguage.tag.ifBlank { "id" }
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
            url = "https://elmee.my/saat/terms?lang=${langTag}",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun LegalWebView(
    url: String,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }

    val sanitizedUrl = remember(url) {
        if (url.contains("lang=&") || url.endsWith("lang=")) {
            url.replace("lang=", "lang=id")
        } else {
            url
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    isVerticalScrollBarEnabled = true
                    overScrollMode = android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    settings.setSupportZoom(false)
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                    loadUrl(sanitizedUrl)
                }
            },
            update = { _ -> },
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
    val context = LocalContext.current
    val haptic = app.kamy.saatApp.ui.feedback.rememberTapHaptic()
    val targetUrl = "https://masjidku.app?ref=saat_android"

    val isIndonesiaUser = remember(context) {
        val locationStore = LocationPreferencesStore.from(context)
        val activeCC = locationStore.activeCountryCode()
        if (!activeCC.isNullOrBlank()) {
            activeCC.equals("ID", ignoreCase = true) || activeCC.equals("IDN", ignoreCase = true)
        } else {
            val detected = CountryDefaultsProvider.detectCountryCode(context)
                ?: Locale.getDefault().country
            detected.equals("ID", ignoreCase = true) || detected.equals("IDN", ignoreCase = true)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Centered Collaboration with Masjidku (Only shown for Indonesian users)
            if (isIndonesiaUser) {
                Surface(
                    onClick = {
                        haptic()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                        runCatching { context.startActivity(intent) }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.collab_with),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_masjidku_logo),
                            contentDescription = "Masjidku",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(7.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Masjidku",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaatColors.DeepEmerald
                        )
                    }

                    Text(
                        text = stringResource(R.string.collab_masjidku_sub),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
                }
            }

            // App Tagline & Version
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SaatColors.DeepEmerald,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.app_version_format, appVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AccountSyncAuthCard(
    isLoading: Boolean,
    onSignIn: () -> Unit,
    onOpenEmailAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        border = BorderStroke(1.dp, Color(0xFFE8E2D2)),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDF5F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudSync,
                        contentDescription = null,
                        tint = SaatColors.HomeDarkGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.account_sync_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = SaatColors.HomeDarkGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.account_sync_desc),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Google Sign-In
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isLoading, onClick = onSignIn),
                shape = RoundedCornerShape(12.dp),
                color = SaatColors.PureWhite,
                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = SaatColors.HomeDarkGreen,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.account_connecting),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = Color(0xFF334155)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_google_logo),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.account_sign_in_google),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Or divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text(
                    text = stringResource(R.string.account_or_divider),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Email Sign-In / Register
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isLoading, onClick = onOpenEmailAuth),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mail,
                        contentDescription = null,
                        tint = SaatColors.HomeDarkGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.account_sign_in_email),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF1E293B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.account_sign_in_optional),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp
                ),
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AccountSyncProfileCard(
    user: AuthUser,
    isLoading: Boolean,
    isSyncing: Boolean,
    lastSyncTimeMillis: Long?,
    onSyncNow: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = SaatColors.PureWhite,
        border = BorderStroke(1.dp, Color(0xFFE8E2D2)),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!user.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, SaatColors.DeepEmerald.copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEDF5F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName?.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaatColors.HomeDarkGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName ?: stringResource(R.string.account_connected_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!user.email.isNullOrBlank()) {
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Hint & Quick Sync Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.account_profile_click_hint),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF047857)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEDF5F0),
                    modifier = Modifier.clickable(
                        enabled = !isSyncing && !isLoading,
                        onClick = onSyncNow
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(11.dp),
                                color = SaatColors.HomeDarkGreen,
                                strokeWidth = 1.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = SaatColors.HomeDarkGreen,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSyncing) stringResource(R.string.account_syncing) else stringResource(R.string.account_sync_now),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SaatColors.HomeDarkGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountDetailScreen(
    state: AccountUiState,
    vm: AccountViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user = state.authUser

    LaunchedEffect(user) {
        if (user == null) {
            onBack()
        }
    }

    if (user == null) return

    var displayNameInput by rememberSaveable(user.uid, user.displayName) {
        mutableStateOf(user.displayName ?: "")
    }
    val hasNameChanged = remember(displayNameInput, user.displayName) {
        displayNameInput.trim() != (user.displayName ?: "").trim() && displayNameInput.isNotBlank()
    }

    var showSignOutSheet by rememberSaveable { mutableStateOf(false) }
    var showDeleteSheet by rememberSaveable { mutableStateOf(false) }

    if (showSignOutSheet) {
        SignOutBottomSheet(
            isLoading = state.isAuthLoading,
            onConfirmSignOut = {
                showSignOutSheet = false
                vm.signOut(context)
            },
            onDismiss = { showSignOutSheet = false }
        )
    }

    if (showDeleteSheet) {
        DeleteAccountBottomSheet(
            isLoading = state.isAuthLoading,
            onConfirmDelete = {
                showDeleteSheet = false
                vm.deleteAccount(context)
            },
            onDismiss = { showDeleteSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaatColors.HomeBg)
    ) {
        // Sticky Header bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SaatColors.HomeBg,
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
                    text = stringResource(R.string.account_detail_title),
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
                .padding(bottom = 32.dp)
        ) {
            // 1. Profile Hero Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SaatColors.PureWhite,
                border = BorderStroke(1.dp, Color(0xFFE8E2D2)),
                shadowElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!user.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.dp, SaatColors.DeepEmerald.copy(alpha = 0.4f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDF5F0))
                                .border(2.dp, Color(0xFFA7F3D0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.displayName?.firstOrNull()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaatColors.HomeDarkGreen
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = user.displayName ?: stringResource(R.string.account_connected_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    if (!user.email.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.account_status_synced),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 2. Section: Informasi Profil (Editable)
            SettingsSectionHeader(stringResource(R.string.account_detail_section_profile))
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.account_name_label),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF334155)
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = displayNameInput,
                        onValueChange = { displayNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.account_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (hasNameChanged) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SaatColors.DeepEmerald,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .clickable(
                                            enabled = !state.isAuthLoading,
                                            onClick = { vm.updateProfile(displayNameInput) }
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (state.isAuthLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                color = Color.White,
                                                strokeWidth = 1.5.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(R.string.account_detail_save_changes),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = SaatColors.DeepEmerald,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.account_email_label),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF334155)
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = user.email ?: "-",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Mail,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFECFDF5)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.account_detail_verified),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedBorderColor = Color(0xFFE2E8F0),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.account_detail_sign_in_method),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF475569)
                        )
                        val providerName = if (user.photoUrl != null && user.photoUrl.contains("google")) {
                            stringResource(R.string.account_detail_provider_google)
                        } else {
                            stringResource(R.string.account_detail_provider_email)
                        }
                        Text(
                            text = providerName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 3. Section: Sinkronisasi Cloud
            SettingsSectionHeader(stringResource(R.string.account_detail_section_sync))
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFECFDF5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudSync,
                                    contentDescription = null,
                                    tint = SaatColors.DeepEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.account_detail_cloud_status),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(Modifier.height(2.dp))
                                val syncLabel = remember(state.lastSyncTimeMillis) {
                                    if (state.lastSyncTimeMillis != null && state.lastSyncTimeMillis > 0L) {
                                        val diff = System.currentTimeMillis() - state.lastSyncTimeMillis
                                        if (diff < 60_000L) null
                                        else java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(state.lastSyncTimeMillis))
                                    } else null
                                }
                                Text(
                                    text = if (state.isSyncing) {
                                        stringResource(R.string.account_syncing)
                                    } else if (syncLabel != null) {
                                        "${stringResource(R.string.account_last_synced)} $syncLabel"
                                    } else if (state.lastSyncTimeMillis != null && state.lastSyncTimeMillis > 0L) {
                                        "${stringResource(R.string.account_last_synced)} ${stringResource(R.string.account_just_now)}"
                                    } else {
                                        stringResource(R.string.account_status_synced)
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEDF5F0),
                            modifier = Modifier.clickable(
                                enabled = !state.isSyncing && !state.isAuthLoading,
                                onClick = { vm.syncNow() }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = SaatColors.HomeDarkGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.account_sync_now),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = SaatColors.HomeDarkGreen
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.account_detail_sync_desc),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // 4. Section: Pengaturan Akun & Keamanan (Logout & Delete)
            SettingsSectionHeader(stringResource(R.string.account_section_security))
            SettingsCard {
                AccountActionRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    iconTint = Color(0xFF64748B),
                    title = stringResource(R.string.account_sign_out_row_title),
                    subtitle = stringResource(R.string.account_sign_out_row_subtitle),
                    onClick = { showSignOutSheet = true },
                    showDivider = true
                )
                AccountActionRow(
                    icon = Icons.Outlined.DeleteForever,
                    iconTint = Color(0xFFDC2626),
                    title = stringResource(R.string.account_delete_row_title),
                    titleColor = Color(0xFFDC2626),
                    subtitle = stringResource(R.string.account_delete_row_subtitle),
                    subtitleColor = Color(0xFFEF4444),
                    onClick = { showDeleteSheet = true },
                    showDivider = false
                )
            }
        }
    }
}

@Composable
private fun AccountActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF1E293B),
    subtitleColor: Color = Color(0xFF64748B),
    showDivider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = titleColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = subtitleColor
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                color = Color(0xFFF1F5F9),
                thickness = 0.8.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignOutBottomSheet(
    isLoading: Boolean,
    onConfirmSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = sheetState,
        containerColor = SaatColors.PureWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.account_sign_out_confirm_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.account_sign_out_confirm_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                ),
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onConfirmSignOut,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.account_sign_out_row_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569))
            ) {
                Text(
                    text = stringResource(R.string.account_sign_out_cancel),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteAccountBottomSheet(
    isLoading: Boolean,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = sheetState,
        containerColor = SaatColors.PureWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.account_delete_sheet_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = Color(0xFF991B1B),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.account_delete_sheet_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFF5F5),
                border = BorderStroke(1.dp, Color(0xFFFED7D7))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    DeleteItemRow(text = stringResource(R.string.account_delete_item_quran))
                    Spacer(Modifier.height(8.dp))
                    DeleteItemRow(text = stringResource(R.string.account_delete_item_tracker))
                    Spacer(Modifier.height(8.dp))
                    DeleteItemRow(text = stringResource(R.string.account_delete_item_dhikr))
                    Spacer(Modifier.height(8.dp))
                    DeleteItemRow(text = stringResource(R.string.account_delete_item_auth))
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onConfirmDelete,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.DeleteForever,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.account_delete_confirm_action),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569))
            ) {
                Text(
                    text = stringResource(R.string.account_delete_cancel_action),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun DeleteItemRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFFFCA5A5).copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626))
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF7F1D1D)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailAuthBottomSheet(
    mode: EmailAuthMode,
    isLoading: Boolean,
    errorMessage: String?,
    onModeChange: (EmailAuthMode) -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, password: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onDismiss: () -> Unit,
    onDismissError: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SaatColors.PureWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header Title
            val title = when (mode) {
                EmailAuthMode.LOGIN -> stringResource(R.string.account_tab_login)
                EmailAuthMode.REGISTER -> stringResource(R.string.account_tab_register)
                EmailAuthMode.FORGOT_PASSWORD -> stringResource(R.string.account_forgot_password_title)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = SaatColors.HomeDarkGreen
            )

            Spacer(modifier = Modifier.height(4.dp))

            val subtitle = when (mode) {
                EmailAuthMode.LOGIN -> stringResource(R.string.account_sign_in_subtitle)
                EmailAuthMode.REGISTER -> stringResource(R.string.account_sign_up_subtitle)
                EmailAuthMode.FORGOT_PASSWORD -> stringResource(R.string.account_forgot_password_desc)
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = Color(0xFF64748B)
            )

            // Segmented switch between Sign In and Sign Up (only if not forgot password)
            if (mode != EmailAuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val isSignIn = mode == EmailAuthMode.LOGIN
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDismissError()
                                    onModeChange(EmailAuthMode.LOGIN)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSignIn) SaatColors.PureWhite else Color.Transparent,
                            shadowElevation = if (isSignIn) 1.dp else 0.dp
                        ) {
                            Text(
                                text = stringResource(R.string.account_tab_login),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSignIn) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                color = if (isSignIn) SaatColors.HomeDarkGreen else Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        val isSignUp = mode == EmailAuthMode.REGISTER
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDismissError()
                                    onModeChange(EmailAuthMode.REGISTER)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSignUp) SaatColors.PureWhite else Color.Transparent,
                            shadowElevation = if (isSignUp) 1.dp else 0.dp
                        ) {
                            Text(
                                text = stringResource(R.string.account_tab_register),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                color = if (isSignUp) SaatColors.HomeDarkGreen else Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Box
            errorMessage?.let { err ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFFB91C1C),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFB91C1C),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Name Field (only if REGISTER)
            if (mode == EmailAuthMode.REGISTER) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.account_name_label)) },
                    placeholder = { Text(stringResource(R.string.account_name_hint)) },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaatColors.HomeDarkGreen,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = SaatColors.HomeDarkGreen
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.account_email_label)) },
                placeholder = { Text(stringResource(R.string.account_email_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SaatColors.HomeDarkGreen,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = SaatColors.HomeDarkGreen
                )
            )

            // Password Field (if not FORGOT_PASSWORD)
            if (mode != EmailAuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.account_password_label)) },
                    placeholder = { Text(stringResource(R.string.account_password_hint)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaatColors.HomeDarkGreen,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = SaatColors.HomeDarkGreen
                    )
                )
            }

            // Forgot password button (only when LOGIN)
            if (mode == EmailAuthMode.LOGIN) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onDismissError()
                            onModeChange(EmailAuthMode.FORGOT_PASSWORD)
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = stringResource(R.string.account_forgot_password),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SaatColors.HomeDarkGreen
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Submit Button
            val submitButtonText = when (mode) {
                EmailAuthMode.LOGIN -> stringResource(R.string.account_submit_login)
                EmailAuthMode.REGISTER -> stringResource(R.string.account_submit_register)
                EmailAuthMode.FORGOT_PASSWORD -> stringResource(R.string.account_action_reset_password)
            }
            Button(
                onClick = {
                    onDismissError()
                    when (mode) {
                        EmailAuthMode.LOGIN -> onSignIn(email, password)
                        EmailAuthMode.REGISTER -> onSignUp(name, email, password)
                        EmailAuthMode.FORGOT_PASSWORD -> onForgotPassword(email)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaatColors.HomeDarkGreen,
                    contentColor = SaatColors.PureWhite
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SaatColors.PureWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = submitButtonText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            // Back to sign in if in forgot password mode
            if (mode == EmailAuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = {
                        onDismissError()
                        onModeChange(EmailAuthMode.LOGIN)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = !isLoading
                ) {
                    Text(
                        text = stringResource(R.string.account_back_to_sign_in),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
