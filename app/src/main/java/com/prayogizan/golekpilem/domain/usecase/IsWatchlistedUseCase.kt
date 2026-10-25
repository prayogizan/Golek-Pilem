package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Interactor responsible for observing the bookmark status of a specific media title.
 *
 * @param watchlistRepository The repository contract providing access to watchlist operations.
 */
class IsWatchlistedUseCase(
    private val watchlistRepository: WatchlistRepository
) {
    /**
     * Executes the use case to observe whether a specific media item is bookmarked.
     *
     * @param id The unique TMDB identifier of the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return A cold [Flow] emitting `true` if bookmarked, `false` otherwise.
     */
    operator fun invoke(id: Int, mediaType: String): Flow<Boolean> =
        watchlistRepository.isWatchlisted(id = id, mediaType = mediaType)
}
