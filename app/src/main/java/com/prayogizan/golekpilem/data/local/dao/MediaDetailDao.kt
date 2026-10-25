package com.prayogizan.golekpilem.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prayogizan.golekpilem.data.local.entity.MediaDetailEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object managing comprehensive media detail cache entries in the `media_detail` table.
 *
 * Exposes cold [Flow] streams for reactive UI observation and suspend methods for
 * stale-while-revalidate caching engines.
 */
@Dao
interface MediaDetailDao {

    /**
     * Observes continuous updates to a media item's cached details.
     *
     * @param id The unique TMDB identifier for the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return A cold [Flow] emitting the updated [MediaDetailEntity], or `null` if uncached.
     */
    @Query("SELECT * FROM media_detail WHERE id = :id AND mediaType = :mediaType")
    fun observeDetail(id: Int, mediaType: String): Flow<MediaDetailEntity?>

    /**
     * Retrieves the single cached detail record for the specified media item.
     *
     * @param id The unique TMDB identifier for the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return The [MediaDetailEntity] instance, or `null` if not found in cache.
     */
    @Query("SELECT * FROM media_detail WHERE id = :id AND mediaType = :mediaType")
    suspend fun getDetail(id: Int, mediaType: String): MediaDetailEntity?

    /**
     * Persists or replaces a detailed media record in the local database.
     *
     * @param detail The [MediaDetailEntity] instance to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: MediaDetailEntity)

    /**
     * Deletes a specific media detail cache entry from the database.
     *
     * @param id The unique TMDB identifier for the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     */
    @Query("DELETE FROM media_detail WHERE id = :id AND mediaType = :mediaType")
    suspend fun deleteDetail(id: Int, mediaType: String): Int

    /**
     * Purges expired media details where `cachedAt + ttlMillis` is earlier than the given [currentTime].
     *
     * @param currentTime The current epoch millisecond timestamp.
     */
    @Query("DELETE FROM media_detail WHERE (cachedAt + ttlMillis) < :currentTime")
    suspend fun purgeExpiredDetails(currentTime: Long): Int
}
