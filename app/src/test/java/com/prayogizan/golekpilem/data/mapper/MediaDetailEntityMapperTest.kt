package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.local.entity.MediaDetailEntity
import com.prayogizan.golekpilem.data.remote.dto.AuthorDetailsDto
import com.prayogizan.golekpilem.data.remote.dto.GenreDto
import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.ReviewDto
import com.prayogizan.golekpilem.data.remote.dto.ReviewsResponseDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import com.prayogizan.golekpilem.data.remote.dto.VideoDto
import com.prayogizan.golekpilem.data.remote.dto.VideosResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaDetailEntityMapperTest {

    @Test
    fun `MovieDetailDto toEntity maps all fields, serializes genres, reviews, and trailer accurately`() {
        val dto = MovieDetailDto(
            id = 550,
            title = "Fight Club",
            tagline = "Mischief. Mayhem. Soap.",
            overview = "An unfulfilled clerk...",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            backdropPath = "/hZkgoQYus5vegHoetLkCJzb17zJ.jpg",
            releaseDate = "1999-10-15",
            runtime = 139,
            voteAverage = 8.433,
            voteCount = 28000,
            status = "Released",
            genres = listOf(GenreDto(id = 18, name = "Drama")),
            reviews = ReviewsResponseDto(
                page = 1,
                results = listOf(
                    ReviewDto(
                        id = "rev-1",
                        author = "Brad",
                        content = "Great movie!",
                        authorDetails = AuthorDetailsDto(name = "Brad Pitt", rating = 9.0)
                    )
                ),
                totalPages = 1,
                totalResults = 1
            ),
            videos = VideosResponseDto(
                results = listOf(
                    VideoDto(
                        id = "vid-1",
                        key = "O-b2VfMmbyA",
                        name = "Official Trailer",
                        site = "YouTube",
                        type = "Trailer",
                        official = true
                    )
                )
            )
        )

        val fixedTime = 1700000000000L
        val entity = dto.toEntity(mediaType = "MOVIE", cachedAt = fixedTime)

        assertEquals(550, entity.id)
        assertEquals("MOVIE", entity.mediaType)
        assertEquals("Fight Club", entity.title)
        assertEquals("An unfulfilled clerk...", entity.overview)
        assertEquals("/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", entity.posterPath)
        assertEquals("/hZkgoQYus5vegHoetLkCJzb17zJ.jpg", entity.backdropPath)
        assertEquals(8.4, entity.voteAverage, 0.001)
        assertEquals(28000, entity.voteCount)
        assertEquals("1999-10-15", entity.releaseDate)
        assertEquals("139 min", entity.runtimeOrSeasons)
        assertEquals("Released", entity.status)
        assertEquals("O-b2VfMmbyA", entity.trailerKey)
        assertEquals("Official Trailer", entity.trailerTitle)
        assertEquals(fixedTime, entity.cachedAt)
        assertTrue(entity.genresJson.contains("Drama"))
        assertTrue(entity.reviewsJson.contains("Great movie!"))
    }

    @Test
    fun `MediaDetailEntity toMovieDetail reconstructs full domain model with isOfflineCache`() {
        val entity = MediaDetailEntity(
            id = 550,
            mediaType = "MOVIE",
            title = "Fight Club",
            overview = "An unfulfilled clerk...",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            voteAverage = 8.4,
            voteCount = 28000,
            releaseDate = "1999-10-15",
            runtimeOrSeasons = "139 min",
            genresJson = "[\"Drama\", \"Thriller\"]",
            status = "Released",
            trailerKey = "trailer123",
            trailerTitle = "Official Trailer",
            reviewsJson = "[{\"id\":\"rev-1\",\"author\":\"Brad\",\"content\":\"Classic\",\"rating\":9.0,\"avatar_url\":null,\"created_at\":\"1999-10-20\"}]",
            cachedAt = System.currentTimeMillis()
        )

        val domain = entity.toMovieDetail(isOfflineCache = true)

        assertEquals(550, domain.id)
        assertEquals("Fight Club", domain.title)
        assertEquals("An unfulfilled clerk...", domain.overview)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w342/poster.jpg", domain.posterUrl)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w780/backdrop.jpg", domain.backdropUrl)
        assertEquals("1999-10-15", domain.releaseDate)
        assertEquals(139, domain.runtime)
        assertEquals(8.4, domain.voteAverage, 0.01)
        assertEquals(28000, domain.voteCount)
        assertEquals(2, domain.genres.size)
        assertEquals("Drama", domain.genres[0].name)
        assertEquals(1, domain.reviews.size)
        assertEquals("Brad", domain.reviews[0].author)
        assertEquals(1, domain.videos.size)
        assertEquals("trailer123", domain.videos[0].key)
        assertTrue("isOfflineCache should be true", domain.isOfflineCache)
    }

    @Test
    fun `TvSeriesDetailDto toEntity and MediaDetailEntity toTvSeriesDetail roundtrip accurately`() {
        val dto = TvSeriesDetailDto(
            id = 1399,
            name = "Game of Thrones",
            overview = "Seven noble families fight for control...",
            posterPath = "/got.jpg",
            backdropPath = "/got_back.jpg",
            firstAirDate = "2011-04-17",
            numberOfSeasons = 8,
            numberOfEpisodes = 73,
            voteAverage = 8.441,
            voteCount = 23000,
            status = "Ended",
            genres = listOf(GenreDto(id = 10765, name = "Sci-Fi & Fantasy")),
            videos = VideosResponseDto(
                results = listOf(
                    VideoDto(
                        id = "vid-got",
                        key = "KPLWWIOCOOQ",
                        name = "GoT Trailer",
                        site = "YouTube",
                        type = "Trailer"
                    )
                )
            )
        )

        val entity = dto.toEntity(mediaType = "TV", cachedAt = 1000L)
        assertEquals(1399, entity.id)
        assertEquals("TV", entity.mediaType)
        assertEquals("Game of Thrones", entity.title)
        assertEquals("8 Seasons", entity.runtimeOrSeasons)
        assertEquals("KPLWWIOCOOQ", entity.trailerKey)

        val domain = entity.toTvSeriesDetail(isOfflineCache = true)
        assertEquals(1399, domain.id)
        assertEquals("Game of Thrones", domain.name)
        assertEquals("2011-04-17", domain.firstAirDate)
        assertEquals(8, domain.numberOfSeasons)
        assertEquals(8.4, domain.voteAverage, 0.01)
        assertEquals("Sci-Fi & Fantasy", domain.genres[0].name)
        assertEquals(1, domain.videos.size)
        assertEquals("KPLWWIOCOOQ", domain.videos[0].key)
        assertTrue(domain.isOfflineCache)
    }

    @Test
    fun `toMovieDetail handles null and empty trailer and reviews gracefully`() {
        val entity = MediaDetailEntity(
            id = 123,
            mediaType = "MOVIE",
            title = "Indie Short",
            overview = "Brief film",
            posterPath = null,
            backdropPath = null,
            voteAverage = 7.0,
            voteCount = 10,
            releaseDate = null,
            runtimeOrSeasons = "45 min",
            genresJson = "[]",
            status = "Released",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = 0L
        )

        val domain = entity.toMovieDetail(isOfflineCache = false)

        assertNull(domain.posterUrl)
        assertNull(domain.backdropUrl)
        assertEquals("N/A", domain.releaseDate)
        assertEquals(45, domain.runtime)
        assertTrue(domain.genres.isEmpty())
        assertTrue(domain.reviews.isEmpty())
        assertTrue(domain.videos.isEmpty())
        assertFalse(domain.isOfflineCache)
    }
}
