package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object representing the nested video results container returned by TMDB's
 * `append_to_response=videos` sub-resource.
 *
 * @property results Collection of [VideoDto] entries.
 */
@Serializable
data class VideosResponseDto(
    @SerialName("results")
    val results: List<VideoDto>? = null
)

/**
 * Data Transfer Object representing an individual video entry returned by the TMDB API.
 *
 * @property id Unique identifier of the video asset.
 * @property name Human-readable title of the video.
 * @property key Platform-specific identifier (such as a YouTube video key).
 * @property site Hosting provider site (e.g. "YouTube").
 * @property size Video resolution quality indicator (e.g. 1080, 720).
 * @property type Content classification (e.g. "Trailer", "Teaser", "Clip", "Featurette").
 * @property official Whether the video is an official release.
 * @property publishedAt ISO timestamp string when the video was uploaded.
 */
@Serializable
data class VideoDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("key")
    val key: String? = null,
    @SerialName("site")
    val site: String? = null,
    @SerialName("size")
    val size: Int? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("official")
    val official: Boolean? = null,
    @SerialName("published_at")
    val publishedAt: String? = null
)
