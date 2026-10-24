package com.prayogizan.golekpilem.ui.main

/**
 * Immutable UI state model for the main dual-tab catalog feed screen.
 *
 * Adheres strictly to the Unidirectional Data Flow (UDF) pattern. In accordance with
 * Paging 3 architecture, the paginated data streams and load states for movies and TV series
 * are streamed reactively and consumed via [androidx.paging.compose.LazyPagingItems], while
 * [MainUiState] maintains the active tab selection and screen-level UI states.
 *
 * @property selectedTabIndex Currently active tab (0 = Movies, 1 = TV Series).
 */
data class MainUiState(
    val selectedTabIndex: Int = 0
)

/**
 * Intent events dispatched from UI composables to [MainViewModel].
 */
sealed interface MainUiEvent {

    /**
     * Dispatched when the user switches tabs between Movies and TV Series.
     *
     * @property index Target tab index (0 = Movies, 1 = TV Series).
     */
    data class SelectTab(val index: Int) : MainUiEvent

    /**
     * Dispatched when the user triggers a retry operation for a specific tab.
     *
     * @property tabIndex Target tab index to retry.
     */
    data class Retry(val tabIndex: Int) : MainUiEvent
}
