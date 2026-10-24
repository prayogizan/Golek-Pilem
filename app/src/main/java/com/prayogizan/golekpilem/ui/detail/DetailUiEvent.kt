package com.prayogizan.golekpilem.ui.detail

/**
 * User interactions and intents originating from the Detail screen dispatched to [DetailViewModel].
 */
sealed interface DetailUiEvent {

    /**
     * User action requesting a retry of the media detail retrieval after an error.
     */
    data object Retry : DetailUiEvent
}
