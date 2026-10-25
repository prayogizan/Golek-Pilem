package com.prayogizan.golekpilem.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object managing user-curated bookmarks in the `watchlist` table.
 *
 * Exposes reactive [Flow] streams to synchronize bookmark states across the main feed,
 * detail screens, and dedicated watchlist collections.
 */
@Dao
interface WatchlistDao {

    /**
     * Observes the complete collection of saved watchlist titles, ordered by most recently added.
     *
     * @return A cold [Flow] emitting the updated list of [WatchlistEntity] items.
     */
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun observeWatchlist(): Flow<List<WatchlistEntity>>

    /**
     * Observes whether a specific title is currently bookmarked in the user's watchlist.
     *
     * @param id The unique TMDB identifier for the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return A cold [Flow] emitting `true` if bookmarked, or `false` otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :id AND mediaType = :mediaType)")
    fun isWatchlisted(id: Int, mediaType: String): Flow<Boolean>

    /**
     * Checks synchronously whether a specific title exists in the watchlist.
     *
     * @param id The unique TMDB identifier for the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return `true` if present, `false` otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :id AND mediaType = :mediaType)")
    suspend fun checkIsWatchlisted(id: Int, mediaType: String): Boolean

    /**
     * Inserts a bookmarked title into the watchlist, replacing any prior instance.
     *
     * @param item The [WatchlistEntity] item to bookmark.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity)

    /**
     * Removes a title from the user's watchlist.
     *
     * @param id The unique TMDB identifier for the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     */
    @Query("DELETE FROM watchlist WHERE id = :id AND mediaType = :mediaType")
    suspend fun deleteWatchlist(id: Int, mediaType: String): Int

    /**
     * Returns the total count of bookmarked titles in the watchlist.
     *
     * @return Total count of saved titles.
     */
    @Query("SELECT COUNT(*) FROM watchlist")
    fun observeWatchlistCount(): Flow<Int>
}
