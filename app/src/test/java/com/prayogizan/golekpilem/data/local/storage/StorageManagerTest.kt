package com.prayogizan.golekpilem.data.local.storage

import android.content.Context
import androidx.room.withTransaction
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.File

class StorageManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val database: GolekPilemDatabase = mockk(relaxed = true)
    private val mediaDao: MediaDao = mockk(relaxed = true)
    private val remoteKeyDao: RemoteKeyDao = mockk(relaxed = true)
    private val imageLoader: ImageLoader = mockk(relaxed = true)
    private val diskCache: DiskCache = mockk(relaxed = true)
    private val memoryCache: MemoryCache = mockk(relaxed = true)

    private lateinit var storageManager: StorageManager

    @Before
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        every { database.mediaDao() } returns mediaDao
        every { database.remoteKeyDao() } returns remoteKeyDao
        coEvery { database.withTransaction<Any?>(any()) } coAnswers {
            val lambda = args.filterIsInstance<Function1<*, *>>().firstOrNull()
                ?: error("No Function1 argument found in withTransaction args: $args")
            @Suppress("UNCHECKED_CAST")
            (lambda as (suspend () -> Any?)).invoke()
        }

        every { imageLoader.diskCache } returns diskCache
        every { imageLoader.memoryCache } returns memoryCache

        storageManager = StorageManager(
            context = context,
            database = database,
            imageLoader = imageLoader,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `getStorageStats returns calculated metrics when files exist`() = runTest(testDispatcher) {
        val tempDbFile = File.createTempFile("test_golek_pilem", ".db")
        tempDbFile.deleteOnExit()
        val testBytes = ByteArray(4096)
        tempDbFile.writeBytes(testBytes)

        every { context.getDatabasePath(GolekPilemDatabase.DATABASE_NAME) } returns tempDbFile
        every { diskCache.size } returns 52_428_800L // 50.0 MB

        val cacheDir = File(System.getProperty("java.io.tmpdir") ?: "", "test_image_cache_non_existent")
        every { context.cacheDir } returns cacheDir

        val stats = storageManager.getStorageStats()

        assertNotNull(stats)
        assertEquals(4096L, stats.dbSizeBytes)
        assertEquals(52_428_800L, stats.imageCacheSizeBytes)
        assertEquals("4.0 KB", stats.dbSizeFormatted)
        assertEquals("50.0 MB", stats.imageCacheSizeFormatted)
        assertEquals("50.0 MB", stats.totalSizeFormatted)

        tempDbFile.delete()
    }


    @Test
    fun `getStorageStats handles missing database and image cache gracefully`() = runTest(testDispatcher) {
        every { context.getDatabasePath(GolekPilemDatabase.DATABASE_NAME) } returns null
        every { imageLoader.diskCache } returns null

        val cacheDir = File(System.getProperty("java.io.tmpdir") ?: "", "test_image_cache_empty")
        every { context.cacheDir } returns cacheDir


        val stats = storageManager.getStorageStats()

        assertEquals(0L, stats.dbSizeBytes)
        assertEquals(0L, stats.imageCacheSizeBytes)
        assertEquals("0 B", stats.dbSizeFormatted)
        assertEquals("0 B", stats.imageCacheSizeFormatted)
        assertEquals("0 B", stats.totalSizeFormatted)
    }

    @Test
    fun `clearImageCache invokes diskCache and memoryCache clear`() = runTest(testDispatcher) {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "test_image_cache_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        File(tempDir, "image_cache").mkdirs()
        every { context.cacheDir } returns tempDir

        storageManager.clearImageCache()

        verify(exactly = 1) { diskCache.clear() }
        verify(exactly = 1) { memoryCache.clear() }

        tempDir.deleteRecursively()
    }

    @Test
    fun `purgeExpiredCatalog executes transaction and deletes items older than cutoff exempting watchlist`() = runTest(testDispatcher) {
        coEvery { mediaDao.purgeExpiredCatalogExceptWatchlist(any()) } returns 24
        coEvery { remoteKeyDao.purgeExpiredKeysExceptWatchlist(any()) } returns 24

        val purgedCount = storageManager.purgeExpiredCatalog(retentionDays = 7)

        assertEquals(24, purgedCount)
        coVerify(exactly = 1) { mediaDao.purgeExpiredCatalogExceptWatchlist(any()) }
        coVerify(exactly = 1) { remoteKeyDao.purgeExpiredKeysExceptWatchlist(any()) }
    }

    @Test(expected = CancellationException::class)
    fun `purgeExpiredCatalog rethrows CancellationException without swallowing`() = runTest(testDispatcher) {
        coEvery { mediaDao.purgeExpiredCatalogExceptWatchlist(any()) } throws CancellationException("Job was cancelled")

        storageManager.purgeExpiredCatalog(retentionDays = 7)
    }
}
