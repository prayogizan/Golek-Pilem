package com.prayogizan.golekpilem.core.di

import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import com.prayogizan.golekpilem.domain.repository.TvRepository
import com.prayogizan.golekpilem.domain.usecase.GetMovieDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetMoviesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesUseCase
import com.prayogizan.golekpilem.ui.main.MainViewModel
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import retrofit2.Retrofit

class AppModuleTest : KoinTest {

    @Before
    fun setUp() {
        startKoin {
            modules(appModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify appModule resolves dependencies provided by included modules`() {
        val retrofit: Retrofit = get()
        assertNotNull("Retrofit should be resolved via appModule included networkModule", retrofit)

        val apiService: TmdbApiService = get()
        assertNotNull("TmdbApiService should be resolved via appModule included dataModule", apiService)

        val movieRepository: MovieRepository = get()
        assertNotNull("MovieRepository should be resolved via dataModule", movieRepository)

        val tvRepository: TvRepository = get()
        assertNotNull("TvRepository should be resolved via dataModule", tvRepository)

        val getMoviesUseCase: GetMoviesUseCase = get()
        assertNotNull("GetMoviesUseCase should be resolved via domainModule", getMoviesUseCase)

        val getTvSeriesUseCase: GetTvSeriesUseCase = get()
        assertNotNull("GetTvSeriesUseCase should be resolved via domainModule", getTvSeriesUseCase)

        val getMovieDetailUseCase: GetMovieDetailUseCase = get()
        assertNotNull("GetMovieDetailUseCase should be resolved via domainModule", getMovieDetailUseCase)

        val getTvSeriesDetailUseCase: GetTvSeriesDetailUseCase = get()
        assertNotNull("GetTvSeriesDetailUseCase should be resolved via domainModule", getTvSeriesDetailUseCase)

        val mainViewModel: MainViewModel = get()
        assertNotNull("MainViewModel should be resolved via presentationModule", mainViewModel)
    }
}
