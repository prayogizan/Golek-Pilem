package com.prayogizan.golekpilem.domain.usecase

import androidx.paging.PagingData
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMovieDetailUseCaseTest {

    private class FakeMovieRepository : MovieRepository {
        var requestedMovieId: Int? = null
        var requestedForceRefresh: Boolean? = null
        val expectedResourceFlow: Flow<Resource<MovieDetail>> = flowOf(
            Resource.Success(
                MovieDetail(
                    id = 100,
                    title = "Test Movie",
                    tagline = "Tagline",
                    overview = "Overview",
                    posterUrl = null,
                    backdropUrl = null,
                    releaseDate = "2026-01-01",
                    runtime = 120,
                    voteAverage = 8.0,
                    voteCount = 100,
                    genres = emptyList(),
                    productionCompanies = emptyList(),
                    status = "Released"
                )
            )
        )

        override fun getMovies(): Flow<PagingData<Movie>> = emptyFlow()

        override fun getMovieDetail(movieId: Int, forceRefresh: Boolean): Flow<Resource<MovieDetail>> {
            requestedMovieId = movieId
            requestedForceRefresh = forceRefresh
            return expectedResourceFlow
        }
    }

    @Test
    fun `invoke delegates execution directly to movieRepository getMovieDetail with correct id and default forceRefresh`() {
        val fakeRepository = FakeMovieRepository()
        val useCase = GetMovieDetailUseCase(fakeRepository)

        val resultFlow = useCase(movieId = 100)

        assertEquals(fakeRepository.expectedResourceFlow, resultFlow)
        assertEquals(100, fakeRepository.requestedMovieId)
        assertEquals(false, fakeRepository.requestedForceRefresh)
    }

    @Test
    fun `invoke delegates execution directly to movieRepository getMovieDetail with explicit forceRefresh true`() {
        val fakeRepository = FakeMovieRepository()
        val useCase = GetMovieDetailUseCase(fakeRepository)

        val resultFlow = useCase(movieId = 100, forceRefresh = true)

        assertEquals(fakeRepository.expectedResourceFlow, resultFlow)
        assertEquals(100, fakeRepository.requestedMovieId)
        assertEquals(true, fakeRepository.requestedForceRefresh)
    }
}
