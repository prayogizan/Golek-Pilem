package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.remote.dto.GenreDto
import com.prayogizan.golekpilem.data.remote.dto.MovieDetailDto
import com.prayogizan.golekpilem.data.remote.dto.MovieItemDto
import com.prayogizan.golekpilem.data.remote.dto.ProductionCompanyDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMapperTest {

    @Test
    fun `toDomain maps complete MovieItemDto correctly`() {
        val dto = MovieItemDto(
            id = 550,
            title = "Fight Club",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            backdropPath = "/hZkgoQYus5vegHoetLkCJzb17zJ.jpg",
            releaseDate = "1999-10-15",
            voteAverage = 8.433,
            voteCount = 26000,
            overview = "An insomniac office worker..."
        )

        val domain = dto.toDomain()

        assertEquals(550, domain.id)
        assertEquals("Fight Club", domain.title)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w185/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", domain.posterUrl)
        assertEquals("1999", domain.releaseYear)
        assertEquals(8.4, domain.voteAverage, 0.001)
    }

    @Test
    fun `toDomain handles null or empty fields in MovieItemDto defensively`() {
        val dto = MovieItemDto(
            id = 100,
            title = null,
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            voteAverage = null,
            voteCount = null,
            overview = null
        )

        val domain = dto.toDomain()

        assertEquals(100, domain.id)
        assertEquals("Unknown", domain.title)
        assertNull(domain.posterUrl)
        assertEquals("N/A", domain.releaseYear)
        assertEquals(0.0, domain.voteAverage, 0.001)
    }

    @Test
    fun `toDomain handles blank strings in MovieItemDto`() {
        val dto = MovieItemDto(
            id = 101,
            title = "   ",
            posterPath = "",
            releaseDate = "   ",
            voteAverage = -2.0
        )

        val domain = dto.toDomain()

        assertEquals("Unknown", domain.title)
        assertNull(domain.posterUrl)
        assertEquals("N/A", domain.releaseYear)
        assertEquals(0.0, domain.voteAverage, 0.001)
    }

    @Test
    fun `toDomain maps complete MovieDetailDto correctly`() {
        val genreDto = GenreDto(id = 18, name = "Drama")
        val companyDto = ProductionCompanyDto(id = 508, name = "Regency Enterprises", logoPath = "/7Pz205nTeZAgWvM0qZf8mD7y0Fk.png")

        val dto = MovieDetailDto(
            id = 550,
            title = "Fight Club",
            tagline = "Mischief. Mayhem. Soap.",
            overview = "A ticking-time-bomb insomniac...",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            backdropPath = "/hZkgoQYus5vegHoetLkCJzb17zJ.jpg",
            releaseDate = "1999-10-15",
            runtime = 139,
            voteAverage = 8.433,
            voteCount = 26000,
            genres = listOf(genreDto),
            productionCompanies = listOf(companyDto),
            status = "Released"
        )

        val domain = dto.toDomain()

        assertEquals(550, domain.id)
        assertEquals("Fight Club", domain.title)
        assertEquals("Mischief. Mayhem. Soap.", domain.tagline)
        assertEquals("A ticking-time-bomb insomniac...", domain.overview)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w342/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", domain.posterUrl)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w780/hZkgoQYus5vegHoetLkCJzb17zJ.jpg", domain.backdropUrl)
        assertEquals("1999-10-15", domain.releaseDate)
        assertEquals(139, domain.runtime)
        assertEquals(8.4, domain.voteAverage, 0.001)
        assertEquals(26000, domain.voteCount)
        assertEquals(1, domain.genres.size)
        assertEquals("Drama", domain.genres.first().name)
        assertEquals(1, domain.productionCompanies.size)
        assertEquals("Regency Enterprises", domain.productionCompanies.first().name)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w185/7Pz205nTeZAgWvM0qZf8mD7y0Fk.png", domain.productionCompanies.first().logoUrl)
        assertEquals("Released", domain.status)
    }

    @Test
    fun `toDomain handles null and blank fields in MovieDetailDto defensively`() {
        val dto = MovieDetailDto(
            id = 200,
            title = null,
            tagline = null,
            overview = null,
            posterPath = null,
            backdropPath = "",
            releaseDate = null,
            runtime = null,
            voteAverage = null,
            voteCount = null,
            genres = emptyList(),
            productionCompanies = emptyList(),
            status = null
        )

        val domain = dto.toDomain()

        assertEquals(200, domain.id)
        assertEquals("Unknown", domain.title)
        assertEquals("", domain.tagline)
        assertEquals("", domain.overview)
        assertNull(domain.posterUrl)
        assertNull(domain.backdropUrl)
        assertEquals("N/A", domain.releaseDate)
        assertEquals(0, domain.runtime)
        assertEquals(0.0, domain.voteAverage, 0.001)
        assertEquals(0, domain.voteCount)
        assertEquals("Unknown", domain.status)
    }

    @Test
    fun `formatReleaseYear extracts 4-digit year or returns fallback`() {
        assertEquals("2026", formatReleaseYear("2026-10-24"))
        assertEquals("1994", formatReleaseYear("1994-09-23"))
        assertEquals("N/A", formatReleaseYear(null))
        assertEquals("N/A", formatReleaseYear(""))
        assertEquals("N/A", formatReleaseYear("   "))
        assertEquals("N/A", formatReleaseYear("invalid"))
        assertEquals("N/A", formatReleaseYear("99-12-01"))
    }

    @Test
    fun `sanitizeVoteAverage rounds to 1 decimal place with proper edge handling`() {
        assertEquals(7.8, sanitizeVoteAverage(7.842), 0.001)
        assertEquals(7.9, sanitizeVoteAverage(7.86), 0.001)
        assertEquals(8.0, sanitizeVoteAverage(8.0), 0.001)
        assertEquals(0.0, sanitizeVoteAverage(null), 0.001)
        assertEquals(0.0, sanitizeVoteAverage(-1.0), 0.001)
    }
}
