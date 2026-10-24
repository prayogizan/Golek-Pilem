package com.prayogizan.golekpilem.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.PaginatedResponseDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import com.prayogizan.golekpilem.domain.model.Movie
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MoviePagingSourceTest {

    private class FakeMovieApiService(
        var getMoviesHandler: (page: Int) -> PaginatedResponseDto<MovieItemDto> = {
            PaginatedResponseDto(
                page = it,
                results = listOf(
                    MovieItemDto(id = 1, title = "Movie 1", posterPath = "/p1.jpg", releaseDate = "2026-01-01", voteAverage = 8.0)
                ),
                totalPages = 10,
                totalResults = 100
            )
        }
    ) : TmdbApiService {
        override suspend fun getMovies(page: Int, sortBy: String, language: String): PaginatedResponseDto<MovieItemDto> {
            return getMoviesHandler(page)
        }

        override suspend fun getTvSeries(page: Int, sortBy: String, language: String): PaginatedResponseDto<TvSeriesItemDto> {
            throw UnsupportedOperationException()
        }

        override suspend fun getMovieDetail(movieId: Int, appendToResponse: String): MovieDetailDto {
            throw UnsupportedOperationException()
        }

        override suspend fun getTvSeriesDetail(seriesId: Int, appendToResponse: String): TvSeriesDetailDto {
            throw UnsupportedOperationException()
        }
    }

    @Test
    fun `load initial page returns Page with correct data, prevKey null, and nextKey 2`() = runBlocking {
        val fakeApi = FakeMovieApiService()
        val pagingSource = MoviePagingSource(fakeApi)

        val params = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        assertTrue("Result should be LoadResult.Page", result is PagingSource.LoadResult.Page)

        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals(1, page.data.first().id)
        assertEquals("Movie 1", page.data.first().title)
        assertNull("prevKey should be null on initial page", page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load middle page calculates correct prevKey and nextKey`() = runBlocking {
        val fakeApi = FakeMovieApiService()
        val pagingSource = MoviePagingSource(fakeApi)

        val params = PagingSource.LoadParams.Append(
            key = 2,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        val page = result as PagingSource.LoadResult.Page

        assertEquals(1, page.prevKey)
        assertEquals(3, page.nextKey)
    }

    @Test
    fun `load last page returns nextKey null`() = runBlocking {
        val fakeApi = FakeMovieApiService(
            getMoviesHandler = { page ->
                PaginatedResponseDto(
                    page = page,
                    results = listOf(MovieItemDto(id = 50, title = "Last Movie")),
                    totalPages = 5,
                    totalResults = 50
                )
            }
        )
        val pagingSource = MoviePagingSource(fakeApi)

        val params = PagingSource.LoadParams.Append(
            key = 5,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        val page = result as PagingSource.LoadResult.Page

        assertEquals(4, page.prevKey)
        assertNull("nextKey must be null when page reaches totalPages", page.nextKey)
    }

    @Test
    fun `load empty results returns nextKey null`() = runBlocking {
        val fakeApi = FakeMovieApiService(
            getMoviesHandler = { page ->
                PaginatedResponseDto(
                    page = page,
                    results = emptyList(),
                    totalPages = 10,
                    totalResults = 0
                )
            }
        )
        val pagingSource = MoviePagingSource(fakeApi)

        val params = PagingSource.LoadParams.Refresh(
            key = 1,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        val page = result as PagingSource.LoadResult.Page

        assertNull("nextKey must be null when results list is empty", page.nextKey)
    }

    @Test
    fun `load network error returns LoadResult Error`() = runBlocking {
        val ioException = IOException("Network unreachable")
        val fakeApi = FakeMovieApiService(
            getMoviesHandler = { throw ioException }
        )
        val pagingSource = MoviePagingSource(fakeApi)

        val params = PagingSource.LoadParams.Refresh(
            key = 1,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        assertTrue("Result should be LoadResult.Error", result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertEquals(ioException, error.throwable)
    }

    @Test(expected = CancellationException::class)
    fun `load rethrows CancellationException without swallowing`() {
        runBlocking {
            val fakeApi = FakeMovieApiService(
                getMoviesHandler = { throw CancellationException("Coroutine cancelled") }
            )
            val pagingSource = MoviePagingSource(fakeApi)

            val params = PagingSource.LoadParams.Refresh<Int>(
                key = 1,
                loadSize = 20,
                placeholdersEnabled = false
            )

            pagingSource.load(params)
        }
    }

    @Test
    fun `getRefreshKey resolves anchor page correctly`() {
        val fakeApi = FakeMovieApiService()
        val pagingSource = MoviePagingSource(fakeApi)

        val state = PagingState<Int, Movie>(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(
                        Movie(id = 1, title = "M1", posterUrl = null, releaseYear = "2026", voteAverage = 8.0)
                    ),
                    prevKey = 1,
                    nextKey = 3
                )
            ),
            anchorPosition = 0,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )

        val refreshKey = pagingSource.getRefreshKey(state)
        assertEquals(2, refreshKey)
    }
}
