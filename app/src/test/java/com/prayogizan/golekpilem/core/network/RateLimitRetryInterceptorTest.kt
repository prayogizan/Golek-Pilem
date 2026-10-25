package com.prayogizan.golekpilem.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RateLimitRetryInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private val recordedSleeps = mutableListOf<Long>()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        recordedSleeps.clear()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `intercept returns 200 immediately without retrying on successful response`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}"))

        val client = OkHttpClient.Builder()
            .addInterceptor(RateLimitRetryInterceptor(maxRetries = 3, sleeper = { recordedSleeps.add(it) }))
            .build()

        val response = client.newCall(Request.Builder().url(mockWebServer.url("/test")).build()).execute()

        assertEquals(200, response.code)
        assertEquals(1, mockWebServer.requestCount)
        assertEquals(0, recordedSleeps.size)
        response.close()
    }

    @Test
    fun `intercept retries on 429 response using exponential backoff and succeeds on 200`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(429).setBody("Rate limited"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}"))

        val client = OkHttpClient.Builder()
            .addInterceptor(
                RateLimitRetryInterceptor(
                    maxRetries = 3,
                    initialBackoffMs = 500L,
                    sleeper = { recordedSleeps.add(it) }
                )
            )
            .build()

        val response = client.newCall(Request.Builder().url(mockWebServer.url("/test")).build()).execute()

        assertEquals(200, response.code)
        assertEquals(2, mockWebServer.requestCount)
        assertEquals(listOf(500L), recordedSleeps)
        response.close()
    }

    @Test
    fun `intercept respects Retry-After header in seconds if provided`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(429)
                .setHeader("Retry-After", "2")
                .setBody("Rate limited")
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}"))

        val client = OkHttpClient.Builder()
            .addInterceptor(
                RateLimitRetryInterceptor(
                    maxRetries = 3,
                    initialBackoffMs = 500L,
                    sleeper = { recordedSleeps.add(it) }
                )
            )
            .build()

        val response = client.newCall(Request.Builder().url(mockWebServer.url("/test")).build()).execute()

        assertEquals(200, response.code)
        assertEquals(2, mockWebServer.requestCount)
        assertEquals(listOf(2000L), recordedSleeps)
        response.close()
    }

    @Test
    fun `intercept stops retrying and returns 429 when max retries exceeded`() {
        repeat(4) {
            mockWebServer.enqueue(MockResponse().setResponseCode(429).setBody("Still rate limited"))
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(
                RateLimitRetryInterceptor(
                    maxRetries = 3,
                    initialBackoffMs = 100L,
                    sleeper = { recordedSleeps.add(it) }
                )
            )
            .build()

        val response = client.newCall(Request.Builder().url(mockWebServer.url("/test")).build()).execute()

        assertEquals(429, response.code)
        assertEquals(4, mockWebServer.requestCount)
        assertEquals(listOf(100L, 200L, 400L), recordedSleeps)
        response.close()
    }
}
