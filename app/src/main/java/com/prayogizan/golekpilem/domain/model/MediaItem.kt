package com.prayogizan.golekpilem.domain.model

/**
 * Universal domain model representing a media entity (Movie or TV Series).
 *
 * Designed to provide a unified representation across cross-cutting features such as
 * the personal offline Watchlist, bookmarks, and discovery collections.
 *
 * @property id The unique TMDB identifier for the media.
 * @property mediaType The media classification identifier ("MOVIE" or "TV").
 * @property title The localized or primary display title of the media.
 * @property posterUrl Full image URL (`w342`), or `null` if no poster artwork is available.
 * @property backdropUrl Full image URL (`w780`), or `null` if no backdrop artwork is available.
 * @property releaseDate Primary release or premiere air date (`YYYY-MM-DD`), or `null`.
 * @property releaseYear The 4-digit release year (`YYYY`) or `"N/A"` if unreleased or unknown.
 * @property voteAverage User rating score scaled between 0.0 and 10.0, rounded to one decimal place.
 * @property overview Narrative overview synopsis.
 * @property addedAt Epoch millisecond timestamp recording when this item was added to the watchlist.
 */
data class MediaItem(
    val id: Int,
    val mediaType: String,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?,
    val releaseYear: String,
    val voteAverage: Double,
    val overview: String,
    val addedAt: Long = 0L
)
