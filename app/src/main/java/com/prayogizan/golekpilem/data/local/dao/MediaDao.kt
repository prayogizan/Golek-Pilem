package com.prayogizan.golekpilem.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity

/**
 * Data Access Object managing cached media catalog records in the `media_catalog` table.
 *
 * Exposes a reactive [PagingSource] for Paging 3 integration and supports batch upsertion
 * and selective cache invalidation.
 */
@Dao
interface MediaDao {

    /**
     * Retrieves paginated media catalog entries for the specified [mediaType],
     * ordered sequentially by page index and identifier.
     *
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return A [PagingSource] providing sequential page loads for Paging 3.
     */
    @Query("SELECT * FROM media_catalog WHERE mediaType = :mediaType ORDER BY pageIndex ASC, id ASC")
    fun getPagedMedia(mediaType: String): PagingSource<Int, MediaItemEntity>

    /**
     * Inserts a collection of media items into the database, replacing conflicting records.
     *
     * @param items The list of [MediaItemEntity] items to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItemEntity>)

    /**
     * Deletes all cached catalog items belonging to the given [mediaType].
     *
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     */
    @Query("DELETE FROM media_catalog WHERE mediaType = :mediaType")
    suspend fun clearByMediaType(mediaType: String): Int

    /**
     * Purges cached catalog items that were recorded earlier than the specified [timestamp].
     *
     * @param timestamp The cutoff epoch millisecond timestamp for pruning old entries.
     */
    @Query("DELETE FROM media_catalog WHERE cachedAt < :timestamp")
    suspend fun purgeOlderThan(timestamp: Long): Int

    /**
     * Purges cached catalog items that were recorded earlier than the specified [cutoffTimestamp],
     * strictly exempting any entries that are currently bookmarked in the user's watchlist.
     *
     * @param cutoffTimestamp The cutoff epoch millisecond timestamp for pruning old entries.
     * @return The number of pruned catalog records.
     */
    @Query("DELETE FROM media_catalog WHERE cachedAt < :cutoffTimestamp AND NOT EXISTS (SELECT 1 FROM watchlist WHERE watchlist.id = media_catalog.id AND watchlist.mediaType = media_catalog.mediaType)")
    suspend fun purgeExpiredCatalogExceptWatchlist(cutoffTimestamp: Long): Int


    /**
     * Returns the total count of cached media items for the given [mediaType].
     *
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return The number of persisted records.
     */
    @Query("SELECT COUNT(*) FROM media_catalog WHERE mediaType = :mediaType")
    suspend fun countByMediaType(mediaType: String): Int
}
