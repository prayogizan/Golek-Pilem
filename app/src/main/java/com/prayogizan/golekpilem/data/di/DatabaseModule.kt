package com.prayogizan.golekpilem.data.di

import androidx.room.Room
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.MediaDetailDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.dao.WatchlistDao
import com.prayogizan.golekpilem.data.local.db.GolekPilemDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Dependency injection module providing local storage singletons via Room and Koin.
 *
 * Provides singletons for:
 * - [GolekPilemDatabase]: Primary Room database instance built using application context.
 * - [MediaDao]: DAO for catalog feed items.
 * - [RemoteKeyDao]: DAO for pagination keys.
 * - [MediaDetailDao]: DAO for media details.
 * - [WatchlistDao]: DAO for user watchlists.
 */
val databaseModule = module {
    single<GolekPilemDatabase> {
        Room.databaseBuilder(
            androidContext(),
            GolekPilemDatabase::class.java,
            GolekPilemDatabase.DATABASE_NAME
        ).build()
    }

    single<MediaDao> { get<GolekPilemDatabase>().mediaDao() }
    single<RemoteKeyDao> { get<GolekPilemDatabase>().remoteKeyDao() }
    single<MediaDetailDao> { get<GolekPilemDatabase>().mediaDetailDao() }
    single<WatchlistDao> { get<GolekPilemDatabase>().watchlistDao() }
}
