package com.prayogizan.golekpilem.data.mapper

import com.prayogizan.golekpilem.core.constants.ApiConstants
import com.prayogizan.golekpilem.data.local.entity.WatchlistEntity
import com.prayogizan.golekpilem.domain.model.Genre
import com.prayogizan.golekpilem.domain.model.MediaItem
import com.prayogizan.golekpilem.domain.model.MovieDetail
import com.prayogizan.golekpilem.domain.model.TvSeriesDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchlistMapperTest {

    @Test
    fun `WatchlistEntity toMediaItem maps all attributes with CDN URLs and release year`() {
        val entity = WatchlistEntity(
            id = 550,
            mediaType = "MOVIE",
            title = "Fight Club",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            backdropPath = "/hZkgoQYus5vegHoetLkCJzb17zJ.jpg",
            voteAverage = 8.432,
            releaseDate = "1999-10-15",
            overview = "An insomniac office worker...",
            addedAt = 1700000000000L
        )

        val mediaItem = entity.toMediaItem()

        assertEquals(550, mediaItem.id)
        assertEquals("MOVIE", mediaItem.mediaType)
        assertEquals("Fight Club", mediaItem.title)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w342/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", mediaItem.posterUrl)
        assertEquals("${ApiConstants.IMAGE_BASE_URL}w780/hZkgoQYus5vegHoetLkCJzb17zJ.jpg", mediaItem.backdropUrl)
        assertEquals("1999-10-15", mediaItem.releaseDate)
        assertEquals("1999", mediaItem.releaseYear)
        assertEquals(8.4, mediaItem.voteAverage, 0.01)
        assertEquals("An insomniac office worker...", mediaItem.overview)
        assertEquals(1700000000000L, mediaItem.addedAt)
    }

    @Test
    fun `WatchlistEntity toMediaItem handles null and blank artwork defensively`() {
        val entity = WatchlistEntity(
            id = 551,
            mediaType = "MOVIE",
            title = "Unknown Film",
            posterPath = "   ",
            backdropPath = null,
            voteAverage = 0.0,
            releaseDate = null,
            overview = "",
            addedAt = 0L
        )

        val mediaItem = entity.toMediaItem()

        assertEquals(551, mediaItem.id)
        assertNull(mediaItem.posterUrl)
        assertNull(mediaItem.backdropUrl)
        assertNull(mediaItem.releaseDate)
        assertEquals("N/A", mediaItem.releaseYear)
        assertEquals(0.0, mediaItem.voteAverage, 0.01)
    }

    @Test
    fun `MediaItem toEntity maps properties and extracts relative paths`() {
        val item = MediaItem(
            id = 1396,
            mediaType = "tv",
            title = "Breaking Bad",
            posterUrl = "${ApiConstants.IMAGE_BASE_URL}w342/ggFHVNu6YYI5L9pCfOacjizRGt.jpg",
            backdropUrl = "${ApiConstants.IMAGE_BASE_URL}w780/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg",
            releaseDate = "2008-01-20",
            releaseYear = "2008",
            voteAverage = 8.88,
            overview = "A chemistry teacher diagnosed with cancer...",
            addedAt = 1600000000000L
        )

        val entity = item.toEntity()

        assertEquals(1396, entity.id)
        assertEquals("TV", entity.mediaType)
        assertEquals("Breaking Bad", entity.title)
        assertEquals("/ggFHVNu6YYI5L9pCfOacjizRGt.jpg", entity.posterPath)
        assertEquals("/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg", entity.backdropPath)
        assertEquals("2008-01-20", entity.releaseDate)
        assertEquals(8.9, entity.voteAverage, 0.01)
        assertEquals(1600000000000L, entity.addedAt)
    }

    @Test
    fun `MovieDetail toMediaItem converts detailed movie to persistable MediaItem`() {
        val detail = MovieDetail(
            id = 27205,
            title = "Inception",
            tagline = "Your mind is the scene of the crime.",
            overview = "Cobb steals corporate secrets through dream-sharing tech.",
            posterUrl = "https://image.tmdb.org/t/p/w342/edv5CZvWj09upOsy2Y6IwDhK8bt.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg",
            releaseDate = "2010-07-15",
            runtime = 148,
            voteAverage = 8.364,
            voteCount = 34000,
            genres = listOf(Genre(id = 28, name = "Action")),
            productionCompanies = emptyList(),
            status = "Released"
        )

        val mediaItem = detail.toMediaItem()

        assertEquals(27205, mediaItem.id)
        assertEquals("MOVIE", mediaItem.mediaType)
        assertEquals("Inception", mediaItem.title)
        assertEquals(detail.posterUrl, mediaItem.posterUrl)
        assertEquals(detail.backdropUrl, mediaItem.backdropUrl)
        assertEquals("2010-07-15", mediaItem.releaseDate)
        assertEquals("2010", mediaItem.releaseYear)
        assertEquals(8.4, mediaItem.voteAverage, 0.01)
        assertEquals(detail.overview, mediaItem.overview)
        assertTrue(mediaItem.addedAt > 0L)
    }

    @Test
    fun `TvSeriesDetail toMediaItem converts detailed TV series to persistable MediaItem`() {
        val detail = TvSeriesDetail(
            id = 66732,
            name = "Stranger Things",
            tagline = "Every ending has a beginning.",
            overview = "When a young boy vanishes...",
            posterUrl = "https://image.tmdb.org/t/p/w342/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/56v2KjBlU4XaOv9rVYEQypROD7P.jpg",
            firstAirDate = "2016-07-15",
            lastAirDate = "2022-07-01",
            numberOfSeasons = 4,
            numberOfEpisodes = 34,
            voteAverage = 8.63,
            voteCount = 16000,
            genres = listOf(Genre(id = 18, name = "Drama")),
            networks = emptyList(),
            status = "Returning Series"
        )

        val mediaItem = detail.toMediaItem()

        assertEquals(66732, mediaItem.id)
        assertEquals("TV", mediaItem.mediaType)
        assertEquals("Stranger Things", mediaItem.title)
        assertEquals(detail.posterUrl, mediaItem.posterUrl)
        assertEquals(detail.backdropUrl, mediaItem.backdropUrl)
        assertEquals("2016-07-15", mediaItem.releaseDate)
        assertEquals("2016", mediaItem.releaseYear)
        assertEquals(8.6, mediaItem.voteAverage, 0.01)
        assertTrue(mediaItem.addedAt > 0L)
    }

    @Test
    fun `extractRelativePath handles TMDB CDN urls and relative paths correctly`() {
        assertEquals("/poster.jpg", extractRelativePath("https://image.tmdb.org/t/p/w342/poster.jpg"))
        assertEquals("/backdrop.jpg", extractRelativePath("https://image.tmdb.org/t/p/w780/backdrop.jpg"))
        assertEquals("/already_relative.jpg", extractRelativePath("/already_relative.jpg"))
        assertNull(extractRelativePath(null))
        assertNull(extractRelativePath(""))
        assertNull(extractRelativePath("   "))
    }
}
