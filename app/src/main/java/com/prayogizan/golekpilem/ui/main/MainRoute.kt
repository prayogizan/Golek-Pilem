package com.prayogizan.golekpilem.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import org.koin.androidx.compose.koinViewModel

/**
 * Stateful route wrapper for the main screen catalog feed.
 *
 * Resolves [MainViewModel] via Koin dependency injection, collects reactive [PagingData]
 * and [MainUiState] with lifecycle safety, and forwards user actions and navigation lambdas
 * to the stateless [MainScreen].
 *
 * @param onItemClick Navigation callback triggered when a user taps a movie or TV series card.
 * @param modifier Optional modifier applied to the screen root container.
 * @param viewModel The presentation ViewModel injected via Koin.
 */
@Composable
fun MainRoute(
    onItemClick: (id: Int, mediaType: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val tvSeries = viewModel.tvSeries.collectAsLazyPagingItems()

    MainScreen(
        uiState = uiState,
        movies = movies,
        tvSeries = tvSeries,
        onEvent = viewModel::onEvent,
        onItemClick = onItemClick,
        modifier = modifier
    )
}
