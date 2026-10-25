package com.prayogizan.golekpilem.ui.detail

/**
 * User interactions and intents originating from the Detail screen dispatched to [DetailViewModel].
 */
sealed interface DetailUiEvent {

    /**
     * User action requesting a retry of the media detail retrieval after an error.
     */
    data object Retry : DetailUiEvent

    /**
     * User action requesting to toggle the watchlist bookmark state of the currently viewed title.
     */
    data object ToggleWatchlist : DetailUiEvent

    /**
     * Action dispatched when a user notification message (e.g. Snackbar) has been presented and should be cleared.
     */
    data object ClearUserMessage : DetailUiEvent
}
