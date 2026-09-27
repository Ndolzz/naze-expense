package com.naze.expense.data.backup

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.naze.expense.AppContainer
import java.io.File

/**
 * Backup otomatis ke folder publik Documents/NazeFinancialOS.
 *
 * Folder & file di penyimpanan publik TIDAK ikut terhapus saat aplikasi
 * di-uninstall. Saat aplikasi dipasang ulang, data akan dipulihkan otomatis
 * dari file backup tersebut. 100% offline, tanpa server.
 */
class AutoBackupManager(
    private val context: Context,
    private val container: AppContainer,
) {

    private val backup = BackupManager(container)
    private val folderName = "NazeFinancialOS"
    private val fileName = "auto-backup.json"

    /** Simpan backup terbaru ke folder publik. */
    suspend fun backupNow(): Boolean {
        val db = container.database
        val isEmpty = db.transactionDao().getAll().isEmpty() &&
            db.categoryDao().getAll().isEmpty()
        if (isEmpty) return true // belum ada data, tidak perlu backup

        return runCatching {
            val text = backup.export()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                writeViaMediaStore(text)
            } else {
                writeLegacy(text)
            }
        }.isSuccess
    }

    /**
     * Pulihkan data dari folder publik HANYA jika database masih kosong
     * (misal: aplikasi baru dipasang ulang setelah uninstall).
     */
    suspend fun restoreIfEmpty(): Boolean {
        val db = container.database
        if (db.transactionDao().getAll().isNotEmpty() ||
            db.categoryDao().getAll().isNotEmpty()
        ) return false

        val text = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) readViaMediaStore()
            else readLegacy()
        }.getOrNull() ?: return false

        return runCatching { backup.import(text) }.isSuccess
    }

    // ---------- Android 10+ (MediaStore, tanpa permission) ----------

    private fun mediaStoreCollection(): Uri =
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

    private fun findMediaStoreFile(): Uri? {
        val resolver = context.contentResolver
        resolver.query(
            mediaStoreCollection(),
            arrayOf(MediaStore.MediaColumns._ID),
            MediaStore.MediaColumns.DISPLAY_NAME + "=?",
            arrayOf(fileName),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return ContentUris.withAppendedId(mediaStoreCollection(), cursor.getLong(0))
            }
        }
        return null
    }

    private fun writeViaMediaStore(text: String) {
        val resolver = context.contentResolver
        // Timpa file lama supaya tidak menumpuk
        findMediaStoreFile()?.let { resolver.delete(it, null, null) }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/" + folderName)
        }
        val uri = resolver.insert(mediaStoreCollection(), values)
            ?: error("Gagal membuat file backup")
        resolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
            ?: error("Gagal menulis backup")
    }

    private fun readViaMediaStore(): String? {
        val uri = findMediaStoreFile() ?: return null
        return context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().decodeToString()
        }
    }

    // ---------- Android 9 ke bawah (File API + permission WRITE) ----------

    private fun legacyFile(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            folderName,
        )
        if (!dir.exists()) dir.mkdirs()
        return File(dir, fileName)
    }

    private fun writeLegacy(text: String) {
        legacyFile().writeText(text)
    }

    private fun readLegacy(): String? {
        val file = legacyFile()
        return if (file.exists()) file.readText() else null
    }
}
