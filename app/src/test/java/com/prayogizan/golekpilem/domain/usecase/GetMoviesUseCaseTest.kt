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

class GetMoviesUseCaseTest {

    private class FakeMovieRepository(
        val pagingDataFlow: Flow<PagingData<Movie>> = flowOf(PagingData.empty())
    ) : MovieRepository {
        var getMoviesCalled = false

        override fun getMovies(): Flow<PagingData<Movie>> {
            getMoviesCalled = true
            return pagingDataFlow
        }

        override fun getMovieDetail(movieId: Int): Flow<Resource<MovieDetail>> {
            return emptyFlow()
        }
    }

    @Test
    fun `invoke delegates execution directly to movieRepository getMovies`() {
        val fakeRepository = FakeMovieRepository()
        val useCase = GetMoviesUseCase(fakeRepository)

        val resultFlow = useCase()

        assertEquals(fakeRepository.pagingDataFlow, resultFlow)
        assertEquals(true, fakeRepository.getMoviesCalled)
    }
}
