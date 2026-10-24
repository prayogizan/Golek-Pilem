package com.prayogizan.golekpilem.data.di

import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Data layer dependency injection module providing TMDB API services and data infrastructure.
 *
 * Provides singletons for:
 * - [TmdbApiService]: Instantiated via Retrofit REST client adapter.
 */
val dataModule = module {
    single<TmdbApiService> {
        get<Retrofit>().create(TmdbApiService::class.java)
    }
}
