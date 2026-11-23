package app.kamy.saatApp.infrastructure.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import app.kamy.saatApp.core.locale.AppLanguage
import app.kamy.saatApp.domain.adhan.AdhanVoice
import app.kamy.saatApp.domain.adhan.FajrAdhanVoice
import app.kamy.saatApp.domain.model.ArabicTextType
import app.kamy.saatApp.domain.model.HifzEntry
import app.kamy.saatApp.domain.model.PrayerType
import app.kamy.saatApp.domain.model.QuranReadingStyle
import app.kamy.saatApp.domain.model.VerseBookmark
import app.kamy.saatApp.domain.model.VerseNote
import app.kamy.saatApp.domain.prayer.PrayerCalculationMethod
import app.kamy.saatApp.domain.prayer.PrayerMadhab
import app.kamy.saatApp.domain.repository.SyncState
import app.kamy.saatApp.domain.repository.SyncStatus
import app.kamy.saatApp.domain.repository.UserDataSyncRepository
import app.kamy.saatApp.infrastructure.preferences.AdhanPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.AppLanguageStore
import app.kamy.saatApp.infrastructure.preferences.AppThemeColor
import app.kamy.saatApp.infrastructure.preferences.DailyVerseNotificationStore
import app.kamy.saatApp.infrastructure.preferences.DhikrStore
import app.kamy.saatApp.infrastructure.preferences.EncyclopediaReadingProgressStore
import app.kamy.saatApp.infrastructure.preferences.LocalReadingProgressStore
import app.kamy.saatApp.infrastructure.preferences.LocationMode
import app.kamy.saatApp.infrastructure.preferences.LocationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.PrayerCalculationStore
import app.kamy.saatApp.infrastructure.preferences.PrayerNotificationPreferencesStore
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
import app.kamy.saatApp.infrastructure.preferences.SavedManualLocation
import app.kamy.saatApp.infrastructure.preferences.SurahReminder
import app.kamy.saatApp.infrastructure.preferences.SurahReminderStore
import app.kamy.saatApp.infrastructure.preferences.ThemePreferencesStore
import app.kamy.saatApp.infrastructure.preferences.TranslationPreferencesStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class FirestoreUserDataSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val themeStore: ThemePreferencesStore,
    private val translationStore: TranslationPreferencesStore,
    private val prayerMethodStore: PrayerCalculationStore,
    private val locationStore: LocationPreferencesStore,
    private val prayerNotificationPrefs: PrayerNotificationPreferencesStore,
    private val adhanPrefs: AdhanPreferencesStore,
    private val dailyVerseStore: DailyVerseNotificationStore,
    private val surahReminderStore: SurahReminderStore
) : UserDataSyncRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val changeTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var isApplyingCloudUpdate = false

    /** Tracks the currently active user to prevent account cross-contamination. */
    private var activeUserId: String? = auth.currentUser?.uid

    private val watchedPrefFiles = listOf(
        "saat_quran_personal",
        "saat_prayer_tracker",
        "saat_dhikr",
        "saat_app_language",
        "saat_prefs",
        "saat_location_prefs",
        "saat_quran_prefs",
        "saat_prayer_notification_prefs",
        "saat_adhan_prefs",
        "saat_daily_verse_notification",
        "saat_surah_reminders",
        "saat_encyclopedia_reading"
    )

    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        if (!isApplyingCloudUpdate && auth.currentUser != null) {
            changeTrigger.tryEmit(Unit)
        }
    }

    private val _syncState = MutableStateFlow(
        SyncState(
            status = SyncStatus.IDLE,
            lastSyncTimeMillis = getLastSyncTime(auth.currentUser?.uid)
        )
    )
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        // Register listeners for all local preference stores
        watchedPrefFiles.forEach { prefName ->
            context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(prefChangeListener)
        }

        // Listen for theme preference changes from DataStore
        scope.launch {
            themeStore.themeFlow.collect {
                if (!isApplyingCloudUpdate && auth.currentUser != null) {
                    changeTrigger.tryEmit(Unit)
                }
            }
        }

        // Keep activeUserId updated on auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                activeUserId = null
                _syncState.value = SyncState(status = SyncStatus.IDLE, lastSyncTimeMillis = null)
            } else if (currentUser.uid != activeUserId) {
                _syncState.value = SyncState(
                    status = SyncStatus.IDLE,
                    lastSyncTimeMillis = getLastSyncTime(currentUser.uid)
                )
            }
        }

        // Debounced automatic upload whenever local data changes (2-second debounce)
        scope.launch {
            changeTrigger
                .debounce(2000L)
                .collect {
                    val uid = auth.currentUser?.uid
                    if (uid != null && !isApplyingCloudUpdate) {
                        runCatching {
                            uploadLocalToCloudInternal(uid)
                            val now = System.currentTimeMillis()
                            saveLastSyncTime(uid, now)
                            _syncState.value = SyncState(
                                status = SyncStatus.SUCCESS,
                                lastSyncTimeMillis = now
                            )
                        }.onFailure { error ->
                            Log.w(TAG, "Debounced sync upload failed: ${error.message}")
                        }
                    }
                }
        }
    }

    private fun getLastSyncTime(uid: String?): Long? {
        if (uid.isNullOrBlank()) return null
        val time = prefs.getLong("last_sync_time_millis_$uid", 0L)
        return if (time > 0L) time else null
    }

    private fun saveLastSyncTime(uid: String, time: Long) {
        prefs.edit().putLong("last_sync_time_millis_$uid", time).apply()
    }

    override suspend fun syncAll(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("ID Pengguna tidak valid"))
        }

        // Account isolation check: if switching from Account A to Account B, reset local stores
        if (activeUserId != null && activeUserId != userId) {
            resetLocalUserStores()
        }
        activeUserId = userId

        _syncState.value = _syncState.value.copy(
            status = SyncStatus.SYNCING,
            errorMessage = null
        )

        runCatching {
            // 1. Download & restore / merge from cloud to local
            downloadCloudToLocalInternal(userId)

            // 2. Upload combined state back to cloud
            uploadLocalToCloudInternal(userId)

            val now = System.currentTimeMillis()
            saveLastSyncTime(userId, now)

            _syncState.value = SyncState(
                status = SyncStatus.SUCCESS,
                lastSyncTimeMillis = now,
                errorMessage = null
            )
        }.onFailure { error ->
            val message = error.localizedMessage ?: "Gagal menyinkronkan data dengan cloud"
            Log.e(TAG, "syncAll failed for $userId", error)
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                errorMessage = message
            )
        }
    }

    override suspend fun uploadLocalToCloud(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.failure(IllegalArgumentException("ID Pengguna tidak valid"))
        runCatching {
            uploadLocalToCloudInternal(userId)
            val now = System.currentTimeMillis()
            saveLastSyncTime(userId, now)
            _syncState.value = SyncState(
                status = SyncStatus.SUCCESS,
                lastSyncTimeMillis = now
            )
        }
    }

    override suspend fun downloadCloudToLocal(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.failure(IllegalArgumentException("ID Pengguna tidak valid"))
        // Account isolation check
        if (activeUserId != null && activeUserId != userId) {
            resetLocalUserStores()
        }
        activeUserId = userId

        runCatching {
            downloadCloudToLocalInternal(userId)
            val now = System.currentTimeMillis()
            saveLastSyncTime(userId, now)
            _syncState.value = SyncState(
                status = SyncStatus.SUCCESS,
                lastSyncTimeMillis = now
            )
        }
    }

    private fun resetLocalUserStores() {
        QuranPersonalStore.clearAll(context)
        LocalReadingProgressStore.clear(context)
        PrayerTrackerStore.clearAll(context)
        DhikrStore.clearAll(context)
        EncyclopediaReadingProgressStore.clearAll(context)
    }

    private suspend fun downloadCloudToLocalInternal(userId: String) {
        isApplyingCloudUpdate = true
        val lastSyncTime = getLastSyncTime(userId) ?: 0L

        try {
            // A. Quran Personal Data
            val quranDoc = firestore.collection("users")
                .document(userId)
                .collection("quran")
                .document("personal")
                .get()
                .await()

            if (quranDoc.exists()) {
                val rawBookmarks = quranDoc.get("bookmarks") as? List<*>
                val bookmarks = rawBookmarks?.mapNotNull { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    val verseKey = map["verseKey"] as? String ?: return@mapNotNull null
                    val chapterNumber = (map["chapterNumber"] as? Number)?.toInt() ?: return@mapNotNull null
                    val verseNumber = (map["verseNumber"] as? Number)?.toInt() ?: return@mapNotNull null
                    val createdAtMillis = (map["createdAtMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    val surahLabel = map["surahLabel"] as? String
                    VerseBookmark(verseKey, chapterNumber, verseNumber, createdAtMillis, surahLabel)
                } ?: emptyList()
                QuranPersonalStore.restoreBookmarks(context, bookmarks, lastSyncTime)

                val rawNotes = quranDoc.get("notes") as? List<*>
                val notes = rawNotes?.mapNotNull { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    val verseKey = map["verseKey"] as? String ?: return@mapNotNull null
                    val chapterNumber = (map["chapterNumber"] as? Number)?.toInt() ?: return@mapNotNull null
                    val verseNumber = (map["verseNumber"] as? Number)?.toInt() ?: return@mapNotNull null
                    val text = map["text"] as? String ?: ""
                    val updatedAtMillis = (map["updatedAtMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    VerseNote(verseKey, chapterNumber, verseNumber, text, updatedAtMillis)
                } ?: emptyList()
                QuranPersonalStore.restoreNotes(context, notes, lastSyncTime)

                val rawHifz = quranDoc.get("hifz") as? List<*>
                val hifz = rawHifz?.mapNotNull { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    val verseKey = map["verseKey"] as? String ?: return@mapNotNull null
                    val chapterNumber = (map["chapterNumber"] as? Number)?.toInt() ?: return@mapNotNull null
                    val verseNumber = (map["verseNumber"] as? Number)?.toInt() ?: return@mapNotNull null
                    val status = map["status"] as? String ?: "NONE"
                    HifzEntry(verseKey, chapterNumber, verseNumber, status)
                } ?: emptyList()
                if (hifz.isNotEmpty()) {
                    QuranPersonalStore.restoreHifz(context, hifz)
                }

                val chapters = (quranDoc.get("chapters") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                val juzs = (quranDoc.get("juzs") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                val lastReadJuz = (quranDoc.get("lastReadJuz") as? Number)?.toInt()
                val lastReadVerseKey = quranDoc.get("lastReadVerseKey") as? String
                val lastReadUpdatedAtMillis = (quranDoc.get("lastReadUpdatedAtMillis") as? Number)?.toLong()

                if (chapters.isNotEmpty() || juzs.isNotEmpty() || lastReadVerseKey != null) {
                    QuranPersonalStore.restoreKhatam(
                        context = context,
                        cloudChapters = chapters,
                        cloudJuzs = juzs,
                        cloudLastReadJuz = lastReadJuz,
                        cloudLastReadVerseKey = lastReadVerseKey,
                        cloudLastReadUpdatedAtMillis = lastReadUpdatedAtMillis
                    )
                }
            }

            // B. Prayer Tracker Data
            val trackerDoc = firestore.collection("users")
                .document(userId)
                .collection("tracker")
                .document("prayer")
                .get()
                .await()

            if (trackerDoc.exists()) {
                val rawRecords = trackerDoc.get("records") as? Map<*, *>
                val records = mutableMapOf<String, Boolean>()
                rawRecords?.forEach { (k, v) ->
                    if (k is String && v is Boolean) {
                        records[k] = v
                    }
                }
                val bestStreak = (trackerDoc.get("bestStreak") as? Number)?.toInt() ?: 0
                if (records.isNotEmpty() || bestStreak > 0) {
                    PrayerTrackerStore.restoreRecords(context, records, bestStreak)
                }
            }

            // C. Dhikr Counters Data
            val dhikrDoc = firestore.collection("users")
                .document(userId)
                .collection("tasbih")
                .document("dhikr")
                .get()
                .await()

            if (dhikrDoc.exists()) {
                val rawCounters = dhikrDoc.get("counters") as? Map<*, *>
                val counters = mutableMapOf<String, Int>()
                rawCounters?.forEach { (k, v) ->
                    if (k is String && v is Number) {
                        counters[k] = v.toInt()
                    }
                }
                if (counters.isNotEmpty()) {
                    DhikrStore.restoreCounters(context, counters)
                }
            }

            // D. Account Preferences Data (/users/{userId}/preferences/settings)
            val prefDoc = firestore.collection("users")
                .document(userId)
                .collection("preferences")
                .document("settings")
                .get()
                .await()

            if (prefDoc.exists()) {
                // 1. Language
                prefDoc.getString("language")?.takeIf { it.isNotBlank() }?.let { tag ->
                    AppLanguageStore.from(context).set(AppLanguage.fromTag(tag))
                }

                // 2. Theme
                prefDoc.getString("theme")?.takeIf { it.isNotBlank() }?.let { key ->
                    themeStore.setTheme(AppThemeColor.fromKey(key))
                }

                // 3. Prayer Method & Madhab
                prefDoc.getString("prayerMethod")?.takeIf { it.isNotBlank() }?.let { raw ->
                    prayerMethodStore.setMethod(PrayerCalculationMethod.fromRawValue(raw))
                }
                prefDoc.getString("prayerMadhab")?.takeIf { it.isNotBlank() }?.let { raw ->
                    prayerMethodStore.setMadhab(PrayerMadhab.fromRawValue(raw))
                }

                // 4. Quran Preferences
                val quranMap = prefDoc.get("quran") as? Map<*, *>
                if (quranMap != null) {
                    val tid = (quranMap["translationId"] as? Number)?.toInt()
                    val tname = quranMap["translationName"] as? String
                    if (tid != null && tname != null) {
                        translationStore.setTranslation(tid, tname)
                    }
                    (quranMap["fontScale"] as? Number)?.toFloat()?.let { translationStore.setFontScale(it) }
                    (quranMap["readingStyle"] as? String)?.let {
                        runCatching { QuranReadingStyle.valueOf(it) }.getOrNull()?.let { style ->
                            translationStore.setReadingStyle(style)
                        }
                    }
                    (quranMap["showTranslation"] as? Boolean)?.let { translationStore.setShowTranslation(it) }
                    (quranMap["showTransliteration"] as? Boolean)?.let { translationStore.setShowTransliteration(it) }
                    (quranMap["isTajweedEnabled"] as? Boolean)?.let { translationStore.setTajweedEnabled(it) }
                    (quranMap["arabicTextType"] as? String)?.let {
                        runCatching { ArabicTextType.valueOf(it) }.getOrNull()?.let { type ->
                            translationStore.setArabicTextType(type)
                        }
                    }
                    (quranMap["recitationId"] as? Number)?.toInt()?.let { translationStore.setRecitation(it) }
                    (quranMap["tafsirSource"] as? String)?.let { translationStore.setTafsirSource(it) }
                }

                // 5. Location Preferences
                val locMap = prefDoc.get("location") as? Map<*, *>
                if (locMap != null) {
                    val isAutomatic = (locMap["isAutomatic"] as? Boolean)
                        ?: (locMap["mode"] as? String == LocationMode.GPS.name)
                    val manualCity = (locMap["city"] as? String) ?: (locMap["label"] as? String)
                    val lat = (locMap["latitude"] as? Number)?.toDouble()
                    val lon = (locMap["longitude"] as? Number)?.toDouble()
                    val cc = locMap["countryCode"] as? String

                    if (manualCity != null && lat != null && lon != null) {
                        locationStore.saveManual(SavedManualLocation(lat, lon, manualCity, cc))
                    } else if (manualCity != null) {
                        val offline = app.kamy.saatApp.infrastructure.local.LocalCityCatalog.findExact(manualCity)
                        if (offline != null) {
                            locationStore.saveManual(SavedManualLocation(offline.latitude, offline.longitude, offline.name, offline.countryCode))
                        }
                    }

                    if (isAutomatic) {
                        locationStore.setMode(LocationMode.GPS)
                    } else {
                        locationStore.setMode(LocationMode.MANUAL)
                    }
                }

                // 6. Notification Preferences
                val notifMap = prefDoc.get("notifications") as? Map<*, *>
                if (notifMap != null) {
                    (notifMap["fajr"] as? Boolean)?.let { prayerNotificationPrefs.setPrayerEnabled(PrayerType.FAJR, it) }
                    (notifMap["dhuhr"] as? Boolean)?.let { prayerNotificationPrefs.setPrayerEnabled(PrayerType.DHUHR, it) }
                    (notifMap["asr"] as? Boolean)?.let { prayerNotificationPrefs.setPrayerEnabled(PrayerType.ASR, it) }
                    (notifMap["maghrib"] as? Boolean)?.let { prayerNotificationPrefs.setPrayerEnabled(PrayerType.MAGHRIB, it) }
                    (notifMap["isha"] as? Boolean)?.let { prayerNotificationPrefs.setPrayerEnabled(PrayerType.ISHA, it) }
                    (notifMap["imsak"] as? Boolean)?.let { prayerNotificationPrefs.setImsakEnabled(it) }
                    (notifMap["midnight"] as? Boolean)?.let { prayerNotificationPrefs.setMidnightEnabled(it) }
                    (notifMap["firstThird"] as? Boolean)?.let { prayerNotificationPrefs.setFirstThirdEnabled(it) }
                    (notifMap["tahajud"] as? Boolean)?.let { prayerNotificationPrefs.setTahajudEnabled(it) }

                    val th = (notifMap["tahajudHour"] as? Number)?.toInt()
                    val tm = (notifMap["tahajudMinute"] as? Number)?.toInt()
                    if (th != null && tm != null) prayerNotificationPrefs.setTahajudTime(th, tm)

                    val tDays = (notifMap["tahajudDays"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toSet()
                    if (!tDays.isNullOrEmpty()) prayerNotificationPrefs.setTahajudDays(tDays)

                    (notifMap["dhuha"] as? Boolean)?.let { prayerNotificationPrefs.setDhuhaEnabled(it) }
                    val dh = (notifMap["dhuhaHour"] as? Number)?.toInt()
                    val dm = (notifMap["dhuhaMinute"] as? Number)?.toInt()
                    if (dh != null && dm != null) prayerNotificationPrefs.setDhuhaTime(dh, dm)

                    val dDays = (notifMap["dhuhaDays"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toSet()
                    if (!dDays.isNullOrEmpty()) prayerNotificationPrefs.setDhuhaDays(dDays)

                    (notifMap["yasin"] as? Boolean)?.let { prayerNotificationPrefs.setYasinReminderEnabled(it) }
                    (notifMap["kahf"] as? Boolean)?.let { prayerNotificationPrefs.setKahfReminderEnabled(it) }
                    (notifMap["importantDays"] as? Boolean)?.let { prayerNotificationPrefs.setImportantDaysReminderEnabled(it) }
                    (notifMap["adhanSound"] as? Boolean)?.let { prayerNotificationPrefs.setAdhanSoundEnabled(it) }
                    (notifMap["monThuFast"] as? Boolean)?.let { prayerNotificationPrefs.setMonThuFastEnabled(it) }
                    (notifMap["quranReminder"] as? Boolean)?.let { prayerNotificationPrefs.setQuranReminderEnabled(it) }
                    (notifMap["liveCountdown"] as? Boolean)?.let { prayerNotificationPrefs.setLiveCountdownEnabled(it) }

                    (notifMap["selectedAdhanVoiceId"] as? String)?.let { adhanPrefs.setVoice(AdhanVoice.fromId(it)) }
                    (notifMap["selectedFajrAdhanVoiceId"] as? String)?.let { adhanPrefs.setFajrVoice(FajrAdhanVoice.fromId(it)) }

                    (notifMap["dailyVerse"] as? Boolean)?.let { dailyVerseStore.setEnabled(it) }
                    val dvh = (notifMap["dailyVerseHour"] as? Number)?.toInt()
                    val dvm = (notifMap["dailyVerseMinute"] as? Number)?.toInt()
                    if (dvh != null && dvm != null) dailyVerseStore.setMorningTime(dvh, dvm)
                    val dvDays = (notifMap["dailyVerseDays"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toSet()
                    if (!dvDays.isNullOrEmpty()) dailyVerseStore.setDays(dvDays)
                }

                // 7. Surah Reminders
                val rawSurahReminders = prefDoc.get("surahReminders") as? List<*>
                if (rawSurahReminders != null) {
                    val reminders = rawSurahReminders.mapNotNull { item ->
                        val map = item as? Map<*, *> ?: return@mapNotNull null
                        val id = map["id"] as? String ?: return@mapNotNull null
                        val surahNumber = (map["surahNumber"] as? Number)?.toInt() ?: return@mapNotNull null
                        val surahName = map["surahName"] as? String ?: ""
                        val weekday = (map["weekday"] as? Number)?.toInt() ?: return@mapNotNull null
                        val hour = (map["hour"] as? Number)?.toInt() ?: return@mapNotNull null
                        val minute = (map["minute"] as? Number)?.toInt() ?: return@mapNotNull null
                        val enabled = map["enabled"] as? Boolean ?: true
                        SurahReminder(id, surahNumber, surahName, weekday, hour, minute, enabled)
                    }
                    if (reminders.isNotEmpty()) {
                        surahReminderStore.saveReminders(reminders)
                    }
                }
            }

            // E. Encyclopedia Reading Progress (/users/{userId}/encyclopedia/reading)
            val encDoc = firestore.collection("users")
                .document(userId)
                .collection("encyclopedia")
                .document("reading")
                .get()
                .await()

            if (encDoc.exists()) {
                val rawPages = encDoc.get("pages") as? Map<*, *>
                val pages = mutableMapOf<String, Int>()
                rawPages?.forEach { (k, v) ->
                    if (k is String && v is Number) {
                        pages[k] = v.toInt()
                    }
                }
                if (pages.isNotEmpty()) {
                    EncyclopediaReadingProgressStore.restorePages(context, pages)
                }
            }
        } finally {
            isApplyingCloudUpdate = false
        }
    }

    private suspend fun uploadLocalToCloudInternal(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        // 1. Upload Quran Personal Data
        val bookmarks = QuranPersonalStore.bookmarks(context).map {
            mapOf(
                "verseKey" to it.verseKey,
                "chapterNumber" to it.chapterNumber,
                "verseNumber" to it.verseNumber,
                "createdAtMillis" to it.createdAtMillis,
                "surahLabel" to (it.surahLabel ?: "")
            )
        }
        val notes = QuranPersonalStore.notes(context).map {
            mapOf(
                "verseKey" to it.verseKey,
                "chapterNumber" to it.chapterNumber,
                "verseNumber" to it.verseNumber,
                "text" to it.text,
                "updatedAtMillis" to it.updatedAtMillis
            )
        }
        val hifz = QuranPersonalStore.hifzEntries(context).map {
            mapOf(
                "verseKey" to it.verseKey,
                "chapterNumber" to it.chapterNumber,
                "verseNumber" to it.verseNumber,
                "status" to it.status
            )
        }

        val chapters = QuranPersonalStore.readChapters(context).toList()
        val juzs = QuranPersonalStore.readJuzs(context).toList()
        val lastReadJuz = QuranPersonalStore.lastReadJuz(context)
        val lastReadVerseKey = QuranPersonalStore.lastReadVerseKey(context)
        val lastReadUpdatedAtMillis = QuranPersonalStore.lastReadUpdatedAtMillis(context)

        val quranPayload = mutableMapOf<String, Any>(
            "uid" to userId,
            "bookmarks" to bookmarks,
            "notes" to notes,
            "hifz" to hifz,
            "chapters" to chapters,
            "juzs" to juzs
        )
        if (lastReadJuz != null) quranPayload["lastReadJuz"] = lastReadJuz
        if (lastReadVerseKey != null) quranPayload["lastReadVerseKey"] = lastReadVerseKey
        if (lastReadUpdatedAtMillis != null) quranPayload["lastReadUpdatedAtMillis"] = lastReadUpdatedAtMillis

        userDocRef.collection("quran").document("personal")
            .set(quranPayload, SetOptions.merge())
            .await()

        // 2. Upload Prayer Tracker Data
        val prayerRecords = PrayerTrackerStore.getAllRecords(context)
        val bestStreak = PrayerTrackerStore.bestStreak(context)
        val trackerPayload = mapOf<String, Any>(
            "uid" to userId,
            "records" to prayerRecords,
            "bestStreak" to bestStreak
        )
        userDocRef.collection("tracker").document("prayer")
            .set(trackerPayload, SetOptions.merge())
            .await()

        // 3. Upload Dhikr Counters Data
        val dhikrCounters = DhikrStore.getAllCounters(context)
        val dhikrPayload = mapOf<String, Any>(
            "uid" to userId,
            "counters" to dhikrCounters
        )
        userDocRef.collection("tasbih").document("dhikr")
            .set(dhikrPayload, SetOptions.merge())
            .await()

        // 4. Upload Account Preferences Data (/users/{userId}/preferences/settings)
        val locManual = locationStore.manualLocation()
        val locMode = locationStore.mode()
        val locMap = mutableMapOf<String, Any>(
            "isAutomatic" to (locMode == LocationMode.GPS),
            "mode" to locMode.name
        )
        if (locManual != null) {
            locMap["city"] = locManual.label
            locMap["label"] = locManual.label
            locMap["latitude"] = locManual.latitude
            locMap["longitude"] = locManual.longitude
            locManual.countryCode?.let { locMap["countryCode"] = it }

            val matchedCity = app.kamy.saatApp.infrastructure.local.LocalCityCatalog.findExact(locManual.label)
                ?: app.kamy.saatApp.infrastructure.local.LocalCityCatalog.findNearest(locManual.latitude, locManual.longitude)
            val regionId = matchedCity?.name?.lowercase(java.util.Locale.ROOT)?.replace(" ", "_")
                ?: locManual.label.lowercase(java.util.Locale.ROOT).replace(" ", "_")
            locMap["regionId"] = regionId
            locMap["cityCode"] = regionId
        }

        val notifMap = mapOf<String, Any>(
            "fajr" to prayerNotificationPrefs.isPrayerEnabled(PrayerType.FAJR),
            "dhuhr" to prayerNotificationPrefs.isPrayerEnabled(PrayerType.DHUHR),
            "asr" to prayerNotificationPrefs.isPrayerEnabled(PrayerType.ASR),
            "maghrib" to prayerNotificationPrefs.isPrayerEnabled(PrayerType.MAGHRIB),
            "isha" to prayerNotificationPrefs.isPrayerEnabled(PrayerType.ISHA),
            "imsak" to prayerNotificationPrefs.isImsakEnabled(),
            "midnight" to prayerNotificationPrefs.isMidnightEnabled(),
            "firstThird" to prayerNotificationPrefs.isFirstThirdEnabled(),
            "tahajud" to prayerNotificationPrefs.isTahajudEnabled(),
            "tahajudHour" to prayerNotificationPrefs.tahajudHour(),
            "tahajudMinute" to prayerNotificationPrefs.tahajudMinute(),
            "tahajudDays" to prayerNotificationPrefs.tahajudDays().toList(),
            "dhuha" to prayerNotificationPrefs.isDhuhaEnabled(),
            "dhuhaHour" to prayerNotificationPrefs.dhuhaHour(),
            "dhuhaMinute" to prayerNotificationPrefs.dhuhaMinute(),
            "dhuhaDays" to prayerNotificationPrefs.dhuhaDays().toList(),
            "yasin" to prayerNotificationPrefs.isYasinReminderEnabled(),
            "kahf" to prayerNotificationPrefs.isKahfReminderEnabled(),
            "importantDays" to prayerNotificationPrefs.isImportantDaysReminderEnabled(),
            "adhanSound" to prayerNotificationPrefs.isAdhanSoundEnabled(),
            "monThuFast" to prayerNotificationPrefs.isMonThuFastEnabled(),
            "quranReminder" to prayerNotificationPrefs.isQuranReminderEnabled(),
            "liveCountdown" to prayerNotificationPrefs.isLiveCountdownEnabled(),
            "selectedAdhanVoiceId" to adhanPrefs.currentVoice().id,
            "selectedFajrAdhanVoiceId" to adhanPrefs.currentFajrVoice().id,
            "dailyVerse" to dailyVerseStore.isEnabled(),
            "dailyVerseHour" to dailyVerseStore.morningHour(),
            "dailyVerseMinute" to dailyVerseStore.morningMinute(),
            "dailyVerseDays" to dailyVerseStore.days().toList()
        )

        val surahReminders = surahReminderStore.getReminders().map {
            mapOf(
                "id" to it.id,
                "surahNumber" to it.surahNumber,
                "surahName" to it.surahName,
                "weekday" to it.weekday,
                "hour" to it.hour,
                "minute" to it.minute,
                "enabled" to it.enabled
            )
        }

        val quranPrefsMap = mapOf<String, Any>(
            "translationId" to translationStore.translationId.value,
            "translationName" to translationStore.translationName.value,
            "fontScale" to translationStore.fontScale.value.toDouble(),
            "readingStyle" to translationStore.readingStyle.value.name,
            "showTranslation" to translationStore.showTranslation.value,
            "showTransliteration" to translationStore.showTransliteration.value,
            "isTajweedEnabled" to translationStore.isTajweedEnabled.value,
            "arabicTextType" to translationStore.arabicTextType.value.name,
            "recitationId" to translationStore.recitationId.value,
            "tafsirSource" to translationStore.tafsirSource.value
        )

        val preferencesPayload = mapOf<String, Any>(
            "uid" to userId,
            "language" to AppLanguageStore.from(context).current().tag,
            "theme" to themeStore.currentTheme().key,
            "prayerMethod" to prayerMethodStore.current().rawValue,
            "prayerMadhab" to prayerMethodStore.currentMadhab().rawValue,
            "quran" to quranPrefsMap,
            "location" to locMap,
            "notifications" to notifMap,
            "surahReminders" to surahReminders,
            "updatedAtMillis" to System.currentTimeMillis()
        )

        userDocRef.collection("preferences").document("settings")
            .set(preferencesPayload, SetOptions.merge())
            .await()

        // 5. Upload Encyclopedia Reading Progress (/users/{userId}/encyclopedia/reading)
        val encyclopediaPages = EncyclopediaReadingProgressStore.allPages(context)
        if (encyclopediaPages.isNotEmpty()) {
            val encPayload = mapOf<String, Any>(
                "uid" to userId,
                "pages" to encyclopediaPages,
                "updatedAtMillis" to System.currentTimeMillis()
            )
            userDocRef.collection("encyclopedia").document("reading")
                .set(encPayload, SetOptions.merge())
                .await()
        }

        // 6. Update Root User Profile Document (Sync Timestamp)
        val profileMeta = mapOf<String, Any>(
            "uid" to userId,
            "lastLoginAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        userDocRef.set(profileMeta, SetOptions.merge()).await()
    }

    override suspend fun deleteCloudUserData(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val userDocRef = firestore.collection("users").document(userId)
            val docsToDelete = listOf(
                userDocRef.collection("quran").document("personal"),
                userDocRef.collection("tracker").document("prayer"),
                userDocRef.collection("tasbih").document("dhikr"),
                userDocRef.collection("preferences").document("settings"),
                userDocRef.collection("encyclopedia").document("reading")
            )
            for (doc in docsToDelete) {
                runCatching { doc.delete().await() }
            }
            runCatching { userDocRef.delete().await() }
            prefs.edit().remove("last_sync_time_millis_$userId").apply()
            _syncState.value = SyncState(status = SyncStatus.IDLE)
            Unit
        }
    }

    companion object {
        private const val TAG = "FirestoreUserDataSync"
        private const val PREFS_NAME = "saat_cloud_sync_prefs"
    }
}
