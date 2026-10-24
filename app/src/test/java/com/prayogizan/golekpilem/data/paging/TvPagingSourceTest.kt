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
import com.prayogizan.golekpilem.domain.model.TvSeries
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class TvPagingSourceTest {

    private class FakeTvApiService(
        var getTvSeriesHandler: (page: Int) -> PaginatedResponseDto<TvSeriesItemDto> = {
            PaginatedResponseDto(
                page = it,
                results = listOf(
                    TvSeriesItemDto(id = 10, name = "Series 10", posterPath = "/s10.jpg", firstAirDate = "2026-02-02", voteAverage = 7.5)
                ),
                totalPages = 8,
                totalResults = 80
            )
        }
    ) : TmdbApiService {
        override suspend fun getMovies(page: Int, sortBy: String, language: String): PaginatedResponseDto<MovieItemDto> {
            throw UnsupportedOperationException()
        }

        override suspend fun getTvSeries(page: Int, sortBy: String, language: String): PaginatedResponseDto<TvSeriesItemDto> {
            return getTvSeriesHandler(page)
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
        val fakeApi = FakeTvApiService()
        val pagingSource = TvPagingSource(fakeApi)

        val params = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        assertTrue("Result should be LoadResult.Page", result is PagingSource.LoadResult.Page)

        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals(10, page.data.first().id)
        assertEquals("Series 10", page.data.first().name)
        assertNull("prevKey should be null on initial page", page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load middle page calculates correct prevKey and nextKey`() = runBlocking {
        val fakeApi = FakeTvApiService()
        val pagingSource = TvPagingSource(fakeApi)

        val params = PagingSource.LoadParams.Append(
            key = 3,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        val page = result as PagingSource.LoadResult.Page

        assertEquals(2, page.prevKey)
        assertEquals(4, page.nextKey)
    }

    @Test
    fun `load last page returns nextKey null`() = runBlocking {
        val fakeApi = FakeTvApiService(
            getTvSeriesHandler = { page ->
                PaginatedResponseDto(
                    page = page,
                    results = listOf(TvSeriesItemDto(id = 80, name = "Last Series")),
                    totalPages = 8,
                    totalResults = 80
                )
            }
        )
        val pagingSource = TvPagingSource(fakeApi)

        val params = PagingSource.LoadParams.Append(
            key = 8,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        val page = result as PagingSource.LoadResult.Page

        assertEquals(7, page.prevKey)
        assertNull("nextKey must be null when page reaches totalPages", page.nextKey)
    }

    @Test
    fun `load empty results returns nextKey null`() = runBlocking {
        val fakeApi = FakeTvApiService(
            getTvSeriesHandler = { page ->
                PaginatedResponseDto(
                    page = page,
                    results = emptyList(),
                    totalPages = 8,
                    totalResults = 0
                )
            }
        )
        val pagingSource = TvPagingSource(fakeApi)

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
        val fakeApi = FakeTvApiService(
            getTvSeriesHandler = { throw ioException }
        )
        val pagingSource = TvPagingSource(fakeApi)

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
            val fakeApi = FakeTvApiService(
                getTvSeriesHandler = { throw CancellationException("Coroutine cancelled") }
            )
            val pagingSource = TvPagingSource(fakeApi)

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
        val fakeApi = FakeTvApiService()
        val pagingSource = TvPagingSource(fakeApi)

        val state = PagingState<Int, TvSeries>(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(
                        TvSeries(id = 10, name = "S10", posterUrl = null, firstAirYear = "2026", voteAverage = 7.5)
                    ),
                    prevKey = 2,
                    nextKey = 4
                )
            ),
            anchorPosition = 0,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0
        )

        val refreshKey = pagingSource.getRefreshKey(state)
        assertEquals(3, refreshKey)
    }

    @Test
    fun `load filters duplicate tv series items within the same page`() = runBlocking {
        val fakeApi = FakeTvApiService(
            getTvSeriesHandler = { page ->
                PaginatedResponseDto(
                    page = page,
                    results = listOf(
                        TvSeriesItemDto(id = 505, name = "Breaking Bad"),
                        TvSeriesItemDto(id = 505, name = "Breaking Bad (Duplicate)"),
                        TvSeriesItemDto(id = 606, name = "Better Call Saul")
                    ),
                    totalPages = 4,
                    totalResults = 50
                )
            }
        )
        val pagingSource = TvPagingSource(fakeApi)

        val params = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 20,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(params)
        val page = result as PagingSource.LoadResult.Page

        assertEquals("Duplicates within the same page must be filtered", 2, page.data.size)
        assertEquals(505, page.data[0].id)
        assertEquals(606, page.data[1].id)
    }

    @Test
    fun `load filters duplicate tv series items across consecutive pages`() = runBlocking {
        val fakeApi = FakeTvApiService(
            getTvSeriesHandler = { page ->
                when (page) {
                    1 -> PaginatedResponseDto(
                        page = 1,
                        results = listOf(
                            TvSeriesItemDto(id = 505, name = "Breaking Bad"),
                            TvSeriesItemDto(id = 606, name = "Better Call Saul")
                        ),
                        totalPages = 4,
                        totalResults = 50
                    )
                    2 -> PaginatedResponseDto(
                        page = 2,
                        results = listOf(
                            TvSeriesItemDto(id = 505, name = "Breaking Bad"), // Duplicate across pages
                            TvSeriesItemDto(id = 707, name = "Severance")
                        ),
                        totalPages = 4,
                        totalResults = 50
                    )
                    else -> error("Unexpected page: $page")
                }
            }
        )
        val pagingSource = TvPagingSource(fakeApi)

        // Page 1
        val page1Result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        assertEquals(2, page1Result.data.size)
        assertEquals(listOf(505, 606), page1Result.data.map { it.id })

        // Page 2
        val page2Result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        assertEquals("Duplicate item from page 1 must be filtered out in page 2", 1, page2Result.data.size)
        assertEquals(707, page2Result.data.first().id)
        assertEquals(3, page2Result.nextKey)
    }
}
