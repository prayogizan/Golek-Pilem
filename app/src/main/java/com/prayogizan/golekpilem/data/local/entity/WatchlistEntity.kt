package com.prayogizan.golekpilem.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Room entity representing a user-bookmarked media title in the Watchlist (Favorites).
 *
 * Persisted indefinitely and strictly exempt from automated cache pruning or eviction,
 * ensuring bookmarked titles remain accessible offline until explicitly removed by the user.
 *
 * @property id The unique TMDB identifier for the media.
 * @property mediaType The media classification identifier ("MOVIE" or "TV").
 * @property title The localized or primary display title.
 * @property posterPath Relative poster image path from TMDB API, or `null`.
 * @property backdropPath Relative cinematic backdrop image path from TMDB API, or `null`.
 * @property voteAverage User rating score scaled between 0.0 and 10.0.
 * @property releaseDate Primary release or premiere air date (`YYYY-MM-DD`), or `null`.
 * @property overview Narrative overview synopsis.
 * @property addedAt Epoch millisecond timestamp recording when the title was bookmarked.
 */
@Entity(
    tableName = "watchlist",
    primaryKeys = ["id", "mediaType"],
    indices = [
        Index(value = ["addedAt"])
    ]
)
data class WatchlistEntity(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "mediaType") val mediaType: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "posterPath") val posterPath: String?,
    @ColumnInfo(name = "backdropPath") val backdropPath: String?,
    @ColumnInfo(name = "voteAverage") val voteAverage: Double,
    @ColumnInfo(name = "releaseDate") val releaseDate: String?,
    @ColumnInfo(name = "overview") val overview: String,
    @ColumnInfo(name = "addedAt") val addedAt: Long = System.currentTimeMillis()
)
