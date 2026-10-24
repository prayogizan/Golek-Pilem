package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object encapsulating the paginated reviews sub-resource returned by TMDB.
 *
 * @property page The active page number.
 * @property results Collection of [ReviewDto] items.
 * @property totalPages Total available pages of reviews.
 * @property totalResults Aggregate review count.
 */
@Serializable
data class ReviewsResponseDto(
    @SerialName("page") val page: Int = 1,
    @SerialName("results") val results: List<ReviewDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0
)

/**
 * Data transfer object representing a single review item.
 *
 * @property id Unique string identifier of the review.
 * @property author Author display name.
 * @property authorDetails Detailed author metadata including username, avatar, and rating.
 * @property content Full critique body text.
 * @property createdAt Timestamp string when the review was submitted.
 * @property url External TMDB URL to view the review online.
 */
@Serializable
data class ReviewDto(
    @SerialName("id") val id: String,
    @SerialName("author") val author: String? = null,
    @SerialName("author_details") val authorDetails: AuthorDetailsDto? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("url") val url: String? = null
)

/**
 * Data transfer object representing detailed author metadata.
 *
 * @property name Author personal name.
 * @property username Author TMDB username.
 * @property avatarPath Relative path or external Gravatar URL to the reviewer's avatar.
 * @property rating Numerical score rating out of 10.0.
 */
@Serializable
data class AuthorDetailsDto(
    @SerialName("name") val name: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("rating") val rating: Double? = null
)
