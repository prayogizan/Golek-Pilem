package com.prayogizan.golekpilem.ui.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.prayogizan.golekpilem.R
import kotlinx.coroutines.flow.flowOf
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.ui.common.ErrorScreen
import com.prayogizan.golekpilem.ui.main.components.EmptyFeedView
import com.prayogizan.golekpilem.ui.main.components.MediaGridCard
import com.prayogizan.golekpilem.ui.main.components.MediaGridCardPlaceholder
import com.prayogizan.golekpilem.ui.main.components.PaginationErrorItem
import com.prayogizan.golekpilem.ui.main.components.PaginationLoadingItem
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Stateless main discovery screen rendering a dual-tab feed ("Movies" and "TV Series").
 *
 * Employs an immersive, responsive 2-column [LazyVerticalStaggeredGrid] for mobile viewports,
 * preserves independent scroll state across tab switches, and renders skeleton shimmer loading
 * placeholders, error recovery screens, and full-width pagination indicators.
 *
 * @param uiState Current immutable UI state representing the selected tab.
 * @param movies Paginated collection of popular movies.
 * @param tvSeries Paginated collection of popular TV series.
 * @param onEvent Callback dispatching user events ([MainUiEvent]) to the ViewModel.
 * @param onItemClick Callback triggered when the user taps a movie or series card.
 * @param modifier Optional modifier applied to the root [Scaffold].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    movies: LazyPagingItems<Movie>,
    tvSeries: LazyPagingItems<TvSeries>,
    onEvent: (MainUiEvent) -> Unit,
    onItemClick: (id: Int, mediaType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Preserve independent scroll states across tab selections
    val moviesGridState = rememberSaveable(saver = LazyStaggeredGridState.Saver) { LazyStaggeredGridState() }
    val tvSeriesGridState = rememberSaveable(saver = LazyStaggeredGridState.Saver) { LazyStaggeredGridState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_main_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dual Tab Row (Movies & TV Series)
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                val moviesCd = stringResource(R.string.cd_movie_tab)
                val tvCd = stringResource(R.string.cd_tv_tab)

                Tab(
                    selected = uiState.selectedTabIndex == 0,
                    onClick = { onEvent(MainUiEvent.SelectTab(0)) },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_movies),
                            fontWeight = if (uiState.selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.semantics { contentDescription = moviesCd }
                )

                Tab(
                    selected = uiState.selectedTabIndex == 1,
                    onClick = { onEvent(MainUiEvent.SelectTab(1)) },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_tv_series),
                            fontWeight = if (uiState.selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.semantics { contentDescription = tvCd }
                )
            }

            // Tab Content with Independent Scroll States
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.selectedTabIndex == 0) {
                    MovieFeedGrid(
                        movies = movies,
                        gridState = moviesGridState,
                        onItemClick = onItemClick
                    )
                } else {
                    TvSeriesFeedGrid(
                        tvSeries = tvSeries,
                        gridState = tvSeriesGridState,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

/**
 * 2-column staggered grid rendering paginated movie cards with skeleton shimmer and error handling.
 */
@Composable
private fun MovieFeedGrid(
    movies: LazyPagingItems<Movie>,
    gridState: LazyStaggeredGridState,
    onItemClick: (id: Int, mediaType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val refreshState = movies.loadState.refresh

    when {
        refreshState is LoadState.Loading && movies.itemCount == 0 -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 16.dp,
                modifier = modifier.fillMaxSize()
            ) {
                items(count = 6) { index ->
                    val placeholderRatio = when (index % 3) {
                        0 -> 0.62f
                        1 -> 0.72f
                        else -> 2f / 3f
                    }
                    MediaGridCardPlaceholder(aspectRatio = placeholderRatio)
                }
            }
        }
        refreshState is LoadState.Error && movies.itemCount == 0 -> {
            val errorMessage = refreshState.error.localizedMessage
                ?: stringResource(R.string.error_default_message)
            ErrorScreen(
                message = errorMessage,
                onRetry = { movies.retry() },
                modifier = modifier.fillMaxSize()
            )
        }
        movies.itemCount == 0 && refreshState is LoadState.NotLoading -> {
            EmptyFeedView(
                onRefresh = { movies.refresh() },
                modifier = modifier.fillMaxSize()
            )
        }
        else -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 16.dp,
                modifier = modifier.fillMaxSize()
            ) {
                items(
                    count = movies.itemCount,
                    key = movies.itemKey { it.id }
                ) { index ->
                    val movie = movies[index]
                    if (movie != null) {
                        val itemAspectRatio = when (movie.id % 3) {
                            0 -> 0.62f
                            1 -> 0.72f
                            else -> 2f / 3f
                        }
                        MediaGridCard(
                            id = movie.id,
                            title = movie.title,
                            posterUrl = movie.posterUrl,
                            releaseYear = movie.releaseYear,
                            voteAverage = movie.voteAverage,
                            onClick = { onItemClick(movie.id, "movie") },
                            aspectRatio = itemAspectRatio
                        )
                    }
                }

                // Append loading & error handlers spanning full width
                when (val appendState = movies.loadState.append) {
                    is LoadState.Loading -> {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            PaginationLoadingItem()
                        }
                    }
                    is LoadState.Error -> {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            PaginationErrorItem(onRetry = { movies.retry() })
                        }
                    }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

/**
 * 2-column staggered grid rendering paginated TV series cards with skeleton shimmer and error handling.
 */
@Composable
private fun TvSeriesFeedGrid(
    tvSeries: LazyPagingItems<TvSeries>,
    gridState: LazyStaggeredGridState,
    onItemClick: (id: Int, mediaType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val refreshState = tvSeries.loadState.refresh

    when {
        refreshState is LoadState.Loading && tvSeries.itemCount == 0 -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 16.dp,
                modifier = modifier.fillMaxSize()
            ) {
                items(count = 6) { index ->
                    val placeholderRatio = when (index % 3) {
                        0 -> 0.62f
                        1 -> 0.72f
                        else -> 2f / 3f
                    }
                    MediaGridCardPlaceholder(aspectRatio = placeholderRatio)
                }
            }
        }
        refreshState is LoadState.Error && tvSeries.itemCount == 0 -> {
            val errorMessage = refreshState.error.localizedMessage
                ?: stringResource(R.string.error_default_message)
            ErrorScreen(
                message = errorMessage,
                onRetry = { tvSeries.retry() },
                modifier = modifier.fillMaxSize()
            )
        }
        tvSeries.itemCount == 0 && refreshState is LoadState.NotLoading -> {
            EmptyFeedView(
                onRefresh = { tvSeries.refresh() },
                modifier = modifier.fillMaxSize()
            )
        }
        else -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 16.dp,
                modifier = modifier.fillMaxSize()
            ) {
                items(
                    count = tvSeries.itemCount,
                    key = tvSeries.itemKey { it.id }
                ) { index ->
                    val series = tvSeries[index]
                    if (series != null) {
                        val itemAspectRatio = when (series.id % 3) {
                            0 -> 0.62f
                            1 -> 0.72f
                            else -> 2f / 3f
                        }
                        MediaGridCard(
                            id = series.id,
                            title = series.name,
                            posterUrl = series.posterUrl,
                            releaseYear = series.firstAirYear,
                            voteAverage = series.voteAverage,
                            onClick = { onItemClick(series.id, "tv") },
                            aspectRatio = itemAspectRatio
                        )
                    }
                }

                // Append loading & error handlers spanning full width
                when (val appendState = tvSeries.loadState.append) {
                    is LoadState.Loading -> {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            PaginationLoadingItem()
                        }
                    }
                    is LoadState.Error -> {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            PaginationErrorItem(onRetry = { tvSeries.retry() })
                        }
                    }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

// =============================================================================
// Comprehensive Compose Previews
// =============================================================================

private val previewSampleMovies = listOf(
    Movie(
        id = 1,
        title = "The Beekeeper",
        posterUrl = "https://image.tmdb.org/t/p/w185/A7EByudX0eOzlkQ2FIbogzyazm2.jpg",
        releaseYear = "2024",
        voteAverage = 7.4
    ),
    Movie(
        id = 2,
        title = "Dune: Part Two",
        posterUrl = "https://image.tmdb.org/t/p/w185/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg",
        releaseYear = "2024",
        voteAverage = 8.3
    ),
    Movie(
        id = 3,
        title = "Deadpool & Wolverine",
        posterUrl = "https://image.tmdb.org/t/p/w185/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
        releaseYear = "2024",
        voteAverage = 7.8
    ),
    Movie(
        id = 4,
        title = "Oppenheimer",
        posterUrl = "https://image.tmdb.org/t/p/w185/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
        releaseYear = "2023",
        voteAverage = 8.1
    ),
    Movie(
        id = 5,
        title = "Inside Out 2",
        posterUrl = "https://image.tmdb.org/t/p/w185/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
        releaseYear = "2024",
        voteAverage = 7.7
    ),
    Movie(
        id = 6,
        title = "Civil War",
        posterUrl = "https://image.tmdb.org/t/p/w185/sh7Rg8Er3tFcN9BpKIPOMvALgZd.jpg",
        releaseYear = "2024",
        voteAverage = 7.0
    )
)

private val previewSampleTvSeries = listOf(
    TvSeries(
        id = 101,
        name = "Stranger Things",
        posterUrl = "https://image.tmdb.org/t/p/w185/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
        firstAirYear = "2016",
        voteAverage = 8.6
    ),
    TvSeries(
        id = 102,
        name = "Breaking Bad",
        posterUrl = "https://image.tmdb.org/t/p/w185/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
        firstAirYear = "2008",
        voteAverage = 8.9
    ),
    TvSeries(
        id = 103,
        name = "The Last of Us",
        posterUrl = "https://image.tmdb.org/t/p/w185/uKvVjK1q12R4t0ToxsZwh8b5gD4.jpg",
        firstAirYear = "2023",
        voteAverage = 8.6
    ),
    TvSeries(
        id = 104,
        name = "Shogun",
        posterUrl = "https://image.tmdb.org/t/p/w185/7O4iVfOMQmdCSxhOg1WNzG1AgYT.jpg",
        firstAirYear = "2024",
        voteAverage = 8.5
    ),
    TvSeries(
        id = 105,
        name = "Severance",
        posterUrl = "https://image.tmdb.org/t/p/w185/b5yq2wA7fS0o94gD6b8X9Y8e0b6.jpg",
        firstAirYear = "2022",
        voteAverage = 8.4
    ),
    TvSeries(
        id = 106,
        name = "Fallout",
        posterUrl = "https://image.tmdb.org/t/p/w185/AnsSKR9LuK0T9bAILezUVjl3ZYZ.jpg",
        firstAirYear = "2024",
        voteAverage = 8.4
    )
)

@Preview(name = "MainScreen - Movies Feed Light", showBackground = true)
@Composable
private fun MainScreenMoviesFeedLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            MainScreen(
                uiState = MainUiState(selectedTabIndex = 0),
                movies = flowOf(PagingData.from(previewSampleMovies)).collectAsLazyPagingItems(),
                tvSeries = flowOf(PagingData.from(previewSampleTvSeries)).collectAsLazyPagingItems(),
                onEvent = {},
                onItemClick = { _, _ -> }
            )
        }
    }
}

