package com.prayogizan.golekpilem.domain.model

/**
 * Represents a simplified television series entity utilized across discovery feeds and lists.
 *
 * @property id The unique TMDB identifier for the television series.
 * @property name The primary display name of the series.
 * @property posterUrl Full image URL (`w185`), or `null` if no poster artwork is available.
 * @property firstAirYear The 4-digit premiere year (`YYYY`) or `"N/A"` if unknown.
 * @property voteAverage User rating score scaled between 0.0 and 10.0, rounded to one decimal place.
 */
data class TvSeries(
    val id: Int,
    val name: String,
    val posterUrl: String?,
    val firstAirYear: String,
    val voteAverage: Double
)
