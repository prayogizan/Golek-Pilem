package com.prayogizan.golekpilem.data.di

import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.data.repository.MovieRepositoryImpl
import com.prayogizan.golekpilem.data.repository.TvRepositoryImpl
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import com.prayogizan.golekpilem.domain.repository.TvRepository
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Data layer dependency injection module providing TMDB API services and repository implementations.
 *
 * Provides singletons for:
 * - [TmdbApiService]: Instantiated via Retrofit REST client adapter.
 * - [MovieRepository]: Concrete [MovieRepositoryImpl] managing movie paging and details.
 * - [TvRepository]: Concrete [TvRepositoryImpl] managing TV series paging and details.
 */
val dataModule = module {
    single<TmdbApiService> {
        get<Retrofit>().create(TmdbApiService::class.java)
    }

    single<MovieRepository> {
        MovieRepositoryImpl(apiService = get())
    }

    single<TvRepository> {
        TvRepositoryImpl(apiService = get())
    }
}

