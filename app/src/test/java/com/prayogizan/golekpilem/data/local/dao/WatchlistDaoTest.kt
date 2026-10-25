package com.prayogizan.golekpilem.data.local.dao

import com.prayogizan.golekpilem.data.local.BaseRoomTest
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying [WatchlistDao] operations inside an in-memory Room database.
 *
 * Validates bookmark insertion, conflict replacement, reactive observation ordered by addedAt DESC,
 * dynamic count emissions, and deletion.
 */
class WatchlistDaoTest : BaseRoomTest() {

    @Test
    fun insertWatchlistAndObserveWatchlistReturnsSavedItemsOrderedByAddedAtDesc() = runBlocking {
        val item1 = WatchlistEntity(
            id = 1,
            mediaType = "MOVIE",
            title = "Movie 1",
            posterPath = "/path1.jpg",
            backdropPath = null,
            voteAverage = 7.5,
            releaseDate = "2024-01-01",
            overview = "Overview 1",
            addedAt = 1000L
        )
        val item2 = WatchlistEntity(
            id = 2,
            mediaType = "TV",
            title = "TV 2",
            posterPath = "/path2.jpg",
            backdropPath = null,
            voteAverage = 8.0,
            releaseDate = "2024-02-01",
            overview = "Overview 2",
            addedAt = 2000L
        )

        watchlistDao.insertWatchlist(item1)
        watchlistDao.insertWatchlist(item2)

        val items = watchlistDao.observeWatchlist().first()
        assertEquals(2, items.size)
        // Ordered by addedAt DESC
        assertEquals(2, items[0].id)
        assertEquals(1, items[1].id)
    }

    @Test
    fun insertWatchlistReplacesPriorEntryOnDuplicateIdAndMediaType() = runBlocking {
        val original = WatchlistEntity(
            id = 10,
            mediaType = "MOVIE",
            title = "Original Title",
            posterPath = null,
            backdropPath = null,
            voteAverage = 6.0,
            releaseDate = "2023-01-01",
            overview = "Overview",
            addedAt = 100L
        )
        val updated = WatchlistEntity(
            id = 10,
            mediaType = "MOVIE",
            title = "Updated Title",
            posterPath = "/updated.jpg",
            backdropPath = null,
            voteAverage = 8.5,
            releaseDate = "2023-01-01",
            overview = "Updated Overview",
            addedAt = 500L
        )

        watchlistDao.insertWatchlist(original)
        watchlistDao.insertWatchlist(updated)

        val items = watchlistDao.observeWatchlist().first()
        assertEquals(1, items.size)
        assertEquals("Updated Title", items.first().title)
        assertEquals(8.5, items.first().voteAverage, 0.01)
    }

    @Test
    fun isWatchlistedAndCheckIsWatchlistedReflectCorrectState() = runBlocking {
        val item = WatchlistEntity(
            id = 10,
            mediaType = "MOVIE",
            title = "Movie 10",
            posterPath = null,
            backdropPath = null,
            voteAverage = 6.5,
            releaseDate = "2023-05-10",
            overview = "Overview",
            addedAt = 500L
        )

        assertFalse(watchlistDao.isWatchlisted(10, "MOVIE").first())
        assertFalse(watchlistDao.checkIsWatchlisted(10, "MOVIE"))

        watchlistDao.insertWatchlist(item)

        assertTrue(watchlistDao.isWatchlisted(10, "MOVIE").first())
        assertTrue(watchlistDao.checkIsWatchlisted(10, "MOVIE"))

        // Different mediaType with same id should return false
        assertFalse(watchlistDao.checkIsWatchlisted(10, "TV"))
    }

    @Test
    fun observeWatchlistCountReflectsDynamicallyOnInsertAndRemove() = runBlocking {
        val countInitial = watchlistDao.observeWatchlistCount().first()
        assertEquals(0, countInitial)

        val item1 = WatchlistEntity(
            id = 1,
            mediaType = "MOVIE",
            title = "Movie 1",
            posterPath = null,
            backdropPath = null,
            voteAverage = 7.0,
            releaseDate = "2024-01-01",
            overview = "Overview",
            addedAt = 100L
        )
        val item2 = WatchlistEntity(
            id = 2,
            mediaType = "MOVIE",
            title = "Movie 2",
            posterPath = null,
            backdropPath = null,
            voteAverage = 8.0,
            releaseDate = "2024-02-01",
            overview = "Overview",
            addedAt = 200L
        )

        watchlistDao.insertWatchlist(item1)
        assertEquals(1, watchlistDao.observeWatchlistCount().first())

        watchlistDao.insertWatchlist(item2)
        assertEquals(2, watchlistDao.observeWatchlistCount().first())

        watchlistDao.deleteWatchlist(1, "MOVIE")
        assertEquals(1, watchlistDao.observeWatchlistCount().first())
    }

    @Test
    fun deleteWatchlistRemovesItemAndDecreasesCount() = runBlocking {
        val item = WatchlistEntity(
            id = 20,
            mediaType = "MOVIE",
            title = "Movie 20",
            posterPath = null,
            backdropPath = null,
            voteAverage = 7.0,
            releaseDate = "2024-03-01",
            overview = "Overview",
            addedAt = 1000L
        )

        watchlistDao.insertWatchlist(item)
        val deletedRows = watchlistDao.deleteWatchlist(20, "MOVIE")
        assertEquals(1, deletedRows)

        val items = watchlistDao.observeWatchlist().first()
        assertTrue(items.isEmpty())

        assertFalse(watchlistDao.checkIsWatchlisted(20, "MOVIE"))
    }
}
