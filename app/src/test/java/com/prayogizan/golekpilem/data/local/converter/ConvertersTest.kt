package com.prayogizan.golekpilem.data.local.converter

import com.prayogizan.golekpilem.data.local.model.ReviewLocalModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    @Test
    fun `fromStringList returns valid json string for list of strings`() {
        val input = listOf("Action", "Drama", "Sci-Fi")
        val json = converters.fromStringList(input)

        assertTrue(json.contains("Action"))
        assertTrue(json.contains("Drama"))
        assertTrue(json.contains("Sci-Fi"))

        val result = converters.toStringList(json)
        assertEquals(input, result)
    }

    @Test
    fun `fromStringList handles empty and null lists safely`() {
        val emptyResult = converters.fromStringList(emptyList())
        assertEquals("[]", emptyResult)
        assertEquals(emptyList<String>(), converters.toStringList(emptyResult))

        val nullResult = converters.fromStringList(null)
        assertEquals("[]", nullResult)
        assertEquals(emptyList<String>(), converters.toStringList(null))
    }

    @Test
    fun `toStringList handles corrupted json without throwing exceptions`() {
        val corruptedJson = "{ not a valid json list"
        val result = converters.toStringList(corruptedJson)
        assertTrue(result.isEmpty())

        val blankResult = converters.toStringList("   ")
        assertTrue(blankResult.isEmpty())
    }

    @Test
    fun `fromReviewList and toReviewList round trip maintains data integrity`() {
        val reviews = listOf(
            ReviewLocalModel(
                id = "rev_1",
                author = "MovieBuff",
                content = "Masterpiece! 10/10.",
                rating = 9.5,
                avatarUrl = "/avatar1.jpg",
                createdAt = "2026-01-15"
            ),
            ReviewLocalModel(
                id = "rev_2",
                author = "Anonymous",
                content = "Could be better, but great visual effects.",
                rating = null,
                avatarUrl = null,
                createdAt = "2026-02-01"
            )
        )

        val json = converters.fromReviewList(reviews)
        val result = converters.toReviewList(json)

        assertEquals(2, result.size)
        assertEquals(reviews[0], result[0])
        assertEquals(reviews[1], result[1])
    }

    @Test
    fun `fromReviewList handles empty and null lists safely`() {
        val emptyJson = converters.fromReviewList(emptyList())
        assertEquals("[]", emptyJson)
        assertTrue(converters.toReviewList(emptyJson).isEmpty())

        val nullJson = converters.fromReviewList(null)
        assertEquals("[]", nullJson)
        assertTrue(converters.toReviewList(nullJson).isEmpty())
    }

    @Test
    fun `toReviewList handles corrupted json gracefully`() {
        val corruptedJson = "[{\"id\": 123, invalid"
        val result = converters.toReviewList(corruptedJson)
        assertTrue(result.isEmpty())

        val blankResult = converters.toReviewList("")
        assertTrue(blankResult.isEmpty())
    }
}
