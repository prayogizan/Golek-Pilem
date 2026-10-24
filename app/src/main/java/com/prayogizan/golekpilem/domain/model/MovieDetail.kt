package com.prayogizan.golekpilem.domain.model

/**
 * Comprehensive domain entity representing detailed information for a specific movie.
 *
 * @property id The unique TMDB identifier for the movie.
 * @property title The localized or primary title of the movie.
 * @property tagline A brief promotional slogan or tagline.
 * @property overview Full synopsis describing the movie narrative.
 * @property posterUrl Full resolution TMDB CDN image URL (`w342`) for the primary poster, or `null` if unavailable.
 * @property backdropUrl Full resolution TMDB CDN image URL (`w780`) for the cinematic hero backdrop, or `null` if unavailable.
 * @property releaseDate The primary release date in `YYYY-MM-DD` format, or `"N/A"` if unavailable.
 * @property runtime Total movie duration in minutes, or `0` if unknown.
 * @property voteAverage Average user rating scaled from 0.0 to 10.0, rounded to one decimal place.
 * @property voteCount Total number of user rating submissions.
 * @property genres Collection of [Genre] entities categorized under this movie.
 * @property productionCompanies Collection of [ProductionCompany] entities involved in production.
 * @property status Current commercial release status (e.g. "Released", "Post Production").
 * @property reviews Collection of [Review] entities submitted for this movie.
 */
data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val runtime: Int,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val productionCompanies: List<ProductionCompany>,
    val status: String,
    val reviews: List<Review> = emptyList()
)

