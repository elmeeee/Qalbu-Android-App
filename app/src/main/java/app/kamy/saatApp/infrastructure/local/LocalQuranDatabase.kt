package app.kamy.saatApp.infrastructure.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalQuranDatabase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile
    private var database: SQLiteDatabase? = null

    @Synchronized
    fun openReadable(): SQLiteDatabase {
        database?.takeIf { it.isOpen }?.let { return it }
        val dbFile = ensureDatabaseFile()
        val db = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        database = db
        return db
    }

    @Synchronized
    fun warmUp() {
        try {
            openReadable().rawQuery("SELECT COUNT(*) FROM suras", null).use { cursor ->
                check(cursor.moveToFirst() && cursor.getInt(0) == 114) {
                    "Bundled Quran database is invalid (expected 114 suras)"
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Quran DB warm-up failed; reinstalling from assets", t)
            reinstallFromAssets()
            openReadable().rawQuery("SELECT COUNT(*) FROM suras", null).use { cursor ->
                check(cursor.moveToFirst() && cursor.getInt(0) == 114) {
                    "Bundled Quran database is invalid after reinstall"
                }
            }
        }
    }

    @Synchronized
    private fun reinstallFromAssets() {
        close()
        val dir = File(context.filesDir, "local_quran")
        if (!dir.exists()) dir.mkdirs()
        val dbFile = File(dir, "qurannew.db")
        val versionFile = File(dir, VERSION_FILE_NAME)
        if (dbFile.exists()) dbFile.delete()
        installFromAssets(dbFile)
        check(isSqliteFile(dbFile)) { "Extracted Quran database is not a valid SQLite file" }
        versionFile.writeText(DB_ASSET_VERSION.toString())
    }

    @Synchronized
    fun close() {
        database?.close()
        database = null
    }

    private fun ensureDatabaseFile(): File {
        val dir = File(context.filesDir, "local_quran")
        if (!dir.exists()) dir.mkdirs()
        val dbFile = File(dir, "qurannew.db")
        val versionFile = File(dir, VERSION_FILE_NAME)
        val installedVersion = versionFile.takeIf { it.exists() }?.readText()?.toIntOrNull()
        val needsInstall = installedVersion != DB_ASSET_VERSION ||
            !dbFile.exists() ||
            dbFile.length() < MIN_DB_BYTES ||
            !isSqliteFile(dbFile)

        if (needsInstall) {
            close()
            if (dbFile.exists()) dbFile.delete()
            installFromAssets(dbFile)
            check(isSqliteFile(dbFile)) {
                "Extracted Quran database is not a valid SQLite file"
            }
            versionFile.writeText(DB_ASSET_VERSION.toString())
        }
        return dbFile
    }

    private fun installFromAssets(target: File) {
        val assetNames = context.assets.list(ASSET_DIR).orEmpty().toSet()
        when {
            ASSET_DB_PLAIN in assetNames -> copyAsset("$ASSET_DIR/$ASSET_DB_PLAIN", target)
            ASSET_DB_GZ in assetNames -> extractGzipAsset("$ASSET_DIR/$ASSET_DB_GZ", target)
            else -> error("Missing Quran database asset in $ASSET_DIR/")
        }
    }

    private fun copyAsset(assetPath: String, target: File) {
        context.assets.open(assetPath).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun extractGzipAsset(assetPath: String, target: File) {
        context.assets.open(assetPath).use { raw ->
            val header = ByteArray(16)
            val read = raw.read(header)
            check(read >= 15) { "Quran asset is empty or truncated" }
        }
        context.assets.open(assetPath).use { input ->
            val source: InputStream = when {
                peekIsSqlite(input) -> input
                else -> GZIPInputStream(input)
            }
            target.outputStream().use { output -> source.copyTo(output) }
        }
    }

    private fun peekIsSqlite(input: InputStream): Boolean {
        input.mark(16)
        val header = ByteArray(15)
        val read = input.read(header)
        input.reset()
        return read == 15 && header.startsWith(SQLITE_MAGIC)
    }

    private fun isSqliteFile(file: File): Boolean {
        if (!file.exists() || file.length() < MIN_DB_BYTES) return false
        return file.inputStream().use { stream ->
            val magic = ByteArray(15)
            stream.read(magic) == 15 && magic.startsWith(SQLITE_MAGIC)
        }
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (size < prefix.size) return false
        for (i in prefix.indices) {
            if (this[i] != prefix[i]) return false
        }
        return true
    }

    companion object {
        private const val ASSET_DIR = "quran"
        private const val ASSET_DB_PLAIN = "qurannew.db"
        private const val ASSET_DB_GZ = "qurannew.db.gz"
        private const val VERSION_FILE_NAME = "quran_db_version"
        private const val TAG = "LocalQuranDatabase"
        /** Bump when bundled asset changes (forces re-copy). */
        private const val DB_ASSET_VERSION = 6
        private const val MIN_DB_BYTES = 15_000_000L
        private val SQLITE_MAGIC = "SQLite format 3".toByteArray(Charsets.US_ASCII)
    }
}
