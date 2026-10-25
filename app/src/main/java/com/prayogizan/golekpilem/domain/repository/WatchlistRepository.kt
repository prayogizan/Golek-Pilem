package com.prayogizan.golekpilem.domain.repository

import com.prayogizan.golekpilem.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction defining access contracts for user-curated offline watchlist persistence.
 *
 * Implements the Dependency Inversion Principle, decoupling presentation and domain policies
 * from underlying SQLite Room operations and caching implementations.
 */
interface WatchlistRepository {

    /**
     * Observes the complete collection of saved watchlist titles, ordered by most recently added.
     *
     * @return A cold [Flow] emitting the updated list of domain [MediaItem] entities.
     */
    fun getWatchlist(): Flow<List<MediaItem>>

    /**
     * Observes whether a specific media title is currently bookmarked in the watchlist.
     *
     * @param id The unique TMDB identifier of the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return A cold [Flow] emitting `true` if bookmarked, `false` otherwise.
     */
    fun isWatchlisted(id: Int, mediaType: String): Flow<Boolean>

    /**
     * Persists a media item into the offline watchlist.
     *
     * @param item The domain [MediaItem] to bookmark.
     */
    suspend fun addToWatchlist(item: MediaItem)

    /**
     * Removes a media title from the offline watchlist.
     *
     * @param id The unique TMDB identifier of the media.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     */
    suspend fun removeFromWatchlist(id: Int, mediaType: String)

    /**
     * Observes the total number of bookmarked items currently stored in the watchlist.
     *
     * @return A cold [Flow] emitting the count of saved titles.
     */
    fun getWatchlistCount(): Flow<Int>
}
