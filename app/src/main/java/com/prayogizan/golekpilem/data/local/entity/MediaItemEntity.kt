package com.prayogizan.golekpilem.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Room entity representing a cached catalog feed item stored for offline browsing.
 *
 * Persists paginated media entries fetched from TMDB discovery endpoints, supporting
 * both Movie and TV Series media types distinguished by [mediaType].
 *
 * @property id The unique TMDB identifier for the media.
 * @property mediaType The media classification identifier ("MOVIE" or "TV").
 * @property title The localized or primary display title of the media.
 * @property posterPath Relative poster image path from TMDB API, or `null`.
 * @property backdropPath Relative cinematic backdrop image path from TMDB API, or `null`.
 * @property releaseDate Primary release or broadcast air date (`YYYY-MM-DD`), or `null`.
 * @property voteAverage User rating score scaled between 0.0 and 10.0.
 * @property voteCount Total count of user ratings.
 * @property overview Narrative overview synopsis.
 * @property pageIndex The pagination page index where this item was retrieved.
 * @property cachedAt Epoch millisecond timestamp recording when this record was cached.
 */
@Entity(
    tableName = "media_catalog",
    primaryKeys = ["id", "mediaType"],
    indices = [
        Index(value = ["mediaType", "pageIndex"])
    ]
)
data class MediaItemEntity(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "mediaType") val mediaType: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "posterPath") val posterPath: String?,
    @ColumnInfo(name = "backdropPath") val backdropPath: String?,
    @ColumnInfo(name = "releaseDate") val releaseDate: String?,
    @ColumnInfo(name = "voteAverage") val voteAverage: Double,
    @ColumnInfo(name = "voteCount") val voteCount: Int,
    @ColumnInfo(name = "overview") val overview: String,
    @ColumnInfo(name = "pageIndex") val pageIndex: Int,
    @ColumnInfo(name = "cachedAt") val cachedAt: Long = System.currentTimeMillis()
)
