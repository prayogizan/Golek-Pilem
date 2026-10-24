package com.prayogizan.golekpilem.domain.model

/**
 * Domain entity representing a production company associated with a film.
 *
 * @property id The unique TMDB identifier for the production company.
 * @property name The official name of the company.
 * @property logoUrl Complete TMDB CDN URL for the company's logo artwork (`w185`), or `null` if unavailable.
 */
data class ProductionCompany(
    val id: Int,
    val name: String,
    val logoUrl: String?
)
