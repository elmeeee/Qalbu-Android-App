package app.kamy.saatApp.features.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kamy.saatApp.domain.adhan.AdhanVoice
import app.kamy.saatApp.domain.adhan.AdhanVoiceCatalog
import app.kamy.saatApp.domain.adhan.FajrAdhanVoice
import app.kamy.saatApp.core.config.LocalQuranConfig
import app.kamy.saatApp.domain.model.QFTranslation
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.domain.prayer.PrayerMethodOption
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.infrastructure.audio.AdhanPreviewPlayer
import app.kamy.saatApp.core.error.AppError
import app.kamy.saatApp.core.error.AppErrorKind
import app.kamy.saatApp.core.error.toAppError
import app.kamy.saatApp.core.error.isAuthenticationFailure
import app.kamy.saatApp.infrastructure.notifications.DailyVerseNotificationScheduler
import app.kamy.saatApp.infrastructure.notifications.PrayerNotificationCoordinator
import app.kamy.saatApp.infrastructure.notifications.PrayerScheduleRefresher
import app.kamy.saatApp.infrastructure.preferences.AdhanPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.DailyVerseNotificationStore
import app.kamy.saatApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.ThemePreferencesStore
import app.kamy.saatApp.infrastructure.preferences.SurahReminder
import app.kamy.saatApp.infrastructure.preferences.SurahReminderStore
import app.kamy.saatApp.domain.share.VerseShareTextComposer
import app.kamy.saatApp.infrastructure.widget.WidgetCoordinator
import app.kamy.saatApp.infrastructure.repository.AlAdhanRepository
import app.kamy.saatApp.infrastructure.repository.ContentRepository
import app.kamy.saatApp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val showFontScaleSheet: Boolean = false,
    val showTranslatorSheet: Boolean = false,
    val showPrayerSheet: Boolean = false,
    val showNotifTimeSheet: Boolean = false,
    val translations: List<QFTranslation> = emptyList(),
    val translationsLoading: Boolean = false,
    val translationsError: AppError? = null,
    val translatorQuery: String = "",
    val selectedTranslationId: Int = 0,
    val selectedTranslationName: String = "",
    val fontScale: Float = 1.0f,
    val showTranslation: Boolean = true,
    val showTransliteration: Boolean = false,
    val prayerMethod: PrayerCalculationMethod = PrayerCalculationMethod.defaultMethod,
    val prayerMethods: List<PrayerMethodOption> = emptyList(),
    val prayerMethodsLoading: Boolean = false,
    val prayerMethodsError: AppError? = null,
    val dailyVerseEnabled: Boolean = true,
    val reminderHour: Int = DailyVerseNotificationStore.DEFAULT_HOUR,
    val reminderMinute: Int = DailyVerseNotificationStore.DEFAULT_MINUTE,
    val reminderTimeLabel: String = "",
    val dailyVerseDays: Set<Int> = (1..7).toSet(),
    val fajrNotificationEnabled: Boolean = true,
    val dhuhrNotificationEnabled: Boolean = true,
    val asrNotificationEnabled: Boolean = true,
    val maghribNotificationEnabled: Boolean = true,
    val ishaNotificationEnabled: Boolean = true,
    val imsakEnabled: Boolean = true,
    val midnightEnabled: Boolean = true,
    val firstThirdEnabled: Boolean = true,
    val tahajudEnabled: Boolean = false,
    val tahajudHour: Int = 3,
    val tahajudMinute: Int = 30,
    val tahajudTimeLabel: String = "03:30",
    val tahajudDays: Set<Int> = (1..7).toSet(),
    val yasinReminderEnabled: Boolean = true,
    val kahfReminderEnabled: Boolean = true,
    val importantDaysReminderEnabled: Boolean = true,
    val adhanSoundEnabled: Boolean = true,
    val monThuFastReminderEnabled: Boolean = true,
    val dhuhaReminderEnabled: Boolean = false,
    val dhuhaHour: Int = 8,
    val dhuhaMinute: Int = 30,
    val dhuhaTimeLabel: String = "08:30",
    val dhuhaDays: Set<Int> = (1..7).toSet(),
    val showAdhanSheet: Boolean = false,
    val showLanguageSheet: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val appTheme: app.kamy.saatApp.infrastructure.preferences.AppThemeColor = app.kamy.saatApp.infrastructure.preferences.AppThemeColor.EMERALD,
    val surahReminders: List<SurahReminder> = emptyList(),
    val showSurahRemindersSheet: Boolean = false,
    val showThemeSheet: Boolean = false,
    val prayerMadhab: app.kamy.saatApp.domain.prayer.PrayerMadhab = app.kamy.saatApp.domain.prayer.PrayerMadhab.SHAFI,
    val showMadhabSheet: Boolean = false,
    val selectedAdhanVoice: AdhanVoice = AdhanVoice.DEFAULT,
    val selectedFajrVoice: FajrAdhanVoice = FajrAdhanVoice.DEFAULT,
    val previewingAdhanVoiceId: String? = null
) {
    fun isPrayerNotificationEnabled(type: PrayerType): Boolean = when (type) {
        PrayerType.FAJR -> fajrNotificationEnabled
        PrayerType.DHUHR -> dhuhrNotificationEnabled
        PrayerType.ASR -> asrNotificationEnabled
        PrayerType.MAGHRIB -> maghribNotificationEnabled
        PrayerType.ISHA -> ishaNotificationEnabled
        else -> false
    }
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository,
    private val alAdhanRepository: AlAdhanRepository,
    private val prayerMethodStore: PrayerCalculationStore,
    private val translationStore: TranslationPreferencesStore,
    private val notificationStore: DailyVerseNotificationStore,
    private val prayerNotificationPrefs: PrayerNotificationPreferencesStore,
    private val adhanPrefs: AdhanPreferencesStore,
    private val adhanPreviewPlayer: AdhanPreviewPlayer,
    private val appLanguageStore: AppLanguageStore,
    private val shareComposer: VerseShareTextComposer,
    private val themeStore: ThemePreferencesStore,
    private val surahReminderStore: SurahReminderStore
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        syncPreferencesIntoState()
        viewModelScope.launch {
            themeStore.themeFlow.collect { theme ->
                _state.update { it.copy(appTheme = theme) }
            }
        }
        viewModelScope.launch {
            prayerMethodStore.madhab.collect { madhab ->
                _state.update { it.copy(prayerMadhab = madhab) }
            }
        }

        viewModelScope.launch {
            prayerMethodStore.method.collect { method ->
                _state.update { it.copy(prayerMethod = method) }
            }
        }
        viewModelScope.launch {
            translationStore.translationId.collect { id ->
                _state.update { it.copy(selectedTranslationId = id) }
            }
        }
        viewModelScope.launch {
            translationStore.translationName.collect { name ->
                _state.update { it.copy(selectedTranslationName = name) }
            }
        }
        viewModelScope.launch {
            translationStore.showTranslation.collect { show ->
                _state.update { it.copy(showTranslation = show) }
            }
        }
        viewModelScope.launch {
            translationStore.showTransliteration.collect { show ->
                _state.update { it.copy(showTransliteration = show) }
            }
        }
        viewModelScope.launch {
            translationStore.fontScale.collect { scale ->
                _state.update { it.copy(fontScale = scale) }
            }
        }
        viewModelScope.launch {
            notificationStore.enabled.collect { enabled ->
                _state.update { it.copy(dailyVerseEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            notificationStore.hour.collect {
                _state.update {
                    it.copy(
                        reminderHour = notificationStore.morningHour(),
                        reminderTimeLabel = notificationStore.formattedMorningTime()
                    )
                }
            }
        }
        viewModelScope.launch {
            notificationStore.minute.collect {
                _state.update {
                    it.copy(
                        reminderMinute = notificationStore.morningMinute(),
                        reminderTimeLabel = notificationStore.formattedMorningTime()
                    )
                }
            }
        }
        viewModelScope.launch {
            prayerNotificationPrefs.changeTick.collect { syncPrayerNotificationState() }
        }
        viewModelScope.launch {
            adhanPrefs.selectedVoice.collect { voice ->
                _state.update { it.copy(selectedAdhanVoice = voice) }
            }
        }
        viewModelScope.launch {
            adhanPrefs.selectedFajrVoice.collect { voice ->
                _state.update { it.copy(selectedFajrVoice = voice) }
            }
        }
        viewModelScope.launch {
            adhanPreviewPlayer.previewingVoiceId.collect { id ->
                _state.update { it.copy(previewingAdhanVoiceId = id) }
            }
        }
    }

    private fun syncPrayerNotificationState() {
        _state.update {
            it.copy(
                fajrNotificationEnabled = prayerNotificationPrefs.isPrayerEnabled(PrayerType.FAJR),
                dhuhrNotificationEnabled = prayerNotificationPrefs.isPrayerEnabled(PrayerType.DHUHR),
                asrNotificationEnabled = prayerNotificationPrefs.isPrayerEnabled(PrayerType.ASR),
                maghribNotificationEnabled = prayerNotificationPrefs.isPrayerEnabled(PrayerType.MAGHRIB),
                ishaNotificationEnabled = prayerNotificationPrefs.isPrayerEnabled(PrayerType.ISHA),
                imsakEnabled = prayerNotificationPrefs.isImsakEnabled(),
                midnightEnabled = prayerNotificationPrefs.isMidnightEnabled(),
                firstThirdEnabled = prayerNotificationPrefs.isFirstThirdEnabled(),
                tahajudEnabled = prayerNotificationPrefs.isTahajudEnabled(),
                tahajudHour = prayerNotificationPrefs.tahajudHour(),
                tahajudMinute = prayerNotificationPrefs.tahajudMinute(),
                tahajudTimeLabel = String.format("%02d:%02d", prayerNotificationPrefs.tahajudHour(), prayerNotificationPrefs.tahajudMinute()),
                tahajudDays = prayerNotificationPrefs.tahajudDays(),
                yasinReminderEnabled = prayerNotificationPrefs.isYasinReminderEnabled(),
                kahfReminderEnabled = prayerNotificationPrefs.isKahfReminderEnabled(),
                importantDaysReminderEnabled = prayerNotificationPrefs.isImportantDaysReminderEnabled(),
                adhanSoundEnabled = prayerNotificationPrefs.isAdhanSoundEnabled(),
                monThuFastReminderEnabled = prayerNotificationPrefs.isMonThuFastEnabled(),
                dhuhaReminderEnabled = prayerNotificationPrefs.isDhuhaEnabled(),
                dhuhaHour = prayerNotificationPrefs.dhuhaHour(),
                dhuhaMinute = prayerNotificationPrefs.dhuhaMinute(),
                dhuhaTimeLabel = String.format("%02d:%02d", prayerNotificationPrefs.dhuhaHour(), prayerNotificationPrefs.dhuhaMinute()),
                dhuhaDays = prayerNotificationPrefs.dhuhaDays()
            )
        }
    }

    fun setTahajudTime(hour: Int, minute: Int, days: Set<Int> = (1..7).toSet()) {
        prayerNotificationPrefs.setTahajudEnabled(true)
        prayerNotificationPrefs.setTahajudTime(hour, minute)
        prayerNotificationPrefs.setTahajudDays(days)
        syncPrayerNotificationState()
        reschedulePrayerNotifications()
    }

    private fun reschedulePrayerNotifications() {
        PrayerNotificationCoordinator.rescheduleFromCache(appContext)
    }

    private fun syncPreferencesIntoState() {
        _state.update {
            it.copy(
                prayerMethod = prayerMethodStore.current(),
                selectedTranslationId = translationStore.currentTranslationId(),
                selectedTranslationName = translationStore.translationName.value,
                showTranslation = translationStore.showTranslation.value,
                showTransliteration = translationStore.showTransliteration.value,
                fontScale = translationStore.fontScale.value,
                dailyVerseEnabled = notificationStore.isEnabled(),
                reminderHour = notificationStore.morningHour(),
                reminderMinute = notificationStore.morningMinute(),
                reminderTimeLabel = notificationStore.formattedMorningTime(),
                dailyVerseDays = notificationStore.days(),
                appLanguage = appLanguageStore.current(),
                appTheme = themeStore.currentTheme(),
                prayerMadhab = prayerMethodStore.currentMadhab(),
                surahReminders = surahReminderStore.getReminders()
            )
        }
    }

    fun openLanguageSheet() {
        _state.update { it.copy(showLanguageSheet = true) }
    }

    fun closeLanguageSheet() {
        _state.update { it.copy(showLanguageSheet = false) }
    }

    fun openThemeSheet() {
        _state.update { it.copy(showThemeSheet = true) }
    }

    fun closeThemeSheet() {
        _state.update { it.copy(showThemeSheet = false) }
    }

    fun setAppTheme(theme: app.kamy.saatApp.infrastructure.preferences.AppThemeColor) {
        viewModelScope.launch {
            themeStore.setTheme(theme)
        }
        _state.update {
            it.copy(
                appTheme = theme,
                showThemeSheet = false
            )
        }
    }

    fun openSurahRemindersSheet() {
        _state.update { it.copy(showSurahRemindersSheet = true) }
    }

    fun closeSurahRemindersSheet() {
        _state.update { it.copy(showSurahRemindersSheet = false) }
    }

    fun addSurahReminder(reminder: SurahReminder) {
        surahReminderStore.addReminder(reminder)
        _state.update { it.copy(surahReminders = surahReminderStore.getReminders()) }
    }

    fun updateSurahReminder(reminder: SurahReminder) {
        surahReminderStore.updateReminder(reminder)
        _state.update { it.copy(surahReminders = surahReminderStore.getReminders()) }
    }

    fun deleteSurahReminder(id: String) {
        surahReminderStore.deleteReminder(id)
        _state.update { it.copy(surahReminders = surahReminderStore.getReminders()) }
    }

    fun toggleSurahReminder(id: String, enabled: Boolean) {
        val reminder = surahReminderStore.getReminders().firstOrNull { it.id == id } ?: return
        val updated = reminder.copy(enabled = enabled)
        surahReminderStore.updateReminder(updated)
        _state.update { it.copy(surahReminders = surahReminderStore.getReminders()) }
    }



    fun openMadhabSheet() {
        _state.update { it.copy(showMadhabSheet = true) }
    }

    fun closeMadhabSheet() {
        _state.update { it.copy(showMadhabSheet = false) }
    }

    fun setPrayerMadhab(madhab: app.kamy.saatApp.domain.prayer.PrayerMadhab) {
        prayerMethodStore.setMadhab(madhab)
        viewModelScope.launch {
            PrayerNotificationCoordinator.rescheduleFromCache(appContext)
            WidgetCoordinator.refreshAll(appContext)
        }
        _state.update {
            it.copy(
                prayerMadhab = madhab,
                showMadhabSheet = false
            )
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        if (language == appLanguageStore.current()) {
            closeLanguageSheet()
            return
        }
        appLanguageStore.set(language)

        // Automatically set default translation corresponding to the new app language
        val defaultTranslation = LocalQuranConfig.translationForAppLanguage(language)
        val label = LocalQuranConfig.translationDisplayLabel(defaultTranslation)
        translationStore.setTranslation(defaultTranslation.id, label)

        contentRepository.clearCache()
        shareComposer.clearCaches()
        viewModelScope.launch {
            PrayerNotificationCoordinator.rescheduleFromCache(appContext)
            DailyVerseNotificationScheduler.reschedule(appContext)
            WidgetCoordinator.refreshAll(appContext)
        }
        _state.update {
            it.copy(
                appLanguage = language,
                showLanguageSheet = false,
                selectedTranslationId = defaultTranslation.id,
                selectedTranslationName = label
            )
        }
    }

    fun selectTranslation(translation: QFTranslation): Boolean {
        val label = LocalQuranConfig.translationDisplayLabel(translation)
        translationStore.setTranslation(translation.id, label)
        _state.update {
            it.copy(
                showTranslatorSheet = false,
                selectedTranslationId = translation.id,
                selectedTranslationName = label
            )
        }
        return false
    }



    fun openFontScale() = _state.update { it.copy(showFontScaleSheet = true) }
    fun closeFontScale() = _state.update { it.copy(showFontScaleSheet = false) }
    fun setFontScale(value: Float) = translationStore.setFontScale(value)

    fun openTranslator() {
        _state.update { it.copy(showTranslatorSheet = true) }
        loadTranslations()
    }

    fun closeTranslator() = _state.update { it.copy(showTranslatorSheet = false) }
    fun setTranslatorQuery(q: String) = _state.update { it.copy(translatorQuery = q) }

    fun loadTranslations() {
        _state.update { it.copy(translationsLoading = true, translationsError = null) }
        viewModelScope.launch {
            runCatching { contentRepository.getTranslations() }
                .onSuccess { list ->
                    val sorted = list.sortedWith(
                        compareByDescending<QFTranslation> {
                            it.languageName.equals(preferredTranslationLanguageName(), true)
                        }
                            .thenBy { it.languageName }
                            .thenBy { it.authorName }
                    )
                    _state.update {
                        it.copy(
                            translationsLoading = false,
                            translations = sorted,
                            translationsError = null
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            translationsLoading = false,
                            translationsError = t.toAppError()
                        )
                    }
                }
        }
    }

    fun togglePrayerSheet(show: Boolean) {
        _state.update { it.copy(showPrayerSheet = show) }
        if (show && _state.value.prayerMethods.isEmpty()) loadPrayerMethods()
    }

    fun loadPrayerMethods() {
        _state.update { it.copy(prayerMethodsLoading = true, prayerMethodsError = null) }
        viewModelScope.launch {
            runCatching { alAdhanRepository.fetchCalculationMethods() }
                .onSuccess { methods ->
                    _state.update { it.copy(prayerMethodsLoading = false, prayerMethods = methods, prayerMethodsError = null) }
                }
                .onFailure { t ->
                    _state.update { it.copy(prayerMethodsLoading = false, prayerMethodsError = t.toAppError()) }
                }
        }
    }

    fun toggleNotifTimeSheet(show: Boolean) = _state.update { it.copy(showNotifTimeSheet = show) }

    fun setShowTranslation(enabled: Boolean) {
        translationStore.setShowTranslation(enabled)
    }

    fun setShowTransliteration(enabled: Boolean) {
        translationStore.setShowTransliteration(enabled)
    }

    fun setDailyVerseEnabled(enabled: Boolean) {
        notificationStore.setEnabled(enabled)
        DailyVerseNotificationScheduler.reschedule(appContext)
    }

    fun saveReminderTime(hour: Int, minute: Int, days: Set<Int> = (1..7).toSet()) {
        notificationStore.setMorningTime(hour, minute)
        notificationStore.setDays(days)
        DailyVerseNotificationScheduler.reschedule(appContext)
        _state.update {
            it.copy(
                showNotifTimeSheet = false,
                reminderHour = notificationStore.morningHour(),
                reminderMinute = notificationStore.morningMinute(),
                reminderTimeLabel = notificationStore.formattedMorningTime(),
                dailyVerseDays = notificationStore.days()
            )
        }
    }

    fun setPrayerMethod(method: PrayerCalculationMethod) {
        prayerMethodStore.setMethod(method)
        viewModelScope.launch {
            runCatching { PrayerScheduleRefresher.refresh(appContext) }
            PrayerNotificationCoordinator.rescheduleFromCache(appContext)
        }
    }

    fun setPrayerNotificationEnabled(type: PrayerType, enabled: Boolean) {
        prayerNotificationPrefs.setPrayerEnabled(type, enabled)
        reschedulePrayerNotifications()
    }

    fun setImsakEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setImsakEnabled(enabled)
        reschedulePrayerNotifications()
    }

    fun setMidnightEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setMidnightEnabled(enabled)
        reschedulePrayerNotifications()
    }

    fun setFirstThirdEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setFirstThirdEnabled(enabled)
        reschedulePrayerNotifications()
    }

    fun setTahajudEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setTahajudEnabled(enabled)
        syncPrayerNotificationState()
        reschedulePrayerNotifications()
    }

    fun setYasinReminderEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setYasinReminderEnabled(enabled)
        surahReminderStore.rescheduleAlarms(surahReminderStore.getReminders())
        reschedulePrayerNotifications()
    }

    fun setKahfReminderEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setKahfReminderEnabled(enabled)
        reschedulePrayerNotifications()
    }

    fun setImportantDaysReminderEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setImportantDaysReminderEnabled(enabled)
        prayerNotificationPrefs.setMonThuFastEnabled(enabled)
        reschedulePrayerNotifications()
    }

    fun setAdhanSoundEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setAdhanSoundEnabled(enabled)
        reschedulePrayerNotifications()
    }

    fun setMonThuFastReminderEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setMonThuFastEnabled(enabled)
        reschedulePrayerNotifications()
    }

    fun setDhuhaReminderEnabled(enabled: Boolean) {
        prayerNotificationPrefs.setDhuhaEnabled(enabled)
        syncPrayerNotificationState()
        reschedulePrayerNotifications()
    }

    fun setDhuhaTime(hour: Int, minute: Int, days: Set<Int> = (1..7).toSet()) {
        val isIndoMalay = appLanguageStore.current() == AppLanguage.INDONESIAN || appLanguageStore.current() == AppLanguage.MALAY
        if (hour < 6 || (hour > 11 || (hour == 11 && minute > 45))) {
            val msg = if (isIndoMalay) {
                "Waktu Duha harus antara 06:00 dan 11:45 (sebelum Zuhur)"
            } else {
                "Dhuha time must be between 06:00 and 11:45 (before Dhuhr)"
            }
            android.widget.Toast.makeText(appContext, msg, android.widget.Toast.LENGTH_LONG).show()
            return
        }
        prayerNotificationPrefs.setDhuhaEnabled(true)
        prayerNotificationPrefs.setDhuhaTime(hour, minute)
        prayerNotificationPrefs.setDhuhaDays(days)
        syncPrayerNotificationState()
        reschedulePrayerNotifications()
    }


    fun openAdhanSheet() = _state.update { it.copy(showAdhanSheet = true) }

    fun closeAdhanSheet() {
        adhanPreviewPlayer.stop()
        _state.update { it.copy(showAdhanSheet = false) }
    }

    fun selectAdhanVoice(voice: AdhanVoice) {
        adhanPrefs.setVoice(voice)
        adhanPreviewPlayer.stop()
    }

    fun selectFajrVoice(voice: FajrAdhanVoice) {
        adhanPrefs.setFajrVoice(voice)
        adhanPreviewPlayer.stop()
    }

    fun toggleAdhanPreview(voice: AdhanVoice) {
        adhanPreviewPlayer.togglePreview(voice.id, AdhanVoiceCatalog.rawResForPreview(voice))
    }

    fun toggleFajrPreview(voice: FajrAdhanVoice) {
        adhanPreviewPlayer.togglePreview(voice.id, AdhanVoiceCatalog.rawResForPreview(voice))
    }

    fun notificationSummary(state: AccountUiState = _state.value): String {
        val labels = buildList {
            if (state.dailyVerseEnabled) add(appContext.getString(R.string.notif_summary_daily_verse))
            val enabledPrayers = PrayerType.ADZAN_NOTIFICATION_PRAYERS
                .filter { state.isPrayerNotificationEnabled(it) }
                .map { appContext.getString(prayerNameRes(it)) }
            when {
                enabledPrayers.size == PrayerType.ADZAN_NOTIFICATION_PRAYERS.size ->
                    add(appContext.getString(R.string.notif_summary_prayer))
                enabledPrayers.isNotEmpty() ->
                    add(enabledPrayers.joinToString(", "))
            }
            if (state.imsakEnabled) add(appContext.getString(R.string.notif_summary_imsak))
            if (state.midnightEnabled) add(appContext.getString(R.string.notif_summary_midnight))
            if (state.firstThirdEnabled) add(appContext.getString(R.string.notif_summary_first_third))
            if (state.tahajudEnabled) add(appContext.getString(R.string.notif_summary_tahajud))
            if (state.yasinReminderEnabled) add(appContext.getString(R.string.notif_summary_yasin))
            if (state.kahfReminderEnabled) add(appContext.getString(R.string.notif_summary_kahf))
            if (state.importantDaysReminderEnabled) add(appContext.getString(R.string.notif_summary_important_days))
        }
        return when {
            labels.isEmpty() -> appContext.getString(R.string.notif_summary_all_off)
            labels.size >= 6 -> appContext.getString(R.string.notif_summary_count, labels.size)
            else -> labels.joinToString(" · ")
        }
    }

    private fun preferredTranslationLanguageName(): String = when (appLanguageStore.current()) {
        AppLanguage.INDONESIAN -> "indonesian"
        AppLanguage.MALAY -> "malay"
        AppLanguage.ENGLISH -> "english"
    }

    private fun prayerNameRes(type: PrayerType): Int = when (type) {
        PrayerType.FAJR -> R.string.prayer_fajr
        PrayerType.DHUHR -> R.string.prayer_dhuhr
        PrayerType.ASR -> R.string.prayer_asr
        PrayerType.MAGHRIB -> R.string.prayer_maghrib
        PrayerType.ISHA -> R.string.prayer_isha
        else -> R.string.prayer_fajr
    }

    fun filteredTranslations(): List<QFTranslation> {
        val q = _state.value.translatorQuery.trim().lowercase()
        val all = _state.value.translations
        if (q.isEmpty()) return all
        return all.filter {
            it.name.lowercase().contains(q) ||
                it.authorName.lowercase().contains(q) ||
                it.languageName.lowercase().contains(q)
        }
    }
}
