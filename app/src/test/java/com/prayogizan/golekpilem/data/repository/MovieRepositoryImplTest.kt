package com.prayogizan.golekpilem.data.repository

import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.PaginatedResponseDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.MediaDetailDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.data.local.entity.MediaDetailEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MovieRepositoryImplTest {

    private val mockMediaDao: MediaDao = mockk(relaxed = true)
    private val mockRemoteKeyDao: RemoteKeyDao = mockk(relaxed = true)
    private val mockMediaDetailDao: MediaDetailDao = mockk(relaxed = true)
    private val mockDatabase: GolekPilemDatabase = mockk(relaxed = true) {
        every { mediaDao() } returns mockMediaDao
        every { remoteKeyDao() } returns mockRemoteKeyDao
        every { mediaDetailDao() } returns mockMediaDetailDao
    }

    @Before
    fun setUp() {
        coEvery { mockMediaDetailDao.getDetail(any(), any()) } returns null
        coEvery { mockMediaDetailDao.insertDetail(any()) } returns Unit
    }

    private class FakeApiService(
        var getMovieDetailHandler: (movieId: Int) -> MovieDetailDto = { id ->
            MovieDetailDto(
                id = id,
                title = "Inception",
                tagline = "Your mind is the scene of the crime",
                overview = "A thief who steals corporate secrets...",
                posterPath = "/inception.jpg",
                backdropPath = "/backdrop.jpg",
                releaseDate = "2010-07-16",
                runtime = 148,
                voteAverage = 8.357,
                voteCount = 35000,
                status = "Released"
            )
        }
    ) : TmdbApiService {
        override suspend fun getMovies(page: Int, sortBy: String, language: String): PaginatedResponseDto<MovieItemDto> {
            return PaginatedResponseDto(page = page, results = emptyList(), totalPages = 1, totalResults = 0)
        }

        override suspend fun getTvSeries(page: Int, sortBy: String, language: String): PaginatedResponseDto<TvSeriesItemDto> {
            throw UnsupportedOperationException()
        }

        override suspend fun getMovieDetail(movieId: Int, appendToResponse: String): MovieDetailDto {
            return getMovieDetailHandler(movieId)
        }

        override suspend fun getTvSeriesDetail(seriesId: Int, appendToResponse: String): TvSeriesDetailDto {
            throw UnsupportedOperationException()
        }
    }

    @Test
    fun `getMovies returns active PagingData Flow`() {
        val fakeApi = FakeApiService()
        val repository = MovieRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val flow = repository.getMovies()
        assertNotNull("PagingData flow should not be null", flow)
    }

    @Test
    fun `getMovieDetail emits Loading followed by Success on valid response when cache is empty`() = runBlocking {
        val fakeApi = FakeApiService()
        val repository = MovieRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getMovieDetail(movieId = 27205).take(2).toList()

        assertEquals(2, emissions.size)
        assertTrue("First emission should be Resource.Loading", emissions[0] is Resource.Loading)
        assertTrue("Second emission should be Resource.Success", emissions[1] is Resource.Success)

        val success = emissions[1] as Resource.Success
        assertEquals(27205, success.data.id)
        assertEquals("Inception", success.data.title)
        assertEquals(8.4, success.data.voteAverage, 0.01)
        assertEquals("2010-07-16", success.data.releaseDate)
        assertFalse(success.data.isOfflineCache)
        coVerify(exactly = 1) { mockMediaDetailDao.insertDetail(any()) }
    }

    @Test
    fun `getMovieDetail emits cached detail immediately with isOfflineCache true and terminates when cache is fresh`() = runBlocking {
        val freshCachedEntity = MediaDetailEntity(
            id = 27205,
            mediaType = "MOVIE",
            title = "Inception (Cached)",
            overview = "Cached synopsis",
            posterPath = "/cached_poster.jpg",
            backdropPath = "/cached_back.jpg",
            voteAverage = 8.4,
            voteCount = 35000,
            releaseDate = "2010-07-16",
            runtimeOrSeasons = "148 min",
            genresJson = "[\"Sci-Fi\"]",
            status = "Released",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = System.currentTimeMillis(), // fresh (within 24h)
            ttlMillis = 86_400_000L
        )
        coEvery { mockMediaDetailDao.getDetail(27205, "MOVIE") } returns freshCachedEntity

        var apiCalled = false
        val fakeApi = FakeApiService(
            getMovieDetailHandler = {
                apiCalled = true
                throw AssertionError("Remote API should not be called when cache is fresh")
            }
        )
        val repository = MovieRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getMovieDetail(movieId = 27205).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val success = emissions[1] as Resource.Success
        assertEquals("Inception (Cached)", success.data.title)
        assertTrue("Should be marked as offline cache", success.data.isOfflineCache)
        assertFalse("API should not be invoked for fresh cache", apiCalled)
    }

    @Test
    fun `getMovieDetail emits cached detail first, then fetches API, inserts into DB, and emits fresh detail when cache is stale`() = runBlocking {
        val staleCachedEntity = MediaDetailEntity(
            id = 27205,
            mediaType = "MOVIE",
            title = "Inception (Stale)",
            overview = "Stale synopsis",
            posterPath = "/stale_poster.jpg",
            backdropPath = "/stale_back.jpg",
            voteAverage = 8.2,
            voteCount = 34000,
            releaseDate = "2010-07-16",
            runtimeOrSeasons = "148 min",
            genresJson = "[]",
            status = "Released",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = System.currentTimeMillis() - 100_000_000L, // expired (> 24h)
            ttlMillis = 86_400_000L
        )
        coEvery { mockMediaDetailDao.getDetail(27205, "MOVIE") } returns staleCachedEntity

        val fakeApi = FakeApiService(
            getMovieDetailHandler = { id ->
                MovieDetailDto(
                    id = id,
                    title = "Inception (Fresh)",
                    overview = "Fresh synopsis",
                    voteAverage = 8.4,
                    voteCount = 36000
                )
            }
        )
        val repository = MovieRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getMovieDetail(movieId = 27205).toList()

        assertEquals(3, emissions.size)
        assertTrue("First emission should be Loading", emissions[0] is Resource.Loading)

        val cachedSuccess = emissions[1] as Resource.Success
        assertEquals("Inception (Stale)", cachedSuccess.data.title)
        assertTrue("First content emission should be marked as offline cache", cachedSuccess.data.isOfflineCache)

        val freshSuccess = emissions[2] as Resource.Success
        assertEquals("Inception (Fresh)", freshSuccess.data.title)
        assertFalse("Second content emission should be fresh network data", freshSuccess.data.isOfflineCache)

        coVerify(exactly = 1) { mockMediaDetailDao.insertDetail(any()) }
    }

    @Test
    fun `getMovieDetail retains cached detail and suppresses Error when network fails but cache exists`() = runBlocking {
        val staleCachedEntity = MediaDetailEntity(
            id = 27205,
            mediaType = "MOVIE",
            title = "Inception (Cached)",
            overview = "Cached synopsis",
            posterPath = "/poster.jpg",
            backdropPath = "/back.jpg",
            voteAverage = 8.3,
            voteCount = 35000,
            releaseDate = "2010-07-16",
            runtimeOrSeasons = "148 min",
            genresJson = "[]",
            status = "Released",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = System.currentTimeMillis() - 100_000_000L,
            ttlMillis = 86_400_000L
        )
        coEvery { mockMediaDetailDao.getDetail(27205, "MOVIE") } returns staleCachedEntity

        val fakeApi = FakeApiService(
            getMovieDetailHandler = { throw IOException("TMDB service unavailable") }
        )
        val repository = MovieRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getMovieDetail(movieId = 27205).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue("Should emit cached success without emitting Resource.Error", emissions[1] is Resource.Success)

        val success = emissions[1] as Resource.Success
        assertEquals("Inception (Cached)", success.data.title)
        assertTrue(success.data.isOfflineCache)
    }

    @Test
    fun `getMovieDetail emits Loading followed by Error on network failure when no cache exists`() = runBlocking {
        coEvery { mockMediaDetailDao.getDetail(999, "MOVIE") } returns null
        val fakeApi = FakeApiService(
            getMovieDetailHandler = { throw IOException("Connection reset") }
        )
        val repository = MovieRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getMovieDetail(movieId = 999).take(2).toList()

        assertEquals(2, emissions.size)
        assertTrue("First emission should be Resource.Loading", emissions[0] is Resource.Loading)
        assertTrue("Second emission should be Resource.Error", emissions[1] is Resource.Error)

        val error = emissions[1] as Resource.Error
        assertEquals("Connection reset", error.message)
        assertTrue(error.cause is IOException)
    }

    @Test(expected = CancellationException::class)
    fun `getMovieDetail rethrows CancellationException without swallowing`() {
        runBlocking {
            val fakeApi = FakeApiService(
                getMovieDetailHandler = { throw CancellationException("Scope cancelled") }
            )
            val repository = MovieRepositoryImpl(
                apiService = fakeApi,
                database = mockDatabase,
                ioDispatcher = Dispatchers.Unconfined
            )

            repository.getMovieDetail(movieId = 123).toList()
        }
    }
}
