package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaEntityMapperTest {

    @Test
    fun `MovieItemDto toEntity maps all fields accurately with defaults`() {
        val dto = MovieItemDto(
            id = 101,
            title = "Dune: Part Two",
            posterPath = "/dune2.jpg",
            backdropPath = "/dune2_back.jpg",
            releaseDate = "2024-03-01",
            voteAverage = 8.24,
            voteCount = 4500,
            overview = "Paul Atreides unites with Chani..."
        )

        val entity = dto.toEntity(mediaType = "MOVIE", pageIndex = 1)

        assertEquals(101, entity.id)
        assertEquals("MOVIE", entity.mediaType)
        assertEquals("Dune: Part Two", entity.title)
        assertEquals("/dune2.jpg", entity.posterPath)
        assertEquals("/dune2_back.jpg", entity.backdropPath)
        assertEquals("2024-03-01", entity.releaseDate)
        assertEquals(8.24, entity.voteAverage, 0.001)
        assertEquals(4500, entity.voteCount)
        assertEquals("Paul Atreides unites with Chani...", entity.overview)
        assertEquals(1, entity.pageIndex)
    }

    @Test
    fun `MovieItemDto toEntity handles null and blank fields defensively`() {
        val dto = MovieItemDto(
            id = 102,
            title = "   ",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = null,
            voteCount = null,
            overview = null
        )

        val entity = dto.toEntity(mediaType = "MOVIE", pageIndex = 2)

        assertEquals(102, entity.id)
        assertEquals("Unknown", entity.title)
        assertNull(entity.posterPath)
        assertNull(entity.backdropPath)
        assertNull(entity.releaseDate)
        assertEquals(0.0, entity.voteAverage, 0.001)
        assertEquals(0, entity.voteCount)
        assertEquals("", entity.overview)
        assertEquals(2, entity.pageIndex)
    }

    @Test
    fun `TvSeriesItemDto toEntity maps all fields accurately`() {
        val dto = TvSeriesItemDto(
            id = 201,
            name = "Shogun",
            posterPath = "/shogun.jpg",
            backdropPath = "/shogun_back.jpg",
            firstAirDate = "2024-02-27",
            voteAverage = 8.58,
            voteCount = 1200,
            overview = "When a mysterious European ship is found..."
        )

        val entity = dto.toEntity(mediaType = "TV", pageIndex = 1)

        assertEquals(201, entity.id)
        assertEquals("TV", entity.mediaType)
        assertEquals("Shogun", entity.title)
        assertEquals("/shogun.jpg", entity.posterPath)
        assertEquals("/shogun_back.jpg", entity.backdropPath)
        assertEquals("2024-02-27", entity.releaseDate)
        assertEquals(8.58, entity.voteAverage, 0.001)
        assertEquals(1200, entity.voteCount)
        assertEquals("When a mysterious European ship is found...", entity.overview)
        assertEquals(1, entity.pageIndex)
    }

    @Test
    fun `TvSeriesItemDto toEntity handles null and blank fields defensively`() {
        val dto = TvSeriesItemDto(
            id = 202,
            name = "",
            posterPath = null,
            backdropPath = null,
            firstAirDate = null,
            voteAverage = null,
            voteCount = null,
            overview = null
        )

        val entity = dto.toEntity(mediaType = "TV", pageIndex = 3)

        assertEquals(202, entity.id)
        assertEquals("Unknown", entity.title)
        assertNull(entity.posterPath)
        assertNull(entity.releaseDate)
        assertEquals(0.0, entity.voteAverage, 0.001)
        assertEquals(0, entity.voteCount)
        assertEquals("", entity.overview)
        assertEquals(3, entity.pageIndex)
    }

    @Test
    fun `MediaItemEntity toMovie transforms accurately with CDN URLs and formatting`() {
        val entity = MediaItemEntity(
            id = 101,
            mediaType = "MOVIE",
            title = "Inception",
            posterPath = "/inception.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2010-07-16",
            voteAverage = 8.357,
            voteCount = 35000,
            overview = "A thief who steals corporate secrets...",
            pageIndex = 1
        )

        val movie = entity.toMovie()

        assertEquals(101, movie.id)
        assertEquals("Inception", movie.title)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w185/inception.jpg", movie.posterUrl)
        assertEquals("2010", movie.releaseYear)
        assertEquals(8.4, movie.voteAverage, 0.01)
    }

    @Test
    fun `MediaItemEntity toMovie handles missing posters and invalid dates defensively`() {
        val entity = MediaItemEntity(
            id = 102,
            mediaType = "MOVIE",
            title = "Untitled Film",
            posterPath = "   ",
            backdropPath = null,
            releaseDate = "invalid-date",
            voteAverage = -1.0,
            voteCount = 0,
            overview = "",
            pageIndex = 1
        )

        val movie = entity.toMovie()

        assertEquals(102, movie.id)
        assertEquals("Untitled Film", movie.title)
        assertNull("Blank poster path must map to null", movie.posterUrl)
        assertEquals("N/A", movie.releaseYear)
        assertEquals(0.0, movie.voteAverage, 0.01)
    }

    @Test
    fun `MediaItemEntity toTvSeries transforms accurately with CDN URLs and premiere year`() {
        val entity = MediaItemEntity(
            id = 201,
            mediaType = "TV",
            title = "Breaking Bad",
            posterPath = "/breaking_bad.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2008-01-20",
            voteAverage = 9.489,
            voteCount = 15000,
            overview = "A high school chemistry teacher diagnosed with lung cancer...",
            pageIndex = 1
        )

        val tvSeries = entity.toTvSeries()

        assertEquals(201, tvSeries.id)
        assertEquals("Breaking Bad", tvSeries.name)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w185/breaking_bad.jpg", tvSeries.posterUrl)
        assertEquals("2008", tvSeries.firstAirYear)
        assertEquals(9.5, tvSeries.voteAverage, 0.01)
    }

    @Test
    fun `MediaItemEntity toTvSeries handles null poster and null release date`() {
        val entity = MediaItemEntity(
            id = 202,
            mediaType = "TV",
            title = "Mystery Series",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = 7.0,
            voteCount = 100,
            overview = "",
            pageIndex = 1
        )

        val tvSeries = entity.toTvSeries()

        assertEquals(202, tvSeries.id)
        assertEquals("Mystery Series", tvSeries.name)
        assertNull(tvSeries.posterUrl)
        assertEquals("N/A", tvSeries.firstAirYear)
        assertEquals(7.0, tvSeries.voteAverage, 0.01)
    }
}
