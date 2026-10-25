package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.local.converter.Converters
import com.prayogizan.golekpilem.data.local.entity.MediaDetailEntity
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity
import com.prayogizan.golekpilem.data.local.model.toDomain
import com.prayogizan.golekpilem.data.local.model.toLocal
import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import com.prayogizan.golekpilem.domain.model.Video

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

/**
 * Transforms raw [MovieDetailDto] network responses into a persistable [MediaDetailEntity] for Room caching.
 *
 * Serializes nested genre collections, user critiques, and primary video trailer key
 * using [Converters] while calculating default expiration thresholds.
 *
 * @receiver The raw DTO received from the TMDB movie detail endpoint.
 * @param mediaType The media classification identifier (defaults to "MOVIE").
 * @param cachedAt Epoch timestamp marking record generation (defaults to current system time).
 * @return A sanitized, SQLite-ready [MediaDetailEntity].
 */
fun MovieDetailDto.toEntity(
    mediaType: String = "MOVIE",
    cachedAt: Long = System.currentTimeMillis()
): MediaDetailEntity {
    val converters = Converters()
    val primaryTrailer = videos.toDomainList().firstOrNull { it.isYouTube && it.type.equals("Trailer", ignoreCase = true) }
        ?: videos.toDomainList().firstOrNull { it.isYouTube }

    val genreNames = genres.mapNotNull { it.name?.takeIf { name -> name.isNotBlank() } }
    val localReviews = reviews?.results?.map { it.toDomain().toLocal() } ?: emptyList()

    return MediaDetailEntity(
        id = id,
        mediaType = mediaType,
        title = title?.takeIf { it.isNotBlank() } ?: "Unknown",
        overview = overview.orEmpty(),
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = sanitizeVoteAverage(voteAverage),
        voteCount = voteCount ?: 0,
        releaseDate = releaseDate,
        runtimeOrSeasons = runtime?.let { "$it min" } ?: "0 min",
        genresJson = converters.fromStringList(genreNames),
        status = status?.takeIf { it.isNotBlank() } ?: "Unknown",
        trailerKey = primaryTrailer?.key,
        trailerTitle = primaryTrailer?.name,
        reviewsJson = converters.fromReviewList(localReviews),
        cachedAt = cachedAt,
        ttlMillis = MediaDetailEntity.DEFAULT_TTL_MILLIS
    )
}

/**
 * Transforms raw [TvSeriesDetailDto] network responses into a persistable [MediaDetailEntity] for Room caching.
 *
 * Serializes nested genre collections, user critiques, and primary video trailer key
 * using [Converters] while formatting season counts and calculating expiration thresholds.
 *
 * @receiver The raw DTO received from the TMDB series detail endpoint.
 * @param mediaType The media classification identifier (defaults to "TV").
 * @param cachedAt Epoch timestamp marking record generation (defaults to current system time).
 * @return A sanitized, SQLite-ready [MediaDetailEntity].
 */
fun TvSeriesDetailDto.toEntity(
    mediaType: String = "TV",
    cachedAt: Long = System.currentTimeMillis()
): MediaDetailEntity {
    val converters = Converters()
    val primaryTrailer = videos.toDomainList().firstOrNull { it.isYouTube && it.type.equals("Trailer", ignoreCase = true) }
        ?: videos.toDomainList().firstOrNull { it.isYouTube }

    val genreNames = genres.mapNotNull { it.name?.takeIf { name -> name.isNotBlank() } }
    val localReviews = reviews?.results?.map { it.toDomain().toLocal() } ?: emptyList()

    val seasonsCount = numberOfSeasons ?: 0
    val seasonsText = if (seasonsCount == 1) "1 Season" else "$seasonsCount Seasons"

    return MediaDetailEntity(
        id = id,
        mediaType = mediaType,
        title = name?.takeIf { it.isNotBlank() } ?: "Unknown",
        overview = overview.orEmpty(),
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = sanitizeVoteAverage(voteAverage),
        voteCount = voteCount ?: 0,
        releaseDate = firstAirDate,
        runtimeOrSeasons = seasonsText,
        genresJson = converters.fromStringList(genreNames),
        status = status?.takeIf { it.isNotBlank() } ?: "Unknown",
        trailerKey = primaryTrailer?.key,
        trailerTitle = primaryTrailer?.name,
        reviewsJson = converters.fromReviewList(localReviews),
        cachedAt = cachedAt,
        ttlMillis = MediaDetailEntity.DEFAULT_TTL_MILLIS
    )
}

