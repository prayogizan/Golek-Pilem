package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.remote.dto.GenreDto
import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.ProductionCompanyDto
import com.prayogizan.golekpilem.data.remote.dto.ReviewDto
import com.prayogizan.golekpilem.data.remote.dto.VideoDto
import com.prayogizan.golekpilem.data.remote.dto.VideosResponseDto
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.ProductionCompany
import com.prayogizan.golekpilem.domain.model.Review
import com.prayogizan.golekpilem.domain.model.Video
import kotlin.math.roundToInt

/**
 * Maps raw [MovieItemDto] network responses to domain [Movie] entities.
 *
 * Applies defensive null checks, constructs complete TMDB CDN image URLs (`w185`),
 * safely parses release years, and rounds ratings to one decimal place.
 *
 * @receiver The raw DTO received from the TMDB network response.
 * @return A sanitized, UI-ready domain [Movie] instance.
 */
fun MovieItemDto.toDomain(): Movie {
    return Movie(
        id = id,
        title = title?.takeIf { it.isNotBlank() } ?: "Unknown",
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w185$it" },
        releaseYear = formatReleaseYear(releaseDate),
        voteAverage = sanitizeVoteAverage(voteAverage)
    )
}

/**
 * Maps raw [MovieDetailDto] network responses to domain [MovieDetail] entities.
 *
 * Applies defensive null checks, constructs complete TMDB CDN image URLs for poster (`w342`)
 * and backdrop (`w780`), safely extracts dates and numbers, and maps nested collections.
 *
 * @receiver The raw DTO received from the TMDB movie detail response.
 * @return A sanitized, UI-ready domain [MovieDetail] instance.
 */
fun MovieDetailDto.toDomain(): MovieDetail {
    return MovieDetail(
        id = id,
        title = title?.takeIf { it.isNotBlank() } ?: "Unknown",
        tagline = tagline.orEmpty(),
        overview = overview.orEmpty(),
        posterUrl = posterPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w342$it" },
        backdropUrl = backdropPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w780$it" },
        releaseDate = releaseDate?.takeIf { it.isNotBlank() } ?: "N/A",
        runtime = runtime ?: 0,
        voteAverage = sanitizeVoteAverage(voteAverage),
        voteCount = voteCount ?: 0,
        genres = genres.map { it.toDomain() },
        productionCompanies = productionCompanies.map { it.toDomain() },
        status = status?.takeIf { it.isNotBlank() } ?: "Unknown",
        reviews = reviews?.results?.map { it.toDomain() } ?: emptyList(),
        videos = videos.toDomainList()
    )
}

/**
 * Maps raw [ReviewDto] to domain [Review] entity.
 *
 * Applies defensive null checks, normalizes author avatar URLs (handling both
 * TMDB CDN paths and external Gravatar URLs), and extracts formatted submission dates.
 *
 * @receiver The raw DTO received from TMDB.
 * @return A sanitized domain [Review] instance.
 */
fun ReviewDto.toDomain(): Review {
    val authorName = authorDetails?.name?.takeIf { it.isNotBlank() }
        ?: author?.takeIf { it.isNotBlank() }
        ?: authorDetails?.username?.takeIf { it.isNotBlank() }
        ?: "Anonymous"

    val rawAvatar = authorDetails?.avatarPath
    val normalizedAvatar = when {
        rawAvatar.isNullOrBlank() -> null
        rawAvatar.startsWith("/http://") || rawAvatar.startsWith("/https://") -> rawAvatar.substring(1)
        rawAvatar.startsWith("http://") || rawAvatar.startsWith("https://") -> rawAvatar
        else -> "${ApiConstants.IMAGE_BASE_URL}w185$rawAvatar"
    }

    val formattedDate = createdAt?.split("T")?.firstOrNull()?.takeIf { it.isNotBlank() }
        ?: createdAt?.takeIf { it.isNotBlank() }
        ?: "N/A"

    return Review(
        id = id,
        author = authorName,
        content = content.orEmpty(),
        rating = authorDetails?.rating?.let { sanitizeVoteAverage(it) },
        avatarUrl = normalizedAvatar,
        createdAt = formattedDate
    )
}

/**
 * Maps raw [GenreDto] to domain [Genre] entity.
 *
 * @receiver The raw DTO received from TMDB.
 * @return A sanitized domain [Genre] instance.
 */
fun GenreDto.toDomain(): Genre {
    return Genre(
        id = id,
        name = name.takeIf { it.isNotBlank() } ?: "Unknown"
    )
}

/**
 * Maps raw [ProductionCompanyDto] to domain [ProductionCompany] entity.
 *
 * @receiver The raw DTO received from TMDB.
 * @return A sanitized domain [ProductionCompany] instance with logo CDN URL (`w185`).
 */
fun ProductionCompanyDto.toDomain(): ProductionCompany {
    return ProductionCompany(
        id = id,
        name = name.takeIf { it.isNotBlank() } ?: "Unknown",
        logoUrl = logoPath?.takeIf { it.isNotBlank() }?.let { "${ApiConstants.IMAGE_BASE_URL}w185$it" }
    )
}

/**
 * Safely extracts a 4-digit year from a TMDB date string formatted as `YYYY-MM-DD`.
 *
 * @param dateString The raw date string received from TMDB.
 * @return The 4-digit year or `"N/A"` if absent, malformed, or blank.
 */
internal fun formatReleaseYear(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "N/A"
    val potentialYear = dateString.split("-").firstOrNull()
    return if (potentialYear != null && potentialYear.length == 4 && potentialYear.all { it.isDigit() }) {
        potentialYear
    } else {
        "N/A"
    }
}

/**
 * Maps raw [VideoDto] to domain [Video] entity.
 *
 * Excludes entries with empty or blank platform keys or identifiers,
 * ensuring unplayable video resources are omitted from the UI layer.
 *
 * @receiver The raw DTO received from TMDB.
 * @return A sanitized domain [Video] instance or `null` if the entry is unplayable.
 */
fun VideoDto.toDomain(): Video? {
    val videoKey = key?.trim().orEmpty()
    val videoId = id?.trim().orEmpty()
    if (videoKey.isBlank() || videoId.isBlank()) return null

    return Video(
        id = videoId,
        name = name?.trim()?.ifBlank { "Trailer" } ?: "Trailer",
        key = videoKey,
        site = site?.trim()?.ifBlank { "YouTube" } ?: "YouTube",
        type = type?.trim()?.ifBlank { "Trailer" } ?: "Trailer",
        isOfficial = official ?: false,
        publishedAt = publishedAt?.take(10).orEmpty()
    )
}

/**
 * Maps [VideosResponseDto] to a list of non-null domain [Video] entities.
 *
 * @receiver The nullable DTO container received from TMDB.
 * @return A non-null list of mapped [Video] entities.
 */
fun VideosResponseDto?.toDomainList(): List<Video> {
    return this?.results?.mapNotNull { it.toDomain() } ?: emptyList()
}

/**
 * Sanitizes and rounds a floating-point rating score to one decimal place.
 *
 * @param voteAverage The raw rating value received from TMDB.
 * @return The rating rounded to one decimal place, or `0.0` if null or negative.
 */
internal fun sanitizeVoteAverage(voteAverage: Double?): Double {
    if (voteAverage == null || voteAverage < 0.0) return 0.0
    return (voteAverage * 10.0).roundToInt() / 10.0
}

