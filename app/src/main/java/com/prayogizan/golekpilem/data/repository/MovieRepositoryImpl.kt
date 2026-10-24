package com.prayogizan.golekpilem.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.data.mapper.toDomain
import com.prayogizan.golekpilem.data.paging.MoviePagingSource
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.domain.model.Movie
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Concrete implementation of [MovieRepository] coordinating remote TMDB network interactions,
 * Paging 3 source creation, and detail stream emission with lifecycle-aware error encapsulation.
 *
 * @property apiService The Retrofit API service abstraction for TMDB.
 * @property ioDispatcher Coroutine dispatcher managing background thread execution (defaults to [Dispatchers.IO]).
 */
class MovieRepositoryImpl(
    private val apiService: TmdbApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MovieRepository {

    private val pagingConfig = PagingConfig(
        pageSize = 20,
        prefetchDistance = 5,
        initialLoadSize = 20,
        enablePlaceholders = false
    )

    override fun getMovies(): Flow<PagingData<Movie>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = { MoviePagingSource(apiService) }
        ).flow
    }

    override fun getMovieDetail(movieId: Int): Flow<Resource<MovieDetail>> = flow {
        emit(Resource.Loading)
        try {
            val detailDto = apiService.getMovieDetail(
                movieId = movieId,
                appendToResponse = "credits,reviews,videos"
            )
            emit(Resource.Success(detailDto.toDomain()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred", cause = e))
        }
    }.flowOn(ioDispatcher)
}
