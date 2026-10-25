package com.prayogizan.golekpilem.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.usecase.GetMoviesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetWatchlistCountUseCase
import com.prayogizan.golekpilem.domain.usecase.GetWatchlistUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing the business state, paginated catalog streams, and watchlist favorites for the main screen.
 *
 * Implements Unidirectional Data Flow (UDF), provides cached [PagingData] streams
 * for Movies and TV Series, and observes reactive Room watchlist streams.
 *
 * Dependency injection is governed strictly via Koin DSL in `PresentationModule`.
 *
 * @param getMoviesUseCase Interactor for streaming paginated popular movies.
 * @param getTvSeriesUseCase Interactor for streaming paginated popular TV series.
 * @param getWatchlistUseCase Interactor for streaming saved watchlist items.
 * @param getWatchlistCountUseCase Interactor for streaming total count of saved watchlist items.
 */
class MainViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val getTvSeriesUseCase: GetTvSeriesUseCase,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getWatchlistCountUseCase: GetWatchlistCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())

    /**
     * Immutable state flow exposing the current screen UI state.
     */
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /**
     * Cold stream emitting paginated popular movie items cached within [viewModelScope].
     */
    val movies: Flow<PagingData<Movie>> = getMoviesUseCase().cachedIn(viewModelScope)

    /**
     * Cold stream emitting paginated popular television series items cached within [viewModelScope].
     */
    val tvSeries: Flow<PagingData<TvSeries>> = getTvSeriesUseCase().cachedIn(viewModelScope)

    init {
        observeWatchlist()
        observeWatchlistCount()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWatchlistLoading = true) }
            getWatchlistUseCase().collect { items ->
                _uiState.update { it.copy(watchlistItems = items, isWatchlistLoading = false) }
            }
        }
    }

    private fun observeWatchlistCount() {
        viewModelScope.launch {
            getWatchlistCountUseCase().collect { count ->
                _uiState.update { it.copy(watchlistCount = count) }
            }
        }
    }

    /**
     * Dispatches user intent events from the UI layer to mutate state or trigger side effects.
     *
     * @param event The [MainUiEvent] dispatched from composables.
     */
    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.SelectTab -> {
                _uiState.update { it.copy(selectedTabIndex = event.index) }
            }
            is MainUiEvent.Retry -> {
                // Retry can be handled at the PagingData stream or trigger side-effects
            }
            is MainUiEvent.RefreshWatchlist -> {
                refreshWatchlist()
            }
        }
    }

    private fun refreshWatchlist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWatchlistRefreshing = true) }
            try {
                val items = getWatchlistUseCase().first()
                _uiState.update { it.copy(watchlistItems = items, isWatchlistRefreshing = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isWatchlistRefreshing = false) }
            }
        }
    }
}
