package com.prayogizan.golekpilem.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.prayogizan.golekpilem.data.mapper.toDomain
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.domain.model.TvSeries
import kotlinx.coroutines.CancellationException
import java.util.concurrent.ConcurrentHashMap

/**
 * Paging 3 source responsible for incremental page retrieval of popular television series from the TMDB API.
 *
 * Implements 1-based pagination indexing conforming to TMDB API requirements, defensive mapping,
 * deduplication of items across pages to ensure unique keys in Jetpack Compose,
 * and coroutine cancellation propagation to avoid silent coroutine leaks.
 *
 * @property apiService The Retrofit service abstraction used for remote data discovery.
 */
class TvPagingSource(
    private val apiService: TmdbApiService
) : PagingSource<Int, TvSeries>() {

    companion object {
        /**
         * TMDB pagination standard starting page index.
         */
        const val TMDB_STARTING_PAGE_INDEX = 1
    }

    private val seenIds = ConcurrentHashMap.newKeySet<Int>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvSeries> {
        val position = params.key ?: TMDB_STARTING_PAGE_INDEX
        return try {
            val response = apiService.getTvSeries(page = position)
            val tvSeries = response.results
                .filter { seenIds.add(it.id) }
                .map { it.toDomain() }
            val nextKey = if (position >= response.totalPages || response.results.isEmpty()) {
                null
            } else {
                position + 1
            }

            LoadResult.Page(
                data = tvSeries,
                prevKey = if (position == TMDB_STARTING_PAGE_INDEX) null else position - 1,
                nextKey = nextKey
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TvSeries>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
