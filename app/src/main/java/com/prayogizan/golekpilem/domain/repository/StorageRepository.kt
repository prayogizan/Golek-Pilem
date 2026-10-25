package com.prayogizan.golekpilem.domain.repository

import com.prayogizan.golekpilem.domain.model.StorageStats

/**
 * Repository abstraction defining storage governance and disk cache maintenance operations.
 *
 * Implements the Dependency Inversion Principle, decoupling storage calculation and cache
 * invalidation policies from concrete Room databases and Coil caching engines.
 */
interface StorageRepository {

    /**
     * Inspects local storage utilization and calculates physical file and directory sizes.
     *
     * @return A [StorageStats] entity containing byte metrics and human-readable formatted sizes.
     */
    suspend fun getStorageStats(): StorageStats

    /**
     * Purges all cached artwork files from the Coil disk cache and memory cache.
     */
    suspend fun clearImageCache()

    /**
     * Deletes stale catalog items and pagination keys older than [retentionDays],
     * strictly preserving any entities currently bookmarked in the user's watchlist.
     *
     * @param retentionDays The maximum retention window in days (defaults to 7 days).
     * @return The count of pruned catalog records.
     */
    suspend fun purgeExpiredCatalog(retentionDays: Int = 7): Int
}
