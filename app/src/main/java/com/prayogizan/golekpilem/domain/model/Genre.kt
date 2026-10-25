package com.prayogizan.golekpilem.domain.model

/**
 * Domain entity representing a film or television genre classification.
 *
 * @property id The unique TMDB genre identifier.
 * @property name The localized name of the genre (e.g. "Action", "Drama").
 */
data class Genre(
    val id: Int,
    val name: String
)
