package com.prayogizan.golekpilem

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.prayogizan.golekpilem.core.di.appModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Main application class responsible for global dependency injection initialization
 * and Coil 3 singleton image loader provision.
 */
class GolekPilemApp : Application(), SingletonImageLoader.Factory, KoinComponent {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@GolekPilemApp)
            modules(appModule)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return get<ImageLoader>()
    }
}

