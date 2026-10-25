package com.prayogizan.golekpilem.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistDaoTest {

    private lateinit var database: GolekPilemDatabase
    private lateinit var watchlistDao: WatchlistDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GolekPilemDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        watchlistDao = database.watchlistDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertWatchlistAndObserveWatchlistReturnsSavedItemsOrderedByAddedAtDesc() = runBlocking {
        val item1 = WatchlistEntity(
            id = 1,
            mediaType = "MOVIE",
            title = "Movie 1",
            posterPath = "/path1.jpg",
            backdropPath = null,
            voteAverage = 7.5,
            releaseDate = "2024-01-01",
            overview = "Overview 1",
            addedAt = 1000L
        )
        val item2 = WatchlistEntity(
            id = 2,
            mediaType = "TV",
            title = "TV 2",
            posterPath = "/path2.jpg",
            backdropPath = null,
            voteAverage = 8.0,
            releaseDate = "2024-02-01",
            overview = "Overview 2",
            addedAt = 2000L
        )

        watchlistDao.insertWatchlist(item1)
        watchlistDao.insertWatchlist(item2)

        val items = watchlistDao.observeWatchlist().first()
        assertEquals(2, items.size)
        // Ordered by addedAt DESC
        assertEquals(2, items[0].id)
        assertEquals(1, items[1].id)
    }

    @Test
    fun isWatchlistedAndCheckIsWatchlistedReflectCorrectState() = runBlocking {
        val item = WatchlistEntity(
            id = 10,
            mediaType = "MOVIE",
            title = "Movie 10",
            posterPath = null,
            backdropPath = null,
            voteAverage = 6.5,
            releaseDate = "2023-05-10",
            overview = "Overview",
            addedAt = 500L
        )

        assertFalse(watchlistDao.isWatchlisted(10, "MOVIE").first())
        assertFalse(watchlistDao.checkIsWatchlisted(10, "MOVIE"))

        watchlistDao.insertWatchlist(item)

        assertTrue(watchlistDao.isWatchlisted(10, "MOVIE").first())
        assertTrue(watchlistDao.checkIsWatchlisted(10, "MOVIE"))
    }

    @Test
    fun deleteWatchlistRemovesItemAndDecreasesCount() = runBlocking {
        val item = WatchlistEntity(
            id = 20,
            mediaType = "TV",
            title = "TV 20",
            posterPath = null,
            backdropPath = null,
            voteAverage = 9.0,
            releaseDate = "2022-10-10",
            overview = "Overview",
            addedAt = 700L
        )

        watchlistDao.insertWatchlist(item)
        assertEquals(1, watchlistDao.observeWatchlistCount().first())

        val deletedCount = watchlistDao.deleteWatchlist(20, "TV")
        assertEquals(1, deletedCount)

        assertFalse(watchlistDao.isWatchlisted(20, "TV").first())
        assertEquals(0, watchlistDao.observeWatchlistCount().first())
    }
}
