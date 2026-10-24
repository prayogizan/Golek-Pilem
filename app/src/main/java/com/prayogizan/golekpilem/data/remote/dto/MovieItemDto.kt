package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing a movie item within a paginated TMDB discovery response.
 *
 * @property id The unique TMDB identifier for the movie.
 * @property title The primary display title of the movie.
 * @property posterPath Relative URL path to the movie poster graphic, or `null`.
 * @property backdropPath Relative URL path to the cinematic backdrop artwork, or `null`.
 * @property releaseDate The release date string in `YYYY-MM-DD` format, or `null`.
 * @property voteAverage User rating score on a 0.0 to 10.0 scale, or `null`.
 * @property voteCount Number of total user votes submitted, or `null`.
 * @property overview Synopsis or plot summary of the movie, or `null`.
 */
@Serializable
data class MovieItemDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
    @SerialName("overview") val overview: String? = null
)
