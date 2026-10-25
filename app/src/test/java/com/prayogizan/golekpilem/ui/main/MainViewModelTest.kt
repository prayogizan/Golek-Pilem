package com.prayogizan.golekpilem.ui.main

import androidx.paging.PagingData
import app.cash.turbine.test
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.usecase.GetMoviesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getMoviesUseCase: GetMoviesUseCase = mockk()
    private val getTvSeriesUseCase: GetTvSeriesUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getMoviesUseCase() } returns flowOf(PagingData.empty())
        every { getTvSeriesUseCase() } returns flowOf(PagingData.empty())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has selectedTabIndex equal to 0`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(getMoviesUseCase, getTvSeriesUseCase)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(0, initialState.selectedTabIndex)
        }
    }

    @Test
    fun `selectTab updates selectedTabIndex in uiState`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(getMoviesUseCase, getTvSeriesUseCase)

        viewModel.uiState.test {
            // Initial state
            assertEquals(0, awaitItem().selectedTabIndex)

            // Select TV Series tab
            viewModel.onEvent(MainUiEvent.SelectTab(1))
            testDispatcher.scheduler.advanceUntilIdle()

            // Verify updated state
            assertEquals(1, awaitItem().selectedTabIndex)

            // Select Movies tab back
            viewModel.onEvent(MainUiEvent.SelectTab(0))
            testDispatcher.scheduler.advanceUntilIdle()

            // Verify updated state
            assertEquals(0, awaitItem().selectedTabIndex)
        }
    }

    @Test
    fun `movies and tvSeries flows invoke corresponding use cases upon initialization`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(getMoviesUseCase, getTvSeriesUseCase)

        assertNotNull(viewModel.movies)
        assertNotNull(viewModel.tvSeries)

        verify(exactly = 1) { getMoviesUseCase() }
        verify(exactly = 1) { getTvSeriesUseCase() }
    }
}
