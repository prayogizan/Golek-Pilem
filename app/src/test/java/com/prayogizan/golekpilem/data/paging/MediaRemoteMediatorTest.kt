package com.prayogizan.golekpilem.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity
import com.prayogizan.golekpilem.data.local.entity.RemoteKeyEntity
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.PaginatedResponseDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class MediaRemoteMediatorTest {

    private val apiService: TmdbApiService = mockk()
    private val mediaDao: MediaDao = mockk(relaxed = true)
    private val remoteKeyDao: RemoteKeyDao = mockk(relaxed = true)
    private val database: GolekPilemDatabase = mockk(relaxed = true)

    private val pagingConfig = PagingConfig(pageSize = 20)

    @Before
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { database.mediaDao() } returns mediaDao
        coEvery { database.remoteKeyDao() } returns remoteKeyDao
        coEvery { database.withTransaction<Any?>(any()) } coAnswers {
            val lambda = args.filterIsInstance<Function1<*, *>>().firstOrNull()
                ?: error("No Function1 argument found in withTransaction args: $args")
            @Suppress("UNCHECKED_CAST")
            (lambda as (suspend () -> Any?)).invoke()
        }
    }


    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `load REFRESH with fresh cache skips network and returns Success`() = runBlocking {
        val cachedEntity = createMediaEntity(id = 1, mediaType = "MOVIE")
        val freshRemoteKey = RemoteKeyEntity(
            id = 1,
            mediaType = "MOVIE",
            prevKey = null,
            nextKey = 2,
            lastUpdated = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)
        )

        coEvery { remoteKeyDao.getRemoteKey(1, "MOVIE") } returns freshRemoteKey

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState(
            pages = listOf(
                androidx.paging.PagingSource.LoadResult.Page(
                    data = listOf(cachedEntity),
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
        assertFalse("endOfPaginationReached should be false for fresh cache", success.endOfPaginationReached)

        coVerify(exactly = 0) { apiService.getMovies(any(), any(), any()) }
    }

    @Test
    fun `load REFRESH with stale cache queries API, resets cache atomically, and returns Success`() = runBlocking {
        val staleRemoteKey = RemoteKeyEntity(
            id = 1,
            mediaType = "MOVIE",
            prevKey = null,
            nextKey = 2,
            lastUpdated = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5)
        )
        coEvery { remoteKeyDao.getRemoteKey(1, "MOVIE") } returns staleRemoteKey

        val response = PaginatedResponseDto(
            page = 1,
            results = listOf(
                MovieItemDto(id = 10, title = "Movie 10", posterPath = "/p10.jpg", releaseDate = "2024-01-01", voteAverage = 7.5)
            ),
            totalPages = 10,
            totalResults = 100
        )
        coEvery { apiService.getMovies(page = 1) } returns response

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState<Int, MediaItemEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertFalse(success.endOfPaginationReached)

        coVerify(exactly = 1) { remoteKeyDao.clearKeys("MOVIE") }
        coVerify(exactly = 1) { mediaDao.clearByMediaType("MOVIE") }
        coVerify(exactly = 1) { remoteKeyDao.insertKeys(any()) }
        coVerify(exactly = 1) { mediaDao.insertAll(any()) }
    }

    @Test
    fun `load REFRESH for TV series queries getTvSeries endpoint and persists records`() = runBlocking {
        val response = PaginatedResponseDto(
            page = 1,
            results = listOf(
                TvSeriesItemDto(id = 20, name = "Series 20", posterPath = "/s20.jpg", firstAirDate = "2024-02-01", voteAverage = 8.1)
            ),
            totalPages = 5,
            totalResults = 50
        )
        coEvery { apiService.getTvSeries(page = 1) } returns response

        val mediator = MediaRemoteMediator(apiService, database, "TV")
        val state = PagingState<Int, MediaItemEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertFalse(success.endOfPaginationReached)

        coVerify(exactly = 1) { apiService.getTvSeries(page = 1) }
        coVerify(exactly = 1) { remoteKeyDao.clearKeys("TV") }
        coVerify(exactly = 1) { mediaDao.clearByMediaType("TV") }
    }

    @Test
    fun `load REFRESH when API returns empty results marks endOfPaginationReached true`() = runBlocking {
        val response = PaginatedResponseDto(
            page = 1,
            results = emptyList<MovieItemDto>(),
            totalPages = 1,
            totalResults = 0
        )
        coEvery { apiService.getMovies(page = 1) } returns response

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState<Int, MediaItemEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertTrue("Empty results must mark endOfPaginationReached as true", success.endOfPaginationReached)
    }

    @Test
    fun `load PREPEND returns Success with endOfPaginationReached true`() = runBlocking {
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
        assertTrue(success.endOfPaginationReached)
        coVerify(exactly = 0) { apiService.getMovies(any(), any(), any()) }
    }

    @Test
    fun `load APPEND with valid nextKey queries subsequent page and returns Success`() = runBlocking {
        val lastEntity = createMediaEntity(id = 5, mediaType = "MOVIE")
        val remoteKey = RemoteKeyEntity(id = 5, mediaType = "MOVIE", prevKey = 1, nextKey = 2)

        coEvery { remoteKeyDao.getRemoteKey(5, "MOVIE") } returns remoteKey

        val response = PaginatedResponseDto(
            page = 2,
            results = listOf(
                MovieItemDto(id = 6, title = "Movie 6", posterPath = "/p6.jpg", releaseDate = "2024-03-01", voteAverage = 7.0)
            ),
            totalPages = 10,
            totalResults = 100
        )
        coEvery { apiService.getMovies(page = 2) } returns response

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState(
            pages = listOf(
                androidx.paging.PagingSource.LoadResult.Page(
                    data = listOf(lastEntity),
                    prevKey = null,
                    nextKey = 2
                )
            ),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.APPEND, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertFalse(success.endOfPaginationReached)

        coVerify(exactly = 1) { apiService.getMovies(page = 2) }
        coVerify(exactly = 1) { mediaDao.insertAll(any()) }
        coVerify(exactly = 1) { remoteKeyDao.insertKeys(any()) }
    }

    @Test
    fun `load APPEND when nextKey is null returns Success with endOfPaginationReached true`() = runBlocking {
        val lastEntity = createMediaEntity(id = 20, mediaType = "MOVIE")
        val remoteKey = RemoteKeyEntity(id = 20, mediaType = "MOVIE", prevKey = 9, nextKey = null)

        coEvery { remoteKeyDao.getRemoteKey(20, "MOVIE") } returns remoteKey

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState(
            pages = listOf(
                androidx.paging.PagingSource.LoadResult.Page(
                    data = listOf(lastEntity),
                    prevKey = 9,
                    nextKey = null
                )
            ),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.APPEND, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        val success = result as RemoteMediator.MediatorResult.Success
        assertTrue("When nextKey is null, endOfPaginationReached should be true", success.endOfPaginationReached)

        coVerify(exactly = 0) { apiService.getMovies(any(), any(), any()) }
    }

    @Test
    fun `load returns Error when network throws IOException`() = runBlocking {
        val ioException = IOException("Network connection dropped")
        coEvery { apiService.getMovies(page = 1) } throws ioException

        val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
        val state = PagingState<Int, MediaItemEntity>(
            pages = emptyList(),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        val error = result as RemoteMediator.MediatorResult.Error
        assertEquals(ioException, error.throwable)
    }

    @Test(expected = CancellationException::class)
    fun `load rethrows CancellationException without catching`() {
        runBlocking {
            coEvery { apiService.getMovies(page = 1) } throws CancellationException("Coroutine cancelled")

            val mediator = MediaRemoteMediator(apiService, database, "MOVIE")
            val state = PagingState<Int, MediaItemEntity>(
                pages = emptyList(),
                anchorPosition = null,
                config = pagingConfig,
                leadingPlaceholderCount = 0
            )

            mediator.load(LoadType.REFRESH, state)
        }
    }

    private fun createMediaEntity(id: Int, mediaType: String): MediaItemEntity {
        return MediaItemEntity(
            id = id,
            mediaType = mediaType,
            title = "Test Media $id",
            posterPath = "/path$id.jpg",
            backdropPath = null,
            releaseDate = "2024-01-01",
            voteAverage = 8.0,
            voteCount = 100,
            overview = "Test overview",
            pageIndex = 1,
            cachedAt = System.currentTimeMillis()
        )
    }
}
