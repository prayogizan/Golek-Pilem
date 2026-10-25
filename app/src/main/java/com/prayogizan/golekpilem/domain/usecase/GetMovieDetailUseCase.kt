package com.prayogizan.golekpilem.domain.usecase

import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

/**
 * Interactor responsible for retrieving full details of a specific movie.
 *
 * Encapsulates the domain business rule for fetching movie details and returns a reactive lifecycle stream.
 *
 * @property movieRepository The repository abstraction providing access to movie data.
 */
class GetMovieDetailUseCase(
    private val movieRepository: MovieRepository
) {
    /**
     * Executes the use case for the given movie identifier.
     *
     * @param movieId The unique TMDB identifier of the target movie.
     * @return A cold [Flow] emitting the retrieval lifecycle state encapsulated within [Resource].
     */
    operator fun invoke(movieId: Int): Flow<Resource<MovieDetail>> = movieRepository.getMovieDetail(movieId)
}
