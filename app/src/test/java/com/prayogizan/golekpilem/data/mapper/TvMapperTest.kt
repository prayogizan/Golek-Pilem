package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.remote.dto.GenreDto
import com.prayogizan.golekpilem.data.remote.dto.NetworkDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesDetailDto
import com.prayogizan.golekpilem.data.remote.dto.TvSeriesItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TvMapperTest {

    @Test
    fun `toDomain maps complete TvSeriesItemDto correctly`() {
        val dto = TvSeriesItemDto(
            id = 1399,
            name = "Game of Thrones",
            posterPath = "/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
            backdropPath = "/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg",
            firstAirDate = "2011-04-17",
            voteAverage = 8.452,
            voteCount = 22000,
            overview = "Seven noble families fight for control..."
        )

        val domain = dto.toDomain()

        assertEquals(1399, domain.id)
        assertEquals("Game of Thrones", domain.name)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w185/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg", domain.posterUrl)
        assertEquals("2011", domain.firstAirYear)
        assertEquals(8.5, domain.voteAverage, 0.001)
    }

    @Test
    fun `toDomain handles null or empty fields in TvSeriesItemDto defensively`() {
        val dto = TvSeriesItemDto(
            id = 200,
            name = null,
            posterPath = null,
            backdropPath = null,
            firstAirDate = null,
            voteAverage = null,
            voteCount = null,
            overview = null
        )

        val domain = dto.toDomain()

        assertEquals(200, domain.id)
        assertEquals("Unknown", domain.name)
        assertNull(domain.posterUrl)
        assertEquals("N/A", domain.firstAirYear)
        assertEquals(0.0, domain.voteAverage, 0.001)
    }

    @Test
    fun `toDomain maps complete TvSeriesDetailDto correctly`() {
        val genreDto = GenreDto(id = 10765, name = "Sci-Fi & Fantasy")
        val networkDto = NetworkDto(id = 49, name = "HBO", logoPath = "/tuomgit2Bb48F6WRMiVoPD2zZs8.png")

        val dto = TvSeriesDetailDto(
            id = 1399,
            name = "Game of Thrones",
            tagline = "Winter Is Coming",
            overview = "Seven noble families fight for control of the mythical land of Westeros.",
            posterPath = "/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
            backdropPath = "/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg",
            firstAirDate = "2011-04-17",
            lastAirDate = "2019-05-19",
            numberOfSeasons = 8,
            numberOfEpisodes = 73,
            voteAverage = 8.452,
            voteCount = 22000,
            genres = listOf(genreDto),
            networks = listOf(networkDto),
            status = "Ended"
        )

        val domain = dto.toDomain()

        assertEquals(1399, domain.id)
        assertEquals("Game of Thrones", domain.name)
        assertEquals("Winter Is Coming", domain.tagline)
        assertEquals("Seven noble families fight for control of the mythical land of Westeros.", domain.overview)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w342/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg", domain.posterUrl)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w780/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg", domain.backdropUrl)
        assertEquals("2011-04-17", domain.firstAirDate)
        assertEquals("2019-05-19", domain.lastAirDate)
        assertEquals(8, domain.numberOfSeasons)
        assertEquals(73, domain.numberOfEpisodes)
        assertEquals(8.5, domain.voteAverage, 0.001)
        assertEquals(22000, domain.voteCount)
        assertEquals(1, domain.genres.size)
        assertEquals("Sci-Fi & Fantasy", domain.genres.first().name)
        assertEquals(1, domain.networks.size)
        assertEquals("HBO", domain.networks.first().name)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w185/tuomgit2Bb48F6WRMiVoPD2zZs8.png", domain.networks.first().logoUrl)
        assertEquals("Ended", domain.status)
    }

    @Test
    fun `toDomain handles null and blank fields in TvSeriesDetailDto defensively`() {
        val dto = TvSeriesDetailDto(
            id = 300,
            name = "   ",
            tagline = null,
            overview = null,
            posterPath = "",
            backdropPath = null,
            firstAirDate = null,
            lastAirDate = "",
            numberOfSeasons = null,
            numberOfEpisodes = null,
            voteAverage = null,
            voteCount = null,
            genres = emptyList(),
            networks = emptyList(),
            status = null
        )

        val domain = dto.toDomain()

        assertEquals(300, domain.id)
        assertEquals("Unknown", domain.name)
        assertEquals("", domain.tagline)
        assertEquals("", domain.overview)
        assertNull(domain.posterUrl)
        assertNull(domain.backdropUrl)
        assertEquals("N/A", domain.firstAirDate)
        assertEquals("N/A", domain.lastAirDate)
        assertEquals(0, domain.numberOfSeasons)
        assertEquals(0, domain.numberOfEpisodes)
        assertEquals(0.0, domain.voteAverage, 0.001)
        assertEquals(0, domain.voteCount)
        assertEquals("Unknown", domain.status)
    }

    @Test
    fun `toDomain maps NetworkDto correctly with defensive fallbacks`() {
        val dtoWithLogo = NetworkDto(id = 1, name = "Netflix", logoPath = "/wwemzKWzjKYJFfCeiB57q3r4Bcm.png")
        val domainWithLogo = dtoWithLogo.toDomain()
        assertEquals("Netflix", domainWithLogo.name)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w185/wwemzKWzjKYJFfCeiB57q3r4Bcm.png", domainWithLogo.logoUrl)

        val dtoWithoutLogo = NetworkDto(id = 2, name = "", logoPath = null)
        val domainWithoutLogo = dtoWithoutLogo.toDomain()
        assertEquals("Unknown", domainWithoutLogo.name)
        assertNull(domainWithoutLogo.logoUrl)
    }
}
