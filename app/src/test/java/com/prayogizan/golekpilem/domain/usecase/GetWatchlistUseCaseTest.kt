package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.model.MediaItem
import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWatchlistUseCaseTest {

    private val mockRepository: WatchlistRepository = mockk()

    @Test
    fun `invoke delegates directly to watchlistRepository getWatchlist`() = runBlocking {
        val sampleItems = listOf(
            MediaItem(
                id = 1,
                mediaType = "MOVIE",
                title = "Inception",
                posterUrl = null,
                backdropUrl = null,
                releaseDate = "2010-07-16",
                releaseYear = "2010",
                voteAverage = 8.4,
                overview = "Overview"
            )
        )
        every { mockRepository.getWatchlist() } returns flowOf(sampleItems)

        val useCase = GetWatchlistUseCase(mockRepository)
        val result = useCase().first()

        assertEquals(sampleItems, result)
        verify(exactly = 1) { mockRepository.getWatchlist() }
    }
}
