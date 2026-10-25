package com.prayogizan.golekpilem.data.repository

import app.cash.turbine.test
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.data.local.BaseRoomTest
import com.prayogizan.golekpilem.data.local.entity.MediaDetailEntity
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Integration test suite testing the Stale-While-Revalidate (SWR) offline detail caching engine.
 *
 * Couples [MovieRepositoryImpl] and [TvRepositoryImpl] with an authentic in-memory Room database
 * and validates that cached details are emitted immediately with `isOfflineCache = true` when
 * network operations fail.
 */
class OfflineDetailRepositoryTest : BaseRoomTest() {

    private val apiService: TmdbApiService = mockk()
    private lateinit var movieRepository: MovieRepositoryImpl
    private lateinit var tvRepository: TvRepositoryImpl

    @Before
    override fun setUp() {
        super.setUp()
        movieRepository = MovieRepositoryImpl(
            apiService = apiService,
            database = database,
            ioDispatcher = Dispatchers.Unconfined
        )
        tvRepository = TvRepositoryImpl(
            apiService = apiService,
            database = database,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @Test
    fun getMovieDetailEmitsCachedDetailImmediatelyWithIsOfflineCacheTrueWhenNetworkFails() = runBlocking {
        // 1. Seed in-memory database with cached movie detail (stale so revalidation is attempted)
        val cachedMovie = createDetailEntity(
            id = 100,
            mediaType = "MOVIE",
            title = "Cached Matrix",
            cachedAt = 1000L,
            ttlMillis = 1000L // Already expired
        )
        mediaDetailDao.insertDetail(cachedMovie)

        // 2. Simulate complete network blackout
        coEvery {
            apiService.getMovieDetail(movieId = 100, appendToResponse = any())
        } throws IOException("Network unreachable")

        // 3. Collect flow via Turbine
        movieRepository.getMovieDetail(movieId = 100, forceRefresh = false).test {
            // Emission 1: Loading
            val loadingItem = awaitItem()
            assertTrue("Expected Resource.Loading", loadingItem is Resource.Loading)

            // Emission 2: Cached content marked with isOfflineCache = true
            val successItem = awaitItem()
            assertTrue("Expected Resource.Success with cached payload", successItem is Resource.Success)
            val movie = (successItem as Resource.Success).data
            assertEquals(100, movie.id)
            assertEquals("Cached Matrix", movie.title)
            assertTrue("Movie detail must have isOfflineCache = true", movie.isOfflineCache)

            // Flow completes without emitting Resource.Error because valid cache was served
            awaitComplete()
        }
    }

    @Test
    fun getMovieDetailWithForceRefreshTrueAndNetworkFailureEmitsCachedDetailFollowedByError() = runBlocking {
        // Seed database
        val cachedMovie = createDetailEntity(
            id = 200,
            mediaType = "MOVIE",
            title = "Cached Gladiator",
            cachedAt = System.currentTimeMillis(),
            ttlMillis = 86_400_000L
        )
        mediaDetailDao.insertDetail(cachedMovie)

        coEvery {
            apiService.getMovieDetail(movieId = 200, appendToResponse = any())
        } throws IOException("Connection timed out")

        movieRepository.getMovieDetail(movieId = 200, forceRefresh = true).test {
            assertTrue(awaitItem() is Resource.Loading)

            val cachedSuccess = awaitItem()
            assertTrue(cachedSuccess is Resource.Success)
            assertTrue((cachedSuccess as Resource.Success).data.isOfflineCache)

            // Because forceRefresh was true, error is dispatched to dismiss spinners/notify user
            val errorItem = awaitItem()
            assertTrue("Expected Resource.Error following failed forced refresh", errorItem is Resource.Error)

            awaitComplete()
        }
    }

    @Test
    fun getMovieDetailOnCacheMissWithNetworkFailureEmitsLoadingFollowedByError() = runBlocking {
        // Empty database (cache miss)
        coEvery {
            apiService.getMovieDetail(movieId = 999, appendToResponse = any())
        } throws IOException("Host unreachable")

        movieRepository.getMovieDetail(movieId = 999, forceRefresh = false).test {
            assertTrue(awaitItem() is Resource.Loading)

            val errorItem = awaitItem()
            assertTrue("Expected Resource.Error on cache miss with network failure", errorItem is Resource.Error)

            awaitComplete()
        }
    }

    @Test
    fun getMovieDetailOnFreshCacheTerminatesEarlyWithoutNetworkInvocation() = runBlocking {
        // Seed fresh cache (not expired)
        val freshMovie = createDetailEntity(
            id = 300,
            mediaType = "MOVIE",
            title = "Fresh Inception",
            cachedAt = System.currentTimeMillis(),
            ttlMillis = 86_400_000L
        )
        mediaDetailDao.insertDetail(freshMovie)

        movieRepository.getMovieDetail(movieId = 300, forceRefresh = false).test {
            assertTrue(awaitItem() is Resource.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)
            assertEquals("Fresh Inception", (successItem as Resource.Success).data.title)
            assertTrue(successItem.data.isOfflineCache)

            awaitComplete()
        }

        // Verify API was never contacted
        coVerify(exactly = 0) { apiService.getMovieDetail(any(), any()) }
    }

    @Test
    fun getMovieDetailWithStaleCacheUpdatesRoomDatabaseOnNetworkSuccess() = runBlocking {
        // Seed stale cache
        val staleMovie = createDetailEntity(
            id = 400,
            mediaType = "MOVIE",
            title = "Stale Title",
            cachedAt = 1000L,
            ttlMillis = 1000L
        )
        mediaDetailDao.insertDetail(staleMovie)

        val freshDto = MovieDetailDto(
            id = 400,
            title = "Fresh Title",
            tagline = null,
            overview = "Updated overview",
            posterPath = "/poster400.jpg",
            backdropPath = null,
            releaseDate = "2024-05-01",
            runtime = 135,
            voteAverage = 8.8,
            voteCount = 5000,
            status = "Released"
        )
        coEvery { apiService.getMovieDetail(movieId = 400, appendToResponse = any()) } returns freshDto

        movieRepository.getMovieDetail(movieId = 400, forceRefresh = false).test {
            assertTrue(awaitItem() is Resource.Loading)

            val cachedEmission = awaitItem() as Resource.Success
            assertEquals("Stale Title", cachedEmission.data.title)
            assertTrue(cachedEmission.data.isOfflineCache)

            val freshEmission = awaitItem() as Resource.Success
            assertEquals("Fresh Title", freshEmission.data.title)
            assertFalse(freshEmission.data.isOfflineCache)

            awaitComplete()
        }

        // Verify Room database was updated with fresh data
        val updatedInRoom = mediaDetailDao.getDetail(400, "MOVIE")
        assertNotNull(updatedInRoom)
        assertEquals("Fresh Title", updatedInRoom?.title)
    }

    @Test
    fun getTvSeriesDetailEmitsCachedDetailImmediatelyWithIsOfflineCacheTrueWhenNetworkFails() = runBlocking {
        // Seed stale TV series detail in Room
        val cachedTv = createDetailEntity(
            id = 500,
            mediaType = "TV",
            title = "Cached Better Call Saul",
            cachedAt = 1000L,
            ttlMillis = 1000L
        )
        mediaDetailDao.insertDetail(cachedTv)

        coEvery {
            apiService.getTvSeriesDetail(seriesId = 500, appendToResponse = any())
        } throws IOException("Offline connection lost")

        tvRepository.getTvSeriesDetail(seriesId = 500, forceRefresh = false).test {
            assertTrue(awaitItem() is Resource.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)
            val series = (successItem as Resource.Success).data
            assertEquals(500, series.id)
            assertEquals("Cached Better Call Saul", series.name)
            assertTrue("TV series detail must have isOfflineCache = true", series.isOfflineCache)

            awaitComplete()
        }
    }

    private fun createDetailEntity(
        id: Int,
        mediaType: String,
        title: String,
        cachedAt: Long,
        ttlMillis: Long
    ): MediaDetailEntity {
        return MediaDetailEntity(
            id = id,
            mediaType = mediaType,
            title = title,
            overview = "Overview for $title",
            posterPath = "/poster$id.jpg",
            backdropPath = "/backdrop$id.jpg",
            voteAverage = 8.5,
            voteCount = 1000,
            releaseDate = "2024-01-01",
            runtimeOrSeasons = "120 min",
            genresJson = "[]",
            status = "Released",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = cachedAt,
            ttlMillis = ttlMillis
        )
    }
}
