package com.prayogizan.golekpilem.ui.common

import app.cash.turbine.test
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.domain.model.StorageStats
import com.prayogizan.golekpilem.domain.usecase.ClearImageCacheUseCase
import com.prayogizan.golekpilem.domain.usecase.GetStorageStatsUseCase
import com.prayogizan.golekpilem.domain.usecase.PurgeExpiredCatalogUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StorageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getStorageStatsUseCase: GetStorageStatsUseCase = mockk()
    private val clearImageCacheUseCase: ClearImageCacheUseCase = mockk(relaxed = true)
    private val purgeExpiredCatalogUseCase: PurgeExpiredCatalogUseCase = mockk()

    private val sampleStats = StorageStats(
        dbSizeBytes = 4_194_304L,
        imageCacheSizeBytes = 52_428_800L,
        dbSizeFormatted = "4.0 MB",
        imageCacheSizeFormatted = "50.0 MB",
        totalSizeFormatted = "54.0 MB"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getStorageStatsUseCase() } returns sampleStats
        coEvery { purgeExpiredCatalogUseCase(any()) } returns 12
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads storage stats and transitions to Success`() = runTest(testDispatcher) {
        val viewModel = StorageViewModel(
            getStorageStatsUseCase,
            clearImageCacheUseCase,
            purgeExpiredCatalogUseCase
        )

        viewModel.uiState.test {
            assertEquals(StorageUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem() as StorageUiState.Success
            assertEquals(sampleStats, successState.stats)
            assertEquals(false, successState.isClearing)
            assertEquals(false, successState.isPurging)
        }
    }

    @Test
    fun `clearImageCache triggers use case, refreshes stats, and sets success message`() = runTest(testDispatcher) {
        val refreshedStats = sampleStats.copy(
            imageCacheSizeBytes = 0L,
            imageCacheSizeFormatted = "0 B",
            totalSizeFormatted = "4.0 MB"
        )
        coEvery { getStorageStatsUseCase() } returnsMany listOf(sampleStats, refreshedStats)

        val viewModel = StorageViewModel(
            getStorageStatsUseCase,
            clearImageCacheUseCase,
            purgeExpiredCatalogUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initial = awaitItem() as StorageUiState.Success
            assertEquals("50.0 MB", initial.stats.imageCacheSizeFormatted)

            viewModel.onEvent(StorageUiEvent.ClearImageCache)

            val clearingState = awaitItem() as StorageUiState.Success
            assertTrue(clearingState.isClearing)

            testDispatcher.scheduler.advanceUntilIdle()

            val completedState = awaitItem() as StorageUiState.Success
            assertEquals("0 B", completedState.stats.imageCacheSizeFormatted)
            assertEquals(false, completedState.isClearing)
            assertEquals(R.string.storage_clear_images_success, completedState.userMessageRes)
        }

        coVerify(exactly = 1) { clearImageCacheUseCase() }
    }

    @Test
    fun `purgeExpiredCatalog triggers use case, refreshes stats, and sets success message with count`() = runTest(testDispatcher) {
        val viewModel = StorageViewModel(
            getStorageStatsUseCase,
            clearImageCacheUseCase,
            purgeExpiredCatalogUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // initial Success

            viewModel.onEvent(StorageUiEvent.PurgeExpiredCatalog(retentionDays = 7))

            val purgingState = awaitItem() as StorageUiState.Success
            assertTrue(purgingState.isPurging)

            testDispatcher.scheduler.advanceUntilIdle()

            val completedState = awaitItem() as StorageUiState.Success
            assertEquals(false, completedState.isPurging)
            assertEquals(12, completedState.purgedCount)
            assertEquals(R.string.storage_purge_catalog_success, completedState.userMessageRes)
        }

        coVerify(exactly = 1) { purgeExpiredCatalogUseCase(7) }
    }

    @Test
    fun `clearUserMessage resets userMessageRes to null`() = runTest(testDispatcher) {
        val viewModel = StorageViewModel(
            getStorageStatsUseCase,
            clearImageCacheUseCase,
            purgeExpiredCatalogUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(StorageUiEvent.ClearImageCache)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val stateWithMessage = awaitItem() as StorageUiState.Success
            assertEquals(R.string.storage_clear_images_success, stateWithMessage.userMessageRes)

            viewModel.onEvent(StorageUiEvent.ClearUserMessage)

            val stateWithoutMessage = awaitItem() as StorageUiState.Success
            assertNull(stateWithoutMessage.userMessageRes)
        }
    }

    @Test
    fun `loadStorageStats failure transitions to Error state`() = runTest(testDispatcher) {
        coEvery { getStorageStatsUseCase() } throws RuntimeException("Disk I/O failure")

        val viewModel = StorageViewModel(
            getStorageStatsUseCase,
            clearImageCacheUseCase,
            purgeExpiredCatalogUseCase
        )

        viewModel.uiState.test {
            assertEquals(StorageUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem() as StorageUiState.Error
            assertEquals("Disk I/O failure", errorState.message)
        }
    }
}
