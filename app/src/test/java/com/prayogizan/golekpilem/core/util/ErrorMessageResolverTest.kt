package com.prayogizan.golekpilem.core.util

import com.prayogizan.golekpilem.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMessageResolverTest {

    private fun createHttpException(code: Int, message: String = "Error"): HttpException {
        val mediaType = "application/json".toMediaType()
        val response = Response.error<String>(code, message.toResponseBody(mediaType))
        return HttpException(response)
    }

    @Test
    fun `resolve classifies UnknownHostException as NetworkOffline`() {
        val error = ErrorMessageResolver.resolve(UnknownHostException("No address associated"))

        assertEquals(ErrorType.NetworkOffline, error)
        assertEquals(R.string.error_no_internet_title, error.titleRes)
        assertEquals(R.string.error_no_internet, error.messageRes)
        assertTrue(error.isNetworkIssue)
    }

    @Test
    fun `resolve classifies ConnectException as NetworkOffline`() {
        val error = ErrorMessageResolver.resolve(ConnectException("Connection refused"))

        assertEquals(ErrorType.NetworkOffline, error)
        assertEquals(R.string.error_no_internet_title, error.titleRes)
        assertEquals(R.string.error_no_internet, error.messageRes)
        assertTrue(error.isNetworkIssue)
    }

    @Test
    fun `resolve classifies SocketTimeoutException as NetworkOffline`() {
        val error = ErrorMessageResolver.resolve(SocketTimeoutException("Read timed out"))

        assertEquals(ErrorType.NetworkOffline, error)
        assertEquals(R.string.error_no_internet_title, error.titleRes)
        assertEquals(R.string.error_no_internet, error.messageRes)
        assertTrue(error.isNetworkIssue)
    }

    @Test
    fun `resolve classifies general IOException as NetworkOffline`() {
        val error = ErrorMessageResolver.resolve(IOException("Network dropped"))

        assertEquals(ErrorType.NetworkOffline, error)
        assertEquals(R.string.error_no_internet_title, error.titleRes)
        assertEquals(R.string.error_no_internet, error.messageRes)
        assertTrue(error.isNetworkIssue)
    }

    @Test
    fun `resolve classifies HTTP 401 as Unauthorized`() {
        val error = ErrorMessageResolver.resolve(createHttpException(401, "Invalid API key"))

        assertEquals(ErrorType.Unauthorized, error)
        assertEquals(R.string.error_auth_title, error.titleRes)
        assertEquals(R.string.error_unauthorized, error.messageRes)
    }

    @Test
    fun `resolve classifies HTTP 429 as RateLimited`() {
        val error = ErrorMessageResolver.resolve(createHttpException(429, "Too Many Requests"))

        assertEquals(ErrorType.RateLimited, error)
        assertEquals(R.string.error_rate_limit_title, error.titleRes)
        assertEquals(R.string.error_rate_limited, error.messageRes)
    }

    @Test
    fun `resolve classifies HTTP 500 through 599 as ServerUnavailable`() {
        val error500 = ErrorMessageResolver.resolve(createHttpException(500, "Internal Server Error"))
        val error503 = ErrorMessageResolver.resolve(createHttpException(503, "Service Unavailable"))

        assertEquals(ErrorType.ServerUnavailable, error500)
        assertEquals(R.string.error_server_title, error500.titleRes)
        assertEquals(R.string.error_server_unavailable, error500.messageRes)

        assertEquals(ErrorType.ServerUnavailable, error503)
        assertEquals(R.string.error_server_title, error503.titleRes)
        assertEquals(R.string.error_server_unavailable, error503.messageRes)
    }

    @Test
    fun `resolve classifies unhandled HTTP status code as Generic`() {
        val error = ErrorMessageResolver.resolve(createHttpException(404, "Not Found"))

        assertTrue(error is ErrorType.Generic)
        assertEquals(R.string.error_default_title, error.titleRes)
        assertEquals(R.string.error_default_message, error.messageRes)
    }

    @Test
    fun `resolve classifies null or arbitrary exceptions as Generic`() {
        val nullError = ErrorMessageResolver.resolve(null)
        val genericError = ErrorMessageResolver.resolve(IllegalStateException("Something went wrong"))

        assertTrue(nullError is ErrorType.Generic)
        assertTrue(genericError is ErrorType.Generic)
        assertEquals("Something went wrong", (genericError as ErrorType.Generic).customMessage)
    }
}
