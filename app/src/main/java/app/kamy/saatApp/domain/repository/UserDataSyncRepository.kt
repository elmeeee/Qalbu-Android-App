package app.kamy.saatApp.domain.repository

import kotlinx.coroutines.flow.StateFlow

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

data class SyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val lastSyncTimeMillis: Long? = null,
    val errorMessage: String? = null
)

interface UserDataSyncRepository {
    val syncState: StateFlow<SyncState>

    /**
     * Performs a two-way sync: fetches cloud documents, merges them into local stores,
     * and uploads the combined state back to Firestore.
     */
    suspend fun syncAll(userId: String): Result<Unit>

    /**
     * Uploads all current local data (Quran bookmarks, notes, khatam, prayer tracker, dhikr) to Firestore.
     */
    suspend fun uploadLocalToCloud(userId: String): Result<Unit>

    /**
     * Downloads cloud data from Firestore and restores it to local stores.
     */
    suspend fun downloadCloudToLocal(userId: String): Result<Unit>
}

