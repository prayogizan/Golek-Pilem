package com.prayogizan.golekpilem.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.Network
import com.prayogizan.golekpilem.domain.model.ProductionCompany
import com.prayogizan.golekpilem.domain.model.Review
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import com.prayogizan.golekpilem.domain.model.Video
import com.prayogizan.golekpilem.ui.common.ErrorScreen
import com.prayogizan.golekpilem.ui.common.GenreChips
import com.prayogizan.golekpilem.ui.common.LoadingScreen
import com.prayogizan.golekpilem.ui.detail.components.CompanyNetworkSection
import com.prayogizan.golekpilem.ui.detail.components.DetailHeader
import com.prayogizan.golekpilem.ui.detail.components.DetailShimmerScreen
import com.prayogizan.golekpilem.ui.detail.components.DetailMetadata
import com.prayogizan.golekpilem.ui.detail.components.FullScreenTrailerPlayer
import com.prayogizan.golekpilem.ui.detail.components.InfoSection
import com.prayogizan.golekpilem.ui.detail.components.ReviewsSection
import com.prayogizan.golekpilem.ui.detail.components.TrailersSection
import com.prayogizan.golekpilem.ui.detail.components.toCreditEntities
import com.prayogizan.golekpilem.ui.detail.components.toNetworkCreditEntities
import com.prayogizan.golekpilem.ui.theme.GolekPilemTheme

