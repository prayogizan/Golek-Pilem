package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing a television series item within a paginated TMDB discovery response.
 *
 * @property id The unique TMDB identifier for the television series.
 * @property name The primary display title of the series.
 * @property posterPath Relative URL path to the series poster graphic, or `null`.
 * @property backdropPath Relative URL path to the series backdrop graphic, or `null`.
 * @property firstAirDate The initial broadcast date string in `YYYY-MM-DD` format, or `null`.
 * @property voteAverage User rating score on a 0.0 to 10.0 scale, or `null`.
 * @property voteCount Number of total user votes submitted, or `null`.
 * @property overview Synopsis or plot summary of the series, or `null`.
 */
@Serializable
data class TvSeriesItemDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
    @SerialName("overview") val overview: String? = null
)
