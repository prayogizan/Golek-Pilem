package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.TvSeries

/**
 * Transforms raw [MovieItemDto] into a persistable [MediaItemEntity] for local Room caching.
 *
 * @receiver The raw DTO received from the TMDB network response.
 * @param mediaType The media classification identifier ("MOVIE" or "TV").
 * @param pageIndex The pagination page index where this item was retrieved.
 * @return A sanitized [MediaItemEntity] ready for SQLite persistence.
 */
fun MovieItemDto.toEntity(mediaType: String = "MOVIE", pageIndex: Int): MediaItemEntity {
    return MediaItemEntity(
        id = id,
        mediaType = mediaType,
        title = title?.takeIf { it.isNotBlank() } ?: "Unknown",
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        overview = overview.orEmpty(),
        pageIndex = pageIndex,
        cachedAt = System.currentTimeMillis()
    )
}

/**
 * Transforms raw [TvSeriesItemDto] into a persistable [MediaItemEntity] for local Room caching.
 *
 * @receiver The raw DTO received from the TMDB network response.
 * @param mediaType The media classification identifier ("MOVIE" or "TV").
 * @param pageIndex The pagination page index where this item was retrieved.
 * @return A sanitized [MediaItemEntity] ready for SQLite persistence.
 */
fun TvSeriesItemDto.toEntity(mediaType: String = "TV", pageIndex: Int): MediaItemEntity {
    return MediaItemEntity(
        id = id,
        mediaType = mediaType,
        title = name?.takeIf { it.isNotBlank() } ?: "Unknown",
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = firstAirDate,
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        overview = overview.orEmpty(),
        pageIndex = pageIndex,
        cachedAt = System.currentTimeMillis()
    )
}

/**
 * Transforms a cached [MediaItemEntity] from Room into a presentation-ready domain [Movie].
 *
 * Applies defensive null checks, constructs complete TMDB CDN image URLs (`w185`),
 * safely parses release years, and rounds ratings to one decimal place.
 *
 * @receiver The cached Room database entity.
 * @return A domain [Movie] model.
 */
fun MediaItemEntity.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w185$it" },
        releaseYear = formatReleaseYear(releaseDate),
        voteAverage = sanitizeVoteAverage(voteAverage)
    )
}

/**
 * Transforms a cached [MediaItemEntity] from Room into a presentation-ready domain [TvSeries].
 *
 * Applies defensive null checks, constructs complete TMDB CDN image URLs (`w185`),
 * safely parses premiere air years, and rounds ratings to one decimal place.
 *
 * @receiver The cached Room database entity.
 * @return A domain [TvSeries] model.
 */
fun MediaItemEntity.toTvSeries(): TvSeries {
    return TvSeries(
        id = id,
        name = title,
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w185$it" },
        firstAirYear = formatReleaseYear(releaseDate),
        voteAverage = sanitizeVoteAverage(voteAverage)
    )
}
