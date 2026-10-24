package com.prayogizan.golekpilem.domain.model

/**
 * Domain entity representing a user review submitted for a movie or television series.
 *
 * @property id Unique identifier for the review.
 * @property author Display name or username of the reviewer.
 * @property content Full text content of the review critique.
 * @property rating Numerical rating score out of 10.0 assigned by the reviewer, or `null` if unrated.
 * @property avatarUrl Complete TMDB CDN or external Gravatar URL for the author's avatar, or `null`.
 * @property createdAt Date string representing when the review was published (`YYYY-MM-DD`).
 */
data class Review(
    val id: String,
    val author: String,
    val content: String,
    val rating: Double?,
    val avatarUrl: String?,
    val createdAt: String
)
