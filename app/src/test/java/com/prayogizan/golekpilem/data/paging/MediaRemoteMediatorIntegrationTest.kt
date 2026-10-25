package com.prayogizan.golekpilem.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.prayogizan.golekpilem.data.local.BaseRoomTest
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity
import com.prayogizan.golekpilem.data.local.entity.RemoteKeyEntity
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.PaginatedResponseDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Integration test harness executing [MediaRemoteMediator] against an authentic in-memory Room database.
 *
 * Verifies transactional persistence, pagination cursor key coordination, cache freshness bypass,
 * and graceful error recovery under live SQLite operations.
 */
@OptIn(ExperimentalPagingApi::class)
class MediaRemoteMediatorIntegrationTest : BaseRoomTest() {

    private val apiService: TmdbApiService = mockk()
    private val pagingConfig = PagingConfig(pageSize = 20)

    @Test
    fun loadRefreshWithValidApiResponsePopulatesRoomDatabaseAndReturnsSuccess() = runBlocking {
        val moviesResponse = PaginatedResponseDto(
            page = 1,
            results = listOf(
                MovieItemDto(id = 1, title = "Movie One", posterPath = "/p1.jpg", releaseDate = "2024-01-01", voteAverage = 8.0),
                MovieItemDto(id = 2, title = "Movie Two", posterPath = "/p2.jpg", releaseDate = "2024-01-02", voteAverage = 7.5)
            ),
            totalPages = 5,
            totalResults = 100
        )
        coEvery { apiService.getMovies(page = 1) } returns moviesResponse

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState<Int, MediaItemEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue("Expected MediatorResult.Success", result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertFalse("endOfPaginationReached should be false", success.endOfPaginationReached)

        // Verify real Room database state
        assertEquals(2, mediaDao.countByMediaType("MOVIE"))

        val key1 = remoteKeyDao.getRemoteKey(1, "MOVIE")
        assertNotNull(key1)
        assertNull(key1?.prevKey)
        assertEquals(2, key1?.nextKey)

        val key2 = remoteKeyDao.getRemoteKey(2, "MOVIE")
        assertNotNull(key2)
        assertNull(key2?.prevKey)
        assertEquals(2, key2?.nextKey)
    }

    @Test
    fun loadAppendReachingFinalPageSetsEndOfPaginationReachedTrue() = runBlocking {
        // Pre-populate page 1 in Room
        val initialEntity = MediaItemEntity(
            id = 1,
            mediaType = "MOVIE",
            title = "Movie 1",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = 7.0,
            voteCount = 10,
            overview = "Overview",
            pageIndex = 1
        )
        val initialKey = RemoteKeyEntity(id = 1, mediaType = "MOVIE", prevKey = null, nextKey = 2)
        mediaDao.insertAll(listOf(initialEntity))
        remoteKeyDao.insertKeys(listOf(initialKey))

        // API returns last page (page 2 of 2)
        val lastPageResponse = PaginatedResponseDto(
            page = 2,
            results = listOf(
                MovieItemDto(id = 2, title = "Movie 2", posterPath = null, releaseDate = null, voteAverage = 7.5)
            ),
            totalPages = 2,
            totalResults = 2
        )
        coEvery { apiService.getMovies(page = 2) } returns lastPageResponse

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(initialEntity),
                    prevKey = null,
                    nextKey = 2
                )
            ),
            anchorPosition = 0,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.APPEND, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertTrue("Reaching totalPages should mark endOfPaginationReached as true", success.endOfPaginationReached)

        // Verify key in Room for page 2 entity has null nextKey
        val key2 = remoteKeyDao.getRemoteKey(2, "MOVIE")
        assertNotNull(key2)
        assertNull(key2?.nextKey)
        assertEquals(1, key2?.prevKey)
    }

    @Test
    fun loadAppendWithNetworkFailureReturnsErrorAndPreservesDatabaseState() = runBlocking {
        // Pre-populate database with page 1
        val initialEntity = MediaItemEntity(
            id = 10,
            mediaType = "MOVIE",
            title = "Movie 10",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = 8.0,
            voteCount = 50,
            overview = "Overview",
            pageIndex = 1
        )
        val initialKey = RemoteKeyEntity(id = 10, mediaType = "MOVIE", prevKey = null, nextKey = 2)
        mediaDao.insertAll(listOf(initialEntity))
        remoteKeyDao.insertKeys(listOf(initialKey))

        val networkError = IOException("Connection reset by peer")
        coEvery { apiService.getMovies(page = 2) } throws networkError

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(initialEntity),
                    prevKey = null,
                    nextKey = 2
                )
            ),
            anchorPosition = 0,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.APPEND, state)

        assertTrue("Expected MediatorResult.Error", result is RemoteMediator.MediatorResult.Error)
        val error = result as RemoteMediator.MediatorResult.Error
        assertEquals(networkError, error.throwable)

        // Existing data in Room must remain intact
        assertEquals(1, mediaDao.countByMediaType("MOVIE"))
        assertNotNull(remoteKeyDao.getRemoteKey(10, "MOVIE"))
    }

    @Test
    fun loadRefreshWithFreshCacheSkipsNetworkAndReturnsSuccess() = runBlocking {
        // Pre-populate fresh cache (within 4 hours)
        val entity = MediaItemEntity(
            id = 5,
            mediaType = "MOVIE",
            title = "Fresh Movie",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = 8.0,
            voteCount = 10,
            overview = "Overview",
            pageIndex = 1,
            cachedAt = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(15)
        )
        val freshKey = RemoteKeyEntity(
            id = 5,
            mediaType = "MOVIE",
            prevKey = null,
            nextKey = 2,
            lastUpdated = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(15)
        )
        mediaDao.insertAll(listOf(entity))
        remoteKeyDao.insertKeys(listOf(freshKey))

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(entity),
                    prevKey = null,
                    nextKey = 2
                )
            ),
            anchorPosition = 0,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertFalse(success.endOfPaginationReached)

        // Verify zero network invocations
        coVerify(exactly = 0) { apiService.getMovies(any(), any(), any()) }
    }

    @Test
    fun loadPrependReturnsSuccessWithEndOfPaginationReachedTrue() = runBlocking {
        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState<Int, MediaItemEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.PREPEND, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertTrue("Prepend should always mark end of pagination as true", success.endOfPaginationReached)
        coVerify(exactly = 0) { apiService.getMovies(any(), any(), any()) }
    }
}
