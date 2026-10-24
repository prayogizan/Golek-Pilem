package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.remote.dto.GenreDto
import com.prayogizan.golekpilem.data.remote.dto.NetworkDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.domain.model.Network
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail

/**
 * Maps raw [TvSeriesItemDto] network responses to domain [TvSeries] entities.
 *
 * Applies defensive null checks, constructs complete TMDB CDN image URLs (`w185`),
 * safely parses premiere air years, and rounds ratings to one decimal place.
 *
 * @receiver The raw DTO received from the TMDB network response.
 * @return A sanitized, UI-ready domain [TvSeries] instance.
 */
fun TvSeriesItemDto.toDomain(): TvSeries {
    return TvSeries(
        id = id,
        name = name?.takeIf { it.isNotBlank() } ?: "Unknown",
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w185$it" },
        firstAirYear = formatReleaseYear(firstAirDate),
        voteAverage = sanitizeVoteAverage(voteAverage)
    )
}

/**
 * Maps raw [TvSeriesDetailDto] network responses to domain [TvSeriesDetail] entities.
 *
 * Applies defensive null checks, constructs complete TMDB CDN image URLs for poster (`w342`)
 * and backdrop (`w780`), safely extracts dates and numbers, and maps nested collections.
 *
 * @receiver The raw DTO received from the TMDB television series detail response.
 * @return A sanitized, UI-ready domain [TvSeriesDetail] instance.
 */
fun TvSeriesDetailDto.toDomain(): TvSeriesDetail {
    return TvSeriesDetail(
        id = id,
        name = name?.takeIf { it.isNotBlank() } ?: "Unknown",
        tagline = tagline.orEmpty(),
        overview = overview.orEmpty(),
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w342$it" },
        backdropUrl = backdropPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w780$it" },
        firstAirDate = firstAirDate?.takeIf { it.isNotBlank() } ?: "N/A",
        lastAirDate = lastAirDate?.takeIf { it.isNotBlank() } ?: "N/A",
        numberOfSeasons = numberOfSeasons ?: 0,
        numberOfEpisodes = numberOfEpisodes ?: 0,
        voteAverage = sanitizeVoteAverage(voteAverage),
        voteCount = voteCount ?: 0,
        genres = genres.map { it.toDomain() },
        networks = networks.map { it.toDomain() },
        status = status?.takeIf { it.isNotBlank() } ?: "Unknown",
        reviews = reviews?.results?.map { it.toDomain() } ?: emptyList()
    )
}

/**
 * Maps raw [NetworkDto] to domain [Network] entity.
 *
 * @receiver The raw DTO received from TMDB.
 * @return A sanitized domain [Network] instance with logo CDN URL (`w185`).
 */
fun NetworkDto.toDomain(): Network {
    return Network(
        id = id,
        name = name.takeIf { it.isNotBlank() } ?: "Unknown",
        logoUrl = logoPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w185$it" }
    )
}
