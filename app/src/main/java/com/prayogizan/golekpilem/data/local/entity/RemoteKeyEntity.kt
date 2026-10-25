package com.prayogizan.golekpilem.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity storing pagination cursors for Paging 3 [androidx.paging.RemoteMediator].
 *
 * Tracks the previous and next page keys for individual media entries within a given [mediaType]
 * feed, enabling seamless bi-directional network pagination and boundary synchronization.
 *
 * @property id The unique TMDB identifier of the media item associated with this key.
 * @property mediaType The media classification identifier ("MOVIE" or "TV").
 * @property prevKey The preceding page index, or `null` if at the first page.
 * @property nextKey The subsequent page index, or `null` if pagination limit reached.
 * @property lastUpdated Epoch millisecond timestamp tracking when the key was last updated.
 */
@Entity(
    tableName = "remote_keys",
    primaryKeys = ["id", "mediaType"]
)
data class RemoteKeyEntity(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "mediaType") val mediaType: String,
    @ColumnInfo(name = "prevKey") val prevKey: Int?,
    @ColumnInfo(name = "nextKey") val nextKey: Int?,
    @ColumnInfo(name = "lastUpdated") val lastUpdated: Long = System.currentTimeMillis()
)
