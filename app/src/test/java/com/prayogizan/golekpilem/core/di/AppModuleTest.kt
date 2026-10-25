package com.prayogizan.golekpilem.core.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.prayogizan.golekpilem.core.connectivity.ConnectivityObserver
import com.prayogizan.golekpilem.core.network.RateLimitRetryInterceptor
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.MediaDetailDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.dao.WatchlistDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import com.prayogizan.golekpilem.domain.repository.TvRepository
import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import com.prayogizan.golekpilem.domain.usecase.GetMovieDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetMoviesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetWatchlistCountUseCase
import com.prayogizan.golekpilem.domain.usecase.GetWatchlistUseCase
import com.prayogizan.golekpilem.domain.usecase.IsWatchlistedUseCase
import com.prayogizan.golekpilem.domain.usecase.ToggleWatchlistUseCase
import com.prayogizan.golekpilem.ui.detail.DetailViewModel
import com.prayogizan.golekpilem.ui.main.MainViewModel
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import retrofit2.Retrofit

class AppModuleTest : KoinTest {

    private val mockContext: Context = mockk(relaxed = true)

    private val testDatabaseOverrides = module {
        single<GolekPilemDatabase> { mockk(relaxed = true) }
        single<WatchlistDao> { mockk(relaxed = true) }
        single<MediaDao> { mockk(relaxed = true) }
        single<RemoteKeyDao> { mockk(relaxed = true) }
        single<MediaDetailDao> { mockk(relaxed = true) }
    }

    @Before
    fun setUp() {
        startKoin {
            androidContext(mockContext)
            modules(appModule, testDatabaseOverrides)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify appModule resolves dependencies provided by included modules`() {
        val connectivityObserver: ConnectivityObserver = get()
        assertNotNull("ConnectivityObserver should be resolved via appModule included networkModule", connectivityObserver)

        val rateLimitRetryInterceptor: RateLimitRetryInterceptor = get()
        assertNotNull("RateLimitRetryInterceptor should be resolved via appModule included networkModule", rateLimitRetryInterceptor)

        val retrofit: Retrofit = get()
        assertNotNull("Retrofit should be resolved via appModule included networkModule", retrofit)

        val apiService: TmdbApiService = get()
        assertNotNull("TmdbApiService should be resolved via appModule included dataModule", apiService)

        val movieRepository: MovieRepository = get()
        assertNotNull("MovieRepository should be resolved via dataModule", movieRepository)

        val tvRepository: TvRepository = get()
        assertNotNull("TvRepository should be resolved via dataModule", tvRepository)

        val watchlistRepository: WatchlistRepository = get()
        assertNotNull("WatchlistRepository should be resolved via dataModule", watchlistRepository)

        val getMoviesUseCase: GetMoviesUseCase = get()
        assertNotNull("GetMoviesUseCase should be resolved via domainModule", getMoviesUseCase)

        val getTvSeriesUseCase: GetTvSeriesUseCase = get()
        assertNotNull("GetTvSeriesUseCase should be resolved via domainModule", getTvSeriesUseCase)

        val getMovieDetailUseCase: GetMovieDetailUseCase = get()
        assertNotNull("GetMovieDetailUseCase should be resolved via domainModule", getMovieDetailUseCase)

        val getTvSeriesDetailUseCase: GetTvSeriesDetailUseCase = get()
        assertNotNull("GetTvSeriesDetailUseCase should be resolved via domainModule", getTvSeriesDetailUseCase)

        val getWatchlistUseCase: GetWatchlistUseCase = get()
        assertNotNull("GetWatchlistUseCase should be resolved via domainModule", getWatchlistUseCase)

        val isWatchlistedUseCase: IsWatchlistedUseCase = get()
        assertNotNull("IsWatchlistedUseCase should be resolved via domainModule", isWatchlistedUseCase)

        val toggleWatchlistUseCase: ToggleWatchlistUseCase = get()
        assertNotNull("ToggleWatchlistUseCase should be resolved via domainModule", toggleWatchlistUseCase)

        val getWatchlistCountUseCase: GetWatchlistCountUseCase = get()
        assertNotNull("GetWatchlistCountUseCase should be resolved via domainModule", getWatchlistCountUseCase)

        val mainViewModel: MainViewModel = get()
        assertNotNull("MainViewModel should be resolved via presentationModule", mainViewModel)

        val detailViewModel: DetailViewModel = get { parametersOf(SavedStateHandle()) }
        assertNotNull("DetailViewModel should be resolved via presentationModule", detailViewModel)
    }
}
