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

import com.prayogizan.golekpilem.core.connectivity.ConnectivityObserver
import com.prayogizan.golekpilem.core.connectivity.NetworkConnectivityObserver
import com.prayogizan.golekpilem.core.network.RateLimitRetryInterceptor
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext

/**
 * Core network dependency injection module providing TMDB HTTP networking infrastructure.
 *
 * Provides singletons for:
 * - [Json]: Pre-configured JSON deserializer.
 * - [HttpLoggingInterceptor]: HTTP request and response logger.
 * - [AuthInterceptor]: TMDB Bearer authentication header injector.
 * - [RateLimitRetryInterceptor]: HTTP 429 exponential backoff retry handler.
 * - [OkHttpClient]: HTTP transport client with timeouts and interceptor chain.
 * - [Retrofit]: REST adapter targeted at the TMDB API v3 base URL.
 * - [ConnectivityObserver]: Reactive system network capability observer.
 * - [ImageLoader]: Global Coil 3 image engine configured with a persistent 250MB disk cache.
 */
val networkModule = module {

    single<ConnectivityObserver> {
        NetworkConnectivityObserver(context = androidContext())
    }

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
        RateLimitRetryInterceptor()
    }

    single {
        OkHttpClient.Builder()
            .connectTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(get<RateLimitRetryInterceptor>())
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

    single<ImageLoader> {
        val context = androidContext()
        ImageLoader.Builder(context)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .crossfade(300)
            .build()
    }
}


