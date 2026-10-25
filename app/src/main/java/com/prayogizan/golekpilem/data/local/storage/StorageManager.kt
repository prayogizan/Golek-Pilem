package com.prayogizan.golekpilem.data.local.storage

import android.content.Context
import androidx.room.withTransaction
import coil3.ImageLoader
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.domain.model.StorageStats
import com.prayogizan.golekpilem.domain.repository.StorageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

/**
 * Concrete implementation of [StorageRepository] governing local disk storage and cache invalidation.
 *
 * Coordinates Room database file size inspection, Coil persistent disk cache clearing,
 * and atomic catalog pruning operations while strictly exempting user-curated watchlist items.
 *
 * @param context Android application context utilized for resolving cache directories and database files.
 * @param database The primary Room database instance.
 * @param imageLoader The global Coil 3 image engine whose caches are governed.
 * @param ioDispatcher Coroutine dispatcher managing background disk I/O operations.
 */
class StorageManager(
    private val context: Context,
    private val database: GolekPilemDatabase,
    private val imageLoader: ImageLoader,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StorageRepository {

    override suspend fun getStorageStats(): StorageStats = withContext(ioDispatcher) {
        val dbBytes = calculateDatabaseBytes()
        val imageCacheBytes = calculateImageCacheBytes()
        val totalBytes = dbBytes + imageCacheBytes

        StorageStats(
            dbSizeBytes = dbBytes,
            imageCacheSizeBytes = imageCacheBytes,
            dbSizeFormatted = formatBytes(dbBytes),
            imageCacheSizeFormatted = formatBytes(imageCacheBytes),
            totalSizeFormatted = formatBytes(totalBytes)
        )
    }

    override suspend fun clearImageCache(): Unit = withContext(ioDispatcher) {
        try {
            imageLoader.diskCache?.clear()
            imageLoader.memoryCache?.clear()

            val cacheDir = context.cacheDir.resolve(IMAGE_CACHE_DIR)
            if (cacheDir.exists() && cacheDir.isDirectory) {
                cacheDir.listFiles()?.forEach { file ->
                    file.deleteRecursively()
                }
            }
        } catch (e: CancellationException) {
            throw e
        }
    }

    override suspend fun purgeExpiredCatalog(retentionDays: Int): Int = withContext(ioDispatcher) {
        try {
            val cutoffTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays.toLong())
            database.withTransaction {
                val purgedItems = database.mediaDao().purgeExpiredCatalogExceptWatchlist(cutoffTimestamp)
                database.remoteKeyDao().purgeExpiredKeysExceptWatchlist(cutoffTimestamp)
                purgedItems
            }
        } catch (e: CancellationException) {
            throw e
        }
    }

    private fun calculateDatabaseBytes(): Long {
        val dbFile = context.getDatabasePath(GolekPilemDatabase.DATABASE_NAME)
        if (dbFile == null || !dbFile.exists()) return 0L

        val walFile = File("${dbFile.path}-wal")
        val shmFile = File("${dbFile.path}-shm")

        var size = dbFile.length()
        if (walFile.exists()) size += walFile.length()
        if (shmFile.exists()) size += shmFile.length()
        return size
    }

    private fun calculateImageCacheBytes(): Long {
        val cacheDir = context.cacheDir.resolve(IMAGE_CACHE_DIR)
        return if (cacheDir.exists() && cacheDir.isDirectory) {
            cacheDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } else {
            imageLoader.diskCache?.size ?: 0L
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / 1024.0.pow(digitGroups.toDouble())
        return if (digitGroups == 0) {
            String.format(Locale.US, "%d B", bytes)
        } else {
            String.format(Locale.US, "%.1f %s", value, units[digitGroups])
        }
    }

    companion object {
        private const val IMAGE_CACHE_DIR = "image_cache"
    }
}
