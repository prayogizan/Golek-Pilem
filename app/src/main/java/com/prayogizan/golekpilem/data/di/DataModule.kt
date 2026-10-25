package com.prayogizan.golekpilem.data.di

import com.prayogizan.golekpilem.data.local.storage.StorageManager
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.data.repository.MovieRepositoryImpl
import com.prayogizan.golekpilem.data.repository.TvRepositoryImpl
import com.prayogizan.golekpilem.data.repository.WatchlistRepositoryImpl
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import com.prayogizan.golekpilem.domain.repository.StorageRepository
import com.prayogizan.golekpilem.domain.repository.TvRepository
import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Data layer dependency injection module providing TMDB API services and repository implementations.
 *
 * Provides singletons for:
 * - [TmdbApiService]: Instantiated via Retrofit REST client adapter.
 * - [MovieRepository]: Concrete [MovieRepositoryImpl] managing movie paging and details.
 * - [TvRepository]: Concrete [TvRepositoryImpl] managing TV series paging and details.
 * - [WatchlistRepository]: Concrete [WatchlistRepositoryImpl] managing user watchlists.
 * - [StorageRepository]: Concrete [StorageManager] managing storage usage and cache invalidation.
 */
val dataModule = module {
    single<TmdbApiService> {
        get<Retrofit>().create(TmdbApiService::class.java)
    }

    single<MovieRepository> {
        MovieRepositoryImpl(
            apiService = get(),
            database = get()
        )
    }

    single<TvRepository> {
        TvRepositoryImpl(
            apiService = get(),
            database = get()
        )
    }

    single<WatchlistRepository> {
        WatchlistRepositoryImpl(
            database = get()
        )
    }

    single<StorageRepository> {
        StorageManager(
            context = androidContext(),
            database = get(),
            imageLoader = get()
        )
    }
}


