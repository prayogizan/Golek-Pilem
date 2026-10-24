package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing a television broadcast/streaming network from TMDB.
 *
 * @property id The unique identifier for the network.
 * @property name The official network name.
 * @property logoPath Relative path to the network logo on TMDB CDN, or `null` if absent.
 */
@Serializable
data class NetworkDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = "",
    @SerialName("logo_path") val logoPath: String? = null
)
