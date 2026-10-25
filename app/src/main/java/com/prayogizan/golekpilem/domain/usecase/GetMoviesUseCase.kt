package com.prayogizan.golekpilem.domain.usecase

import androidx.paging.PagingData
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

/**
 * Interactor responsible for retrieving paginated popular movies from the repository.
 *
 * Encapsulates the domain business policy for movie catalog discovery and streams a reactive
 * [PagingData] sequence for presentation consumption.
 *
 * @property movieRepository The repository abstraction providing access to movie data.
 */
class GetMoviesUseCase(
    private val movieRepository: MovieRepository
) {
    /**
     * Executes the use case to stream popular movies.
     *
     * @return A cold [Flow] emitting reactive [PagingData] containing domain [Movie] entities.
     */
    operator fun invoke(): Flow<PagingData<Movie>> = movieRepository.getMovies()
}
