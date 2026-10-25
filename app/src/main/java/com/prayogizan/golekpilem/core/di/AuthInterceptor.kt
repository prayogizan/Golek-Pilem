package com.prayogizan.golekpilem.core.di

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp [Interceptor] that appends the TMDB API Read Access Token as a Bearer token
 * to the `Authorization` header of all outgoing network requests.
 *
 * @property token The TMDB API Read Access Token retrieved from secure build configurations.
 */
class AuthInterceptor(
    private val token: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithAuth = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(requestWithAuth)
    }
}
