package com.prayogizan.golekpilem.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.usecase.GetMoviesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel managing the business state and paginated media streams for the main feed screen.
 *
 * Implements Unidirectional Data Flow (UDF) and provides cached [PagingData] streams
 * for both Movies and TV Series, scoped to [viewModelScope].
 *
 * Dependency injection is governed strictly via Koin DSL in `PresentationModule`.
 *
 * @param getMoviesUseCase Interactor for streaming paginated popular movies.
 * @param getTvSeriesUseCase Interactor for streaming paginated popular TV series.
 */
class MainViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val getTvSeriesUseCase: GetTvSeriesUseCase
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
        }
    }
}
