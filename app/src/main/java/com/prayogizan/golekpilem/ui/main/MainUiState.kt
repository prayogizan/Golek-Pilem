package com.prayogizan.golekpilem.ui.main

import com.prayogizan.golekpilem.domain.model.MediaItem

/**
 * Immutable UI state model for the main catalog feed screen.
 *
 * Adheres strictly to the Unidirectional Data Flow (UDF) pattern. In accordance with
 * Paging 3 architecture, the paginated data streams and load states for movies and TV series
 * are streamed reactively and consumed via [androidx.paging.compose.LazyPagingItems], while
 * [MainUiState] maintains active tab selection, personal watchlist items, and the reactive badge count.
 *
 * @property selectedTabIndex Currently active tab (0 = Movies, 1 = TV Series, 2 = Watchlist).
 * @property watchlistItems Current list of saved titles in the user's watchlist.
 * @property watchlistCount Total count of titles saved in the watchlist driving the reactive tab badge.
 * @property isWatchlistLoading Indicates whether the initial watchlist query is in flight.
 */
data class MainUiState(
    val selectedTabIndex: Int = 0,
    val watchlistItems: List<MediaItem> = emptyList(),
    val watchlistCount: Int = 0,
    val isWatchlistLoading: Boolean = false
)

/**
 * Intent events dispatched from UI composables to [MainViewModel].
 */
sealed interface MainUiEvent {

    /**
     * Dispatched when the user switches tabs between Movies, TV Series, and Watchlist.
     *
     * @property index Target tab index (0 = Movies, 1 = TV Series, 2 = Watchlist).
     */
    data class SelectTab(val index: Int) : MainUiEvent

    /**
     * Dispatched when the user triggers a retry operation for a specific tab.
     *
     * @property tabIndex Target tab index to retry.
     */
    data class Retry(val tabIndex: Int) : MainUiEvent
}
