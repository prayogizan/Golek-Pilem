package com.prayogizan.golekpilem.core.di

import com.prayogizan.golekpilem.core.constants.ApiConstants
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.get
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class NetworkModuleTest : KoinTest {

    @Before
    fun setUp() {
        startKoin {
            modules(networkModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify networkModule provides singleton Json instance`() {
        val json1: Json = get()
        val json2: Json = get()

        assertNotNull(json1)
        assertSame("Json should be a singleton", json1, json2)
        assertTrue(json1.configuration.ignoreUnknownKeys)
        assertTrue(json1.configuration.isLenient)
        assertTrue(json1.configuration.coerceInputValues)
    }

    @Test
    fun `verify networkModule provides singleton OkHttpClient with configured timeouts and interceptors`() {
        val client1: OkHttpClient = get()
        val client2: OkHttpClient = get()

        assertNotNull(client1)
        assertSame("OkHttpClient should be a singleton", client1, client2)
        assertEquals(15000, client1.connectTimeoutMillis)
        assertEquals(15000, client1.readTimeoutMillis)
        assertEquals(15000, client1.writeTimeoutMillis)

        val hasAuthInterceptor = client1.interceptors.any { it is AuthInterceptor }
        val hasLoggingInterceptor = client1.interceptors.any { it is HttpLoggingInterceptor }

        assertTrue("OkHttpClient should contain AuthInterceptor", hasAuthInterceptor)
        assertTrue("OkHttpClient should contain HttpLoggingInterceptor", hasLoggingInterceptor)
    }

    @Test
    fun `verify networkModule provides singleton Retrofit with correct base URL`() {
        val retrofit1: Retrofit = get()
        val retrofit2: Retrofit = get()

        assertNotNull(retrofit1)
        assertSame("Retrofit should be a singleton", retrofit1, retrofit2)
        assertEquals(ApiConstants.BASE_URL, retrofit1.baseUrl().toString())
    }

    @Test
    fun `verify networkModule provides TmdbAccessToken`() {
        val token: String = get(named("TmdbAccessToken"))
        assertNotNull(token)
    }
}
