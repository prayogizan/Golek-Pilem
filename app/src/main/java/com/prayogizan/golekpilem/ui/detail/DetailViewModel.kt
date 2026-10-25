package com.prayogizan.golekpilem.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.core.util.ErrorMessageResolver
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.data.mapper.toMediaItem
import com.prayogizan.golekpilem.domain.usecase.GetMovieDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.IsWatchlistedUseCase
import com.prayogizan.golekpilem.domain.usecase.ToggleWatchlistUseCase
import com.prayogizan.golekpilem.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation ViewModel managing data retrieval, bookmark status, and lifecycle state for the media detail view.
 *
 * Resolves destination arguments (`id` and `mediaType`) via [SavedStateHandle] and orchestrates
 * invocations of [GetMovieDetailUseCase], [GetTvSeriesDetailUseCase], [IsWatchlistedUseCase],
 * and [ToggleWatchlistUseCase].
 *
 * @param savedStateHandle Key-value store persisting navigation route arguments across process death.
 * @param getMovieDetailUseCase Domain interactor for fetching detailed movie metadata.
 * @param getTvSeriesDetailUseCase Domain interactor for fetching detailed TV series metadata.
 * @param isWatchlistedUseCase Domain interactor for observing title bookmark presence in local storage.
 * @param toggleWatchlistUseCase Domain interactor for atomically toggling watchlist bookmark state.
 */
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val getTvSeriesDetailUseCase: GetTvSeriesDetailUseCase,
    private val isWatchlistedUseCase: IsWatchlistedUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase
) : ViewModel() {

    private val mediaId: Int
    private val mediaType: String

    init {
        val (parsedId, parsedType) = try {
            val route = savedStateHandle.toRoute<Screen.Detail>()
            route.id to route.mediaType
        } catch (_: Exception) {
            val id = savedStateHandle.get<Int>("id") ?: 0
            val type = savedStateHandle.get<String>("mediaType").orEmpty()
            id to type
        }
        mediaId = parsedId
        mediaType = parsedType
    }

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        observeWatchlistStatus()
        loadDetail()
    }

    /**
     * Centralized event handler receiving user intents from the UI layer.
     *
     * @param event The [DetailUiEvent] dispatched by the composable screen.
     */
    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.Retry -> loadDetail()
            is DetailUiEvent.ToggleWatchlist -> toggleWatchlist()
            is DetailUiEvent.ClearUserMessage -> clearUserMessage()
        }
    }

    /**
     * Retries loading media details. Provided for direct caller ergonomics and explicit contract compliance.
     */
    fun retry() {
        loadDetail()
    }

    private var isWatchlistedCache = false

    private fun observeWatchlistStatus() {
        if (mediaId <= 0) return
        viewModelScope.launch {
            isWatchlistedUseCase(mediaId, mediaType).collect { isSaved ->
                isWatchlistedCache = isSaved
                _uiState.update { currentState ->
                    when (currentState) {
                        is DetailUiState.MovieSuccess -> currentState.copy(isWatchlisted = isSaved)
                        is DetailUiState.TvSuccess -> currentState.copy(isWatchlisted = isSaved)
                        else -> currentState
                    }
                }
            }
        }
    }

    private fun toggleWatchlist() {
        val currentState = _uiState.value
        val mediaItem = when (currentState) {
            is DetailUiState.MovieSuccess -> currentState.detail.toMediaItem()
            is DetailUiState.TvSuccess -> currentState.detail.toMediaItem()
            else -> null
        } ?: return

        viewModelScope.launch {
            val isNowSaved = toggleWatchlistUseCase(mediaItem)
            val messageRes = if (isNowSaved) {
                R.string.msg_added_to_watchlist
            } else {
                R.string.msg_removed_from_watchlist
            }
            _uiState.update { state ->
                when (state) {
                    is DetailUiState.MovieSuccess -> state.copy(userMessageRes = messageRes)
                    is DetailUiState.TvSuccess -> state.copy(userMessageRes = messageRes)
                    else -> state
                }
            }
        }
    }

    private fun clearUserMessage() {
        _uiState.update { state ->
            when (state) {
                is DetailUiState.MovieSuccess -> state.copy(userMessageRes = null)
                is DetailUiState.TvSuccess -> state.copy(userMessageRes = null)
                else -> state
            }
        }
    }

    private fun loadDetail() {
        if (mediaId <= 0) {
            _uiState.value = DetailUiState.Error("Invalid or missing media identifier: $mediaId")
            return
        }

        _uiState.value = DetailUiState.Loading

        viewModelScope.launch {
            val currentIsWatchlisted = when (val s = _uiState.value) {
                is DetailUiState.MovieSuccess -> s.isWatchlisted
                is DetailUiState.TvSuccess -> s.isWatchlisted
                else -> isWatchlistedCache
            }

            when (mediaType.lowercase()) {
                "movie" -> {
                    getMovieDetailUseCase(mediaId).collect { resource ->
                        _uiState.value = when (resource) {
                            is Resource.Loading -> DetailUiState.Loading
                            is Resource.Success -> DetailUiState.MovieSuccess(
                                detail = resource.data,
                                isWatchlisted = currentIsWatchlisted
                            )
                            is Resource.Error -> {
                                val errorType = ErrorMessageResolver.resolve(resource.cause)
                                DetailUiState.Error(
                                    message = resource.message.ifBlank { "Unable to load movie details" },
                                    titleRes = errorType.titleRes,
                                    messageRes = errorType.messageRes,
                                    isNetworkError = errorType.isNetworkIssue
                                )
                            }
                        }
                    }
                }
                "tv" -> {
                    getTvSeriesDetailUseCase(mediaId).collect { resource ->
                        _uiState.value = when (resource) {
                            is Resource.Loading -> DetailUiState.Loading
                            is Resource.Success -> DetailUiState.TvSuccess(
                                detail = resource.data,
                                isWatchlisted = currentIsWatchlisted
                            )
                            is Resource.Error -> {
                                val errorType = ErrorMessageResolver.resolve(resource.cause)
                                DetailUiState.Error(
                                    message = resource.message.ifBlank { "Unable to load TV series details" },
                                    titleRes = errorType.titleRes,
                                    messageRes = errorType.messageRes,
                                    isNetworkError = errorType.isNetworkIssue
                                )
                            }
                        }
                    }
                }
                else -> {
                    _uiState.value = DetailUiState.Error("Unsupported media type: $mediaType")
                }
            }
        }
    }
}
