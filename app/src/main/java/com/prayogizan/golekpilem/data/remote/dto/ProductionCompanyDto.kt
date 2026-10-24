package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing a film production company from TMDB.
 *
 * @property id The unique identifier for the production company.
 * @property name The official name of the production entity.
 * @property logoPath Relative path to the company logo on TMDB CDN, or `null` if absent.
 */
@Serializable
data class ProductionCompanyDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = "",
    @SerialName("logo_path") val logoPath: String? = null
)
