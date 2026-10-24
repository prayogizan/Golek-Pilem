package com.prayogizan.golekpilem.domain.usecase

import androidx.paging.PagingData
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.repository.TvRepository
import kotlinx.coroutines.flow.Flow

/**
 * Interactor responsible for retrieving paginated popular television series from the repository.
 *
 * Encapsulates the domain business policy for TV catalog discovery and streams a reactive
 * [PagingData] sequence for presentation consumption.
 *
 * @property tvRepository The repository abstraction providing access to television series data.
 */
class GetTvSeriesUseCase(
    private val tvRepository: TvRepository
) {
    /**
     * Executes the use case to stream popular television series.
     *
     * @return A cold [Flow] emitting reactive [PagingData] containing domain [TvSeries] entities.
     */
    operator fun invoke(): Flow<PagingData<TvSeries>> = tvRepository.getTvSeries()
}
