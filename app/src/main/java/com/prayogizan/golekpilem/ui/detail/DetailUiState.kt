package com.prayogizan.golekpilem.ui.detail

import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail

/**
 * Immutable UI state model for the Detail screen adhering to the LCE (Loading-Content-Error) pattern.
 */
sealed interface DetailUiState {

    /**
     * Indicates that media details are currently being fetched from the remote data source.
     */
    data object Loading : DetailUiState

    /**
     * Successfully loaded detailed metadata for a movie entity.
     *
     * @property detail The comprehensive domain [MovieDetail] model.
     * @property isWatchlisted Indicates whether this movie is present in the user's watchlist.
     * @property userMessageRes Optional string resource ID for transient snackbar notifications.
     * @property isRefreshing Indicates whether a background pull-to-refresh revalidation is currently active.
     * @property isOfflineCache Indicates whether this record is served from offline Room storage.
     */
    data class MovieSuccess(
        val detail: MovieDetail,
        val isWatchlisted: Boolean = false,
        val userMessageRes: Int? = null,
        val isRefreshing: Boolean = false
    ) : DetailUiState {
        val isOfflineCache: Boolean get() = detail.isOfflineCache
    }

    /**
     * Successfully loaded detailed metadata for a television series entity.
     *
     * @property detail The comprehensive domain [TvSeriesDetail] model.
     * @property isWatchlisted Indicates whether this TV series is present in the user's watchlist.
     * @property userMessageRes Optional string resource ID for transient snackbar notifications.
     * @property isRefreshing Indicates whether a background pull-to-refresh revalidation is currently active.
     * @property isOfflineCache Indicates whether this record is served from offline Room storage.
     */
    data class TvSuccess(
        val detail: TvSeriesDetail,
        val isWatchlisted: Boolean = false,
        val userMessageRes: Int? = null,
        val isRefreshing: Boolean = false
    ) : DetailUiState {
        val isOfflineCache: Boolean get() = detail.isOfflineCache
    }

    /**
     * Terminal or recoverable error encountered during media detail retrieval.
     *
     * @property message A human-readable error description or reason.
     * @property titleRes Optional string resource ID representing the error heading.
     * @property messageRes Optional string resource ID representing the descriptive error text.
     * @property isNetworkError Indicates whether the error was caused by connection drop or network unavailability.
     */
    data class Error(
        val message: String,
        val titleRes: Int? = null,
        val messageRes: Int? = null,
        val isNetworkError: Boolean = false
    ) : DetailUiState
}
