package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.model.MediaItem
import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Interactor responsible for observing the complete collection of saved watchlist titles.
 *
 * Encapsulates the domain business policy for personal favorites discovery and streams
 * reactive updates directly from the underlying repository.
 *
 * @param watchlistRepository The repository contract providing access to watchlist operations.
 */
class GetWatchlistUseCase(
    private val watchlistRepository: WatchlistRepository
) {
    /**
     * Executes the use case to observe bookmarked watchlist media.
     *
     * @return A cold [Flow] emitting the updated list of domain [MediaItem] entities.
     */
    operator fun invoke(): Flow<List<MediaItem>> = watchlistRepository.getWatchlist()
}
