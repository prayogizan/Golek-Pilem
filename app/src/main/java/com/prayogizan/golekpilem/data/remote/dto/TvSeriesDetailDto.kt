package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing the complete detail payload for a TMDB television series endpoint.
 *
 * @property id The unique TMDB identifier for the television series.
 * @property name The official name of the television series.
 * @property tagline Short promotional tagline.
 * @property overview Full synopsis or narrative overview.
 * @property posterPath Relative URL path to the primary poster image, or `null`.
 * @property backdropPath Relative URL path to the hero backdrop image, or `null`.
 * @property firstAirDate Premiere broadcast air date string (`YYYY-MM-DD`).
 * @property lastAirDate Most recent broadcast air date string (`YYYY-MM-DD`).
 * @property numberOfSeasons Total count of aired seasons, or `null`.
 * @property numberOfEpisodes Total count of aired episodes, or `null`.
 * @property voteAverage Mean user rating score between 0.0 and 10.0, or `null`.
 * @property voteCount Cumulative count of user votes recorded, or `null`.
 * @property genres Collection of genres linked to the television series.
 * @property networks Collection of broadcast/streaming networks associated with the series.
 * @property status Current production/broadcast status (e.g. "Returning Series", "Ended").
 */
@Serializable
data class TvSeriesDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("last_air_date") val lastAirDate: String? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int? = null,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("vote_count") val voteCount: Int? = null,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    @SerialName("networks") val networks: List<NetworkDto> = emptyList(),
    @SerialName("status") val status: String? = null
)
