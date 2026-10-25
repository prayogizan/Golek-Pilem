package com.prayogizan.golekpilem.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity
import com.prayogizan.golekpilem.data.local.entity.RemoteKeyEntity
import com.prayogizan.golekpilem.data.mapper.toEntity
import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Paging 3 [RemoteMediator] coordinating network-to-database synchronization for media catalog discovery feeds.
 *
 * Utilizes AndroidX Room as the Single Source of Truth (SSOT), persisting network results in atomic transactions,
 * managing cursor tracking via [RemoteKeyEntity], and enforcing a 4-hour cache freshness window.
 *
 * @property apiService The Retrofit service abstraction used for remote data discovery.
 * @property database The primary Room database managing cache tables and transactions.
 * @property mediaType The media classification identifier ("MOVIE" or "TV").
 */
@OptIn(ExperimentalPagingApi::class)
class MediaRemoteMediator(
    private val apiService: TmdbApiService,
    private val database: GolekPilemDatabase,
    private val mediaType: String
) : RemoteMediator<Int, MediaItemEntity>() {

    companion object {
        /**
         * Media classification constant for movie discovery feeds.
         */
        const val MEDIA_TYPE_MOVIE = "MOVIE"

        /**
         * Media classification constant for television series discovery feeds.
         */
        const val MEDIA_TYPE_TV = "TV"

        /**
         * TMDB standard starting page index.
         */
        const val TMDB_STARTING_PAGE_INDEX = 1

        /**
         * Maximum duration (4 hours) that cached media items remain fresh before requiring a network refresh.
         */
        val CACHE_TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(4)
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MediaItemEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKey = getRemoteKeyClosestToPosition(state)
                        ?: getRemoteKeyForFirstItem(state)

                    if (remoteKey != null) {
                        val cacheAge = System.currentTimeMillis() - remoteKey.lastUpdated
                        if (cacheAge < CACHE_TIMEOUT_MILLIS) {
                            return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }
                    TMDB_STARTING_PAGE_INDEX
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    val remoteKey = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKey?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                    nextKey
                }
            }

            val (entities, totalPages) = when (mediaType) {
                MEDIA_TYPE_MOVIE -> {
                    val response = apiService.getMovies(page = page)
                    val mapped = response.results.map { it.toEntity(mediaType, page) }
                    mapped to response.totalPages
                }
                MEDIA_TYPE_TV -> {
                    val response = apiService.getTvSeries(page = page)
                    val mapped = response.results.map { it.toEntity(mediaType, page) }
                    mapped to response.totalPages
                }
                else -> throw IllegalArgumentException("Unsupported media type: $mediaType")
            }

            val endOfPaginationReached = entities.isEmpty() || page >= totalPages

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeyDao().clearKeys(mediaType)
                    database.mediaDao().clearByMediaType(mediaType)
                }

                val prevPage = if (page == TMDB_STARTING_PAGE_INDEX) null else page - 1
                val nextPage = if (endOfPaginationReached) null else page + 1
                val now = System.currentTimeMillis()

                val keys = entities.map {
                    RemoteKeyEntity(
                        id = it.id,
                        mediaType = mediaType,
                        prevKey = prevPage,
                        nextKey = nextPage,
                        lastUpdated = now
                    )
                }

                database.remoteKeyDao().insertKeys(keys)
                database.mediaDao().insertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyClosestToPosition(state: PagingState<Int, MediaItemEntity>): RemoteKeyEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeyDao().getRemoteKey(id = id, mediaType = mediaType)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, MediaItemEntity>): RemoteKeyEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { item ->
            database.remoteKeyDao().getRemoteKey(id = item.id, mediaType = mediaType)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, MediaItemEntity>): RemoteKeyEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { item ->
            database.remoteKeyDao().getRemoteKey(id = item.id, mediaType = mediaType)
        }
    }
}
