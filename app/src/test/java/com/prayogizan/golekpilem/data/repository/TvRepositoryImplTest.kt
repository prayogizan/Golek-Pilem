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

class TvRepositoryImplTest {

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
        var getTvSeriesDetailHandler: (seriesId: Int) -> TvSeriesDetailDto = { id ->
            TvSeriesDetailDto(
                id = id,
                name = "Breaking Bad",
                tagline = "Change the equation",
                overview = "A chemistry teacher diagnosed with terminal lung cancer...",
                posterPath = "/breaking_bad.jpg",
                backdropPath = "/bb_backdrop.jpg",
                firstAirDate = "2008-01-20",
                lastAirDate = "2013-09-29",
                numberOfSeasons = 5,
                numberOfEpisodes = 62,
                voteAverage = 8.892,
                voteCount = 14000,
                status = "Ended"
            )
        }
    ) : TmdbApiService {
        override suspend fun getMovies(page: Int, sortBy: String, language: String): PaginatedResponseDto<MovieItemDto> {
            throw UnsupportedOperationException()
        }

        override suspend fun getTvSeries(page: Int, sortBy: String, language: String): PaginatedResponseDto<TvSeriesItemDto> {
            return PaginatedResponseDto(page = page, results = emptyList(), totalPages = 1, totalResults = 0)
        }

        override suspend fun getMovieDetail(movieId: Int, appendToResponse: String): MovieDetailDto {
            throw UnsupportedOperationException()
        }

        override suspend fun getTvSeriesDetail(seriesId: Int, appendToResponse: String): TvSeriesDetailDto {
            return getTvSeriesDetailHandler(seriesId)
        }
    }

    @Test
    fun `getTvSeries returns active PagingData Flow`() {
        val fakeApi = FakeApiService()
        val repository = TvRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val flow = repository.getTvSeries()
        assertNotNull("PagingData flow should not be null", flow)
    }

    @Test
    fun `getTvSeriesDetail emits Loading followed by Success on valid response when cache is empty`() = runBlocking {
        val fakeApi = FakeApiService()
        val repository = TvRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getTvSeriesDetail(seriesId = 1396).take(2).toList()

        assertEquals(2, emissions.size)
        assertTrue("First emission should be Resource.Loading", emissions[0] is Resource.Loading)
        assertTrue("Second emission should be Resource.Success", emissions[1] is Resource.Success)

        val success = emissions[1] as Resource.Success
        assertEquals(1396, success.data.id)
        assertEquals("Breaking Bad", success.data.name)
        assertEquals(8.9, success.data.voteAverage, 0.01)
        assertEquals(5, success.data.numberOfSeasons)
        assertEquals(62, success.data.numberOfEpisodes)
        assertFalse(success.data.isOfflineCache)
        coVerify(exactly = 1) { mockMediaDetailDao.insertDetail(any()) }
    }

    @Test
    fun `getTvSeriesDetail emits cached detail immediately with isOfflineCache true and terminates when cache is fresh`() = runBlocking {
        val freshCachedEntity = MediaDetailEntity(
            id = 1396,
            mediaType = "TV",
            title = "Breaking Bad (Cached)",
            overview = "Cached TV synopsis",
            posterPath = "/cached_bb.jpg",
            backdropPath = "/cached_bb_back.jpg",
            voteAverage = 8.9,
            voteCount = 14000,
            releaseDate = "2008-01-20",
            runtimeOrSeasons = "5 Seasons",
            genresJson = "[\"Drama\"]",
            status = "Ended",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = System.currentTimeMillis(),
            ttlMillis = 86_400_000L
        )
        coEvery { mockMediaDetailDao.getDetail(1396, "TV") } returns freshCachedEntity

        var apiCalled = false
        val fakeApi = FakeApiService(
            getTvSeriesDetailHandler = {
                apiCalled = true
                throw AssertionError("Remote API should not be called when cache is fresh")
            }
        )
        val repository = TvRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getTvSeriesDetail(seriesId = 1396).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val success = emissions[1] as Resource.Success
        assertEquals("Breaking Bad (Cached)", success.data.name)
        assertTrue("Should be marked as offline cache", success.data.isOfflineCache)
        assertFalse("API should not be called when fresh cache exists", apiCalled)
    }

    @Test
    fun `getTvSeriesDetail emits cached detail first, then fetches API, inserts into DB, and emits fresh detail when cache is stale`() = runBlocking {
        val staleCachedEntity = MediaDetailEntity(
            id = 1396,
            mediaType = "TV",
            title = "Breaking Bad (Stale)",
            overview = "Stale synopsis",
            posterPath = "/stale_bb.jpg",
            backdropPath = "/stale_bb_back.jpg",
            voteAverage = 8.8,
            voteCount = 13500,
            releaseDate = "2008-01-20",
            runtimeOrSeasons = "5 Seasons",
            genresJson = "[]",
            status = "Ended",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = System.currentTimeMillis() - 100_000_000L,
            ttlMillis = 86_400_000L
        )
        coEvery { mockMediaDetailDao.getDetail(1396, "TV") } returns staleCachedEntity

        val fakeApi = FakeApiService(
            getTvSeriesDetailHandler = { id ->
                TvSeriesDetailDto(
                    id = id,
                    name = "Breaking Bad (Fresh)",
                    overview = "Fresh synopsis",
                    firstAirDate = "2008-01-20",
                    numberOfSeasons = 5,
                    numberOfEpisodes = 62,
                    voteAverage = 8.9,
                    voteCount = 14500
                )
            }
        )
        val repository = TvRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getTvSeriesDetail(seriesId = 1396).toList()

        assertEquals(3, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)

        val cachedSuccess = emissions[1] as Resource.Success
        assertEquals("Breaking Bad (Stale)", cachedSuccess.data.name)
        assertTrue(cachedSuccess.data.isOfflineCache)

        val freshSuccess = emissions[2] as Resource.Success
        assertEquals("Breaking Bad (Fresh)", freshSuccess.data.name)
        assertFalse(freshSuccess.data.isOfflineCache)

        coVerify(exactly = 1) { mockMediaDetailDao.insertDetail(any()) }
    }

    @Test
    fun `getTvSeriesDetail retains cached detail and suppresses Error when network fails but cache exists`() = runBlocking {
        val staleCachedEntity = MediaDetailEntity(
            id = 1396,
            mediaType = "TV",
            title = "Breaking Bad (Cached)",
            overview = "Cached synopsis",
            posterPath = "/poster.jpg",
            backdropPath = "/back.jpg",
            voteAverage = 8.8,
            voteCount = 13500,
            releaseDate = "2008-01-20",
            runtimeOrSeasons = "5 Seasons",
            genresJson = "[]",
            status = "Ended",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = System.currentTimeMillis() - 100_000_000L,
            ttlMillis = 86_400_000L
        )
        coEvery { mockMediaDetailDao.getDetail(1396, "TV") } returns staleCachedEntity

        val fakeApi = FakeApiService(
            getTvSeriesDetailHandler = { throw IOException("Socket timeout") }
        )
        val repository = TvRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getTvSeriesDetail(seriesId = 1396).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue("Should emit cached success without emitting Resource.Error", emissions[1] is Resource.Success)

        val success = emissions[1] as Resource.Success
        assertEquals("Breaking Bad (Cached)", success.data.name)
        assertTrue(success.data.isOfflineCache)
    }

    @Test
    fun `getTvSeriesDetail emits Loading followed by Error on network failure when no cache exists`() = runBlocking {
        coEvery { mockMediaDetailDao.getDetail(999, "TV") } returns null
        val fakeApi = FakeApiService(
            getTvSeriesDetailHandler = { throw IOException("Socket timeout") }
        )
        val repository = TvRepositoryImpl(
            apiService = fakeApi,
            database = mockDatabase,
            ioDispatcher = Dispatchers.Unconfined
        )

        val emissions = repository.getTvSeriesDetail(seriesId = 999).take(2).toList()

        assertEquals(2, emissions.size)
        assertTrue("First emission should be Resource.Loading", emissions[0] is Resource.Loading)
        assertTrue("Second emission should be Resource.Error", emissions[1] is Resource.Error)

        val error = emissions[1] as Resource.Error
        assertEquals("Socket timeout", error.message)
        assertTrue(error.cause is IOException)
    }

    @Test(expected = CancellationException::class)
    fun `getTvSeriesDetail rethrows CancellationException without swallowing`() {
        runBlocking {
            val fakeApi = FakeApiService(
                getTvSeriesDetailHandler = { throw CancellationException("Scope cancelled") }
            )
            val repository = TvRepositoryImpl(
                apiService = fakeApi,
                database = mockDatabase,
                ioDispatcher = Dispatchers.Unconfined
            )

            repository.getTvSeriesDetail(seriesId = 123).toList()
        }
    }
}

