package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import com.prayogizan.golekpilem.domain.repository.TvRepository
import kotlinx.coroutines.flow.Flow

/**
 * Interactor responsible for retrieving full details of a specific television series.
 *
 * Encapsulates the domain business rule for fetching television series details and returns a reactive lifecycle stream.
 *
 * @property tvRepository The repository abstraction providing access to television series data.
 */
class GetTvSeriesDetailUseCase(
    private val tvRepository: TvRepository
) {
    /**
     * Executes the use case for the given television series identifier.
     *
     * @param seriesId The unique TMDB identifier of the target television series.
     * @param forceRefresh Whether to bypass local cache and force fresh remote retrieval.
     * @return A cold [Flow] emitting the retrieval lifecycle state encapsulated within [Resource].
     */
    operator fun invoke(seriesId: Int, forceRefresh: Boolean = false): Flow<Resource<TvSeriesDetail>> =
        tvRepository.getTvSeriesDetail(seriesId, forceRefresh)
}