@Preview(name = "MainScreen - Movies Feed Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScreenMoviesFeedDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            MainScreen(
                uiState = MainUiState(selectedTabIndex = 0),
                movies = flowOf(PagingData.from(previewSampleMovies)).collectAsLazyPagingItems(),
                tvSeries = flowOf(PagingData.from(previewSampleTvSeries)).collectAsLazyPagingItems(),
                onEvent = {},
                onItemClick = { _, _ -> }
            )
        }
    }
}

@Preview(name = "MainScreen - Loading Shimmer State", showBackground = true)
@Composable
private fun MainScreenLoadingPreview() {
    val loadingLoadStates = LoadStates(
        refresh = LoadState.Loading,
        prepend = LoadState.NotLoading(endOfPaginationReached = false),
        append = LoadState.NotLoading(endOfPaginationReached = false)
    )
    GolekPilemTheme(darkTheme = false) {
        Surface {
            MainScreen(
                uiState = MainUiState(selectedTabIndex = 0),
                movies = flowOf(PagingData.empty<Movie>(sourceLoadStates = loadingLoadStates)).collectAsLazyPagingItems(),
                tvSeries = flowOf(PagingData.empty<TvSeries>()).collectAsLazyPagingItems(),
                onEvent = {},
                onItemClick = { _, _ -> }
            )
        }
    }
}

