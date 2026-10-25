package com.prayogizan.golekpilem.ui.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.Network
import com.prayogizan.golekpilem.domain.model.ProductionCompany
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import com.prayogizan.golekpilem.domain.usecase.GetMovieDetailUseCase
import com.prayogizan.golekpilem.domain.usecase.GetTvSeriesDetailUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getMovieDetailUseCase: GetMovieDetailUseCase = mockk()
    private val getTvSeriesDetailUseCase: GetTvSeriesDetailUseCase = mockk()

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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `movie detail loads successfully and updates uiState to MovieSuccess`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Loading,
            Resource.Success(testMovieDetail)
        )

        val viewModel = DetailViewModel(
            savedStateHandle = savedStateHandle,
            getMovieDetailUseCase = getMovieDetailUseCase,
            getTvSeriesDetailUseCase = getTvSeriesDetailUseCase
        )

        viewModel.uiState.test {
            // Initial state is Loading
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

        val viewModel = DetailViewModel(
            savedStateHandle = savedStateHandle,
            getMovieDetailUseCase = getMovieDetailUseCase,
            getTvSeriesDetailUseCase = getTvSeriesDetailUseCase
        )

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
    fun `error during retrieval updates uiState to Error`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 533535, "mediaType" to "movie"))
        every { getMovieDetailUseCase(533535) } returns flowOf(
            Resource.Loading,
            Resource.Error("HTTP 404: Not Found")
        )

        val viewModel = DetailViewModel(
            savedStateHandle = savedStateHandle,
            getMovieDetailUseCase = getMovieDetailUseCase,
            getTvSeriesDetailUseCase = getTvSeriesDetailUseCase
        )

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

        val viewModel = DetailViewModel(
            savedStateHandle = savedStateHandle,
            getMovieDetailUseCase = getMovieDetailUseCase,
            getTvSeriesDetailUseCase = getTvSeriesDetailUseCase
        )

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is DetailUiState.Error)
            val detailError = errorState as DetailUiState.Error
            assertTrue(detailError.isNetworkError)
            assertEquals(com.prayogizan.golekpilem.R.string.error_no_internet, detailError.messageRes)
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

        val viewModel = DetailViewModel(
            savedStateHandle = savedStateHandle,
            getMovieDetailUseCase = getMovieDetailUseCase,
            getTvSeriesDetailUseCase = getTvSeriesDetailUseCase
        )

        viewModel.uiState.test {
            // Initial state
            assertEquals(DetailUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is DetailUiState.Error)

            // Act - dispatch retry event
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

        val viewModel = DetailViewModel(
            savedStateHandle = savedStateHandle,
            getMovieDetailUseCase = getMovieDetailUseCase,
            getTvSeriesDetailUseCase = getTvSeriesDetailUseCase
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DetailUiState.Error)
        }
    }

    @Test
    fun `unsupported media type emits Error state`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("id" to 1234, "mediaType" to "anime"))

        val viewModel = DetailViewModel(
            savedStateHandle = savedStateHandle,
            getMovieDetailUseCase = getMovieDetailUseCase,
            getTvSeriesDetailUseCase = getTvSeriesDetailUseCase
        )

        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is DetailUiState.Error)
            assertTrue((state as DetailUiState.Error).message.contains("Unsupported media type"))
        }
    }
}
