package com.prayogizan.golekpilem.data.local.model

import com.prayogizan.golekpilem.domain.model.Review
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer model representing a user critique stored within local Room database JSON fields.
 *
 * Facilitates conversion between pure domain [Review] entities and persistent Room JSON strings
 * via Room TypeConverters without violating domain layer independence.
 *
 * @property id Unique identifier for the critique.
 * @property author Display name or username of the reviewer.
 * @property content Full critique body text.
 * @property rating Numerical score out of 10.0, or `null` if unrated.
 * @property avatarUrl Full CDN image URL for author avatar, or `null`.
 * @property createdAt Publication date string (`YYYY-MM-DD`).
 */
@Serializable
data class ReviewLocalModel(
    @SerialName("id") val id: String,
    @SerialName("author") val author: String,
    @SerialName("content") val content: String,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

/**
 * Maps this local storage review representation to a pure domain [Review] entity.
 *
 * @receiver The persistent local review model.
 * @return A clean domain [Review] instance.
 */
fun ReviewLocalModel.toDomain(): Review = Review(
    id = id,
    author = author,
    content = content,
    rating = rating,
    avatarUrl = avatarUrl,
    createdAt = createdAt
)

/**
 * Maps a pure domain [Review] entity to a serializable local [ReviewLocalModel].
 *
 * @receiver The domain [Review] instance.
 * @return A serializable [ReviewLocalModel] instance for Room persistence.
 */
fun Review.toLocal(): ReviewLocalModel = ReviewLocalModel(
    id = id,
    author = author,
    content = content,
    rating = rating,
    avatarUrl = avatarUrl,
    createdAt = createdAt
)
