package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity
import com.prayogizan.golekpilem.domain.model.MediaItem
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail

/**
 * Transforms a persistent [WatchlistEntity] from Room into a presentation-ready domain [MediaItem].
 *
 * Applies defensive null checks, constructs complete TMDB CDN image URLs (`w342` for poster,
 * `w780` for backdrop), parses release years, and rounds ratings to one decimal place.
 *
 * @receiver The stored Room watchlist entity.
 * @return A clean domain [MediaItem] instance.
 */
fun WatchlistEntity.toMediaItem(): MediaItem {
    return MediaItem(
        id = id,
        mediaType = mediaType,
        title = title,
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { path ->
            if (path.startsWith("http")) path else "${ApiConstants.IMAGE_BASE_URL}w342$path"
        },
        backdropUrl = backdropPath?.takeIf { it.isNotBlank() }?.let { path ->
            if (path.startsWith("http")) path else "${ApiConstants.IMAGE_BASE_URL}w780$path"
        },
        releaseDate = releaseDate,
        releaseYear = formatReleaseYear(releaseDate),
        voteAverage = sanitizeVoteAverage(voteAverage),
        overview = overview,
        addedAt = addedAt
    )
}

/**
 * Transforms a domain [MediaItem] into a persistable [WatchlistEntity] for Room database storage.
 *
 * Extracts relative TMDB artwork paths when complete CDN URLs are provided and sets the bookmark timestamp.
 *
 * @receiver The domain [MediaItem] to persist.
 * @return A sanitized [WatchlistEntity] ready for SQLite insertion.
 */
fun MediaItem.toEntity(): WatchlistEntity {
    return WatchlistEntity(
        id = id,
        mediaType = mediaType.uppercase(),
        title = title.takeIf { it.isNotBlank() } ?: "Unknown",
        posterPath = extractRelativePath(posterUrl),
        backdropPath = extractRelativePath(backdropUrl),
        voteAverage = sanitizeVoteAverage(voteAverage),
        releaseDate = releaseDate,
        overview = overview,
        addedAt = if (addedAt > 0L) addedAt else System.currentTimeMillis()
    )
}

/**
 * Converts a domain [MovieDetail] entity into a universal [MediaItem] suitable for bookmarking.
 *
 * @receiver The detailed movie model retrieved from the repository.
 * @return A [MediaItem] representation of the movie.
 */
fun MovieDetail.toMediaItem(): MediaItem {
    return MediaItem(
        id = id,
        mediaType = "MOVIE",
        title = title,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate.takeIf { it != "N/A" },
        releaseYear = formatReleaseYear(releaseDate),
        voteAverage = sanitizeVoteAverage(voteAverage),
        overview = overview,
        addedAt = System.currentTimeMillis()
    )
}

/**
 * Converts a domain [TvSeriesDetail] entity into a universal [MediaItem] suitable for bookmarking.
 *
 * @receiver The detailed television series model retrieved from the repository.
 * @return A [MediaItem] representation of the TV series.
 */
fun TvSeriesDetail.toMediaItem(): MediaItem {
    return MediaItem(
        id = id,
        mediaType = "TV",
        title = name,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = firstAirDate.takeIf { it != "N/A" },
        releaseYear = formatReleaseYear(firstAirDate),
        voteAverage = sanitizeVoteAverage(voteAverage),
        overview = overview,
        addedAt = System.currentTimeMillis()
    )
}

/**
 * Extracts the relative image path from a potential TMDB CDN URL or returns the path as-is.
 *
 * For example:
 * `https://image.tmdb.org/t/p/w342/xyz.jpg` -> `/xyz.jpg`
 */
internal fun extractRelativePath(urlOrPath: String?): String? {
    if (urlOrPath.isNullOrBlank()) return null
    return if (urlOrPath.contains("/t/p/")) {
        val pathIndex = urlOrPath.indexOf("/t/p/")
        val afterPrefix = urlOrPath.substring(pathIndex + "/t/p/".length)
        // afterPrefix looks like "w342/xyz.jpg" or "w780/xyz.jpg"
        val slashIndex = afterPrefix.indexOf('/')
        if (slashIndex != -1) afterPrefix.substring(slashIndex) else urlOrPath
    } else {
        urlOrPath
    }
}
