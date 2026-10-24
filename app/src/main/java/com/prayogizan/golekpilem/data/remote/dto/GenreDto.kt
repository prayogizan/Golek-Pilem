package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing a TMDB genre entry.
 *
 * @property id The unique genre identifier assigned by TMDB.
 * @property name The display name of the genre category.
 */
@Serializable
data class GenreDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = ""
)
