package com.prayogizan.golekpilem.core.di

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class AuthInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private val testToken = "test_tmdb_token_12345"

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `intercept dispatches request with Authorization Bearer header`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val authInterceptor = AuthInterceptor(testToken)
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/movie/popular"))
            .build()

        val response = client.newCall(request).execute()
        response.close()

        val recordedRequest = mockWebServer.takeRequest(5, TimeUnit.SECONDS)
        assertNotNull(recordedRequest)

        val authHeader = recordedRequest?.getHeader("Authorization")
        assertEquals("Bearer $testToken", authHeader)
    }
}
