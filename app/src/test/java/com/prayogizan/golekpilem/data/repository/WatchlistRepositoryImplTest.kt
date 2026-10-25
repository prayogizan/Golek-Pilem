package com.prayogizan.golekpilem.data.repository

import com.prayogizan.golekpilem.data.local.dao.WatchlistDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity
import com.prayogizan.golekpilem.domain.model.MediaItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WatchlistRepositoryImplTest {

    private val mockWatchlistDao: WatchlistDao = mockk(relaxed = true)
    private val mockDatabase: GolekPilemDatabase = mockk {
        every { watchlistDao() } returns mockWatchlistDao
    }

    private lateinit var repository: WatchlistRepositoryImpl

    @Before
    fun setUp() {
        repository = WatchlistRepositoryImpl(database = mockDatabase)
    }

    @Test
    fun `getWatchlist streams domain MediaItem entities transformed from WatchlistEntity`() = runBlocking {
        val entities = listOf(
            WatchlistEntity(
                id = 101,
                mediaType = "MOVIE",
                title = "Inception",
                posterPath = "/inception.jpg",
                backdropPath = "/backdrop.jpg",
                voteAverage = 8.4,
                releaseDate = "2010-07-16",
                overview = "A thief who steals corporate secrets...",
                addedAt = 1000L
            )
        )
        every { mockWatchlistDao.observeWatchlist() } returns flowOf(entities)

        val result = repository.getWatchlist().first()

        assertEquals(1, result.size)
        assertEquals(101, result[0].id)
        assertEquals("MOVIE", result[0].mediaType)
        assertEquals("Inception", result[0].title)
    }

    @Test
    fun `isWatchlisted delegates to DAO with uppercase mediaType`() = runBlocking {
        every { mockWatchlistDao.isWatchlisted(101, "MOVIE") } returns flowOf(true)

        val result = repository.isWatchlisted(101, "movie").first()

        assertTrue(result)
        coVerify(exactly = 1) { mockWatchlistDao.isWatchlisted(101, "MOVIE") }
    }

    @Test
    fun `addToWatchlist persists converted entity via DAO`() = runBlocking {
        val item = MediaItem(
            id = 202,
            mediaType = "tv",
            title = "Severance",
            posterUrl = null,
            backdropUrl = null,
            releaseDate = "2022-02-18",
            releaseYear = "2022",
            voteAverage = 8.7,
            overview = "Mark leads a team of office workers..."
        )
        coEvery { mockWatchlistDao.insertWatchlist(any()) } returns Unit

        repository.addToWatchlist(item)

        coVerify(exactly = 1) {
            mockWatchlistDao.insertWatchlist(match {
                it.id == 202 && it.mediaType == "TV" && it.title == "Severance"
            })
        }
    }

    @Test
    fun `removeFromWatchlist deletes item via DAO with uppercase mediaType`() = runBlocking {
        coEvery { mockWatchlistDao.deleteWatchlist(303, "TV") } returns 1

        repository.removeFromWatchlist(303, "tv")

        coVerify(exactly = 1) { mockWatchlistDao.deleteWatchlist(303, "TV") }
    }

    @Test
    fun `getWatchlistCount delegates to DAO count observation stream`() = runBlocking {
        every { mockWatchlistDao.observeWatchlistCount() } returns flowOf(7)

        val count = repository.getWatchlistCount().first()

        assertEquals(7, count)
        coVerify(exactly = 1) { mockWatchlistDao.observeWatchlistCount() }
    }

    @Test(expected = CancellationException::class)
    fun `addToWatchlist rethrows CancellationException without swallowing`() = runBlocking {
        coEvery { mockWatchlistDao.insertWatchlist(any()) } throws CancellationException("Coroutine cancelled")

        val item = MediaItem(
            id = 999,
            mediaType = "MOVIE",
            title = "Cancelled Movie",
            posterUrl = null,
            backdropUrl = null,
            releaseDate = null,
            releaseYear = "N/A",
            voteAverage = 0.0,
            overview = ""
        )

        repository.addToWatchlist(item)
    }

    @Test(expected = CancellationException::class)
    fun `removeFromWatchlist rethrows CancellationException without swallowing`() = runBlocking {
        coEvery { mockWatchlistDao.deleteWatchlist(any(), any()) } throws CancellationException("Coroutine cancelled")

        repository.removeFromWatchlist(999, "movie")
    }
}
