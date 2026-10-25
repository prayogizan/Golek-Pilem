package com.prayogizan.golekpilem.data.local.dao

import com.prayogizan.golekpilem.data.local.BaseRoomTest
import com.prayogizan.golekpilem.data.local.entity.RemoteKeyEntity
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying [RemoteKeyDao] operations inside an in-memory Room database.
 *
 * Validates pagination cursor insertion, lookup by composite key (id, mediaType), category cleanup,
 * and selective cursor pruning with watchlist preservation.
 */
class RemoteKeyDaoTest : BaseRoomTest() {

    @Test
    fun insertKeysAndGetRemoteKeyRetrievesStoredCursorKey() = runBlocking {
        val key = RemoteKeyEntity(id = 10, mediaType = "MOVIE", prevKey = null, nextKey = 2)

        remoteKeyDao.insertKeys(listOf(key))

        val retrieved = remoteKeyDao.getRemoteKey(10, "MOVIE")
        assertNotNull(retrieved)
        assertEquals(10, retrieved?.id)
        assertEquals("MOVIE", retrieved?.mediaType)
        assertNull(retrieved?.prevKey)
        assertEquals(2, retrieved?.nextKey)
    }

    @Test
    fun insertKeysReplacesExistingKeyOnConflict() = runBlocking {
        val initialKey = RemoteKeyEntity(id = 10, mediaType = "MOVIE", prevKey = 1, nextKey = 2)
        val updatedKey = RemoteKeyEntity(id = 10, mediaType = "MOVIE", prevKey = 2, nextKey = 3)

        remoteKeyDao.insertKeys(listOf(initialKey))
        remoteKeyDao.insertKeys(listOf(updatedKey))

        val retrieved = remoteKeyDao.getRemoteKey(10, "MOVIE")
        assertNotNull(retrieved)
        assertEquals(2, retrieved?.prevKey)
        assertEquals(3, retrieved?.nextKey)
    }

    @Test
    fun clearKeysRemovesOnlySpecifiedMediaType() = runBlocking {
        val movieKey = RemoteKeyEntity(id = 1, mediaType = "MOVIE", prevKey = null, nextKey = 2)
        val tvKey = RemoteKeyEntity(id = 2, mediaType = "TV", prevKey = null, nextKey = 2)

        remoteKeyDao.insertKeys(listOf(movieKey, tvKey))

        val deletedCount = remoteKeyDao.clearKeys("MOVIE")
        assertEquals(1, deletedCount)

        assertNull(remoteKeyDao.getRemoteKey(1, "MOVIE"))
        assertNotNull(remoteKeyDao.getRemoteKey(2, "TV"))
    }

    @Test
    fun purgeExpiredKeysExceptWatchlistProtectsWatchlistedEntries() = runBlocking {
        val cutoff = 500_000L
        val staleWatchlistedKey = RemoteKeyEntity(id = 1, mediaType = "MOVIE", prevKey = null, nextKey = 2, lastUpdated = 100_000L)
        val staleUnsavedKey = RemoteKeyEntity(id = 2, mediaType = "MOVIE", prevKey = null, nextKey = 2, lastUpdated = 100_000L)
        val freshKey = RemoteKeyEntity(id = 3, mediaType = "MOVIE", prevKey = 1, nextKey = 3, lastUpdated = 900_000L)

        remoteKeyDao.insertKeys(listOf(staleWatchlistedKey, staleUnsavedKey, freshKey))

        // Bookmark id=1 in watchlist
        watchlistDao.insertWatchlist(
            WatchlistEntity(
                id = 1,
                mediaType = "MOVIE",
                title = "Bookmarked",
                posterPath = null,
                backdropPath = null,
                voteAverage = 8.0,
                releaseDate = "2024-01-01",
                overview = "Overview",
                addedAt = 100_000L
            )
        )

        val purgedCount = remoteKeyDao.purgeExpiredKeysExceptWatchlist(cutoff)
        assertEquals(1, purgedCount) // Only key id=2 should be purged

        assertNotNull(remoteKeyDao.getRemoteKey(1, "MOVIE"))
        assertNull(remoteKeyDao.getRemoteKey(2, "MOVIE"))
        assertNotNull(remoteKeyDao.getRemoteKey(3, "MOVIE"))
    }
}
