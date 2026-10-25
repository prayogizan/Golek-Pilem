package com.prayogizan.golekpilem.domain.model

/**
 * Domain model representing a promotional or supplementary video asset (such as an official trailer,
 * teaser, or clip) associated with a movie or television series.
 *
 * Encapsulates the video's metadata and exposes convenience properties for YouTube streaming URLs
 * and CDN thumbnail endpoints without introducing framework dependencies.
 *
 * @property id Unique identifier of the video.
 * @property name Human-readable title or description of the video.
 * @property key Platform-specific video identifier (e.g. YouTube video ID).
 * @property site Video hosting provider platform (e.g., "YouTube").
 * @property type Categorical classification (e.g., "Trailer", "Teaser", "Featurette", "Clip").
 * @property isOfficial Whether the video is officially sanctioned by the studio or network.
 * @property publishedAt Publication timestamp or date string (e.g. `YYYY-MM-DD`).
 */
data class Video(
    val id: String,
    val name: String,
    val key: String,
    val site: String,
    val type: String,
    val isOfficial: Boolean = false,
    val publishedAt: String = ""
) {
    /**
     * Indicates whether the video asset is hosted on YouTube.
     */
    val isYouTube: Boolean
        get() = site.equals("YouTube", ignoreCase = true)

    /**
     * Generates the canonical web URL for streaming the video on YouTube.
     */
    val youtubeUrl: String
        get() = "https://www.youtube.com/watch?v=$key"

    /**
     * Resolves the high-quality thumbnail image URL from YouTube's image CDN.
     */
    val thumbnailUrl: String
        get() = "https://img.youtube.com/vi/$key/hqdefault.jpg"
}
