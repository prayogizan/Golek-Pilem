package com.prayogizan.golekpilem.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.prayogizan.golekpilem.R
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.Network
import com.prayogizan.golekpilem.domain.model.ProductionCompany
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import com.prayogizan.golekpilem.domain.usecase.GetMovieDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.IsWatchlistedUseCase
import com.prayogizan.golekpilem.domain.usecase.ToggleWatchlistUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getMovieDetailUseCase: GetMovieDetailUseCase = mockk()
    private val getTvSeriesDetailUseCase: GetTvSeriesDetailUseCase = mockk()
    private val isWatchlistedUseCase: IsWatchlistedUseCase = mockk()
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase = mockk()

    private val testMovieDetail = MovieDetail(
        id = 533535,
        title = "Deadpool & Wolverine",
        tagline = "Come together.",
        overview = "A listless Wade Wilson toils away in civilian life.",
        posterUrl = "https://image.tmdb.org/t/p/w342/poster.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
        releaseDate = "2024-07-24",
        runtime = 128,
        voteAverage = 7.7,
        voteCount = 5120,
        genres = listOf(Genre(id = 28, name = "Action")),
        productionCompanies = listOf(ProductionCompany(id = 420, name = "Marvel Studios", logoUrl = null)),
        status = "Released"
    )

    private val testTvDetail = TvSeriesDetail(
        id = 66732,
        name = "Stranger Things",
        tagline = "Every ending has a beginning.",
        overview = "When a young boy vanishes...",
        posterUrl = "https://image.tmdb.org/t/p/w342/poster_tv.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop_tv.jpg",
        firstAirDate = "2016-07-15",
        lastAirDate = "2022-07-01",
        numberOfSeasons = 4,
        numberOfEpisodes = 34,
        voteAverage = 8.6,
        voteCount = 16400,
        genres = listOf(Genre(id = 18, name = "Drama")),
        networks = listOf(Network(id = 213, name = "Netflix", logoUrl = null)),
        status = "Returning Series"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { isWatchlistedUseCase(any(), any()) } returns flowOf(false)
        coEvery { toggleWatchlistUseCase(any()) } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle): DetailViewModel {
        return DetailViewModel(
            savedStateHandle = savedStateHandle,
            getMovieDetailUseCase = getMovieDetailUseCase,
            getTvSeriesDetailUseCase = getTvSeriesDetailUseCase,
            isWatchlistedUseCase = isWatchlistedUseCase,
            toggleWatchlistUseCase = toggleWatchlistUseCase
        )
    }

    @Test
    fun `movie detail loads successfully and updates uiState to MovieSuccess`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Loading,
            Resource.Success(testMovieDetail)
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is DetailUiState.MovieSuccess)
            assertEquals(testMovieDetail, (successState as DetailUiState.MovieSuccess).detail)
        }

        verify(exactly = 1) { getMovieDetailUseCase(533535) }
    }

    @Test
    fun `tv series detail loads successfully and updates uiState to TvSuccess`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 66732, "mediaType" to "tv"))
        every { getTvSeriesDetailUseCase(66732) } returns flowOf(
            Resource.Loading,
            Resource.Success(testTvDetail)
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is DetailUiState.TvSuccess)
            assertEquals(testTvDetail, (successState as DetailUiState.TvSuccess).detail)
        }

        verify(exactly = 1) { getTvSeriesDetailUseCase(66732) }
    }

    @Test
    fun `isWatchlisted stream updates isWatchlisted in uiState`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Loading,
            Resource.Success(testMovieDetail)
        )
        every { isWatchlistedUseCase(533535, "movie") } returns flowOf(true)

        val viewModel = createViewModel(savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DetailUiState.MovieSuccess)
            assertTrue((state as DetailUiState.MovieSuccess).isWatchlisted)
        }
    }

    @Test
    fun `ToggleWatchlist event invokes ToggleWatchlistUseCase and updates userMessageRes with added confirmation`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Success(testMovieDetail)
        )
        coEvery { toggleWatchlistUseCase(any()) } returns true

        val viewModel = createViewModel(savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.ToggleWatchlist)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as DetailUiState.MovieSuccess
            assertEquals(R.string.msg_added_to_watchlist, state.userMessageRes)
        }

        coVerify(exactly = 1) { toggleWatchlistUseCase(any()) }
    }

    @Test
    fun `ToggleWatchlist event invokes ToggleWatchlistUseCase and updates userMessageRes with removed confirmation`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Success(testMovieDetail)
        )
        coEvery { toggleWatchlistUseCase(any()) } returns false

        val viewModel = createViewModel(savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.ToggleWatchlist)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as DetailUiState.MovieSuccess
            assertEquals(R.string.msg_removed_from_watchlist, state.userMessageRes)
        }

        coVerify(exactly = 1) { toggleWatchlistUseCase(any()) }
    }

    @Test
    fun `ClearUserMessage event resets userMessageRes to null`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Success(testMovieDetail)
        )
        coEvery { toggleWatchlistUseCase(any()) } returns true

        val viewModel = createViewModel(savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.ToggleWatchlist)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(DetailUiEvent.ClearUserMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as DetailUiState.MovieSuccess
            assertNull(state.userMessageRes)
        }
    }

    @Test
    fun `error during retrieval updates uiState to Error`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Loading,
            Resource.Error("HTTP 404: Not Found")
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is DetailUiState.Error)
            assertEquals("HTTP 404: Not Found", (errorState as DetailUiState.Error).message)
        }
    }

    @Test
    fun `network error during retrieval sets isNetworkError true on uiState Error`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Loading,
            Resource.Error("No internet connection", cause = java.net.UnknownHostException())
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is DetailUiState.Error)
            val detailError = errorState as DetailUiState.Error
            assertTrue(detailError.isNetworkError)
            assertEquals(R.string.error_no_internet, detailError.messageRes)
        }
    }

    @Test
    fun `retry event re-invokes usecase and recovers to MovieSuccess`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Error("Network Timeout")
        ) andThen flowOf(
            Resource.Success(testMovieDetail)
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is DetailUiState.Error)

            viewModel.onEvent(DetailUiEvent.Retry)
            val retryLoadingState = awaitItem()
            assertEquals(DetailUiState.Loading, retryLoadingState)

            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is DetailUiState.MovieSuccess)
            assertEquals(testMovieDetail, (successState as DetailUiState.MovieSuccess).detail)
        }

        verify(exactly = 2) { getMovieDetailUseCase(533535) }
    }

    @Test
    fun `invalid media identifier emits immediate Error state`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 0, "mediaType" to "movie"))

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DetailUiState.Error)
        }
    }

    @Test
    fun `unsupported media type emits Error state`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 1234, "mediaType" to "anime"))

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is DetailUiState.Error)
            assertTrue((state as DetailUiState.Error).message.contains("Unsupported media type"))
        }
    }

    @Test
    fun `cached movie detail propagates isOfflineCache true to uiState MovieSuccess`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        val cachedMovie = testMovieDetail.copy(isOfflineCache = true)
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Loading,
            Resource.Success(cachedMovie)
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is DetailUiState.MovieSuccess)
            assertTrue((successState as DetailUiState.MovieSuccess).isOfflineCache)
        }
    }

    @Test
    fun `cached tv series detail propagates isOfflineCache true to uiState TvSuccess`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 66732, "mediaType" to "tv"))
        val cachedTv = testTvDetail.copy(isOfflineCache = true)
        every { getTvSeriesDetailUseCase(66732) } returns flowOf(
            Resource.Loading,
            Resource.Success(cachedTv)
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is DetailUiState.TvSuccess)
            assertTrue((successState as DetailUiState.TvSuccess).isOfflineCache)
        }
    }

    @Test
    fun `refresh event triggers getMovieDetailUseCase with forceRefresh true and updates movie detail`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        val freshMovie = testMovieDetail.copy(voteAverage = 9.0)

        every { getMovieDetailUseCase(533535, forceRefresh = false) } returns flowOf(
            Resource.Loading,
            Resource.Success(testMovieDetail)
        )
        every { getMovieDetailUseCase(533535, forceRefresh = true) } returns flowOf(
            Resource.Loading,
            Resource.Success(freshMovie)
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val initialSuccess = awaitItem()
            assertTrue(initialSuccess is DetailUiState.MovieSuccess)
            assertEquals(7.7, (initialSuccess as DetailUiState.MovieSuccess).detail.voteAverage, 0.01)
            assertFalse(initialSuccess.isRefreshing)

            viewModel.onEvent(DetailUiEvent.Refresh)
            val refreshingState = awaitItem()
            assertTrue(refreshingState is DetailUiState.MovieSuccess)
            assertTrue((refreshingState as DetailUiState.MovieSuccess).isRefreshing)

            testDispatcher.scheduler.advanceUntilIdle()
            val updatedSuccess = awaitItem()
            assertTrue(updatedSuccess is DetailUiState.MovieSuccess)
            assertEquals(9.0, (updatedSuccess as DetailUiState.MovieSuccess).detail.voteAverage, 0.01)
            assertFalse(updatedSuccess.isRefreshing)
        }

        verify(exactly = 1) { getMovieDetailUseCase(533535, forceRefresh = true) }
    }

    @Test
    fun `refresh event network failure preserves existing movie detail and sets userMessageRes with isRefreshing false`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))

        every { getMovieDetailUseCase(533535, forceRefresh = false) } returns flowOf(
            Resource.Loading,
            Resource.Success(testMovieDetail)
        )
        every { getMovieDetailUseCase(533535, forceRefresh = true) } returns flowOf(
            Resource.Loading,
            Resource.Error("Network offline", java.io.IOException("No internet"))
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val initialSuccess = awaitItem()
            assertTrue(initialSuccess is DetailUiState.MovieSuccess)
            assertFalse((initialSuccess as DetailUiState.MovieSuccess).isRefreshing)

            viewModel.onEvent(DetailUiEvent.Refresh)
            val refreshingState = awaitItem()
            assertTrue(refreshingState is DetailUiState.MovieSuccess)
            assertTrue((refreshingState as DetailUiState.MovieSuccess).isRefreshing)

            testDispatcher.scheduler.advanceUntilIdle()
            val failedRefreshState = awaitItem()
            assertTrue(failedRefreshState is DetailUiState.MovieSuccess)
            val successResult = failedRefreshState as DetailUiState.MovieSuccess
            assertEquals(testMovieDetail, successResult.detail)
            assertFalse(successResult.isRefreshing)
            assertEquals(R.string.error_no_internet, successResult.userMessageRes)
        }
    }

    @Test
    fun `refresh event triggers getTvSeriesDetailUseCase with forceRefresh true and updates tv detail`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 66732, "mediaType" to "tv"))
        val freshTv = testTvDetail.copy(numberOfEpisodes = 40)

        every { getTvSeriesDetailUseCase(66732, forceRefresh = false) } returns flowOf(
            Resource.Loading,
            Resource.Success(testTvDetail)
        )
        every { getTvSeriesDetailUseCase(66732, forceRefresh = true) } returns flowOf(
            Resource.Loading,
            Resource.Success(freshTv)
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val initialSuccess = awaitItem()
            assertTrue(initialSuccess is DetailUiState.TvSuccess)
            assertFalse((initialSuccess as DetailUiState.TvSuccess).isRefreshing)

            viewModel.onEvent(DetailUiEvent.Refresh)
            val refreshingState = awaitItem()
            assertTrue(refreshingState is DetailUiState.TvSuccess)
            assertTrue((refreshingState as DetailUiState.TvSuccess).isRefreshing)

            testDispatcher.scheduler.advanceUntilIdle()
            val updatedSuccess = awaitItem()
            assertTrue(updatedSuccess is DetailUiState.TvSuccess)
            assertEquals(40, (updatedSuccess as DetailUiState.TvSuccess).detail.numberOfEpisodes)
            assertFalse(updatedSuccess.isRefreshing)
        }

        verify(exactly = 1) { getTvSeriesDetailUseCase(66732, forceRefresh = true) }
    }

    @Test
    fun `refresh event from Error state triggers loadDetail to retry`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535, forceRefresh = false) } returns flowOf(
            Resource.Error("Timeout")
        ) andThen flowOf(
            Resource.Success(testMovieDetail)
        )

        val viewModel = createViewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is DetailUiState.Error)

            viewModel.onEvent(DetailUiEvent.Refresh)
            assertEquals(DetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is DetailUiState.MovieSuccess)
        }

        verify(exactly = 2) { getMovieDetailUseCase(533535, forceRefresh = false) }
    }
}
