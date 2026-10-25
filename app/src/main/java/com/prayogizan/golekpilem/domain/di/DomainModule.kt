package com.prayogizan.golekpilem.domain.di

import com.prayogizan.golekpilem.domain.usecase.ClearImageCacheUseCase
import com.prayogizan.golekpilem.domain.usecase.GetMovieDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetMoviesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetStorageStatsUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetWatchlistCountUseCase
import com.prayogizan.golekpilem.domain.usecase.GetWatchlistUseCase
import com.prayogizan.golekpilem.domain.usecase.IsWatchlistedUseCase
import com.prayogizan.golekpilem.domain.usecase.PurgeExpiredCatalogUseCase
import com.prayogizan.golekpilem.domain.usecase.ToggleWatchlistUseCase
import org.koin.dsl.module

/**
 * Domain layer dependency injection module providing clean architecture use cases.
 *
 * Configures stateless factory definitions for:
 * - [GetMoviesUseCase]: Interactor for streaming paginated popular movies.
 * - [GetTvSeriesUseCase]: Interactor for streaming paginated popular TV series.
 * - [GetMovieDetailUseCase]: Interactor for retrieving movie details.
 * - [GetTvSeriesDetailUseCase]: Interactor for retrieving TV series details.
 * - [GetWatchlistUseCase]: Interactor for streaming saved watchlist items.
 * - [IsWatchlistedUseCase]: Interactor for observing media bookmark status.
 * - [ToggleWatchlistUseCase]: Interactor for atomic bookmark addition/removal.
 * - [GetWatchlistCountUseCase]: Interactor for streaming total watchlist count.
 * - [GetStorageStatsUseCase]: Interactor for computing storage utilization metrics.
 * - [ClearImageCacheUseCase]: Interactor for purging Coil image disk/memory cache.
 * - [PurgeExpiredCatalogUseCase]: Interactor for pruning stale catalog records exempting watchlists.
 */
val domainModule = module {
    factory { GetMoviesUseCase(movieRepository = get()) }
    factory { GetTvSeriesUseCase(tvRepository = get()) }
    factory { GetMovieDetailUseCase(movieRepository = get()) }
    factory { GetTvSeriesDetailUseCase(tvRepository = get()) }
    factory { GetWatchlistUseCase(watchlistRepository = get()) }
    factory { IsWatchlistedUseCase(watchlistRepository = get()) }
    factory { ToggleWatchlistUseCase(watchlistRepository = get()) }
    factory { GetWatchlistCountUseCase(watchlistRepository = get()) }
    factory { GetStorageStatsUseCase(storageRepository = get()) }
    factory { ClearImageCacheUseCase(storageRepository = get()) }
    factory { PurgeExpiredCatalogUseCase(storageRepository = get()) }
}

