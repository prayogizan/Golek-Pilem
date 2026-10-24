package com.prayogizan.golekpilem.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbDtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun `deserialize PaginatedResponseDto with MovieItemDto and ignore unknown keys`() {
        val rawJson = """
            {
                "page": 1,
                "results": [
                    {
                        "id": 550,
                        "title": "Fight Club",
                        "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
                        "backdrop_path": "/hZkgoQYus5vegHoetLkCJzb17zJ.jpg",
                        "release_date": "1999-10-15",
                        "vote_average": 8.433,
                        "vote_count": 26000,
                        "overview": "An insomniac office worker...",
                        "adult": false,
                        "original_language": "en",
                        "popularity": 61.416
                    }
                ],
                "total_pages": 500,
                "total_results": 10000,
                "extra_metadata": "ignored"
            }
        """.trimIndent()

        val response = json.decodeFromString<PaginatedResponseDto<MovieItemDto>>(rawJson)

        assertEquals(1, response.page)
        assertEquals(500, response.totalPages)
        assertEquals(10000, response.totalResults)
        assertEquals(1, response.results.size)

        val item = response.results.first()
        assertEquals(550, item.id)
        assertEquals("Fight Club", item.title)
        assertEquals("/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", item.posterPath)
        assertEquals(8.433, item.voteAverage)
        assertEquals(26000, item.voteCount)
    }

    @Test
    fun `deserialize PaginatedResponseDto with TvSeriesItemDto`() {
        val rawJson = """
            {
                "page": 2,
                "results": [
                    {
                        "id": 1399,
                        "name": "Game of Thrones",
                        "poster_path": "/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
                        "backdrop_path": "/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg",
                        "first_air_date": "2011-04-17",
                        "vote_average": 8.452,
                        "vote_count": 22000,
                        "overview": "Seven noble families...",
                        "origin_country": ["US"]
                    }
                ],
                "total_pages": 100,
                "total_results": 2000
            }
        """.trimIndent()

        val response = json.decodeFromString<PaginatedResponseDto<TvSeriesItemDto>>(rawJson)

        assertEquals(2, response.page)
        assertEquals(1, response.results.size)
        val tv = response.results.first()
        assertEquals(1399, tv.id)
        assertEquals("Game of Thrones", tv.name)
        assertEquals("2011-04-17", tv.firstAirDate)
    }

    @Test
    fun `deserialize MovieDetailDto with nested objects and unknown keys`() {
        val rawJson = """
            {
                "id": 550,
                "title": "Fight Club",
                "tagline": "Mischief. Mayhem. Soap.",
                "overview": "A ticking-time-bomb insomniac...",
                "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
                "backdrop_path": "/hZkgoQYus5vegHoetLkCJzb17zJ.jpg",
                "release_date": "1999-10-15",
                "runtime": 139,
                "vote_average": 8.433,
                "vote_count": 26000,
                "genres": [
                    { "id": 18, "name": "Drama" }
                ],
                "production_companies": [
                    { "id": 508, "name": "Regency Enterprises", "logo_path": "/7Pz205nTeZAgWvM0qZf8mD7y0Fk.png" }
                ],
                "status": "Released",
                "budget": 63000000,
                "revenue": 100853753
            }
        """.trimIndent()

        val detail = json.decodeFromString<MovieDetailDto>(rawJson)

        assertEquals(550, detail.id)
        assertEquals("Fight Club", detail.title)
        assertEquals(139, detail.runtime)
        assertEquals(1, detail.genres.size)
        assertEquals(18, detail.genres.first().id)
        assertEquals("Drama", detail.genres.first().name)
        assertEquals(1, detail.productionCompanies.size)
        assertEquals(508, detail.productionCompanies.first().id)
        assertEquals("/7Pz205nTeZAgWvM0qZf8mD7y0Fk.png", detail.productionCompanies.first().logoPath)
        assertEquals("Released", detail.status)
    }

    @Test
    fun `deserialize TvSeriesDetailDto with nested objects and null safety`() {
        val rawJson = """
            {
                "id": 1399,
                "name": "Game of Thrones",
                "tagline": "Winter Is Coming",
                "overview": "Seven noble families...",
                "poster_path": null,
                "backdrop_path": null,
                "first_air_date": "2011-04-17",
                "last_air_date": "2019-05-19",
                "number_of_seasons": 8,
                "number_of_episodes": 73,
                "vote_average": 8.452,
                "vote_count": 22000,
                "genres": [],
                "networks": [
                    { "id": 49, "name": "HBO", "logo_path": null }
                ],
                "status": "Ended"
            }
        """.trimIndent()

        val detail = json.decodeFromString<TvSeriesDetailDto>(rawJson)

        assertEquals(1399, detail.id)
        assertEquals("Game of Thrones", detail.name)
        assertNull(detail.posterPath)
        assertNull(detail.backdropPath)
        assertEquals(8, detail.numberOfSeasons)
        assertEquals(73, detail.numberOfEpisodes)
        assertEquals(0, detail.genres.size)
        assertEquals(1, detail.networks.size)
        assertEquals("HBO", detail.networks.first().name)
        assertNull(detail.networks.first().logoPath)
    }
}
