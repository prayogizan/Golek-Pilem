package com.prayogizan.golekpilem.data.repository

import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.PaginatedResponseDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MovieRepositoryImplTest {

    private val mockMediaDao: MediaDao = mockk(relaxed = true)
    private val mockRemoteKeyDao: RemoteKeyDao = mockk(relaxed = true)
    private val mockDatabase: GolekPilemDatabase = mockk(relaxed = true) {
        every { mediaDao() } returns mockMediaDao
        every { remoteKeyDao() } returns mockRemoteKeyDao
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
    fun `getMovieDetail emits Loading followed by Success on valid response`() = runBlocking {
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
    }

    @Test
    fun `getMovieDetail emits Loading followed by Error on network failure`() = runBlocking {
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
