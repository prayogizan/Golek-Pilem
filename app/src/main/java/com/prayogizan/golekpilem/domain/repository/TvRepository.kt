package com.prayogizan.golekpilem.domain.repository

import androidx.paging.PagingData
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction defining access contracts for television series pagination and entity details.
 *
 * Implements the Dependency Inversion Principle, decoupling presentation and domain policies
 * from underlying remote network APIs and local persistence frameworks.
 */
interface TvRepository {

    /**
     * Streams paginated popular television series from the underlying data source.
     *
     * @return A cold [Flow] emitting reactive [PagingData] containing domain [TvSeries] entities.
     */
    fun getTvSeries(): Flow<PagingData<TvSeries>>

    /**
     * Retrieves comprehensive detailed information for a specific television series by its unique identifier.
     *
     * Emits a lifecycle stream starting with [Resource.Loading], resolving to [Resource.Success] on success,
     * or [Resource.Error] with sanitized error messages on failure.
     *
     * @param seriesId The unique TMDB identifier of the target television series.
     * @param forceRefresh Whether to bypass valid local cache and force remote TMDB retrieval.
     * @return A cold [Flow] emitting the retrieval lifecycle state encapsulated within [Resource].
     */
    fun getTvSeriesDetail(seriesId: Int, forceRefresh: Boolean = false): Flow<Resource<TvSeriesDetail>>
}