/**
 * Stateless screen displaying comprehensive metadata for movies and television series.
 *
 * Renders an immersive hero backdrop, overlapping poster card, title and info row,
 * interactive genre chips, storyline overview, trailers/videos, and production credits.
 * Employs [TopAppBarDefaults.enterAlwaysScrollBehavior] to smoothly collapse the floating
 * back navigation button on downward scroll and immediately reveal it on upward scroll.
 *
 * @param uiState Current immutable UI state representing loading, content, or error.
 * @param onEvent Lambda callback for dispatching user actions ([DetailUiEvent]) to the ViewModel.
 * @param onBackClick Lambda callback invoked when user navigates back to the catalog feed.
 * @param modifier Optional modifier applied to the screen root container.
 * @param onVideoClick Optional lambda callback invoked when navigating externally to the official video. Defaults to YouTube intent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onEvent: (DetailUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onVideoClick: ((Video) -> Unit)? = null
) {
    var activeVideoId by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val handleNavigateToOfficial: (Video) -> Unit = onVideoClick ?: { video ->
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${video.key}"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
        try {
            context.startActivity(appIntent)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }

    val activePlayerVideo = remember(activeVideoId, uiState) {
        if (activeVideoId == null) null
        else {
            when (uiState) {
                is DetailUiState.MovieSuccess -> uiState.detail.videos.firstOrNull { it.id == activeVideoId }
                is DetailUiState.TvSuccess -> uiState.detail.videos.firstOrNull { it.id == activeVideoId }
                else -> null
            }
        }
    }

    // Scroll behavior that collapses back button on scroll down and reveals it immediately on scroll up
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        when (uiState) {
            is DetailUiState.Loading -> {
                DetailShimmerScreen(
                    onBackClick = onBackClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is DetailUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    val resolvedTitle = uiState.titleRes?.let { stringResource(it) }
                        ?: stringResource(R.string.error_default_title)
                    val resolvedMessage = uiState.messageRes?.let { stringResource(it) }
                        ?: uiState.message.ifBlank { stringResource(R.string.error_default_message) }
                    ErrorScreen(
                        title = resolvedTitle,
                        message = resolvedMessage,
                        isNetworkError = uiState.isNetworkError,
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
                    onPlayInApp = { activeVideoId = it.id },
                    onNavigateToOfficial = handleNavigateToOfficial,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is DetailUiState.TvSuccess -> {
                TvSeriesDetailContent(
                    detail = uiState.detail,
                    onPlayInApp = { activeVideoId = it.id },
                    onNavigateToOfficial = handleNavigateToOfficial,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Floating Enter-Always Back Navigation Bar
        if (uiState is DetailUiState.MovieSuccess || uiState is DetailUiState.TvSuccess) {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(48.dp)
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            val isOfflineCache = when (uiState) {
                is DetailUiState.MovieSuccess -> uiState.isOfflineCache
                is DetailUiState.TvSuccess -> uiState.isOfflineCache
                else -> false
            }

            AnimatedVisibility(
                visible = isOfflineCache,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 64.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f),
                    tonalElevation = 4.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = stringResource(R.string.cd_offline_cached_badge),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.detail_offline_cache_badge),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Fullscreen In-App Landscape Player Overlay
        activePlayerVideo?.let { video ->
            FullScreenTrailerPlayer(
                video = video,
                onDismiss = { activeVideoId = null },
                onNavigateToOfficial = handleNavigateToOfficial
            )
        }
    }
}

@Composable
private fun MovieDetailContent(
    detail: MovieDetail,
    onPlayInApp: (Video) -> Unit,
    onNavigateToOfficial: (Video) -> Unit,
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
            posterUrl = detail.posterUrl
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

        if (detail.videos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            TrailersSection(
                videos = detail.videos,
                onNavigateToOfficial = onNavigateToOfficial,
                onOpenFullscreen = onPlayInApp
            )
        }

        if (detail.productionCompanies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            CompanyNetworkSection(
                title = stringResource(R.string.detail_production_companies_title),
                items = detail.productionCompanies.toCreditEntities()
            )
        }

        if (detail.reviews.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            ReviewsSection(
                reviews = detail.reviews,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TvSeriesDetailContent(
    detail: TvSeriesDetail,
    onPlayInApp: (Video) -> Unit,
    onNavigateToOfficial: (Video) -> Unit,
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
            posterUrl = detail.posterUrl
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

        if (detail.videos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            TrailersSection(
                videos = detail.videos,
                onNavigateToOfficial = onNavigateToOfficial,
                onOpenFullscreen = onPlayInApp
            )
        }

        if (detail.networks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            CompanyNetworkSection(
                title = stringResource(R.string.detail_networks_title),
                items = detail.networks.toNetworkCreditEntities()
            )
        }

        if (detail.reviews.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            ReviewsSection(
                reviews = detail.reviews,
                modifier = Modifier.padding(horizontal = 16.dp)
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
    status = "Released",
    reviews = listOf(
        Review(
            id = "rev_m1",
            author = "CinemaCritique",
            content = "Deadpool & Wolverine delivers non-stop visceral comedy and glorious action sequences. The dynamic between Reynolds and Jackman elevates the entire cinematic journey.",
            rating = 8.5,
            avatarUrl = null,
            createdAt = "2024-07-25"
        )
    ),
    videos = listOf(
        Video(
            id = "vid_m1",
            name = "Official Trailer",
            key = "73_1biulkYk",
            site = "YouTube",
            type = "Trailer",
            isOfficial = true,
            publishedAt = "2024-04-22"
        ),
        Video(
            id = "vid_m2",
            name = "Official Teaser",
            key = "uJMCNJP2ipI",
            site = "YouTube",
            type = "Teaser",
            isOfficial = true,
            publishedAt = "2024-02-11"
        )
    )
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
    status = "Returning Series",
    reviews = listOf(
        Review(
            id = "rev_tv1",
            author = "SciFiExplorer",
            content = "A masterclass in 80s nostalgia, suspenseful writing, and extraordinary character development. Season four raised the stakes to phenomenal heights.",
            rating = 9.2,
            avatarUrl = null,
            createdAt = "2022-07-05"
        )
    ),
    videos = listOf(
        Video(
            id = "vid_tv1",
            name = "Stranger Things 4 | Official Trailer",
            key = "yQEondeGvKo",
            site = "YouTube",
            type = "Trailer",
            isOfficial = true,
            publishedAt = "2022-04-12"
        )
    )
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

@Preview(name = "DetailScreen Movie Offline Cached - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailScreenMovieOfflineDarkPreview() {
    GolekPilemTheme(darkTheme = true) {
        Surface {
            DetailScreen(
                uiState = DetailUiState.MovieSuccess(sampleMovieDetail.copy(isOfflineCache = true)),
                onEvent = {},
                onBackClick = {}
            )
        }
    }
}
