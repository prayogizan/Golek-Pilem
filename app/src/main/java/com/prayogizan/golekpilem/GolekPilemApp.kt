package com.prayogizan.golekpilem

import android.app.Application
import com.prayogizan.golekpilem.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Main application class responsible for global dependency injection initialization.
 */
class GolekPilemApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@GolekPilemApp)
            modules(appModule)
        }
    }
}
