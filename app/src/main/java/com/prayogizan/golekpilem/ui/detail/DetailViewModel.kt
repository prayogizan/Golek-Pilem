package com.prayogizan.golekpilem.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.prayogizan.golekpilem.core.util.ErrorMessageResolver
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.usecase.GetMovieDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesDetailUseCase
import com.prayogizan.golekpilem.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Presentation ViewModel managing data retrieval and lifecycle state for the media detail view.
 *
 * Resolves destination arguments (`id` and `mediaType`) via [SavedStateHandle] and orchestrates
 * invocations of [GetMovieDetailUseCase] and [GetTvSeriesDetailUseCase].
 *
 * @param savedStateHandle Key-value store persisting navigation route arguments across process death.
 * @param getMovieDetailUseCase Domain interactor for fetching detailed movie metadata.
 * @param getTvSeriesDetailUseCase Domain interactor for fetching detailed TV series metadata.
 */
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val getTvSeriesDetailUseCase: GetTvSeriesDetailUseCase
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
        }
    }

    /**
     * Retries loading media details. Provided for direct caller ergonomics and explicit contract compliance.
     */
    fun retry() {
        loadDetail()
    }

    private fun loadDetail() {
        if (mediaId <= 0) {
            _uiState.value = DetailUiState.Error("Invalid or missing media identifier: $mediaId")
            return
        }

        _uiState.value = DetailUiState.Loading

        viewModelScope.launch {
            when (mediaType.lowercase()) {
                "movie" -> {
                    getMovieDetailUseCase(mediaId).collect { resource ->
                        _uiState.value = when (resource) {
                            is Resource.Loading -> DetailUiState.Loading
                            is Resource.Success -> DetailUiState.MovieSuccess(resource.data)
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
                            is Resource.Success -> DetailUiState.TvSuccess(resource.data)
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
