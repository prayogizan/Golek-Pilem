package com.prayogizan.golekpilem.domain.repository

import androidx.paging.PagingData
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.MovieDetail
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction defining access contracts for movie catalog pagination and entity details.
 *
 * Implements the Dependency Inversion Principle, decoupling presentation and domain policies
 * from underlying remote network APIs and local persistence frameworks.
 */
interface MovieRepository {

    /**
     * Streams paginated popular movies from the underlying data source.
     *
     * @return A cold [Flow] emitting reactive [PagingData] containing domain [Movie] entities.
     */
    fun getMovies(): Flow<PagingData<Movie>>

    /**
     * Retrieves comprehensive detailed information for a specific movie by its unique identifier.
     *
     * Emits a lifecycle stream starting with [Resource.Loading], resolving to [Resource.Success] on success,
     * or [Resource.Error] with sanitized error messages on failure.
     *
     * @param movieId The unique TMDB identifier of the target movie.
     * @param forceRefresh Whether to bypass valid local cache and force remote TMDB retrieval.
     * @return A cold [Flow] emitting the retrieval lifecycle state encapsulated within [Resource].
     */
    fun getMovieDetail(movieId: Int, forceRefresh: Boolean = false): Flow<Resource<MovieDetail>>
}
