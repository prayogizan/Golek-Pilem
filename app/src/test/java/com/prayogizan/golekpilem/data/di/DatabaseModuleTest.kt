package com.prayogizan.golekpilem.data.di

import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.MediaDetailDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.dao.WatchlistDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class DatabaseModuleTest : KoinTest {

    private val mockMediaDao: MediaDao = mockk()
    private val mockRemoteKeyDao: RemoteKeyDao = mockk()
    private val mockMediaDetailDao: MediaDetailDao = mockk()
    private val mockWatchlistDao: WatchlistDao = mockk()
    private val mockDatabase: GolekPilemDatabase = mockk {
        every { mediaDao() } returns mockMediaDao
        every { remoteKeyDao() } returns mockRemoteKeyDao
        every { mediaDetailDao() } returns mockMediaDetailDao
        every { watchlistDao() } returns mockWatchlistDao
    }

    private val testDatabaseModule = module {
        single<GolekPilemDatabase> { mockDatabase }
        single<MediaDao> { get<GolekPilemDatabase>().mediaDao() }
        single<RemoteKeyDao> { get<GolekPilemDatabase>().remoteKeyDao() }
        single<MediaDetailDao> { get<GolekPilemDatabase>().mediaDetailDao() }
        single<WatchlistDao> { get<GolekPilemDatabase>().watchlistDao() }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(testDatabaseModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify databaseModule resolves database and all DAOs successfully`() {
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
}