/**
 * Transforms a cached [MediaDetailEntity] from Room into a presentation-ready domain [MovieDetail].
 *
 * Restores serialized genres, reviews, and trailer metadata, constructing full TMDB CDN
 * image URLs and tagging the entity with its offline cache state.
 *
 * @receiver The persistent Room detail record.
 * @param isOfflineCache Indicates whether this record is served from offline storage.
 * @return A clean domain [MovieDetail] instance.
 */
fun MediaDetailEntity.toMovieDetail(isOfflineCache: Boolean = true): MovieDetail {
    val converters = Converters()
    val parsedGenres = converters.toStringList(genresJson).map { Genre(id = 0, name = it) }
    val parsedReviews = converters.toReviewList(reviewsJson).map { it.toDomain() }
    val parsedRuntime = runtimeOrSeasons.filter { it.isDigit() }.toIntOrNull() ?: 0

    val mappedVideos = if (!trailerKey.isNullOrBlank()) {
        listOf(
            Video(
                id = trailerKey,
                name = trailerTitle?.takeIf { it.isNotBlank() } ?: "Official Trailer",
                key = trailerKey,
                site = "YouTube",
                type = "Trailer",
                isOfficial = true,
                publishedAt = ""
            )
        )
    } else {
        emptyList()
    }

    return MovieDetail(
        id = id,
        title = title,
        tagline = "",
        overview = overview,
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w342$it" },
        backdropUrl = backdropPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w780$it" },
        releaseDate = releaseDate?.takeIf { it.isNotBlank() } ?: "N/A",
        runtime = parsedRuntime,
        voteAverage = sanitizeVoteAverage(voteAverage),
        voteCount = voteCount,
        genres = parsedGenres,
        productionCompanies = emptyList(),
        status = status,
        reviews = parsedReviews,
        videos = mappedVideos,
        isOfflineCache = isOfflineCache
    )
}

/**
 * Transforms a cached [MediaDetailEntity] from Room into a presentation-ready domain [TvSeriesDetail].
 *
 * Restores serialized genres, reviews, and trailer metadata, constructing full TMDB CDN
 * image URLs and tagging the entity with its offline cache state.
 *
 * @receiver The persistent Room detail record.
 * @param isOfflineCache Indicates whether this record is served from offline storage.
 * @return A clean domain [TvSeriesDetail] instance.
 */
fun MediaDetailEntity.toTvSeriesDetail(isOfflineCache: Boolean = true): TvSeriesDetail {
    val converters = Converters()
    val parsedGenres = converters.toStringList(genresJson).map { Genre(id = 0, name = it) }
    val parsedReviews = converters.toReviewList(reviewsJson).map { it.toDomain() }
    val parsedSeasons = runtimeOrSeasons.filter { it.isDigit() }.toIntOrNull() ?: 0

    val mappedVideos = if (!trailerKey.isNullOrBlank()) {
        listOf(
            Video(
                id = trailerKey,
                name = trailerTitle?.takeIf { it.isNotBlank() } ?: "Official Trailer",
                key = trailerKey,
                site = "YouTube",
                type = "Trailer",
                isOfficial = true,
                publishedAt = ""
            )
        )
    } else {
        emptyList()
    }

    return TvSeriesDetail(
        id = id,
        name = title,
        tagline = "",
        overview = overview,
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w342$it" },
        backdropUrl = backdropPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w780$it" },
        firstAirDate = releaseDate?.takeIf { it.isNotBlank() } ?: "N/A",
        lastAirDate = "N/A",
        numberOfSeasons = parsedSeasons,
        numberOfEpisodes = 0,
        voteAverage = sanitizeVoteAverage(voteAverage),
        voteCount = voteCount,
        genres = parsedGenres,
        networks = emptyList(),
        status = status,
        reviews = parsedReviews,
        videos = mappedVideos,
        isOfflineCache = isOfflineCache
    )
}

