package com.prayogizan.golekpilem.domain.model

/**
 * Domain entity representing a broadcast or streaming network producing a television series.
 *
 * @property id The unique TMDB identifier for the network.
 * @property name The official network name (e.g. "HBO", "Netflix").
 * @property logoUrl Complete TMDB CDN URL for the network logo artwork (`w185`), or `null` if unavailable.
 */
data class Network(
    val id: Int,
    val name: String,
    val logoUrl: String?
)
