package com.prayogizan.golekpilem.data.local.dao

import androidx.paging.PagingSource
import com.prayogizan.golekpilem.data.local.BaseRoomTest
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying [MediaDao] operations inside an in-memory Room database.
 *
 * Validates batch entity insertion, reactive [PagingSource] generation, category filtering,
 * conflict replacement, and selective catalog pruning with watchlist exemption.
 */
class MediaDaoTest : BaseRoomTest() {

    @Test
    fun insertAllAndCountByMediaTypeVerifiesBatchInsertion() = runBlocking {
        val movies = listOf(
            createMediaItem(id = 1, mediaType = "MOVIE", pageIndex = 1),
            createMediaItem(id = 2, mediaType = "MOVIE", pageIndex = 1),
            createMediaItem(id = 3, mediaType = "MOVIE", pageIndex = 2)
        )
        val series = listOf(
            createMediaItem(id = 10, mediaType = "TV", pageIndex = 1)
        )

        mediaDao.insertAll(movies)
        mediaDao.insertAll(series)

        assertEquals(3, mediaDao.countByMediaType("MOVIE"))
        assertEquals(1, mediaDao.countByMediaType("TV"))
    }

    @Test
    fun getPagedMediaReturnsPagingSourceWithOrderedItemsForTargetMediaType() = runBlocking {
        val movie1 = createMediaItem(id = 1, mediaType = "MOVIE", pageIndex = 1, title = "Alpha")
        val movie2 = createMediaItem(id = 2, mediaType = "MOVIE", pageIndex = 2, title = "Beta")
        val series = createMediaItem(id = 3, mediaType = "TV", pageIndex = 1, title = "Gamma")

        mediaDao.insertAll(listOf(movie2, movie1, series))

        val pagingSource = mediaDao.getPagedMedia("MOVIE")
        val loadResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(loadResult is PagingSource.LoadResult.Page)
        val page = loadResult as PagingSource.LoadResult.Page
        assertEquals(2, page.data.size)
        // Ordered by pageIndex ASC, id ASC
        assertEquals(1, page.data[0].id)
        assertEquals(2, page.data[1].id)
    }

    @Test
    fun insertAllReplacesConflictingRecordsOnDuplicateIdAndMediaType() = runBlocking {
        val initialItem = createMediaItem(id = 100, mediaType = "MOVIE", title = "Original Title", voteAverage = 7.0)
        val updatedItem = createMediaItem(id = 100, mediaType = "MOVIE", title = "Updated Title", voteAverage = 8.5)

        mediaDao.insertAll(listOf(initialItem))
        assertEquals(1, mediaDao.countByMediaType("MOVIE"))

        mediaDao.insertAll(listOf(updatedItem))
        assertEquals(1, mediaDao.countByMediaType("MOVIE"))

        val pagingSource = mediaDao.getPagedMedia("MOVIE")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 5, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertEquals("Updated Title", result.data.first().title)
        assertEquals(8.5, result.data.first().voteAverage, 0.01)
    }

    @Test
    fun clearByMediaTypeRemovesOnlyItemsOfTargetMediaType() = runBlocking {
        val movies = listOf(createMediaItem(id = 1, mediaType = "MOVIE", pageIndex = 1))
        val series = listOf(createMediaItem(id = 2, mediaType = "TV", pageIndex = 1))

        mediaDao.insertAll(movies + series)

        val deletedCount = mediaDao.clearByMediaType("MOVIE")
        assertEquals(1, deletedCount)
        assertEquals(0, mediaDao.countByMediaType("MOVIE"))
        assertEquals(1, mediaDao.countByMediaType("TV"))
    }

    @Test
    fun purgeOlderThanDeletesStaleEntriesAndPreservesFreshEntries() = runBlocking {
        val now = 1_000_000L
        val staleItem = createMediaItem(id = 1, mediaType = "MOVIE", pageIndex = 1, cachedAt = 100_000L)
        val freshItem = createMediaItem(id = 2, mediaType = "MOVIE", pageIndex = 1, cachedAt = 900_000L)

        mediaDao.insertAll(listOf(staleItem, freshItem))

        val deletedCount = mediaDao.purgeOlderThan(500_000L)
        assertEquals(1, deletedCount)
        assertEquals(1, mediaDao.countByMediaType("MOVIE"))

        val pagingSource = mediaDao.getPagedMedia("MOVIE")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 5, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertEquals(2, result.data.first().id)
    }

    @Test
    fun purgeExpiredCatalogExceptWatchlistProtectsBookmarkedItemsFromPruning() = runBlocking {
        val cutoff = 500_000L
        val expiredWatchlistedMovie = createMediaItem(id = 1, mediaType = "MOVIE", pageIndex = 1, cachedAt = 100_000L)
        val expiredUnsavedMovie = createMediaItem(id = 2, mediaType = "MOVIE", pageIndex = 1, cachedAt = 100_000L)
        val freshMovie = createMediaItem(id = 3, mediaType = "MOVIE", pageIndex = 1, cachedAt = 900_000L)

        mediaDao.insertAll(listOf(expiredWatchlistedMovie, expiredUnsavedMovie, freshMovie))

        // Bookmark movie id=1 in watchlist
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

        val prunedCount = mediaDao.purgeExpiredCatalogExceptWatchlist(cutoff)
        assertEquals(1, prunedCount) // Only movie 2 should be deleted

        assertEquals(2, mediaDao.countByMediaType("MOVIE"))

        val pagingSource = mediaDao.getPagedMedia("MOVIE")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 5, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        val survivingIds = result.data.map { it.id }
        assertTrue("Watchlisted item must survive pruning", survivingIds.contains(1))
        assertTrue("Fresh item must survive pruning", survivingIds.contains(3))
    }

    private fun createMediaItem(
        id: Int,
        mediaType: String,
        pageIndex: Int = 1,
        title: String = "Test $id",
        voteAverage: Double = 7.5,
        cachedAt: Long = System.currentTimeMillis()
    ): MediaItemEntity {
        return MediaItemEntity(
            id = id,
            mediaType = mediaType,
            title = title,
            posterPath = "/path$id.jpg",
            backdropPath = null,
            releaseDate = "2024-01-01",
            voteAverage = voteAverage,
            voteCount = 50,
            overview = "Overview $id",
            pageIndex = pageIndex,
            cachedAt = cachedAt
        )
    }
}
