package com.prayogizan.golekpilem.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.prayogizan.golekpilem.data.local.entity.RemoteKeyEntity

/**
 * Data Access Object managing pagination cursor keys in the `remote_keys` table.
 *
 * Coordinates cursor resolution for Paging 3 [androidx.paging.RemoteMediator] during
 * append, prepend, and refresh synchronization operations.
 */
@Dao
interface RemoteKeyDao {

    /**
     * Resolves the pagination cursor entity for a specific media item.
     *
     * @param id The unique identifier of the media item.
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     * @return The matching [RemoteKeyEntity], or `null` if not found.
     */
    @Query("SELECT * FROM remote_keys WHERE id = :id AND mediaType = :mediaType")
    suspend fun getRemoteKey(id: Int, mediaType: String): RemoteKeyEntity?

    /**
     * Inserts a list of pagination keys, replacing any existing matching records.
     *
     * @param keys The collection of [RemoteKeyEntity] instances to store.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(keys: List<RemoteKeyEntity>)

    /**
     * Deletes all pagination cursor records for the given [mediaType].
     *
     * @param mediaType The media classification identifier ("MOVIE" or "TV").
     */
    @Query("DELETE FROM remote_keys WHERE mediaType = :mediaType")
    suspend fun clearKeys(mediaType: String): Int
}
