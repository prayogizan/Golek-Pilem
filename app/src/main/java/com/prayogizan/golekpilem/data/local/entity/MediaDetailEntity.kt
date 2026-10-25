package com.prayogizan.golekpilem.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Room entity storing comprehensive details for movies and television series.
 *
 * Persists complete detail metadata—including serialized genre lists, trailer video keys,
 * and user reviews—to support instant offline display and stale-while-revalidate caching.
 *
 * @property id The unique TMDB identifier for the media.
 * @property mediaType The media classification identifier ("MOVIE" or "TV").
 * @property title The localized or primary display title.
 * @property overview Full synopsis narrative describing the media.
 * @property posterPath Relative poster image path from TMDB API, or `null`.
 * @property backdropPath Relative cinematic backdrop image path from TMDB API, or `null`.
 * @property voteAverage Average user rating score scaled between 0.0 and 10.0.
 * @property voteCount Total number of user rating submissions.
 * @property releaseDate Primary release or premiere air date (`YYYY-MM-DD`), or `null`.
 * @property runtimeOrSeasons Formatted runtime (e.g. "142 min") or season count (e.g. "3 Seasons").
 * @property genresJson JSON string representing a serialized collection of genre names.
 * @property status Commercial release or broadcast status (e.g. "Released", "Ended").
 * @property trailerKey YouTube video key for the official trailer, or `null` if unavailable.
 * @property trailerTitle Descriptive title for the trailer video, or `null`.
 * @property reviewsJson JSON string representing serialized [com.prayogizan.golekpilem.data.local.model.ReviewLocalModel] items.
 * @property cachedAt Epoch millisecond timestamp recording when this record was persisted.
 * @property ttlMillis Time-to-live validity window in milliseconds (default: 24 hours / 86,400,000 ms).
 */
@Entity(
    tableName = "media_detail",
    primaryKeys = ["id", "mediaType"]
)
data class MediaDetailEntity(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "mediaType") val mediaType: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "overview") val overview: String,
    @ColumnInfo(name = "posterPath") val posterPath: String?,
    @ColumnInfo(name = "backdropPath") val backdropPath: String?,
    @ColumnInfo(name = "voteAverage") val voteAverage: Double,
    @ColumnInfo(name = "voteCount") val voteCount: Int,
    @ColumnInfo(name = "releaseDate") val releaseDate: String?,
    @ColumnInfo(name = "runtimeOrSeasons") val runtimeOrSeasons: String,
    @ColumnInfo(name = "genresJson") val genresJson: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "trailerKey") val trailerKey: String?,
    @ColumnInfo(name = "trailerTitle") val trailerTitle: String?,
    @ColumnInfo(name = "reviewsJson") val reviewsJson: String,
    @ColumnInfo(name = "cachedAt") val cachedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "ttlMillis") val ttlMillis: Long = DEFAULT_TTL_MILLIS
) {
    /**
     * Evaluates whether the cached record has exceeded its expiration threshold.
     */
    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        return (currentTime - cachedAt) > ttlMillis
    }

    companion object {
        /**
         * Standard cache Time-To-Live window: 24 Hours in milliseconds.
         */
        const val DEFAULT_TTL_MILLIS: Long = 86_400_000L
    }
}
