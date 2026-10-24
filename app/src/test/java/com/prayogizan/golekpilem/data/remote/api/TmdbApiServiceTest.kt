package com.prayogizan.golekpilem.data.remote.api

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class TmdbApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: TmdbApiService

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        apiService = retrofit.create(TmdbApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getMovies issues GET request with correct path, default parameters, and deserializes response`() = runBlocking {
        val jsonBody = """
            {
                "page": 1,
                "results": [
                    {
                        "id": 550,
                        "title": "Fight Club",
                        "poster_path": "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
                        "release_date": "1999-10-15",
                        "vote_average": 8.433
                    }
                ],
                "total_pages": 500,
                "total_results": 10000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonBody))

        val response = apiService.getMovies(page = 1)

        val recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        assertNotNull(recordedRequest)
        assertEquals("GET", recordedRequest?.method)
        assertEquals("/discover/movie?page=1&sort_by=popularity.desc&language=en-US", recordedRequest?.path)

        assertEquals(1, response.page)
        assertEquals(500, response.totalPages)
        assertEquals(1, response.results.size)
        assertEquals(550, response.results.first().id)
        assertEquals("Fight Club", response.results.first().title)
    }

    @Test
    fun `getTvSeries issues GET request with correct path and parameters`() = runBlocking {
        val jsonBody = """
            {
                "page": 2,
                "results": [
                    {
                        "id": 1399,
                        "name": "Game of Thrones",
                        "first_air_date": "2011-04-17",
                        "vote_average": 8.452
                    }
                ],
                "total_pages": 100,
                "total_results": 2000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonBody))

        val response = apiService.getTvSeries(page = 2, sortBy = "vote_count.desc", language = "id-ID")

        val recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        assertNotNull(recordedRequest)
        assertEquals("GET", recordedRequest?.method)
        assertEquals("/discover/tv?page=2&sort_by=vote_count.desc&language=id-ID", recordedRequest?.path)

        assertEquals(2, response.page)
        assertEquals(1, response.results.size)
        assertEquals(1399, response.results.first().id)
        assertEquals("Game of Thrones", response.results.first().name)
    }

    @Test
    fun `getMovieDetail issues GET request with correct path and query parameters`() = runBlocking {
        val jsonBody = """
            {
                "id": 550,
                "title": "Fight Club",
                "tagline": "Mischief. Mayhem. Soap.",
                "overview": "An insomniac...",
                "genres": [{ "id": 18, "name": "Drama" }],
                "production_companies": [{ "id": 508, "name": "Regency", "logo_path": "/regency.png" }],
                "status": "Released"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonBody))

        val detail = apiService.getMovieDetail(movieId = 550)

        val recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        assertNotNull(recordedRequest)
        assertEquals("GET", recordedRequest?.method)
        assertEquals("/movie/550?append_to_response=credits", recordedRequest?.path)

        assertEquals(550, detail.id)
        assertEquals("Fight Club", detail.title)
        assertEquals("Mischief. Mayhem. Soap.", detail.tagline)
        assertEquals(1, detail.genres.size)
        assertEquals("Drama", detail.genres.first().name)
    }

    @Test
    fun `getTvSeriesDetail issues GET request with correct path and query parameters`() = runBlocking {
        val jsonBody = """
            {
                "id": 1399,
                "name": "Game of Thrones",
                "tagline": "Winter Is Coming",
                "overview": "Seven noble families...",
                "number_of_seasons": 8,
                "number_of_episodes": 73,
                "genres": [{ "id": 10765, "name": "Sci-Fi & Fantasy" }],
                "networks": [{ "id": 49, "name": "HBO", "logo_path": "/hbo.png" }],
                "status": "Ended"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonBody))

        val detail = apiService.getTvSeriesDetail(seriesId = 1399)

        val recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        assertNotNull(recordedRequest)
        assertEquals("GET", recordedRequest?.method)
        assertEquals("/tv/1399?append_to_response=credits", recordedRequest?.path)

        assertEquals(1399, detail.id)
        assertEquals("Game of Thrones", detail.name)
        assertEquals(8, detail.numberOfSeasons)
        assertEquals(73, detail.numberOfEpisodes)
        assertEquals(1, detail.networks.size)
        assertEquals("HBO", detail.networks.first().name)
    }
}
