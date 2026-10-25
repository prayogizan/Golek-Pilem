package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.repository.StorageRepository

/**
 * Interactor responsible for purging cached artwork files from Coil's disk and memory caches.
 *
 * @param storageRepository The repository contract managing storage operations.
 */
class ClearImageCacheUseCase(
    private val storageRepository: StorageRepository
) {
    /**
     * Executes the use case to purge image caches.
     */
    suspend operator fun invoke() = storageRepository.clearImageCache()
}
