package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing the complete detail payload for a TMDB movie endpoint.
 *
 * @property id The unique TMDB identifier for the movie.
 * @property title The official title of the movie.
 * @property tagline Short promotional tagline.
 * @property overview Full synopsis or narrative overview.
 * @property posterPath Relative URL path to the primary poster image, or `null`.
 * @property backdropPath Relative URL path to the hero backdrop image, or `null`.
 * @property releaseDate The primary theatrical or digital release date string (`YYYY-MM-DD`).
 * @property runtime Duration of the movie in minutes, or `null`.
 * @property voteAverage Mean user rating score between 0.0 and 10.0, or `null`.
 * @property voteCount Cumulative count of user votes recorded, or `null`.
 * @property genres Collection of genres linked to the movie.
 * @property productionCompanies Collection of companies associated with movie production.
 * @property status Current commercial status (e.g. "Released", "In Production").
 * @property reviews Embedded reviews sub-resource, or `null`.
 */
@Serializable
data class MovieDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    @SerialName("production_companies") val productionCompanies: List<ProductionCompanyDto> = emptyList(),
    @SerialName("status") val status: String? = null,
    @SerialName("reviews") val reviews: ReviewsResponseDto? = null
)
