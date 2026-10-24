package com.prayogizan.golekpilem.domain.model

/**
 * Represents a simplified movie entity utilized across discovery feeds and lists.
 *
 * @property id The unique TMDB identifier for the movie.
 * @property title The localized or primary display title.
 * @property posterUrl Full image URL (`w185`), or `null` if no poster artwork is available.
 * @property releaseYear The 4-digit release year (`YYYY`) or `"N/A"` if unreleased or unknown.
 * @property voteAverage User rating score scaled between 0.0 and 10.0, rounded to one decimal place.
 */
data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val releaseYear: String,
    val voteAverage: Double
)
