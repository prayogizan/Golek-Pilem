package com.prayogizan.golekpilem.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp [Interceptor] that automatically detects HTTP 429 (Too Many Requests) rate-limiting responses
 * and retries the request using exponential backoff or the server-provided `Retry-After` header.
 *
 * Designed to handle TMDB API rate throttling transparently at the transport layer before errors
 * propagate to repository layers or UI components.
 *
 * @property maxRetries Maximum number of sequential retry attempts before failing (default: 3).
 * @property initialBackoffMs Initial sleep duration in milliseconds before the first retry (default: 1000ms).
 * @property sleeper Lambda used to pause thread execution; injected to facilitate deterministic unit testing.
 */
class RateLimitRetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialBackoffMs: Long = 1000L,
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var retryCount = 0
        var currentBackoffMs = initialBackoffMs

        while (response.code == 429 && retryCount < maxRetries) {
            retryCount++
            response.close()

            val retryAfterHeader = response.header("Retry-After")
            val sleepDurationMs = retryAfterHeader?.toLongOrNull()?.times(1000L)
                ?: currentBackoffMs

            try {
                sleeper(sleepDurationMs)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Interrupted while executing exponential backoff for rate limit", e)
            }

            currentBackoffMs *= 2
            response = chain.proceed(request)
        }

        return response
    }
}
