package com.prayogizan.golekpilem.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.MediaDetailDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.dao.WatchlistDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Base test harness for hermetic in-memory Room database execution on the JVM.
 *
 * Configures [RobolectricTestRunner] to provide an Android runtime environment without
 * requiring an emulator or physical device. Each test suite inherits an isolated, in-memory
 * SQLite database instance built with [allowMainThreadQueries] and closed automatically on tear down.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
abstract class BaseRoomTest {

    protected lateinit var database: GolekPilemDatabase
    protected lateinit var mediaDao: MediaDao
    protected lateinit var watchlistDao: WatchlistDao
    protected lateinit var mediaDetailDao: MediaDetailDao
    protected lateinit var remoteKeyDao: RemoteKeyDao

    @Before
    open fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GolekPilemDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        mediaDao = database.mediaDao()
        watchlistDao = database.watchlistDao()
        mediaDetailDao = database.mediaDetailDao()
        remoteKeyDao = database.remoteKeyDao()
    }

    @After
    open fun tearDown() {
        database.close()
    }
}
