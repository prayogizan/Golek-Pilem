package com.prayogizan.golekpilem.core.di

import com.prayogizan.golekpilem.data.remote.api.TmdbApiService
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import retrofit2.Retrofit

class AppModuleTest : KoinTest {

    @Before
    fun setUp() {
        startKoin {
            modules(appModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify appModule resolves dependencies provided by included modules`() {
        val retrofit: Retrofit = get()
        assertNotNull("Retrofit should be resolved via appModule included networkModule", retrofit)

        val apiService: TmdbApiService = get()
        assertNotNull("TmdbApiService should be resolved via appModule included dataModule", apiService)
    }
}
