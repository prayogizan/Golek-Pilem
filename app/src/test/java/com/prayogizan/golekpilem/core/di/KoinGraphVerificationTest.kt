package com.prayogizan.golekpilem.core.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.prayogizan.golekpilem.data.di.databaseModule
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.MediaDetailDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.dao.WatchlistDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import com.prayogizan.golekpilem.domain.repository.StorageRepository
import com.prayogizan.golekpilem.domain.repository.TvRepository
import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import com.prayogizan.golekpilem.ui.common.StorageViewModel
import com.prayogizan.golekpilem.ui.detail.DetailViewModel
import com.prayogizan.golekpilem.ui.main.MainViewModel
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Automated test suite validating Koin dependency injection graph completeness.
 *
 * Runs under [RobolectricTestRunner] to provide valid Android [Context] for [databaseModule]
 * and validates that [GolekPilemDatabase], all DAOs, all repositories, domain interactors,
 * and view models resolve without runtime exceptions or missing definitions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class KoinGraphVerificationTest : KoinTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun verifyDatabaseModuleResolvesAllRoomComponentsSuccessfully() {
        startKoin {
            androidContext(context)
            modules(databaseModule)
        }

        val database: GolekPilemDatabase = get()
        assertNotNull(database)

        val mediaDao: MediaDao = get()
        assertNotNull(mediaDao)

        val remoteKeyDao: RemoteKeyDao = get()
        assertNotNull(remoteKeyDao)

        val mediaDetailDao: MediaDetailDao = get()
        assertNotNull(mediaDetailDao)

        val watchlistDao: WatchlistDao = get()
        assertNotNull(watchlistDao)
    }

    @Test
    fun verifyAppModuleResolvesAllRepositoriesAndViewModelsWithoutExceptions() {
        startKoin {
            androidContext(context)
            modules(appModule)
        }

        val movieRepository: MovieRepository = get()
        assertNotNull(movieRepository)

        val tvRepository: TvRepository = get()
        assertNotNull(tvRepository)

        val watchlistRepository: WatchlistRepository = get()
        assertNotNull(watchlistRepository)

        val storageRepository: StorageRepository = get()
        assertNotNull(storageRepository)

        val mainViewModel: MainViewModel = get()
        assertNotNull(mainViewModel)

        val detailViewModel: DetailViewModel = get()
        assertNotNull(detailViewModel)

        val storageViewModel: StorageViewModel = get()
        assertNotNull(storageViewModel)
    }

    @Test
    fun verifyCompleteKoinModuleGraphIntegrity() {
        startKoin {
            androidContext(context)
            modules(appModule)
        }.checkModules()
    }
}
