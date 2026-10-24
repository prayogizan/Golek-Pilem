package com.prayogizan.golekpilem.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.prayogizan.golekpilem.data.mapper.toDomain
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.domain.model.Movie
import kotlinx.coroutines.CancellationException

/**
 * Paging 3 source responsible for incremental page retrieval of popular movies from the TMDB API.
 *
 * Implements 1-based pagination indexing conforming to TMDB API requirements, defensive mapping,
 * and coroutine cancellation propagation to avoid silent coroutine leaks.
 *
 * @property apiService The Retrofit service abstraction used for remote data discovery.
 */
class MoviePagingSource(
    private val apiService: TmdbApiService
) : PagingSource<Int, Movie>() {

    companion object {
        /**
         * TMDB pagination standard starting page index.
         */
        const val TMDB_STARTING_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val position = params.key ?: TMDB_STARTING_PAGE_INDEX
        return try {
            val response = apiService.getMovies(page = position)
            val movies = response.results.map { it.toDomain() }
            val nextKey = if (position >= response.totalPages || movies.isEmpty()) {
                null
            } else {
                position + 1
            }

            LoadResult.Page(
                data = movies,
                prevKey = if (position == TMDB_STARTING_PAGE_INDEX) null else position - 1,
                nextKey = nextKey
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
