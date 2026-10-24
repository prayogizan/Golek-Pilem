package com.prayogizan.golekpilem.ui.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.Network
import com.prayogizan.golekpilem.domain.model.ProductionCompany
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import com.prayogizan.golekpilem.ui.common.ErrorScreen
import com.prayogizan.golekpilem.ui.common.GenreChips
import com.prayogizan.golekpilem.ui.common.LoadingScreen
import com.prayogizan.golekpilem.ui.detail.components.CompanyNetworkSection
import com.prayogizan.golekpilem.ui.detail.components.DetailHeader
import com.prayogizan.golekpilem.ui.detail.components.DetailMetadata
import com.prayogizan.golekpilem.ui.detail.components.InfoSection
import com.prayogizan.golekpilem.ui.detail.components.toCreditEntities
import com.prayogizan.golekpilem.ui.detail.components.toNetworkCreditEntities
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Stateless screen displaying comprehensive metadata for movies and television series.
 *
 * Renders an immersive hero backdrop, overlapping poster card, title and info row,
 * interactive genre chips, storyline overview, and production credits.
 *
 * @param uiState Current immutable UI state representing loading, content, or error.
 * @param onEvent Lambda callback for dispatching user actions ([DetailUiEvent]) to the ViewModel.
 * @param onBackClick Lambda callback invoked when user navigates back to the catalog feed.
 * @param modifier Optional modifier applied to the screen root container.
 */
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onEvent: (DetailUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is DetailUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize()) {
                LoadingScreen()
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .size(48.dp)
                        .align(Alignment.TopStart)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        is DetailUiState.Error -> {
            Box(modifier = modifier.fillMaxSize()) {
                ErrorScreen(
                    message = uiState.message,
                    onRetry = { onEvent(DetailUiEvent.Retry) }
                )
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .size(48.dp)
                        .align(Alignment.TopStart)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        is DetailUiState.MovieSuccess -> {
            MovieDetailContent(
                detail = uiState.detail,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
        is DetailUiState.TvSuccess -> {
            TvSeriesDetailContent(
                detail = uiState.detail,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun MovieDetailContent(
    detail: MovieDetail,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        DetailHeader(
            title = detail.title,
            backdropUrl = detail.backdropUrl,
            posterUrl = detail.posterUrl,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailMetadata(
            title = detail.title,
            tagline = detail.tagline,
            releaseDate = detail.releaseDate,
            status = detail.status,
            voteAverage = detail.voteAverage,
            voteCount = detail.voteCount,
            runtime = detail.runtime,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (detail.genres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            GenreChips(
                genres = detail.genres,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        InfoSection(
            title = stringResource(R.string.detail_overview_title),
            content = detail.overview,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (detail.productionCompanies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            CompanyNetworkSection(
                title = stringResource(R.string.detail_production_companies_title),
                items = detail.productionCompanies.toCreditEntities()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TvSeriesDetailContent(
    detail: TvSeriesDetail,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        DetailHeader(
            title = detail.name,
            backdropUrl = detail.backdropUrl,
            posterUrl = detail.posterUrl,
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailMetadata(
            title = detail.name,
            tagline = detail.tagline,
            releaseDate = detail.firstAirDate,
            status = detail.status,
            voteAverage = detail.voteAverage,
            voteCount = detail.voteCount,
            numberOfSeasons = detail.numberOfSeasons,
            numberOfEpisodes = detail.numberOfEpisodes,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (detail.genres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            GenreChips(
                genres = detail.genres,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        InfoSection(
            title = stringResource(R.string.detail_overview_title),
            content = detail.overview,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (detail.networks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            CompanyNetworkSection(
                title = stringResource(R.string.detail_networks_title),
                items = detail.networks.toNetworkCreditEntities()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// -------------------------------------------------------------------------
// Previews
// -------------------------------------------------------------------------

private val sampleMovieDetail = MovieDetail(
    id = 533535,
    title = "Deadpool & Wolverine",
    tagline = "A listless Wade Wilson toils away in civilian life.",
    overview = "A listless Wade Wilson toils away in civilian life with his days as the morally flexible mercenary, Deadpool, behind him. But when his homeworld faces an existential threat, Wade must reluctantly suit-up again with an even more reluctant Wolverine.",
    posterUrl = null,
    backdropUrl = null,
    releaseDate = "2024-07-24",
    runtime = 128,
    voteAverage = 7.7,
    voteCount = 5120,
    genres = listOf(
        Genre(id = 28, name = "Action"),
        Genre(id = 35, name = "Comedy"),
        Genre(id = 878, name = "Science Fiction")
    ),
    productionCompanies = listOf(
        ProductionCompany(id = 420, name = "Marvel Studios", logoUrl = null),
        ProductionCompany(id = 104, name = "21 Laps Entertainment", logoUrl = null)
    ),
    status = "Released"
)

private val sampleTvDetail = TvSeriesDetail(
    id = 66732,
    name = "Stranger Things",
    tagline = "Every ending has a beginning.",
    overview = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
    posterUrl = null,
    backdropUrl = null,
    firstAirDate = "2016-07-15",
    lastAirDate = "2022-07-01",
    numberOfSeasons = 4,
    numberOfEpisodes = 34,
    voteAverage = 8.6,
    voteCount = 16400,
    genres = listOf(
        Genre(id = 18, name = "Drama"),
        Genre(id = 10765, name = "Sci-Fi & Fantasy"),
        Genre(id = 9648, name = "Mystery")
    ),
    networks = listOf(
        Network(id = 213, name = "Netflix", logoUrl = null)
    ),
    status = "Returning Series"
)

@Preview(name = "DetailScreen Movie - Light Theme", showBackground = true)
@Composable
private fun DetailScreenMovieLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            DetailScreen(
                uiState = DetailUiState.MovieSuccess(sampleMovieDetail),
                onEvent = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "DetailScreen Movie - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailScreenMovieDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            DetailScreen(
                uiState = DetailUiState.MovieSuccess(sampleMovieDetail),
                onEvent = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "DetailScreen TV - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailScreenTvDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            DetailScreen(
                uiState = DetailUiState.TvSuccess(sampleTvDetail),
                onEvent = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "DetailScreen Loading - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailScreenLoadingDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            DetailScreen(
                uiState = DetailUiState.Loading,
                onEvent = {},
                onBackClick = {}
            )
        }
    }
}

@Preview(name = "DetailScreen Error - Light Theme", showBackground = true)
@Composable
private fun DetailScreenErrorLightPreview() {
    GolekPilemTheme(darkTheme = false) {
        Surface {
            DetailScreen(
                uiState = DetailUiState.Error("Network connection lost. Please check connection."),
                onEvent = {},
                onBackClick = {}
            )
        }
    }
}
