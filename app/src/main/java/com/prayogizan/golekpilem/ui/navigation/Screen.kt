package com.prayogizan.golekpilem.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe destination routes for Compose Navigation in Golek Pilem.
 *
 * Utilizes Kotlinx Serialization to guarantee compile-time argument verification
 * and eliminate fragile runtime bundle/string route parsing.
 */
@Serializable
sealed interface Screen {

    /**
     * Root destination representing the main dual-tab catalog feed (Movies and TV Series).
     */
    @Serializable
    data object Main : Screen

    /**
     * Detail destination displaying comprehensive metadata for a specific film or series.
     *
     * @property id The unique TMDB identifier for the media item.
     * @property mediaType The classification of the media, strictly `"movie"` or `"tv"`.
     */
    @Serializable
    data class Detail(
        val id: Int,
        val mediaType: String
    ) : Screen
}
