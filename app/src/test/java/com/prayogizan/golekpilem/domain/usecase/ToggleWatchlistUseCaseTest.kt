package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.model.MediaItem
import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleWatchlistUseCaseTest {

    private val mockRepository: WatchlistRepository = mockk(relaxed = true)
    private val useCase = ToggleWatchlistUseCase(mockRepository)

    private val sampleItem = MediaItem(
        id = 42,
        mediaType = "MOVIE",
        title = "The Hitchhiker's Guide to the Galaxy",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = "2005-04-28",
        releaseYear = "2005",
        voteAverage = 6.8,
        overview = "Don't Panic."
    )

    @Test
    fun `invoke adds item to watchlist when not currently watchlisted and returns true`() = runBlocking {
        every { mockRepository.isWatchlisted(42, "MOVIE") } returns flowOf(false)
        coEvery { mockRepository.addToWatchlist(sampleItem) } returns Unit

        val result = useCase(sampleItem)

        assertTrue(result)
        coVerify(exactly = 1) { mockRepository.addToWatchlist(sampleItem) }
        coVerify(exactly = 0) { mockRepository.removeFromWatchlist(any(), any()) }
    }

    @Test
    fun `invoke removes item from watchlist when currently watchlisted and returns false`() = runBlocking {
        every { mockRepository.isWatchlisted(42, "MOVIE") } returns flowOf(true)
        coEvery { mockRepository.removeFromWatchlist(42, "MOVIE") } returns Unit

        val result = useCase(sampleItem)

        assertFalse(result)
        coVerify(exactly = 1) { mockRepository.removeFromWatchlist(42, "MOVIE") }
        coVerify(exactly = 0) { mockRepository.addToWatchlist(any()) }
    }
}
