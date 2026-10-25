package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.repository.StorageRepository

/**
 * Interactor responsible for deleting stale feed items and pagination cursors older than the
 * retention threshold, while ensuring user-curated watchlist items are never deleted.
 *
 * @param storageRepository The repository contract managing local storage.
 */
class PurgeExpiredCatalogUseCase(
    private val storageRepository: StorageRepository
) {
    /**
     * Executes the use case to prune expired catalog items.
     *
     * @param retentionDays The maximum retention age in days (defaults to 7).
     * @return The number of records pruned from the catalog.
     */
    suspend operator fun invoke(retentionDays: Int = 7): Int =
        storageRepository.purgeExpiredCatalog(retentionDays)
}
