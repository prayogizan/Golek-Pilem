package com.prayogizan.golekpilem.core.di

import com.prayogizan.golekpilem.BuildConfig
import com.prayogizan.golekpilem.core.constants.ApiConstants
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Core network dependency injection module providing TMDB HTTP networking infrastructure.
 *
 * Provides singletons for:
 * - [Json]: Pre-configured JSON deserializer.
 * - [HttpLoggingInterceptor]: HTTP request and response logger.
 * - [AuthInterceptor]: TMDB Bearer authentication header injector.
 * - [OkHttpClient]: HTTP transport client with timeouts and interceptor chain.
 * - [Retrofit]: REST adapter targeted at the TMDB API v3 base URL.
 */
val networkModule = module {

    single(named("TmdbAccessToken")) {
        BuildConfig.TMDB_ACCESS_TOKEN
    }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    single {
        AuthInterceptor(token = get(named("TmdbAccessToken")))
    }

    single {
        OkHttpClient.Builder()
            .connectTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory(contentType))
            .build()
    }
}