@Preview(name = "MainScreen - Error State", showBackground = true)
@Composable
private fun MainScreenErrorPreview() {
    val errorLoadStates = LoadStates(
        refresh = LoadState.Error(Exception("Unable to load popular movies. Please check your network connection.")),
        prepend = LoadState.NotLoading(endOfPaginationReached = false),
        append = LoadState.NotLoading(endOfPaginationReached = false)
    )
    GolekPilemTheme(darkTheme = false) {
        Surface {
            MainScreen(
                uiState = MainUiState(selectedTabIndex = 0),
                movies = flowOf(PagingData.empty<Movie>(sourceLoadStates = errorLoadStates)).collectAsLazyPagingItems(),
                tvSeries = flowOf(PagingData.empty<TvSeries>()).collectAsLazyPagingItems(),
                onEvent = {},
                onItemClick = { _, _ -> }
            )
        }
    }
}

@Preview(name = "MainScreen - Empty Feed Light", showBackground = true)
@Composable
private fun MainScreenEmptyFeedLightPreview() {
    val emptyStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true)
    )
    GolekPilemTheme(darkTheme = false) {
        Surface {
            MainScreen(
                uiState = MainUiState(selectedTabIndex = 0),
                movies = flowOf(PagingData.empty<Movie>(sourceLoadStates = emptyStates)).collectAsLazyPagingItems(),
                tvSeries = flowOf(PagingData.empty<TvSeries>(sourceLoadStates = emptyStates)).collectAsLazyPagingItems(),
                onEvent = {},
                onItemClick = { _, _ -> }
            )
        }
    }
}

@Preview(name = "MainScreen - Empty Feed Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScreenEmptyFeedDarkPreview() {
    val emptyStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true)
    )
    GolekPilemTheme(darkTheme = true) {
        Surface {
            MainScreen(
                uiState = MainUiState(selectedTabIndex = 0),
                movies = flowOf(PagingData.empty<Movie>(sourceLoadStates = emptyStates)).collectAsLazyPagingItems(),
                tvSeries = flowOf(PagingData.empty<TvSeries>(sourceLoadStates = emptyStates)).collectAsLazyPagingItems(),
                onEvent = {},
                onItemClick = { _, _ -> }
            )
        }
    }
}


