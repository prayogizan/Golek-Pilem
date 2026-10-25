package com.prayogizan.golekpilem.domain.di

import com.prayogizan.golekpilem.domain.usecase.GetMovieDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetMoviesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesUseCase
import org.koin.dsl.module

/**
 * Domain layer dependency injection module providing clean architecture use cases.
 *
 * Configures stateless factory definitions for:
 * - [GetMoviesUseCase]: Interactor for streaming paginated popular movies.
 * - [GetTvSeriesUseCase]: Interactor for streaming paginated popular TV series.
 * - [GetMovieDetailUseCase]: Interactor for retrieving movie details.
 * - [GetTvSeriesDetailUseCase]: Interactor for retrieving TV series details.
 */
val domainModule = module {
    factory { GetMoviesUseCase(movieRepository = get()) }
    factory { GetTvSeriesUseCase(tvRepository = get()) }
    factory { GetMovieDetailUseCase(movieRepository = get()) }
    factory { GetTvSeriesDetailUseCase(tvRepository = get()) }
}
