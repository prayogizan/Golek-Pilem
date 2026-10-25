package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.model.StorageStats
import com.prayogizan.golekpilem.domain.repository.StorageRepository

/**
 * Interactor responsible for calculating current local storage and image disk cache usage.
 *
 * @param storageRepository The repository contract providing storage diagnostics.
 */
class GetStorageStatsUseCase(
    private val storageRepository: StorageRepository
) {
    /**
     * Executes the use case to compute storage metrics.
     *
     * @return [StorageStats] representing physical storage consumption.
     */
    suspend operator fun invoke(): StorageStats = storageRepository.getStorageStats()
}
