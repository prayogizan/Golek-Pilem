package com.prayogizan.golekpilem.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.prayogizan.golekpilem.data.local.converter.Converters
import com.prayogizan.golekpilem.data.local.dao.MediaDao
import com.prayogizan.golekpilem.data.local.dao.MediaDetailDao
import com.prayogizan.golekpilem.data.local.dao.RemoteKeyDao
import com.prayogizan.golekpilem.data.local.dao.WatchlistDao
import com.prayogizan.golekpilem.data.local.entity.MediaDetailEntity
import com.prayogizan.golekpilem.data.local.entity.MediaItemEntity
import com.prayogizan.golekpilem.data.local.entity.RemoteKeyEntity
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity

/**
 * Primary AndroidX Room Database for Golek Pilem.
 *
 * Encapsulates the relational tables supporting offline catalog browsing, pagination cursor tracking,
 * media detail caching, and personal watchlists.
 */
@Database(
    entities = [
        MediaItemEntity::class,
        RemoteKeyEntity::class,
        MediaDetailEntity::class,
        WatchlistEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GolekPilemDatabase : RoomDatabase() {

    /**
     * Provides access to [MediaDao] operations on the `media_catalog` table.
     */
    abstract fun mediaDao(): MediaDao

    /**
     * Provides access to [RemoteKeyDao] operations on the `remote_keys` table.
     */
    abstract fun remoteKeyDao(): RemoteKeyDao

    /**
     * Provides access to [MediaDetailDao] operations on the `media_detail` table.
     */
    abstract fun mediaDetailDao(): MediaDetailDao

    /**
     * Provides access to [WatchlistDao] operations on the `watchlist` table.
     */
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        /**
         * Physical database file name on the Android device filesystem.
         */
        const val DATABASE_NAME = "golek_pilem.db"
    }
}
