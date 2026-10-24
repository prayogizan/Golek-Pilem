package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic wrapper DTO for paginated TMDB API responses.
 *
 * @param T The specific item payload type contained within the page results list.
 * @property page The 1-based page index returned by the remote TMDB service.
 * @property results The list of data transfer objects belonging to the current page.
 * @property totalPages Total number of pages available across the entire dataset.
 * @property totalResults Total number of discrete items available across all pages.
 */
@Serializable
data class PaginatedResponseDto<T>(
    @SerialName("page") val page: Int,
    @SerialName("results") val results: List<T> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0
)
