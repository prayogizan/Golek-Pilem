package com.prayogizan.golekpilem.domain.model

/**
 * Comprehensive domain entity representing detailed information for a specific television series.
 *
 * @property id The unique TMDB identifier for the television series.
 * @property name The primary display name of the series.
 * @property tagline A brief promotional slogan or tagline.
 * @property overview Full synopsis describing the television series narrative.
 * @property posterUrl Full resolution TMDB CDN image URL (`w342`) for the primary poster, or `null` if unavailable.
 * @property backdropUrl Full resolution TMDB CDN image URL (`w780`) for the cinematic hero backdrop, or `null` if unavailable.
 * @property firstAirDate The premiere broadcast date in `YYYY-MM-DD` format, or `"N/A"` if unknown.
 * @property lastAirDate The most recent broadcast air date in `YYYY-MM-DD` format, or `"N/A"` if unknown.
 * @property numberOfSeasons Total count of officially aired seasons.
 * @property numberOfEpisodes Total count of episodes across all seasons.
 * @property voteAverage Average user rating scaled from 0.0 to 10.0, rounded to one decimal place.
 * @property voteCount Total number of user rating submissions.
 * @property genres Collection of [Genre] entities categorized under this series.
 * @property networks Collection of [Network] broadcast networks associated with the series.
 * @property status Current broadcast/production status (e.g. "Returning Series", "Ended").
 */
data class TvSeriesDetail(
    val id: Int,
    val name: String,
    val tagline: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val firstAirDate: String,
    val lastAirDate: String,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val networks: List<Network>,
    val status: String
)
