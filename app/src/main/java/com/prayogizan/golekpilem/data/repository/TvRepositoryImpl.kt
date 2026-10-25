package com.prayogizan.golekpilem.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.prayogizan.golekpilem.core.util.Resource
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.data.mapper.toDomain
import com.prayogizan.golekpilem.data.mapper.toEntity
import com.prayogizan.golekpilem.data.mapper.toTvSeries
import com.prayogizan.golekpilem.data.mapper.toTvSeriesDetail
import com.prayogizan.golekpilem.data.paging.MediaRemoteMediator
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import com.prayogizan.golekpilem.domain.model.TvSeries
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import com.prayogizan.golekpilem.domain.repository.TvRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of [TvRepository] coordinating remote TMDB network interactions,
 * Paging 3 source creation, and detail stream emission with lifecycle-aware error encapsulation.
 *
 * Employs a Stale-While-Revalidate caching engine backed by Room's [com.prayogizan.golekpilem.data.local.dao.MediaDetailDao].
 *
 * @property apiService The Retrofit API service abstraction for TMDB.
 * @property database The primary Room database providing local persistence and Single Source of Truth caching.
 * @property ioDispatcher Coroutine dispatcher managing background thread execution (defaults to [Dispatchers.IO]).
 */
class TvRepositoryImpl(
    private val apiService: TmdbApiService,
    private val database: GolekPilemDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TvRepository {

    private val pagingConfig = PagingConfig(
        pageSize = 20,
        prefetchDistance = 5,
        initialLoadSize = 20,
        enablePlaceholders = false
    )

    @OptIn(ExperimentalPagingApi::class)
    override fun getTvSeries(): Flow<PagingData<TvSeries>> {
        return Pager(
            config = pagingConfig,
            remoteMediator = MediaRemoteMediator(
                apiService = apiService,
                database = database,
                mediaType = MediaRemoteMediator.MEDIA_TYPE_TV
            ),
            pagingSourceFactory = {
                database.mediaDao().getPagedMedia(MediaRemoteMediator.MEDIA_TYPE_TV)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toTvSeries() }
        }
    }

    override fun getTvSeriesDetail(seriesId: Int): Flow<Resource<TvSeriesDetail>> = flow {
        emit(Resource.Loading)

        // 1. Query local Room cache
        val cachedEntity = database.mediaDetailDao().getDetail(seriesId, "TV")
        if (cachedEntity != null) {
            emit(Resource.Success(cachedEntity.toTvSeriesDetail(isOfflineCache = true)))
        }

        // 2. Evaluate cache freshness
        val isStale = cachedEntity == null || cachedEntity.isExpired()

        // 3. If cache is fresh, terminate flow early to prevent redundant network hit
        if (cachedEntity != null && !isStale) {
            return@flow
        }

        // 4. Stale cache or cache miss: retrieve fresh details from TMDB network
        try {
            val detailDto = apiService.getTvSeriesDetail(
                seriesId = seriesId,
                appendToResponse = "credits,reviews,videos"
            )
            database.mediaDetailDao().insertDetail(detailDto.toEntity(mediaType = "TV"))
            emit(Resource.Success(detailDto.toDomain(isOfflineCache = false)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 5. Offline fallback: only emit error if no cached version was previously emitted
            if (cachedEntity == null) {
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred", cause = e))
            }
        }
    }.flowOn(ioDispatcher)
}

