package com.prayogizan.golekpilem.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * Stateful route wrapper for the media detail destination.
 *
 * Resolves [DetailViewModel] via Koin dependency injection, collects reactive [DetailUiState]
 * with lifecycle awareness, and delegates presentation and navigation callbacks to the stateless
 * [DetailScreen].
 *
 * @param onBackClick Navigation callback triggered when the user taps back.
 * @param modifier Optional modifier applied to the screen root container.
 * @param viewModel The presentation ViewModel injected via Koin.
 */
@Composable
fun DetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier
    )
}
