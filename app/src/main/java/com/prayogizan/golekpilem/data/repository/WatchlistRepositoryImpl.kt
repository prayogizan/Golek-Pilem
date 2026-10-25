package com.prayogizan.golekpilem.data.repository

import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.data.mapper.toEntity
import com.prayogizan.golekpilem.data.mapper.toMediaItem
import com.prayogizan.golekpilem.domain.model.MediaItem
import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of [WatchlistRepository] backed by AndroidX Room local persistence.
 *
 * Provides a reactive Single Source of Truth (SSOT) for the personal watchlist collection,
 * guarantees coroutine cancellation safety, and performs canonical media type normalization.
 *
 * @param database The primary [GolekPilemDatabase] instance providing [com.prayogizan.golekpilem.data.local.dao.WatchlistDao].
 */
class WatchlistRepositoryImpl(
    private val database: GolekPilemDatabase
) : WatchlistRepository {

    private val watchlistDao = database.watchlistDao()

    override fun getWatchlist(): Flow<List<MediaItem>> {
        return watchlistDao.observeWatchlist().map { entities ->
            entities.map { it.toMediaItem() }
        }
    }

    override fun isWatchlisted(id: Int, mediaType: String): Flow<Boolean> {
        return watchlistDao.isWatchlisted(id = id, mediaType = mediaType.uppercase())
    }

    override suspend fun addToWatchlist(item: MediaItem) {
        try {
            watchlistDao.insertWatchlist(item.toEntity())
        } catch (e: CancellationException) {
            throw e
        }
    }

    override suspend fun removeFromWatchlist(id: Int, mediaType: String) {
        try {
            watchlistDao.deleteWatchlist(id = id, mediaType = mediaType.uppercase())
        } catch (e: CancellationException) {
            throw e
        }
    }

    override fun getWatchlistCount(): Flow<Int> {
        return watchlistDao.observeWatchlistCount()
    }
}
