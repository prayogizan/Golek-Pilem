package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class IsWatchlistedUseCaseTest {

    private val mockRepository: WatchlistRepository = mockk()

    @Test
    fun `invoke delegates directly to watchlistRepository isWatchlisted`() = runBlocking {
        every { mockRepository.isWatchlisted(550, "MOVIE") } returns flowOf(true)

        val useCase = IsWatchlistedUseCase(mockRepository)
        val result = useCase(550, "MOVIE").first()

        assertTrue(result)
        verify(exactly = 1) { mockRepository.isWatchlisted(550, "MOVIE") }
    }
}
