package app.kamy.saatApp.infrastructure.repository

import android.content.Context
import android.content.SharedPreferences
import app.kamy.saatApp.domain.model.HifzEntry
import app.kamy.saatApp.domain.model.VerseBookmark
import app.kamy.saatApp.domain.model.VerseNote
import app.kamy.saatApp.domain.repository.SyncState
import app.kamy.saatApp.domain.repository.SyncStatus
import app.kamy.saatApp.domain.repository.UserDataSyncRepository
import app.kamy.saatApp.infrastructure.preferences.DhikrStore
import app.kamy.saatApp.infrastructure.preferences.PrayerTrackerStore
import app.kamy.saatApp.infrastructure.preferences.QuranPersonalStore
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
    private val auth: FirebaseAuth
) : UserDataSyncRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val changeTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var isApplyingCloudUpdate = false

    private val quranPrefs = context.getSharedPreferences("saat_quran_personal", Context.MODE_PRIVATE)
    private val prayerPrefs = context.getSharedPreferences("saat_prayer_tracker", Context.MODE_PRIVATE)
    private val dhikrPrefs = context.getSharedPreferences("saat_dhikr", Context.MODE_PRIVATE)

    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        if (!isApplyingCloudUpdate && auth.currentUser != null) {
            changeTrigger.tryEmit(Unit)
        }
    }

    private val _syncState = MutableStateFlow(
        SyncState(
            status = SyncStatus.IDLE,
            lastSyncTimeMillis = prefs.getLong(KEY_LAST_SYNC_TIME, 0L).takeIf { it > 0 }
        )
    )
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        quranPrefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
        prayerPrefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
        dhikrPrefs.registerOnSharedPreferenceChangeListener(prefChangeListener)

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
                            prefs.edit().putLong(KEY_LAST_SYNC_TIME, now).apply()
                            _syncState.value = SyncState(
                                status = SyncStatus.SUCCESS,
                                lastSyncTimeMillis = now
                            )
                        }
                    }
                }
        }
    }

    override suspend fun syncAll(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("ID Pengguna tidak valid"))
        }

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
            prefs.edit().putLong(KEY_LAST_SYNC_TIME, now).apply()

            _syncState.value = SyncState(
                status = SyncStatus.SUCCESS,
                lastSyncTimeMillis = now,
                errorMessage = null
            )
        }.onFailure { error ->
            val message = error.localizedMessage ?: "Gagal menyinkronkan data dengan cloud"
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
            prefs.edit().putLong(KEY_LAST_SYNC_TIME, now).apply()
            _syncState.value = SyncState(
                status = SyncStatus.SUCCESS,
                lastSyncTimeMillis = now
            )
        }
    }

    override suspend fun downloadCloudToLocal(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.failure(IllegalArgumentException("ID Pengguna tidak valid"))
        runCatching {
            downloadCloudToLocalInternal(userId)
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_SYNC_TIME, now).apply()
            _syncState.value = SyncState(
                status = SyncStatus.SUCCESS,
                lastSyncTimeMillis = now
            )
        }
    }

    private suspend fun downloadCloudToLocalInternal(userId: String) {
        isApplyingCloudUpdate = true
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
                if (bookmarks.isNotEmpty()) {
                    QuranPersonalStore.restoreBookmarks(context, bookmarks)
                }

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
                if (notes.isNotEmpty()) {
                    QuranPersonalStore.restoreNotes(context, notes)
                }

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

        // 4. Update Root User Profile Document (Sync Timestamp)
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
                userDocRef.collection("tasbih").document("dhikr")
            )
            for (doc in docsToDelete) {
                runCatching { doc.delete().await() }
            }
            runCatching { userDocRef.delete().await() }
            prefs.edit().remove(KEY_LAST_SYNC_TIME).apply()
            _syncState.value = SyncState(status = SyncStatus.IDLE)
            Unit
        }
    }

    companion object {
        private const val PREFS_NAME = "saat_cloud_sync_prefs"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time_millis"
    }
}

