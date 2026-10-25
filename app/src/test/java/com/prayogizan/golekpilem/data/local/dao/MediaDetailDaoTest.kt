package com.prayogizan.golekpilem.data.local.dao

import com.prayogizan.golekpilem.data.local.BaseRoomTest
import com.prayogizan.golekpilem.data.local.entity.MediaDetailEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit test suite verifying [MediaDetailDao] operations inside an in-memory Room database.
 *
 * Validates entity insertion, retrieval by composite primary key (id, mediaType), conflict replacement,
 * reactive flow emission, deletion, and TTL-based cache pruning.
 */
class MediaDetailDaoTest : BaseRoomTest() {

    @Test
    fun insertDetailAndGetDetailRetrievesPersistedDetail() = runBlocking {
        val detail = createDetail(id = 550, mediaType = "MOVIE", title = "Fight Club")

        mediaDetailDao.insertDetail(detail)

        val retrieved = mediaDetailDao.getDetail(550, "MOVIE")
        assertNotNull(retrieved)
        assertEquals(550, retrieved?.id)
        assertEquals("MOVIE", retrieved?.mediaType)
        assertEquals("Fight Club", retrieved?.title)
    }

    @Test
    fun getDetailReturnsNullForNonExistentOrMismatchedType() = runBlocking {
        val detail = createDetail(id = 550, mediaType = "MOVIE", title = "Fight Club")
        mediaDetailDao.insertDetail(detail)

        assertNull(mediaDetailDao.getDetail(999, "MOVIE"))
        assertNull(mediaDetailDao.getDetail(550, "TV"))
    }

    @Test
    fun insertDetailReplacesConflictingEntity() = runBlocking {
        val initial = createDetail(id = 101, mediaType = "MOVIE", title = "Initial Title", voteAverage = 7.0)
        val updated = createDetail(id = 101, mediaType = "MOVIE", title = "Updated Title", voteAverage = 8.5)

        mediaDetailDao.insertDetail(initial)
        mediaDetailDao.insertDetail(updated)

        val result = mediaDetailDao.getDetail(101, "MOVIE")
        assertNotNull(result)
        assertEquals("Updated Title", result?.title)
        assertEquals(8.5, result?.voteAverage ?: 0.0, 0.01)
    }

    @Test
    fun observeDetailEmitsUpdatesReactively() = runBlocking {
        val initialFlowItem = mediaDetailDao.observeDetail(200, "MOVIE").first()
        assertNull(initialFlowItem)

        val detail = createDetail(id = 200, mediaType = "MOVIE", title = "Interstellar")
        mediaDetailDao.insertDetail(detail)

        val updatedFlowItem = mediaDetailDao.observeDetail(200, "MOVIE").first()
        assertNotNull(updatedFlowItem)
        assertEquals("Interstellar", updatedFlowItem?.title)
    }

    @Test
    fun deleteDetailRemovesEntry() = runBlocking {
        val detail = createDetail(id = 300, mediaType = "TV", title = "Breaking Bad")
        mediaDetailDao.insertDetail(detail)

        val deletedCount = mediaDetailDao.deleteDetail(300, "TV")
        assertEquals(1, deletedCount)

        assertNull(mediaDetailDao.getDetail(300, "TV"))
    }

    @Test
    fun purgeExpiredDetailsPrunesStaleRecordsAndPreservesValidRecords() = runBlocking {
        val currentTime = 1_000_000L
        // Expired: cachedAt (100_000) + ttl (100_000) = 200_000 < currentTime (1_000_000)
        val expired = createDetail(id = 1, mediaType = "MOVIE", cachedAt = 100_000L, ttlMillis = 100_000L)
        // Valid: cachedAt (900_000) + ttl (200_000) = 1_100_000 > currentTime (1_000_000)
        val valid = createDetail(id = 2, mediaType = "MOVIE", cachedAt = 900_000L, ttlMillis = 200_000L)

        mediaDetailDao.insertDetail(expired)
        mediaDetailDao.insertDetail(valid)

        val purgedCount = mediaDetailDao.purgeExpiredDetails(currentTime)
        assertEquals(1, purgedCount)

        assertNull(mediaDetailDao.getDetail(1, "MOVIE"))
        assertNotNull(mediaDetailDao.getDetail(2, "MOVIE"))
    }

    private fun createDetail(
        id: Int,
        mediaType: String,
        title: String = "Title $id",
        voteAverage: Double = 8.0,
        cachedAt: Long = System.currentTimeMillis(),
        ttlMillis: Long = 86_400_000L
    ): MediaDetailEntity {
        return MediaDetailEntity(
            id = id,
            mediaType = mediaType,
            title = title,
            overview = "Overview $id",
            posterPath = "/poster$id.jpg",
            backdropPath = "/backdrop$id.jpg",
            voteAverage = voteAverage,
            voteCount = 1000,
            releaseDate = "2024-01-01",
            runtimeOrSeasons = "120 min",
            genresJson = "[]",
            status = "Released",
            trailerKey = null,
            trailerTitle = null,
            reviewsJson = "[]",
            cachedAt = cachedAt,
            ttlMillis = ttlMillis
        )
    }
}
