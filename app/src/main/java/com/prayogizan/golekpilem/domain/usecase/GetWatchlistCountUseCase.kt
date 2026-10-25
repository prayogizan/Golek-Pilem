package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Interactor responsible for observing the real-time total count of saved watchlist titles.
 *
 * Utilized across UI presentation components such as tab badges to display reactive counters.
 *
 * @param watchlistRepository The repository contract providing access to watchlist operations.
 */
class GetWatchlistCountUseCase(
    private val watchlistRepository: WatchlistRepository
) {
    /**
     * Executes the use case to stream the total count of bookmarked items.
     *
     * @return A cold [Flow] emitting the updated count of saved titles.
     */
    operator fun invoke(): Flow<Int> = watchlistRepository.getWatchlistCount()
}
