package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.domain.repository.WatchlistRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWatchlistCountUseCaseTest {

    private val mockRepository: WatchlistRepository = mockk()

    @Test
    fun `invoke delegates directly to watchlistRepository getWatchlistCount`() = runBlocking {
        every { mockRepository.getWatchlistCount() } returns flowOf(12)

        val useCase = GetWatchlistCountUseCase(mockRepository)
        val result = useCase().first()

        assertEquals(12, result)
        verify(exactly = 1) { mockRepository.getWatchlistCount() }
    }
}
