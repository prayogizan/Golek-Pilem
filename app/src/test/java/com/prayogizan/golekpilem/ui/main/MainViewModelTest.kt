package com.prayogizan.golekpilem.ui.main

import androidx.paging.PagingData
import app.cash.turbine.test
import com.prayogizan.golekpilem.domain.model.MediaItem
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.usecase.GetMoviesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesUseCase
import com.prayogizan.golekpilem.domain.usecase.GetWatchlistCountUseCase
import com.prayogizan.golekpilem.domain.usecase.GetWatchlistUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getMoviesUseCase: GetMoviesUseCase = mockk()
    private val getTvSeriesUseCase: GetTvSeriesUseCase = mockk()
    private val getWatchlistUseCase: GetWatchlistUseCase = mockk()
    private val getWatchlistCountUseCase: GetWatchlistCountUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getMoviesUseCase() } returns flowOf(PagingData.empty())
        every { getTvSeriesUseCase() } returns flowOf(PagingData.empty())
        every { getWatchlistUseCase() } returns flowOf(emptyList())
        every { getWatchlistCountUseCase() } returns flowOf(0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has selectedTabIndex equal to 0 and empty watchlist`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(
            getMoviesUseCase,
            getTvSeriesUseCase,
            getWatchlistUseCase,
            getWatchlistCountUseCase
        )

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(0, initialState.selectedTabIndex)
            assertEquals(emptyList<MediaItem>(), initialState.watchlistItems)
            assertEquals(0, initialState.watchlistCount)
        }
    }

    @Test
    fun `selectTab updates selectedTabIndex in uiState across all three tabs`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(
            getMoviesUseCase,
            getTvSeriesUseCase,
            getWatchlistUseCase,
            getWatchlistCountUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            // Initial state
            assertEquals(0, awaitItem().selectedTabIndex)

            // Select TV Series tab
            viewModel.onEvent(MainUiEvent.SelectTab(1))
            assertEquals(1, awaitItem().selectedTabIndex)

            // Select Watchlist tab
            viewModel.onEvent(MainUiEvent.SelectTab(2))
            assertEquals(2, awaitItem().selectedTabIndex)

            // Select Movies tab back
            viewModel.onEvent(MainUiEvent.SelectTab(0))
            assertEquals(0, awaitItem().selectedTabIndex)
        }
    }

    @Test
    fun `observeWatchlist updates watchlistItems and loading state in uiState`() = runTest(testDispatcher) {
        val sampleItem = MediaItem(
            id = 10,
            mediaType = "MOVIE",
            title = "Dune",
            posterUrl = null,
            backdropUrl = null,
            releaseDate = "2021-10-22",
            releaseYear = "2021",
            voteAverage = 7.9,
            overview = "Paul Atreides leads nomadic tribes..."
        )
        every { getWatchlistUseCase() } returns flowOf(listOf(sampleItem))

        val viewModel = MainViewModel(
            getMoviesUseCase,
            getTvSeriesUseCase,
            getWatchlistUseCase,
            getWatchlistCountUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.watchlistItems.size)
            assertEquals("Dune", state.watchlistItems[0].title)
        }
    }

    @Test
    fun `observeWatchlistCount updates reactive badge count in uiState`() = runTest(testDispatcher) {
        every { getWatchlistCountUseCase() } returns flowOf(5)

        val viewModel = MainViewModel(
            getMoviesUseCase,
            getTvSeriesUseCase,
            getWatchlistUseCase,
            getWatchlistCountUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(5, state.watchlistCount)
        }
    }

    @Test
    fun `all use cases are invoked upon ViewModel initialization`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(
            getMoviesUseCase,
            getTvSeriesUseCase,
            getWatchlistUseCase,
            getWatchlistCountUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.movies)
        assertNotNull(viewModel.tvSeries)

        verify(exactly = 1) { getMoviesUseCase() }
        verify(exactly = 1) { getTvSeriesUseCase() }
        verify(exactly = 1) { getWatchlistUseCase() }
        verify(exactly = 1) { getWatchlistCountUseCase() }
    }

    @Test
    fun `refreshWatchlist triggers getWatchlistUseCase and updates isWatchlistRefreshing state`() = runTest(testDispatcher) {
        val sampleItem = MediaItem(
            id = 10,
            mediaType = "MOVIE",
            title = "Dune",
            posterUrl = null,
            backdropUrl = null,
            releaseDate = "2021-10-22",
            releaseYear = "2021",
            voteAverage = 7.9,
            overview = "Paul Atreides..."
        )
        every { getWatchlistUseCase() } returns flowOf(emptyList()) andThen flowOf(listOf(sampleItem))

        val viewModel = MainViewModel(
            getMoviesUseCase,
            getTvSeriesUseCase,
            getWatchlistUseCase,
            getWatchlistCountUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.isWatchlistRefreshing)

            viewModel.onEvent(MainUiEvent.RefreshWatchlist)
            val refreshingState = awaitItem()
            assertTrue(refreshingState.isWatchlistRefreshing)

            testDispatcher.scheduler.advanceUntilIdle()

            val refreshedState = awaitItem()
            assertEquals(1, refreshedState.watchlistItems.size)
            assertFalse(refreshedState.isWatchlistRefreshing)
        }
    }
}
