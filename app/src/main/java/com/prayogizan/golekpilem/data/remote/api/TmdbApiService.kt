package com.prayogizan.golekpilem.data.remote.api

import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.PaginatedResponseDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit REST service interface defining TMDB API endpoints for discovering and retrieving details
 * for movies and television series.
 */
interface TmdbApiService {

    /**
     * Discovers movies ordered by popularity with pagination support.
     *
     * @param page The 1-based page number to fetch.
     * @param sortBy Sorting criteria (defaults to "popularity.desc").
     * @param language ISO 639-1 language code (defaults to "en-US").
     * @return [PaginatedResponseDto] containing a page of [MovieItemDto] items.
     */
    @GET("discover/movie")
    suspend fun getMovies(
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("language") language: String = "en-US"
    ): PaginatedResponseDto<MovieItemDto>

    /**
     * Discovers television series ordered by popularity with pagination support.
     *
     * @param page The 1-based page number to fetch.
     * @param sortBy Sorting criteria (defaults to "popularity.desc").
     * @param language ISO 639-1 language code (defaults to "en-US").
     * @return [PaginatedResponseDto] containing a page of [TvSeriesItemDto] items.
     */
    @GET("discover/tv")
    suspend fun getTvSeries(
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("language") language: String = "en-US"
    ): PaginatedResponseDto<TvSeriesItemDto>

    /**
     * Fetches detailed information for a specific movie by its TMDB identifier.
     *
     * @param movieId The unique identifier of the movie.
     * @param appendToResponse Comma-separated list of additional sub-resources to append (defaults to "credits").
     * @return [MovieDetailDto] representing full movie details.
     */
    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits"
    ): MovieDetailDto

    /**
     * Fetches detailed information for a specific television series by its TMDB identifier.
     *
     * @param seriesId The unique identifier of the television series.
     * @param appendToResponse Comma-separated list of additional sub-resources to append (defaults to "credits").
     * @return [TvSeriesDetailDto] representing full television series details.
     */
    @GET("tv/{series_id}")
    suspend fun getTvSeriesDetail(
        @Path("series_id") seriesId: Int,
        @Query("append_to_response") appendToResponse: String = "credits"
    ): TvSeriesDetailDto
}
