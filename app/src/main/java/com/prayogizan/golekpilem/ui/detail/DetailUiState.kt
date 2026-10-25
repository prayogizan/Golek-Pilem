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
     */
    data class MovieSuccess(val detail: MovieDetail) : DetailUiState

    /**
     * Successfully loaded detailed metadata for a television series entity.
     *
     * @property detail The comprehensive domain [TvSeriesDetail] model.
     */
    data class TvSuccess(val detail: TvSeriesDetail) : DetailUiState

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
