package com.prayogizan.golekpilem.core.constants

/**
 * Global constants and configuration endpoints for TMDB API interactions.
 */
object ApiConstants {
    /**
     * TMDB REST API v3 base URL.
     */
    const val BASE_URL = "https://api.themoviedb.org/3/"

    /**
     * TMDB image CDN base URL. Append resolution tag (e.g. `w185`, `w342`, `w780`) and relative path.
     */
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

    /**
     * Default network connect, read, and write timeout in seconds.
     */
    const val TIMEOUT_SECONDS = 15L
}
