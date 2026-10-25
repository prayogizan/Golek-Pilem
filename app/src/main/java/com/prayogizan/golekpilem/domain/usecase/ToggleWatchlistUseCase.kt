package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.model.MediaItem
import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.first

/**
 * Interactor responsible for atomically toggling the bookmark state of a media item in the personal watchlist.
 *
 * If the item is currently saved, it removes it from the local storage; if absent, it saves it.
 *
 * @param watchlistRepository The repository contract providing access to watchlist operations.
 */
class ToggleWatchlistUseCase(
    private val watchlistRepository: WatchlistRepository
) {
    /**
     * Executes the atomic toggle operation for the specified media item.
     *
     * @param item The [MediaItem] entity to add or remove.
     * @return `true` if the item was added to the watchlist, `false` if it was removed.
     */
    suspend operator fun invoke(item: MediaItem): Boolean {
        val currentlyWatchlisted = watchlistRepository.isWatchlisted(item.id, item.mediaType).first()
        return if (currentlyWatchlisted) {
            watchlistRepository.removeFromWatchlist(item.id, item.mediaType)
            false
        } else {
            watchlistRepository.addToWatchlist(item)
            true
        }
    }
}
